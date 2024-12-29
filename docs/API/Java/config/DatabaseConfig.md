---
title: 配置类 - DatabaseConfig
version: 1.0
last_updated: 2024-12-29
---

# 配置类 - DatabaseConfig

**概述:**

`DatabaseConfig` 类负责配置和管理应用程序的数据库连接池。它使用 [HikariCP](https://github.com/brettwooldridge/HikariCP)
作为连接池管理器，并通过读取 `db.properties`
文件中的配置信息来初始化连接池。此类实现了单例模式，确保在应用程序中只有一个数据库连接池实例。它提供了获取数据库连接、检查连接池状态以及关闭连接池的方法。

## 配置项

### 数据库 URL

**类型:** `String`
**默认值:**  无
**描述:**  数据库的 JDBC URL，用于建立数据库连接。此配置项从 `db.properties` 文件中的 `db.url` 属性读取。

**技术细节:** 对应 HikariConfig 的 `jdbcUrl` 属性。

### 数据库用户名

**类型:** `String`
**默认值:**  无
**描述:**  连接数据库所需的用户名。此配置项从 `db.properties` 文件中的 `db.username` 属性读取。

**技术细节:** 对应 HikariConfig 的 `username` 属性。

### 数据库密码

**类型:** `String`
**默认值:**  无
**描述:**  连接数据库所需的密码。此配置项从 `db.properties` 文件中的 `db.password` 属性读取。

**技术细节:** 对应 HikariConfig 的 `password` 属性。

### 连接池大小

**类型:** `Integer`
**默认值:** `10`
**描述:**  连接池中维护的最大连接数。此配置项从 `db.properties` 文件中的 `db.poolSize` 属性读取，如果未配置则默认为 10。

**技术细节:** 对应 HikariConfig 的 `maximumPoolSize` 属性。

### 连接超时时间

**类型:** `Long`
**默认值:** `30000` (毫秒)
**描述:**  客户端尝试从连接池获取连接的最大等待时间（毫秒）。如果超过此时间仍未获取到连接，则会抛出异常。此配置项从
`db.properties` 文件中的 `db.connectionTimeout` 属性读取，如果未配置则默认为 30000 毫秒。

**技术细节:** 对应 HikariConfig 的 `connectionTimeout` 属性。

### 空闲连接超时时间

**类型:** `Long`
**默认值:** `600000` (毫秒)
**描述:**  连接在连接池中保持空闲状态且被回收前的最大时间（毫秒）。设置为 0 表示永远不会超时。此配置项从 `db.properties`
文件中的 `db.idleTimeout` 属性读取，如果未配置则默认为 600000 毫秒。

**技术细节:** 对应 HikariConfig 的 `idleTimeout` 属性。

### 连接的最大生命周期

**类型:** `Long`
**默认值:** `1800000` (毫秒)
**描述:**  连接在连接池中存在的最长时间（毫秒），无论是否空闲。超过此时间，连接将被关闭并从池中移除。此配置项从
`db.properties` 文件中的 `db.maxLifetime` 属性读取，如果未配置则默认为 1800000 毫秒。

**技术细节:** 对应 HikariConfig 的 `maxLifetime` 属性。

### 驱动类名

**类型:** `String`
**默认值:** `org.postgresql.Driver`
**描述:**  用于连接 PostgreSQL 数据库的 JDBC 驱动类的名称。

**技术细节:** 对应 HikariConfig 的 `driverClassName` 属性。

### 连接池名称

**类型:** `String`
**默认值:** `E2EEChatPool`
**描述:**  连接池的名称，用于监控和日志记录。

**技术细节:** 对应 HikariConfig 的 `poolName` 属性。

### 缓存预处理语句

**类型:** `Boolean`
**默认值:** `true`
**描述:**  是否开启预处理语句缓存以提高性能。

**技术细节:**  通过 `addDataSourceProperty("cachePrepStmts", "true")` 设置。

### 预处理语句缓存大小

**类型:** `Integer`
**默认值:** `250`
**描述:**  缓存的预处理语句的最大数量。

**技术细节:**  通过 `addDataSourceProperty("prepStmtCacheSize", "250")` 设置。

### 预处理语句缓存的 SQL 长度限制

**类型:** `Integer`
**默认值:** `2048`
**描述:**  可以缓存的预处理语句的最大 SQL 长度。

**技术细节:**  通过 `addDataSourceProperty("prepStmtCacheSqlLimit", "2048")` 设置。

### 连接测试查询

**类型:** `String`
**默认值:** `SELECT 1`
**描述:**  用于在从连接池获取连接时验证连接是否有效的 SQL 查询。

**技术细节:** 对应 HikariConfig 的 `connectionTestQuery` 属性。

### 连接初始化 SQL

**类型:** `String`
**默认值:** `SET TIME ZONE 'UTC'`
**描述:**  在建立新连接后立即执行的 SQL 语句，例如设置会话时区。

**技术细节:** 对应 HikariConfig 的 `connectionInitSql` 属性。

## 方法

### getDataSource

**描述:**  获取数据库连接池的单例实例。如果连接池尚未初始化，则进行初始化。

**返回值:**

* `返回类型`: `HikariDataSource`
* `描述`:  数据库连接池实例。如果初始化失败，则会抛出 `RuntimeException`。

### getConnection

**描述:**  从连接池中获取一个数据库连接。

**返回值:**

* `返回类型`: `Connection`
* `描述`:  数据库连接对象。如果获取连接过程中发生任何 SQL 异常，则会抛出 `SQLException`。

### isHealthy

**描述:**  检查数据库连接池是否健康可用。

**返回值:**

* `返回类型`: `boolean`
* `描述`:  如果连接池可用且可以成功获取连接，则返回 `true`，否则返回 `false`。

### getPoolStats

**描述:**  获取数据库连接池的统计信息。

**返回值:**

* `返回类型`: `String`
* `描述`:  包含活动连接数、空闲连接数和等待连接数的格式化字符串。如果连接池未初始化，则返回 "连接池未初始化"。

### closeDataSource

**描述:**  关闭数据库连接池，释放所有连接。此操作通常在应用程序关闭时执行。

**返回值:**

* `返回类型`: `void`
* `描述`:  无返回值。

**代码示例:**

```java
// 获取 DataSource 实例
HikariDataSource dataSource = DatabaseConfig.getDataSource();

// 获取数据库连接
try (Connection connection = DatabaseConfig.getConnection()) {
    // 执行数据库操作
} catch (SQLException e) {
    e.printStackTrace();
}
