<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>E2EE Chat - 组件展示</title>
    <link href="./assets/css/dist/styles.css" rel="stylesheet">
    <script src="./assets/js/forms.js"></script>
    <script>
        // 更新服务器时间
        function updateServerTime() {
            fetch(window.location.href)
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const time = doc.querySelector('#server-time');
                    if (time) {
                        document.querySelector('#server-time').textContent = time.textContent;
                    }
                });
        }

        // 每500ms更新一次服务器时间
        setInterval(updateServerTime, 500);

        // 初始化表单验证
        document.addEventListener('DOMContentLoaded', () => {
            // 设置用户名和密码验证
            document.querySelectorAll('.input-text').forEach(input => {
                formValidation.setupUsernameValidation(input);
            });
            document.querySelectorAll('.input-password').forEach(input => {
                formValidation.setupPasswordValidation(input);
            });

            // 设置PIN输入框
            document.querySelectorAll('.input-pin').forEach(input => {
                PinInput.setup(input);
            });

            // 设置下拉菜单
            Dropdown.setup();
        });
    </script>
</head>
<body class="bg-gray-100">
    <div class="container-main">
        <h1 class="text-2xl font-bold mb-8">组件展示</h1>

        <!-- 服务器时间 -->
        <div class="w-full mb-8 text-center">
            <p class="text-gray-600">
                服务器时间：
                <span id="server-time">
                    <%= LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    ) %>
                </span>
            </p>
        </div>

        <!-- 头像展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">头像</h2>
            <div class="flex gap-4">
                <div class="avatar bg-blue-500">A</div>
                <div class="avatar bg-green-500">B</div>
                <div class="avatar bg-purple-500">C</div>
            </div>
        </section>

        <!-- 按钮展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">按钮</h2>
            <div class="flex gap-4 items-center">
                <button class="btn-primary">主要按钮</button>
                <button class="btn-secondary">次要按钮</button>
                <button class="btn-warning">警告按钮</button>
                <button class="btn-icon-primary">+</button>
                <button class="btn-icon-secondary">×</button>
            </div>
        </section>

        <!-- 输入框展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">输入框</h2>
            <div class="flex flex-col gap-4">
                <div class="relative">
                    <input type="text" class="input-text" placeholder="单行文本框">
                    <div class="input-error-message"></div>
                </div>
                <div class="relative">
                    <input type="password" class="input-password" placeholder="密码框">
                    <div class="input-error-message"></div>
                </div>
                <div class="flex justify-center gap-2">
                    <input type="text" class="input-pin" maxlength="1" placeholder="#">
                    <input type="text" class="input-pin" maxlength="1" placeholder="#">
                    <input type="text" class="input-pin" maxlength="1" placeholder="#">
                    <input type="text" class="input-pin" maxlength="1" placeholder="#">
                </div>
                <textarea class="input-textarea" placeholder="多行文本框 (超过5行显示滚动条)"></textarea>
            </div>
        </section>

        <!-- 弹出菜单展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">弹出菜单</h2>
            <div class="dropdown">
                <button class="btn-secondary" onclick="Dropdown.toggle(this)">
                    点击显示菜单 ▼
                </button>
                <div class="dropdown-menu">
                    <a href="#" class="dropdown-item">菜单项 1</a>
                    <a href="#" class="dropdown-item">菜单项 2</a>
                    <a href="#" class="dropdown-item-warning">退出登录</a>
                </div>
            </div>
        </section>

        <!-- 提示条展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">提示条</h2>
            <div class="alert-success">这是一条成功提示消息</div>
            <div class="alert-warning">这是一条警告提示消息</div>
            <div class="alert-error">这是一条错误提示消息</div>
        </section>

        <!-- 分割线展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">分割线</h2>
            <div class="divider"></div>
            <div class="divider-text" data-text="或者"></div>
            <div class="divider-text" data-text="2024-12-25"></div>
        </section>

        <!-- 消息泡展示 -->
        <section class="w-full mb-8">
            <h2 class="text-xl font-semibold mb-4">消息泡</h2>
            <div class="message-date">2024-12-25</div>
            <div class="flex items-start gap-2 mb-4">
                <div class="avatar bg-green-500">B</div>
                <div>
                    <div class="message-received">
                        这是一条<span class="message-monospace">接收</span>的消息，
                        包含<span class="message-bold">粗体</span>、
                        <span class="message-italic">斜体</span>、
                        <span class="message-underline">下划线</span>和
                        <span class="message-strikethrough">删除线</span>格式
                        <div class="message-time">12:34</div>
                    </div>
                </div>
            </div>
            <div class="flex items-start gap-2 mb-4 flex-row-reverse">
                <div class="avatar bg-blue-500">A</div>
                <div>
                    <div class="message-sent">
                        这是一条<span class="message-monospace">发送</span>的消息，
                        包含<span class="message-bold">粗体</span>、
                        <span class="message-italic">斜体</span>、
                        <span class="message-underline">下划线</span>和
                        <span class="message-strikethrough">删除线</span>格式
                        <div class="message-time">12:35</div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</body>
</html>
