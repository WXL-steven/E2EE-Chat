---
title: 账户控制器 API
version: 1.0
last_updated: 2024-12-29
---

# 账户控制器

**概述:**

`AccountController` 负责处理用户账户相关的操作，包括用户注册、登录、注销以及检查用户名是否可用。它作为 Web 层入口，接收客户端请求，并调用
`UserService` 处理相应的业务逻辑。此控制器通过 Servlet 技术实现，并映射到 `/account` 及其子路径。

## 接口/端点

### GET - /account

**描述:**

如果用户未登录，则重定向到 `/account/login` 页面。如果用户已登录，则重定向到 `/vault` 页面。

**请求方法:** `GET`

**URL:** `/account`

**响应状态码:**

| 状态码 | 描述          |
|-----|-------------|
| 302 | 重定向到登录页或保险库 |

### GET - /account/register

**描述:**

显示用户注册页面。

**请求方法:** `GET`

**URL:** `/account/register`

**响应状态码:**

| 状态码 | 描述   |
|-----|------|
| 200 | 请求成功 |

### GET - /account/login

**描述:**

显示用户登录页面。

**请求方法:** `GET`

**URL:** `/account/login`

**响应状态码:**

| 状态码 | 描述   |
|-----|------|
| 200 | 请求成功 |

### GET - /account/logout

**描述:**

处理用户注销操作。清除当前用户的会话信息，并将用户重定向到登录页面。

**请求方法:** `GET`

**URL:** `/account/logout`

**响应状态码:**

| 状态码 | 描述      |
|-----|---------|
| 302 | 重定向到登录页 |

### POST - /account/register

**描述:**

处理用户注册请求。接收用户提交的注册信息，验证数据的有效性，并调用 `UserService` 进行用户注册。

**请求方法:** `POST`

**URL:** `/account/register`

**请求体 (Request Body):** `application/x-www-form-urlencoded`

| 参数名         | 类型        | 是否必须 | 描述                         | 示例            |
|-------------|-----------|------|----------------------------|---------------|
| username    | `String`  | 是    | 用户名 (1-16位小写字母、数字、下划线或连字符) | `testuser`    |
| displayName | `String`  | 是    | 显示名称 (非空)                  | `Test User`   |
| password    | `String`  | 是    | 密码 (8-64位ASCII字符)          | `Password123` |
| trustDevice | `Boolean` | 否    | 是否信任此设备                    | `true`        |

**响应体 (Response Body):**

* **注册成功 (重定向):**
    * 重定向到 `/vault` 页面。
    * Session 中包含 `messageLevel: success` 和 `messageContent: 注册成功`。
* **注册失败 (重定向):**
    * 重定向回 `/account/register` 页面。
    * Session 中包含 `messageLevel: error` 和 `messageContent: 注册失败，请重试` 或其他错误信息（例如用户名或密码格式错误，显示名称不能为空）。

**响应状态码:**

| 状态码 | 描述  |
|-----|-----|
| 302 | 重定向 |

### POST - /account/login

**描述:**

处理用户登录请求。接收用户提交的登录信息，验证用户身份。

**请求方法:** `POST`

**URL:** `/account/login`

**请求体 (Request Body):** `application/x-www-form-urlencoded`

| 参数名         | 类型        | 是否必须 | 描述      | 示例            |
|-------------|-----------|------|---------|---------------|
| username    | `String`  | 是    | 用户名     | `testuser`    |
| password    | `String`  | 是    | 密码      | `Password123` |
| trustDevice | `Boolean` | 否    | 是否信任此设备 | `true`        |

**响应体 (Response Body):**

* **登录成功 (重定向):**
    * 重定向到 `/vault` 页面。
    * Session 中包含 `user` 属性（UserProfile 对象），`messageLevel: success` 和 `messageContent: 登录成功`。
* **登录失败 (重定向):**
    * 重定向回 `/account/login` 页面。
    * Session 中包含 `messageLevel: error` 和 `messageContent: 用户名或密码错误` 或 `输入格式错误`。

**响应状态码:**

| 状态码 | 描述  |
|-----|-----|
| 302 | 重定向 |

### POST - /account/check-username

**描述:**

检查用户名是否可用。接收用户名作为参数，并返回 JSON 格式的响应，指示用户名是否已被使用。

**请求方法:** `POST`

**URL:** `/account/check-username`

**请求参数:**

| 参数名      | 类型       | 是否必须 | 描述                             | 示例         |
|----------|----------|------|--------------------------------|------------|
| username | `String` | 是    | 要检查的用户名 (1-16位小写字母、数字、下划线或连字符) | `testuser` |

**响应体 (Response Body):** `application/json`

用户名可用
```json
{
  "available": true,
  "message": "用户名可用"
}
```

用户名不可用
```json
{
  "available": false,
  "message": "用户名已被使用"
}
```

用户名格式错误
```json
{
  "available": false,
  "message": "用户名格式错误"
}
```

**响应状态码:**

| 状态码 | 描述   |
|-----|------|
| 200 | 请求成功 |

## 方法

### handleRegister

**描述:**

处理用户注册的内部逻辑。从请求中获取注册信息，进行格式校验，并调用 `UserService` 的 `register` 方法。根据注册结果设置会话属性并进行重定向。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`

### handleLogin

**描述:**

处理用户登录的内部逻辑。从请求中获取登录信息，进行格式校验，并调用 `UserService` 的 `login` 方法进行身份验证。根据登录结果设置会话属性并进行重定向。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`

### handleCheckUsername

**描述:**

处理检查用户名是否可用的内部逻辑。从请求中获取用户名，进行格式校验，并调用 `UserService` 的 `checkUsernameAvailable`
方法。将结果以 JSON 格式写入响应。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`

### handleLogout

**描述:**

处理用户注销的内部逻辑。使当前的 HTTP 会话失效，清除用户登录状态。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`

### validateUsername

**描述:**

验证给定的用户名是否符合预定义的格式要求。

**参数:**

| 参数名      | 类型       | 描述      |
|----------|----------|---------|
| username | `String` | 要验证的用户名 |

**返回值:** `boolean` - 如果用户名格式正确则返回 `true`，否则返回 `false`。

### validatePassword

**描述:**

验证给定的密码是否符合预定义的格式要求。

**参数:**

| 参数名      | 类型       | 描述     |
|----------|----------|--------|
| password | `String` | 要验证的密码 |

**返回值:** `boolean` - 如果密码格式正确则返回 `true`，否则返回 `false`。