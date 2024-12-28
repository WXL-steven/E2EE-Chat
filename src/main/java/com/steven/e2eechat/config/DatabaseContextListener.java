package com.steven.e2eechat.config;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * {@code DatabaseContextListener} 监听 ServletContext 的生命周期事件。
 * <p>
 * 在 ServletContext 销毁时，它负责注销已注册的 JDBC 驱动程序并关闭数据库连接池，
 * 以避免内存泄漏和资源未释放的问题。
 * 在 ServletContext 初始化时，目前没有执行任何操作，但可以用于添加初始化逻辑。
 */
@WebListener
public class DatabaseContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseContextListener.class.getName());

    /**
     * 在 ServletContext 销毁时被调用。
     * <p>
     * 此方法执行以下操作：
     * 1. 遍历并注销所有已注册的 JDBC 驱动程序，防止可能的内存泄漏。
     * 2. 调用 {@link DatabaseConfig#closeDataSource()} 关闭数据库连接池，释放数据库连接等资源。
     *
     * @param sce {@link ServletContextEvent} 对象，包含有关 ServletContext 的信息。
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 注销所有已注册的 JDBC 驱动程序
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            try {
                Driver driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);
                logger.info("注销JDBC驱动程序: " + driver);
            } catch (Exception e) {
                logger.warning("注销JDBC驱动程序时发生错误: " + e.getMessage());
            }
        }

        // 关闭数据库连接池
        DatabaseConfig.closeDataSource();
    }

    /**
     * 在 ServletContext 初始化时被调用。
     * <p>
     * 目前此方法为空，可以根据需要在应用程序启动时执行一些初始化操作，
     * 例如，初始化某些全局配置或执行必要的检查。
     *
     * @param sce {@link ServletContextEvent} 对象，包含有关 ServletContext 的信息。
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 在此处添加应用程序启动时需要执行的初始化操作
    }
}
