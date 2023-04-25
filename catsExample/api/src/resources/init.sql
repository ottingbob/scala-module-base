CREATE TABLE IF NOT EXISTS kv_items (
	-- UUID with dashes
	id 					VARCHAR(36) NOT NULL,
	description TEXT,
	created_at 	TIMESTAMP NOT NULL,
	updated_at 	TIMESTAMP,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS countries (
	code			 VARCHAR(3)  		NOT NULL,
	name       text        		NOT NULL,
	population integer     	 	NOT NULL,
	PRIMARY KEY (code)
);
