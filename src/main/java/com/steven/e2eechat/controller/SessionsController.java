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
 * {@code SessionsController} 负责处理用户会话相关的 HTTP 请求。
 * <p>
 * 提供了会话列表展示、创建新会话、查看会话详情以及发送和接收消息的功能。
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

    /**
     * 默认构造器，初始化 {@link SessionService}, {@link UserService}, {@link MessageService} 和配置了 {@link OffsetDateTime} 适配器的 {@link Gson} 实例。
     */
    public SessionsController() {
        this.sessionService = new SessionService();
        this.userService = new UserService();
        this.messageService = new MessageService();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                    /**
                     * 将 {@link OffsetDateTime} 对象写入 JSON 输出流。如果值为 null，则写入 JSON null。
                     *
                     * @param out   JSON 输出流。
                     * @param value 要写入的 {@link OffsetDateTime} 对象。
                     * @throws IOException 如果写入过程中发生 I/O 错误。
                     */
                    @Override
                    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
                    }

                    /**
                     * 从 JSON 输入流中读取 {@link OffsetDateTime} 对象。如果遇到 JSON null，则返回 null。
                     *
                     * @param in JSON 输入流。
                     * @return 从输入流中读取的 {@link OffsetDateTime} 对象，如果为 null 则返回 null。
                     * @throws IOException 如果读取过程中发生 I/O 错误或 JSON 格式不正确。
                     */
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

    /**
     * 处理 HTTP GET 请求，根据请求路径执行不同的操作。
     * <ul>
     *     <li>`/sessions` 或 `/sessions/`: 显示会话列表页面。</li>
     *     <li>`/sessions/list`: 获取并返回会话列表数据。</li>
     *     <li>`/sessions/{sessionId}`: 显示特定会话的聊天页面。</li>
     *     <li>`/sessions/{sessionId}/messages`: 获取特定会话的消息。</li>
     * </ul>
     * 如果用户未登录，则重定向到登录页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向或转发时发生 I/O 异常。
     */
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

    /**
     * 处理 HTTP POST 请求，根据请求路径执行不同的操作。
     * <ul>
     *     <li>`/sessions/new`: 创建新的会话。</li>
     *     <li>`/sessions/{sessionId}/messages`: 发送消息到特定会话。</li>
     * </ul>
     * 如果用户未登录，则重定向到登录页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向或发送错误时发生 I/O 异常。
     */
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
     * 处理会话列表请求，获取当前用户的最近会话列表，并将相关数据设置到请求属性中，最后转发到会话列表视图。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在转发请求时发生 Servlet 异常。
     * @throws IOException      如果在转发请求时发生 I/O 异常。
     */
    private void handleSessionsList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserProfile currentUser = (UserProfile) session.getAttribute("user");

        // 获取最近会话列表
        List<ChatSession> sessions = sessionService.getRecentSessions(currentUser.getUserId());

        // 获取所有相关用户的资料
        Set<UUID> userIds = new HashSet<>();
        for (ChatSession chatSession : sessions) {
            if (!chatSession.getInitiatorId().equals(currentUser.getUserId())) {
                userIds.add(chatSession.getInitiatorId());
            }
            if (!chatSession.getParticipantId().equals(currentUser.getUserId())) {
                userIds.add(chatSession.getParticipantId());
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
     * 处理创建新会话的请求，从请求参数中获取目标用户名，并创建与该用户的新会话。
     * 如果用户名为空或目标用户不存在，则设置错误消息并重定向回会话列表页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含目标用户的用户名参数。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws IOException 如果在重定向时发生 I/O 异常。
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
     * 处理特定会话的详情查看请求，验证会话ID格式，并获取会话信息，最后转发到聊天页面。
     * 如果会话ID格式不正确或会话不存在，则设置错误消息并重定向回会话列表页面。
     *
     * @param request   客户端发送的 {@link HttpServletRequest} 对象。
     * @param response  服务器发送的 {@link HttpServletResponse} 对象。
     * @param sessionId 会话ID字符串。
     * @throws ServletException 如果在转发请求时发生 Servlet 异常。
     * @throws IOException      如果在重定向时发生 I/O 异常。
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
     * 处理获取消息的请求，根据提供的游标和限制获取指定会话的消息，并将消息以 JSON 格式返回。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含会话ID、游标和限制参数。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在写入响应时发生 I/O 异常。
     */
    private void handleGetMessages(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserProfile currentUser = (UserProfile) session.getAttribute("user");

        // 从路径中提取会话ID
        String pathInfo = request.getPathInfo();
        // 检查 pathInfo 是否为 null 或长度不足
        if (pathInfo == null || pathInfo.length() <= 9) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
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
     * 处理发送消息的请求，接收包含消息内容和初始化向量的 JSON 数据，并将消息保存到数据库。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含消息数据的 JSON 请求体。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在读取请求体或写入响应时发生 I/O 异常。
     */
    private void handleSendMessage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserProfile currentUser = (UserProfile) session.getAttribute("user");

        // 从路径中提取会话ID
        String pathInfo = request.getPathInfo();
        // 检查 pathInfo 是否为 null 或长度不足
        if (pathInfo == null || pathInfo.length() <= 9) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
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
