-- 切换到e2ee_chat数据库
\c e2ee_chat

-- 获取用户最近会话列表函数
CREATE OR REPLACE FUNCTION get_recent_sessions(
    p_user_id UUID
) RETURNS TABLE(
    session_id UUID,
    initiator_id UUID,
    participant_id UUID,
    created_at TIMESTAMPTZ,
    message_counter BIGINT,
    last_message_id UUID,
    last_message_at TIMESTAMPTZ
)
SECURITY DEFINER
AS $$
BEGIN
    -- 更新最后在线时间
    PERFORM update_last_online(p_user_id);
    
    -- 返回会话列表
    RETURN QUERY
    SELECT s.session_id,
           s.initiator_id,
           s.participant_id,
           s.created_at,
           s.message_counter,
           s.last_message_id,
           s.last_message_at
    FROM chat_sessions s
    WHERE s.initiator_id = p_user_id 
       OR s.participant_id = p_user_id
    ORDER BY s.last_message_at DESC NULLS LAST;
END;
$$ LANGUAGE plpgsql;

-- 获取会话未读消息数函数
CREATE OR REPLACE FUNCTION get_unread_count(
    p_session_id UUID,
    p_user_id UUID
) RETURNS INTEGER
SECURITY DEFINER
AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*)::INTEGER INTO v_count
    FROM chat_messages m
    WHERE m.session_id = p_session_id
      AND m.receiver_id = p_user_id
      AND m.is_read = FALSE;
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- 获取会话首条未读消息ID函数
CREATE OR REPLACE FUNCTION get_first_unread(
    p_user_id UUID,
    p_session_id UUID
) RETURNS UUID
SECURITY DEFINER
AS $$
DECLARE
    v_message_id UUID;
BEGIN
    -- 尝试获取第一条未读消息
    SELECT m.message_id INTO v_message_id
    FROM chat_messages m
    WHERE m.session_id = p_session_id
      AND m.receiver_id = p_user_id
      AND m.is_read = FALSE
    ORDER BY m.cursor
    LIMIT 1;
    
    -- 如果没有未读消息，返回最后一条消息ID
    IF v_message_id IS NULL THEN
        SELECT m.message_id INTO v_message_id
        FROM chat_messages m
        WHERE m.session_id = p_session_id
        ORDER BY m.cursor DESC
        LIMIT 1;
    END IF;
    
    RETURN v_message_id;
END;
$$ LANGUAGE plpgsql;

-- 获取指定游标之前的消息函数
CREATE OR REPLACE FUNCTION get_messages_before(
    p_user_id UUID,
    p_session_id UUID,
    p_cursor BIGINT DEFAULT -1,
    p_limit INTEGER DEFAULT 50
) RETURNS TABLE(
    message_id UUID,
    cursor BIGINT,
    is_sender BOOLEAN,
    is_system BOOLEAN,
    is_read BOOLEAN,
    message_iv BYTEA,
    message_content BYTEA
)
SECURITY DEFINER
AS $$
BEGIN
    -- 更新最后在线时间
    PERFORM update_last_online(p_user_id);
    
    -- 标记要返回的接收消息为已读
    UPDATE chat_messages
    SET is_read = TRUE
    WHERE session_id = p_session_id
      AND receiver_id = p_user_id
      AND is_read = FALSE
      AND (p_cursor = -1 OR cursor <= p_cursor)
      AND cursor >= (
          SELECT COALESCE(MIN(cursor), 0)
          FROM (
              SELECT cursor
              FROM chat_messages
              WHERE session_id = p_session_id
                AND (p_cursor = -1 OR cursor <= p_cursor)
              ORDER BY cursor DESC
              LIMIT p_limit
          ) sub
      );
    
    -- 返回消息列表
    RETURN QUERY
    SELECT m.message_id,
           m.cursor,
           m.sender_id = p_user_id,
           m.is_system,
           m.is_read,
           m.message_iv,
           m.message_content
    FROM chat_messages m
    WHERE m.session_id = p_session_id
      AND (p_cursor = -1 OR m.cursor <= p_cursor)
    ORDER BY m.cursor DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 获取指定游标之后的消息函数
