package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.SessionDAO;
import com.steven.e2eechat.model.ChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code SessionService} 负责处理会话相关的业务逻辑，例如获取用户的最近会话列表、获取未读消息数、创建会话等。
 * <p>
 * 该服务依赖于 {@link SessionDAO} 来进行数据库操作。
 */
public class SessionService {
    private final SessionDAO sessionDAO;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
    }

    /**
     * 获取用户的最近会话列表，并按照最后消息时间降序排序。
     *
     * @param userId 用户ID，不能为空。
     * @return 用户的最近会话列表。如果用户不存在或没有会话，则返回空列表。
     */
    public List<ChatSession> getRecentSessions(UUID userId) {
        return sessionDAO.getRecentSessions(userId);
    }

    /**
     * 获取指定会话中的未读消息数量。
     *
     * @param sessionId 会话ID，不能为空。
     * @param userId    用户ID，不能为空。
     * @return 未读消息数量。如果发生错误，例如会话不存在或用户不在会话中，则返回 -1。
     */
    public int getUnreadCount(UUID sessionId, UUID userId) {
        try {
            return sessionDAO.getUnreadCount(sessionId, userId);
        } catch (Exception e) {
            // 记录日志，方便调试
            return -1;
        }
    }

    /**
     * 获取指定会话中的首条未读消息的ID。
     *
     * @param userId    用户ID，不能为空。
     * @param sessionId 会话ID，不能为空。
     * @return 如果有未读消息，则返回包含首条未读消息ID的 {@link Optional}。
     *         如果没有未读消息，则返回包含最后一条消息ID的 {@link Optional}。
     *         如果会话中没有消息，则返回空的 {@link Optional}。
     */
    public Optional<UUID> getFirstUnreadMessageId(UUID userId, UUID sessionId) {
        try {
            UUID messageId = sessionDAO.getFirstUnreadMessageId(userId, sessionId);
            return Optional.ofNullable(messageId);
        } catch (Exception e) {
            // 记录日志，方便调试
            return Optional.empty();
        }
    }

    /**
     * 获取或创建与指定用户的会话。
     *
     * @param userId      当前用户ID，不能为空。
     * @param otherUserId 对方用户ID，不能为空。
     * @return 如果成功获取或创建会话，则返回包含会话ID的 {@link Optional}。
     *         如果对方用户不存在，则返回空的 {@link Optional}。
     */
    public Optional<UUID> getOrCreateSession(UUID userId, UUID otherUserId) {
        return sessionDAO.getOrCreateSession(userId, otherUserId);
    }

    /**
     * 获取指定的会话。
     *
     * @param userId    用户ID，不能为空。
     * @param sessionId 会话ID，不能为空。
     * @return 如果会话存在且用户有权限访问，则返回包含 {@link ChatSession} 的 {@link Optional}；否则返回空的 {@link Optional}。
     */
    public Optional<ChatSession> getSession(UUID userId, UUID sessionId) {
        if (userId == null || sessionId == null) {
            return Optional.empty();
        }
        try {
            return sessionDAO.getSession(userId, sessionId);
        } catch (Exception e) {
            // 记录日志，方便调试
            return Optional.empty();
        }
    }
}
