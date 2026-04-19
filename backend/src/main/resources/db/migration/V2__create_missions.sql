-- ミッション本体（例: 「Lv.1 コミットしてみよう」）
CREATE TABLE missions (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    level       INT         NOT NULL,           -- レベル番号（1〜5）
    order_index INT         NOT NULL,           -- レベル内の順番
    title       VARCHAR(100) NOT NULL,
    description TEXT        NOT NULL,           -- ミッションの説明
    hint        TEXT,                           -- 詰まったときのヒント
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (level, order_index)
);

-- ミッションのステップ（1ミッションに複数のステップがある）
-- 例: ステップ1「git init を実行」、ステップ2「git add を実行」
CREATE TABLE mission_steps (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id       UUID        NOT NULL REFERENCES missions (id) ON DELETE CASCADE,
    order_index      INT         NOT NULL,          -- ステップの順番
    description      VARCHAR(200) NOT NULL,         -- 「git init を実行してください」
    expected_command VARCHAR(200) NOT NULL,          -- 正解コマンドのパターン（例: "git init"）
    UNIQUE (mission_id, order_index)
);

CREATE INDEX idx_mission_steps_mission ON mission_steps (mission_id);
