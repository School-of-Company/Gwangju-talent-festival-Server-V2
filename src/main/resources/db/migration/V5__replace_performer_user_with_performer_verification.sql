RENAME TABLE performer_user TO performer_user_legacy;

CREATE TABLE performer_verification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    participant_name VARCHAR(100) NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    claimed_user_id BIGINT,
    claimed_at DATETIME(6),
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_performer_verification_code_hash UNIQUE (code_hash),
    CONSTRAINT uk_performer_verification_claimed_user UNIQUE (claimed_user_id),
    CONSTRAINT fk_performer_verification_user FOREIGN KEY (claimed_user_id) REFERENCES users (id)
) ENGINE=InnoDB;
