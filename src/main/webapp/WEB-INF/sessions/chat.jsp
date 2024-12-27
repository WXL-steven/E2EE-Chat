<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.steven.e2eechat.model.UserProfile" %>
<%@ page import="com.steven.e2eechat.model.ChatSession" %>
<%@ page import="com.steven.e2eechat.service.UserService" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.UUID" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    ChatSession chatSession = (ChatSession) request.getAttribute("session");
    UserProfile currentUser = (UserProfile) request.getSession().getAttribute("user");
    UserService userService = new UserService();

    // 确定对方的userId
    UUID otherUserId = chatSession.getInitiatorId().equals(currentUser.getUserId())
            ? chatSession.getParticipantId()
            : chatSession.getInitiatorId();

    // 获取对方的用户资料
    UserProfile otherUser = userService.getUserById(otherUserId).orElse(null);

    if (otherUser == null || otherUser.getPublicKey() == null) {
        session.setAttribute("messageLevel", "error");
        session.setAttribute("messageContent", "对方的保险库尚未就绪，无法开始加密通信");
        response.sendRedirect(request.getContextPath() + "/sessions");
        return;
    }

    String otherUserPublicKey = Base64.getEncoder().encodeToString(otherUser.getPublicKey());
    
    // 设置request属性以供JSTL使用
    request.setAttribute("otherUser", otherUser);
%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>聊天 - E2EE Chat</title>
    <link href="${pageContext.request.contextPath}/assets/css/dist/chat.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
<div class="chat-dialog">
    <div class="chat-header">
        <div class="header-info">
            <a href="${pageContext.request.contextPath}/sessions" class="back-button">&lt; 返回</a>
            <div class="display-name"><c:out value="${otherUser.displayName}"/></div>
            <div class="username">@<c:out value="${otherUser.username}"/></div>
        </div>
    </div>
    <div class="chat-body">
        <section class="mb-8 w-full" id="message-container">
            <!-- <div class="divider-text" data-text="未读消息"></div>
            <div class="message-date">2024-12-25</div>
            <div class="mb-4 flex items-start gap-2">
                <div>
                    <div class="message-received">
                        这是一条<span class="message-monospace">接收</span>的消息， 包含<span
                            class="message-bold">粗体</span>、 <span class="message-italic">斜体</span>、 <span
                            class="message-underline">下划线</span>和 <span class="message-strikethrough">删除线</span>格式
                        <div class="message-time">12:34</div>
                    </div>
                </div>
            </div>
            <div class="mb-4 flex flex-row-reverse items-start gap-2">
                <div>
                    <div class="message-sent">
                        这是一条<span class="message-monospace">发送</span>的消息， 包含<span
                            class="message-bold">粗体</span>、 <span class="message-italic">斜体</span>、 <span
                            class="message-underline">下划线</span>和 <span class="message-strikethrough">删除线</span>格式
                        <div class="message-time">12:35</div>
                    </div>
                </div>
            </div> -->
        </section>
    </div>
    <div class="chat-footer">
        <div class="input-error-snackbar hidden" id="error-snackbar"></div>
        <div class="chat-input-container">
            <textarea class="chat-input" placeholder="输入消息内容"></textarea>
            <button class="send-button">发送</button>
        </div>
    </div>
