package com.picc.java.learn.ratelimit;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流功能测试类
 * 测试各种限流场景和功能
 * 
 * @author learn
 * @since 1.0
 */
public class RateLimitTest {
    
    public static void main(String[] args) {
        System.out.println("=== 限流功能测试开始 ===\n");
        
        // 测试1: 基本限流功能
        testBasicRateLimit();
        
        // 测试2: 用户+IP维度限流
        testUserIpRateLimit();
        
        // 测试3: 方法级别限流
        testMethodOnlyRateLimit();
        
        // 测试4: 并发限流测试
        testConcurrentRateLimit();
        
        // 测试5: 时间窗口重置测试
        testTimeWindowReset();
        
        System.out.println("\n=== 限流功能测试完成 ===");
    }
    
    /**
     * 测试基本限流功能
     */
    private static void testBasicRateLimit() {
        System.out.println("--- 测试1: 基本限流功能 ---");
        
        RateLimitManager manager = RateLimitManager.getInstance();
        manager.clearAllCounters(); // 清理之前的计数器
        
        String limitKey = "test:user1:192.168.1.1";
        int timeWindow = 10; // 10秒
        int maxRequests = 5; // 最多5次请求
        String message = "请求过于频繁";
        
        // 正常请求测试
        for (int i = 1; i <= 5; i++) {
            try {
                manager.tryAccess(limitKey, timeWindow, maxRequests, message);
                System.out.println("请求 " + i + " 成功");
            } catch (RateLimitException e) {
                System.out.println("请求 " + i + " 被限流: " + e.getMessage());
            }
        }
        
        // 超出限制的请求测试
        try {
            manager.tryAccess(limitKey, timeWindow, maxRequests, message);
            System.out.println("第6次请求成功（不应该成功）");
        } catch (RateLimitException e) {
            System.out.println("第6次请求被限流: " + e.getMessage());
            System.out.println("限流详情: " + e.toString());
        }
        
        System.out.println();
    }
    
    /**
     * 测试用户+IP维度限流
     */
    private static void testUserIpRateLimit() {
        System.out.println("--- 测试2: 用户+IP维度限流 ---");
        
        RateLimitManager manager = RateLimitManager.getInstance();
        manager.clearAllCounters();
        
        // 不同用户+IP组合应该有不同的限流计数器
        String[] limitKeys = {
            "getUserInfo:user1:192.168.1.1",
            "getUserInfo:user1:192.168.1.2", // 同一用户，不同IP
            "getUserInfo:user2:192.168.1.1"  // 不同用户，同一IP
        };
        
        for (String limitKey : limitKeys) {
            try {
                manager.tryAccess(limitKey, 60, 10, "用户+IP限流测试");
                System.out.println("限流键 " + limitKey + " 请求成功");
            } catch (RateLimitException e) {
                System.out.println("限流键 " + limitKey + " 被限流: " + e.getMessage());
            }
        }
        
        System.out.println("当前计数器数量: " + manager.getCounterCount());
        System.out.println();
    }
    
    /**
     * 测试方法级别限流
     */
    private static void testMethodOnlyRateLimit() {
        System.out.println("--- 测试3: 方法级别限流 ---");
        
        RateLimitManager manager = RateLimitManager.getInstance();
        manager.clearAllCounters();
        
        String methodKey = "login";
        int timeWindow = 60;
        int maxRequests = 3;
        
        // 模拟多个用户同时登录
        String[] users = {"user1", "user2", "user3", "user4"};
        String[] ips = {"192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4"};
        
        for (int i = 0; i < users.length; i++) {
            try {
                manager.tryAccess(methodKey, timeWindow, maxRequests, "登录限流测试");
                System.out.println("用户 " + users[i] + " 登录请求成功");
            } catch (RateLimitException e) {
                System.out.println("用户 " + users[i] + " 登录请求被限流: " + e.getMessage());
            }
        }
        
        System.out.println("当前计数器数量: " + manager.getCounterCount());
        System.out.println();
    }
    
    /**
     * 测试并发限流
     */
    private static void testConcurrentRateLimit() {
        System.out.println("--- 测试4: 并发限流测试 ---");
        
        RateLimitManager manager = RateLimitManager.getInstance();
        manager.clearAllCounters();
        
        String limitKey = "concurrent:user1:192.168.1.1";
        int timeWindow = 60;
        int maxRequests = 10;
        int threadCount = 20; // 20个并发线程
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger limitCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    manager.tryAccess(limitKey, timeWindow, maxRequests, "并发限流测试");
                    successCount.incrementAndGet();
                    System.out.println("线程 " + threadId + " 请求成功");
                } catch (RateLimitException e) {
                    limitCount.incrementAndGet();
                    System.out.println("线程 " + threadId + " 请求被限流");
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        
        System.out.println("并发测试结果:");
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("限流请求数: " + limitCount.get());
        System.out.println();
    }
    
    /**
     * 测试时间窗口重置
     */
    private static void testTimeWindowReset() {
        System.out.println("--- 测试5: 时间窗口重置测试 ---");
        
        RateLimitManager manager = RateLimitManager.getInstance();
        manager.clearAllCounters();
        
        String limitKey = "timeWindow:user1:192.168.1.1";
        int timeWindow = 3; // 3秒窗口
        int maxRequests = 2; // 最多2次请求
        
        // 第一次请求
        try {
            manager.tryAccess(limitKey, timeWindow, maxRequests, "时间窗口测试");
            System.out.println("第1次请求成功");
        } catch (RateLimitException e) {
            System.out.println("第1次请求被限流: " + e.getMessage());
        }
        
        // 第二次请求
        try {
            manager.tryAccess(limitKey, timeWindow, maxRequests, "时间窗口测试");
            System.out.println("第2次请求成功");
        } catch (RateLimitException e) {
            System.out.println("第2次请求被限流: " + e.getMessage());
        }
        
        // 第三次请求（应该被限流）
        try {
            manager.tryAccess(limitKey, timeWindow, maxRequests, "时间窗口测试");
            System.out.println("第3次请求成功（不应该成功）");
        } catch (RateLimitException e) {
            System.out.println("第3次请求被限流: " + e.getMessage());
        }
        
        // 等待时间窗口过期
        System.out.println("等待 " + timeWindow + " 秒让时间窗口过期...");
        try {
            Thread.sleep((timeWindow + 1) * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 时间窗口过期后，应该可以重新请求
        try {
            manager.tryAccess(limitKey, timeWindow, maxRequests, "时间窗口测试");
            System.out.println("时间窗口过期后，第1次请求成功");
        } catch (RateLimitException e) {
            System.out.println("时间窗口过期后，第1次请求被限流: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 测试注解功能（模拟AOP）
     */
    private static void testAnnotationFunctionality() {
        System.out.println("--- 测试6: 注解功能测试 ---");
        
        UserService userService = new UserServiceImpl();
        RateLimitAspect aspect = new RateLimitAspect();
        
        try {
            // 获取getUserInfo方法
            Method method = UserService.class.getMethod("getUserInfo", String.class);
            
            // 模拟AOP调用
            Object result = aspect.around(method, new Object[]{"user1"}, userService);
            System.out.println("通过AOP调用getUserInfo成功: " + result);
            
        } catch (Throwable e) {
            System.out.println("AOP调用失败: " + e.getMessage());
        }
        
        System.out.println();
    }
}
