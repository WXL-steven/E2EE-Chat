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
 * 数据库配置类
 * 使用HikariCP连接池管理数据库连接
 */
public class DatabaseConfig {
    private static final String DB_PROPERTIES_FILE = "db.properties";
    
    private static volatile HikariDataSource dataSource;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Object lock = new Object();

    /**
     * 初始化数据库连接池
     * 从db.properties文件读取配置信息并创建连接池
     *
     * @return HikariDataSource 数据库连接池实例
     * @throws RuntimeException 如果初始化失败
     */
    public static HikariDataSource getDataSource() {
        if (!initialized.get()) {
            synchronized (lock) {
                if (!initialized.get()) {
                    try {
                        initializeDataSource();
                        initialized.set(true);
                        // 添加JVM关闭钩子
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
     * 获取数据库连接
     * 
     * @return Connection 数据库连接
     * @throws SQLException 如果获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        HikariDataSource ds = getDataSource();
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            // 如果连接池已关闭，尝试重新初始化
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
     * 检查连接池状态
     *
     * @return boolean 如果连接池正常运行返回true
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
     * 获取连接池状态信息
     *
     * @return String 连接池状态的详细信息
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

    private static void initializeDataSource() {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        
        // 设置基本连接属性
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        
        // 设置连接池属性
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize", "10")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.maxLifetime", "1800000")));
        
        // 设置连接池名称
        config.setPoolName("E2EEChatPool");
        
        // 其他优化配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // 设置连接测试查询
        config.setConnectionTestQuery("SELECT 1");
        
        // 设置连接初始化SQL（设置时区等）
        config.setConnectionInitSql("SET TIME ZONE 'UTC'");
        
        dataSource = new HikariDataSource(config);
    }

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
     * 关闭数据库连接池
     * 在应用程序关闭时自动调用
     */
    public static void closeDataSource() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                initialized.set(false);
            }
        }
    }

    // 私有构造函数防止实例化
    private DatabaseConfig() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
}
