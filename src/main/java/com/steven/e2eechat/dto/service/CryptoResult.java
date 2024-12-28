package com.steven.e2eechat.dto.service;

import java.util.Arrays;

/**
 * {@code CryptoResult} 记录用于封装密码学操作的返回值，并确保数据的不可变性。
 * <p>
 * 它包含一个散列值和一个安全随机数，常用于密码哈希和密钥派生等场景。
 * 通过使用 record，自动提供了构造方法、getter 方法、{@code equals()}、{@code hashCode()} 和 {@code toString()} 方法。
 */
public record CryptoResult(
        /**
         * 散列值。
         * <ul>
         *     <li>固定长度：32字节</li>
         *     <li>通常使用 SHA-256 算法生成。</li>
         *     <li>可用作密码哈希或密钥派生的输入。</li>
         * </ul>
         */
        byte[] hash,

        /**
         * 安全随机数。
         * <ul>
         *     <li>固定长度：16字节</li>
         *     <li>使用安全随机数生成器生成。</li>
         *     <li>可用作密码盐值或其他密码学操作的随机源。</li>
         * </ul>
         */
        byte[] random
) {
    /**
     * {@code CryptoResult} 的构造方法。
     * <p>
     * 验证传入的 {@code hash} 和 {@code random} 参数是否为 {@code null}，如果为 {@code null} 则抛出 {@link IllegalArgumentException}。
     * 为了确保记录的不可变性，会对传入的字节数组进行复制。
     *
     * @param hash   散列值，不能为空。
     * @param random 安全随机数，不能为空。
     * @throws IllegalArgumentException 如果 {@code hash} 或 {@code random} 为 {@code null}。
     */
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

    /**
     * 重写 {@code equals()} 方法以比较 {@code CryptoResult} 对象的字节数组内容。
     *
     * @param obj 要与之比较的对象。
     * @return 如果给定的对象是 {@code CryptoResult} 的实例且其 {@code hash} 和 {@code random} 字节数组与当前对象的对应数组相等，则返回 {@code true}；否则返回 {@code false}。
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CryptoResult other)) return false;
        return Arrays.equals(hash, other.hash) && Arrays.equals(random, other.random);
    }

    /**
     * 重写 {@code hashCode()} 方法以基于 {@code hash} 和 {@code random} 字节数组的内容生成哈希码。
     *
     * @return 基于对象内容的哈希码。
     */
    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(hash) + Arrays.hashCode(random);
    }
}
