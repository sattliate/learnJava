package com.picc.java.learn;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Day1 Spring Boot 应用测试类
 * 
 * @author learn
 * @since 1.0.0
 * 
 * @SpringBootTest 注解说明：
 * - 标识这是一个Spring Boot测试类
 * - 会加载完整的Spring应用上下文
 * - 可以注入Spring容器中的Bean进行测试
 * 
 * @ActiveProfiles 注解说明：
 * - 指定测试时使用的配置文件profile
 * - 这里使用dev配置文件
 */
@SpringBootTest
@ActiveProfiles("dev")
class Day1ApplicationTests {

    /**
     * 测试Spring应用上下文是否正常加载
     * 这是一个基础的集成测试
     */
    @Test
    void contextLoads() {
        // 如果Spring应用上下文能够正常加载，说明配置正确
        // 这个测试方法不需要任何断言，Spring Boot会自动验证
        System.out.println("Spring应用上下文加载成功！");
    }

    /**
     * 测试应用基本信息
     */
    @Test
    void testAppInfo() {
        System.out.println("Day1 Hello World 应用测试开始");
        System.out.println("测试环境: dev");
        System.out.println("测试时间: " + java.time.LocalDateTime.now());
    }
}
