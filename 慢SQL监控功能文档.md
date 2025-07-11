# 慢SQL监控功能文档

## 🎯 功能概述

慢SQL监控功能基于Druid连接池的StatFilter，自动识别和记录执行时间超过设定阈值的SQL语句，提供完整的监控和统计功能。

### 核心特性
- **自动识别慢SQL** - 执行时间超过1000ms的SQL自动标记
- **Web监控界面** - 图形化的SQL执行统计和详情查看
- **日志文件记录** - 慢SQL详情写入专用日志文件
- **SQL统计合并** - 相同SQL语句的执行统计自动合并
- **实时监控** - 无需重启即可查看最新的SQL执行情况

## 📊 监控配置

### 慢SQL阈值设置
- **默认阈值**: 1000毫秒（1秒）
- **监控范围**: 所有通过Druid连接池执行的SQL
- **记录策略**: 只记录和突出显示慢SQL，快速SQL不会被特别标记

### 配置文件设置 (application.yml)
```yaml
spring:
  datasource:
    druid:
      # 慢SQL监控配置
      filter:
        stat:
          enabled: true                 # 启用SQL监控
          log-slow-sql: true           # 记录慢SQL到日志
          slow-sql-millis: 1000         # 慢SQL阈值（毫秒）
          merge-sql: true              # 合并相同的SQL统计
          db-type: oracle              # 数据库类型
        wall:
          enabled: true                 # 启用SQL防火墙
          config:
            multi-statement-allow: true # 允许多语句执行
            
      # 启用过滤器
      filters: stat,wall
      
      # 连接属性配置
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=1000;druid.stat.logSlowSql=true;druid.stat.dbType=oracle

# 日志输出配置
logging:
  level:
    druid.sql.Statement: debug         # 慢SQL日志级别
```

## 🌐 监控页面使用

### 访问监控界面
- **URL**: http://localhost:8080/druid/
- **用户名**: `admin`
- **密码**: `admin123`

### 监控页面功能
1. **数据源页面** - 查看连接池状态、配置信息
2. **SQL监控页面** - 重点功能，查看SQL执行统计
3. **Web应用页面** - HTTP请求统计信息
4. **Session监控** - Web Session管理信息
5. **Spring监控** - Spring Bean相关信息

### SQL监控页面说明
在"SQL监控"标签页中可以查看：
- **SQL语句** - 执行的完整SQL语句
- **执行次数** - 该SQL的总执行次数
- **平均执行时间** - SQL的平均执行耗时
- **最大执行时间** - SQL的最大执行耗时
- **读取行数** - SQL查询返回的数据行数
- **慢SQL标记** - 超过阈值的SQL会被特别标识

## 🧪 测试慢SQL功能

### 慢SQL测试接口
项目提供以下测试接口用于生成不同程度的慢SQL：

| 接口 | 完整URL | 执行时间 | SQL类型 | 说明 |
|------|---------|----------|---------|------|
| `/test/slow-sql` | http://localhost:8080/test/slow-sql | 1-3秒 | 聚合计算 | 轻量级慢SQL，80万行聚合计算 |
| `/test/slow-sql2` | http://localhost:8080/test/slow-sql2 | 5-8秒 | 字符串操作 | 中等慢SQL，20万行字符串处理 |
| `/test/slow-sql3` | http://localhost:8080/test/slow-sql3 | 10+秒 | 数学计算 | 重量级慢SQL，50万行复杂数学运算 |

### 对比测试接口（快速SQL）
以下接口执行快速SQL，不会在监控页面被特别标记：

| 接口 | 完整URL | 功能说明 |
|------|---------|----------|
| `/test/normal-sql` | http://localhost:8080/test/normal-sql | 普通SELECT查询，获取当前时间和用户 |
| `/test/db-time` | http://localhost:8080/test/db-time | 获取数据库当前时间 |
| `/test/connection` | http://localhost:8080/test/connection | 简单连接测试，验证数据库可用性 |



### 测试步骤
1. **启动应用**: `mvn spring-boot:run`
2. **测试连接**: 首先访问 http://localhost:8080/test/connection 确认数据库连接正常
3. **执行快速SQL**: 访问 http://localhost:8080/test/normal-sql 或 http://localhost:8080/test/db-time 测试正常SQL
4. **执行慢SQL**: 按耗时递增顺序测试
   - http://localhost:8080/test/slow-sql （轻量级，1-3秒）
   - http://localhost:8080/test/slow-sql2 （中等级，5-8秒）
   - http://localhost:8080/test/slow-sql3 （重量级，10+秒）
5. **查看监控**: 打开 http://localhost:8080/druid/
6. **登录系统**: 输入用户名 `admin`，密码 `admin123`
7. **查看结果**: 点击"SQL监控"标签查看慢SQL统计

### 推荐测试方案

