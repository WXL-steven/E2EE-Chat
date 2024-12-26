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
 * 静态资源控制器
 * 处理所有/assets/*请求，提供静态资源访问
 */
@WebServlet(name = "AssetsController", urlPatterns = {"/assets/*"})
public class AssetsController extends HttpServlet {
    
    /**
     * 处理GET请求
     * 返回请求的静态资源
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取请求的资源路径
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 获取webapp目录的真实路径
        String webappPath = request.getServletContext().getRealPath("/");
        Path resourcePath = Paths.get(webappPath, "assets", pathInfo);

        // 检查文件是否存在且在assets目录下
        if (!Files.exists(resourcePath) || !resourcePath.startsWith(Paths.get(webappPath, "assets"))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 设置Content-Type
        String contentType = getServletContext().getMimeType(resourcePath.toString());
        if (contentType != null) {
            response.setContentType(contentType);
        }

        // 设置缓存控制
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1年
        
        // 发送文件
        Files.copy(resourcePath, response.getOutputStream());
    }
}
