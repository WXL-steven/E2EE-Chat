package com.steven.e2eechat.dto.web;

/**
 * 注册请求DTO
 * 用于封装前端提交的注册表单数据
 */
public class RegisterRequest {
    /**
     * 用户名
     * 限制：
     * - 长度：1-16个字符
     * - 字符集：仅ASCII字符（字母、数字、下划线、连字符）
     * - 格式：^[a-zA-Z0-9_-]{1,16}$
     */
    private String username;

    /**
     * 显示名称
     * 限制：
     * - 长度：1-32个字符
     * - 字符集：UTF-8编码的Unicode字符
     */
    private String displayName;

    /**
     * 密码
     * - 固定长度：32字节
     * - Base64编码：44字符
     */
    private String password;

    /**
     * 密码盐值
     * - 固定长度：16字节
     * - Base64编码：24字符
     * - 安全随机数
     */
    private String passwordSalt;

    /**
     * 保险库主密钥
     * - 固定长度：32字节
     * - Base64编码：44字符
     * - 安全随机数
     */
    private String vaultMasterKey;

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getVaultMasterKey() {
        return vaultMasterKey;
    }

    public void setVaultMasterKey(String vaultMasterKey) {
        this.vaultMasterKey = vaultMasterKey;
    }
}