#### 基础测试（建议顺序）
1. **连接测试**: http://localhost:8080/test/connection
2. **快速SQL**: http://localhost:8080/test/db-time
3. **轻量慢SQL**: http://localhost:8080/test/slow-sql
4. **查看监控页面**: http://localhost:8080/druid/ 确认慢SQL被记录

#### 完整测试（性能评估）
1. 执行所有快速SQL接口（不会在监控页面特别显示）
2. 按耗时递增执行所有慢SQL接口
3. 在监控页面观察SQL执行统计
4. 检查日志文件 `./logs/druid-slow-sql-slow-sql.log`



## 📁 日志文件记录

### 日志文件位置
```
logs/
├── druid-slow-sql-slow-sql.log      # 慢SQL专用日志
└── archive/                         # 归档目录
    └── druid-slow-sql-slow-sql-2024-01-20-1.log.gz
```

### 日志文件配置
- **文件大小**: 30MB（达到后自动滚动）
- **保留天数**: 45天
- **压缩方式**: gzip压缩归档
- **启动行为**: 系统启动时清空当前日志文件

### 日志内容示例
```
2024-01-20 15:30:45.123 DEBUG [main] druid.sql.Statement - {conn-10001, pstmt-20001} executed. 2.345 seconds. 
SELECT COUNT(*) FROM (
    SELECT LEVEL FROM DUAL CONNECT BY LEVEL <= 1000000
) WHERE MOD(LEVEL, 2) = 0
```

## ⚙️ 高级配置

### 自定义慢SQL阈值
修改 `application.yml` 中的阈值设置：
```yaml
spring:
  datasource:
    druid:
      filter:
        stat:
          slow-sql-millis: 2000  # 改为2秒阈值
```

### 启用更详细的日志
```yaml
logging:
  level:
    druid.sql.Statement: debug          # 慢SQL详细日志
    druid.sql.DataSource: debug         # 数据源日志
    druid.sql.Connection: debug         # 连接日志
```

### 监控页面配置
```yaml
spring:
  datasource:
    druid:
      stat-view-servlet:
        enabled: true                   # 启用监控页面
        url-pattern: /druid/*          # 访问路径
        login-username: admin          # 用户名
        login-password: admin123       # 密码
        reset-enable: false            # 禁用重置功能
```

## 📈 监控指标说明

### 关键指标
- **执行次数** - SQL语句的总执行次数
- **平均时间** - 所有执行的平均耗时
- **最大时间** - 单次执行的最大耗时
- **最小时间** - 单次执行的最小耗时
- **总时间** - 所有执行的累计耗时
- **错误次数** - 执行失败的次数

### 性能分析
- **慢SQL比例** - 慢SQL占总SQL执行的比例
- **平均执行时间趋势** - 识别性能变化
- **频繁慢SQL** - 执行次数多且耗时长的SQL
- **异常SQL** - 错误率高的SQL语句

## 🔧 故障排除

### 慢SQL不显示的问题
1. **确认阈值设置** - 检查 `slow-sql-millis` 配置
2. **验证SQL执行时间** - 确保测试SQL确实超过阈值
3. **检查过滤器配置** - 确认 `filters` 包含 `stat`
4. **查看应用日志** - 检查是否有配置错误

### 监控页面访问问题
1. **检查端口** - 确认应用运行在8080端口
2. **验证路径** - 确认访问 `/druid/` 路径
3. **检查认证** - 使用正确的用户名密码
4. **查看防火墙** - 确认网络访问正常

### 日志文件问题
1. **检查目录权限** - 确保应用有写入权限
2. **验证磁盘空间** - 确认有足够的存储空间
3. **查看日志配置** - 检查 `logback-spring.xml` 配置

## 🎯 最佳实践

### 生产环境配置
1. **修改默认密码** - 更改监控页面的默认用户名密码
2. **设置合理阈值** - 根据业务需求调整慢SQL阈值
3. **控制访问权限** - 限制监控页面的访问IP范围
4. **定期清理日志** - 监控日志文件的磁盘占用

### 性能优化建议
1. **关注慢SQL Top列表** - 优先优化执行频率高的慢SQL
2. **分析执行计划** - 使用数据库工具分析SQL执行计划
3. **添加索引** - 根据慢SQL的WHERE条件添加合适索引
4. **SQL重构** - 重写性能差的SQL语句

### 监控告警
1. **设置阈值监控** - 当慢SQL数量或比例超过阈值时告警
2. **关键SQL监控** - 重点监控核心业务SQL的性能
3. **趋势分析** - 定期分析SQL性能变化趋势
4. **容量规划** - 基于SQL性能数据进行容量规划

## ⚠️ 注意事项

1. **性能影响** - SQL监控会对数据库性能产生轻微影响
2. **存储空间** - 监控数据和日志文件会占用磁盘空间
3. **版本兼容** - 确保Druid版本与数据库驱动版本兼容
4. **安全考虑** - 监控页面包含敏感信息，需要适当的访问控制
5. **网络配置** - 确保监控端口的网络访问策略正确配置 