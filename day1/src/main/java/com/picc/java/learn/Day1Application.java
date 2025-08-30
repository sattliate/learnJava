
package com.picc.java.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Day1 Spring Boot 主启动类
 * 
 * @author learn
 * @since 1.0.0
 * 
 * @SpringBootApplication 注解说明：
 * - @EnableAutoConfiguration: 启用Spring Boot的自动配置机制
 * - @ComponentScan: 启用组件扫描，扫描当前包及子包下的Spring组件
 * - @Configuration: 标识这是一个配置类
 */
@SpringBootApplication
public class Day1Application {

    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(Day1Application.class, args);
        
        System.out.println("==========================================");
        System.out.println("Day1 Hello World 应用启动成功！");
        System.out.println("访问地址: http://localhost:8080/day1");
        System.out.println("==========================================");
    }
}
