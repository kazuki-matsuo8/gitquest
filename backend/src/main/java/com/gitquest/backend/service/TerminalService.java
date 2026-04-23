package com.gitquest.backend.service;

import com.gitquest.backend.dto.terminal.TerminalCommandResponse;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.BranchRef;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.CommitNode;
import com.gitquest.backend.dto.terminal.TerminalCommandResponse.GraphData;
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

    // セッション ID → 作業ディレクトリ のマップ（メモリ管理）
    private final Map<String, Path> sessions = new ConcurrentHashMap<>();

    // 許可するコマンドプレフィックス（セキュリティ: git 以外を弾く）
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "git init", "git add", "git commit", "git branch",
            "git checkout", "git switch", "git merge", "git log",
            "git diff", "git status", "git reset", "git restore", "ls"
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

    public TerminalCommandResponse execute(String sessionId, String command) {
        Path workDir = sessions.get(sessionId);
        if (workDir == null) {
            return new TerminalCommandResponse("セッションが見つかりません", false, emptyGraph());
        }

        String trimmed = command.trim();
        if (!isAllowed(trimmed)) {
            return new TerminalCommandResponse(
                    "このコマンドは使用できません。git コマンドを入力してください。",
                    false,
                    emptyGraph()
            );
        }

        try {
            String output = runCommand(workDir, trimmed);
            GraphData graph = buildGraph(workDir);
            return new TerminalCommandResponse(output, true, graph);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TerminalCommandResponse("コマンドの実行に失敗しました: " + e.getMessage(), false, emptyGraph());
        }
    }

    // コマンドを実行して標準出力 + 標準エラーを結合して返す
    private String runCommand(Path workDir, String command) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("/bin/sh");
        args.add("-c");

        // git commit 時に必要なユーザー情報を環境変数でセット
        String envPrefix = "GIT_AUTHOR_NAME='GitQuest User' GIT_AUTHOR_EMAIL='user@gitquest.local' "
                + "GIT_COMMITTER_NAME='GitQuest User' GIT_COMMITTER_EMAIL='user@gitquest.local' ";
        args.add(envPrefix + command);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true); // stderr を stdout にマージ

        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        process.waitFor();
        return output.isBlank() ? "(出力なし)" : output;
    }

    // git log / git branch からグラフデータを構築
    private GraphData buildGraph(Path workDir) {
        try {
            if (!Files.exists(workDir.resolve(".git"))) {
                return emptyGraph();
            }

            // コミット一覧を取得。フィールド区切りに Unit Separator (\x1f) を使用
            // %H=完全ハッシュ, %h=短縮ハッシュ, %s=件名, %an=作者名, %ci=日時, %P=親ハッシュ群
            String logFormat = "--format='%H|%h|%s|%an|%ci|%P'";
            String logOutput = runCommand(workDir, "git log --all " + logFormat);

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

            // ブランチ一覧を取得。| 区切りで名前・ハッシュ・HEAD マーカーを取得
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

            // HEAD の指すコミットハッシュを取得
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
