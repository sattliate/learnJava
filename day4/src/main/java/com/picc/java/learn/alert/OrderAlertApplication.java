package com.picc.java.learn.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 订单预警模块主应用类
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@SpringBootApplication
@EnableAsync // 启用异步处理
public class OrderAlertApplication {

    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderAlertApplication.class, args);
        // 使用log打印
        System.out.println("🚀 订单预警模块启动成功！");
        System.out.println("📊 监控维度：会员手机号 + 省份 + 渠道");
        System.out.println("⏰ 时间窗口：12小时");
        System.out.println("🔔 预警功能：订单数量阈值监控");
    }
}
