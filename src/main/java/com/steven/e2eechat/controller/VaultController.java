package com.steven.e2eechat.controller;

import com.google.gson.Gson;
import com.steven.e2eechat.dto.web.CreateVaultRequest;
import com.steven.e2eechat.model.UserProfile;
import com.steven.e2eechat.model.UserVault;
import com.steven.e2eechat.service.UserVaultService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Optional;

/**
 * {@code VaultController} 是一个 Servlet，负责处理与用户保险库相关的 HTTP 请求。
 * <p>
 * 它处理诸如创建、解锁和获取保险库信息的请求。所有端点都需要用户先登录。
 */
@WebServlet(name = "vaultController", urlPatterns = {
        "/vault",
        "/vault/",
        "/vault/setup",
        "/vault/unlock",
        "/vault/info"
})
public class VaultController extends HttpServlet {
    private final UserVaultService vaultService;
    private final Gson gson;

    public VaultController() {
        this.vaultService = new UserVaultService();
        this.gson = new Gson();
    }

    /**
     * 处理 HTTP GET 请求，根据不同的路径执行相应的操作。
     * <p>
     * 验证用户是否已登录。如果未登录，则重定向到登录页面。
     * 根据请求路径分发到不同的处理方法，包括显示保险库根页面、设置页面、解锁页面或获取保险库信息。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向或写入响应时发生 I/O 异常。
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

        // 获取用户资料
        UserProfile user = (UserProfile) session.getAttribute("user");
        String path = request.getServletPath();

        // 根据路径处理请求
        switch (path) {
            case "/vault", "/vault/" -> handleVaultRoot(request, response, user);
            case "/vault/setup" -> handleVaultSetup(request, response, user);
            case "/vault/unlock" -> handleVaultUnlock(request, response, user);
            case "/vault/info" -> handleGetVault(request, response, user);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理 HTTP POST 请求，根据不同的路径执行相应的操作。
     * <p>
     * 验证用户是否已登录。如果未登录，则返回 401 Unauthorized 状态码。
     * 目前只处理 "/vault/setup" 路径的创建保险库请求。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在读取请求体或写入响应时发生 I/O 异常。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String path = request.getServletPath();
        if ("/vault/setup".equals(path)) {
            handleCreateVault(request, response, (UserProfile) session.getAttribute("user"));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理获取保险库信息的 API 请求。
     * <p>
     * 从 {@link UserVaultService} 获取用户的保险库信息，并将结果以 JSON 格式写入响应。
     * 如果保险库不存在，则返回一个空的 JSON 对象。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @param user     当前登录的 {@link UserProfile} 对象。
     * @throws IOException 如果在写入响应时发生 I/O 异常。
     */
    private void handleGetVault(HttpServletRequest request, HttpServletResponse response, UserProfile user)
            throws IOException {
        // 获取保险库信息
        Optional<UserVault> vault = vaultService.getVault(user.getUserId());

        // 设置响应类型
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 如果保险库不存在，返回空对象
        if (vault.isEmpty()) {
            response.getWriter().write("{}");
            return;
        }

        // 构造响应JSON
        UserVault vaultData = vault.get();
        String json = gson.toJson(new com.steven.e2eechat.dto.web.VaultResponse(
                Optional.ofNullable(vaultData.getVaultMasterKey())
                        .map(key -> Base64.getEncoder().encodeToString(key))
                        .orElse(null),
                Optional.ofNullable(vaultData.getVaultSalt())
                        .map(salt -> Base64.getEncoder().encodeToString(salt))
                        .orElse(null),
                Optional.ofNullable(vaultData.getVaultIv())
                        .map(iv -> Base64.getEncoder().encodeToString(iv))
                        .orElse(null),
                Optional.ofNullable(vaultData.getEncryptedPrivateKey())
                        .map(key -> Base64.getEncoder().encodeToString(key))
                        .orElse(null),
                vaultData.isReady()
        ));

        response.getWriter().write(json);
    }

