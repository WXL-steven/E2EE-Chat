package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 聊天会话模型
 * 对应数据库中的chat_sessions表
 * 
 * @property sessionId 会话唯一标识
 * @property initiatorId 会话发起者ID
 * @property participantId 会话参与者ID
 * @property createdAt 会话创建时间
 * @property messageCounter 会话中的消息计数器，用于消息排序
 * @property lastMessageId 最后一条消息的ID
 * @property lastMessageAt 最后一条消息的发送时间
 */
public class ChatSession {
    private UUID sessionId;
    private UUID initiatorId;
    private UUID participantId;
    private OffsetDateTime createdAt;
    private long messageCounter;
    private UUID lastMessageId;
    private OffsetDateTime lastMessageAt;

    // Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public void setParticipantId(UUID participantId) {
        this.participantId = participantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getMessageCounter() {
        return messageCounter;
    }

    public void setMessageCounter(long messageCounter) {
        this.messageCounter = messageCounter;
    }

    public UUID getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(UUID lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(OffsetDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
