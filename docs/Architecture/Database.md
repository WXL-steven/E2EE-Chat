# 数据库架构设计

## 1. 技术栈
- PostgreSQL 17.2

## 2. 数据库表设计

> 表格中使用的缩写说明：
> - 键类型：PK=主键, FK=外键
> - 约束：UQ=唯一约束
> - 索引类型：IX=普通索引, CX=聚集索引（主键默认）

### 2.1 用户相关表

#### 2.1.1 user_profiles（用户资料表）

| 字段名 | 类型 | 可空 | 默认值 | 键/约束 | 索引 | 说明 |
|--------|------|------|--------|----------|------|------|
| idx | BIGSERIAL | 否 | 自增 | PK | CX | 自增主键 |
| user_id | UUID | 否 | gen_random_uuid() | UQ | IX | 用户唯一标识 |
| username | VARCHAR(16) | 否 | - | UQ | IX | 用户名，仅ASCII字符 |
| display_name | VARCHAR(32) | 否 | - | - | - | UTF-8字符串 |
| public_key | BYTEA | 否 | - | - | - | 用户公钥 |
| last_online | TIMESTAMPTZ | 否 | CURRENT_TIMESTAMP | - | - | 最后在线时间 |
| registered_at | TIMESTAMPTZ | 否 | CURRENT_TIMESTAMP | - | - | 注册时间 |

**其他约束：**
- username: `^[[:ascii:]]{1,16}$`

#### 2.1.2 user_credentials（用户机密表）

| 字段名 | 类型 | 可空 | 默认值 | 键/约束 | 索引 | 说明 |
|--------|------|------|--------|----------|------|------|
| idx | BIGSERIAL | 否 | 自增 | PK | CX | 自增主键 |
| user_id | UUID | 否 | - | FK,UQ | IX | 关联用户标识 |
| password_hash | BYTEA | 否 | - | - | - | 32字节固定长度 |
| password_salt | BYTEA | 否 | - | - | - | 16字节固定长度 |

**其他约束：**
- password_hash: 固定长度32字节
- password_salt: 固定长度16字节
- FK: user_id REFERENCES user_profiles ON DELETE CASCADE

#### 2.1.3 user_vaults（用户保险库表）

| 字段名 | 类型 | 可空 | 默认值 | 键/约束 | 索引 | 说明 |
|--------|------|------|--------|----------|------|------|
| idx | BIGSERIAL | 否 | 自增 | PK | CX | 自增主键 |
| user_id | UUID | 否 | - | FK,UQ | IX | 关联用户标识 |
| vault_master_key | BYTEA | 否 | - | - | - | 32字节固定长度 |
| vault_salt | BYTEA | 是 | - | - | - | 16字节固定长度 |
| vault_iv | BYTEA | 是 | - | - | - | 12字节固定长度 |
| encrypted_private_key | BYTEA | 是 | - | - | - | 48-64字节长度 |
| ready | BOOLEAN | 否 | FALSE | - | - | 保险库配置状态 |

**其他约束：**
- vault_master_key: 固定长度32字节（创建时必需）
- vault_salt: 固定长度16字节（配置时设置）
- vault_iv: 固定长度12字节（配置时设置）
- encrypted_private_key: 长度在48-64字节之间（配置时设置）
- ready: 只有当所有加密字段都设置后才能为true
- FK: user_id REFERENCES user_profiles ON DELETE CASCADE

### 2.2 会话相关表

#### 2.2.1 chat_sessions（会话表）

| 字段名 | 类型 | 可空 | 默认值 | 键/约束 | 索引 | 说明 |
|--------|------|------|--------|----------|------|------|
| idx | BIGSERIAL | 否 | 自增 | PK | CX | 自增主键 |
| session_id | UUID | 否 | gen_random_uuid() | UQ | IX | 会话唯一标识 |
| initiator_id | UUID | 否 | - | FK | IX | 发起者ID |
| participant_id | UUID | 否 | - | FK | IX | 参与者ID |
| created_at | TIMESTAMPTZ | 否 | CURRENT_TIMESTAMP | - | - | 会话创建时间 |
| message_counter | BIGINT | 否 | 0 | - | - | 消息计数器 |
| last_message_id | UUID | 是 | NULL | FK | - | 最后消息ID |
| last_message_at | TIMESTAMPTZ | 是 | NULL | - | IX | 最后消息时间 |

