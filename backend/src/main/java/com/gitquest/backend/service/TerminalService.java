package com.gitquest.backend.service;

import com.gitquest.backend.dto.terminal.TerminalCommandResponse;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.BranchRef;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.CommitNode;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.GraphData;
import com.gitquest.backend.entity.Mission;
import com.gitquest.backend.repository.MissionRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TerminalService {

    private final Map<String, Path> sessions = new ConcurrentHashMap<>();
    private final MissionRepository missionRepository;

    public TerminalService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    // 許可コマンドプレフィックス（セキュリティ: 学習に必要なコマンドのみ）
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "git init", "git add", "git commit", "git branch",
            "git checkout", "git switch", "git merge", "git log",
            "git diff", "git status", "git reset", "git restore",
            "ls", "touch", "echo", "mkdir", "cat", "rm"
    );

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        try {
            Path workDir = Files.createTempDirectory("gitquest-" + sessionId.substring(0, 8));
            sessions.put(sessionId, workDir);
            return sessionId;
        } catch (IOException e) {
            throw new IllegalStateException("セッションの作成に失敗しました", e);
        }
    }

    public void deleteSession(String sessionId) {
        Path workDir = sessions.remove(sessionId);
        if (workDir != null) {
            deleteDirectory(workDir);
        }
    }

    public TerminalCommandResponse execute(String sessionId, String command, String missionId) {
        Path workDir = sessions.get(sessionId);
        if (workDir == null) {
            return new TerminalCommandResponse("セッションが見つかりません", false, emptyGraph(), false);
        }

        String trimmed = command.trim();
        if (!isAllowed(trimmed)) {
            return new TerminalCommandResponse(
                    "このコマンドは使用できません。git コマンドや基本的なファイル操作を入力してください。",
                    false,
                    emptyGraph(),
                    false
            );
        }

        try {
            String output = runCommand(workDir, trimmed);
            GraphData graph = buildGraph(workDir);
            boolean completed = missionId != null && checkMissionCompleted(workDir, graph, trimmed, missionId);
            return new TerminalCommandResponse(output, true, graph, completed);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TerminalCommandResponse("コマンドの実行に失敗しました: " + e.getMessage(), false, emptyGraph(), false);
        }
    }

    // ミッションの完了条件を満たしているか判定する
    private boolean checkMissionCompleted(Path workDir, GraphData graph, String command, String missionIdStr) {
        try {
            UUID missionId = UUID.fromString(missionIdStr);
            Mission mission = missionRepository.findById(missionId).orElse(null);
            if (mission == null) return false;

            int level = mission.getLevel();
            int order = mission.getOrderIndex();

            return switch (level) {
                case 1 -> switch (order) {
                    // git init → .git ディレクトリが存在すればOK
                    case 1 -> Files.exists(workDir.resolve(".git"));
                    // git add → ステージされたファイルがあればOK
                    case 2 -> hasStagedFiles(workDir);
                    // git commit → コミットが1件以上あればOK
                    case 3 -> !graph.commits().isEmpty();
                    default -> false;
                };
                case 2 -> switch (order) {
                    // ブランチを作ろう → main/master 以外のブランチが存在する
                    case 1 -> graph.branches().stream()
                            .anyMatch(b -> !b.name().equals("main") && !b.name().equals("master")
                                    && !b.name().startsWith("HEAD"));
                    // ブランチを切り替えよう → 現在のブランチが main/master でない
                    case 2 -> {
                        String currentBranch = getCurrentBranch(workDir);
                        yield currentBranch != null && !currentBranch.equals("main") && !currentBranch.equals("master");
                    }
                    // ブランチをマージしよう → main/master にコミットが2件以上（マージ後）
                    case 3 -> {
                        long mainCommits = graph.commits().stream()
                                .filter(c -> !c.parents().isEmpty())
                                .count();
                        yield graph.commits().size() >= 2 && mainCommits >= 1;
                    }
                    default -> false;
                };
                case 3 -> switch (order) {
                    // git log を実行したか（コマンドプレフィックスで判定）
                    case 1 -> command.startsWith("git log");
                    // git diff を実行したか
                    case 2 -> command.startsWith("git diff");
                    // git status を実行したか
                    case 3 -> command.startsWith("git status");
                    default -> false;
                };
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasStagedFiles(Path workDir) throws IOException, InterruptedException {
        String status = runCommand(workDir, "git status --porcelain");
        if (status.equals("(出力なし)")) return false;
        // 行頭が A, M, D など（インデックスに変更がある行）があればステージ済み
        return Arrays.stream(status.split("\n"))
                .anyMatch(line -> line.length() >= 2 && line.charAt(0) != ' ' && line.charAt(0) != '?');
    }

    private String getCurrentBranch(Path workDir) {
        try {
            String result = runCommand(workDir, "git rev-parse --abbrev-ref HEAD");
            return result.equals("(出力なし)") ? null : result.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String runCommand(Path workDir, String command) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("/bin/sh");
        args.add("-c");

        String envPrefix = "GIT_AUTHOR_NAME='GitQuest User' GIT_AUTHOR_EMAIL='user@gitquest.local' "
                + "GIT_COMMITTER_NAME='GitQuest User' GIT_COMMITTER_EMAIL='user@gitquest.local' ";
        args.add(envPrefix + command);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        process.waitFor();
        return output.isBlank() ? "(出力なし)" : output;
    }

    private GraphData buildGraph(Path workDir) {
        try {
            if (!Files.exists(workDir.resolve(".git"))) {
                return emptyGraph();
            }

            String logFormat = "--format=%H|%h|%s|%an|%ci|%P";
            String logOutput = runCommand(workDir, "git log --all \"" + logFormat + "\"");

            List<CommitNode> commits = new ArrayList<>();
            if (!logOutput.equals("(出力なし)") && !logOutput.isBlank()) {
                for (String line : logOutput.split("\n")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 5) continue;
                    List<String> parents = parts.length >= 6 && !parts[5].isBlank()
                            ? Arrays.asList(parts[5].trim().split(" "))
                            : List.of();
                    commits.add(new CommitNode(parts[0], parts[1], parts[2], parts[3], parts[4], parents));
                }
            }

            String branchOutput = runCommand(workDir,
                    "git branch -a '--format=%(refname:short)|%(objectname)|%(HEAD)'");
            List<BranchRef> branches = new ArrayList<>();
            if (!branchOutput.equals("(出力なし)") && !branchOutput.isBlank()) {
                for (String line : branchOutput.split("\n")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 3) continue;
                    branches.add(new BranchRef(parts[0].trim(), parts[1].trim(), "*".equals(parts[2].trim())));
                }
            }

            String head = runCommand(workDir, "git rev-parse HEAD 2>/dev/null || echo ''").trim();
            if (head.equals("(出力なし)")) head = "";

            return new GraphData(commits, branches, head);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return emptyGraph();
        }
    }

    private boolean isAllowed(String command) {
        String lower = command.toLowerCase();
        return ALLOWED_COMMANDS.stream().anyMatch(lower::startsWith);
    }

    private GraphData emptyGraph() {
        return new GraphData(List.of(), List.of(), "");
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}
