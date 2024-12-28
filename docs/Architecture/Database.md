# 数据库文档

> **版本：** 0.1 | **日期：** 2024年12月28日

## 目录

**数据库结构**
  *   [数据库概览](#1-数据库概览)：数据库的用途和设计理念。
  *   [用户资料表 (user_profiles)](#22-user_profiles)：存储用户的基本信息，如用户名、显示名和公钥。
  *   [用户凭证表 (user_credentials)](#23-user_credentials)：存储用户的登录凭证，如密码哈希和盐值。
  *   [用户保险库表 (user_vaults)](#24-user_vaults)：存储用户的加密密钥信息。

**用户管理存储过程**
  *   [检查用户名是否可用 (check_username_available)](#32-check_username_available)：验证用户名格式和唯一性。
  *   [注册新用户 (register_user)](#33-register_user)：创建用户账户并初始化相关数据。
  *   [获取用户密码盐值 (get_user_salt)](#34-get_user_salt)：用于密码验证。
  *   [验证用户登录 (verify_login)](#35-verify_login)：核对用户提供的密码。
  *   [创建用户保险库 (create_vault)](#36-create_vault)：配置用户的加密密钥。
  *   [获取用户保险库信息 (get_vault)](#37-get_vault)：获取用户的加密密钥信息。
  *   [获取用户资料信息 (get_user_profile)](#38-get_user_profile)：获取用户的基本信息。
  *   [更新用户最后在线时间 (update_last_online)](#39-update_last_online)：记录用户的活动状态。
  *   [通过用户名获取用户UUID (get_user_uuid_by_username)](#310-get_user_uuid_by_username)：根据用户名查找用户 ID。

**会话管理存储过程**
  *   [获取最近会话列表 (get_recent_sessions)](#311-get_recent_sessions)：查看用户的聊天列表。
  *   [获取未读消息数 (get_unread_count)](#312-get_unread_count)：统计会话中的未读消息。
  *   [获取首条未读消息 (get_first_unread)](#313-get_first_unread)：定位会话中的未读消息。
  *   [获取之前的消息 (get_messages_before)](#314-get_messages_before)：加载历史消息。
  *   [获取之后的消息 (get_messages_after)](#315-get_messages_after)：加载新消息。
  *   [发送消息 (send_message)](#316-send_message)：向会话发送消息。
  *   [获取或创建会话 (get_or_create_session)](#317-get_or_create_session)：开始新的聊天或继续现有聊天。
  *   [获取指定会话 (get_session)](#318-get_session)：查看会话详情。
  *   [获取指定消息 (get_message)](#319-get_message)：查看单条消息详情。

**数据库用户**
  *   [服务账号 (e2ee_chat_service)](#41-e2ee_chat_service)：应用程序访问数据库的专用账户及其权限。

## 1. 数据库概览

本数据库用于支持一个端到端加密的聊天平台，名为 E2EE-Chat。它主要负责存储用户身份信息、加密密钥以及聊天会话和消息数据。

## 2. 数据库模式与表

本节详细描述了数据库中的所有模式和表结构。

### 2.1 表结构通用说明

* 所有表都使用 `BIGSERIAL` 类型的 `idx` 列作为自增主键。
* 所有使用 UUID 作为外键的表，其外键列名都遵循 `*_id` 的命名约定。
* 时间戳相关的列都使用 `TIMESTAMPTZ` 类型，存储带时区的时间信息。

### 2.2 user_profiles

#### 2.2.1 表结构

| 键名            | 类型          | 能否为空 | 备注     | 释义/解释/注释                                                    |
|---------------|-------------|------|--------|-------------------------------------------------------------|
| idx           | BIGSERIAL   | 否    | 主键     | 自增主键，无业务含义。                                                 |
| user_id       | UUID        | 否    | 唯一     | 用户的唯一标识符，由程序生成。                                             |
| username      | VARCHAR(16) | 否    | 唯一，校验  | 用户名，用于登录。限制为 ASCII 字符，长度 1-16。设计为 ASCII 是为了简化早期版本和避免字符编码问题。 |
| display_name  | VARCHAR(32) | 否    |        | 用户显示的昵称，允许使用 UTF-8 字符，长度不超过 32。                             |
| public_key    | BYTEA       | 是    |        | 用户的公钥，用于端到端加密。在用户创建保险库后设置。                                  |
| last_online   | TIMESTAMPTZ | 否    | 默认当前时间 | 用户最后一次在线的时间。每次用户活动时更新。                                      |
| registered_at | TIMESTAMPTZ | 否    | 默认当前时间 | 用户的注册时间。                                                    |

#### 2.2.2 外键索引数据约束

* **唯一约束 (Unique Constraints):**
    * `uk_user_profiles_user_id`: 确保 `user_id` 列的唯一性。
    * `uk_user_profiles_username`: 确保 `username` 列的唯一性。
* **检查约束 (Check Constraints):**
    * `ck_username_ascii`: 限制 `username` 只能包含 ASCII 字符。
    * `ck_public_key_length`: 限制 `public_key` 的长度在 32 到 256 字节之间，允许为空。

### 2.3 user_credentials

#### 2.3.1 表结构

| 键名            | 类型        | 能否为空 | 备注    | 释义/解释/注释                          |
|---------------|-----------|------|-------|-----------------------------------|
| idx           | BIGSERIAL | 否    | 主键    | 自增主键，无业务含义。                       |
| user_id       | UUID      | 否    | 唯一，外键 | 关联 `user_profiles` 表的 `user_id`。  |
| password_hash | BYTEA     | 否    | 校验    | 存储用户密码的 Argon2id 哈希值，长度固定为 32 字节。 |
| password_salt | BYTEA     | 否    | 校验    | 存储用于生成密码哈希的盐值，长度固定为 16 字节。        |

#### 2.3.2 外键索引数据约束

* **唯一约束 (Unique Constraints):**
    * `uk_user_credentials_user_id`: 确保每个用户只有一条凭证记录。
* **外键约束 (Foreign Key Constraints):**
    * `fk_user_credentials_user_id`: 关联 `user_profiles` 表的 `user_id`，并设置 `ON DELETE CASCADE`，即删除用户时级联删除其凭证信息。
* **检查约束 (Check Constraints):**
    * `ck_password_hash_length`: 确保 `password_hash` 的长度为 32 字节。
    * `ck_password_salt_length`: 确保 `password_salt` 的长度为 16 字节。

### 2.4 user_vaults

#### 2.4.1 表结构

| 键名                    | 类型        | 能否为空 | 备注       | 释义/解释/注释                                                                                                 |
|-----------------------|-----------|------|----------|----------------------------------------------------------------------------------------------------------|
| idx                   | BIGSERIAL | 否    | 主键       | 自增主键，无业务含义。                                                                                              |
| user_id               | UUID      | 否    | 唯一，外键    | 关联 `user_profiles` 表的 `user_id`。                                                                         |
| vault_master_key      | BYTEA     | 否    | 校验       | 用户的主密钥，由客户端生成并存储。长度固定为 32 字节。                                                                            |
| vault_salt            | BYTEA     | 是    | 校验       | 用于派生保险库密钥的盐值，在配置保险库时设置。长度固定为 16 字节。                                                                      |
| vault_iv              | BYTEA     | 是    | 校验       | 用于加密用户私钥的初始化向量，在配置保险库时设置。长度固定为 12 字节。                                                                    |
| encrypted_private_key | BYTEA     | 是    | 校验       | 使用保险库密钥加密后的用户私钥，在配置保险库时设置。长度在 32 到 256 字节之间。                                                             |
| ready                 | BOOLEAN   | 否    | 默认 FALSE | 标识用户的保险库是否已配置完成。当 `vault_salt`, `vault_iv`, `encrypted_private_key` 均不为空时，此字段为 `TRUE`。用于快速判断用户是否已完成密钥设置。 |

#### 2.4.2 外键索引数据约束

* **唯一约束 (Unique Constraints):**
    * `uk_user_vaults_user_id`: 确保每个用户只有一条保险库记录。
* **外键约束 (Foreign Key Constraints):**
    * `fk_user_vaults_user_id`: 关联 `user_profiles` 表的 `user_id`，并设置 `ON DELETE CASCADE`，即删除用户时级联删除其保险库信息。
* **检查约束 (Check Constraints):**
    * `ck_vault_master_key_length`: 确保 `vault_master_key` 的长度为 32 字节。
    * `ck_vault_salt_length`: 确保 `vault_salt` 的长度为 16 字节或为空。
    * `ck_vault_iv_length`: 确保 `vault_iv` 的长度为 12 字节或为空。
    * `ck_encrypted_private_key_length`: 确保 `encrypted_private_key` 的长度在 32 到 256 字节之间或为空。
    * `ck_vault_ready`:  使用复杂的逻辑表达式确保当 `ready` 为 `TRUE` 时，`vault_salt`、`vault_iv` 和
      `encrypted_private_key` 都不为空；反之亦然。

## 3. 存储过程

本节详细描述了数据库中的所有存储过程。

### 3.1 存储过程通用说明

* 所有存储过程都使用 `SECURITY DEFINER` 属性，表示它们以创建者的权限执行。
* 存储过程的命名通常反映其功能，并使用下划线分隔单词。
* 对于返回结果集的存储过程，通常返回一个 `TABLE` 类型。

### 3.2 check_username_available

#### 3.2.1 功能描述

检查给定的用户名是否可用。首先验证用户名是否符合格式要求（只包含字母、数字、下划线和连字符，长度 1-16），然后检查数据库中是否已存在该用户名。

#### 3.2.2 输入

| 参数名        | 类型          | 说明       |
|------------|-------------|----------|
| p_username | VARCHAR(16) | 要检查的用户名。 |

#### 3.2.3 输出

| 参数名 | 类型      | 说明                              |
|-----|---------|---------------------------------|
|     | BOOLEAN | 如果用户名可用则返回 `TRUE`，否则返回 `FALSE`。 |

#### 3.2.4 注意事项

无。

### 3.3 register_user

#### 3.3.1 功能描述

注册一个新用户。该过程会执行以下操作：

1. 验证用户名是否可用（调用 `check_username_available` 函数）。
2. 验证提供的 `user_id` 是否唯一。
3. 验证密码哈希、密码盐值和保险库主密钥的长度是否符合要求。
4. 在一个事务中插入用户的基本信息到 `user_profiles` 表，凭证信息到 `user_credentials` 表，以及初始的保险库信息（仅包含主密钥）到
   `user_vaults` 表。
5. 如果任何步骤失败，则回滚事务并返回 `NULL`。

#### 3.3.2 输入

| 参数名                | 类型          | 说明                        |
|--------------------|-------------|---------------------------|
| p_username         | VARCHAR(16) | 用户名。                      |
| p_display_name     | VARCHAR(32) | 显示名称。                     |
| p_password_hash    | BYTEA       | 密码的 Argon2id 哈希值 (32 字节)。 |
| p_password_salt    | BYTEA       | 用于生成密码哈希的盐值 (16 字节)。      |
| p_vault_master_key | BYTEA       | 用户的保险库主密钥 (32 字节)。        |
| p_user_id          | UUID        | （可选）用户 ID，默认为自动生成。        |
| p_registered_at    | TIMESTAMPTZ | （可选）注册时间，默认为当前时间。         |

#### 3.3.3 输出

| 参数名 | 类型   | 说明                                   |
|-----|------|--------------------------------------|
|     | UUID | 如果注册成功，返回新用户的 `user_id`，否则返回 `NULL`。 |

#### 3.3.4 注意事项

如果提供的用户名已被占用或 `user_id` 已存在，则注册失败。

### 3.4 get_user_salt

#### 3.4.1 功能描述

通过用户名获取用户的 `user_id` 和密码盐值。

#### 3.4.2 输入

| 参数名        | 类型          | 说明       |
|------------|-------------|----------|
| p_username | VARCHAR(16) | 要查询的用户名。 |

#### 3.4.3 输出

| 列名            | 类型    | 说明        |
|---------------|-------|-----------|
| user_id       | UUID  | 用户的唯一标识符。 |
| password_salt | BYTEA | 用户的密码盐值。  |

#### 3.4.4 注意事项

无。

### 3.5 verify_login

#### 3.5.1 功能描述

验证用户的登录凭证。通过提供的 `user_id` 和密码哈希值，与数据库中存储的密码哈希进行比较。

#### 3.5.2 输入

| 参数名             | 类型    | 说明                     |
|-----------------|-------|------------------------|
| p_user_id       | UUID  | 用户的唯一标识符。              |
| p_password_hash | BYTEA | 用户提供的密码的 Argon2id 哈希值。 |

#### 3.5.3 输出

| 参数名 | 类型      | 说明                               |
|-----|---------|----------------------------------|
|     | BOOLEAN | 如果登录凭证有效则返回 `TRUE`，否则返回 `FALSE`。 |

#### 3.5.4 注意事项

输入的密码哈希长度必须为 32 字节。

### 3.6 create_vault

#### 3.6.1 功能描述

完成用户的保险库配置。该过程会执行以下操作：

1. 验证提供的 `user_id` 对应的用户存在且保险库尚未就绪（`ready` 为 `FALSE`）。
2. 验证输入的盐值、初始化向量和加密私钥的长度是否符合要求。
3. 在一个事务中更新 `user_profiles` 表中的 `public_key` 字段，并更新 `user_vaults` 表中的 `vault_salt`、`vault_iv`、
   `encrypted_private_key` 字段，并将 `ready` 字段设置为 `TRUE`。
4. 如果更新成功，则返回 `TRUE`，否则回滚事务并返回 `FALSE`。

#### 3.6.2 输入

| 参数名                     | 类型    | 说明                    |
|-------------------------|-------|-----------------------|
| p_user_id               | UUID  | 用户的唯一标识符。             |
| p_vault_salt            | BYTEA | 保险库盐值 (16 字节)。        |
| p_vault_iv              | BYTEA | 保险库初始化向量 (12 字节)。     |
| p_encrypted_private_key | BYTEA | 加密后的用户私钥 (32-256 字节)。 |
| p_public_key            | BYTEA | 用户的公钥 (32-256 字节)。    |

#### 3.6.3 输出

| 参数名 | 类型      | 说明                                |
|-----|---------|-----------------------------------|
|     | BOOLEAN | 如果保险库配置成功则返回 `TRUE`，否则返回 `FALSE`。 |

#### 3.6.4 注意事项

在调用此函数之前，应确保用户已登录并且拥有合法的保险库配置数据。

### 3.7 get_vault

#### 3.7.1 功能描述

获取指定用户的完整保险库信息。

#### 3.7.2 输入

| 参数名       | 类型   | 说明        |
|-----------|------|-----------|
| p_user_id | UUID | 用户的唯一标识符。 |

#### 3.7.3 输出

| 列名                    | 类型      | 说明          |
|-----------------------|---------|-------------|
| vault_master_key      | BYTEA   | 用户的保险库主密钥。  |
| vault_salt            | BYTEA   | 保险库盐值。      |
| vault_iv              | BYTEA   | 保险库初始化向量。   |
| encrypted_private_key | BYTEA   | 加密后的用户私钥。   |
| ready                 | BOOLEAN | 保险库是否已配置完成。 |

#### 3.7.4 注意事项

无。

### 3.8 get_user_profile

#### 3.8.1 功能描述

获取指定用户的完整资料信息。

#### 3.8.2 输入

| 参数名       | 类型   | 说明        |
|-----------|------|-----------|
| p_user_id | UUID | 用户的唯一标识符。 |

#### 3.8.3 输出

| 列名            | 类型          | 说明        |
|---------------|-------------|-----------|
| user_id       | UUID        | 用户的唯一标识符。 |
| username      | VARCHAR(16) | 用户名。      |
| display_name  | VARCHAR(32) | 显示名称。     |
| public_key    | BYTEA       | 用户的公钥。    |
| last_online   | TIMESTAMPTZ | 最后在线时间。   |
| registered_at | TIMESTAMPTZ | 注册时间。     |

#### 3.8.4 注意事项

无。

### 3.9 update_last_online

#### 3.9.1 功能描述

更新指定用户的最后在线时间。

#### 3.9.2 输入

| 参数名         | 类型          | 说明                   |
|-------------|-------------|----------------------|
| p_user_id   | UUID        | 用户的唯一标识符。            |
| p_timestamp | TIMESTAMPTZ | （可选）要设置的时间戳，默认为当前时间。 |

#### 3.9.3 输出

无。

#### 3.9.4 注意事项

无。

### 3.10 get_user_uuid_by_username

#### 3.10.1 功能描述

通过用户名获取用户的 UUID。

#### 3.10.2 输入

| 参数名        | 类型          | 说明       |
|------------|-------------|----------|
| p_username | VARCHAR(16) | 要查询的用户名。 |

#### 3.10.3 输出

| 参数名 | 类型   | 说明        |
|-----|------|-----------|
|     | UUID | 用户的唯一标识符。 |

#### 3.10.4 注意事项

无。

### 3.11 get_recent_sessions

#### 3.11.1 功能描述

获取指定用户最近的会话列表，并按照最后消息时间降序排列。该过程会先更新用户的最后在线时间。

#### 3.11.2 输入

| 参数名       | 类型   | 说明        |
|-----------|------|-----------|
| p_user_id | UUID | 用户的唯一标识符。 |

#### 3.11.3 输出

| 列名              | 类型          | 说明         |
|-----------------|-------------|------------|
| session_id      | UUID        | 会话唯一标识符。   |
| initiator_id    | UUID        | 会话发起者 ID。  |
| participant_id  | UUID        | 会话参与者 ID。  |
| created_at      | TIMESTAMPTZ | 会话创建时间。    |
| message_counter | BIGINT      | 会话消息计数器。   |
| last_message_id | UUID        | 最后一条消息 ID。 |
| last_message_at | TIMESTAMPTZ | 最后一条消息时间。  |

#### 3.11.4 注意事项

无。

### 3.12 get_unread_count

#### 3.12.1 功能描述

获取指定会话中，指定用户的未读消息数量。

#### 3.12.2 输入

| 参数名          | 类型   | 说明                |
|--------------|------|-------------------|
| p_session_id | UUID | 会话的唯一标识符。         |
| p_user_id    | UUID | 要查询未读消息的用户的唯一标识符。 |

#### 3.12.3 输出

| 参数名 | 类型      | 说明       |
|-----|---------|----------|
|     | INTEGER | 未读消息的数量。 |

#### 3.12.4 注意事项

无。

### 3.13 get_first_unread

#### 3.13.1 功能描述

获取指定会话中，指定用户的首条未读消息的 ID。如果所有消息都已读，则返回最后一条消息的 ID。

#### 3.13.2 输入

| 参数名          | 类型   | 说明        |
|--------------|------|-----------|
| p_user_id    | UUID | 用户的唯一标识符。 |
| p_session_id | UUID | 会话的唯一标识符。 |

#### 3.13.3 输出

| 参数名          | 类型   | 说明                              |
|--------------|------|---------------------------------|
| v_message_id | UUID | 首条未读消息的 ID，如果全部已读则返回最后一条消息的 ID。 |

#### 3.13.4 注意事项

无。

### 3.14 get_messages_before

#### 3.14.1 功能描述

获取指定会话中，指定游标（cursor）之前的消息列表。默认返回最新的 50 条消息。同时，**会将返回给接收者的消息标记为已读**。

#### 3.14.2 输入

| 参数名          | 类型      | 说明                                   |
|--------------|---------|--------------------------------------|
| p_user_id    | UUID    | 当前用户的唯一标识符。                          |
| p_session_id | UUID    | 会话的唯一标识符。                            |
| p_cursor     | BIGINT  | （可选）游标值，返回此游标之前的消息。默认为 -1，表示获取最新的消息。 |
| p_limit      | INTEGER | （可选）返回的消息数量上限，默认为 50。                |

#### 3.14.3 输出

| 列名              | 类型          | 说明         |
|-----------------|-------------|------------|
| message_id      | UUID        | 消息的唯一标识符。  |
| cursor          | BIGINT      | 消息在会话中的游标。 |
| sender_id       | UUID        | 发送者的用户 ID。 |
| receiver_id     | UUID        | 接收者的用户 ID。 |
| is_system       | BOOLEAN     | 是否为系统消息。   |
| is_read         | BOOLEAN     | 消息是否已读。    |
| message_iv      | BYTEA       | 消息的初始化向量。  |
| message_content | BYTEA       | 消息的密文内容。   |
| sent_at         | TIMESTAMPTZ | 消息发送的时间戳。  |

#### 3.14.4 注意事项

* 如果 `p_cursor` 为 -1，则返回最新的 `p_limit` 条消息。
* 返回的消息会根据 `cursor` 降序排列。
* **重要:** 此函数会自动将返回给 `p_user_id` 的未读消息标记为已读。

### 3.15 get_messages_after

#### 3.15.1 功能描述

获取指定会话中，指定游标（cursor）之后的消息列表。默认返回最早的 50 条消息。同时，**会将返回给接收者的消息标记为已读**。

#### 3.15.2 输入

| 参数名          | 类型      | 说明                                   |
|--------------|---------|--------------------------------------|
| p_user_id    | UUID    | 当前用户的唯一标识符。                          |
| p_session_id | UUID    | 会话的唯一标识符。                            |
| p_cursor     | BIGINT  | （可选）游标值，返回此游标之后的消息。默认为 -1，表示获取最早的消息。 |
| p_limit      | INTEGER | （可选）返回的消息数量上限，默认为 50。                |

#### 3.15.3 输出

| 列名              | 类型          | 说明         |
|-----------------|-------------|------------|
| message_id      | UUID        | 消息的唯一标识符。  |
| cursor          | BIGINT      | 消息在会话中的游标。 |
| sender_id       | UUID        | 发送者的用户 ID。 |
| receiver_id     | UUID        | 接收者的用户 ID。 |
| is_system       | BOOLEAN     | 是否为系统消息。   |
| is_read         | BOOLEAN     | 消息是否已读。    |
| message_iv      | BYTEA       | 消息的初始化向量。  |
| message_content | BYTEA       | 消息的密文内容。   |
| sent_at         | TIMESTAMPTZ | 消息发送的时间戳。  |

#### 3.15.4 注意事项

* 如果 `p_cursor` 为 -1，则返回最早的 `p_limit` 条消息。
* 返回的消息会根据 `cursor` 升序排列。
* **重要:** 此函数会自动将返回给 `p_user_id` 的未读消息标记为已读。

### 3.16 send_message

#### 3.16.1 功能描述

发送一条新消息到指定的会话。该过程会执行以下操作：

1. 获取并锁定会话信息，防止并发修改。
2. 验证会话是否存在，并且发送者是会话的参与者之一。
3. 递增会话的消息计数器 (`message_counter`).
4. 插入新消息到 `chat_messages` 表。
5. 更新会话的最后消息 ID (`last_message_id`) 和最后消息时间 (`last_message_at`)。

#### 3.16.2 输入

| 参数名               | 类型      | 说明                       |
|-------------------|---------|--------------------------|
| p_user_id         | UUID    | 发送者的用户 ID。               |
| p_session_id      | UUID    | 目标会话的唯一标识符。              |
| p_message_iv      | BYTEA   | 消息的初始化向量 (12 字节)。        |
| p_message_content | BYTEA   | 消息的密文内容。                 |
| p_is_system       | BOOLEAN | （可选）是否为系统消息，默认为 `FALSE`。 |

#### 3.16.3 输出

| 参数名 | 类型      | 说明                               |
|-----|---------|----------------------------------|
|     | BOOLEAN | 如果消息发送成功则返回 `TRUE`，否则返回 `FALSE`。 |

#### 3.16.4 注意事项

* 发送者必须是指定会话的参与者才能发送消息。
* 此操作会在数据库层面保证消息的 `cursor` 的唯一性和递增性。

### 3.17 get_or_create_session

#### 3.17.1 功能描述

获取指定两个用户之间的会话。如果会话不存在，则创建一个新的会话。

#### 3.17.2 输入

| 参数名             | 类型   | 说明           |
|-----------------|------|--------------|
| p_user_id       | UUID | 当前用户的唯一标识符。  |
| p_other_user_id | UUID | 另一个用户的唯一标识符。 |

#### 3.17.3 输出

| 参数名          | 类型   | 说明                                     |
|--------------|------|----------------------------------------|
| v_session_id | UUID | 现有会话的 ID，或新创建会话的 ID。如果用户不存在则返回 `NULL`。 |

#### 3.17.4 注意事项

* 会话的 `initiator_id` 和 `participant_id` 会按照用户 ID 的大小进行排序，以保证相同用户之间的会话始终具有相同的
  `session_id`。
* 在创建新会话之前，会检查 `p_other_user_id` 是否存在于 `user_profiles` 表中。

### 3.18 get_session

#### 3.18.1 功能描述

获取指定会话的详细信息。只有会话的参与者才能获取会话信息。

#### 3.18.2 输入

| 参数名          | 类型   | 说明            |
|--------------|------|---------------|
| p_user_id    | UUID | 当前用户的唯一标识符。   |
| p_session_id | UUID | 要获取的会话的唯一标识符。 |

#### 3.18.3 输出

| 列名              | 类型          | 说明         |
|-----------------|-------------|------------|
| session_id      | UUID        | 会话唯一标识符。   |
| initiator_id    | UUID        | 会话发起者 ID。  |
| participant_id  | UUID        | 会话参与者 ID。  |
| created_at      | TIMESTAMPTZ | 会话创建时间。    |
| message_counter | BIGINT      | 会话消息计数器。   |
| last_message_id | UUID        | 最后一条消息 ID。 |
| last_message_at | TIMESTAMPTZ | 最后一条消息时间。  |

#### 3.18.4 注意事项

只会返回 `p_user_id` 是会话发起者或参与者的会话信息。

### 3.19 get_message

#### 3.19.1 功能描述

获取指定消息的详细信息。只有消息的发送者或接收者才能获取消息信息。如果用户是接收者且消息未读，则会将消息标记为已读。

#### 3.19.2 输入

| 参数名          | 类型   | 说明            |
|--------------|------|---------------|
| p_user_id    | UUID | 当前用户的唯一标识符。   |
| p_message_id | UUID | 要获取的消息的唯一标识符。 |

#### 3.19.3 输出

| 列名              | 类型          | 说明         |
|-----------------|-------------|------------|
| message_id      | UUID        | 消息的唯一标识符。  |
| session_id      | UUID        | 所属会话的 ID。  |
| cursor          | BIGINT      | 消息在会话中的游标。 |
| sender_id       | UUID        | 发送者的用户 ID。 |
| receiver_id     | UUID        | 接收者的用户 ID。 |
| is_system       | BOOLEAN     | 是否为系统消息。   |
| is_read         | BOOLEAN     | 消息是否已读。    |
| message_iv      | BYTEA       | 消息的初始化向量。  |
| message_content | BYTEA       | 消息的密文内容。   |
| sent_at         | TIMESTAMPTZ | 消息发送的时间戳。  |

#### 3.19.4 注意事项

* 只会返回 `p_user_id` 是消息发送者或接收者的消息信息。
* **重要:** 如果 `p_user_id` 是消息的接收者并且消息未读，此函数会自动将消息标记为已读。

## 4. 数据库用户

本节描述了用于程序操作的数据库用户及其权限。

### 4.1 e2ee_chat_service

#### 4.1.1 权限说明

该用户拥有以下权限：

* 连接到 `e2ee_chat` 数据库 (`CONNECT`)。
* 执行指定的存储过程 (`EXECUTE`)，包括：
* `check_username_available(VARCHAR)`
* `register_user(VARCHAR, VARCHAR, BYTEA, BYTEA, BYTEA, UUID, TIMESTAMPTZ)`
* `get_user_salt(VARCHAR)`
* `verify_login(UUID, BYTEA)`
* `create_vault(UUID, BYTEA, BYTEA, BYTEA, BYTEA)`
* `get_vault(UUID)`
* `get_user_profile(UUID)`
* `update_last_online(UUID, TIMESTAMPTZ)`
* `get_user_uuid_by_username(VARCHAR)`
* `get_recent_sessions(UUID)`
* `get_unread_count(UUID, UUID)`
* `get_first_unread(UUID, UUID)`
* `get_messages_before(UUID, UUID, BIGINT, INTEGER)`
* `get_messages_after(UUID, UUID, BIGINT, INTEGER)`
* `send_message(UUID, UUID, BYTEA, BYTEA, BOOLEAN)`
* `get_or_create_session(UUID, UUID)`
* `get_session(UUID, UUID)`
* `get_message(UUID, UUID)`

#### 4.1.2 权限原因

`e2ee_chat_service` 是应用程序使用的数据库服务账号。为了安全起见，它被授予了最小必要的权限，即只能连接数据库并执行预定义的存储过程。
这种做法符合最小权限原则，可以有效降低安全风险，防止应用程序直接操作数据表或执行其他未授权的操作。 该用户没有直接操作表、序列或执行其他函数的权限。