**其他约束：**
- FK: initiator_id REFERENCES user_profiles(user_id)
- FK: participant_id REFERENCES user_profiles(user_id)
- FK: last_message_id REFERENCES chat_messages(message_id)
- CHECK: initiator_id != participant_id
- 联合索引：(initiator_id, participant_id) 用于会话查找
- 联合索引：(initiator_id, last_message_at DESC NULLS LAST) 优化发起者的最近会话查询
- 联合索引：(participant_id, last_message_at DESC NULLS LAST) 优化参与者的最近会话查询

#### 2.2.2 chat_messages（消息表）

| 字段名 | 类型 | 可空 | 默认值 | 键/约束 | 索引 | 说明 |
|--------|------|------|--------|----------|------|------|
| idx | BIGSERIAL | 否 | 自增 | PK | CX | 自增主键 |
| message_id | UUID | 否 | gen_random_uuid() | UQ | IX | 消息唯一标识 |
| session_id | UUID | 否 | - | FK | IX | 所属会话ID |
| cursor | BIGINT | 否 | - | - | IX | 消息游标 |
| sender_id | UUID | 否 | - | FK | IX | 发送者ID |
| receiver_id | UUID | 否 | - | FK | IX | 接收者ID |
| message_iv | BYTEA | 否 | - | - | - | 消息初始化向量 |
| message_content | BYTEA | 否 | - | - | - | 消息密文 |
| is_read | BOOLEAN | 否 | FALSE | - | IX | 消息已读状态 |
| is_system | BOOLEAN | 否 | FALSE | - | - | 是否功能性消息 |

**其他约束：**
- FK: session_id REFERENCES chat_sessions(session_id) ON DELETE CASCADE
- FK: sender_id REFERENCES user_profiles(user_id)
- FK: receiver_id REFERENCES user_profiles(user_id)
- CHECK: length(message_iv) = 12
- CHECK: sender_id != receiver_id
- UNIQUE(session_id, cursor) 确保游标在会话内唯一
- 联合索引：(session_id, cursor DESC) 用于消息历史查询
- 联合索引：(receiver_id, is_read) 用于未读消息查询
- 联合索引：(session_id, sender_id, cursor DESC) 优化用户发送消息的查询
- 联合索引：(receiver_id, is_read, cursor DESC) 优化未读消息查询和排序

### 2.3 索引策略

#### 2.3.1 会话表索引
- session_id：用于直接访问会话
- initiator_id, participant_id：用于用户会话列表查询
- (initiator_id, participant_id)：用于会话查找和防重复
- (initiator_id, last_message_at DESC NULLS LAST)：优化发起者的最近会话查询
- (participant_id, last_message_at DESC NULLS LAST)：优化参与者的最近会话查询

#### 2.3.2 消息表索引
- message_id：用于直接访问消息
- session_id：用于会话消息查询
- (session_id, cursor DESC)：用于高效分页和历史消息查询
- (session_id, sender_id, cursor DESC)：优化用户发送消息的查询
- (receiver_id, is_read, cursor DESC)：优化未读消息查询和排序
- cursor：使用会话消息计数器生成，确保会话内唯一且递增

#### 2.3.3 性能优化
- 消息游标使用BIGINT，配合会话的message_counter实现会话内自增
- 关键查询场景都有对应的联合索引，减少排序操作
- 使用NULLS LAST确保新会话和空消息排序合理
- 索引字段顺序经过优化，支持最左前缀匹配原则
- 使用CASCADE删除确保数据一致性
- 重要的外键都建立了索引以提升JOIN性能

## 3. 索引设计策略

