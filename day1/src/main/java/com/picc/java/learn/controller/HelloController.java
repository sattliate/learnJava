package com.picc.java.learn.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello World 控制器
 * 演示Spring Boot Web基础功能
 * 
 * @author learn
 * @since 1.0.0
 * 
 * @RestController 注解说明：
 * - @Controller: 标识这是一个Spring MVC控制器
 * - @ResponseBody: 所有方法的返回值都会自动转换为JSON格式
 * 
 * @RequestMapping 注解说明：
 * - 为控制器类指定基础请求路径
 * - 所有方法都会继承这个路径前缀
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    /**
     * 从配置文件中注入应用名称
     */
    @Value("${app.name:Day1 Hello World}")
    private String appName;

    /**
     * 从配置文件中注入应用版本
     */
    @Value("${app.version:1.0.0}")
    private String appVersion;



    /**
     * 基础Hello接口
     * 
     * @return 欢迎信息
     * 
     * @GetMapping 注解说明：
     * - 处理HTTP GET请求
     * - 路径为 /hello (继承类级别的@RequestMapping)
     */
    @GetMapping
    public String hello() {
        return "Hello World from " + appName + " v" + appVersion + "!";
    }

    /**
     * 带参数的Hello接口
     * 
     * @param name 用户名
     * @return 个性化欢迎信息
     * 
     * @RequestParam 注解说明：
     * - 绑定请求参数到方法参数
     * - required=false: 参数可选
     * - defaultValue: 参数默认值
     */
    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Hello %s! 当前时间: %s", name, currentTime);
    }

    /**
     * 返回JSON数据的接口
     * 
     * @return 应用信息JSON对象
     */
    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("appName", appName);
        info.put("appVersion", appVersion);
        info.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("status", "running");
        info.put("message", "Day1 Spring Boot应用运行正常");
        
        return info;
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        health.put("service", appName);
        
        return health;
    }

    /**
     * POST接口 - 创建问候消息
     * 
     * @param request 请求体
     * @return 创建的问候消息
     * 
     * @PostMapping 注解说明：
     * - 处理HTTP POST请求
     * - 路径为 /hello/create
     * - consumes指定接收的Content-Type
     * - produces指定返回的Content-Type
     */
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public Map<String, Object> createGreeting(@RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "World");
        String message = request.getOrDefault("message", "Hello");
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", System.currentTimeMillis()); // 模拟ID生成
        response.put("name", name);
        response.put("message", message);
        response.put("createdTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("status", "created");
        
        return response;
    }

    /**
     * POST接口 - 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        Map<String, Object> response = new HashMap<>();

        // 简单的模拟验证逻辑
        if ("admin".equals(username) && "123456".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", "token_" + System.currentTimeMillis());

            // 修复：使用 HashMap 替代 Map.of()
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", 1);
            userInfo.put("username", username);
            userInfo.put("role", "admin");
            response.put("userInfo", userInfo);

        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            response.put("errorCode", "AUTH_FAILED");
        }
        
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }

    /**
     * POST接口 - 批量处理数据
     * 
     * @param batchRequest 批量请求
     * @return 处理结果
     */
    @PostMapping(value = "/batch", consumes = "application/json", produces = "application/json")
    public Map<String, Object> batchProcess(@RequestBody Map<String, Object> batchRequest) {
        List<String> items = (List<String>) batchRequest.getOrDefault("items", Collections.emptyList());
        String operation = (String) batchRequest.getOrDefault("operation", "process");
        
        Map<String, Object> response = new HashMap<>();
        response.put("operation", operation);
        response.put("totalItems", items.size());
        response.put("processedItems", items.size());
        response.put("success", true);
        response.put("message", String.format("成功处理 %d 个项目", items.size()));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return response;
    }
}
