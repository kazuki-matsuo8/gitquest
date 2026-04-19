# Issue 完了コマンド

現在の作業をコミット・PR 作成・マージ・Issue クローズまで一括で行う。

## 使い方
/done {コミットメッセージ} {Issue番号}

## 実行内容
1. git add -A
2. git commit -m "{メッセージ} (#{Issue番号})"
3. git push
4. gh pr create
5. gh pr merge --squash --delete-branch
6. git checkout main && git pull
