package com.steven.e2eechat.config;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.logging.Logger;

@WebListener
public class DatabaseContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseContextListener.class.getName());

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 注销所有JDBC驱动程序
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
        
        // 关闭数据源
        DatabaseConfig.closeDataSource();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 在这里可以添加数据源初始化的代码，如果需要的话
    }
}
