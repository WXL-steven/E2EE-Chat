package com.steven.e2eechat.controller;

import com.steven.e2eechat.dto.web.LoginRequest;
import com.steven.e2eechat.dto.web.RegisterRequest;
import com.steven.e2eechat.model.UserProfile;
import com.steven.e2eechat.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet(name = "accountController", urlPatterns = {
        "/account",
        "/account/",
        "/account/register",
        "/account/login",
        "/account/check-username",
        "/account/logout"
})
public class AccountController extends HttpServlet {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{1,16}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[\\x20-\\x7E]{8,64}$");
    private static final int WEEK_IN_SECONDS = 7 * 24 * 60 * 60;
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // 已登录用户重定向到保险库
            if ("/account".equals(path) || "/account/".equals(path) || 
                "/account/login".equals(path) || "/account/register".equals(path)) {
                response.sendRedirect(request.getContextPath() + "/vault");
                return;
            }
        }
        
        // 处理各个路径
        switch (path) {
            case "/account", "/account/" -> response.sendRedirect(request.getContextPath() + "/account/login");
            case "/account/register" -> request.getRequestDispatcher("/WEB-INF/account/register.jsp")
                    .forward(request, response);
            case "/account/login" -> request.getRequestDispatcher("/WEB-INF/account/login.jsp")
                    .forward(request, response);
            case "/account/logout" -> handleLogout(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/account/register" -> handleRegister(request, response);
            case "/account/login" -> handleLogin(request, response);
            case "/account/check-username" -> handleCheckUsername(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理注册请求
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 解析请求
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(request.getParameter("username"));
        registerRequest.setDisplayName(request.getParameter("displayName").trim());
        registerRequest.setPassword(request.getParameter("password"));
        boolean trustDevice = Boolean.parseBoolean(request.getParameter("trustDevice"));

        // 验证输入
        if (!validateUsername(registerRequest.getUsername())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "用户名格式错误");
            response.sendRedirect("./register");
            return;
        }
        if (!validatePassword(registerRequest.getPassword())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "密码格式错误");
            response.sendRedirect("./register");
            return;
        }
        if (registerRequest.getDisplayName() == null || registerRequest.getDisplayName().trim().isEmpty()) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "显示名称不能为空");
            response.sendRedirect("./register");
            return;
        }

        // 注册用户
        Optional<UserProfile> profile = userService.register(registerRequest);
        if (profile.isPresent()) {
            // 设置会话
            HttpSession session = request.getSession();
            session.setAttribute("user", profile.get());
            session.setAttribute("messageLevel", "success");
            session.setAttribute("messageContent", "注册成功");
            session.setAttribute("shouldRedirect", true);
            
            // 如果信任设备，设置会话过期时间为7天
            if (trustDevice) {
                session.setMaxInactiveInterval(WEEK_IN_SECONDS);
                request.setAttribute("persistSession", true);
            }
            
            response.sendRedirect("${pageContext.request.contextPath}/vault");
        } else {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "注册失败，请重试");
            response.sendRedirect("./register");
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 解析请求
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getParameter("username"));
        loginRequest.setPassword(request.getParameter("password"));
        boolean trustDevice = Boolean.parseBoolean(request.getParameter("trustDevice"));

        // 验证输入
        if (!validateUsername(loginRequest.getUsername()) ||
            !validatePassword(loginRequest.getPassword())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "输入格式错误");
            response.sendRedirect("./login");
            return;
        }

        // 验证登录
        Optional<UserProfile> profile = userService.login(loginRequest);
        if (profile.isPresent()) {
            // 设置会话
            HttpSession session = request.getSession();
            session.setAttribute("user", profile.get());
            session.setAttribute("messageLevel", "success");
            session.setAttribute("messageContent", "登录成功");
            session.setAttribute("shouldRedirect", true);
            
            // 如果信任设备，设置会话过期时间为7天
            if (trustDevice) {
                session.setMaxInactiveInterval(WEEK_IN_SECONDS);
                request.setAttribute("persistSession", true);
            }
            
            response.sendRedirect("./login");
        } else {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "用户名或密码错误");
            response.sendRedirect("./login");
        }
    }

    /**
     * 处理检查用户名请求
     */
    private void handleCheckUsername(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 设置响应类型
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 获取并验证用户名
        String username = request.getParameter("username");
        if (!validateUsername(username)) {
            response.getWriter().write("{\"available\":false,\"message\":\"用户名格式错误\"}");
            return;
        }

        // 检查用户名是否可用
        boolean available = userService.checkUsernameAvailable(username);
        String message = available ? "用户名可用" : "用户名已被使用";
        response.getWriter().write(String.format(
            "{\"available\":%b,\"message\":\"%s\"}", 
            available, 
            message
        ));
    }

    /**
     * 处理登出请求
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/account/login");
    }

    /**
     * 验证用户名格式
     */
    private boolean validateUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码格式
     */
    private boolean validatePassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
