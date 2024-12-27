package com.steven.e2eechat.dao;

import com.steven.e2eechat.config.DatabaseConfig;
import com.steven.e2eechat.dto.db.NewMessageDTO;
import com.steven.e2eechat.model.ChatMessage;
import com.steven.e2eechat.model.ChatSession;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 会话数据访问对象
 * 封装所有会话相关的数据库操作
 */
public class SessionDAO {
    
    /**
     * 获取用户的最近会话列表
     *
     * @param userId 用户ID
     * @return 会话列表，按最后消息时间降序排序
     */
    public List<ChatSession> getRecentSessions(UUID userId) {
        String sql = "SELECT * FROM get_recent_sessions(?)";
        List<ChatSession> sessions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ChatSession session = new ChatSession();
                session.setSessionId((UUID) rs.getObject("session_id"));
                session.setInitiatorId((UUID) rs.getObject("initiator_id"));
                session.setParticipantId((UUID) rs.getObject("participant_id"));
                session.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                session.setMessageCounter(rs.getLong("message_counter"));
                session.setLastMessageId(rs.getObject("last_message_id") != null ? (UUID) rs.getObject("last_message_id") : null);
                session.setLastMessageAt(rs.getObject("last_message_at") != null ? rs.getObject("last_message_at", OffsetDateTime.class) : null);
                sessions.add(session);
            }
            
            return sessions;
        } catch (SQLException e) {
            throw new RuntimeException("获取最近会话列表失败", e);
        }
    }

    /**
     * 获取会话中的未读消息数量
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 未读消息数量
     */
    public int getUnreadCount(UUID sessionId, UUID userId) {
        String sql = "SELECT get_unread_count(?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, sessionId);
            stmt.setObject(2, userId);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("获取未读消息数量失败", e);
        }
    }

    /**
     * 获取会话中的首条未读消息ID
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 首条未读消息ID，如果全部已读则返回最后一条消息的ID
     */
    public UUID getFirstUnreadMessageId(UUID userId, UUID sessionId) {
        String sql = "SELECT get_first_unread(?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() ? (UUID) rs.getObject(1) : null;
        } catch (SQLException e) {
            throw new RuntimeException("获取首条未读消息ID失败", e);
        }
    }

    /**
     * 获取指定游标之前的消息
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param cursor 消息游标，-1表示从最新消息开始
     * @param limit 返回的消息数量限制
     * @return 消息列表
     */
    public List<ChatMessage> getMessagesBefore(UUID userId, UUID sessionId, long cursor, int limit) {
        String sql = "SELECT * FROM get_messages_before(?, ?, ?, ?)";
        return getMessages(sql, userId, sessionId, cursor, limit);
    }

    /**
     * 获取指定游标之后的消息
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param cursor 消息游标，-1表示从最早消息开始
     * @param limit 返回的消息数量限制
     * @return 消息列表
     */
    public List<ChatMessage> getMessagesAfter(UUID userId, UUID sessionId, long cursor, int limit) {
        String sql = "SELECT * FROM get_messages_after(?, ?, ?, ?)";
        return getMessages(sql, userId, sessionId, cursor, limit);
    }

    /**
     * 发送新消息
     *
     * @param userId 发送者ID
     * @param newMessage 新消息DTO
     * @return 是否发送成功
     */
    public boolean sendMessage(UUID userId, NewMessageDTO newMessage) {
        String sql = "SELECT send_message(?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, newMessage.getSessionId());
            stmt.setBytes(3, newMessage.getMessageIv());
            stmt.setBytes(4, newMessage.getMessageContent());
            stmt.setBoolean(5, newMessage.isSystem());
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 获取或创建会话
     * 如果两个用户之间已有会话则返回现有会话ID，否则创建新会话
     *
     * @param userId 当前用户ID
     * @param otherUserId 对方用户ID
     * @return 如果成功返回会话ID，如果对方用户不存在返回空
     */
    public Optional<UUID> getOrCreateSession(UUID userId, UUID otherUserId) {
        String sql = "SELECT get_or_create_session(?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, otherUserId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                UUID sessionId = (UUID) rs.getObject(1);
                return sessionId != null ? Optional.of(sessionId) : Optional.empty();
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取或创建会话失败", e);
        }
    }

    /**
     * 获取指定会话
     * 仅当用户是会话的发起者或参与者时才能获取
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 如果会话存在且用户有权限访问则返回会话对象，否则返回空
     */
    public Optional<ChatSession> getSession(UUID userId, UUID sessionId) {
        String sql = "SELECT * FROM get_session(?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                ChatSession session = new ChatSession();
                session.setSessionId((UUID) rs.getObject("session_id"));
                session.setInitiatorId((UUID) rs.getObject("initiator_id"));
                session.setParticipantId((UUID) rs.getObject("participant_id"));
                session.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                session.setMessageCounter(rs.getLong("message_counter"));
                session.setLastMessageId(rs.getObject("last_message_id") != null ? (UUID) rs.getObject("last_message_id") : null);
                session.setLastMessageAt(rs.getObject("last_message_at") != null ? rs.getObject("last_message_at", OffsetDateTime.class) : null);
                return Optional.of(session);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取会话失败", e);
        }
    }

    /**
     * 获取指定消息
     * 仅当用户是消息的发送者或接收者时才能获取
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 如果消息存在且用户有权限访问则返回消息对象，否则返回空
     */
    public Optional<ChatMessage> getMessage(UUID userId, UUID messageId) {
        String sql = "SELECT * FROM get_message(?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, messageId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(messageId);
                message.setSessionId((UUID) rs.getObject("session_id"));
                message.setCursor(rs.getLong("cursor"));
                message.setSenderId((UUID) rs.getObject("sender_id"));
                message.setReceiverId((UUID) rs.getObject("receiver_id"));
                message.setSystem(rs.getBoolean("is_system"));
                message.setRead(rs.getBoolean("is_read"));
                message.setMessageIv(rs.getBytes("message_iv"));
                message.setMessageContent(rs.getBytes("message_content"));
                message.setSentAt(rs.getObject("sent_at", OffsetDateTime.class));
                return Optional.of(message);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取消息失败", e);
        }
    }

    /**
     * 获取消息列表的辅助方法
     */
    private List<ChatMessage> getMessages(String sql, UUID userId, UUID sessionId, long cursor, int limit) {
        List<ChatMessage> messages = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setObject(2, sessionId);
            stmt.setLong(3, cursor);
            stmt.setInt(4, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId((UUID) rs.getObject("message_id"));
                message.setCursor(rs.getLong("cursor"));
                message.setSenderId((UUID) rs.getObject("sender_id"));
                message.setReceiverId((UUID) rs.getObject("receiver_id"));
                message.setSystem(rs.getBoolean("is_system"));
                message.setRead(rs.getBoolean("is_read"));
                message.setMessageIv(rs.getBytes("message_iv"));
                message.setMessageContent(rs.getBytes("message_content"));
                message.setSentAt(rs.getObject("sent_at", OffsetDateTime.class));
                messages.add(message);
            }
            
            return messages;
        } catch (SQLException e) {
            throw new RuntimeException("获取消息列表失败", e);
        }
    }
}
