package org.example.druid.controller;

import org.example.druid.dto.FrontendLogRequest;
import org.example.druid.service.FrontendLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端日志控制器
 * 提供前端日志记录接口，日志将保存到专门的前端日志文件中
 */
@RestController
@RequestMapping("/api/frontend-log")
@CrossOrigin(origins = "*") // 允许跨域访问
public class FrontendLogController {

    // 普通应用日志记录器
    private static final Logger logger = LoggerFactory.getLogger(FrontendLogController.class);
    
    @Autowired
    private FrontendLogService frontendLogService;

    /**
     * 记录单条前端日志
     * POST /api/frontend-log/single
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> logSingle(
            @RequestBody FrontendLogRequest logRequest,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证日志内容大小
            String validationError = validateLogRequest(logRequest);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 补充请求信息
            enrichLogRequest(logRequest, request);
            
            // 使用service记录日志
            frontendLogService.logSingle(logRequest);
            
            response.put("success", true);
            response.put("message", "日志记录成功");
            response.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("记录前端日志失败", e);
            response.put("success", false);
            response.put("message", "日志记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量记录前端日志
     * POST /api/frontend-log/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> logBatch(
            @RequestBody List<FrontendLogRequest> logRequests,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            // 验证批量日志内容
            if (logRequests.size() > 100) {
                response.put("success", false);
                response.put("message", "批量日志数量过多，最大支持100条");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证每条日志内容大小
            for (int i = 0; i < logRequests.size(); i++) {
                String validationError = validateLogRequest(logRequests.get(i));
                if (validationError != null) {
                    response.put("success", false);
                    response.put("message", "第" + (i + 1) + "条日志：" + validationError);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 补充请求信息
            for (FrontendLogRequest logRequest : logRequests) {
                enrichLogRequest(logRequest, request);
            }
            
            // 使用service批量记录日志
            FrontendLogService.BatchLogResult result = frontendLogService.logBatch(logRequests);
            
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("successCount", result.getSuccessCount());
            response.put("failureCount", result.getFailureCount());
            response.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("批量记录前端日志失败", e);
            response.put("success", false);
            response.put("message", "批量日志记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 快速记录日志（简化接口）
     * POST /api/frontend-log/quick
     */
    @PostMapping("/quick")
    public ResponseEntity<Map<String, Object>> logQuick(
            @RequestParam String level,
            @RequestParam String message,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userId,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            FrontendLogRequest logRequest = new FrontendLogRequest(level, message, module);
            logRequest.setUserId(userId);
            
            // 补充请求信息
            enrichLogRequest(logRequest, request);
            
            // 使用service记录日志
            frontendLogService.logSingle(logRequest);
            
            response.put("success", true);
            response.put("message", "快速日志记录成功");
            response.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("快速记录前端日志失败", e);
            response.put("success", false);
            response.put("message", "快速日志记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取前端日志配置信息
     * GET /api/frontend-log/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("supportedLevels", new String[]{"DEBUG", "INFO", "WARN", "ERROR"});
        config.put("logPath", "./logs/frontend/druid-slow-sql-frontend.log");
        config.put("archivePath", "./logs/frontend/archive/");
        config.put("maxFileSize", "25MB");
        config.put("maxHistory", 30);
        config.put("totalSizeCap", "800MB");
        config.put("pattern", "%d{yyyy-MM-dd HH:mm:ss.SSS} [Frontend] %-5level - %msg%n");
        config.put("fileNamingPattern", "druid-slow-sql-frontend-%d{yyyy-MM-dd}-%i.log.gz");
        config.put("compression", "gzip");
        config.put("clearOnStartup", true);
        return ResponseEntity.ok(config);
    }

    /**
     * 获取日志文件信息
     * GET /api/frontend-log/info
     */
    @GetMapping("/info")
    public ResponseEntity<FrontendLogService.LogFileInfo> getLogFileInfo() {
        try {
            FrontendLogService.LogFileInfo info = frontendLogService.getLogFileInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("获取日志文件信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取最近的日志内容
     * GET /api/frontend-log/recent?lines=50
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "50") int lines) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (lines > 500) {
                lines = 500; // 限制最大行数
            }
            
            List<String> logs = frontendLogService.getRecentLogs(lines);
            
            response.put("success", true);
            response.put("lines", lines);
            response.put("logs", logs);
            response.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取最近日志失败", e);
            response.put("success", false);
            response.put("message", "获取日志失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建日志目录（用于初始化）
     * POST /api/frontend-log/init
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initLogDirectory() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean created = frontendLogService.createLogDirectory();
            
            response.put("success", true);
            response.put("message", created ? "日志目录创建成功" : "日志目录已存在");
            response.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("创建日志目录失败", e);
            response.put("success", false);
            response.put("message", "创建日志目录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 验证日志请求内容大小
     */
    private String validateLogRequest(FrontendLogRequest logRequest) {
        // 单个字段最大长度限制（防止过大内容）
        final int MAX_MESSAGE_LENGTH = 10000;  // 消息最大10KB
        final int MAX_STACK_LENGTH = 50000;    // 堆栈最大50KB 
        final int MAX_METADATA_LENGTH = 5000;  // 元数据最大5KB
        final int MAX_URL_LENGTH = 2000;       // URL最大2KB
        
        if (logRequest.getMessage() != null && logRequest.getMessage().length() > MAX_MESSAGE_LENGTH) {
            return "日志消息过长，最大支持" + MAX_MESSAGE_LENGTH + "字符";
        }
        
        if (logRequest.getStack() != null && logRequest.getStack().length() > MAX_STACK_LENGTH) {
            return "错误堆栈过长，最大支持" + MAX_STACK_LENGTH + "字符";
        }
        
        if (logRequest.getMetadata() != null && logRequest.getMetadata().length() > MAX_METADATA_LENGTH) {
            return "元数据过长，最大支持" + MAX_METADATA_LENGTH + "字符";
        }
        
        if (logRequest.getUrl() != null && logRequest.getUrl().length() > MAX_URL_LENGTH) {
            return "URL过长，最大支持" + MAX_URL_LENGTH + "字符";
        }
        
        return null; // 验证通过
    }

    /**
     * 补充日志请求信息
     */
    private void enrichLogRequest(FrontendLogRequest logRequest, HttpServletRequest request) {
        if (logRequest.getTimestamp() == null) {
            logRequest.setTimestamp(new Date());
        }
        
        if (logRequest.getUserAgent() == null) {
            logRequest.setUserAgent(request.getHeader("User-Agent"));
        }
        
        if (logRequest.getUrl() == null) {
            String referer = request.getHeader("Referer");
            logRequest.setUrl(referer != null ? referer : "N/A");
        }
    }

} 