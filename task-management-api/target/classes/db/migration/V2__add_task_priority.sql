ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS priority VARCHAR(255) DEFAULT 'MEDIUM';

UPDATE tasks
SET priority = 'MEDIUM'
WHERE priority IS NULL;

ALTER TABLE tasks
    ALTER COLUMN priority SET DEFAULT 'MEDIUM';

ALTER TABLE tasks
    ALTER COLUMN priority SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT ck_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
