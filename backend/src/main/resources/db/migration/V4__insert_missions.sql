-- Lv.1: Git の基本（init / add / commit）
INSERT INTO missions (id, level, order_index, title, description, hint) VALUES
(gen_random_uuid(), 1, 1, 'リポジトリを初期化しよう', 'git init コマンドを使って、新しい Git リポジトリを作成してください。', 'git init と入力してみよう'),
(gen_random_uuid(), 1, 2, 'ファイルをステージしよう', 'git add コマンドを使って、ファイルをステージングエリアに追加してください。', 'git add . と入力してみよう'),
(gen_random_uuid(), 1, 3, '最初のコミットをしよう', 'git commit コマンドを使って、変更を記録してください。', 'git commit -m "first commit" と入力してみよう');

-- Lv.2: ブランチ操作（branch / checkout / merge）
INSERT INTO missions (id, level, order_index, title, description, hint) VALUES
(gen_random_uuid(), 2, 1, 'ブランチを作ろう', 'git branch コマンドを使って、新しいブランチを作成してください。', 'git branch feature と入力してみよう'),
(gen_random_uuid(), 2, 2, 'ブランチを切り替えよう', 'git checkout コマンドを使って、作成したブランチに切り替えてください。', 'git checkout feature と入力してみよう'),
(gen_random_uuid(), 2, 3, 'ブランチをマージしよう', 'git merge コマンドを使って、feature ブランチを main にマージしてください。', 'git merge feature と入力してみよう');

-- Lv.3: 履歴確認（log / diff / status）
INSERT INTO missions (id, level, order_index, title, description, hint) VALUES
(gen_random_uuid(), 3, 1, 'コミット履歴を見よう', 'git log コマンドでコミットの履歴を確認してください。', 'git log と入力してみよう'),
(gen_random_uuid(), 3, 2, '変更内容を確認しよう', 'git diff コマンドで変更内容を確認してください。', 'git diff と入力してみよう'),
(gen_random_uuid(), 3, 3, '状態を確認しよう', 'git status コマンドで現在の状態を確認してください。', 'git status と入力してみよう');
