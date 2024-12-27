package com.steven.e2eechat.controller;

import com.steven.e2eechat.model.ChatSession;
import com.steven.e2eechat.model.UserProfile;
import com.steven.e2eechat.model.ChatMessage;
import com.steven.e2eechat.dto.db.NewMessageDTO;
import com.steven.e2eechat.service.SessionService;
import com.steven.e2eechat.service.UserService;
import com.steven.e2eechat.service.MessageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.io.BufferedReader;

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
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
    private final SessionService sessionService;
    private final UserService userService;
    private final MessageService messageService;
    private final Gson gson;

    public SessionsController() {
        this.sessionService = new SessionService();
        this.userService = new UserService();
        this.messageService = new MessageService();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                    @Override
                    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
                    }

                    @Override
                    public OffsetDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return OffsetDateTime.parse(in.nextString());
                    }
                })
                .create();
    }

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
        if (pathInfo == null || "/".equals(pathInfo)) {
            request.getRequestDispatcher("/WEB-INF/sessions/index.jsp")
                    .forward(request, response);
        } else if ("/list".equals(pathInfo)) {
            handleSessionsList(request, response);
        } else if (pathInfo.endsWith("/messages")) {
            handleGetMessages(request, response);
        } else {
            // 移除开头的斜杠
            String sessionId = pathInfo.substring(1);
            handleSessionDetail(request, response, sessionId);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/account");
            return;
        }

        String pathInfo = request.getPathInfo();
        if ("/new".equals(pathInfo)) {
            handleNewSession(request, response);
        } else if (pathInfo.endsWith("/messages")) {
            handleSendMessage(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理会话列表请求
     */
    private void handleSessionsList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserProfile currentUser = (UserProfile) request.getSession().getAttribute("user");
        
        // 获取最近会话列表
        List<ChatSession> sessions = sessionService.getRecentSessions(currentUser.getUserId());
        
        // 获取所有相关用户的资料
        Set<UUID> userIds = new HashSet<>();
        for (ChatSession session : sessions) {
            if (!session.getInitiatorId().equals(currentUser.getUserId())) {
                userIds.add(session.getInitiatorId());
            }
            if (!session.getParticipantId().equals(currentUser.getUserId())) {
                userIds.add(session.getParticipantId());
            }
        }
        
        Map<UUID, UserProfile> profiles = new HashMap<>();
        for (UUID userId : userIds) {
            userService.getUserById(userId).ifPresent(profile -> 
                profiles.put(userId, profile)
            );
        }
        
        // 设置请求属性
        request.setAttribute("sessions", sessions);
        request.setAttribute("profiles", profiles);
        
        // 转发到列表视图
        request.getRequestDispatcher("/WEB-INF/sessions/list.jsp")
                .forward(request, response);
    }

    /**
     * 处理创建新会话的请求
     */
    private void handleNewSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        UserProfile currentUser = (UserProfile) session.getAttribute("user");
        String username = request.getParameter("username");
        
        if (username == null || username.trim().isEmpty()) {
            session.setAttribute("messageLevel", "error");
            session.setAttribute("messageContent", "用户名不能为空");
            response.sendRedirect(request.getContextPath() + "/sessions");
            return;
        }

        // 处理用户名，去除首尾空格和@符号
        username = username.trim();
        if (username.contains("@")) {
            username = username.replaceAll("@", "");
            username = username.trim();  // 再次去除可能的首尾空格
        }

        // 查找目标用户
        Optional<UUID> targetUserUuid = userService.getUserUuidByUsername(username);
        if (targetUserUuid.isEmpty()) {
            session.setAttribute("messageLevel", "error");
            session.setAttribute("messageContent", "用户不存在");
            response.sendRedirect(request.getContextPath() + "/sessions");
            return;
        }

        Optional<UserProfile> targetUser = userService.getUserById(targetUserUuid.get());
        if (targetUser.isEmpty()) {
            session.setAttribute("messageLevel", "error");
            session.setAttribute("messageContent", "用户不存在");
            response.sendRedirect(request.getContextPath() + "/sessions");
            return;
        }

        // 不能和自己创建会话
        if (targetUser.get().getUserId().equals(currentUser.getUserId())) {
            session.setAttribute("messageLevel", "error");
            session.setAttribute("messageContent", "不能和自己创建会话");
            response.sendRedirect(request.getContextPath() + "/sessions");
            return;
        }

        // 获取或创建会话
        Optional<UUID> sessionId = sessionService.getOrCreateSession(
            currentUser.getUserId(), 
            targetUser.get().getUserId()
        );
        
        if (sessionId.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/sessions/" + sessionId.get().toString());
        } else {
            session.setAttribute("messageLevel", "error");
            session.setAttribute("messageContent", "创建会话失败");
            response.sendRedirect(request.getContextPath() + "/sessions");
        }
    }

    /**
     * 处理特定会话的请求
     */
    private void handleSessionDetail(HttpServletRequest request, HttpServletResponse response, String sessionId)
            throws ServletException, IOException {
        try {
            // 验证会话ID格式
            if (!UUID_PATTERN.matcher(sessionId.toLowerCase()).matches()) {
                throw new IllegalArgumentException("无效的会话ID格式");
            }
            
            // 尝试解析UUID
            UUID sessionUUID = UUID.fromString(sessionId);
            UserProfile currentUser = (UserProfile) request.getSession().getAttribute("user");
            
            // 获取会话信息
            Optional<ChatSession> session = sessionService.getSession(currentUser.getUserId(), sessionUUID);
            if (session.isEmpty()) {
                throw new IllegalArgumentException("会话不存在或无权访问");
            }
            
            // 设置会话信息到请求属性
            request.setAttribute("session", session.get());
            
            // 转发到聊天页面
            request.getRequestDispatcher("/WEB-INF/sessions/chat.jsp").forward(request, response);
            
        } catch (Exception e) {
            // 设置错误消息
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", e.getMessage());
            // 重定向回会话列表
            response.sendRedirect(request.getContextPath() + "/sessions");
        }
    }

    /**
     * 处理获取消息请求
     */
    private void handleGetMessages(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserProfile currentUser = (UserProfile) request.getSession().getAttribute("user");
        
        // 从路径中提取会话ID
        String pathInfo = request.getPathInfo();
        String sessionIdStr = pathInfo.substring(1, pathInfo.length() - 9); // 移除开头的/和结尾的/messages
        
        if (!UUID_PATTERN.matcher(sessionIdStr).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid session ID format");
            return;
        }
        
        UUID sessionId = UUID.fromString(sessionIdStr);
        
        // 获取查询参数
        String cursorStr = request.getParameter("cursor");
        String limitStr = request.getParameter("limit");
        String direction = request.getParameter("direction");
        
        Long cursor = null;
        if (cursorStr != null && !cursorStr.isEmpty()) {
            try {
                cursor = Long.parseLong(cursorStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid cursor format");
                return;
            }
        }
        
        Integer limit = null;
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid limit format");
                return;
            }
        }
        
        List<ChatMessage> messages;
        if ("after".equals(direction)) {
            messages = messageService.getMessagesAfter(currentUser.getUserId(), sessionId, cursor, limit);
        } else {
            messages = messageService.getMessagesBefore(currentUser.getUserId(), sessionId, cursor, limit);
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(messages));
    }

    /**
     * 处理发送消息请求
     */
    private void handleSendMessage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserProfile currentUser = (UserProfile) request.getSession().getAttribute("user");
        
        // 从路径中提取会话ID
        String pathInfo = request.getPathInfo();
        String sessionIdStr = pathInfo.substring(1, pathInfo.length() - 9); // 移除开头的/和结尾的/messages
        
        if (!UUID_PATTERN.matcher(sessionIdStr).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid session ID format");
            return;
        }
        
        // 读取请求体
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        JsonObject jsonRequest;
        try {
            jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }
        
        // 验证必要字段
        if (!jsonRequest.has("message_content") || !jsonRequest.has("message_iv")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required fields");
            return;
        }
        
        // 获取并验证消息内容
        String messageContent = jsonRequest.get("message_content").getAsString();
        String messageIv = jsonRequest.get("message_iv").getAsString();
        boolean isSystem = jsonRequest.has("is_system") && jsonRequest.get("is_system").getAsBoolean();
        
        // Base64解码并验证消息大小
        byte[] decodedContent;
        try {
            decodedContent = Base64.getDecoder().decode(messageContent);
            if (decodedContent.length > 65535) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message content too large");
                return;
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Base64 encoding");
            return;
        }
        
        // 解码IV
        byte[] decodedIv;
        try {
            decodedIv = Base64.getDecoder().decode(messageIv);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid IV Base64 encoding");
            return;
        }
        
        // 创建新消息DTO
        NewMessageDTO newMessage = new NewMessageDTO();
        newMessage.setSessionId(UUID.fromString(sessionIdStr));
        newMessage.setMessageContent(decodedContent);
        newMessage.setMessageIv(decodedIv);
        newMessage.setSystem(isSystem);
        
        // 发送消息
        boolean success = messageService.sendMessage(currentUser.getUserId(), newMessage);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
