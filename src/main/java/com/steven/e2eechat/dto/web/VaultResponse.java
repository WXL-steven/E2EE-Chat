package com.steven.e2eechat.dto.web;

/**
 * {@code VaultResponse} DTO 用于封装服务器端返回给客户端的保险库信息。
 * <p>
 * 该 DTO 包含保险库的主密钥、盐值、初始化向量 (IV)、加密后的私钥以及一个表示保险库是否已准备就绪的布尔值。
 * 所有字符串类型的属性通常以 Base64 编码的形式传输。
 */
public class VaultResponse {
    private final String vaultMasterKey;
    private final String vaultSalt;
    private final String vaultIv;
    private final String encryptedPrivateKey;
    private final boolean ready;

    /**
     * 构造一个 {@code VaultResponse} 对象。
     *
     * @param vaultMasterKey    Base64 编码的保险库主密钥。
     * @param vaultSalt         Base64 编码的保险库盐值。
     * @param vaultIv           Base64 编码的保险库初始化向量 (IV)。
     * @param encryptedPrivateKey Base64 编码的加密后私钥。
     * @param ready             指示保险库是否已准备就绪的布尔值。
     */
    public VaultResponse(String vaultMasterKey, String vaultSalt, String vaultIv,
                         String encryptedPrivateKey, boolean ready) {
        this.vaultMasterKey = vaultMasterKey;
        this.vaultSalt = vaultSalt;
        this.vaultIv = vaultIv;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.ready = ready;
    }

    /**
     * 获取 Base64 编码的保险库主密钥。
     *
     * @return Base64 编码的保险库主密钥字符串。
     */
    public String getVaultMasterKey() {
        return vaultMasterKey;
    }

    /**
     * 获取 Base64 编码的保险库盐值。
     *
     * @return Base64 编码的保险库盐值字符串。
     */
    public String getVaultSalt() {
        return vaultSalt;
    }

    /**
     * 获取 Base64 编码的保险库初始化向量 (IV)。
     *
     * @return Base64 编码的保险库 IV 字符串。
     */
    public String getVaultIv() {
        return vaultIv;
    }

    /**
     * 获取 Base64 编码的加密后私钥。
     *
     * @return Base64 编码的加密后私钥字符串。
     */
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * 获取指示保险库是否已准备就绪的布尔值。
     *
     * @return 如果保险库已准备就绪，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isReady() {
        return ready;
    }
}
