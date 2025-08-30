package com.picc.java.learn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * DevTools 热加载演示控制器
 * 
 * 演示 Spring Boot DevTools 的热重载功能：
 * 1. 修改代码后自动重启应用
 * 2. 实时查看修改效果
 * 3. 提高开发效率
 * 
 * @author learn
 * @since 1.0.0
 */
@RestController
@RequestMapping("/devtools")
public class DevToolsDemoController {

    // 这个变量用于演示热加载效果
    private String welcomeMessage = "🎉 ！！！欢迎1使用 DevTools 热加载演示！！";
    
    // 这个变量用于演示配置热更新
    private int counter = 4;
    
    // 这个变量用于演示方法热更新
    private String currentTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取欢迎信息 - 演示变量热更新
     * 
     * 修改 welcomeMessage 变量后，保存文件即可看到效果
     * 
     * @return 欢迎信息
     */
    @GetMapping("/welcome")
    public String getWelcomeMessage() {
        return welcomeMessage + " (修改这个变量来测试热加载)";
    }

    /**
     * 获取计数器 - 演示状态热更新
     * 
     * 修改 counter 变量的初始值后，保存文件即可看到效果
     * 
     * @return 计数器信息
     */
    @GetMapping("/counter")
    public Map<String, Object> getCounter() {
        counter++;
        Map<String, Object> result = new HashMap<>();
        result.put("counter", counter);
        result.put("message", "当前计数: " + counter);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        result.put("tip", "修改 counter 变量的初始值来测试热加载");
        return result;
    }

    /**
     * 获取当前时间 - 演示方法热更新
     * 
     * 修改 currentTimeFormat 变量后，保存文件即可看到效果
     * 
     * @return 当前时间信息
     */
    @GetMapping("/time")
    public Map<String, Object> getCurrentTime() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        result.put("format", currentTimeFormat);
        result.put("message", "修改 currentTimeFormat 变量来测试热加载");
        result.put("examples", new String[]{
            "yyyy-MM-dd HH:mm:ss",
            "yyyy年MM月dd日 HH时mm分ss秒",
            "MM/dd/yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss"
        });
        return result;
    }

    /**
     * 获取热加载状态信息
     * 
     * @return 热加载状态
     */
    @GetMapping("/status")
    public Map<String, Object> getDevToolsStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("devtoolsEnabled", true);
        status.put("autoRestart", true);
        status.put("liveReload", true);
        status.put("message", "DevTools 热加载功能已启用！");
        status.put("instructions", new String[]{
            "1. 修改 Java 代码后保存文件",
            "2. 应用会自动重启",
            "3. 刷新浏览器查看效果",
            "4. 无需手动重启应用"
        });
        status.put("supportedChanges", new String[]{
            "修改 Java  类",
            "修改配置文件",
            "修改静态资源",
            "修改模板文件"
        });
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        return status;
    }

    /**
     * 动态修改欢迎信息 - 演示运行时配置
     * 
     * @param message 新的欢迎信息
     * @return 修改结果
     */
    @GetMapping("/update-welcome")
    public Map<String, Object> updateWelcomeMessage(@RequestParam String message) {
        this.welcomeMessage = message;
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("oldMessage", welcomeMessage);
        result.put("newMessage", message);
        result.put("message", "欢迎信息已更新！");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        return result;
    }

    /**
     * 动态修改时间格式 - 演示运行时配置
     * 
     * @param format 新的时间格式
     * @return 修改结果
     */
    @GetMapping("/update-format")
    public Map<String, Object> updateTimeFormat(@RequestParam String format) {
        String oldFormat = this.currentTimeFormat;
        this.currentTimeFormat = format;
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("oldFormat", oldFormat);
        result.put("newFormat", format);
        result.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(format)));
        result.put("message", "时间格式已更新！");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        return result;
    }

    /**
     * 重置计数器 - 演示状态重置
     * 
     * @return 重置结果
     */
    @GetMapping("/reset-counter")
    public Map<String, Object> resetCounter() {
        int oldCounter = this.counter;
        this.counter = 0;
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("oldCounter", oldCounter);
        result.put("newCounter", counter);
        result.put("message", "计数器已重置！");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(currentTimeFormat)));
        return result;
    }
}
