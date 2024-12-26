package com.steven.e2eechat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 会话控制器
 * 处理用户会话相关的请求
 */
@WebServlet(name = "sessionsController", urlPatterns = {
        "/sessions",
        "/sessions/",
        "/sessions/*"
})
public class SessionsController extends HttpServlet {
    private static final Pattern UUID_PATTERN = 
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/account");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 处理根路径请求
            request.getRequestDispatcher("/WEB-INF/sessions/index.jsp").forward(request, response);
        } else {
            // 处理会话ID请求
            handleSessionDetail(request, response, pathInfo.substring(1));
        }
    }

    /**
     * 处理特定会话的请求
     */
    private void handleSessionDetail(HttpServletRequest request, HttpServletResponse response, String sessionId)
            throws IOException {
        try {
            // 验证会话ID格式
            if (!UUID_PATTERN.matcher(sessionId.toLowerCase()).matches()) {
                throw new IllegalArgumentException("无效的会话ID格式");
            }
            
            // 尝试解析UUID
            UUID.fromString(sessionId);
            
            // TODO: 处理特定会话的逻辑
            
        } catch (Exception e) {
            // 设置错误消息
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "无效的会话ID");
            // 重定向回会话列表
            response.sendRedirect(request.getContextPath() + "/sessions");
        }
    }
}