</div>
<script>
    const PEER_PUBLIC_KEY = '<%= otherUserPublicKey %>';
    const CURRENT_USER_ID = '<%= currentUser.getUserId() %>';
    
    // 全局消息存储
    const messages = new Map();
    let lastCursor = -1;

    function showError(message) {
        const errorSnackbar = document.getElementById('error-snackbar');
        errorSnackbar.textContent = message;
        errorSnackbar.classList.remove('hidden');
        setTimeout(() => {
            errorSnackbar.classList.add('hidden');
        }, 5000);
    }

    document.addEventListener('DOMContentLoaded', function () {
        if (!sessionStorage.getItem('privateKey')) {
            window.location.href = '${pageContext.request.contextPath}/vault';
        }
        
        // 页面加载时初始化共享密钥
        initializeSharedKey();
    });

    // 初始化共享密钥
    let sharedKey = null;
    async function initializeSharedKey() {
        try {
            sharedKey = await deriveSharedKey();
        } catch (error) {
            showError('初始化共享密钥失败：' + error.message);
        }
    }

    // 生成安全随机数
    async function getRandomValues(length) {
        const array = new Uint8Array(length);
        crypto.getRandomValues(array);
        return array;
    }

    // 使用ECDH生成共享密钥并导入为AES密钥
    async function deriveSharedKey() {
        try {
            // 从Base64解码私钥
            const privateKeyBase64 = sessionStorage.getItem('privateKey');
            const privateKeyBytes = Uint8Array.from(atob(privateKeyBase64), c => c.charCodeAt(0));
            
            // 导入PKCS8格式的私钥
            const privateKey = await crypto.subtle.importKey(
                'pkcs8',
                privateKeyBytes,
                {
                    name: 'ECDH',
                    namedCurve: 'P-256'
                },
                false,
                ['deriveKey']
            );

            // 从Base64解码并导入对方的公钥
            const peerPublicKeyBytes = Uint8Array.from(atob(PEER_PUBLIC_KEY), c => c.charCodeAt(0));
            const peerPublicKey = await crypto.subtle.importKey(
                'raw',
                peerPublicKeyBytes,
                {
                    name: 'ECDH',
                    namedCurve: 'P-256'
                },
                false,
                []
            );

            // 使用ECDH生成共享密钥
            return await crypto.subtle.deriveKey(
                {
                    name: 'ECDH',
                    public: peerPublicKey
                },
                privateKey,
                {
                    name: 'AES-GCM',
                    length: 256
                },
                false,
                ['encrypt', 'decrypt']
            );
        } catch (error) {
            showError('生成共享密钥时发生错误：' + error.message);
            throw error;
        }
    }

    // 辅助函数：将字节数组转换为Base64
    function bytesToBase64(bytes) {
        const binString = Array.from(bytes, (x) => String.fromCharCode(x & 0xFF)).join('');
        return btoa(binString);
    }

    // 辅助函数：将Base64转换为字节数组
    function base64ToBytes(base64) {
        const binString = atob(base64);
        return new Uint8Array(binString.split('').map(c => c.charCodeAt(0)));
    }

    // 加密函数：接收明文，返回密文和IV的Base64编码
    async function encrypt(plaintext) {
        try {
            // 生成随机IV
            const iv = await getRandomValues(12); // AES-GCM 使用12字节IV
            
            // 将明文转换为 Uint8Array
            const encoder = new TextEncoder();
            const plaintextBytes = encoder.encode(plaintext);
            
            // 加密
            const ciphertext = await crypto.subtle.encrypt(
                {
                    name: 'AES-GCM',
                    iv: iv
                },
                sharedKey,
                plaintextBytes
            );
            
            // 将密文和IV转换为Base64
            const ciphertextBase64 = bytesToBase64(new Uint8Array(ciphertext));
            const ivBase64 = bytesToBase64(iv);
            
            return {
                ciphertext: ciphertextBase64,
                iv: ivBase64
            };
        } catch (error) {
            showError('加密失败：' + error.message);
            throw error;
        }
    }

    // 解密函数：接收密文和IV的Base64编码，返回明文
    async function decrypt(ciphertextBase64, ivBase64) {
        try {
            // 将Base64转换回 Uint8Array
            const ciphertext = base64ToBytes(ciphertextBase64);
            const iv = base64ToBytes(ivBase64);
            
            // 解密
            const plaintextBytes = await crypto.subtle.decrypt(
                {
                    name: 'AES-GCM',
                    iv: iv
                },
                sharedKey,
                ciphertext
            );
            
            // 将解密后的字节转换为字符串
            const decoder = new TextDecoder();
            return decoder.decode(plaintextBytes);
        } catch (error) {
            showError('解密失败：' + error.message);
            throw error;
        }
    }

    // 发送消息
    async function sendMessage(content, isSystem = false) {
        try {
            // 加密消息内容
            const encrypted = await encrypt(content);
            
            // 准备请求数据
            const data = {
                message_content: encrypted.ciphertext,
                message_iv: encrypted.iv,
                is_system: isSystem
            };
            
            // 发送POST请求
            const response = await fetch(window.location.pathname + '/messages', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                throw new Error('发送消息失败：' + response.statusText);
            }
            
            // 清空输入框并刷新消息
            document.querySelector('.chat-input').value = '';
            await loadNewMessages();
            
        } catch (error) {
            showError('发送消息失败：' + error.message);
        }
    }

    // 获取新消息
    async function loadNewMessages() {
        try {
            // 构建URL
            const url = new URL(window.location.pathname + '/messages', window.location.origin);
            if (lastCursor >= 0) {
                url.searchParams.append('cursor', lastCursor);
                url.searchParams.append('direction', 'after');
            }
            url.searchParams.append('limit', '50');
            
            console.log('正在请求新消息，URL:', url.toString());
            
            // 发送GET请求
            const response = await fetch(url);
            if (response.redirected) {
                console.log('请求被重定向，正在跳转到:', response.url);
                window.location.href = response.url;
                return;
            }
            if (!response.ok) {
                throw new Error('获取消息失败：' + response.statusText);
            }
            
            const messages = await response.json();
            console.log('成功获取 ' + messages.length + ' 条新消息');
            
            if (messages.length > 0) {
                // 如果是增量加载，跳过第一条消息（因为是上次的最后一条）
                const newMessages = lastCursor >= 0 ? messages.slice(1) : messages;
                
                if (newMessages.length > 0) {
                    // 更新最后一条消息的游标
                    lastCursor = messages[messages.length - 1].cursor;
                    console.log('更新lastCursor为:', lastCursor);
                    
                    // 解密并显示消息
                    console.log('开始解密并显示消息');
                    await displayMessages(newMessages);
                    console.log('消息显示完成');
                } else {
                    console.log('没有新消息（都是重复的）');
                }
            } else if (lastCursor >= 0) {
                console.log('游标非空但返回空列表，可能出错了');
                showError('获取消息失败，请刷新页面重试');
            } else {
                console.log('没有新消息');
            }
        } catch (error) {
            console.error('获取消息失败:', error);
            showError('获取消息失败：' + error.message);
        }
    }

    // 安全地格式化消息文本
    function formatMessageSecure(text) {
        const elements = [];
        let currentText = '';
        let currentIndex = 0;

        function pushText() {
            if (currentText) {
                elements.push({ type: 'text', content: currentText });
                currentText = '';
            }
        }

        while (currentIndex < text.length) {
            if (text.startsWith('**', currentIndex)) {
                pushText();
                const endIndex = text.indexOf('**', currentIndex + 2);
                if (endIndex !== -1) {
                    elements.push({ type: 'bold', content: text.substring(currentIndex + 2, endIndex) });
                    currentIndex = endIndex + 2;
                } else {
                    currentText += text[currentIndex];
                    currentIndex++;
                }
            } else if (text.startsWith('*', currentIndex)) {
                pushText();
                const endIndex = text.indexOf('*', currentIndex + 1);
                if (endIndex !== -1) {
                    elements.push({ type: 'italic', content: text.substring(currentIndex + 1, endIndex) });
                    currentIndex = endIndex + 1;
                } else {
                    currentText += text[currentIndex];
                    currentIndex++;
                }
            } else if (text.startsWith('__', currentIndex)) {
                pushText();
                const endIndex = text.indexOf('__', currentIndex + 2);
                if (endIndex !== -1) {
                    elements.push({ type: 'underline', content: text.substring(currentIndex + 2, endIndex) });
                    currentIndex = endIndex + 2;
                } else {
                    currentText += text[currentIndex];
                    currentIndex++;
                }
            } else if (text.startsWith('~~', currentIndex)) {
                pushText();
                const endIndex = text.indexOf('~~', currentIndex + 2);
                if (endIndex !== -1) {
                    elements.push({ type: 'strikethrough', content: text.substring(currentIndex + 2, endIndex) });
                    currentIndex = endIndex + 2;
                } else {
                    currentText += text[currentIndex];
                    currentIndex++;
                }
            } else if (text.startsWith('`', currentIndex)) {
                pushText();
                const endIndex = text.indexOf('`', currentIndex + 1);
                if (endIndex !== -1) {
                    elements.push({ type: 'monospace', content: text.substring(currentIndex + 1, endIndex) });
                    currentIndex = endIndex + 1;
                } else {
                    currentText += text[currentIndex];
                    currentIndex++;
                }
            } else if (text[currentIndex] === '\n') {
                pushText();
                elements.push({ type: 'newline' });
                currentIndex++;
            } else {
                currentText += text[currentIndex];
                currentIndex++;
            }
        }
        pushText();
        return elements;
    }

    // 显示消息
    async function displayMessages(newMessages) {
        const messageContainer = document.getElementById('message-container');
        let currentDate = null;

        // 按发送时间排序消息
        for (const message of newMessages) {
            try {
                // 检查消息是否已存在
                if (messages.has(message.messageId)) {
                    continue;
                }

                // 解密消息内容
                const content = await decrypt(
                    bytesToBase64(message.messageContent),
                    bytesToBase64(message.messageIv)
                );

                // 将消息添加到全局存储
                messages.set(message.messageId, {
                    ...message,
                    decryptedContent: content
                });

                const messageTime = new Date(message.sentAt).getTime();
                const messageDate = new Date(message.sentAt).toLocaleDateString();
                
                // 找到正确的插入位置
                let insertPosition = null;
                const messageElements = messageContainer.children;
                
                for (let i = messageElements.length - 1; i >= 0; i--) {
                    const element = messageElements[i];
                    if (element.classList.contains('message-date')) {
                        const dateAttr = element.getAttribute('data-date');
                        if (dateAttr === messageDate) {
                            insertPosition = element.nextSibling;
                            currentDate = messageDate;
                            break;
                        } else if (new Date(dateAttr).getTime() < new Date(messageDate).getTime()) {
                            // 需要插入新的日期分隔符
                            insertPosition = element.nextSibling;
                            break;
                        }
                    } else if (element.classList.contains('mb-4')) {
                        const msgId = element.getAttribute('data-message-id');
                        const existingMsg = messages.get(msgId);
                        if (existingMsg && new Date(existingMsg.sentAt).getTime() < messageTime) {
                            insertPosition = element.nextSibling;
                            break;
                        }
                    }
                }

                // 如果需要插入新的日期分隔符
                if (messageDate !== currentDate) {
                    currentDate = messageDate;
                    if (!messageContainer.querySelector('[data-date="' + messageDate + '"]')) {
                        const dateDiv = document.createElement('div');
                        dateDiv.className = 'message-date';
                        dateDiv.setAttribute('data-date', messageDate);
                        dateDiv.textContent = messageDate;
                        messageContainer.insertBefore(dateDiv, insertPosition);
                    }
                }

                // 创建消息元素
                const messageTimeStr = new Date(message.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                const isCurrentUser = message.senderId === CURRENT_USER_ID;

                const messageWrapper = document.createElement('div');
                messageWrapper.className = 'mb-4 flex ' + (isCurrentUser ? 'flex-row-reverse' : '') + ' items-start gap-2';
                messageWrapper.setAttribute('data-message-id', message.messageId);

                const timeDiv = document.createElement('div');
                timeDiv.className = 'message-time';
                timeDiv.textContent = messageTimeStr;

                const messageBubble = document.createElement('div');
                messageBubble.className = isCurrentUser ? 'message-sent' : 'message-received';

                // 使用安全的格式化函数
                const formattedElements = formatMessageSecure(content);
                formattedElements.forEach(element => {
                    let span;
                    switch (element.type) {
                        case 'bold':
                            span = document.createElement('span');
                            span.className = 'message-bold';
                            span.textContent = element.content;
                            messageBubble.appendChild(span);
                            break;
                        case 'italic':
                            span = document.createElement('span');
                            span.className = 'message-italic';
                            span.textContent = element.content;
                            messageBubble.appendChild(span);
                            break;
                        case 'underline':
                            span = document.createElement('span');
                            span.className = 'message-underline';
                            span.textContent = element.content;
                            messageBubble.appendChild(span);
                            break;
                        case 'strikethrough':
                            span = document.createElement('span');
                            span.className = 'message-strikethrough';
                            span.textContent = element.content;
                            messageBubble.appendChild(span);
                            break;
                        case 'monospace':
                            span = document.createElement('span');
                            span.className = 'message-monospace';
                            span.textContent = element.content;
                            messageBubble.appendChild(span);
                            break;
                        case 'newline':
                            messageBubble.appendChild(document.createElement('br'));
                            break;
                        case 'text':
                            messageBubble.appendChild(document.createTextNode(element.content));
                            break;
                    }
                });

                messageBubble.appendChild(timeDiv);
                messageWrapper.appendChild(messageBubble);

                // 在正确的位置插入消息
                messageContainer.insertBefore(messageWrapper, insertPosition);
            } catch (error) {
                console.error('解密消息失败：', error);
            }
        }

        // 滚动到底部
        messageContainer.scrollTop = messageContainer.scrollHeight;
        console.log('消息显示完成，已滚动到底部');
    }

    // 清理消息容器
    function clearMessages() {
        const messageContainer = document.getElementById('message-container');
        messageContainer.innerHTML = '';
        messages.clear();
        lastCursor = -1;
    }

    // 重新加载所有消息
    async function reloadAllMessages() {
        clearMessages();
        await loadNewMessages();
    }

    // 绑定发送按钮事件
    document.querySelector('.send-button').addEventListener('click', async () => {
        const input = document.querySelector('.chat-input');
        const content = input.value.trim();
        if (content) {
            await sendMessage(content);
        }
    });

    // 绑定输入框回车事件
    document.querySelector('.chat-input').addEventListener('keypress', async (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            const content = e.target.value.trim();
            if (content) {
                await sendMessage(content);
            }
        }
    });

    // 定期检查新消息
    setInterval(loadNewMessages, 3000);

    // 初始加载消息
    loadNewMessages();
</script>
</body>
</html>
