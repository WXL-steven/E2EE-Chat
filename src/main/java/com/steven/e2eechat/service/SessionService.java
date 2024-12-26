package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.SessionDAO;
import com.steven.e2eechat.model.ChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 会话服务
 * 处理会话相关的业务逻辑
 */
public class SessionService {
    private final SessionDAO sessionDAO;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
    }

    /**
     * 获取用户的最近会话列表
     *
     * @param userId 用户ID
     * @return 会话列表，按最后消息时间降序排序
     */
    public List<ChatSession> getRecentSessions(UUID userId) {
        return sessionDAO.getRecentSessions(userId);
    }

    /**
     * 获取会话中的未读消息数量
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 未读消息数量，如果发生错误返回-1
     */
    public int getUnreadCount(UUID sessionId, UUID userId) {
        try {
            return sessionDAO.getUnreadCount(sessionId, userId);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取会话中的首条未读消息ID
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 如果有未读消息返回其ID，否则返回最后一条消息的ID，如果没有消息返回空
     */
    public Optional<UUID> getFirstUnreadMessageId(UUID userId, UUID sessionId) {
        try {
            UUID messageId = sessionDAO.getFirstUnreadMessageId(userId, sessionId);
            return Optional.ofNullable(messageId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 获取或创建与指定用户的会话
     *
     * @param userId 当前用户ID
     * @param otherUserId 对方用户ID
     * @return 如果成功返回会话ID，如果对方用户不存在返回空
     */
    public Optional<UUID> getOrCreateSession(UUID userId, UUID otherUserId) {
        return sessionDAO.getOrCreateSession(userId, otherUserId);
    }
}
