# Spring Boot + Druid 监控 Demo

这是一个集成了 Druid 1.1.14 数据库连接池监控功能的 Spring Boot 项目。

## 功能特性

- ✅ Druid 数据库连接池
- ✅ Druid 监控页面
- ✅ 慢SQL日志记录和监控
- ✅ Web请求监控
- ✅ Oracle 数据库支持

## 快速开始

### 1. 启动项目

```bash
mvn spring-boot:run
```

### 2. 访问测试接口

启动成功后，可以通过以下接口测试数据库连接：

- **测试数据库连接**: http://localhost:8080/test/connection
- **获取数据库时间**: http://localhost:8080/test/db-time
- **执行普通SQL**: http://localhost:8080/test/normal-sql
- **执行慢SQL测试（简单聚合 - 最轻）**: http://localhost:8080/test/slow-sql
- **执行慢SQL测试（字符串操作 - 中等）**: http://localhost:8080/test/slow-sql2
- **执行慢SQL测试（数学计算 - 最重）**: http://localhost:8080/test/slow-sql3

### 3. 访问 Druid 监控页面

**URL**: http://localhost:8080/druid/

**登录信息**:
- 用户名: `admin`
- 密码: `admin123`

### 4. 监控功能说明

#### 主要监控页面:

1. **数据源**: 查看连接池状态、配置信息
2. **SQL监控**: 查看SQL执行统计、慢SQL记录
3. **Web应用**: 查看Web请求统计
4. **Session监控**: 监控Web Session
5. **Spring监控**: 监控Spring Bean

#### 慢SQL配置:
- 慢SQL阈值: **2秒**
- 超过2秒的SQL会被记录为慢SQL
- 可以在 "SQL监控" 页面查看慢SQL统计

## 配置说明

### 数据库配置 (application.yml)

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:oracle:thin:@192.168.139.101:1521:XE
    username: sjzt
    password: oracle123
    driver-class-name: oracle.jdbc.OracleDriver
```

### Druid监控配置

```yaml
spring:
  datasource:
    druid:
      # 监控页面配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
      
      # 慢SQL配置
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 2000  # 2秒阈值
```

## 使用建议

### 测试慢SQL功能:
1. 按照耗时从轻到重的顺序测试（推荐顺序）：
   - **先试**: http://localhost:8080/test/slow-sql （简单聚合，80万行，最轻）
   - **再试**: http://localhost:8080/test/slow-sql2 （字符串操作，20万行，中等）
   - **最后**: http://localhost:8080/test/slow-sql3 （数学计算，50万行，最重）
2. 然后到 Druid 监控页面的 "SQL监控" 查看慢SQL记录
3. 查看控制台日志，也会输出慢SQL信息

### 监控Web请求:
1. 多次访问不同的测试接口
2. 到 Druid 监控页面的 "Web应用" 查看请求统计

### 查看连接池状态:
- 在 "数据源" 页面可以看到连接池的实时状态
- 包括活跃连接数、空闲连接数等信息

## 注意事项

1. **Oracle驱动**: 项目使用 `ojdbc6` 驱动，适用于 Oracle 11g
2. **慢SQL测试**: 使用Oracle标准的CONNECT BY语法和聚合函数来模拟慢查询，完全兼容Oracle数据库
3. **监控安全**: 生产环境建议修改默认的监控页面用户名密码
4. **日志级别**: 开发环境开启了 debug 级别的 Druid 日志，生产环境建议调整

## TODO 功能清单

### 📋 待实现功能
- [ ] **慢SQL数据库存储功能**
  - 创建慢SQL日志表 (slow_sql_log)
  - 实现SlowSqlLog实体类
  - 实现SlowSqlLogService服务类
  - 创建自定义Druid过滤器捕获慢SQL
  - 提供慢SQL查询和统计接口
  - 支持慢SQL记录的清理和维护

- [ ] **监控功能增强**
  - 添加慢SQL统计图表
  - 支持自定义告警阈值
  - 实现邮件/短信告警机制

- [ ] **性能优化**
  - 异步保存慢SQL记录
  - 批量插入优化
  - 分库分表支持

## 项目结构

```
src/main/java/org/example/druid/
├── DruidApplication.java          # 启动类
└── controller/
    └── TestController.java       # 测试控制器

src/main/resources/
└── application.yml               # 应用配置文件（包含所有Druid配置）
``` 