CREATE OR REPLACE FUNCTION get_messages_after(
    p_user_id UUID,
    p_session_id UUID,
    p_cursor BIGINT DEFAULT -1,
    p_limit INTEGER DEFAULT 50
) RETURNS TABLE(
    message_id UUID,
    cursor BIGINT,
    is_sender BOOLEAN,
    is_system BOOLEAN,
    is_read BOOLEAN,
    message_iv BYTEA,
    message_content BYTEA
)
SECURITY DEFINER
AS $$
BEGIN
    -- 更新最后在线时间
    PERFORM update_last_online(p_user_id);
    
    -- 标记要返回的接收消息为已读
    UPDATE chat_messages
    SET is_read = TRUE
    WHERE session_id = p_session_id
      AND receiver_id = p_user_id
      AND is_read = FALSE
      AND (p_cursor = -1 OR cursor >= p_cursor)
      AND cursor <= (
          SELECT COALESCE(MAX(cursor), 0)
          FROM (
              SELECT cursor
              FROM chat_messages
              WHERE session_id = p_session_id
                AND (p_cursor = -1 OR cursor >= p_cursor)
              ORDER BY cursor
              LIMIT p_limit
          ) sub
      );
    
    -- 返回消息列表
    RETURN QUERY
    SELECT m.message_id,
           m.cursor,
           m.sender_id = p_user_id,
           m.is_system,
           m.is_read,
           m.message_iv,
           m.message_content
    FROM chat_messages m
    WHERE m.session_id = p_session_id
      AND (p_cursor = -1 OR m.cursor >= p_cursor)
    ORDER BY m.cursor
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 发送消息函数
CREATE OR REPLACE FUNCTION send_message(
    p_user_id UUID,
    p_session_id UUID,
    p_message_iv BYTEA,
    p_message_content BYTEA,
    p_is_system BOOLEAN DEFAULT FALSE
) RETURNS BOOLEAN
SECURITY DEFINER
AS $$
DECLARE
    v_session RECORD;
    v_new_cursor BIGINT;
    v_message_id UUID;
BEGIN
    -- 获取并锁定会话信息
    SELECT * INTO v_session
    FROM chat_sessions
    WHERE session_id = p_session_id
    FOR UPDATE;
    
    -- 验证会话存在且用户有权限
    IF v_session.session_id IS NULL OR 
       (v_session.initiator_id != p_user_id AND v_session.participant_id != p_user_id) THEN
        RETURN FALSE;
    END IF;
    
    -- 递增消息计数器
    UPDATE chat_sessions
    SET message_counter = message_counter + 1
    WHERE session_id = p_session_id
    RETURNING message_counter INTO v_new_cursor;
    
    -- 插入新消息
    INSERT INTO chat_messages (
        message_id,
        session_id,
        cursor,
        sender_id,
        receiver_id,
        message_iv,
        message_content,
        is_system
    ) VALUES (
        gen_random_uuid(),
        p_session_id,
        v_new_cursor,
        p_user_id,
        CASE 
            WHEN v_session.initiator_id = p_user_id THEN v_session.participant_id
            ELSE v_session.initiator_id
        END,
        p_message_iv,
        p_message_content,
        p_is_system
    )
    RETURNING message_id INTO v_message_id;
    
    -- 更新会话最后消息信息
    UPDATE chat_sessions
    SET last_message_id = v_message_id,
        last_message_at = CURRENT_TIMESTAMP
    WHERE session_id = p_session_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 添加函数注释
COMMENT ON FUNCTION get_recent_sessions IS '获取用户最近会话列表，按最后消息时间降序排序';
COMMENT ON FUNCTION get_unread_count IS '获取会话中用户的未读消息数量';
COMMENT ON FUNCTION get_first_unread IS '获取会话中用户的首条未读消息ID，若全部已读则返回最后消息ID';
COMMENT ON FUNCTION get_messages_before IS '获取指定游标之前的消息，自动标记接收消息为已读';
COMMENT ON FUNCTION get_messages_after IS '获取指定游标之后的消息，自动标记接收消息为已读';
COMMENT ON FUNCTION send_message IS '发送新消息，自动更新会话信息';
