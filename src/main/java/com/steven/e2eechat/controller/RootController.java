package com.steven.e2eechat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 根控制器
 * 处理发送到根路径的请求
 */
@WebServlet(name = "RootController", urlPatterns = {"", "/"})
public class RootController extends HttpServlet {
    
    /**
     * 处理GET请求
     * 将请求重定向到/account路径
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/account");
    }

    /**
     * 处理POST请求
     * 将请求重定向到/account路径
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/account");
    }
}
