CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB;

CREATE TABLE performer_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE team (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_name VARCHAR(255) NOT NULL,
    school VARCHAR(255) NOT NULL,
    team_status VARCHAR(255) NOT NULL,
    team_genre VARCHAR(255) NOT NULL,
    applicant_name VARCHAR(255),
    perform_order INTEGER,
    total_score INTEGER NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE apply (
    id BIGINT NOT NULL AUTO_INCREMENT,
    video_key VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE slogans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slogan TEXT NOT NULL,
    description TEXT NOT NULL,
    school VARCHAR(255),
    grade INTEGER,
    name VARCHAR(255) NOT NULL,
    class_num INTEGER,
    phone_number VARCHAR(255),
    birth_date DATE,
    retry_count INTEGER NOT NULL,
    next_retry_at DATETIME(6),
    last_error TEXT,
    synced_at DATETIME(6),
    sheet_sync_status VARCHAR(255) NOT NULL,
    school_status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE seat_ban (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seat_section VARCHAR(1) NOT NULL,
    seat_number INTEGER NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_seat_ban_section_number UNIQUE (seat_section, seat_number)
) ENGINE=InnoDB;

CREATE TABLE seat (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seat_section VARCHAR(1) NOT NULL,
    seat_number INTEGER NOT NULL,
    user_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_seat_section_number UNIQUE (seat_section, seat_number),
    CONSTRAINT fk_seat_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_seat_user_id ON seat (user_id);

CREATE TABLE judgement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    completeness_expression_score INTEGER NOT NULL,
    creativity_composition_score INTEGER NOT NULL,
    stage_performance_teamwork_score INTEGER NOT NULL,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_judgement_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_judgement_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE,
    CONSTRAINT fk_judgement_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_judgement_user_id ON judgement (user_id);

CREATE TABLE judge_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    strokes JSON NOT NULL,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_judge_comment_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_judge_comment_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE,
    CONSTRAINT fk_judge_comment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE judge_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    affiliation_strokes JSON NOT NULL,
    position_strokes JSON NOT NULL,
    name_strokes JSON NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_judge_profile_user UNIQUE (user_id),
    CONSTRAINT fk_judge_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE anomaly_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    domain VARCHAR(255) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    detected_value FLOAT(53) NOT NULL,
    threshold_value FLOAT(53) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6),
    anomaly_score FLOAT(53),
    model_version VARCHAR(50),
    predicted_label VARCHAR(20),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE incident_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    anomaly_event_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    note VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_feedback_anomaly_event UNIQUE (anomaly_event_id),
    CONSTRAINT fk_feedback_anomaly_event FOREIGN KEY (anomaly_event_id) REFERENCES anomaly_event (id)
) ENGINE=InnoDB;
