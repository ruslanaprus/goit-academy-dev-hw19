CREATE SEQUENCE IF NOT EXISTS seq_notes_id
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS seq_users_id
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT DEFAULT nextval('seq_users_id'),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    failed_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    CONSTRAINT pk_users_id PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS notes (
    id BIGINT DEFAULT nextval('seq_notes_id'),
    title VARCHAR(255) NOT NULL CHECK (length(title) > 0),
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_notes_id PRIMARY KEY (id),
    CONSTRAINT fk_notes_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX note_user_idx ON notes (user_id);
