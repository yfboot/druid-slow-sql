package org.example.druid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 环境配置服务
 * 读取和管理应用环境声明配置
 */
@Service
public class EnvironmentService {

    /**
     * 从application.yml中读取环境声明
     * 注意：这个配置只是声明作用，不会激活Spring Profile
     */
    @Value("${app.environment:unknown}")
    private String appEnvironment;



    /**
     * 获取当前声明的环境
     */
    public String getEnvironment() {
        return appEnvironment;
    }

    /**
     * 判断是否为开发环境
     */
    public boolean isDevelopment() {
        return "dev".equalsIgnoreCase(appEnvironment);
    }

    /**
     * 判断是否为测试环境
     */
    public boolean isTest() {
        return "test".equalsIgnoreCase(appEnvironment);
    }

    /**
     * 判断是否为生产环境
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(appEnvironment) || "production".equalsIgnoreCase(appEnvironment);
    }

    /**
     * 获取环境相关的配置信息
     */
    public EnvironmentInfo getEnvironmentInfo() {
        EnvironmentInfo info = new EnvironmentInfo();
        info.setEnvironment(appEnvironment);
        info.setDevelopment(isDevelopment());
        info.setTest(isTest());
        info.setProduction(isProduction());
        
        // 根据环境设置不同的配置
        if (isDevelopment()) {
            info.setLogLevel("DEBUG");
            info.setEnableDebugFeatures(true);
            info.setDatabasePoolSize(5);
        } else if (isTest()) {
            info.setLogLevel("INFO");
            info.setEnableDebugFeatures(false);
            info.setDatabasePoolSize(10);
        } else if (isProduction()) {
            info.setLogLevel("WARN");
            info.setEnableDebugFeatures(false);
            info.setDatabasePoolSize(20);
        } else {
            info.setLogLevel("INFO");
            info.setEnableDebugFeatures(false);
            info.setDatabasePoolSize(10);
        }
        
        return info;
    }

    /**
     * 环境信息DTO
     */
    public static class EnvironmentInfo {
        private String environment;
        private boolean development;
        private boolean test;
        private boolean production;
        private String logLevel;
        private boolean enableDebugFeatures;
        private int databasePoolSize;

        // Getter 和 Setter 方法
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public boolean isDevelopment() { return development; }
        public void setDevelopment(boolean development) { this.development = development; }

        public boolean isTest() { return test; }
        public void setTest(boolean test) { this.test = test; }

        public boolean isProduction() { return production; }
        public void setProduction(boolean production) { this.production = production; }

        public String getLogLevel() { return logLevel; }
        public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

        public boolean isEnableDebugFeatures() { return enableDebugFeatures; }
        public void setEnableDebugFeatures(boolean enableDebugFeatures) { this.enableDebugFeatures = enableDebugFeatures; }

        public int getDatabasePoolSize() { return databasePoolSize; }
        public void setDatabasePoolSize(int databasePoolSize) { this.databasePoolSize = databasePoolSize; }
    }
} 