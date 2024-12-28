package com.steven.e2eechat.dto.web;

/**
 * {@code LoginRequest} DTO 用于封装前端提交的登录表单数据。
 * <p>
 * 该 DTO 包含用户登录所需的用户名和密码。
 */
public class LoginRequest {
    /**
     * 用户名。
     * <ul>
     *     <li>长度：1-16个字符</li>
     *     <li>字符集：仅限 ASCII 字符（字母、数字、下划线、连字符）。</li>
     *     <li>格式：满足正则表达式 <code>^[a-zA-Z0-9_-]{1,16}$</code>。</li>
     * </ul>
     */
    private String username;

    /**
     * 密码。
     * <ul>
     *     <li>长度：8-64个字符。</li>
     * </ul>
     */
    private String password;

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
     * 获取密码。
     *
     * @return 密码字符串。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码字符串。
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
