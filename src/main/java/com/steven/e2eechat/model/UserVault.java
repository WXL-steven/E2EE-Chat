package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * {@code UserVault} 模型类，映射数据库中的 {@code user_vaults} 表。
 * <p>
 * 用于存储用户的加密相关信息，包括主密钥、盐值、初始化向量和加密后的私钥。
 *
 * <p><b>实例特性：</b></p>
 * <ul>
 *     <li>{@code userId}: 用户唯一标识，关联到 {@code user_profiles} 表的 {@code user_id}。</li>
 *     <li>{@code vaultMasterKey}: 保险库主密钥，固定长度 32 字节，在用户注册时生成，用于加密其他密钥。</li>
 *     <li>{@code vaultSalt}: 保险库盐值，固定长度 16 字节，在创建保险库时生成，用于密钥派生。</li>
 *     <li>{@code vaultIv}: 保险库初始化向量，固定长度 12 字节，在创建保险库时生成，用于 AES-GCM 加密。</li>
 *     <li>{@code encryptedPrivateKey}: 加密的私钥，长度范围 32-256 字节，使用派生密钥加密的用户私钥，配对的公钥存储在 {@code user_profiles} 表中。</li>
 *     <li>{@code ready}: 保险库就绪状态，{@code true} 表示保险库配置完成可以使用，{@code false} 表示保险库未完成配置（仅有主密钥）。</li>
 * </ul>
 */
public class UserVault {
    /**
     * 用户唯一标识，关联到 user_profiles 表的 user_id。
     */
    private UUID userId;
    /**
     * 保险库主密钥，固定长度 32 字节，在用户注册时生成，用于加密其他密钥。
     */
    private byte[] vaultMasterKey;
    /**
     * 保险库盐值，固定长度 16 字节，在创建保险库时生成，用于密钥派生。
     */
    private byte[] vaultSalt;
    /**
     * 保险库初始化向量，固定长度 12 字节，在创建保险库时生成，用于 AES-GCM 加密。
     */
    private byte[] vaultIv;
    /**
     * 加密的私钥，长度范围 32-256 字节，使用派生密钥加密的用户私钥，配对的公钥存储在 user_profiles 表中。
     */
    private byte[] encryptedPrivateKey;
    /**
     * 保险库就绪状态，true 表示保险库配置完成可以使用，false 表示保险库未完成配置（仅有主密钥）。
     */
    private boolean ready;

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
     * 获取保险库主密钥。
     *
     * @return 保险库主密钥。
     */
    public byte[] getVaultMasterKey() {
        return vaultMasterKey;
    }

    /**
     * 设置保险库主密钥。
     *
     * @param vaultMasterKey 保险库主密钥。
     */
    public void setVaultMasterKey(byte[] vaultMasterKey) {
        this.vaultMasterKey = vaultMasterKey;
    }

    /**
     * 获取保险库盐值。
     *
     * @return 保险库盐值。
     */
    public byte[] getVaultSalt() {
        return vaultSalt;
    }

    /**
     * 设置保险库盐值。
     *
     * @param vaultSalt 保险库盐值。
     */
    public void setVaultSalt(byte[] vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    /**
     * 获取保险库初始化向量。
     *
     * @return 保险库初始化向量。
     */
    public byte[] getVaultIv() {
        return vaultIv;
    }

    /**
     * 设置保险库初始化向量。
     *
     * @param vaultIv 保险库初始化向量。
     */
    public void setVaultIv(byte[] vaultIv) {
        this.vaultIv = vaultIv;
    }

    /**
     * 获取加密后的私钥。
     *
     * @return 加密后的私钥。
     */
    public byte[] getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * 设置加密后的私钥。
     *
     * @param encryptedPrivateKey 加密后的私钥。
     */
    public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    /**
     * 获取保险库的就绪状态。
     *
     * @return 如果保险库已就绪，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 设置保险库的就绪状态。
     *
     * @param ready {@code true} 表示保险库已就绪，{@code false} 表示未就绪。
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
