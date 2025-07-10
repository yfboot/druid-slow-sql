package org.example.druid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 测试控制器，用于验证数据库连接和Druid监控功能
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试数据库连接
     */
    @GetMapping("/connection")
    public String testConnection() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT 1 FROM DUAL");
            return "数据库连接成功！结果：" + result.toString();
        } catch (Exception e) {
            return "数据库连接失败：" + e.getMessage();
        }
    }

    /**
     * 测试慢SQL（最轻 - 简单聚合计算）
     */
    @GetMapping("/slow-sql")
    public String testSlowSql() {
        try {
            // 简单但数据量大的聚合计算（最轻量）
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT " +
                "  COUNT(*) as total_count, " +
                "  SUM(LEVEL) as level_sum, " +
                "  AVG(LEVEL) as level_avg " +
                "FROM DUAL " +
                "CONNECT BY LEVEL <= 800000"  // 80万行简单聚合计算
            );
            return "慢SQL执行完成！结果：" + result.toString() + " 请检查Druid监控页面的慢SQL统计。";
        } catch (Exception e) {
            return "慢SQL执行失败：" + e.getMessage();
        }
    }

    /**
     * 测试慢SQL方法2（中等 - 字符串操作）
     */
    @GetMapping("/slow-sql2")
    public String testSlowSql2() {
        try {
            // 字符串操作计算（中等耗时）
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT " +
                "  COUNT(*) as total_rows, " +
                "  SUM(LENGTH(RPAD('X', LEVEL, 'ABC'))) as string_len_sum, " +
                "  MAX(SUBSTR(TO_CHAR(LEVEL * 12345), 1, 5)) as max_substr " +
                "FROM DUAL " +
                "CONNECT BY LEVEL <= 200000 " +
                "HAVING COUNT(*) > 0"  // 20万行字符串操作
            );
            return "慢SQL方法2执行完成！结果：" + result.toString() + " 请检查Druid监控页面的慢SQL统计。";
        } catch (Exception e) {
            return "慢SQL方法2执行失败：" + e.getMessage();
        }
    }

    /**
     * 测试慢SQL方法3（最重 - 复杂数学计算）
     */
    @GetMapping("/slow-sql3")
    public String testSlowSql3() {
        try {
            // 复杂数学计算（最耗时）
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT " +
                "  COUNT(*) as total_count, " +
                "  SUM(POWER(LEVEL, 2)) as power_sum, " +
                "  AVG(SIN(LEVEL/100) * COS(LEVEL/50)) as trig_avg " +
                "FROM DUAL " +
                "CONNECT BY LEVEL <= 500000"  // 50万行复杂数学计算
            );
            return "慢SQL方法3执行完成！结果：" + result.toString() + " 请检查Druid监控页面的慢SQL统计。";
        } catch (Exception e) {
            return "慢SQL方法3执行失败：" + e.getMessage();
        }
    }

    /**
     * 测试普通SQL查询
     */
    @GetMapping("/normal-sql")
    public String testNormalSql() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT SYSDATE as current_time, USER as current_user FROM DUAL"
            );
            return "普通SQL执行成功！结果：" + result.toString();
        } catch (Exception e) {
            return "普通SQL执行失败：" + e.getMessage();
        }
    }

    /**
     * 获取数据库时间（快速查询）
     */
    @GetMapping("/db-time")
    public String getDbTime() {
        try {
            String time = jdbcTemplate.queryForObject(
                "SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL", 
                String.class
            );
            return "当前数据库时间：" + time;
        } catch (Exception e) {
            return "获取数据库时间失败：" + e.getMessage();
        }
    }
} 