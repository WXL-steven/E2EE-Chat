package com.steven.e2eechat.dto.web;

import java.util.Base64;

/**
 * 创建保险库请求DTO
 */
public class CreateVaultRequest {
    private String vaultSalt;
    private String vaultIv;
    private String encryptedPrivateKey;
    private String publicKey;

    public byte[] getVaultSalt() {
        return Base64.getDecoder().decode(vaultSalt);
    }

    public void setVaultSalt(String vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    public byte[] getVaultIv() {
        return Base64.getDecoder().decode(vaultIv);
    }

    public void setVaultIv(String vaultIv) {
        this.vaultIv = vaultIv;
    }

    public byte[] getEncryptedPrivateKey() {
        return Base64.getDecoder().decode(encryptedPrivateKey);
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public byte[] getPublicKey() {
        return Base64.getDecoder().decode(publicKey);
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
