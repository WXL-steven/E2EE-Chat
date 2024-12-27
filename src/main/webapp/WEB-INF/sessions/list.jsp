<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.steven.e2eechat.model.*" %>
<%@ page import="java.time.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:choose>
    <c:when test="${empty sessions}">
        <div class="no-sessions">
            没有会话
        </div>
    </c:when>
    <c:otherwise>
        <c:forEach var="chatSession" items="${sessions}">
            <c:set var="otherUserId" value="${chatSession.initiatorId eq user.userId ? chatSession.participantId : chatSession.initiatorId}" />
            <c:set var="otherUser" value="${profiles[otherUserId]}" />
            
            <%-- 计算显示时间 --%>
            <%
            UserProfile otherUser = (UserProfile)pageContext.getAttribute("otherUser");
            UserProfile currentUser = (UserProfile)session.getAttribute("user");
            
            OffsetDateTime lastOnline = otherUser.getLastOnline();
            // 转换到东八区
            ZoneOffset chinaOffset = ZoneOffset.ofHours(8);
            lastOnline = lastOnline.withOffsetSameInstant(chinaOffset);
            
            Duration duration = Duration.between(lastOnline, OffsetDateTime.now(chinaOffset));
            String lastOnlineText;

            if (duration.toMinutes() < 1) {
                lastOnlineText = "当前在线";
            } else if (lastOnline.toLocalDate().equals(LocalDate.now(chinaOffset))) {
                lastOnlineText = String.format("最后上线于 %02d:%02d",
                    lastOnline.getHour(),
                    lastOnline.getMinute());
            } else {
                lastOnlineText = String.format("最后上线于 %02d-%02d %02d:%02d",
                    lastOnline.getMonthValue(),
                    lastOnline.getDayOfMonth(),
                    lastOnline.getHour(),
                    lastOnline.getMinute());
            }
            pageContext.setAttribute("lastOnlineText", lastOnlineText);
            
            // 处理最后消息时间
            ChatSession chatSession = (ChatSession)pageContext.getAttribute("chatSession");
            String lastMessageTime = "";
            if (chatSession.getLastMessageAt().isPresent()) {
                OffsetDateTime lastMessageAt = chatSession.getLastMessageAt().get();
                // 转换到东八区
                lastMessageAt = lastMessageAt.withOffsetSameInstant(chinaOffset);
                if (lastMessageAt.toLocalDate().equals(LocalDate.now(chinaOffset))) {
                    lastMessageTime = String.format("%02d:%02d", 
                        lastMessageAt.getHour(), 
                        lastMessageAt.getMinute());
                } else {
                    lastMessageTime = String.format("%02d-%02d %02d:%02d",
                        lastMessageAt.getMonthValue(),
                        lastMessageAt.getDayOfMonth(),
                        lastMessageAt.getHour(),
                        lastMessageAt.getMinute());
                }
            }
            pageContext.setAttribute("lastMessageTime", lastMessageTime);
            %>
            
            <div class="session-item" onclick="window.location.href='${pageContext.request.contextPath}/sessions/<c:out value="${chatSession.sessionId}"/>'">
                <div class="avatar">
                    <%
                    String displayName = otherUser.getDisplayName().trim();
                    String avatar = "?";
                    if (!displayName.isEmpty()) {
                        String firstChar = displayName.substring(0, 1);
                        avatar = firstChar.matches("[a-zA-Z]") ? firstChar.toUpperCase() : firstChar;
                    }
                    pageContext.setAttribute("avatar", avatar);
                    %>
                    <c:out value="${avatar}" />
                </div>
                <div class="info">
                    <div class="username">
                        <c:out value="${otherUser.displayName}" />
                    </div>
                    <div class="lastinfo">
                        <c:out value="${lastOnlineText}" />
                    </div>
                </div>
                <c:if test="${not empty lastMessageTime}">
                    <div class="lastsent">
                        <c:out value="${lastMessageTime}" />
                    </div>
                </c:if>
            </div>
        </c:forEach>
    </c:otherwise>
</c:choose>
