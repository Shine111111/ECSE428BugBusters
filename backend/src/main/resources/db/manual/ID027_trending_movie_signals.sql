-- Run once against the catalog database before deploying the ID027 backend.
-- Keep unknown signals NULL so incomplete movies are excluded from trending.
ALTER TABLE movies ADD COLUMN IF NOT EXISTS comment_count INTEGER;
ALTER TABLE movies ADD COLUMN IF NOT EXISTS view_count INTEGER;
