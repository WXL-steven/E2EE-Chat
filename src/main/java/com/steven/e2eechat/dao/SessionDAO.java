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
 * {@code SessionDAO} 封装了所有与聊天会话相关的数据库操作。
 * <p>
 * 它提供了用于获取最近会话、未读消息计数、消息列表、发送消息以及管理会话的方法。
 * 所有数据库交互都通过存储过程进行。
 */
public class SessionDAO {

    /**
     * 获取指定用户的最近聊天会话列表。
     * <p>
     * 会话列表按照最后消息的时间降序排列。
     *
     * @param userId 用户的 UUID。
     * @return 包含用户最近会话的 {@link List}<{@link ChatSession}>。如果用户没有会话，则返回空列表。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public List<ChatSession> getRecentSessions(UUID userId) {
        String sql = "SELECT * FROM get_recent_sessions(?)";
        List<ChatSession> sessions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ChatSession session = getSessionModel(rs);
                sessions.add(session);
            }

            return sessions;
        } catch (SQLException e) {
            throw new RuntimeException("获取最近会话列表失败", e);
        }
    }

    /**
     * 获取指定会话中指定用户的未读消息数量。
     *
     * @param sessionId 会话的 UUID。
     * @param userId    用户的 UUID。
     * @return 未读消息的数量。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
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
     * 获取指定会话中指定用户的首条未读消息的 ID。
     * <p>
     * 如果所有消息都已读，则返回最后一条消息的 ID。
     *
     * @param userId    用户的 UUID。
     * @param sessionId 会话的 UUID。
     * @return 首条未读消息的 UUID，如果会话中没有消息则返回 {@code null}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
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
     * 获取指定会话中指定用户在给定游标之前的消息列表。
     *
     * @param userId    用户的 UUID。
     * @param sessionId 会话的 UUID。
     * @param cursor    消息游标，使用 -1 表示从最新消息开始。
     * @param limit     返回的消息数量限制。
     * @return 包含消息的 {@link List}<{@link ChatMessage}>。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public List<ChatMessage> getMessagesBefore(UUID userId, UUID sessionId, long cursor, int limit) {
        String sql = "SELECT * FROM get_messages_before(?, ?, ?, ?)";
        return getMessages(sql, userId, sessionId, cursor, limit);
    }

    /**
     * 获取指定会话中指定用户在给定游标之后的消息列表。
     *
     * @param userId    用户的 UUID。
     * @param sessionId 会话的 UUID。
     * @param cursor    消息游标，使用 -1 表示从最早消息开始。
     * @param limit     返回的消息数量限制。
     * @return 包含消息的 {@link List}<{@link ChatMessage}>。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public List<ChatMessage> getMessagesAfter(UUID userId, UUID sessionId, long cursor, int limit) {
        String sql = "SELECT * FROM get_messages_after(?, ?, ?, ?)";
        return getMessages(sql, userId, sessionId, cursor, limit);
    }

    /**
     * 发送一条新的聊天消息。
     *
     * @param userId     发送消息的用户的 UUID。
     * @param newMessage 包含新消息详细信息的 {@link NewMessageDTO}。
     * @return 如果消息发送成功，则返回 {@code true}；否则返回 {@code false}。
     * @throws RuntimeException 如果在执行数据库操作时发生 {@link SQLException}。
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
     * 获取或创建两个用户之间的聊天会话。
     * <p>
     * 如果两个用户之间已经存在会话，则返回现有会话的 ID。否则，创建一个新的会话并返回其 ID。
     *
     * @param userId      当前用户的 UUID。
     * @param otherUserId 对方用户的 UUID。
     * @return 如果成功，则返回包含会话 UUID 的 {@link Optional}。如果对方用户不存在，则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库操作时发生 {@link SQLException}。
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
                return Optional.ofNullable(sessionId);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取或创建会话失败", e);
        }
    }

    /**
     * 获取指定用户的指定聊天会话。
     * <p>
     * 只有当用户是会话的发起者或参与者时才能获取。
     *
     * @param userId    用户的 UUID。
     * @param sessionId 会话的 UUID。
     * @return 如果会话存在且用户有权限访问，则返回包含会话信息的 {@link Optional}<{@link ChatSession}>；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public Optional<ChatSession> getSession(UUID userId, UUID sessionId) {
        String sql = "SELECT * FROM get_session(?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ChatSession session = getSessionModel(rs);
                return Optional.of(session);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取会话失败", e);
        }
    }

    private ChatSession getSessionModel(ResultSet rs) throws SQLException {
        ChatSession session = new ChatSession();
        session.setSessionId((UUID) rs.getObject("session_id"));
        session.setInitiatorId((UUID) rs.getObject("initiator_id"));
        session.setParticipantId((UUID) rs.getObject("participant_id"));
        session.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        session.setMessageCounter(rs.getLong("message_counter"));
        session.setLastMessageId(rs.getObject("last_message_id", Object.class) instanceof UUID ? (UUID) rs.getObject("last_message_id") : null);
        session.setLastMessageAt(rs.getObject("last_message_at", Object.class) instanceof OffsetDateTime ? rs.getObject("last_message_at", OffsetDateTime.class) : null);
        return session;
    }

    /**
     * 获取指定用户的指定聊天消息。
     * <p>
     * 只有当用户是消息的发送者或接收者时才能获取。
     *
     * @param userId    用户的 UUID。
     * @param messageId 消息的 UUID。
     * @return 如果消息存在且用户有权限访问，则返回包含消息信息的 {@link Optional}<{@link ChatMessage}>；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
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
     * 获取消息列表的辅助方法。
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
