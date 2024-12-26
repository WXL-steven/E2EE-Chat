package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * 聊天消息模型
 * 对应数据库中的chat_messages表
 * 
 * @property messageId 消息唯一标识
 * @property sessionId 所属会话ID
 * @property cursor 消息在会话中的序号，用于消息排序
 * @property senderId 发送者ID
 * @property receiverId 接收者ID
 * @property isSystem 是否为系统消息
 * @property isRead 消息是否已读
 * @property messageIv 消息加密IV向量
 * @property messageContent 加密后的消息内容
 */
public class ChatMessage {
    private UUID messageId;
    private UUID sessionId;
    private long cursor;
    private UUID senderId;
    private UUID receiverId;
    private boolean isSystem;
    private boolean isRead;
    private byte[] messageIv;
    private byte[] messageContent;

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public long getCursor() {
        return cursor;
    }

    public void setCursor(long cursor) {
        this.cursor = cursor;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
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
}
