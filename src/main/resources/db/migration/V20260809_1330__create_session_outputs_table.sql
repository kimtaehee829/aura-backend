ALTER TABLE session_outputs
    ADD COLUMN aura_code VARCHAR(100) NULL,
    ADD COLUMN accessory_id BIGINT NULL,
    ADD COLUMN object_path VARCHAR(500) NULL;
