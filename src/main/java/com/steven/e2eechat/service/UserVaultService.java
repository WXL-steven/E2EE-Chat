package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.UserDAO;
import com.steven.e2eechat.model.UserVault;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code UserVaultService} 负责处理用户保险库相关的业务逻辑，例如获取和创建用户保险库。
 * <p>
 * 该服务依赖于 {@link UserDAO} 来进行数据库操作。
 */
public class UserVaultService {
    private final UserDAO userDAO;

    public UserVaultService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 获取指定用户的保险库信息。
     *
     * @param userId 用户ID，不能为空。
     * @return 如果用户的保险库存在，则返回包含 {@link UserVault} 对象的 {@link Optional}；否则返回空的 {@link Optional}。
     */
    public Optional<UserVault> getVault(UUID userId) {
        return userDAO.getVault(userId);
    }

    /**
     * 创建用户的保险库。
     *
     * @param userId            用户ID，不能为空。
     * @param vaultSalt         保险库盐值，长度必须为 16 字节。
     * @param vaultIv           保险库初始化向量，长度必须为 12 字节。
     * @param encryptedPrivateKey 加密的私钥，长度必须在 32 到 256 字节之间。
     * @param publicKey         公钥，长度必须在 32 到 256 字节之间。
     * @return 如果保险库创建成功，则返回 {@code true}；否则返回 {@code false}。
     * @throws IllegalArgumentException 如果任何参数的长度不符合要求。
     */
    public boolean createVault(UUID userId, byte[] vaultSalt, byte[] vaultIv,
                               byte[] encryptedPrivateKey, byte[] publicKey) {
        // 验证参数长度
        if (vaultSalt.length != 16) {
            throw new IllegalArgumentException("保险库盐值必须是16字节");
        }
        if (vaultIv.length != 12) {
            throw new IllegalArgumentException("保险库初始化向量必须是12字节");
        }
        if (encryptedPrivateKey.length < 32 || encryptedPrivateKey.length > 256) {
            throw new IllegalArgumentException("加密的私钥长度必须在32-256字节之间");
        }
        if (publicKey.length < 32 || publicKey.length > 256) {
            throw new IllegalArgumentException("公钥长度必须在32-256字节之间");
        }

        return userDAO.createVault(userId, vaultSalt, vaultIv, encryptedPrivateKey, publicKey);
    }
}
