-- 切换到e2ee_chat数据库
\c e2ee_chat

-- 创建会话表
CREATE TABLE chat_sessions (
    idx BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL DEFAULT gen_random_uuid(),
    initiator_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message_counter BIGINT NOT NULL DEFAULT 0,
    last_message_id UUID NULL DEFAULT NULL,
    last_message_at TIMESTAMPTZ NULL DEFAULT NULL,
    CONSTRAINT uk_chat_sessions_session_id UNIQUE (session_id),
    CONSTRAINT fk_chat_sessions_initiator FOREIGN KEY (initiator_id) 
        REFERENCES user_profiles(user_id),
    CONSTRAINT fk_chat_sessions_participant FOREIGN KEY (participant_id) 
        REFERENCES user_profiles(user_id),
    CONSTRAINT ck_chat_sessions_different_users CHECK (initiator_id != participant_id)
);

-- 创建消息表
CREATE TABLE chat_messages (
    idx BIGSERIAL PRIMARY KEY,
    message_id UUID NOT NULL DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    cursor BIGINT NOT NULL,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    message_iv BYTEA NOT NULL,
    message_content BYTEA NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chat_messages_message_id UNIQUE (message_id),
    CONSTRAINT uk_chat_messages_session_cursor UNIQUE (session_id, cursor),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) 
        REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) 
        REFERENCES user_profiles(user_id),
    CONSTRAINT fk_chat_messages_receiver FOREIGN KEY (receiver_id) 
        REFERENCES user_profiles(user_id),
    CONSTRAINT ck_chat_messages_different_users CHECK (sender_id != receiver_id),
    CONSTRAINT ck_chat_messages_iv_length CHECK (length(message_iv) = 12)
);

-- 添加会话表的最后消息ID外键（需要在消息表创建后添加）
ALTER TABLE chat_sessions ADD CONSTRAINT fk_chat_sessions_last_message 
    FOREIGN KEY (last_message_id) REFERENCES chat_messages(message_id);

-- 创建索引
CREATE INDEX ix_chat_sessions_initiator ON chat_sessions(initiator_id);
CREATE INDEX ix_chat_sessions_participant ON chat_sessions(participant_id);
CREATE UNIQUE INDEX ix_chat_sessions_participants ON chat_sessions(
    LEAST(initiator_id, participant_id),
    GREATEST(initiator_id, participant_id)
);
CREATE INDEX ix_chat_sessions_last_message_at ON chat_sessions(last_message_at DESC NULLS LAST);
CREATE INDEX ix_chat_sessions_initiator_last ON chat_sessions(initiator_id, last_message_at DESC NULLS LAST);
CREATE INDEX ix_chat_sessions_participant_last ON chat_sessions(participant_id, last_message_at DESC NULLS LAST);

CREATE INDEX ix_chat_messages_session ON chat_messages(session_id);
CREATE INDEX ix_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX ix_chat_messages_receiver ON chat_messages(receiver_id);
CREATE INDEX ix_chat_messages_session_cursor ON chat_messages(session_id, cursor DESC);
CREATE INDEX ix_chat_messages_session_sender ON chat_messages(session_id, sender_id, cursor DESC);
CREATE INDEX ix_chat_messages_receiver_read ON chat_messages(receiver_id, is_read, cursor DESC);

-- 添加注释
COMMENT ON TABLE chat_sessions IS '聊天会话表';
COMMENT ON TABLE chat_messages IS '聊天消息表';

COMMENT ON COLUMN chat_sessions.idx IS '自增主键';
COMMENT ON COLUMN chat_sessions.session_id IS '会话唯一标识（UUID）';
COMMENT ON COLUMN chat_sessions.initiator_id IS '会话发起者ID';
COMMENT ON COLUMN chat_sessions.participant_id IS '会话参与者ID';
COMMENT ON COLUMN chat_sessions.created_at IS '会话创建时间';
COMMENT ON COLUMN chat_sessions.message_counter IS '会话消息计数器';
COMMENT ON COLUMN chat_sessions.last_message_id IS '最后一条消息ID';
COMMENT ON COLUMN chat_sessions.last_message_at IS '最后一条消息时间';

COMMENT ON COLUMN chat_messages.idx IS '自增主键';
COMMENT ON COLUMN chat_messages.message_id IS '消息唯一标识（UUID）';
COMMENT ON COLUMN chat_messages.session_id IS '所属会话ID';
COMMENT ON COLUMN chat_messages.cursor IS '会话内消息游标';
COMMENT ON COLUMN chat_messages.sender_id IS '发送者ID';
COMMENT ON COLUMN chat_messages.receiver_id IS '接收者ID';
COMMENT ON COLUMN chat_messages.message_iv IS '消息初始化向量（12字节）';
COMMENT ON COLUMN chat_messages.message_content IS '消息密文';
COMMENT ON COLUMN chat_messages.is_read IS '消息是否已读';
COMMENT ON COLUMN chat_messages.is_system IS '是否为系统消息';
COMMENT ON COLUMN chat_messages.sent_at IS '消息发送时间';
