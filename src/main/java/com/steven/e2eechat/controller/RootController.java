package com.steven.e2eechat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * {@code RootController} 是应用的根控制器，负责处理发送到根路径 ("/" 或 "") 的 HTTP 请求。
 * <p>
 * 它会将所有请求重定向到 "/account" 路径。
 */
@WebServlet(name = "RootController", urlPatterns = {"", "/"})
public class RootController extends HttpServlet {

    /**
     * 处理 HTTP GET 请求。
     * <p>
     * 此方法接收 GET 请求并将其重定向到 "/account" 路径。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向时发生 I/O 异常。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/account");
    }

    /**
     * 处理 HTTP POST 请求。
     * <p>
     * 此方法接收 POST 请求并将其重定向到 "/account" 路径。
     *
     * @param request  客户端发送的 {@link HttpServletRequest} 对象。
     * @param response 服务器发送的 {@link HttpServletResponse} 对象。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在执行重定向时发生 I/O 异常。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/account");
    }
}