### 3.1 索引类型选择
- 主键列使用聚集索引(CX)，提供最快的查找性能
- 外键列使用普通索引(IX)，加速关联查询
- 频繁查询条件（如username）使用普通索引(IX)
- 唯一约束列自动创建唯一索引

### 3.2 索引维护
- 定期重建索引以减少碎片
- 监控索引使用情况和性能
- 定期分析查询计划，优化索引策略

## 4. 存储过程

### 4.1 用户管理相关

#### 4.1.1 check_username_available
检查用户名是否可用
- **输入参数**：
  - username (VARCHAR(16)): ASCII字符串，最长16字符
- **返回值**：BOOLEAN
- **验证规则**：
  - 用户名必须由字母、数字、连字符(-)、下划线(_)构成
  - 用户名在user_profiles表中不存在

#### 4.1.2 register_user
注册新用户
- **输入参数**：
  - username (VARCHAR(16)): ASCII字符串，最长16字符
  - display_name (VARCHAR(32)): UTF-8字符串，最长32字符
  - password_hash (BYTEA): 32字节定长
  - password_salt (BYTEA): 16字节定长
  - vault_master_key (BYTEA): 32字节定长
  - user_id (UUID) [可选]: 用户唯一标识
  - registered_at (TIMESTAMPTZ) [可选]: 注册时间
- **返回值**：BOOLEAN
- **处理流程**：
  1. 调用check_username_available验证用户名
  2. 检查UUID唯一性（如果提供）
  3. 插入user_profiles表
  4. 插入user_credentials表
  5. 插入user_vaults表（仅vault_master_key）
  6. 返回是否全部成功

#### 4.1.3 get_user_salt
获取用户的密码盐值
- **输入参数**：
  - username (VARCHAR(16)): ASCII字符串
- **返回值**：TABLE(user_id UUID, password_salt BYTEA)
- **说明**：
  - 如果用户不存在返回0行
  - 存在则返回1行包含user_id和password_salt

#### 4.1.4 verify_login
验证用户登录
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - password_hash (BYTEA): 32字节密码散列
- **返回值**：BOOLEAN
- **验证规则**：
  - 验证user_id存在
  - 验证password_hash长度为32字节
  - 比对存储的密码散列

#### 4.1.5 get_user_profile
获取用户资料信息
- **输入参数**：
  - user_id (UUID): 用户唯一标识
- **返回值**：TABLE(user_id UUID, username VARCHAR(16), display_name VARCHAR(32), public_key BYTEA, last_online TIMESTAMPTZ, registered_at TIMESTAMPTZ)
- **说明**：
  - 如果用户不存在返回0行
  - 存在则返回1行完整的用户资料（不含idx）

#### 4.1.6 update_last_online
更新用户最后在线时间
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - timestamp (TIMESTAMPTZ): 可选的时间戳，默认为当前时间
- **返回值**：void
- **说明**：
  - 更新user_profiles表中的last_online字段
  - 如果未提供时间戳则使用CURRENT_TIMESTAMP
  - 如果用户不存在则静默失败

### 4.2 保险库管理相关

#### 4.2.1 create_vault
创建用户保险库
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - vault_salt (BYTEA): 16字节定长
  - vault_iv (BYTEA): 12字节定长
  - encrypted_private_key (BYTEA): 48-64字节
- **返回值**：BOOLEAN
- **验证规则**：
  - 验证user_id存在且ready为False
  - 验证所有输入的字节长度
  - 更新保险库信息并设置ready为True

#### 4.2.2 get_vault
获取用户保险库信息
- **输入参数**：
  - user_id (UUID): 用户唯一标识
- **返回值**：TABLE(vault_master_key BYTEA, vault_salt BYTEA, vault_iv BYTEA, encrypted_private_key BYTEA)
- **说明**：
  - 如果用户不存在返回0行
  - 存在则返回1行保险库信息，包括未完成配置的保险库
  - vault_salt、vault_iv、encrypted_private_key在未配置时为NULL

### 4.3 聊天相关

#### 4.3.1 get_recent_sessions
获取用户最近会话列表
- **输入参数**：
  - user_id (UUID): 用户唯一标识
