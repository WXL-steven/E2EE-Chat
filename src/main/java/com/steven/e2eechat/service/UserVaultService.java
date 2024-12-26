package com.steven.e2eechat.service;

import com.steven.e2eechat.dao.UserDAO;
import com.steven.e2eechat.model.UserVault;

import java.util.Optional;
import java.util.UUID;

/**
 * 用户保险库服务
 * 处理用户保险库相关的业务逻辑
 */
public class UserVaultService {
    private final UserDAO userDAO;

    public UserVaultService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 获取用户保险库
     * 
     * @param userId 用户ID
     * @return 如果保险库存在返回保险库对象，否则返回空
     */
    public Optional<UserVault> getVault(UUID userId) {
        return userDAO.getVault(userId);
    }

    /**
     * 创建用户保险库
     * 
     * @param userId 用户ID
     * @param vaultSalt 保险库盐值（16字节）
     * @param vaultIv 保险库初始化向量（12字节）
     * @param encryptedPrivateKey 加密的私钥（32-256字节）
     * @param publicKey 公钥（32-256字节）
     * @return 如果创建成功返回true，否则返回false
     * @throws IllegalArgumentException 如果参数长度不符合要求
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
