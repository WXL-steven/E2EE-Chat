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
 * 保险库控制器
 * 处理用户保险库相关的请求
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
     * 处理获取保险库信息的API请求
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
        String json = gson.toJson(new VaultResponse(
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
     * 处理创建保险库的API请求
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
                vaultRequest.getVaultSalt(),
                vaultRequest.getVaultIv(),
                vaultRequest.getEncryptedPrivateKey(),
                vaultRequest.getPublicKey()
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
     * 处理保险库根路径请求
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
     * 处理保险库设置页面请求
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
     * 处理保险库解锁页面请求
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

    /**
     * 保险库响应DTO
     */
    private static class VaultResponse {
        private final String vaultMasterKey;
        private final String vaultSalt;
        private final String vaultIv;
        private final String encryptedPrivateKey;
        private final boolean ready;

        public VaultResponse(String vaultMasterKey, String vaultSalt, String vaultIv, 
                           String encryptedPrivateKey, boolean ready) {
            this.vaultMasterKey = vaultMasterKey;
            this.vaultSalt = vaultSalt;
            this.vaultIv = vaultIv;
            this.encryptedPrivateKey = encryptedPrivateKey;
            this.ready = ready;
        }
    }
}
