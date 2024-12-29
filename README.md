# E2EE-Chat: 端到端加密聊天平台

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> [!WARNING]
> 这是我的课程设计项目，仅供学习参考，未就绪于投入生产环境。

> [!WARNING]
> 本人尽全力保证加密方法正确性与内容安全性，但不提供任何担保。用户需要为自己的行为和可能的风险负责。

## 项目简介

E2EE-Chat 是一个基于 Java EE 构建的端到端加密聊天平台demo。该平台旨在提供安全可靠的通信方式，确保用户的消息内容只能被通信双方解密，从而保护用户隐私。

本项目利用现代 Web 技术和强大的加密算法，提供了一套完整的用户管理、密钥管理和会话管理机制，为用户提供安全便捷的聊天体验。

## 主要特性

* **端到端加密 (E2EE):**  所有消息在发送端加密，接收端解密，服务端无法访问消息明文。
* **用户管理:**
    * 用户注册（用户名、显示名、密码），客户端校验和服务器端用户名唯一性校验。
    * 密码使用 Argon2id 加盐哈希存储，增强安全性。
    * 用户登录，服务器端使用存储的盐进行 Argon2id 哈希校验。
    * 登录成功后，客户端接收一个 32 字节的安全随机用户主密钥。
    * 用户登出，服务端会话失效。
* **密钥管理 (Vault Management):**
    * 用户首次登录需要创建或解密密钥保险库。
    * **创建保险库:** 使用 4 位 PIN 码，通过 HKDF 与用户主密钥衍生出保险库密钥。
    * 生成 P-256 密钥对，使用 AES-256-GCM 加密私钥，并存储加密后的私钥、公钥、盐和 IV。
    * 私钥的 Base64 编码存储在 sessionStorage 中。
    * **解密保险库:** 使用 4 位 PIN 码，通过 HKDF 衍生密钥解密存储的私钥。
* **会话管理:**
    * **会话列表:**  3 秒轮询更新，展示在线用户、最后上线时间、最后消息，提供登出和发起聊天功能。
    * **聊天:**
        * 3 秒轮询更新消息。
        * 页面嵌入接收者公钥 (Base64)。
        * 使用 ECDH+P-256 算法为每个会话衍生 AES-GCM (256-bit) 会话密钥。
        * 消息内容使用 AES-GCM 加密，包含 IV 和密文。
        * 使用正则表达式和 JavaScript 进行 Markdown 解析。
* **非功能特性:**
    * **安全性:** 端到端加密，Argon2id 密码哈希，基于 PIN 码和主密钥的保险库密钥，会话管理。
    * **实时性:** 3 秒轮询机制。
    * **前端技术:** Tailwind CSS 提供美观的界面。
    * **用户体验:** SnackBar 提供友好的错误提示。
* **安全措施:**
    * **XSS 防护:** 使用 JSTL 处理显示名称，使用 JS 解析和渲染消息内容。
    * **数据库安全:** 所有数据库操作通过存储过程完成，使用具有 `EXECUTE` 权限的程序操作用户，并使用参数化查询防止 SQL 注入。

## 技术栈

* **后端:** Java EE
* **前端:** HTML, Tailwind CSS, JavaScript
* **Servlet 容器:** Tomcat
* **数据库:** PostgreSQL
* **构建工具:** Maven

**依赖库:**

* Servlet API
* JSP API
* PostgreSQL driver
* HikariCP
* Bouncy Castle
* Gson
* JSTL API
* JSTL implementation

## 文档

更详细的文档位于 `docs` 目录下。

## 开发环境要求

- JDK 21+
- PostgreSQL 17.2+
- Maven 3.9+
- 现代浏览器（支持 WebCrypto API）

## 快速开始

1. 克隆仓库
```bash
git clone https://github.com/WXL-steven/E2EE-Chat.git
cd E2EE-Chat
```

2. 初始化数据库
```bash
cd sql
psql -U postgres -f 01_init_database.sql
psql -U postgres -d e2ee_chat -f 02_create_user_tables.sql
psql -U postgres -d e2ee_chat -f 03_create_user_procedures.sql
psql -U postgres -d e2ee_chat -f 04_create_chat_tables.sql
psql -U postgres -d e2ee_chat -f 05_create_chat_procedures.sql
psql -U postgres -d e2ee_chat -f 06_create_service_account.sql  # 请在执行前修改服务账号密码
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

5. 部署到 Servlet 容器
```bash
cp target/e2ee-chat.war $CATALINA_HOME/webapps/
```

## CSS 编译

本项目使用 Tailwind CSS 进行样式管理。CSS 源码位于 `src/main/webapp/assets/css/dev` 目录，编译后的 CSS 文件位于 `src/main/webapp/assets/css/dist` 目录。

**确保你已经安装了 Node.js 和 npm (或 bun)。**

### 编译 CSS

```bash
npx tailwindcss -i ./src/main/webapp/assets/css/dev/styles.css -o ./src/main/webapp/assets/css/dist/styles.css
```

或者使用 bun:

```bash
bunx tailwindcss -i ./src/main/webapp/assets/css/dev/styles.css -o ./src/main/webapp/assets/css/dist/styles.css
```

### 监视 CSS 变化

```bash
npx tailwindcss -i ./src/main/webapp/assets/css/dev/styles.css -o ./src/main/webapp/assets/css/dist/styles.css --watch
```

或者使用 bun:

```bash
bunx tailwindcss -i ./src/main/webapp/assets/css/dev/styles.css -o ./src/main/webapp/assets/css/dist/styles.css --watch
```

## 贡献

欢迎任何形式的贡献！请随意提交 Issue 或 Pull Request。

## 许可证

本项目使用 Apache License 2.0 许可证，详情请查看 [LICENSE](LICENSE) 文件。
