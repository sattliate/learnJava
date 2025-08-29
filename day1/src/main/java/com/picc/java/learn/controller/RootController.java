package com.picc.java.learn.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 根路径控制器
 * 处理应用根路径的访问请求
 * 
 * @author learn
 * @since 1.0.0
 */
@RestController
public class RootController {

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
     * 根路径映射 - 直接访问 /day1 时显示欢迎页面
     * 
     * @return 欢迎页面HTML
     */
    @GetMapping("/")
    public String root() {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<title>Day1 Hello World</title>" +
               "<meta charset='UTF-8'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }" +
               ".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               "h1 { color: #333; text-align: center; }" +
               ".api-list { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }" +
               ".api-item { margin: 10px 0; padding: 10px; background: white; border-left: 4px solid #007bff; }" +
               ".method { font-weight: bold; color: #007bff; }" +
               ".url { font-family: monospace; background: #e9ecef; padding: 2px 6px; border-radius: 3px; }" +
               ".status { color: #28a745; font-weight: bold; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<h1>🚀 Day1 Hello World 应用</h1>" +
               "<p class='status'>✅ 应用运行正常！</p>" +
               "<p>欢迎使用 Day1 Spring Boot 学习项目！</p>" +
               "<div class='api-list'>" +
               "<h3>📚 可用的API接口：</h3>" +
               "<div class='api-item'>" +
               "<span class='method'>GET</span> <span class='url'>/day1/hello</span> - 基础Hello接口" +
               "</div>" +
               "<div class='api-item'>" +
               "<span class='method'>GET</span> <span class='url'>/day1/hello/greeting?name=张三</span> - 个性化问候" +
               "</div>" +
               "<div class='api-item'>" +
               "<span class='method'>GET</span> <span class='url'>/day1/hello/info</span> - 应用信息" +
               "</div>" +
               "<div class='api-item'>" +
               "<span class='method'>GET</span> <span class='url'>/day1/hello/health</span> - 健康检查" +
               "</div>" +
               "<div class='api-item'>" +
               "<span class='method'>POST</span> <span class='url'>/day1/hello/login</span> - 用户登录" +
               "</div>" +
               "<div class='api-item'>" +
               "<span class='method'>POST</span> <span class='url'>/day1/hello/batch</span> - 批量处理" +
               "</div>" +
               "</div>" +
               "<p><strong>当前时间：</strong>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>" +
               "<p><strong>应用版本：</strong>" + appVersion + "</p>" +
               "<p><strong>应用名称：</strong>" + appName + "</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}
