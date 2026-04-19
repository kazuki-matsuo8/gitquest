CREATE TYPE progress_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');

-- ユーザーごとのミッション進捗
CREATE TABLE user_progress (
    id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID            NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    mission_id   UUID            NOT NULL REFERENCES missions (id) ON DELETE CASCADE,
    status       progress_status NOT NULL DEFAULT 'NOT_STARTED',
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, mission_id)
);

CREATE INDEX idx_user_progress_user ON user_progress (user_id);
