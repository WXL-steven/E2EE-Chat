# E2EE-Chat

一个基于Java EE Web Application的端到端加密即时通讯平台demo，我的课程设计项目。大道至简，注重核心通讯功能和安全性。

> [!CAUTION]
> 警告：本项目尚未完成，仅供学习演示，不适用于生产环境。

> [!WARNING]
> 本人尽全力保证加密方法正确性与内容安全性，但不提供任何担保。用户需要为自己的行为和可能的风险负责。

## 特性

- 🔒 端到端加密，保护您的隐私
- 🚀 轻量级设计，专注核心功能
- 📱 响应式界面，支持多端访问
- ✨ 支持Markdown基础格式
- 🔑 密钥完全由客户端控制

## 技术栈

### 后端
- Java EE 10
- PostgreSQL 17.2
- HikariCP 连接池
- Jakarta Servlet 6.1

### 前端
- HTML5 + CSS3
- WebCrypto API
- 纯原生JavaScript，无依赖

### 安全
- Argon2id 密码散列
- AES-256-GCM 消息加密
- P-256 密钥协商
- PBKDF2-SHA-256 密钥派生

## 文档

- [需求文档](./docs/PRD.md)
- 架构文档
  - [数据库架构](./docs/Architecture/Database.md)
  - [后端架构](./docs/Architecture/Backend.md)
  - [前端架构](./docs/Architecture/Frontend.md)

## 项目进度

### 已完成
- [x] 数据库设计
  - [x] 用户系统表结构
  - [x] 聊天系统表结构
  - [x] 存储过程和权限管理
- [x] 架构文档
  - [x] 数据库架构
  - [x] 后端架构

### 进行中
- [ ] 后端开发
  - [ ] 用户认证模块
  - [ ] 会话管理模块
  - [ ] 消息处理模块
- [ ] 前端开发
  - [ ] 用户界面设计
  - [ ] 加密模块实现
  - [ ] 消息格式化

## 路线图

1. 第一阶段（基础功能）
   - 完成用户认证系统
   - 实现基本的消息收发
   - 完成核心加密功能

2. 第二阶段（功能完善）
   - 添加消息格式化支持
   - 实现历史消息加载
   - 优化用户界面体验

3. 第三阶段（性能优化）
   - 实现消息缓存
   - 优化加密性能
   - 改进数据库查询

4. 第四阶段（安全加固）
   - 完成安全审计
   - 添加异常监控
   - 实现数据备份

## 开发环境要求

- JDK 21+
- PostgreSQL 17.2+
- Maven 3.9+
- 现代浏览器（支持WebCrypto API）

## 快速开始

1. 克隆仓库
```bash
git clone https://github.com/yourusername/e2ee-chat.git
cd e2ee-chat
```

2. 初始化数据库
```bash
cd sql
psql -U postgres -f 01_init_database.sql
psql -U postgres -d e2ee_chat -f 02_create_user_tables.sql
psql -U postgres -d e2ee_chat -f 03_create_user_procedures.sql
psql -U postgres -d e2ee_chat -f 04_create_chat_tables.sql
psql -U postgres -d e2ee_chat -f 05_create_chat_procedures.sql
psql -U postgres -d e2ee_chat -f 06_create_service_account.sql
```

3. 配置数据库连接
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
# 编辑 application.properties 配置数据库连接信息
```

4. 构建项目
```bash
mvn clean package
```

5. 部署到Servlet容器
```bash
cp target/e2ee-chat.war $CATALINA_HOME/webapps/
```

## 许可证

本项目采用 Apache License 2.0 许可证
