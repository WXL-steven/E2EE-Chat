package com.steven.e2eechat.dto.db;

import java.util.UUID;

/**
 * {@code UserSaltDTO} 数据传输对象，用于封装从数据库中获取的用户密码盐值信息。
 * <p>
 * 该对象对应于 `get_user_salt` 存储过程的返回值，包含了用户的唯一标识符和用于密码哈希的盐值。
 */
public class UserSaltDTO {
    /**
     * 用户的唯一标识符。
     * <p>
     * 此 ID 用于在后续的登录验证过程中识别用户。
     */
    private UUID userId;

    /**
     * 用于密码哈希的盐值。
     * <ul>
     *     <li>固定长度：16字节</li>
     *     <li>用于增强密码哈希的安全性，防止彩虹表攻击。</li>
     *     <li>每个用户拥有唯一的盐值。</li>
     * </ul>
     */
    private byte[] passwordSalt;

    /**
     * 获取用户的唯一标识符。
     *
     * @return 用户的 UUID。
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * 设置用户的唯一标识符。
     *
     * @param userId 用户的 UUID。
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * 获取密码盐值。
     *
     * @return 用户的密码盐值字节数组。
     */
    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * 设置密码盐值。
     *
     * @param passwordSalt 用户的密码盐值字节数组。
     */
    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
}
