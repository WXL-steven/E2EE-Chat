package com.steven.e2eechat.dto.db;

import java.util.UUID;

/**
 * 新建消息数据传输对象
 * 用于发送新消息时传递必要的参数
 * 
 * @property sessionId 目标会话ID
 * @property messageIv 消息加密IV向量
 * @property messageContent 加密后的消息内容
 * @property isSystem 是否为系统消息
 */
public class NewMessageDTO {
    private UUID sessionId;
    private byte[] messageIv;
    private byte[] messageContent;
    private boolean isSystem;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public byte[] getMessageIv() {
        return messageIv;
    }

    public void setMessageIv(byte[] messageIv) {
        this.messageIv = messageIv;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }
}
