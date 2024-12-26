package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.UserDAO;
import com.steven.e2eechat.dto.db.UserSaltDTO;
import com.steven.e2eechat.dto.service.CryptoResult;
import com.steven.e2eechat.dto.web.LoginRequest;
import com.steven.e2eechat.dto.web.RegisterRequest;
import com.steven.e2eechat.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务
 * 处理用户注册、登录等业务逻辑
 */
public class UserService {
    private final UserDAO userDAO;
    private final CryptoService cryptoService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.cryptoService = new CryptoService();
    }

    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 如果用户名可用返回true，否则返回false
     */
    public boolean checkUsernameAvailable(String username) {
        return userDAO.checkUsernameAvailable(username);
    }

    /**
     * 用户登录
     * @param request 登录请求
     * @return 如果登录成功返回用户信息，否则返回空
     */
    public Optional<UserProfile> login(LoginRequest request) {
        // 获取用户盐值
        Optional<UserSaltDTO> saltDTO = userDAO.getUserSalt(request.getUsername());
        if (saltDTO.isEmpty()) {
            return Optional.empty();
        }

        // 计算密码哈希
        CryptoResult result = cryptoService.hashPassword(
            request.getPassword(),
            Optional.of(saltDTO.get().getPasswordSalt())
        );

        // 验证登录
        if (!userDAO.verifyLogin(saltDTO.get().getUserId(), result.hash())) {
            return Optional.empty();
        }

        // 获取用户信息
        return userDAO.getUserProfile(saltDTO.get().getUserId());
    }

    /**
     * 用户注册
     * @param request 注册请求
     * @return 如果注册成功返回用户信息，否则返回空
     */
    public Optional<UserProfile> register(RegisterRequest request) {
        // 生成密码盐值和哈希
        CryptoResult passwordResult = cryptoService.hashPassword(request.getPassword());

        // 生成保险库主密钥
        byte[] vaultMasterKey = cryptoService.generateSecureBytes(32);

        // 注册用户
        Optional<UUID> userId = userDAO.registerUser(
            request.getUsername(),
            request.getDisplayName(),
            passwordResult.hash(),
            passwordResult.random(),
            vaultMasterKey
        );

        // 获取用户信息
        return userId.flatMap(userDAO::getUserProfile);
    }
}
