package org.example.druid;

import org.example.druid.service.EnvironmentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DruidApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DruidApplication.class, args);

        // 启动时显示环境信息
        EnvironmentService environmentService = context.getBean(EnvironmentService.class);
        EnvironmentService.EnvironmentInfo envInfo = environmentService.getEnvironmentInfo();

        System.out.println("=====================================");
        System.out.println("📌 当前环境: " + envInfo.getEnvironment());
        System.out.println("=====================================");
    }

}
