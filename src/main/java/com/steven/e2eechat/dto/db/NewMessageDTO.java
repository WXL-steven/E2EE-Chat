package com.steven.e2eechat.dto.db;

import java.util.UUID;

/**
 * {@code NewMessageDTO} 数据传输对象，用于在发送新消息时传递必要的参数。
 * <p>
 * 该对象封装了发送消息所需的会话 ID、消息加密的初始化向量 (IV)、加密后的消息内容以及消息是否为系统消息的标志。
 */
public class NewMessageDTO {
    private UUID sessionId;
    private byte[] messageIv;
    private byte[] messageContent;
    private boolean isSystem;

    /**
     * 获取目标会话的 ID。
     *
     * @return 目标会话的 UUID。
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * 设置目标会话的 ID。
     *
     * @param sessionId 目标会话的 UUID。
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取消息加密的初始化向量 (IV)。
     *
     * @return 消息加密的 IV 字节数组。
     */
    public byte[] getMessageIv() {
        return messageIv;
    }

    /**
     * 设置消息加密的初始化向量 (IV)。
     *
     * @param messageIv 消息加密的 IV 字节数组。
     */
    public void setMessageIv(byte[] messageIv) {
        this.messageIv = messageIv;
    }

    /**
     * 获取加密后的消息内容。
     *
     * @return 加密后的消息内容字节数组。
     */
    public byte[] getMessageContent() {
        return messageContent;
    }

    /**
     * 设置加密后的消息内容。
     *
     * @param messageContent 加密后的消息内容字节数组。
     */
    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    /**
     * 检查消息是否为系统消息。
     *
     * @return 如果消息为系统消息，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isSystem() {
        return isSystem;
    }

    /**
     * 设置消息是否为系统消息。
     *
     * @param system {@code true} 如果消息为系统消息，否则为 {@code false}。
     */
    public void setSystem(boolean system) {
        isSystem = system;
    }
}