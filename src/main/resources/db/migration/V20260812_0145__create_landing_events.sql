CREATE TABLE landing_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    product_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_landing_events_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_landing_events_product FOREIGN KEY (product_id) REFERENCES products(id)
);
