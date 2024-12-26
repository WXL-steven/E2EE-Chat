package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * 用户保险库模型
 * 对应数据库中的user_vaults表
 * 用于存储用户的加密相关信息
 *
 * 实例特性：
 * - userId: 用户唯一标识，关联到user_profiles表的user_id
 * - vaultMasterKey: 保险库主密钥，固定长度32字节，在用户注册时生成，用于加密其他密钥
 * - vaultSalt: 保险库盐值，固定长度16字节，在创建保险库时生成，用于密钥派生
 * - vaultIv: 保险库初始化向量，固定长度12字节，在创建保险库时生成，用于AES-GCM加密
 * - encryptedPrivateKey: 加密的私钥，长度范围48-64字节，使用派生密钥加密的用户私钥，配对的公钥存储在user_profiles表中
 * - ready: 保险库就绪状态，true表示保险库配置完成可以使用，false表示保险库未完成配置（仅有主密钥）
 */
public class UserVault {
    private UUID userId;
    private byte[] vaultMasterKey;
    private byte[] vaultSalt;
    private byte[] vaultIv;
    private byte[] encryptedPrivateKey;
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
