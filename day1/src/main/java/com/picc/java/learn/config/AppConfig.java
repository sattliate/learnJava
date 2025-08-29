package com.picc.java.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 应用配置类
 * 演示Spring Boot配置管理功能
 * 
 * @author learn
 * @since 1.0.0
 * 
 * @Configuration 注解说明：
 * - 标识这是一个Spring配置类
 * - 相当于XML配置文件中的<beans>标签
 * - 类中的@Bean方法会被Spring容器管理
 */
@Configuration
public class AppConfig {

    /**
     * 配置CORS跨域支持
     * 
     * @return WebMvcConfigurer配置器
     * 
     * @Bean 注解说明：
     * - 标识这个方法返回的对象会被Spring容器管理
     * - 方法名作为Bean的名称
     * - 可以在其他类中通过@Autowired注入
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }

    /**
     * 创建日期时间格式化器Bean
     * 
     * @return DateTimeFormatter实例
     */
    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 创建应用启动时间Bean
     * 
     * @return 应用启动时间
     */
    @Bean
    public LocalDateTime appStartTime() {
        return LocalDateTime.now();
    }
}

/**
 * 应用属性配置类
 * 绑定配置文件中的app.*属性
 * 
 * @ConfigurationProperties 注解说明：
 * - 自动绑定配置文件中的属性到Java对象
 * - prefix指定属性前缀
 * - 需要配合@EnableConfigurationProperties使用
 */
@ConfigurationProperties(prefix = "app")
class AppProperties {
    
    private String name;
    private String version;
    private String description;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