- **返回值**：TABLE(
    session_id UUID,
    initiator_id UUID,
    participant_id UUID,
    created_at TIMESTAMPTZ,
    message_counter BIGINT,
    last_message_id UUID,
    last_message_at TIMESTAMPTZ
  )
- **说明**：
  - 按last_message_at降序排序
  - 自动更新用户最后在线时间
  - 返回用户参与的所有会话

#### 4.3.2 get_unread_count
获取会话未读消息数
- **输入参数**：
  - session_id (UUID): 会话唯一标识
- **返回值**：INTEGER
- **说明**：
  - 返回会话中未读消息的数量

#### 4.3.3 get_first_unread
获取会话首条未读消息ID
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - session_id (UUID): 会话唯一标识
- **返回值**：UUID
- **说明**：
  - 如果全部已读，返回最后一条消息的ID
  - 如果没有消息，返回NULL

#### 4.3.4 get_messages_before
获取指定游标之前的消息
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - session_id (UUID): 会话唯一标识
  - cursor (BIGINT): 消息游标，默认-1
  - limit (INTEGER): 返回消息数量，默认50
- **返回值**：TABLE(
    message_id UUID,
    cursor BIGINT,
    is_sender BOOLEAN,
    is_system BOOLEAN,
    is_read BOOLEAN,
    message_iv BYTEA,
    message_content BYTEA
  )
- **说明**：
  - cursor为-1时从最新消息开始
  - 自动更新用户最后在线时间
  - 自动标记返回的接收消息为已读
  - 包含cursor指定的消息

#### 4.3.5 get_messages_after
获取指定游标之后的消息
- **输入参数**：
  - user_id (UUID): 用户唯一标识
  - session_id (UUID): 会话唯一标识
  - cursor (BIGINT): 消息游标，默认-1
  - limit (INTEGER): 返回消息数量，默认50
- **返回值**：同get_messages_before
- **说明**：
  - cursor为-1时从最早消息开始
  - 其他特性同get_messages_before

#### 4.3.6 send_message
发送新消息
- **输入参数**：
  - user_id (UUID): 发送者ID
  - session_id (UUID): 会话唯一标识
  - message_iv (BYTEA): 消息初始化向量
  - message_content (BYTEA): 消息密文
  - is_system (BOOLEAN): 是否系统消息
- **返回值**：BOOLEAN
- **说明**：
  - 自动递增消息计数器作为游标
  - 更新会话的last_message_id和last_message_at
  - 验证发送者身份和会话存在性

## 5. 数据安全

### 5.1 数据隔离
- 敏感数据（如密码、密钥）与基本信息分离存储
- 使用专门的user_vaults表存储加密相关数据
- 所有密码和密钥相关字段使用BYTEA类型，避免字符编码问题
- 严格控制字段长度，防止溢出攻击

### 5.2 数据完整性
- 使用外键约束确保数据关联的完整性
- 使用CHECK约束确保数据格式的正确性
- 所有表都使用CASCADE删除确保数据一致性
- 使用BIGSERIAL和UUID确保标识符的唯一性

### 5.3 访问控制
- 所有存储过程使用SECURITY DEFINER模式运行
  - 以创建者权限执行而非调用者权限
  - 确保数据访问的一致性和安全性
- 使用专门的服务账号(e2ee_chat_service)
  - 仅具有存储过程执行权限
  - 禁止直接读写所有数据表
  - 禁止修改任何数据库对象

### 5.4 加密策略
- 密码存储：
  - 使用随机盐值(16字节)
  - 密码散列固定为32字节
  - 客户端进行密码散列，服务端仅存储
- 消息加密：
  - 每条消息使用唯一的IV(12字节)
  - 消息内容使用端到端加密
  - 密钥和IV永不重用

### 5.5 审计和监控
- 记录关键操作时间戳
  - 用户注册时间
  - 最后在线时间
  - 消息发送时间
- 使用事务确保操作原子性
- 关键操作使用行级锁防止并发冲突
