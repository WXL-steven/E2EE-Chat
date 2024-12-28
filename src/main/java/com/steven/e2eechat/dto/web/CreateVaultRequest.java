package com.steven.e2eechat.dto.web;

import java.util.Base64;

/**
 * {@code CreateVaultRequest} DTO 用于封装客户端创建用户保险库的请求数据。
 * <p>
 * 该 DTO 包含保险库的盐值、初始化向量 (IV)、加密后的私钥和公钥，这些数据都以 Base64 编码的字符串形式传输。
 * 在后端接收后，这些 Base64 编码的字符串会被解码为原始字节数组。
 */
public class CreateVaultRequest {
    private String vaultSalt;
    private String vaultIv;
    private String encryptedPrivateKey;
    private String publicKey;

    /**
     * 获取 Base64 编码的保险库盐值并解码为字节数组。
     *
     * @return 解码后的保险库盐值字节数组。
     */
    public byte[] getVaultSalt() {
        return Base64.getDecoder().decode(vaultSalt);
    }

    /**
     * 设置 Base64 编码的保险库盐值。
     *
     * @param vaultSalt Base64 编码的保险库盐值字符串。
     */
    public void setVaultSalt(String vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    /**
     * 获取 Base64 编码的保险库初始化向量 (IV) 并解码为字节数组。
     *
     * @return 解码后的保险库 IV 字节数组。
     */
    public byte[] getVaultIv() {
        return Base64.getDecoder().decode(vaultIv);
    }

    /**
     * 设置 Base64 编码的保险库初始化向量 (IV)。
     *
     * @param vaultIv Base64 编码的保险库 IV 字符串。
     */
    public void setVaultIv(String vaultIv) {
        this.vaultIv = vaultIv;
    }

    /**
     * 获取 Base64 编码的加密后私钥并解码为字节数组。
     *
     * @return 解码后的加密私钥字节数组。
     */
    public byte[] getEncryptedPrivateKey() {
        return Base64.getDecoder().decode(encryptedPrivateKey);
    }

    /**
     * 设置 Base64 编码的加密后私钥。
     *
     * @param encryptedPrivateKey Base64 编码的加密私钥字符串。
     */
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    /**
     * 获取 Base64 编码的公钥并解码为字节数组。
     *
     * @return 解码后的公钥字节数组。
     */
    public byte[] getPublicKey() {
        return Base64.getDecoder().decode(publicKey);
    }

    /**
     * 设置 Base64 编码的公钥。
     *
     * @param publicKey Base64 编码的公钥字符串。
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
