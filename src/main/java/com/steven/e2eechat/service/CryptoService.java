package com.steven.e2eechat.service;

import com.steven.e2eechat.dto.service.CryptoResult;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

/**
 * 密码学服务
 * 提供密码散列和随机数生成等功能
 */
public class CryptoService {
    
    /**
     * Argon2 算法参数
     */
    private static final int ARGON2_TYPE = Argon2Parameters.ARGON2_id;  // Argon2id变体
    private static final int SALT_LENGTH = 16;       // 盐长度（字节）
    private static final int HASH_LENGTH = 32;       // 散列长度（字节）
    private static final int ITERATIONS = 3;         // 迭代次数
    private static final int MEMORY = 64 * 1024;     // 内存参数（64MB）
    private static final int PARALLELISM = 1;        // 并行度
    private static final int VERSION = Argon2Parameters.ARGON2_VERSION_13;  // Argon2 版本（0x13）

    /**
     * 计算密码散列
     * @param password 原始密码字符串
     * @param salt 可选的盐值，如果未提供则生成新的随机盐
     * @return 包含32字节散列值和16字节随机数（盐值）的结果
     * @throws IllegalArgumentException 如果参数无效
     */
    public CryptoResult hashPassword(String password, Optional<byte[]> salt) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        // 获取或生成盐值
        byte[] actualSalt = salt.map(s -> {
            if (s.length != SALT_LENGTH) {
                throw new IllegalArgumentException("Salt must be " + SALT_LENGTH + " bytes");
            }
            return Arrays.copyOf(s, s.length);
        }).orElseGet(() -> generateSecureBytes(SALT_LENGTH));

        // 创建Argon2参数
        Argon2Parameters params = new Argon2Parameters.Builder(ARGON2_TYPE)
                .withVersion(VERSION)
                .withMemoryAsKB(MEMORY)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM)
                .withSalt(actualSalt)
                .build();

        // 初始化Argon2生成器
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        // 生成散列
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);

        return new CryptoResult(hash, actualSalt);
    }

    /**
     * 计算密码散列（使用新生成的随机盐）
     * @param password 原始密码字符串
     * @return 包含32字节散列值和16字节随机数（盐值）的结果
     * @throws IllegalArgumentException 如果password为null
     */
    public CryptoResult hashPassword(String password) {
        return hashPassword(password, Optional.empty());
    }

    /**
     * 生成密码学安全的随机字节
     * @param length 需要的字节数
     * @return 指定长度的随机字节数组
     * @throws IllegalArgumentException 如果length小于1
     */
    public byte[] generateSecureBytes(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be positive");
        }

        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
