package org.example.druid.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 前端日志请求DTO
 */
public class FrontendLogRequest {
    
    /** 日志级别：INFO, WARN, ERROR, DEBUG */
    private String level;
    
    /** 日志消息 */
    private String message;
    
    /** 模块名称或页面名称 */
    private String module;
    
    /** 用户ID或用户名 */
    private String userId;
    
    /** 错误堆栈信息（可选） */
    private String stack;
    
    /** 浏览器信息 */
    private String userAgent;
    
    /** 页面URL */
    private String url;
    
    /** 时间戳 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;
    
    /** 额外的元数据信息（JSON格式） */
    private String metadata;

    // 构造函数
    public FrontendLogRequest() {
    }

    public FrontendLogRequest(String level, String message, String module) {
        this.level = level;
        this.message = message;
        this.module = module;
        this.timestamp = new Date();
    }

    // Getter 和 Setter 方法
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "FrontendLogRequest{" +
                "level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", module='" + module + '\'' +
                ", userId='" + userId + '\'' +
                ", url='" + url + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 