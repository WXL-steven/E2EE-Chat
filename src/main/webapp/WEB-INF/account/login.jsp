<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/forms.js"></script>
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
            document.cookie = 'JSESSIONID=' + '<%= session.getId() %>' + 
                            '; max-age=' + <%= weekInSeconds %> + 
                            '; path=/; SameSite=Strict; Secure';
            <% } %>
        });
    </script>
    <% } %>

    <script>
        // 初始化表单验证
        document.addEventListener('DOMContentLoaded', () => {
            const formValidation = new FormValidation();
            const form = document.getElementById('loginForm');
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            const registerBtn = document.getElementById('registerBtn');
            
            // 设置验证器
            formValidation.setupUsernameValidation(usernameInput);
            formValidation.setupPasswordValidation(passwordInput);
            
            // 表单提交事件
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                // 验证所有字段
                const usernameValid = await formValidation.validate(usernameInput);
                const passwordValid = await formValidation.validate(passwordInput);
                
                if (!usernameValid.isValid || !passwordValid.isValid) {
                    return;
                }
                
                // 提交表单
                form.submit();
            });
            
            // 注册按钮点击事件
            registerBtn.addEventListener('click', () => {
                window.location.href = '${pageContext.request.contextPath}/account/register';
            });
        });
    </script>
</body>
</html>
