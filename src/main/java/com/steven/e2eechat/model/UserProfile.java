package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 用户资料模型
 * 对应数据库中的user_profiles表
 * 
 * 实例特性：
 * - userId: 用户唯一标识，UUID格式，由系统自动生成
 * - username: 用户名
 *   - 长度：1-16个字符
 *   - 字符集：仅ASCII字符（字母、数字、下划线、连字符）
 *   - 格式：^[a-zA-Z0-9_-]{1,16}$
 *   - 唯一性：在系统中必须唯一
 * - displayName: 显示名称
 *   - 长度：1-32个字符
 *   - 字符集：UTF-8编码的Unicode字符
 *   - 用于界面显示，支持各种语言
 * - publicKey: 用户公钥
 *   - 用于端到端加密
 *   - 可为空（注册时为空，创建保险库时设置）
 *   - 使用Base64编码存储
 * - lastOnline: 最后在线时间
 *   - 使用带时区的时间戳
 *   - 由系统自动更新
 *   - 用于显示用户状态
 * - registeredAt: 注册时间
 *   - 使用带时区的时间戳
 *   - 由系统在用户注册时自动设置
 *   - 不可修改
 */
public class UserProfile {
    private UUID userId;
    private String username;
    private String displayName;
    private byte[] publicKey;
    private OffsetDateTime lastOnline;
    private OffsetDateTime registeredAt;

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public OffsetDateTime getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(OffsetDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
