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

/**
 * {@code AccountController} 处理用户账户相关的请求，例如注册、登录、注销和用户名检查。
 * <p>
 * 此 Servlet 映射到 `/account` 及其子路径，并根据不同的请求路径调用相应的方法进行处理。
 * 它使用 {@link UserService} 来处理用户相关的业务逻辑。
 */
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

    /**
     * 处理 HTTP GET 请求。
     * <p>
     * 根据请求路径执行不同的操作，包括重定向到登录页面、显示注册或登录表单，以及处理用户注销。
     * 如果用户已登录，则会重定向到保险库页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果 Servlet 处理请求时遇到问题。
     * @throws IOException      如果在处理请求的 I/O 过程中发生错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // 已登录用户访问账户相关页面时重定向到保险库
            if ("/account".equals(path) || "/account/".equals(path) ||
                    "/account/login".equals(path) || "/account/register".equals(path)) {
                response.sendRedirect(request.getContextPath() + "/vault");
                return;
            }
        }

        // 根据请求路径处理不同的 GET 请求
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

    /**
     * 处理 HTTP POST 请求。
     * <p>
     * 根据请求路径执行不同的操作，包括处理用户注册、登录和检查用户名是否可用的请求。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws IOException 如果在处理请求的 I/O 过程中发生错误。
     */
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
     * 处理用户注册请求。
     * <p>
     * 从请求中获取注册信息，验证输入，并调用 {@link UserService#register(RegisterRequest)} 方法注册用户。
     * 注册成功后，设置会话属性并重定向到保险库页面；注册失败则设置错误消息并重定向回注册页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含注册信息。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象，用于重定向。
     * @throws IOException 如果在处理请求的 I/O 过程中发生错误。
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 从请求参数中解析注册信息
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(request.getParameter("username"));
        registerRequest.setDisplayName(request.getParameter("displayName"));
        registerRequest.setPassword(request.getParameter("password"));
        String trustDeviceParam = request.getParameter("trustDevice");
        boolean trustDevice = Boolean.parseBoolean(trustDeviceParam != null ? trustDeviceParam : "false");

        // 验证用户名格式
        if (!validateUsername(registerRequest.getUsername())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "用户名格式错误");
            response.sendRedirect("./register");
            return;
        }
        // 验证密码格式
        if (!validatePassword(registerRequest.getPassword())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "密码格式错误");
            response.sendRedirect("./register");
            return;
        }
        // 验证显示名称是否为空
        if (registerRequest.getDisplayName() == null || registerRequest.getDisplayName().trim().isEmpty()) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "显示名称不能为空");
            response.sendRedirect("./register");
            return;
        }

        // 调用 UserService 注册用户
        Optional<UserProfile> profile = userService.register(registerRequest);
        if (profile.isPresent()) {
            // 注册成功，设置会话属性
            HttpSession session = request.getSession();
            session.setAttribute("user", profile.get());
            session.setAttribute("messageLevel", "success");
            session.setAttribute("messageContent", "注册成功");
            session.setAttribute("shouldRedirect", true);

            // 如果用户选择信任设备，则设置会话过期时间
            if (trustDevice) {
                session.setMaxInactiveInterval(WEEK_IN_SECONDS);
                request.setAttribute("persistSession", true);
            }

            response.sendRedirect(request.getContextPath() + "/vault");
        } else {
            // 注册失败，设置错误消息
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "注册失败，请重试");
            response.sendRedirect("./register");
        }
    }

    /**
     * 处理用户登录请求。
     * <p>
     * 从请求中获取登录信息，验证输入，并调用 {@link UserService#login(LoginRequest)} 方法验证用户身份。
     * 登录成功后，设置会话属性并重定向到保险库页面；登录失败则设置错误消息并重定向回登录页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含登录信息。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象，用于重定向。
     * @throws IOException 如果在处理请求的 I/O 过程中发生错误。
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 从请求参数中解析登录信息
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getParameter("username"));
        loginRequest.setPassword(request.getParameter("password"));
        String trustDeviceParam = request.getParameter("trustDevice");
        boolean trustDevice = Boolean.parseBoolean(trustDeviceParam != null ? trustDeviceParam : "false");

        // 验证用户名和密码格式
        if (!validateUsername(loginRequest.getUsername()) ||
                !validatePassword(loginRequest.getPassword())) {
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "输入格式错误");
            response.sendRedirect("./login");
            return;
        }

        // 调用 UserService 进行登录验证
        Optional<UserProfile> profile = userService.login(loginRequest);
        if (profile.isPresent()) {
            // 登录成功，设置会话属性
            HttpSession session = request.getSession();
            session.setAttribute("user", profile.get());
            session.setAttribute("messageLevel", "success");
            session.setAttribute("messageContent", "登录成功");
            session.setAttribute("shouldRedirect", true);

            // 如果用户选择信任设备，则设置会话过期时间
            if (trustDevice) {
                session.setMaxInactiveInterval(WEEK_IN_SECONDS);
                request.setAttribute("persistSession", true);
            }

            response.sendRedirect(request.getContextPath() + "/vault");
        } else {
            // 登录失败，设置错误消息
            request.getSession().setAttribute("messageLevel", "error");
            request.getSession().setAttribute("messageContent", "用户名或密码错误");
            response.sendRedirect("./login");
        }
    }

    /**
     * 处理检查用户名是否可用的请求。
     * <p>
     * 从请求中获取用户名，验证格式，并调用 {@link UserService#checkUsernameAvailable(String)} 方法检查用户名是否已被使用。
     * 返回 JSON 格式的响应，指示用户名是否可用以及相应的消息。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象，包含要检查的用户名。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象，用于发送 JSON 响应。
     * @throws IOException 如果在处理请求的 I/O 过程中发生错误。
     */
    private void handleCheckUsername(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 设置响应内容类型为 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 获取并验证用户名
        String username = request.getParameter("username");
        if (!validateUsername(username)) {
            response.getWriter().write("{\"available\":false,\"message\":\"用户名格式错误\"}");
            return;
        }

        // 检查用户名是否可用并返回结果
        boolean available = userService.checkUsernameAvailable(username);
        String message = available ? "用户名可用" : "用户名已被使用";
        response.getWriter().write(String.format(
                "{\"available\":%b,\"message\":\"%s\"}",
                available,
                message
        ));
    }

    /**
     * 处理用户注销请求。
     * <p>
     * 使当前会话失效，并重定向到登录页面。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象，用于重定向。
     * @throws IOException 如果在处理请求的 I/O 过程中发生错误。
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
     * 验证用户名格式是否符合要求。
     *
     * @param username 要验证的用户名。
     * @return {@code true} 如果用户名格式正确，否则返回 {@code false}。
     */
    private boolean validateUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码格式是否符合要求。
     *
     * @param password 要验证的密码。
     * @return {@code true} 如果密码格式正确，否则返回 {@code false}。
     */
    private boolean validatePassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
