package com.steven.e2eechat.model;

import java.util.UUID;

/**
 * 用户密钥保险库实体类
 * 存储用户的加密私钥和相关密钥材料
 */
public class UserVault {
    /** 关联的用户UUID */
    private UUID userId;
    /** 保险库主密钥（32字节定长） */
    private byte[] vaultMasterKey;
    /** 保险库盐值（16字节定长） */
    private byte[] vaultSalt;
    /** 保险库初始化向量（12字节定长） */
    private byte[] vaultIV;
    /** 加密的用户私钥（64字节定长） */
    private byte[] encryptedPrivateKey;

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
     * 获取保险库主密钥（32字节定长）
     * @return vaultMasterKey
     */
    public byte[] getVaultMasterKey() {
        return vaultMasterKey;
    }

    /**
     * 设置保险库主密钥（32字节定长）
     * @param vaultMasterKey vaultMasterKey
     */
    public void setVaultMasterKey(byte[] vaultMasterKey) {
        this.vaultMasterKey = vaultMasterKey;
    }

    /**
     * 获取保险库盐值（16字节定长）
     * @return vaultSalt
     */
    public byte[] getVaultSalt() {
        return vaultSalt;
    }

    /**
     * 设置保险库盐值（16字节定长）
     * @param vaultSalt vaultSalt
     */
    public void setVaultSalt(byte[] vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    /**
     * 获取保险库初始化向量（12字节定长）
     * @return vaultIV
     */
    public byte[] getVaultIV() {
        return vaultIV;
    }

    /**
     * 设置保险库初始化向量（12字节定长）
     * @param vaultIV vaultIV
     */
    public void setVaultIV(byte[] vaultIV) {
        this.vaultIV = vaultIV;
    }

    /**
     * 获取加密的用户私钥（64字节定长）
     * @return encryptedPrivateKey
     */
    public byte[] getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * 设置加密的用户私钥（64字节定长）
     * @param encryptedPrivateKey encryptedPrivateKey
     */
    public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}
