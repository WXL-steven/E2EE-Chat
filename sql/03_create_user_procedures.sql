-- 切换到e2ee_chat数据库
\c e2ee_chat

-- 检查用户名是否可用的函数
CREATE OR REPLACE FUNCTION check_username_available(
    p_username VARCHAR(16)
) RETURNS BOOLEAN
SECURITY DEFINER
AS $$
BEGIN
    -- 验证用户名格式
    IF p_username !~ '^[a-zA-Z0-9_-]{1,16}$' THEN
        RETURN FALSE;
    END IF;
    
    -- 检查用户名是否已存在
    RETURN NOT EXISTS (
        SELECT 1 FROM user_profiles 
        WHERE username = p_username
    );
END;
$$ LANGUAGE plpgsql;

-- 用户注册函数
CREATE OR REPLACE FUNCTION register_user(
    p_username VARCHAR(16),
    p_display_name VARCHAR(32),
    p_password_hash BYTEA,
    p_password_salt BYTEA,
    p_vault_master_key BYTEA,
    p_user_id UUID DEFAULT gen_random_uuid(),
    p_registered_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
) RETURNS UUID
SECURITY DEFINER
AS $$
DECLARE
    v_success BOOLEAN;
BEGIN
    -- 验证用户名可用性
    IF NOT check_username_available(p_username) THEN
        RETURN NULL;
    END IF;
    
    -- 验证UUID唯一性
    IF EXISTS (SELECT 1 FROM user_profiles WHERE user_id = p_user_id) THEN
        RETURN NULL;
    END IF;
    
    -- 验证输入长度
    IF length(p_password_hash) != 32 OR length(p_password_salt) != 16 OR length(p_vault_master_key) != 32 THEN
        RETURN NULL;
    END IF;
    
    -- 开始事务
    BEGIN
        -- 插入用户资料
        INSERT INTO user_profiles (user_id, username, display_name, public_key, registered_at)
        VALUES (p_user_id, p_username, p_display_name, '\x'::BYTEA, p_registered_at);
        
        -- 插入用户凭证
        INSERT INTO user_credentials (user_id, password_hash, password_salt)
        VALUES (p_user_id, p_password_hash, p_password_salt);
        
        -- 插入用户保险库（仅主密钥）
        INSERT INTO user_vaults (user_id, vault_master_key)
        VALUES (p_user_id, p_vault_master_key);
        
        v_success := TRUE;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN NULL;
    END;
    
    IF v_success THEN
        RETURN p_user_id;
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 获取用户密码盐值函数
CREATE OR REPLACE FUNCTION get_user_salt(
    p_username VARCHAR(16)
) RETURNS TABLE(user_id UUID, password_salt BYTEA)
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT p.user_id, c.password_salt
    FROM user_profiles p
    JOIN user_credentials c ON c.user_id = p.user_id
    WHERE p.username = p_username;
END;
$$ LANGUAGE plpgsql;

-- 验证用户登录函数
CREATE OR REPLACE FUNCTION verify_login(
    p_user_id UUID,
    p_password_hash BYTEA
) RETURNS BOOLEAN
SECURITY DEFINER
AS $$
BEGIN
    -- 验证输入
    IF length(p_password_hash) != 32 THEN
        RETURN FALSE;
    END IF;
    
    -- 验证用户ID和密码散列
    RETURN EXISTS (
        SELECT 1 
        FROM user_credentials
        WHERE user_id = p_user_id 
        AND password_hash = p_password_hash
    );
END;
$$ LANGUAGE plpgsql;

-- 完成用户保险库配置函数
CREATE OR REPLACE FUNCTION create_vault(
    p_user_id UUID,
    p_vault_salt BYTEA,
    p_vault_iv BYTEA,
    p_encrypted_private_key BYTEA,
    p_public_key BYTEA
) RETURNS BOOLEAN
SECURITY DEFINER
AS $$
DECLARE
    v_ready BOOLEAN;
BEGIN
    -- 验证用户ID存在且保险库未就绪
    IF NOT EXISTS (
        SELECT 1 FROM user_vaults v
        JOIN user_profiles p ON p.user_id = v.user_id
        WHERE v.user_id = p_user_id 
        AND v.ready = FALSE
    ) THEN
        RETURN FALSE;
    END IF;
    
    -- 验证输入长度
    IF length(p_vault_salt) != 16 OR 
       length(p_vault_iv) != 12 OR 
       length(p_encrypted_private_key) < 48 OR 
       length(p_encrypted_private_key) > 64
    THEN
        RETURN FALSE;
    END IF;
    
    -- 开始事务
    BEGIN
        -- 更新用户公钥
        UPDATE user_profiles
        SET public_key = p_public_key
        WHERE user_id = p_user_id;
        
        -- 更新保险库并设置ready为true
        UPDATE user_vaults
        SET vault_salt = p_vault_salt,
            vault_iv = p_vault_iv,
            encrypted_private_key = p_encrypted_private_key,
            ready = TRUE
        WHERE user_id = p_user_id
        RETURNING ready INTO v_ready;
        
        IF v_ready IS NULL OR NOT v_ready THEN
            RETURN FALSE;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN FALSE;
    END;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 获取用户保险库信息函数
CREATE OR REPLACE FUNCTION get_vault(
    p_user_id UUID
) RETURNS TABLE(
    vault_master_key BYTEA,
    vault_salt BYTEA,
    vault_iv BYTEA,
    encrypted_private_key BYTEA
)
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT v.vault_master_key,
           v.vault_salt,
           v.vault_iv,
           v.encrypted_private_key
    FROM user_vaults v
    WHERE v.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- 获取用户资料信息函数
CREATE OR REPLACE FUNCTION get_user_profile(
    p_user_id UUID
) RETURNS TABLE(
    user_id UUID,
    username VARCHAR(16),
    display_name VARCHAR(32),
    public_key BYTEA,
    last_online TIMESTAMPTZ,
    registered_at TIMESTAMPTZ
)
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT p.user_id,
           p.username,
           p.display_name,
           p.public_key,
           p.last_online,
           p.registered_at
    FROM user_profiles p
    WHERE p.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- 更新用户最后在线时间函数
CREATE OR REPLACE FUNCTION update_last_online(
    p_user_id UUID,
    p_timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
) RETURNS void
SECURITY DEFINER
AS $$
BEGIN
    UPDATE user_profiles
    SET last_online = p_timestamp
    WHERE user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- 添加函数注释
COMMENT ON FUNCTION check_username_available IS '检查用户名是否可用，验证格式并确保唯一性';
COMMENT ON FUNCTION register_user IS '注册新用户，创建用户资料、凭证和保险库';
COMMENT ON FUNCTION get_user_salt IS '通过用户名获取用户ID和密码盐值';
COMMENT ON FUNCTION verify_login IS '验证用户登录凭证';
COMMENT ON FUNCTION create_vault IS '完成用户保险库配置';
COMMENT ON FUNCTION get_vault IS '获取用户保险库完整信息';
COMMENT ON FUNCTION get_user_profile IS '获取用户完整资料信息';
COMMENT ON FUNCTION update_last_online IS '更新用户最后在线时间，可选指定时间戳';
