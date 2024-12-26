package com.steven.e2eechat.dto.db;

import java.util.UUID;

/**
 * 用户密码盐值DTO
 * 对应get_user_salt存储过程的返回值
 */
public class UserSaltDTO {
    /**
     * 用户唯一标识
     * 用于后续登录验证
     */
    private UUID userId;

    /**
     * 密码盐值
     * - 固定长度：16字节
     * - 用于客户端计算密码哈希
     * - 每个用户唯一
     */
    private byte[] passwordSalt;

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
}
