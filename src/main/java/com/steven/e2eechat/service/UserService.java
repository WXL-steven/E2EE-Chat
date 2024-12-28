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
 * {@code UserService} 负责处理用户注册、登录、信息查询等与用户相关的业务逻辑。
 * <p>
 * 该服务依赖于 {@link UserDAO} 进行数据库操作，并使用 {@link CryptoService} 进行密码学相关的操作。
 */
public class UserService {
    private final UserDAO userDAO;
    private final CryptoService cryptoService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.cryptoService = new CryptoService();
    }

    /**
     * 检查给定的用户名是否在数据库中可用。
     *
     * @param username 要检查的用户名。
     * @return 如果用户名未被占用，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean checkUsernameAvailable(String username) {
        return userDAO.checkUsernameAvailable(username);
    }

    /**
     * 用户登录验证。
     * <p>
     * 根据提供的登录请求，首先获取用户的盐值，然后使用该盐值对请求中的密码进行哈希处理，
     * 最后验证哈希值是否与数据库中存储的哈希值匹配。
     *
     * @param request 包含用户名和密码的登录请求对象。
     * @return 如果登录成功，则返回包含用户信息的 {@link Optional<UserProfile>}；否则返回空的 {@link Optional}。
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
     * 用户注册。
     * <p>
     * 根据提供的注册请求，生成密码盐值和哈希值，并生成用于用户保险库的主密钥。
     * 然后将用户信息、密码哈希和盐值、保险库主密钥等信息注册到数据库中。
     *
     * @param request 包含用户名、显示名称和密码的注册请求对象。
     * @return 如果注册成功，则返回包含新用户信息 的 {@link Optional<UserProfile>}；否则返回空的 {@link Optional}。
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

    /**
     * 根据用户ID获取用户资料。
     *
     * @param userId 要获取的用户ID。
     * @return 如果用户存在，则返回包含用户资料的 {@link Optional<UserProfile>}；否则返回空的 {@link Optional}。
     */
    public Optional<UserProfile> getUserById(UUID userId) {
        return userDAO.getUserProfile(userId);
    }

    /**
     * 根据用户名获取用户UUID。
     *
     * @param username 要查找的用户名。
     * @return 如果用户存在，则返回包含用户UUID的 {@link Optional<UUID>}；否则返回空的 {@link Optional}。
     */
    public Optional<UUID> getUserUuidByUsername(String username) {
        return userDAO.getUserUuidByUsername(username);
    }

    /**
     * 刷新用户的最后在线时间。
     * <p>
     * 此方法通常由数据库存储过程自动调用，仅在特殊情况下使用。
     *
     * @param userId 要更新最后在线时间的用户ID。
     */
    public void updateLastOnline(UUID userId) {
        userDAO.updateLastOnline(userId, null);
    }
}
