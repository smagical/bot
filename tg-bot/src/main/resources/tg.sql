CREATE TABLE IF NOT EXISTS tg_messages(
    id BIGINT NOT NULL ,
    chat_id BIGINT NOT NULL ,
    album BIGINT DEFAULT 0,
    message TEXT NOT NULL ,
    link TEXT NOT NULL ,
    other tsvector,
    PRIMARY KEY (id,chat_id)
);


CREATE INDEX IF NOT EXISTS tg_message_gin ON tg_messages USING gin(other);

CREATE TABLE IF NOT EXISTS tg_spider(
    chat_id BIGINT NOT NULL ,
    chat_name TEXT,
    last_spider_id BIGINT DEFAULT 0,
    PRIMARY KEY (chat_id)
);

CREATE TABLE IF NOT EXISTS tg_group(
    chat_id BIGINT NOT NULL ,
    invite_user_id BIGINT,
    chat_name TEXT,
    PRIMARY KEY (chat_id,invite_user_id)
);

CREATE TABLE IF NOT EXISTS tg_bot_info(
    attr_name TEXT,
    attr_value TEXT,
    PRIMARY KEY (attr_name)
);