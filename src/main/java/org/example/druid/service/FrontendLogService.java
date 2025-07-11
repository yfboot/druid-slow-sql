package org.example.druid.service;

import org.example.druid.dto.FrontendLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 前端日志服务类
 * 提供日志记录、查询、管理等功能
 */
@Service
public class FrontendLogService {

    private static final Logger frontendLogger = LoggerFactory.getLogger("frontend.logger");
    private static final Logger logger = LoggerFactory.getLogger(FrontendLogService.class);
    
    // 异步执行器，用于批量处理日志
    private final Executor logExecutor = Executors.newFixedThreadPool(2);

    @Value("${frontend.log.enabled:true}")
    private boolean logEnabled;

    @Value("${frontend.log.max-batch-size:100}")
    private int maxBatchSize;

    /**
     * 记录单条日志
     */
    public void logSingle(FrontendLogRequest logRequest) {
        if (!logEnabled) {
            return;
        }

        try {
            String logMessage = formatLogMessage(logRequest);
            writeLogByLevel(logRequest.getLevel(), logMessage);
        } catch (Exception e) {
            logger.error("记录前端日志失败: " + logRequest, e);
        }
    }

    /**
     * 异步记录单条日志
     */
    public CompletableFuture<Void> logSingleAsync(FrontendLogRequest logRequest) {
        return CompletableFuture.runAsync(() -> logSingle(logRequest), logExecutor);
    }

    /**
     * 批量记录日志
     */
    public BatchLogResult logBatch(List<FrontendLogRequest> logRequests) {
        if (!logEnabled) {
            return new BatchLogResult(0, 0, "前端日志功能已禁用");
        }

        if (logRequests.size() > maxBatchSize) {
            return new BatchLogResult(0, logRequests.size(), 
                String.format("批量日志数量超过限制（%d），请分批处理", maxBatchSize));
        }

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (FrontendLogRequest logRequest : logRequests) {
            try {
                String logMessage = formatLogMessage(logRequest);
                writeLogByLevel(logRequest.getLevel(), logMessage);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add("日志记录失败: " + e.getMessage());
                logger.warn("批量日志中单条记录失败: " + logRequest, e);
            }
        }

        String message = String.format("批量处理完成，成功: %d, 失败: %d", successCount, failureCount);
        if (!errors.isEmpty() && errors.size() <= 5) {
            message += " 错误详情: " + String.join("; ", errors);
        }

        return new BatchLogResult(successCount, failureCount, message);
    }

    /**
     * 异步批量记录日志
     */
    public CompletableFuture<BatchLogResult> logBatchAsync(List<FrontendLogRequest> logRequests) {
        return CompletableFuture.supplyAsync(() -> logBatch(logRequests), logExecutor);
    }

