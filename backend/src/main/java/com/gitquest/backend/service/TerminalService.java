package com.gitquest.backend.service;

import com.gitquest.backend.dto.terminal.SessionCreateResponse;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.BranchRef;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.CommitNode;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.GraphData;
import com.gitquest.backend.entity.Mission;
import com.gitquest.backend.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TerminalService {

    private final MissionRepository missionRepository;

    public TerminalService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    private record PendingSession(Path workDir, String setupMessage) {}
    private record PtySession(Path workDir, Process process, Path resizeFifo) {}

    private final Map<String, PendingSession>   pendingSessions = new ConcurrentHashMap<>();
    private final Map<String, PtySession>       activeSessions  = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> wsSessions      = new ConcurrentHashMap<>();

    // ptyhelper.py のパス (初回抽出後にキャッシュ)
    private volatile Path helperScriptPath;

    // ────────────────────────────────────────────
    // セッション作成 (HTTP POST)
    // ────────────────────────────────────────────

    public SessionCreateResponse createSession(String missionId) {
        String sessionId = UUID.randomUUID().toString();
        try {
            Path workDir = Files.createTempDirectory("gitquest-" + sessionId.substring(0, 8));
            String setupMessage = setupMissionEnvironment(workDir, missionId);
            pendingSessions.put(sessionId, new PendingSession(workDir, setupMessage));
            return new SessionCreateResponse(sessionId, setupMessage);
        } catch (IOException e) {
            throw new IllegalStateException("セッションの作成に失敗しました", e);
        }
    }

    // ────────────────────────────────────────────
    // WebSocket 接続時に PTY を起動
    // ────────────────────────────────────────────

    public void attachWebSocket(String sessionId, WebSocketSession ws) {
        PendingSession pending = pendingSessions.remove(sessionId);
        if (pending == null) return;

        try {
            Path workDir  = pending.workDir();
            Path initFile = workDir.resolve(".bash_init");
            Files.writeString(initFile, buildInitScript(pending.setupMessage()));

            // resize 用 FIFO を作成
            Path resizeFifo = workDir.resolve(".resize_pipe");
            new ProcessBuilder("mkfifo", resizeFifo.toString())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start().waitFor();

            // ptyhelper.py をクラスパスから抽出
            Path helper = getHelperScript();

            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("TERM",                  "xterm-256color");
            env.put("COLORTERM",             "truecolor");
            env.put("FORCE_COLOR",           "1");
            env.put("GIT_PAGER",             "cat");
            env.put("GIT_AUTHOR_NAME",       "GitQuest User");
            env.put("GIT_AUTHOR_EMAIL",      "user@gitquest.local");
            env.put("GIT_COMMITTER_NAME",    "GitQuest User");
            env.put("GIT_COMMITTER_EMAIL",   "user@gitquest.local");

            // ptyhelper.py: bash を PTY 内で起動し stdin/stdout を仲介する
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", helper.toString(),
                    initFile.toString(),
                    resizeFifo.toString(),
                    "200", "50");
            pb.environment().putAll(env);
            pb.directory(workDir.toFile());

            Process process = pb.start();
            activeSessions.put(sessionId, new PtySession(workDir, process, resizeFifo));
            wsSessions.put(sessionId, ws);

            // PTY → WebSocket のストリーミング
            Thread.ofVirtual().start(() -> streamOutput(sessionId, process, ws));

        } catch (Exception e) {
            sendError(ws, "PTY の起動に失敗しました: " + e.getMessage());
        }
    }

    // PTY 出力を WebSocket に転送し続ける
    private void streamOutput(String sessionId, Process process, WebSocketSession ws) {
        byte[] buf = new byte[4096];
        InputStream in = process.getInputStream();
        try {
            int n;
            while ((n = in.read(buf)) != -1) {
                if (!ws.isOpen()) break;
                byte[] chunk = Arrays.copyOf(buf, n);
                synchronized (ws) {
                    ws.sendMessage(new BinaryMessage(ByteBuffer.wrap(chunk)));
                }
            }
        } catch (IOException ignored) {
        } finally {
            deleteSession(sessionId);
        }
    }

    // ────────────────────────────────────────────
    // PTY 操作
    // ────────────────────────────────────────────

    public void writeToSession(String sessionId, byte[] data) {
        PtySession session = activeSessions.get(sessionId);
        if (session == null) return;
        try {
            OutputStream out = session.process().getOutputStream();
            out.write(data);
            out.flush();
        } catch (IOException ignored) {}
    }

    public void resizeSession(String sessionId, int cols, int rows) {
        PtySession session = activeSessions.get(sessionId);
        if (session == null) return;
        // FIFO への書き込みはブロックする可能性があるためバーチャルスレッドで実行
        Thread.ofVirtual().start(() -> {
            try {
                byte[] data = (cols + " " + rows + "\n").getBytes();
                try (FileOutputStream fos = new FileOutputStream(session.resizeFifo().toFile())) {
                    fos.write(data);
                }
            } catch (IOException ignored) {}
        });
    }

    // ────────────────────────────────────────────
    // グラフ取得 (HTTP GET)
    // ────────────────────────────────────────────

    public GraphData getGraph(String sessionId) {
        PtySession session = activeSessions.get(sessionId);
        if (session != null) return buildGraph(session.workDir());

        PendingSession pending = pendingSessions.get(sessionId);
        if (pending != null) return buildGraph(pending.workDir());

        return emptyGraph();
    }

    // ────────────────────────────────────────────
    // ミッション完了判定 (HTTP GET)
    // ────────────────────────────────────────────

    public boolean checkMission(String sessionId, String missionId) {
        Path workDir = findWorkDir(sessionId);
        if (workDir == null || missionId == null) return false;
        try {
            Mission mission = missionRepository.findById(UUID.fromString(missionId)).orElse(null);
            if (mission == null) return false;
            return checkByLevel(workDir, mission.getLevel(), mission.getOrderIndex());
        } catch (Exception e) {
            return false;
        }
    }

    // リポジトリの実状態 + コマンド履歴で完了条件を判定する
    private boolean checkByLevel(Path workDir, int level, int order) throws IOException, InterruptedException {
        boolean hasRepo = Files.exists(workDir.resolve(".git"));

        if (level == 1) {
            return switch (order) {
                case 1 -> hasRepo;
                // ステージ済みファイルがある (先にコミットまで進めた場合も OK とする)
                case 2 -> hasRepo && (!runCapture(workDir, "git diff --cached --name-only").isBlank()
                        || countCommits(workDir) >= 1);
                default -> hasRepo && countCommits(workDir) >= 1;
            };
        }

        if (level == 2) {
            if (!hasRepo) return false;
            return switch (order) {
                case 1 -> hasBranchOtherThanMain(workDir);
                case 2 -> {
                    String cur = runCapture(workDir, "git rev-parse --abbrev-ref HEAD").trim();
                    yield !cur.isBlank() && !cur.equals("main") && !cur.equals("master") && !cur.equals("HEAD");
                }
                // fast-forward マージも検出できるよう「feature が main に含まれたか」で判定
                default -> runCapture(workDir,
                        "git merge-base --is-ancestor feature main 2>/dev/null && echo yes || echo no")
                        .trim().endsWith("yes");
            };
        }

        if (level == 3) {
            // 確認系コマンドは実行履歴 (.bash_history) で判定する
            String history = readHistory(workDir);
            return switch (order) {
                case 1 -> history.contains("git log");
                case 2 -> history.contains("git diff");
                default -> history.contains("git status");
            };
        }

        return false;
    }

    private Path findWorkDir(String sessionId) {
        PtySession session = activeSessions.get(sessionId);
        if (session != null) return session.workDir();
        PendingSession pending = pendingSessions.get(sessionId);
        return pending != null ? pending.workDir() : null;
    }

    private String readHistory(Path workDir) {
        try {
            Path hist = workDir.resolve(".bash_history");
            return Files.exists(hist) ? Files.readString(hist) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private int countCommits(Path workDir) throws IOException, InterruptedException {
        String out = runCapture(workDir, "git rev-list --all --count 2>/dev/null");
        try {
            return Integer.parseInt(out.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean hasBranchOtherThanMain(Path workDir) throws IOException, InterruptedException {
        String out = runCapture(workDir, "git branch '--format=%(refname:short)'");
        for (String line : out.split("\n")) {
            String name = line.trim();
            if (!name.isBlank() && !name.equals("main") && !name.equals("master")) return true;
        }
        return false;
    }

    // ────────────────────────────────────────────
    // セッション削除
    // ────────────────────────────────────────────

    public void deleteSession(String sessionId) {
        pendingSessions.remove(sessionId);
        wsSessions.remove(sessionId);

        PtySession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.process().destroy();
            deleteDirectory(session.workDir());
        }
    }

    // ────────────────────────────────────────────
    // 内部ユーティリティ
    // ────────────────────────────────────────────

    private Path getHelperScript() throws IOException {
        if (helperScriptPath != null && Files.exists(helperScriptPath)) {
            return helperScriptPath;
        }
        InputStream src = getClass().getClassLoader().getResourceAsStream("ptyhelper.py");
        if (src == null) throw new IOException("ptyhelper.py がクラスパスに見つかりません");

        Path tmp = Files.createTempFile("ptyhelper-", ".py");
        Files.copy(src, tmp, StandardCopyOption.REPLACE_EXISTING);
        tmp.toFile().setExecutable(true);
        helperScriptPath = tmp;
        return tmp;
    }

    private String buildInitScript(String setupMessage) {
        String escapedMsg = setupMessage.replace("'", "'\"'\"'");
        // HISTFILE: 実行したコマンドを作業ディレクトリに記録し、ミッション完了判定に使う
        return """
                export TERM=xterm-256color COLORTERM=truecolor FORCE_COLOR=1 GIT_PAGER=cat
                export GIT_AUTHOR_NAME='GitQuest User' GIT_AUTHOR_EMAIL='user@gitquest.local'
                export GIT_COMMITTER_NAME='GitQuest User' GIT_COMMITTER_EMAIL='user@gitquest.local'
                export HISTFILE="$PWD/.bash_history"
                shopt -s histappend
                PROMPT_COMMAND='history -a'
                PS1='\\[\\e[32m\\]\\w\\[\\e[0m\\] \\[\\e]9999;ready\\a\\]\\$ '
                printf '\\033[32m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\033[0m\\n'
                printf '\\033[1;32m  GitQuest ターミナル (本物の Git 環境)\\033[0m\\n'
                printf '\\033[32m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\033[0m\\n'
                printf '\\033[33m%%s\\033[0m\\n' '%s'
                printf '\\033[32m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\033[0m\\n\\n'
                """.formatted(escapedMsg);
    }

    private String setupMissionEnvironment(Path workDir, String missionId) {
        if (missionId == null) return "git init からはじめてみよう。";
        try {
            Mission mission = missionRepository.findById(UUID.fromString(missionId)).orElse(null);
            if (mission == null) return "git init からはじめてみよう。";
            return setupByLevel(workDir, mission.getLevel(), mission.getOrderIndex());
        } catch (Exception e) {
            return "git init からはじめてみよう。";
        }
    }

    private String setupByLevel(Path workDir, int level, int order) throws IOException, InterruptedException {
        if (level == 1 && order == 1)
            return "このフォルダを Git リポジトリとして初期化してください。";

        Files.writeString(workDir.resolve("README.md"),
                "# GitQuest へようこそ\n\nここで Git を学びましょう。\n");
        runSetup(workDir, "git init");

        if (level == 1 && order == 2)
            return "README.md が作成されています。\nこのファイルをステージングエリアに追加してください。";

        runSetup(workDir, "git add .");

        if (level == 1 && order == 3)
            return "ファイルがステージ済みの状態です。\n現在の変更をコミットして記録してください。";

        runSetup(workDir, "git commit -m \"initial commit\"");

        if (level == 2 && order == 1)
            return "1 つコミットがある状態です。\nここから新しいブランチを作成してください。";

        runSetup(workDir, "git branch feature");

        if (level == 2 && order == 2)
            return "feature ブランチが作成されています。\nそのブランチに切り替えて作業を開始してください。";

        runSetup(workDir, "git checkout feature");
        Files.writeString(workDir.resolve("feature.txt"), "feature ブランチのファイル\n");
        runSetup(workDir, "git add .");
        runSetup(workDir, "git commit -m \"add feature file\"");
        runSetup(workDir, "git checkout main");

        if (level == 2 && order == 3)
            return "feature ブランチに変更が加わっています (現在 main にいます)。\nそのブランチの変更を main に取り込んでください。";

        runSetup(workDir, "git merge feature");
        Files.writeString(workDir.resolve("README.md"),
                "# GitQuest へようこそ\n\nここで Git を学びましょう。\n\n## 更新\n\nこの変更はまだステージされていません。\n");

        return switch (order) {
            case 1 -> "2 つのコミットがある状態です。\nこれまでの変更履歴を確認してください。";
            case 2 -> "README.md に未ステージの変更があります。\n何が変わったのかを確認してください。";
            default -> "README.md に未ステージの変更があります。\nリポジトリの現在の状態を確認してください。";
        };
    }

    private void runSetup(Path workDir, String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "/bin/sh", "-c",
                "GIT_AUTHOR_NAME='GitQuest User' GIT_AUTHOR_EMAIL='user@gitquest.local' "
                + "GIT_COMMITTER_NAME='GitQuest User' GIT_COMMITTER_EMAIL='user@gitquest.local' "
                + command);
        pb.directory(workDir.toFile());
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start().waitFor();
    }

    private GraphData buildGraph(Path workDir) {
        try {
            if (!Files.exists(workDir.resolve(".git"))) return emptyGraph();

            String logOut = runCapture(workDir,
                    "git log --all --format='%H|%h|%s|%an|%ci|%P'");
            List<CommitNode> commits = new ArrayList<>();
            if (!logOut.isBlank()) {
                for (String line : logOut.split("\n")) {
                    String[] p = line.split("\\|", -1);
                    if (p.length < 5) continue;
                    List<String> parents = p.length >= 6 && !p[5].isBlank()
                            ? Arrays.asList(p[5].trim().split(" ")) : List.of();
                    commits.add(new CommitNode(p[0], p[1], p[2], p[3], p[4], parents));
                }
            }

            String branchOut = runCapture(workDir,
                    "git branch -a '--format=%(refname:short)|%(objectname)|%(HEAD)'");
            List<BranchRef> branches = new ArrayList<>();
            if (!branchOut.isBlank()) {
                for (String line : branchOut.split("\n")) {
                    String[] p = line.split("\\|", -1);
                    if (p.length < 3) continue;
                    branches.add(new BranchRef(p[0].trim(), p[1].trim(), "*".equals(p[2].trim())));
                }
            }

            String head = runCapture(workDir, "git rev-parse HEAD 2>/dev/null || echo ''").trim();
            return new GraphData(commits, branches, head, true);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return emptyGraph();
        }
    }

    private String runCapture(Path workDir, String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out;
        try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            out = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
        }
        p.waitFor();
        return out;
    }

    private GraphData emptyGraph() {
        return new GraphData(List.of(), List.of(), "", false);
    }

    private void sendError(WebSocketSession ws, String msg) {
        try {
            synchronized (ws) {
                ws.sendMessage(new BinaryMessage(
                        ("\r\n\033[31m" + msg + "\033[0m\r\n").getBytes()));
            }
        } catch (IOException ignored) {}
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        } catch (IOException ignored) {}
    }
}
