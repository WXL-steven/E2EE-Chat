package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * {@code ChatMessage} 模型类，映射数据库中的 {@code chat_messages} 表。
 * <p>
 * 该类封装了聊天消息的各项属性，包括消息 ID、所属会话、发送者、接收者、消息内容等。
 */
public class ChatMessage {
    /**
     * 消息的唯一标识符。
     */
    private UUID messageId;
    /**
     * 消息所属会话的 ID。
     */
    private UUID sessionId;
    /**
     * 消息在会话中的序号，用于消息排序。
     */
    private long cursor;
    /**
     * 发送消息的用户的 ID。
     */
    private UUID senderId;
    /**
     * 接收消息的用户的 ID。
     */
    private UUID receiverId;
    /**
     * 标识消息是否为系统消息。
     */
    private boolean isSystem;
    /**
     * 标识消息是否已被接收者读取。
     */
    private boolean isRead;
    /**
     * 用于加密消息内容的初始化向量 (IV)。
     */
    private byte[] messageIv;
    /**
     * 加密后的消息内容。
     */
    private byte[] messageContent;
    /**
     * 消息发送的时间戳。
     */
    private OffsetDateTime sentAt;

    /**
     * 获取消息的唯一标识符。
     *
     * @return 消息 ID。
     */
    public UUID getMessageId() {
        return messageId;
    }

    /**
     * 设置消息的唯一标识符。
     *
     * @param messageId 消息 ID。
     */
    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    /**
     * 获取消息所属会话的 ID。
     *
     * @return 会话 ID。
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * 设置消息所属会话的 ID。
     *
     * @param sessionId 会话 ID。
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取消息在会话中的序号。
     *
     * @return 消息游标。
     */
    public long getCursor() {
        return cursor;
    }

    /**
     * 设置消息在会话中的序号。
     *
     * @param cursor 消息游标。
     */
    public void setCursor(long cursor) {
        this.cursor = cursor;
    }

    /**
     * 获取发送消息的用户的 ID。
     *
     * @return 发送者 ID。
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * 设置发送消息的用户的 ID。
     *
     * @param senderId 发送者 ID。
     */
    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    /**
     * 获取接收消息的用户的 ID。
     *
     * @return 接收者 ID。
     */
    public UUID getReceiverId() {
        return receiverId;
    }

    /**
     * 设置接收消息的用户的 ID。
     *
     * @param receiverId 接收者 ID。
     */
    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * 检查消息是否为系统消息。
     *
     * @return 如果是系统消息，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isSystem() {
        return isSystem;
    }

    /**
     * 设置消息是否为系统消息。
     *
     * @param system {@code true} 表示是系统消息，{@code false} 表示不是。
     */
    public void setSystem(boolean system) {
        isSystem = system;
    }

    /**
     * 检查消息是否已被读取。
     *
     * @return 如果已读，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * 设置消息的已读状态。
     *
     * @param read {@code true} 表示已读，{@code false} 表示未读。
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * 获取用于加密消息内容的初始化向量 (IV)。
     *
     * @return 消息 IV。
     */
    public byte[] getMessageIv() {
        return messageIv;
    }

    /**
     * 设置用于加密消息内容的初始化向量 (IV)。
     *
     * @param messageIv 消息 IV。
     */
    public void setMessageIv(byte[] messageIv) {
        this.messageIv = messageIv;
    }

    /**
     * 获取加密后的消息内容。
     *
     * @return 加密后的消息内容。
     */
    public byte[] getMessageContent() {
        return messageContent;
    }

    /**
     * 设置加密后的消息内容。
     *
     * @param messageContent 加密后的消息内容。
     */
    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    /**
     * 获取消息发送的时间戳。
     *
     * @return 消息发送时间。
     */
    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    /**
     * 设置消息发送的时间戳。
     *
     * @param sentAt 消息发送时间。
     */
    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
