CREATE INDEX idx_tasks_user_created_at_id
    ON tasks (user_id, created_at DESC, id ASC);