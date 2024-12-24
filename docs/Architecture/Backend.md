# 后端架构设计

## 1. 技术栈
- Java EE 8
- PostgreSQL 17.2

## 2. 系统架构

### 2.1 Model层

#### 2.1.1 用户相关实体
- UserProfile（用户资料）
  * idx: BIGSERIAL - 数据库自增主键（内部使用）
  * userId: UUID - 用户唯一标识
  * username: VARCHAR(16) - 用户名（ASCII字符，组合唯一）
  * displayName: VARCHAR(32) - 展示名（UTF-8字符串）
  * publicKey: BYTEA - 用户公钥
  * lastOnline: TIMESTAMPTZ - 最后在线时间
  * registeredAt: TIMESTAMPTZ - 注册时间

- UserCredential（用户机密）
  * idx: BIGSERIAL - 数据库自增主键（内部使用）
  * userId: UUID - 关联用户标识
  * passwordHash: BYTEA - 用户密码散列（32字节）
  * passwordSalt: BYTEA - 用户密码盐（16字节）

- UserVault（用户保险库）
  * idx: BIGSERIAL - 数据库自增主键（内部使用）
  * userId: UUID - 关联用户标识
  * vaultMasterKey: BYTEA - 保险库主密钥（32字节）
  * vaultSalt: BYTEA - 保险库盐（16字节）
  * vaultIV: BYTEA - 保险库初始化向量（12字节）
  * encryptedPrivateKey: BYTEA - 加密的用户私钥（64字节）

### 2.2 Service层
- UserService - 用户管理服务
  * 用户注册与登录验证
  * 密码加密与验证
  * 用户信息管理
- ChatService - 聊天业务服务
  * 会话创建与管理
  * 消息收发处理
  * 未读消息统计
- SecurityService - 安全相关服务
  * 会话token管理
  * 密钥对存储管理
  * XSS防护

### 2.3 Controller层
- AuthController - 认证相关控制器
  * 处理用户注册
  * 处理用户登录
  * 处理会话管理
- ChatController - 聊天相关控制器
  * 处理会话创建
  * 处理消息发送
  * 处理消息轮询
- UserController - 用户相关控制器
  * 处理用户信息查询
  * 处理用户搜索

## 数据访问层（DAO）

### 用户管理（UserDAO）

#### 用户注册与认证
- **用户名检查**
  - 方法：`checkUsernameAvailable(String username)`
  - 功能：验证用户名格式和可用性
  - 实现：调用`check_username_available`存储过程
  - 返回：布尔值表示是否可用

- **用户注册**
  - 方法：`registerUser(UserRegistrationDTO)`
  - 功能：完整的用户注册流程
  - 实现：调用`register_user`存储过程
  - 特点：
    - 支持自定义UUID和注册时间
    - 事务性操作确保数据一致性
    - 包含所有必要的数据验证

- **密码验证流程**
  - 方法1：`getUserSalt(String username)`
    - 功能：获取用户的密码盐值和UUID
    - 实现：调用`get_user_salt`存储过程
    - 返回：可选的UserSaltDTO（包含UUID和盐值）
  
  - 方法2：`verifyLogin(UUID userId, byte[] passwordHash)`
    - 功能：验证用户登录凭证
    - 实现：调用`verify_login`存储过程
    - 特点：分离盐值获取和密码验证，支持客户端哈希

- **用户资料获取**
  - 方法：`getUserProfile(UUID userId)`
  - 功能：获取用户的完整资料信息
  - 实现：调用`get_user_profile`存储过程
  - 返回：可选的UserProfileDTO（包含除idx外的所有用户资料）

### 保险库管理（VaultDAO）

#### 保险库操作
- **创建保险库**
  - 方法：`createVault(VaultCreationDTO)`
  - 功能：完成用户保险库的配置
  - 实现：调用`create_vault`存储过程
  - 特点：
    - 验证保险库未配置状态
    - 确保加密数据格式正确
    - 原子性操作

- **获取保险库**
  - 方法：`getVault(UUID userId)`
  - 功能：获取用户的保险库信息
  - 实现：调用`get_vault`存储过程
  - 特点：
    - 返回保险库信息，无论是否完成配置
    - 未配置的字段将为null
    - 总是返回vault_master_key用于配置

### 数据传输对象（DTO）

#### UserRegistrationDTO
- username: 用户名（ASCII字符串）
- displayName: 显示名称（UTF-8字符串）
- passwordHash: 密码散列（32字节）
- passwordSalt: 密码盐值（16字节）
- vaultMasterKey: 保险库主密钥（32字节）
- userId: 可选的用户UUID
- registeredAt: 可选的注册时间

#### UserSaltDTO
- userId: 用户UUID
- passwordSalt: 密码盐值（16字节）

#### UserProfileDTO
- userId: 用户UUID
- username: 用户名（ASCII字符串）
- displayName: 显示名称（UTF-8字符串）
- publicKey: 用户公钥（字节数组）
- lastOnline: 最后在线时间
- registeredAt: 注册时间

#### VaultCreationDTO
- userId: 用户UUID
- vaultSalt: 保险库盐值（16字节）
- vaultIv: 初始化向量（12字节）
- encryptedPrivateKey: 加密的私钥（48-64字节）

#### VaultDTO
- vaultMasterKey: 保险库主密钥（32字节）
- vaultSalt: 保险库盐值（16字节）
- vaultIv: 初始化向量（12字节）
- encryptedPrivateKey: 加密的私钥（48-64字节）

### 设计特点

1. **安全性考虑**
   - 密码相关操作在客户端完成，服务端仅存储和验证散列值
   - 保险库配置采用两阶段设计，确保密钥安全性
   - 所有敏感数据使用字节数组传输，避免字符串转换

2. **可扩展性**
   - DTO对象封装所有数据传输，便于未来扩展字段
   - DAO方法独立封装存储过程调用，便于切换实现

3. **异常处理**
   - 使用Optional返回可能不存在的数据
   - 通过布尔返回值表示操作结果，避免异常传播
   - 所有数据验证在DAO层完成，确保数据一致性
