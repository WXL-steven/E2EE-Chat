package com.steven.e2eechat.dto.web;

/**
 * {@code RegisterRequest} DTO 用于封装前端提交的注册表单数据。
 * <p>
 * 该 DTO 包含用户注册所需的所有信息，包括用户名、显示名称、密码（Base64 编码的哈希值）、密码盐值（Base64 编码）和保险库主密钥（Base64 编码）。
 */
public class RegisterRequest {
    /**
     * 用户名。
     * <ul>
     *     <li>长度：1-16个字符。</li>
     *     <li>字符集：仅限 ASCII 字符（字母、数字、下划线、连字符）。</li>
     *     <li>格式：满足正则表达式 <code>^[a-zA-Z0-9_-]{1,16}$</code>。</li>
     * </ul>
     */
    private String username;

    /**
     * 显示名称。
     * <ul>
     *     <li>长度：1-32个字符。</li>
     *     <li>字符集：UTF-8 编码的 Unicode 字符。</li>
     * </ul>
     */
    private String displayName;

    /**
     * 密码的哈希值（经过 Base64 编码）。
     * <ul>
     *     <li>原始哈希值长度：32字节。</li>
     *     <li>Base64 编码后长度：44字符。</li>
     * </ul>
     */
    private String password;

    /**
     * 密码盐值（经过 Base64 编码）。
     * <ul>
     *     <li>原始盐值长度：16字节。</li>
     *     <li>Base64 编码后长度：24字符。</li>
     *     <li>应为安全随机数。</li>
     * </ul>
     */
    private String passwordSalt;

    /**
     * 保险库主密钥（经过 Base64 编码）。
     * <ul>
     *     <li>原始密钥长度：32字节。</li>
     *     <li>Base64 编码后长度：44字符。</li>
     *     <li>应为安全随机数。</li>
     * </ul>
     */
    private String vaultMasterKey;

    /**
     * 获取用户名。
     *
     * @return 用户名字符串。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名字符串。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取显示名称。
     *
     * @return 显示名称字符串。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称。
     *
     * @param displayName 显示名称字符串。
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取 Base64 编码的密码哈希值。
     *
     * @return Base64 编码的密码哈希值字符串。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 Base64 编码的密码哈希值。
     *
     * @param password Base64 编码的密码哈希值字符串。
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取 Base64 编码的密码盐值。
     *
     * @return Base64 编码的密码盐值字符串。
     */
    public String getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * 设置 Base64 编码的密码盐值。
     *
     * @param passwordSalt Base64 编码的密码盐值字符串。
     */
    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
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
     * 设置 Base64 编码的保险库主密钥。
     *
     * @param vaultMasterKey Base64 编码的保险库主密钥字符串。
     */
    public void setVaultMasterKey(String vaultMasterKey) {
        this.vaultMasterKey = vaultMasterKey;
    }
}