    /**
     * 获取日志文件信息
     */
    public LogFileInfo getLogFileInfo() {
        LogFileInfo info = new LogFileInfo();
        
        try {
            // 主日志文件
            Path mainLogPath = Paths.get("./logs/frontend/druid-slow-sql-frontend.log");
            if (Files.exists(mainLogPath)) {
                File mainLogFile = mainLogPath.toFile();
                info.setMainLogPath(mainLogPath.toString());
                info.setMainLogSize(mainLogFile.length());
                info.setMainLogLastModified(new Date(mainLogFile.lastModified()));
            }
            
            // 统计历史日志文件
            Path logDir = Paths.get("./logs/frontend/");
            if (Files.exists(logDir)) {
                File[] logFiles = logDir.toFile().listFiles((dir, name) -> 
                    name.startsWith("druid-slow-sql-frontend") && name.endsWith(".log"));
                
                if (logFiles != null) {
                    info.setHistoryLogCount(logFiles.length);
                    
                    long totalSize = 0;
                    for (File logFile : logFiles) {
                        totalSize += logFile.length();
                    }
                    info.setTotalLogSize(totalSize);
                }
            }
            
            // 检查归档目录
            Path archiveDir = Paths.get("./logs/frontend/archive/");
            if (Files.exists(archiveDir)) {
                File[] archiveFiles = archiveDir.toFile().listFiles((dir, name) -> 
                    name.startsWith("druid-slow-sql-frontend") && name.endsWith(".log.gz"));
                
                if (archiveFiles != null) {
                    info.setHistoryLogCount(info.getHistoryLogCount() + archiveFiles.length);
                    
                    for (File archiveFile : archiveFiles) {
                        info.setTotalLogSize(info.getTotalLogSize() + archiveFile.length());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("获取日志文件信息失败", e);
        }
        
        return info;
    }

    /**
     * 读取最近的日志内容（用于调试）
     */
    public List<String> getRecentLogs(int lineCount) {
        List<String> logs = new ArrayList<>();
        
        try {
            Path logPath = Paths.get("./logs/frontend/druid-slow-sql-frontend.log");
            if (Files.exists(logPath)) {
                List<String> allLines = Files.readAllLines(logPath);
                int startIndex = Math.max(0, allLines.size() - lineCount);
                logs = allLines.subList(startIndex, allLines.size());
            } else {
                logs.add("日志文件不存在，可能还未产生日志或者系统刚启动");
            }
        } catch (IOException e) {
            logger.warn("读取日志文件失败", e);
            logs.add("读取日志失败: " + e.getMessage());
        }
        
        return logs;
    }

    /**
     * 创建日志目录
     */
    public boolean createLogDirectory() {
        try {
            Path logDir = Paths.get("./logs/frontend/");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("创建前端日志目录: " + logDir.toAbsolutePath());
                return true;
            }
            return true;
        } catch (IOException e) {
            logger.error("创建前端日志目录失败", e);
            return false;
        }
    }

    /**
     * 格式化日志消息
     */
    private String formatLogMessage(FrontendLogRequest logRequest) {
        StringBuilder sb = new StringBuilder();
        
        // 模块信息
        if (logRequest.getModule() != null) {
            sb.append("[").append(logRequest.getModule()).append("] ");
        }
        
        // 消息
        sb.append(logRequest.getMessage());
        
        // 用户信息
        if (logRequest.getUserId() != null) {
            sb.append(" | User: ").append(logRequest.getUserId());
        }
        
        // URL信息
        if (logRequest.getUrl() != null && !"N/A".equals(logRequest.getUrl())) {
            sb.append(" | URL: ").append(logRequest.getUrl());
        }
        
        // 错误堆栈
        if (logRequest.getStack() != null) {
            sb.append(" | Stack: ").append(logRequest.getStack());
        }
        
        // 元数据
        if (logRequest.getMetadata() != null) {
            sb.append(" | Metadata: ").append(logRequest.getMetadata());
        }
        
        return sb.toString();
    }

    /**
     * 根据日志级别写入日志
     */
    private void writeLogByLevel(String level, String message) {
        if (level == null) {
            level = "INFO";
        }
        
        switch (level.toUpperCase()) {
            case "DEBUG":
                frontendLogger.debug(message);
                break;
            case "INFO":
                frontendLogger.info(message);
                break;
            case "WARN":
                frontendLogger.warn(message);
                break;
            case "ERROR":
                frontendLogger.error(message);
                break;
            default:
                frontendLogger.info("[" + level + "] " + message);
                break;
        }
    }

    /**
     * 批量日志处理结果类
     */
    public static class BatchLogResult {
        private int successCount;
        private int failureCount;
        private String message;

        public BatchLogResult(int successCount, int failureCount, String message) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.message = message;
        }

        // Getter 方法
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public String getMessage() { return message; }
    }

    /**
     * 日志文件信息类
     */
    public static class LogFileInfo {
        private String mainLogPath;
        private long mainLogSize;
        private Date mainLogLastModified;
        private int historyLogCount;
        private long totalLogSize;

        // Getter 和 Setter 方法
        public String getMainLogPath() { return mainLogPath; }
        public void setMainLogPath(String mainLogPath) { this.mainLogPath = mainLogPath; }
        
        public long getMainLogSize() { return mainLogSize; }
        public void setMainLogSize(long mainLogSize) { this.mainLogSize = mainLogSize; }
        
        public Date getMainLogLastModified() { return mainLogLastModified; }
        public void setMainLogLastModified(Date mainLogLastModified) { this.mainLogLastModified = mainLogLastModified; }
        
        public int getHistoryLogCount() { return historyLogCount; }
        public void setHistoryLogCount(int historyLogCount) { this.historyLogCount = historyLogCount; }
        
        public long getTotalLogSize() { return totalLogSize; }
        public void setTotalLogSize(long totalLogSize) { this.totalLogSize = totalLogSize; }
    }
} 