ALTER TABLE interaction_events
    ADD CONSTRAINT uq_interaction_session_seq UNIQUE (session_id, seq);
