package com.miya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MiyaShop应用启动类
 */
@SpringBootApplication(scanBasePackages = "com.miya")
@EnableScheduling
public class MiyaShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiyaShopApplication.class, args);
        System.out.println("========================================");
        System.out.println("MiyaShop电商系统启动成功！");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}