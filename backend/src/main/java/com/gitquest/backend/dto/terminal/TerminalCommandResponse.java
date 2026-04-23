package com.gitquest.backend.dto.terminal;

public record TerminalCommandResponse(
        String output,   // コマンドの標準出力 + 標準エラー
        boolean success, // 終了コードが 0 かどうか
        GraphData graph  // コミットグラフ情報
) {
    public record GraphData(
            java.util.List<CommitNode> commits,
            java.util.List<BranchRef> branches,
            String head  // 現在の HEAD が指すコミットハッシュ
    ) {}

    public record CommitNode(
            String hash,
            String shortHash,
            String message,
            String author,
            String timestamp,
            java.util.List<String> parents // 親コミットのハッシュ一覧
    ) {}

    public record BranchRef(
            String name,
            String hash,
            boolean isHead  // 現在チェックアウト中かどうか
    ) {}
}
