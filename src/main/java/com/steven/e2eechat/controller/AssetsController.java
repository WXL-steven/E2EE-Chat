package com.steven.e2eechat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code AssetsController} 用于处理对 `/assets/*` 路径的静态资源请求。
 * <p>
 * 该 Servlet 负责从 Web 应用的 `assets` 目录下检索请求的资源，并将其发送回客户端。
 * 它设置了缓存控制头以优化性能，并确保只提供 `assets` 目录下的文件，防止目录遍历攻击。
 */
@WebServlet(name = "AssetsController", urlPatterns = {"/assets/*"})
public class AssetsController extends HttpServlet {

    /**
     * 处理对静态资源的 HTTP GET 请求。
     * <p>
     * 接收客户端对 `/assets/*` 的请求，解析请求的资源路径，并在服务器的 `assets` 目录下查找对应的文件。
     * 如果文件存在且位于 `assets` 目录下，则设置正确的 `Content-Type` 头和缓存控制头，并将文件内容写入响应输出流。
     * 如果请求的资源不存在或路径不合法，则返回 404 错误。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果 Servlet 处理请求时遇到问题。
     * @throws IOException      如果在处理请求的 I/O 过程中发生错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取请求的资源路径信息
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 获取 Web 应用根目录的真实路径
        String webappPath = request.getServletContext().getRealPath("/");
        // 构建请求资源的完整路径
        Path resourcePath = Paths.get(webappPath, "assets", pathInfo);

        // 安全检查：确保请求的资源位于 assets 目录下，防止目录遍历攻击
        Path assetsRoot = Paths.get(webappPath, "assets").toAbsolutePath().normalize();
        Path absoluteResourcePath = resourcePath.toAbsolutePath().normalize();
        if (!Files.exists(resourcePath) || !absoluteResourcePath.startsWith(assetsRoot)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 设置响应的 Content-Type，根据文件扩展名获取 MIME 类型
        String contentType = getServletContext().getMimeType(resourcePath.toString());
        if (contentType != null) {
            response.setContentType(contentType);
        }

        // 设置缓存控制头，指示客户端可以缓存资源一年
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1年

        // 将请求的资源内容写入响应输出流
        Files.copy(resourcePath, response.getOutputStream());
    }
}
