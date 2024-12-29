---
title: 静态资源控制器 API
version: 1.0
last_updated: 2024-12-29
---

# 静态资源控制器

**概述:**

`AssetsController` 负责处理对 `/assets/*` 路径的静态资源请求。它从 Web 应用的 `assets`
目录下检索请求的资源，并将其发送回客户端。为了优化性能，该控制器设置了缓存控制头。同时，它实施了安全措施，确保只提供 `assets`
目录下的文件，以防止潜在的目录遍历攻击。

## 接口/端点

### GET - /assets/*

**描述:**

处理对静态资源的 GET 请求。接收客户端对 `/assets/*` 的请求，其中 `*` 代表请求的资源路径。控制器会在服务器的 `assets`
目录下查找对应的文件。

**请求方法:** `GET`

**URL:** `/assets/{资源路径}`

**请求参数:**

* **资源路径:**  位于 `/assets/` 之后的路径，指向请求的静态资源文件。例如：`/assets/css/style.css` 或 `/assets/js/main.js`。

**响应体 (Response Body):**

* **成功:**  如果请求的资源存在且位于 `assets` 目录下，服务器将返回该资源的内容。响应的 `Content-Type` 头会根据文件的扩展名进行设置（例如
  `text/css`，`application/javascript`，`image/png` 等）。同时，会设置 `Cache-Control` 头为 `public, max-age=31536000`
  ，指示客户端可以缓存该资源一年。
* **失败:** 如果请求的资源不存在或请求的路径尝试访问 `assets` 目录之外的文件，服务器将返回 **404 Not Found** 错误。

**响应状态码:**

| 状态码 | 描述          |
|-----|-------------|
| 200 | 请求成功，返回资源内容 |
| 404 | 资源未找到或路径不合法 |

## 方法

### doGet

**描述:**

处理对静态资源的 HTTP GET 请求。接收客户端的请求，解析资源路径，进行安全检查以防止目录遍历，设置响应头（包括 `Content-Type` 和
`Cache-Control`），并将资源内容写入响应输出流。

**参数:**

| 参数名      | 类型                    | 描述        |
|----------|-----------------------|-----------|
| request  | `HttpServletRequest`  | HTTP 请求对象 |
| response | `HttpServletResponse` | HTTP 响应对象 |

**返回值:** `void`