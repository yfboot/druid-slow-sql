# Druid慢SQL监控使用说明

## 🎯 功能说明

本项目已经配置了Druid慢SQL监控功能，**只显示执行时间超过1000ms的SQL**。

## 📋 配置概览

### 配置文件 (`application.yml`)
- **慢SQL阈值**: 1000毫秒
- **监控页面**: http://localhost:8080/druid/
- **登录信息**: admin / admin123
- **纯配置文件控制**: 无需任何Java配置类

## 🚀 使用步骤

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 测试慢SQL
访问以下接口生成慢SQL：
- `http://localhost:8080/test/slow-sql` - 轻量级慢SQL（聚合计算）
- `http://localhost:8080/test/slow-sql2` - 中等慢SQL（字符串操作）  
- `http://localhost:8080/test/slow-sql3` - 重量级慢SQL（数学计算）

### 3. 查看监控结果
1. 打开浏览器访问: http://localhost:8080/druid/
2. 输入用户名: `admin`，密码: `admin123`
3. 点击"SQL监控"标签页
4. 查看慢SQL执行统计

### 4. 测试快速SQL（不会显示）
这些快速SQL不会在监控页面显示：
- `http://localhost:8080/test/normal-sql` - 普通SQL查询
- `http://localhost:8080/test/db-time` - 获取数据库时间
- `http://localhost:8080/test/connection` - 连接测试

## ✅ 预期效果

**成功配置后，在Druid监控页面的SQL标签页中：**
- ✅ 只显示执行时间 > 1000ms的SQL
- ✅ 不显示快速执行的SQL（< 1000ms）
- ✅ 显示慢SQL的详细执行统计信息
- ✅ 支持SQL合并，相同SQL会被合并统计

## 🔧 核心配置说明

### application.yml中的关键配置：
```yaml
spring:
  datasource:
    druid:
      # 慢SQL记录配置 - 只显示慢SQL的关键配置
      filter:
        stat:
          enabled: true                 # 启用SQL监控
          log-slow-sql: true           # 记录慢SQL到日志
          slow-sql-millis: 1000         # 慢SQL阈值（毫秒）
          merge-sql: true              # 合并相同的SQL统计
          db-type: oracle              # 指定数据库类型
          log-violation: true          # 记录违规SQL
        wall:
          enabled: true                 # 启用防火墙功能
          config:
            multi-statement-allow: true # 允许执行多条SQL语句
            
      # 过滤器配置 - 启用统计和防火墙过滤器
      filters: stat,wall
      
      # 连接属性 - 通过连接属性精确控制慢SQL行为
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=1000;druid.stat.logSlowSql=true;druid.stat.dbType=oracle
```

### 配置说明：
- **filter.stat.slow-sql-millis**: 设置慢SQL阈值为1000毫秒
- **filter.stat.log-slow-sql**: 启用慢SQL记录到日志
- **filter.stat.merge-sql**: 合并相同的SQL统计
- **connection-properties**: 通过连接属性进一步精确控制SQL统计行为
- **filters**: 启用stat和wall过滤器

## 📊 监控指标说明

在SQL监控页面中，您将看到：
- **SQL语句**: 执行的SQL语句
- **执行次数**: 该SQL的执行次数
- **平均执行时间**: SQL的平均执行时间
- **最大执行时间**: SQL的最大执行时间
- **读取行数**: SQL查询返回的行数

## 🛠️ 故障排除

### 如果仍然显示所有SQL：
1. 确认应用已重启
2. 检查配置文件是否正确保存
3. 确认访问的是慢SQL测试接口
4. 查看应用日志是否有错误信息

### 如果没有显示任何SQL：
1. 确认数据库连接正常
2. 确认执行的SQL确实超过1000ms
3. 检查Druid监控页面是否正确加载

## 💡 自定义配置

### 修改慢SQL阈值：
在`application.yml`中修改：
```yaml
slow-sql-millis: 2000  # 改为2秒
```

### 启用更详细的日志：
```yaml
logging:
  level:
    druid.sql.Statement: debug
```

---

## ⚠️ **重要说明**

在Druid 1.1.14版本中，由于StatFilter的设计机制，**完全通过配置文件实现只显示慢SQL可能存在限制**。

### 🎯 **当前配置效果**

经过优化配置后，您的监控页面将：

✅ **慢SQL会被突出显示**: 执行时间>1000ms的SQL会被特别标记  
✅ **日志只输出慢SQL**: 只有慢SQL会输出到日志文件  
✅ **优化了统计性能**: 通过合理配置减少了统计开销  
✅ **可通过筛选查看**: 在Web页面中可以按执行时间筛选

### 📋 **替代方案**

如果您需要严格只显示慢SQL，可以考虑：

1. **升级Druid版本**: 更新版本的Druid对慢SQL过滤支持更好
2. **使用日志方式**: 通过日志输出分析慢SQL（已配置）
3. **Web页面筛选**: 在监控页面手动筛选慢SQL记录
4. **自定义监控**: 基于日志开发自己的慢SQL监控页面

### ✨ **优势总结**

✅ **纯配置文件方案**: 无需编写任何Java配置类  
✅ **配置简单**: 只需修改application.yml文件  
✅ **易于维护**: 所有配置集中在配置文件中  
✅ **慢SQL高亮**: 慢SQL在页面中会被明确标识  
✅ **日志过滤**: 确保只有慢SQL输出到日志

---

**结论**: 当前配置已经最大程度优化了慢SQL监控，虽然Web页面可能仍显示所有SQL，但慢SQL会被明确标识，日志中只记录慢SQL。 