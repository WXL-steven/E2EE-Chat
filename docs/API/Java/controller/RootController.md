---
title: 根控制器 API
version: 1.0
last_updated: 2024-12-29
---

# 根控制器

**概述:**

`RootController` 是应用程序的根控制器，负责处理发送到根路径 (`/` 或空字符串) 的 HTTP 请求。它的主要功能是将所有到达根路径的请求重定向到
`/account` 路径，作为应用程序的入口点。

## 接口/端点

### GET - /

**描述:**

处理发送到根路径的 HTTP GET 请求。此端点会将客户端重定向到 `/account` 路径。

**请求方法:** `GET`

**URL:** `/`

**响应状态码:**

| 状态码 | 描述            |
|-----|---------------|
| 302 | 重定向到 /account |

### GET - (empty path)

**描述:**

处理发送到空路径的 HTTP GET 请求。此端点会将客户端重定向到 `/account` 路径。

**请求方法:** `GET`

**URL:** (empty)

**响应状态码:**

| 状态码 | 描述            |
|-----|---------------|
| 302 | 重定向到 /account |

### POST - /

**描述:**

处理发送到根路径的 HTTP POST 请求。此端点会将客户端重定向到 `/account` 路径。

**请求方法:** `POST`

**URL:** `/`

**响应状态码:**

| 状态码 | 描述            |
|-----|---------------|
| 302 | 重定向到 /account |

### POST - (empty path)

**描述:**

处理发送到空路径的 HTTP POST 请求。此端点会将客户端重定向到 `/account` 路径。

**请求方法:** `POST`

**URL:** (empty)

**响应状态码:**

| 状态码 | 描述            |
|-----|---------------|
| 302 | 重定向到 /account |

## 方法

### doGet

**描述:**

处理 HTTP GET 请求。接收到根路径的 GET 请求后，将客户端重定向到 `/account` 路径。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`

### doPost

**描述:**

处理 HTTP POST 请求。接收到根路径的 POST 请求后，将客户端重定向到 `/account` 路径。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`
