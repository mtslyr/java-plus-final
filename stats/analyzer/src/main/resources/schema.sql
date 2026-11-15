CREATE TABLE IF NOT EXISTS user_action_weights (
    user_id          INTEGER          NOT NULL,
    event_id         INTEGER          NOT NULL,
    weight           DOUBLE PRECISION NOT NULL,
    last_interaction TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_user_action_weights_event
    ON user_action_weights (event_id);

CREATE TABLE IF NOT EXISTS event_similarity (
    event_a    INTEGER          NOT NULL,
    event_b    INTEGER          NOT NULL,
    score      DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (event_a, event_b),
    CHECK (event_a < event_b)
);

CREATE INDEX IF NOT EXISTS idx_event_similarity_event_b
    ON event_similarity (event_b);
