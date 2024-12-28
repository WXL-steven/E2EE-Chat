package com.steven.e2eechat.dao;

import com.steven.e2eechat.config.DatabaseConfig;
import com.steven.e2eechat.dto.db.UserSaltDTO;
import com.steven.e2eechat.model.UserProfile;
import com.steven.e2eechat.model.UserVault;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code UserDAO} 封装了所有与用户相关的数据库操作。
 * <p>
 * 它提供了用于检查用户名可用性、注册用户、获取用户凭据、验证登录、管理用户保险库和获取用户资料的方法。
 * 所有数据库交互都通过存储过程进行，以提高安全性和数据访问的一致性。
 */
public class UserDAO {
    /**
     * 检查给定的用户名在数据库中是否可用。
     *
     * @param username 要检查的用户名。
     * @return 如果用户名可用，则返回 {@code true}；否则返回 {@code false}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public boolean checkUsernameAvailable(String username) {
        String sql = "SELECT check_username_available(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("检查用户名可用性失败", e);
        }
    }

    /**
     * 注册一个新用户到数据库。
     * <p>
     * 此操作使用数据库事务来确保注册过程的原子性。
     *
     * @param username       用户的用户名。
     * @param displayName    用户的显示名称。
     * @param passwordHash 用户的密码哈希值。
     * @param passwordSalt 用户的密码盐值。
     * @param vaultMasterKey 用户的保险库主密钥。
     * @return 如果注册成功，则返回包含新用户 UUID 的 {@link Optional}；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库操作时发生 {@link SQLException}。
     */
    public Optional<UUID> registerUser(String username, String displayName, byte[] passwordHash,
                                       byte[] passwordSalt, byte[] vaultMasterKey) {
        String sql = "SELECT register_user(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, displayName);
            stmt.setBytes(3, passwordHash);
            stmt.setBytes(4, passwordSalt);
            stmt.setBytes(5, vaultMasterKey);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID userId = (UUID) rs.getObject(1);
                return Optional.ofNullable(userId);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("SQL error occurred during registration:");
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("SQL state: " + e.getSQLState());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("用户注册失败", e);
        }
    }

    /**
     * 获取指定用户的密码盐值。
     *
     * @param username 要获取密码盐值的用户名。
     * @return 如果找到用户，则返回包含用户 ID 和密码盐值的 {@link UserSaltDTO} 的 {@link Optional}；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public Optional<UserSaltDTO> getUserSalt(String username) {
        String sql = "SELECT * FROM get_user_salt(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserSaltDTO dto = new UserSaltDTO();
                dto.setUserId((UUID) rs.getObject("user_id"));
                dto.setPasswordSalt(rs.getBytes("password_salt"));
                return Optional.of(dto);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取用户密码盐值失败", e);
        }
    }

    /**
     * 验证用户的登录凭据。
     *
     * @param userId       用户的 UUID。
     * @param passwordHash 用户提供的密码哈希值。
     * @return 如果提供的密码哈希与数据库中的匹配，则返回 {@code true}；否则返回 {@code false}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public boolean verifyLogin(UUID userId, byte[] passwordHash) {
        String sql = "SELECT verify_login(?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setBytes(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("验证用户登录失败", e);
        }
    }

    /**
     * 为用户创建新的保险库。
     * <p>
     * 此操作使用数据库事务来确保创建过程的原子性。
     *
     * @param userId            要创建保险库的用户的 UUID。
     * @param vaultSalt         保险库的盐值。
     * @param vaultIv           保险库的初始化向量。
     * @param encryptedPrivateKey 加密的私钥。
     * @param publicKey         用户的公钥。
     * @return 如果成功创建保险库，则返回 {@code true}；否则返回 {@code false}。
     * @throws RuntimeException 如果在执行数据库操作时发生 {@link SQLException}。
     */
    public boolean createVault(UUID userId, byte[] vaultSalt, byte[] vaultIv,
                               byte[] encryptedPrivateKey, byte[] publicKey) {
        String sql = "SELECT create_vault(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, userId);
                stmt.setBytes(2, vaultSalt);
                stmt.setBytes(3, vaultIv);
                stmt.setBytes(4, encryptedPrivateKey);
                stmt.setBytes(5, publicKey);

                ResultSet rs = stmt.executeQuery();
                boolean success = rs.next() && rs.getBoolean(1);

                if (success) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("创建用户保险库失败", e);
        }
    }

    /**
     * 获取指定用户的保险库信息。
     *
     * @param userId 要获取保险库信息的用户的 UUID。
     * @return 如果找到用户保险库，则返回包含保险库信息的 {@link UserVault} 的 {@link Optional}；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public Optional<UserVault> getVault(UUID userId) {
        String sql = "SELECT * FROM get_vault(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserVault vault = new UserVault();
                vault.setUserId(userId);
                vault.setVaultMasterKey(rs.getBytes("vault_master_key"));
                vault.setVaultSalt(rs.getBytes("vault_salt"));
                vault.setVaultIv(rs.getBytes("vault_iv"));
                vault.setEncryptedPrivateKey(rs.getBytes("encrypted_private_key"));
                vault.setReady(rs.getBoolean("ready"));
                return Optional.of(vault);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取用户保险库失败", e);
        }
    }

    /**
     * 获取指定用户的用户资料信息。
     *
     * @param userId 要获取资料的用户的 UUID。
     * @return 如果找到用户，则返回包含用户资料的 {@link UserProfile} 的 {@link Optional}；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public Optional<UserProfile> getUserProfile(UUID userId) {
        String sql = "SELECT * FROM get_user_profile(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserProfile profile = new UserProfile();
                profile.setUserId(userId);
                profile.setUsername(rs.getString("username"));
                profile.setDisplayName(rs.getString("display_name"));
                profile.setPublicKey(rs.getBytes("public_key"));
                profile.setLastOnline(rs.getObject("last_online", OffsetDateTime.class));
                profile.setRegisteredAt(rs.getObject("registered_at", OffsetDateTime.class));
                return Optional.of(profile);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取用户资料失败", e);
        }
    }

    /**
     * 更新用户的最后在线时间。
     *
     * @param userId    要更新最后在线时间的用户的 UUID。
     * @param timestamp 用户最后一次在线的时间戳。如果为 {@code null}，则使用当前时间。
     * @throws RuntimeException 如果在执行数据库更新时发生 {@link SQLException}。
     */
    public void updateLastOnline(UUID userId, OffsetDateTime timestamp) {
        String sql = "SELECT update_last_online(?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, timestamp != null ? timestamp : OffsetDateTime.now());
            stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException("更新用户最后在线时间失败", e);
        }
    }

    /**
     * 通过用户名获取用户的 UUID。
     *
     * @param username 要查找的用户名。
     * @return 如果找到用户，则返回包含用户 UUID 的 {@link Optional}；否则返回空的 {@link Optional}。
     * @throws RuntimeException 如果在执行数据库查询时发生 {@link SQLException}。
     */
    public Optional<UUID> getUserUuidByUsername(String username) {
        String sql = "SELECT get_user_uuid_by_username(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UUID userId = (UUID) rs.getObject(1);
                return Optional.ofNullable(userId);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("获取用户UUID失败", e);
        }
    }
}
