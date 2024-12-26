package com.steven.e2eechat.dto.service;

import java.util.Arrays;

/**
 * 密码学操作结果DTO
 * 用于封装密码学操作的返回值，确保数据不可变性
 */
public record CryptoResult(
        /**
         * 散列值
         * - 固定长度：32字节
         * - 使用SHA-256算法生成
         * - 可用作密码哈希或密钥派生的输入
         */
        byte[] hash,

        /**
         * 安全随机数
         * - 固定长度：16字节
         * - 使用安全随机数生成器生成
         * - 可用作密码盐值或其他密码学操作
         */
        byte[] random
) {
    public CryptoResult {
        // 验证参数并创建副本以确保不可变性
        if (hash == null) {
            throw new IllegalArgumentException("Hash cannot be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        hash = Arrays.copyOf(hash, hash.length);
        random = Arrays.copyOf(random, random.length);
    }

    // 重写这些方法以处理字节数组
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CryptoResult other)) return false;
        return Arrays.equals(hash, other.hash) && Arrays.equals(random, other.random);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(hash) + Arrays.hashCode(random);
    }
}
