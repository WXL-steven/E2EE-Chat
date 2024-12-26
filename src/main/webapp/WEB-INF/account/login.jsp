<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
    <div class="content-panel-wrapper">
        <div class="content-panel">
            <div class="flex justify-start">
                <h1 class="text-2xl font-bold mb-8">登录 - E2EE Chat</h1>
            </div>

            <!-- 消息提示框 -->
            <div id="message-container" class="w-full mb-8 max-w-md hidden">
                <div id="message-content" class="px-4 py-3 rounded relative" role="alert">
                    <span class="block sm:inline"></span>
                </div>
            </div>

            <!-- 登录表单 -->
            <form id="loginForm" class="w-full max-w-md" method="post">
                <div class="mb-6">
                    <label for="username" class="block text-gray-700 text-sm font-bold mb-2">用户名</label>
                    <input type="text" id="username" name="username" 
                           class="input-text" 
                           placeholder="请输入用户名">
                    <div class="input-error-message"></div>
                </div>

                <div class="mb-6">
                    <label for="password" class="block text-gray-700 text-sm font-bold mb-2">密码</label>
                    <input type="password" id="password" name="password" 
                           class="input-password" 
                           placeholder="请输入密码">
                    <div class="input-error-message"></div>
                </div>

                <label class="input-checkbox mb-6">
                    <input type="checkbox" name="trustDevice">
                    信任此设备7天
                </label>

                <div class="flex justify-end gap-4">
                    <button type="button" id="registerBtn" class="btn-secondary">
                        注册
                    </button>
                    <button type="reset" class="btn-secondary">
                        清空
                    </button>
                    <button type="submit" class="btn-primary">
                        登录
                    </button>
                </div>
            </form>
        </div>
    </div>

    <%-- 处理消息提示和跳转 --%>
    <%
        String messageLevel = (String) session.getAttribute("messageLevel");
        String messageContent = (String) session.getAttribute("messageContent");
        Boolean shouldRedirect = (Boolean) session.getAttribute("shouldRedirect");
        Boolean persistSession = (Boolean) request.getAttribute("persistSession");
        int weekInSeconds = 7 * 24 * 60 * 60;
        String sessionId = session.getId();
        
        if (messageLevel != null && messageContent != null) {
            session.removeAttribute("messageLevel");
            session.removeAttribute("messageContent");
            session.removeAttribute("shouldRedirect");
    %>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const container = document.getElementById('message-container');
            const content = container.querySelector('#message-content');
            
            container.classList.remove('hidden');
            content.classList.add('alert-<%= messageLevel %>');
            content.querySelector('span').textContent = '<%= messageContent %>';
            
            // 5秒后隐藏消息
            setTimeout(() => {
                container.classList.add('hidden');
            }, 5000);
            
            <% if (Boolean.TRUE.equals(shouldRedirect)) { %>
            // 禁用所有输入和按钮
            document.querySelectorAll('input, button').forEach(el => el.disabled = true);
            
            // 5秒后跳转
            setTimeout(() => {
                window.location.href = '${pageContext.request.contextPath}/vault';
            }, 5000);
            <% } %>
            
            <% if (Boolean.TRUE.equals(persistSession)) { %>
            // 持久化会话ID到Cookie
            document.cookie = `JSESSIONID=${sessionId}; max-age=${weekInSeconds}; path=/; SameSite=Strict`;
            <% } %>
        });
    </script>
    <% } %>

    <script>
        // 验证工具函数
        function showError(input, message) {
            const group = input.closest('div');
            const errorElement = group.querySelector('.input-error-message');
            errorElement.textContent = message;
            errorElement.style.display = 'block';
            input.classList.add('input-error');
        }

        function clearError(input) {
            const group = input.closest('div');
            const errorElement = group.querySelector('.input-error-message');
            errorElement.textContent = '';
            errorElement.style.display = 'none';
            input.classList.remove('input-error');
        }

        // 表单验证
        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('loginForm');
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');

            // 注册按钮点击事件
            document.getElementById('registerBtn').addEventListener('click', () => {
                window.location.href = '${pageContext.request.contextPath}/account/register';
            });

            // 表单提交验证
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                // 清除所有错误
                [usernameInput, passwordInput].forEach(clearError);
                
                let isValid = true;

                // 验证用户名
                if (!usernameInput.value.trim()) {
                    showError(usernameInput, '请输入用户名');
                    isValid = false;
                } else if (usernameInput.value.length < 3 || usernameInput.value.length > 20) {
                    showError(usernameInput, '用户名长度必须在3-20个字符之间');
                    isValid = false;
                }

                // 验证密码
                if (!passwordInput.value) {
                    showError(passwordInput, '请输入密码');
                    isValid = false;
                } else if (passwordInput.value.length < 6 || passwordInput.value.length > 20) {
                    showError(passwordInput, '密码长度必须在6-20个字符之间');
                    isValid = false;
                }

                if (isValid) {
                    form.submit();
                }
            });
        });
    </script>
</body>
</html>
