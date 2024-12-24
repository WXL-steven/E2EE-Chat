package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * 用户认证信息实体类
 * 存储用户的密码散列和盐值
 */
public class UserCredential {
    /** 关联的用户UUID */
    private UUID userId;
    /** 密码散列值（32字节定长） */
    private byte[] passwordHash;
    /** 密码盐值（16字节定长） */
    private byte[] passwordSalt;

    // Getters and Setters
    /**
     * 获取用户UUID
     * @return userId
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * 设置用户UUID
     * @param userId userId
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * 获取密码散列值（32字节定长）
     * @return passwordHash
     */
    public byte[] getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码散列值（32字节定长）
     * @param passwordHash passwordHash
     */
    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 获取密码盐值（16字节定长）
     * @return passwordSalt
     */
    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * 设置密码盐值（16字节定长）
     * @param passwordSalt passwordSalt
     */
    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
}
