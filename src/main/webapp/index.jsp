<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>服务器时间</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f0f2f5;
        }
        .time-container {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        #serverTime {
            font-size: 24px;
            color: #1a73e8;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <div class="time-container">
        <h2>服务器时间</h2>
        <div id="serverTime">加载中...</div>
    </div>

    <script>
        function updateTime() {
            fetch('time.jsp')
                .then(response => response.text())
                .then(time => {
                    document.getElementById('serverTime').textContent = time;
                })
                .catch(error => console.error('Error:', error));
        }

        // 立即更新一次
        updateTime();
        // 每秒更新一次
        setInterval(updateTime, 1000);
    </script>
</body>
</html>
