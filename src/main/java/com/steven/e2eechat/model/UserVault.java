package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * 用户保险库模型
 * 对应数据库中的user_vaults表
 * 用于存储用户的加密相关信息
 */
public class UserVault {
    /**
     * 用户唯一标识
     * 关联到user_profiles表的user_id
     */
    private UUID userId;

    /**
     * 保险库主密钥
     * - 固定长度：32字节
     * - 在用户注册时生成
     * - 用于加密其他密钥
     */
    private byte[] vaultMasterKey;

    /**
     * 保险库盐值
     * - 固定长度：16字节
     * - 在创建保险库时生成
     * - 用于密钥派生
     */
    private byte[] vaultSalt;

    /**
     * 保险库初始化向量
     * - 固定长度：12字节
     * - 在创建保险库时生成
     * - 用于AES-GCM加密
     */
    private byte[] vaultIv;

    /**
     * 加密的私钥
     * - 长度范围：48-64字节
     * - 使用派生密钥加密的用户私钥
     * - 配对的公钥存储在user_profiles表中
     */
    private byte[] encryptedPrivateKey;

    /**
     * 保险库就绪状态
     * - true：保险库配置完成，可以使用
     * - false：保险库未完成配置（仅有主密钥）
     */
    private boolean ready;

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public byte[] getVaultMasterKey() {
        return vaultMasterKey;
    }

    public void setVaultMasterKey(byte[] vaultMasterKey) {
        this.vaultMasterKey = vaultMasterKey;
    }

    public byte[] getVaultSalt() {
        return vaultSalt;
    }

    public void setVaultSalt(byte[] vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    public byte[] getVaultIv() {
        return vaultIv;
    }

    public void setVaultIv(byte[] vaultIv) {
        this.vaultIv = vaultIv;
    }

    public byte[] getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
