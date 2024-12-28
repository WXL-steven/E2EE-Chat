package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code ChatSession} 模型类，映射数据库中的 {@code chat_sessions} 表。
 * <p>
 * 该类封装了聊天会话的各项属性，例如会话 ID、参与者、创建时间、消息计数以及最后一条消息的信息。
 */
public class ChatSession {
    /**
     * 会话的唯一标识符。
     */
    private UUID sessionId;
    /**
     * 发起会话的用户的 ID。
     */
    private UUID initiatorId;
    /**
     * 参与会话的另一个用户的 ID。
     */
    private UUID participantId;
    /**
     * 会话创建的时间戳。
     */
    private OffsetDateTime createdAt;
    /**
     * 会话中的消息计数器，用于消息排序。
     */
    private long messageCounter;
    /**
     * 最后一条消息的 ID，可能为空。
     */
    private Optional<UUID> lastMessageId = Optional.empty();
    /**
     * 最后一条消息的发送时间，可能为空。
     */
    private Optional<OffsetDateTime> lastMessageAt = Optional.empty();

    /**
     * 获取会话的唯一标识符。
     *
     * @return 会话 ID。
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * 设置会话的唯一标识符。
     *
     * @param sessionId 会话 ID。
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取发起会话的用户的 ID。
     *
     * @return 发起者 ID。
     */
    public UUID getInitiatorId() {
        return initiatorId;
    }

    /**
     * 设置发起会话的用户的 ID。
     *
     * @param initiatorId 发起者 ID。
     */
    public void setInitiatorId(UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    /**
     * 获取参与会话的另一个用户的 ID。
     *
     * @return 参与者 ID。
     */
    public UUID getParticipantId() {
        return participantId;
    }

    /**
     * 设置参与会话的另一个用户的 ID。
     *
     * @param participantId 参与者 ID。
     */
    public void setParticipantId(UUID participantId) {
        this.participantId = participantId;
    }

    /**
     * 获取会话创建的时间戳。
     *
     * @return 创建时间。
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置会话创建的时间戳。
     *
     * @param createdAt 创建时间。
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取会话中的消息计数器。
     *
     * @return 消息计数器。
     */
    public long getMessageCounter() {
        return messageCounter;
    }

    /**
     * 设置会话中的消息计数器。
     *
     * @param messageCounter 消息计数器。
     */
    public void setMessageCounter(long messageCounter) {
        this.messageCounter = messageCounter;
    }

    /**
     * 获取最后一条消息的 ID。
     *
     * @return 包含最后一条消息 ID 的 {@link Optional}，如果不存在则为空。
     */
    public Optional<UUID> getLastMessageId() {
        return lastMessageId;
    }

    /**
     * 设置最后一条消息的 ID。
     *
     * @param lastMessageId 最后一条消息的 ID。
     */
    public void setLastMessageId(UUID lastMessageId) {
        this.lastMessageId = Optional.ofNullable(lastMessageId);
    }

    /**
     * 获取最后一条消息的发送时间。
     *
     * @return 包含最后一条消息发送时间的 {@link Optional}，如果不存在则为空。
     */
    public Optional<OffsetDateTime> getLastMessageAt() {
        return lastMessageAt;
    }

    /**
     * 设置最后一条消息的发送时间。
     *
     * @param lastMessageAt 最后一条消息的发送时间。
     */
    public void setLastMessageAt(OffsetDateTime lastMessageAt) {
        this.lastMessageAt = Optional.ofNullable(lastMessageAt);
    }
}
