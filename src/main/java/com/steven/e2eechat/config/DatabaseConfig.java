package com.steven.e2eechat.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code DatabaseConfig} 类负责配置和管理数据库连接池。
 * <p>
 * 使用 HikariCP 作为连接池管理器，通过读取 `db.properties` 文件中的配置信息来初始化连接池。
 * 提供了获取数据库连接、检查连接池状态以及关闭连接池的方法。
 * <p>
 * 本类使用了单例模式以保证在应用程序中只有一个连接池实例。
 */
public class DatabaseConfig {
    private static final String DB_PROPERTIES_FILE = "db.properties";

    private static volatile HikariDataSource dataSource;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Object lock = new Object();

    /**
     * 获取数据库连接池的单例实例。
     * <p>
     * 如果连接池尚未初始化，则进行初始化操作，并注册 JVM 关闭钩子以确保在应用程序关闭时关闭连接池。
     * 使用双重检查锁定模式确保线程安全。
     *
     * @return {@link HikariDataSource} 数据库连接池实例，如果初始化失败则抛出 {@link RuntimeException}。
     * @throws RuntimeException 如果初始化过程中发生任何异常。
     */
    public static HikariDataSource getDataSource() {
        if (!initialized.get()) {
            synchronized (lock) {
                if (!initialized.get()) {
                    try {
                        initializeDataSource();
                        initialized.set(true);
                        // 添加 JVM 关闭钩子，在程序退出时关闭数据源
                        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConfig::closeDataSource));
                    } catch (Exception e) {
                        throw new RuntimeException("数据库连接池初始化失败", e);
                    }
                }
            }
        }
        return dataSource;
    }

    /**
     * 从连接池中获取一个数据库连接。
     * <p>
     * 每次调用此方法都会尝试从连接池中获取一个新的连接。如果连接池已关闭，则会尝试重新初始化连接池。
     *
     * @return {@link Connection} 数据库连接对象。
     * @throws SQLException 如果获取连接过程中发生任何 SQL 异常。
     */
    public static Connection getConnection() throws SQLException {
        HikariDataSource ds = getDataSource();
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            // 如果是因为连接池未初始化导致的异常，尝试重新初始化
            if (e.getCause() instanceof HikariPool.PoolInitializationException) {
                synchronized (lock) {
                    initialized.set(false);
                    closeDataSource();
                    return getDataSource().getConnection();
                }
            }
            throw e;
        }
    }

    /**
     * 检查数据库连接池是否健康可用。
     * <p>
     * 通过尝试从连接池获取一个连接并验证其是否有效来判断连接池的健康状态。
     *
     * @return {@code true} 如果连接池可用且可以成功获取连接，否则返回 {@code false}。
     */
    public static boolean isHealthy() {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 获取数据库连接池的统计信息。
     * <p>
     * 返回包含活动连接数、空闲连接数和等待连接数的格式化字符串。
     *
     * @return {@link String} 包含连接池统计信息的字符串，如果连接池未初始化则返回 "连接池未初始化"。
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "连接池未初始化";
        }
        return String.format(
                "活动连接数: %d, 空闲连接数: %d, 等待连接数: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    /**
     * 初始化数据库连接池。
     * <p>
     * 从 `db.properties` 文件加载配置，并使用这些配置创建 {@link HikariDataSource} 实例。
     * 此方法仅在连接池未被初始化时调用。
     *
     * @throws RuntimeException 如果加载配置文件或初始化连接池过程中发生任何异常。
     */
    private static void initializeDataSource() {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();

        // 设置数据库连接基础信息
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        // 设置连接池大小
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize", "10")));
        // 设置连接超时时间
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.connectionTimeout", "30000")));
        // 设置空闲连接超时时间
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.idleTimeout", "600000")));
        // 设置连接的最长生命周期
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.maxLifetime", "1800000")));

        // 设置连接池名称
        config.setPoolName("E2EEChatPool");

        // 优化配置，使用预处理语句缓存
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // 设置连接测试查询，用于在从连接池获取连接时验证连接的有效性
        config.setConnectionTestQuery("SELECT 1");

        // 设置连接初始化 SQL，例如设置时区
        config.setConnectionInitSql("SET TIME ZONE 'UTC'");

        dataSource = new HikariDataSource(config);
    }

    /**
     * 从类路径加载数据库配置文件 {@code db.properties}。
     *
     * @return {@link Properties} 包含数据库配置信息的属性对象。
     * @throws RuntimeException 如果无法找到或加载配置文件。
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("无法找到数据库配置文件: " + DB_PROPERTIES_FILE);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("加载数据库配置文件失败", e);
        }
        return props;
    }

    /**
     * 关闭数据库连接池。
     * <p>
     * 释放所有连接并关闭连接池，此操作通常在应用程序关闭时执行。
     * 使用同步块确保线程安全。
     */
    public static void closeDataSource() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                initialized.set(false);
            }
        }
    }

    /**
     * 私有构造函数，防止外部实例化 {@code DatabaseConfig} 类。
     * <p>
     * 因为这是一个工具类，不应该被实例化。
     *
     * @throws UnsupportedOperationException 当尝试实例化时抛出异常。
     */
    private DatabaseConfig() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
}
