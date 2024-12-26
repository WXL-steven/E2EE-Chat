package com.steven.e2eechat.dto.web;

/**
 * 登录请求DTO
 * 用于封装前端提交的登录表单数据
 */
public class LoginRequest {
    /**
     * 用户名
     * 限制：
     * - 长度：1-16个字符
     * - 字符集：仅ASCII字符（字母、数字、下划线、连字符）
     * - 格式：^[a-zA-Z0-9_-]{1,16}$
     */
    private String username;

    /**
     * 密码
     * - 长度：8-64个字符
     */
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
