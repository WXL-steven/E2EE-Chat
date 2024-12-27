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
 * 用户数据访问对象
 * 封装所有用户相关的数据库操作
 */
public class UserDAO {
    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @return 如果用户名可用返回true，否则返回false
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
     * 注册新用户
     * 使用数据库事务确保原子性
     *
     * @param username 用户名
     * @param displayName 显示名称
     * @param passwordHash 密码哈希
     * @param passwordSalt 密码盐值
     * @param vaultMasterKey 保险库主密钥
     * @return 如果注册成功返回用户ID，否则返回空
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
     * 获取用户密码盐值
     *
     * @param username 用户名
     * @return 包含用户ID和密码盐值的DTO，如果用户不存在返回空
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
     * 验证用户登录
     *
     * @param userId 用户ID
     * @param passwordHash 密码哈希
     * @return 如果验证成功返回true，否则返回false
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
     * 创建用户保险库
     * 使用数据库事务确保原子性
     *
     * @param userId 用户ID
     * @param vaultSalt 保险库盐值
     * @param vaultIv 保险库初始化向量
     * @param encryptedPrivateKey 加密的私钥
     * @param publicKey 公钥
     * @return 如果创建成功返回true，否则返回false
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
     * 获取用户保险库
     *
     * @param userId 用户ID
     * @return 用户保险库对象，如果不存在返回空
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
     * 获取用户资料
     *
     * @param userId 用户ID
     * @return 用户资料对象，如果不存在返回空
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
     * 更新用户最后在线时间
     *
     * @param userId 用户ID
     * @param timestamp 时间戳，如果为null则使用当前时间
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
     * 通过用户名获取用户UUID
     *
     * @param username 用户名
     * @return 用户UUID，如果用户不存在返回空
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
