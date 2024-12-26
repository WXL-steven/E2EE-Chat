-- 切换到e2ee_chat数据库
\c e2ee_chat

-- 创建用户资料表
CREATE TABLE user_profiles (
    idx BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(16) NOT NULL,
    display_name VARCHAR(32) NOT NULL,
    public_key BYTEA,
    last_online TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_user_profiles_username UNIQUE (username),
    CONSTRAINT ck_username_ascii CHECK (username ~ '^[[:ascii:]]{1,16}$')
);

-- 创建用户机密表
CREATE TABLE user_credentials (
    idx BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    password_hash BYTEA NOT NULL,
    password_salt BYTEA NOT NULL,
    CONSTRAINT uk_user_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_credentials_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES user_profiles(user_id) 
        ON DELETE CASCADE,
    CONSTRAINT ck_password_hash_length CHECK (length(password_hash) = 32),
    CONSTRAINT ck_password_salt_length CHECK (length(password_salt) = 16)
);

-- 创建用户保险库表
CREATE TABLE user_vaults (
    idx BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    vault_master_key BYTEA NOT NULL,
    vault_salt BYTEA,
    vault_iv BYTEA,
    encrypted_private_key BYTEA,
    ready BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_vaults_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_vaults_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES user_profiles(user_id) 
        ON DELETE CASCADE,
    CONSTRAINT ck_vault_master_key_length CHECK (length(vault_master_key) = 32),
    CONSTRAINT ck_vault_salt_length CHECK (vault_salt IS NULL OR length(vault_salt) = 16),
    CONSTRAINT ck_vault_iv_length CHECK (vault_iv IS NULL OR length(vault_iv) = 12),
    CONSTRAINT ck_encrypted_private_key_length CHECK (encrypted_private_key IS NULL OR length(encrypted_private_key) BETWEEN 48 AND 64),
    CONSTRAINT ck_vault_ready CHECK (
        (ready = FALSE) OR 
        (ready = TRUE AND vault_salt IS NOT NULL AND vault_iv IS NOT NULL AND encrypted_private_key IS NOT NULL)
    )
);

-- 添加注释
COMMENT ON TABLE user_profiles IS '用户资料表';
COMMENT ON TABLE user_credentials IS '用户机密表';
COMMENT ON TABLE user_vaults IS '用户保险库表';

COMMENT ON COLUMN user_profiles.idx IS '自增主键';
COMMENT ON COLUMN user_profiles.user_id IS '用户唯一标识（UUID）';
COMMENT ON COLUMN user_profiles.username IS '用户名（ASCII字符，最长16字符）';
COMMENT ON COLUMN user_profiles.display_name IS '显示名称（UTF-8字符串，最长32字符）';
COMMENT ON COLUMN user_profiles.public_key IS '用户公钥';
COMMENT ON COLUMN user_profiles.last_online IS '最后在线时间';
COMMENT ON COLUMN user_profiles.registered_at IS '注册时间';

COMMENT ON COLUMN user_credentials.idx IS '自增主键';
COMMENT ON COLUMN user_credentials.user_id IS '关联的用户UUID';
COMMENT ON COLUMN user_credentials.password_hash IS '密码散列值（32字节定长）';
COMMENT ON COLUMN user_credentials.password_salt IS '密码盐值（16字节定长）';

COMMENT ON COLUMN user_vaults.idx IS '自增主键';
COMMENT ON COLUMN user_vaults.user_id IS '关联的用户UUID';
COMMENT ON COLUMN user_vaults.vault_master_key IS '保险库主密钥（32字节定长，创建时必需）';
COMMENT ON COLUMN user_vaults.vault_salt IS '保险库盐值（16字节定长，配置时设置）';
COMMENT ON COLUMN user_vaults.vault_iv IS '保险库初始化向量（12字节定长，配置时设置）';
COMMENT ON COLUMN user_vaults.encrypted_private_key IS '加密的用户私钥（48-64字节，配置时设置）';
COMMENT ON COLUMN user_vaults.ready IS '保险库是否配置完成（需要所有加密字段都设置后才能为true）';
