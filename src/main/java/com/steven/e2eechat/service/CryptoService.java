package com.steven.e2eechat.service;

import com.steven.e2eechat.dto.service.CryptoResult;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

/**
 * {@code CryptoService} 提供密码散列和随机数生成等密码学相关的功能。
 * <p>
 * 使用 Argon2id 算法进行密码散列，并提供生成安全随机字节数组的功能。
 */
public class CryptoService {

    /**
     * Argon2 算法参数配置。
     * <ul>
     *     <li>{@code ARGON2_TYPE}: 使用 Argon2id 变体。</li>
     *     <li>{@code SALT_LENGTH}: 盐的长度为 16 字节。</li>
     *     <li>{@code HASH_LENGTH}: 散列值的长度为 32 字节。</li>
     *     <li>{@code ITERATIONS}: 迭代次数为 3。</li>
     *     <li>{@code MEMORY}: 内存参数为 64MB。</li>
     *     <li>{@code PARALLELISM}: 并行度为 1。</li>
     *     <li>{@code VERSION}: 使用 Argon2 版本 13。</li>
     * </ul>
     */
    private static final int ARGON2_TYPE = Argon2Parameters.ARGON2_id;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 64 * 1024;
    private static final int PARALLELISM = 1;
    private static final int VERSION = Argon2Parameters.ARGON2_VERSION_13;

    /**
     * 计算密码的 Argon2id 散列值。
     * <p>
     * 如果提供了盐值，则使用提供的盐值进行散列。否则，生成一个新的随机盐。
     *
     * @param password 原始密码字符串，不能为空。
     * @param salt     可选的盐值。如果存在，其长度必须为 {@link #SALT_LENGTH} 字节。
     * @return {@link CryptoResult} 对象，包含 32 字节的散列值和 16 字节的盐值。
     * @throws IllegalArgumentException 如果 {@code password} 为 null，或者提供的 {@code salt} 长度不正确。
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
            return Arrays.copyOf(s, s.length); // 复制以避免外部修改
        }).orElseGet(() -> generateSecureBytes(SALT_LENGTH));

        // 构建 Argon2 参数
        Argon2Parameters params = new Argon2Parameters.Builder(ARGON2_TYPE)
                .withVersion(VERSION)
                .withMemoryAsKB(MEMORY)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM)
                .withSalt(actualSalt)
                .build();

        // 初始化 Argon2 生成器
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        // 生成散列值
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);

        return new CryptoResult(hash, actualSalt);
    }

    /**
     * 计算密码的 Argon2id 散列值，并生成新的随机盐。
     *
     * @param password 原始密码字符串，不能为空。
     * @return {@link CryptoResult} 对象，包含 32 字节的散列值和 16 字节的随机盐值。
     * @throws IllegalArgumentException 如果 {@code password} 为 null。
     */
    public CryptoResult hashPassword(String password) {
        return hashPassword(password, Optional.empty());
    }

    /**
     * 生成指定长度的安全随机字节数组。
     *
     * @param length 需要生成的随机字节数组的长度，必须为正数。
     * @return 长度为 {@code length} 的安全随机字节数组。
     * @throws IllegalArgumentException 如果 {@code length} 小于 1。
     */
    public byte[] generateSecureBytes(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be positive");
        }

        byte[] bytes = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
}