    /**
     * 处理创建保险库的 API 请求。
     * <p>
     * 从请求体中解析 {@link CreateVaultRequest} 对象，并调用 {@link UserVaultService} 创建用户保险库。
     * 根据操作结果，返回相应的 JSON 响应。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含保险库创建信息。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @param user     当前登录的 {@link UserProfile} 对象。
     * @throws IOException 如果在读取请求体或写入响应时发生 I/O 异常。
     */
    private void handleCreateVault(HttpServletRequest request, HttpServletResponse response, UserProfile user)
            throws IOException {
        try {
            // 解析请求体
            CreateVaultRequest vaultRequest = gson.fromJson(
                    new InputStreamReader(request.getInputStream()),
                    CreateVaultRequest.class
            );

            // 创建保险库
            boolean success = vaultService.createVault(
                    user.getUserId(),
                    Base64.getDecoder().decode(vaultRequest.getVaultSalt()),
                    Base64.getDecoder().decode(vaultRequest.getVaultIv()),
                    Base64.getDecoder().decode(vaultRequest.getEncryptedPrivateKey()),
                    Base64.getDecoder().decode(vaultRequest.getPublicKey())
            );

            // 设置响应
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (success) {
                response.getWriter().write("{\"success\":true}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"创建保险库失败\"}");
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(String.format(
                    "{\"success\":false,\"message\":\"%s\"}",
                    e.getMessage()
            ));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"服务器内部错误\"}");
        }
    }

    /**
     * 处理保险库根路径的请求。
     * <p>
     * 检查用户是否已设置公钥以及保险库是否已就绪。
     * 如果未设置或未就绪，则重定向到保险库设置页面。
     * 否则，转发到保险库的首页 JSP 页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @param user     当前登录的 {@link UserProfile} 对象。
     * @throws ServletException 如果在转发请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向时发生 I/O 异常。
     */
    private void handleVaultRoot(HttpServletRequest request, HttpServletResponse response, UserProfile user)
            throws ServletException, IOException {
        // 检查用户是否有公钥
        if (user.getPublicKey() == null) {
            response.sendRedirect(request.getContextPath() + "/vault/setup");
            return;
        }

        // 检查保险库是否就绪
        Optional<UserVault> vault = vaultService.getVault(user.getUserId());
        if (vault.isEmpty() || !vault.get().isReady()) {
            response.sendRedirect(request.getContextPath() + "/vault/setup");
            return;
        }

        // 显示保险库根页面
        request.getRequestDispatcher("/WEB-INF/vault/index.jsp").forward(request, response);
    }

    /**
     * 处理保险库设置页面的请求。
     * <p>
     * 检查用户的保险库状态。如果保险库已经就绪，则重定向到保险库根路径。
     * 否则，转发到保险库设置的 JSP 页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @param user     当前登录的 {@link UserProfile} 对象。
     * @throws ServletException 如果在转发请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向时发生 I/O 异常。
     */
    private void handleVaultSetup(HttpServletRequest request, HttpServletResponse response, UserProfile user)
            throws ServletException, IOException {
        // 检查保险库状态
        Optional<UserVault> vault = vaultService.getVault(user.getUserId());

        // 如果保险库已经就绪，重定向到根路径
        if (vault.isPresent() && vault.get().isReady() && user.getPublicKey() != null) {
            response.sendRedirect(request.getContextPath() + "/vault");
            return;
        }

        // 显示设置页面
        request.getRequestDispatcher("/WEB-INF/vault/setup.jsp").forward(request, response);
    }

    /**
     * 处理保险库解锁页面的请求。
     * <p>
     * 检查用户的保险库状态。如果保险库未设置或未就绪，则重定向到保险库设置页面。
     * 否则，转发到保险库解锁的 JSP 页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @param user     当前登录的 {@link UserProfile} 对象。
     * @throws ServletException 如果在转发请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向时发生 I/O 异常。
     */
    private void handleVaultUnlock(HttpServletRequest request, HttpServletResponse response, UserProfile user)
            throws ServletException, IOException {
        // 检查保险库状态
        Optional<UserVault> vault = vaultService.getVault(user.getUserId());

        // 如果保险库未设置或未就绪，重定向到设置页面
        if (vault.isEmpty() || !vault.get().isReady() || user.getPublicKey() == null) {
            response.sendRedirect(request.getContextPath() + "/vault/setup");
            return;
        }

        // 显示解锁页面
        request.getRequestDispatcher("/WEB-INF/vault/unlock.jsp").forward(request, response);
    }
}
