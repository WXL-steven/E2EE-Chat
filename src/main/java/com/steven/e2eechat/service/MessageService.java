package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.SessionDAO;
import com.steven.e2eechat.dto.db.NewMessageDTO;
import com.steven.e2eechat.model.ChatMessage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code MessageService} 负责处理消息相关的业务逻辑，例如获取消息列表、获取特定消息以及发送新消息。
 * <p>
 * 该服务依赖于 {@link SessionDAO} 来进行数据库操作。
 */
public class MessageService {
    private final SessionDAO sessionDAO;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final long DEFAULT_CURSOR = -1L;

    public MessageService() {
        this.sessionDAO = new SessionDAO();
    }

    /**
     * 获取指定游标之前的消息列表。
     * <p>
     * 如果提供了游标，则返回该游标之前的消息。如果游标为 null 或 -1，则从最新的消息开始获取。
     * 可以指定返回的消息数量限制，如果 limit 为 null 或小于 1，则使用默认值 {@link #DEFAULT_PAGE_SIZE}。
     *
     * @param userId    用户ID，不能为空。
     * @param sessionId 会话ID，不能为空。
     * @param cursor    消息游标，用于分页，可以为 null 或 -1。
     * @param limit     返回的消息数量限制，可以为 null。
     * @return 消息列表，如果 {@code userId} 或 {@code sessionId} 为 null，或者发生任何异常，则返回空列表。
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
            // 记录日志，方便调试
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定游标之后的消息列表。
     * <p>
     * 如果提供了游标，则返回该游标之后的消息。如果游标为 null 或 -1，则从最早的消息开始获取。
     * 可以指定返回的消息数量限制，如果 limit 为 null 或小于 1，则使用默认值 {@link #DEFAULT_PAGE_SIZE}。
     *
     * @param userId    用户ID，不能为空。
     * @param sessionId 会话ID，不能为空。
     * @param cursor    消息游标，用于分页，可以为 null 或 -1。
     * @param limit     返回的消息数量限制，可以为 null。
     * @return 消息列表，如果 {@code userId} 或 {@code sessionId} 为 null，或者发生任何异常，则返回空列表。
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
            // 记录日志，方便调试
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定的消息。
     *
     * @param userId    用户ID，不能为空。
     * @param messageId 消息ID，不能为空。
     * @return 如果消息存在且用户有权限访问，则返回包含 {@link ChatMessage} 的 {@link Optional}；否则返回空的 {@link Optional}。
     */
    public Optional<ChatMessage> getMessage(UUID userId, UUID messageId) {
        if (userId == null || messageId == null) {
            return Optional.empty();
        }
        return sessionDAO.getMessage(userId, messageId);
    }

    /**
     * 发送新消息。
     *
     * @param userId     发送者ID，不能为空。
     * @param newMessage 包含新消息信息的 DTO，不能为空。
     * @return 如果消息发送成功则返回 true，否则返回 false。
     */
    public boolean sendMessage(UUID userId, NewMessageDTO newMessage) {
        if (userId == null || newMessage == null) {
            return false;
        }
        try {
            return sessionDAO.sendMessage(userId, newMessage);
        } catch (Exception e) {
            // 记录日志，方便调试
            return false;
        }
    }
}
