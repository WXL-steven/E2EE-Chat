<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/forms.js"></script>
</head>
<body class="bg-gray-100">
    <div class="content-panel-wrapper">
        <div class="content-panel">
            <div class="flex justify-start">
                <h1 class="text-2xl font-bold mb-8">注册 - E2EE Chat</h1>
            </div>

            <!-- 消息提示框 -->
            <div id="message-container" class="w-full mb-8 max-w-md hidden">
                <div id="message-content" class="px-4 py-3 rounded relative" role="alert">
                    <span class="block sm:inline"></span>
                </div>
            </div>

            <!-- 注册表单 -->
            <form id="registerForm" class="w-full max-w-md" method="post">
                <div id="usernameGroup" class="mb-6">
                    <label for="username" class="block text-gray-700 text-sm font-bold mb-2">用户名</label>
                    <input type="text" id="username" name="username" 
                           class="input-text" 
                           placeholder="请输入用户名">
                    <div class="input-error-message"></div>
                </div>

                <div id="displayNameGroup" class="mb-6">
                    <label for="displayName" class="block text-gray-700 text-sm font-bold mb-2">昵称</label>
                    <input type="text" id="displayName" name="displayName" 
                           class="input-text" 
                           placeholder="请输入昵称">
                    <div class="input-error-message"></div>
                </div>

                <div id="passwordGroup" class="mb-6">
                    <label for="password" class="block text-gray-700 text-sm font-bold mb-2">密码</label>
                    <input type="password" id="password" name="password" 
                           class="input-password" 
                           placeholder="请输入密码">
                    <div class="input-error-message"></div>
                </div>

                <div id="confirmPasswordGroup" class="mb-6">
                    <label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" 
                           class="input-password" 
                           placeholder="请再次输入密码">
                    <div class="input-error-message"></div>
                </div>

                <label class="input-checkbox mb-6">
                    <input type="checkbox" name="trustDevice">
                    信任此设备7天
                </label>

                <div class="flex justify-end gap-4">
                    <button type="button" id="loginBtn" class="btn-secondary">
                        登录
                    </button>
                    <button type="reset" class="btn-secondary">
                        清空
                    </button>
                    <button type="submit" class="btn-primary">
                        注册
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
        // 验证工具函数
        function showError(input, message) {
            const group = document.getElementById(input.id + 'Group');
            const errorElement = group.querySelector('.input-error-message');
            console.log('显示错误:', {
                inputId: input.id,
                groupId: input.id + 'Group',
                message: message,
                errorElement: errorElement
            });
            input.classList.add('input-error');
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }

        function clearError(input) {
            const group = document.getElementById(input.id + 'Group');
            const errorElement = group.querySelector('.input-error-message');
            input.classList.remove('input-error');
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }

        // 验证规则
        async function validateUsername(username) {
            console.log('验证用户名:', username);
            // 长度检查
            if (username.length < 3 || username.length > 20) {
                return '用户名长度必须在3-20个字符之间';
            }
            
            // 格式检查
            if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
                return '用户名只能包含字母、数字、下划线和短横线';
            }

            // 服务器端验证
            try {
                const response = await fetch('${pageContext.request.contextPath}/account/check-username', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'username=' + encodeURIComponent(username)
                });
                
                const data = await response.json();
                console.log('服务器验证结果:', data);
                if (!data.available) {
                    return '该用户名已被使用';
                }
            } catch (error) {
                console.error('检查用户名可用性失败:', error);
                return '服务器验证失败，请稍后重试';
            }
        }

        function validateDisplayName(displayName) {
            if (displayName.length < 1 || displayName.length > 20) {
                return '昵称长度必须在1-20个字符之间';
            }
        }

        function validatePassword(password) {
            if (password.length < 6 || password.length > 20) {
                return '密码长度必须在6-20个字符之间';
            }
            
            if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/.test(password)) {
                return '密码必须包含大小写字母和数字';
            }
        }

        function validateConfirmPassword(password, confirmPassword) {
            if (password !== confirmPassword) {
                return '两次输入的密码不一致';
            }
        }

        // 初始化表单验证
        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('registerForm');
            const usernameInput = document.getElementById('username');
            const displayNameInput = document.getElementById('displayName');
            const passwordInput = document.getElementById('password');
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const loginBtn = document.getElementById('loginBtn');

            // 用户名失焦验证
            usernameInput.addEventListener('blur', async () => {
                clearError(usernameInput);
                const error = await validateUsername(usernameInput.value);
                if (error) {
                    showError(usernameInput, error);
                }
            });

            // 用户名输入变化时清除错误
            usernameInput.addEventListener('input', () => {
                if (usernameInput.classList.contains('input-error')) {
                    clearError(usernameInput);
                }
            });

            // 昵称失焦验证
            displayNameInput.addEventListener('blur', () => {
                clearError(displayNameInput);
                const error = validateDisplayName(displayNameInput.value);
                if (error) {
                    showError(displayNameInput, error);
                }
            });

            // 昵称输入变化时清除错误
            displayNameInput.addEventListener('input', () => {
                if (displayNameInput.classList.contains('input-error')) {
                    clearError(displayNameInput);
                }
            });

            // 密码失焦验证
            passwordInput.addEventListener('blur', () => {
                clearError(passwordInput);
                const error = validatePassword(passwordInput.value);
                if (error) {
                    showError(passwordInput, error);
                }
            });

            // 密码输入变化时清除错误
            passwordInput.addEventListener('input', () => {
                if (passwordInput.classList.contains('input-error')) {
                    clearError(passwordInput);
                }
                // 如果确认密码已填写，也要清除它的错误（因为密码变了，之前的匹配验证可能不再有效）
                if (confirmPasswordInput.value && confirmPasswordInput.classList.contains('input-error')) {
                    clearError(confirmPasswordInput);
                }
            });

            // 确认密码失焦验证
            confirmPasswordInput.addEventListener('blur', () => {
                clearError(confirmPasswordInput);
                const error = validateConfirmPassword(passwordInput.value, confirmPasswordInput.value);
                if (error) {
                    showError(confirmPasswordInput, error);
                }
            });

            // 确认密码输入变化时清除错误
            confirmPasswordInput.addEventListener('input', () => {
                if (confirmPasswordInput.classList.contains('input-error')) {
                    clearError(confirmPasswordInput);
                }
            });

            // 表单提交处理
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                // 清除所有错误
                [usernameInput, displayNameInput, passwordInput, confirmPasswordInput].forEach(clearError);

                // 禁用所有输入和按钮
                const formElements = form.querySelectorAll('input, button');
                formElements.forEach(el => el.disabled = true);

                try {
                    // 验证所有字段
                    const usernameError = await validateUsername(usernameInput.value);
                    if (usernameError) {
                        showError(usernameInput, usernameError);
                    }

                    const displayNameError = validateDisplayName(displayNameInput.value);
                    if (displayNameError) {
                        showError(displayNameInput, displayNameError);
                    }

                    const passwordError = validatePassword(passwordInput.value);
                    if (passwordError) {
                        showError(passwordInput, passwordError);
                    }

                    const confirmPasswordError = validateConfirmPassword(passwordInput.value, confirmPasswordInput.value);
                    if (confirmPasswordError) {
                        showError(confirmPasswordInput, confirmPasswordError);
                    }

                    // 如果有任何错误，阻止提交
                    if (usernameError || displayNameError || passwordError || confirmPasswordError) {
                        throw new Error('表单验证失败');
                    }

                    // 提交表单
                    form.submit();
                } catch (error) {
                    console.error('表单提交失败:', error);
                    // 重新启用所有输入和按钮
                    formElements.forEach(el => el.disabled = false);
                }
            });

            // 登录按钮点击事件
            loginBtn.addEventListener('click', () => {
                window.location.href = '${pageContext.request.contextPath}/account/login';
            });
        });
    </script>
</body>
</html>
