<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>保险库 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/styles.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
    <div class="content-panel-wrapper">
        <div class="content-panel">
            <div class="text-center">
                <h1 class="text-2xl font-bold mb-2">正在检查保险库</h1>
                <p class="text-gray-600 mb-4">将会在几秒内自动跳转</p>
            </div>
        </div>
    </div>

    <script>
        // 检查sessionStorage中是否有已解密的私钥
        window.addEventListener('DOMContentLoaded', function() {
            if (sessionStorage.getItem('privateKey')) {
                window.location.href = '${pageContext.request.contextPath}/sessions';
            } else {
                window.location.href = '${pageContext.request.contextPath}/vault/unlock';
            }
        });
    </script>
</body>
</html>
