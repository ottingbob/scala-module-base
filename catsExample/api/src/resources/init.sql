CREATE TABLE IF NOT EXISTS kv_items (
	id VARCHAR(32) NOT NULL,
	description TEXT,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP,
	PRIMARY KEY (id)
)
