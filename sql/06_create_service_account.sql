-- 切换到e2ee_chat数据库
\c e2ee_chat

-- 创建机器账号
CREATE USER e2ee_chat_service WITH PASSWORD 'your_password_here';

-- 限制数据库访问
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM e2ee_chat_service;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM e2ee_chat_service;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM e2ee_chat_service;

-- 授予数据库连接权限
GRANT CONNECT ON DATABASE e2ee_chat TO e2ee_chat_service;

-- 授予执行指定存储过程的权限
GRANT EXECUTE ON FUNCTION check_username_available(VARCHAR) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION register_user(VARCHAR, VARCHAR, BYTEA, BYTEA, BYTEA, UUID, TIMESTAMPTZ) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_user_salt(VARCHAR) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION verify_login(UUID, BYTEA) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION create_vault(UUID, BYTEA, BYTEA, BYTEA, BYTEA) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_vault(UUID) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_user_profile(UUID) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION update_last_online(UUID, TIMESTAMPTZ) TO e2ee_chat_service;

GRANT EXECUTE ON FUNCTION get_recent_sessions(UUID) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_unread_count(UUID, UUID) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_first_unread(UUID, UUID) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_messages_before(UUID, UUID, BIGINT, INTEGER) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION get_messages_after(UUID, UUID, BIGINT, INTEGER) TO e2ee_chat_service;
GRANT EXECUTE ON FUNCTION send_message(UUID, UUID, BYTEA, BYTEA, BOOLEAN) TO e2ee_chat_service;

-- 添加注释
COMMENT ON ROLE e2ee_chat_service IS '端到端加密聊天应用的服务账号，仅具有存储过程执行权限';
