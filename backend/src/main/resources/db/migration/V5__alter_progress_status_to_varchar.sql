-- JPA の EnumType.STRING と互換性を持たせるため VARCHAR に変更
ALTER TABLE user_progress ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;
DROP TYPE IF EXISTS progress_status CASCADE;
