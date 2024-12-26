package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.SessionDAO;
import com.steven.e2eechat.dto.db.NewMessageDTO;
import com.steven.e2eechat.model.ChatMessage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 消息服务
 * 处理消息相关的业务逻辑
 */
public class MessageService {
    private final SessionDAO sessionDAO;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final long DEFAULT_CURSOR = -1L;

    public MessageService() {
        this.sessionDAO = new SessionDAO();
    }

    /**
     * 获取指定游标之前的消息
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param cursor 消息游标，null或-1表示从最新消息开始
     * @param limit 返回的消息数量限制，null或小于1则使用默认值
     * @return 消息列表，如果发生错误返回空列表
     */
    public List<ChatMessage> getMessagesBefore(UUID userId, UUID sessionId, Long cursor, Integer limit) {
        if (userId == null || sessionId == null) {
            return Collections.emptyList();
        }

        try {
            long actualCursor = cursor != null ? cursor : DEFAULT_CURSOR;
            int actualLimit = limit != null && limit > 0 ? limit : DEFAULT_PAGE_SIZE;
            return sessionDAO.getMessagesBefore(userId, sessionId, actualCursor, actualLimit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定游标之后的消息
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param cursor 消息游标，null或-1表示从最早消息开始
     * @param limit 返回的消息数量限制，null或小于1则使用默认值
     * @return 消息列表，如果发生错误返回空列表
     */
    public List<ChatMessage> getMessagesAfter(UUID userId, UUID sessionId, Long cursor, Integer limit) {
        if (userId == null || sessionId == null) {
            return Collections.emptyList();
        }

        try {
            long actualCursor = cursor != null ? cursor : DEFAULT_CURSOR;
            int actualLimit = limit != null && limit > 0 ? limit : DEFAULT_PAGE_SIZE;
            return sessionDAO.getMessagesAfter(userId, sessionId, actualCursor, actualLimit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定消息
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 如果消息存在且用户有权限访问则返回消息对象，否则返回空
     */
    public Optional<ChatMessage> getMessage(UUID userId, UUID messageId) {
        if (userId == null || messageId == null) {
            return Optional.empty();
        }
        return sessionDAO.getMessage(userId, messageId);
    }

    /**
     * 发送新消息
     *
     * @param userId 发送者ID
     * @param newMessage 新消息DTO
     * @return 是否发送成功
     */
    public boolean sendMessage(UUID userId, NewMessageDTO newMessage) {
        if (userId == null || newMessage == null) {
            return false;
        }
        try {
            return sessionDAO.sendMessage(userId, newMessage);
        } catch (Exception e) {
            return false;
        }
    }
}
