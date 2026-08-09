CREATE TABLE session_outputs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    video_status VARCHAR(20) NOT NULL,
    video_url VARCHAR(500),
    video_duration_ms INT,
    thumbnail_url VARCHAR(500),
    soul_tag_url VARCHAR(500),
    landing_url VARCHAR(500),
    qr_image_url VARCHAR(500),
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    aura_code VARCHAR(100),
    accessory_id BIGINT,
    object_path VARCHAR(500),
    
    CONSTRAINT fk_session_outputs_session_id FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT uk_session_outputs_session_id UNIQUE (session_id)
);
