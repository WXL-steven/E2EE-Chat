<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.steven.e2eechat.model.UserProfile" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>会话 - E2EE Chat</title>
  <link href="${pageContext.request.contextPath}/assets/css/dist/sessions.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
  <%
  UserProfile user = (UserProfile) session.getAttribute("user");
  String messageContent = (String) session.getAttribute("messageContent");
  String messageLevel = (String) session.getAttribute("messageLevel");
  
  String displayName = user.getDisplayName().trim();
  String avatarText;
  if (!displayName.isEmpty()) {
    String firstChar = displayName.substring(0, 1);
    if (firstChar.matches("[a-z]")) {
      avatarText = firstChar.toUpperCase();
    } else {
      avatarText = firstChar;
    }
  } else {  
    avatarText = "X";
  }
  pageContext.setAttribute("avatarText", avatarText);
  
  // 清除消息，避免刷新时重复显示
  session.removeAttribute("messageContent");
  session.removeAttribute("messageLevel");
  %>
  <div class="card">
    <div class="card-title">
      <div class="user-info">
        <div class="user-avatar">
          <c:out value="${avatarText}" />
        </div>
        <div class="user-name">
          <c:out value="${user.displayName}" />
        </div>
      </div>
      <div class="logout">
        <button class="logout-button" onclick="handleLogout()">登出</button>
      </div>
    </div>
    <div class="card-body" id="sessions-container">
      <!-- 会话列表将通过AJAX动态加载 -->
    </div>
    <div class="card-bottom">
      <div class="input-error-snackbar hidden" id="error-snackbar"></div>
      <form action="${pageContext.request.contextPath}/sessions/new" method="POST" onsubmit="return validateForm()" class="input-form">
          <div class="input-username">
            <input type="text" name="username" class="input-username-input" id="username-input" placeholder="输入用户名"
              pattern="^[a-zA-Z0-9_-@]{1,16}$" required>
          </div>
          <div class="open-dialog">
            <button type="submit" class="open-dialog-button">打开会话</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <script>
    // 如果有错误消息，显示Snackbar
    <% if (messageContent != null && messageLevel != null && messageLevel.equals("error")) { %>
      showError('<%= messageContent %>');
    <% } %>

    function showError(message) {
      const errorSnackbar = document.getElementById('error-snackbar');
      errorSnackbar.textContent = message;
      errorSnackbar.classList.remove('hidden');
      setTimeout(() => {
        errorSnackbar.classList.add('hidden');
      }, 5000);
    }

    function handleLogout() {
      sessionStorage.clear();
      window.location.href = '${pageContext.request.contextPath}/account/logout';
    }

    function validateForm() {
      const username = document.getElementById('username-input').value.trim();
      if (!username) {
        showError('请输入用户名');
        return false;
      }
      return true;
    }

    // 定期更新会话列表
    function updateSessionsList() {
      fetch('${pageContext.request.contextPath}/sessions/list')
        .then(response => {
          if (response.redirected) {
            window.location.href = response.url;
            return;
          }
          return response.text();
        })
        .then(html => {
          if (html) {
            document.getElementById('sessions-container').innerHTML = html;
          }
        })
        .catch(error => console.error('更新会话列表失败:', error));
    }

    // 初始加载和定期更新
    updateSessionsList();
    setInterval(updateSessionsList, 3000);
  </script>
</body>
</html>