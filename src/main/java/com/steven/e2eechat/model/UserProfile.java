package com.steven.e2eechat.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * {@code UserProfile} 模型类，映射数据库中的 {@code user_profiles} 表。
 * <p>
 * 实例特性：
 * <ul>
 *     <li>{@code userId}: 用户唯一标识，{@link UUID} 格式，由系统自动生成。</li>
 *     <li>{@code username}: 用户名
 *         <ul>
 *             <li>长度：1-16个字符。</li>
 *             <li>字符集：仅ASCII字符（字母、数字、下划线、连字符）。</li>
 *             <li>格式：{@code ^[a-zA-Z0-9_-]{1,16}$}。</li>
 *             <li>唯一性：在系统中必须唯一。</li>
 *         </ul>
 *     </li>
 *     <li>{@code displayName}: 显示名称
 *         <ul>
 *             <li>长度：1-32个字符。</li>
 *             <li>字符集：UTF-8编码的Unicode字符。</li>
 *             <li>用于界面显示，支持各种语言。</li>
 *         </ul>
 *     </li>
 *     <li>{@code publicKey}: 用户公钥
 *         <ul>
 *             <li>用于端到端加密。</li>
 *             <li>可为空（注册时为空，创建保险库时设置）。</li>
 *         </ul>
 *     </li>
 *     <li>{@code lastOnline}: 最后在线时间
 *         <ul>
 *             <li>使用带时区的 {@link OffsetDateTime}。</li>
 *             <li>由系统自动更新。</li>
 *             <li>用于显示用户状态。</li>
 *         </ul>
 *     </li>
 *     <li>{@code registeredAt}: 注册时间
 *         <ul>
 *             <li>使用带时区的 {@link OffsetDateTime}。</li>
 *             <li>由系统在用户注册时自动设置。</li>
 *             <li>不可修改。</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class UserProfile {
    private UUID userId;
    private String username;
    private String displayName;
    private byte[] publicKey;
    private OffsetDateTime lastOnline;
    private OffsetDateTime registeredAt;

    /**
     * 获取用户的唯一标识符。
     *
     * @return 用户 ID。
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * 设置用户的唯一标识符。
     *
     * @param userId 用户 ID。
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取显示名称。
     *
     * @return 显示名称。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称。
     *
     * @param displayName 显示名称。
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取用户的公钥。
     *
     * @return 公钥。
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * 设置用户的公钥。
     *
     * @param publicKey 公钥。
     */
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 获取最后在线时间。
     *
     * @return 最后在线时间。
     */
    public OffsetDateTime getLastOnline() {
        return lastOnline;
    }

    /**
     * 设置最后在线时间。
     *
     * @param lastOnline 最后在线时间。
     */
    public void setLastOnline(OffsetDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    /**
     * 获取注册时间。
     *
     * @return 注册时间。
     */
    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    /**
     * 设置注册时间。
     *
     * @param registeredAt 注册时间。
     */
    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
