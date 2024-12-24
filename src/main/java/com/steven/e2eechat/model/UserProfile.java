package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 用户资料实体类
 * 存储用户的基本信息，包括用户名、显示名称、公钥等
 */
public class UserProfile {
    /** 数据库自增主键，仅用于内部索引 */
    private Long idx;
    /** 用户唯一标识符（UUID） */
    private UUID userId;
    /** 用户名（仅ASCII字符，最长16字符） */
    private String username;
    /** 显示名称（UTF-8字符串，最长32字符） */
    private String displayName;
    /** 用户公钥（字节数组） */
    private byte[] publicKey;
    /** 最后在线时间（带时区） */
    private OffsetDateTime lastOnline;
    /** 注册时间（带时区） */
    private OffsetDateTime registeredAt;

    // Getters and Setters
    /**
     * 获取数据库自增主键
     * @return idx
     */
    public Long getIdx() {
        return idx;
    }

    /**
     * 设置数据库自增主键
     * @param idx idx
     */
    public void setIdx(Long idx) {
        this.idx = idx;
    }

    /**
     * 获取用户唯一标识符（UUID）
     * @return userId
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * 设置用户唯一标识符（UUID）
     * @param userId userId
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名（仅ASCII字符，最长16字符）
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名（仅ASCII字符，最长16字符）
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取显示名称（UTF-8字符串，最长32字符）
     * @return displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称（UTF-8字符串，最长32字符）
     * @param displayName displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取用户公钥（字节数组）
     * @return publicKey
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * 设置用户公钥（字节数组）
     * @param publicKey publicKey
     */
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 获取最后在线时间（带时区）
     * @return lastOnline
     */
    public OffsetDateTime getLastOnline() {
        return lastOnline;
    }

    /**
     * 设置最后在线时间（带时区）
     * @param lastOnline lastOnline
     */
    public void setLastOnline(OffsetDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    /**
     * 获取注册时间（带时区）
     * @return registeredAt
     */
    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    /**
     * 设置注册时间（带时区）
     * @param registeredAt registeredAt
     */
    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
