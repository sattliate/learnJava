package com.picc.java.learn.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 限流管理器
 * 负责管理所有限流计数器，支持多种限流维度
 * 
 * @author learn
 * @since 1.0
 */
public class RateLimitManager {
    
    /**
     * 限流计数器存储
     * key: 限流键（用户+IP 或 方法名）
     * value: 限流计数器
     */
    private final ConcurrentMap<String, RateLimitCounter> counters = new ConcurrentHashMap<>();
    
    /**
     * 定时清理过期计数器的调度器
     */
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * 单例实例
     */
    private static final RateLimitManager INSTANCE = new RateLimitManager();
    
    /**
     * 私有构造函数，启动清理任务
     */
    private RateLimitManager() {
        // 启动定时清理任务，每5分钟清理一次过期的计数器
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredCounters, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 获取单例实例
     */
    public static RateLimitManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 尝试访问，如果超过限流阈值则抛出异常
     * 
     * @param limitKey 限流键
     * @param timeWindow 时间窗口（秒）
     * @param maxRequests 最大请求次数
     * @param message 限流消息
     * @throws RateLimitException 当超过限流阈值时抛出
     */
    public void tryAccess(String limitKey, int timeWindow, int maxRequests, String message) {
        RateLimitCounter counter = getOrCreateCounter(limitKey, timeWindow, maxRequests);
        
        if (!counter.tryIncrement()) {
            throw new RateLimitException(
                message, 
                limitKey, 
                counter.getCurrentCount(), 
                maxRequests, 
                timeWindow
            );
        }
    }
    
    /**
     * 获取或创建限流计数器
     * 
     * @param limitKey 限流键
     * @param timeWindow 时间窗口（秒）
     * @param maxRequests 最大请求次数
     * @return 限流计数器
     */
    private RateLimitCounter getOrCreateCounter(String limitKey, int timeWindow, int maxRequests) {
        return counters.computeIfAbsent(limitKey, k -> new RateLimitCounter(timeWindow, maxRequests));
    }
    
    /**
     * 生成限流键
     * 
     * @param dimension 限流维度
     * @param methodName 方法名
     * @param userId 用户ID（可为null）
     * @param ipAddress IP地址（可为null）
     * @return 限流键
     */
    public static String generateLimitKey(RateLimit.LimitDimension dimension, String methodName, String userId, String ipAddress) {
        switch (dimension) {
            case USER_IP:
                if (userId == null || ipAddress == null) {
                    throw new IllegalArgumentException("USER_IP维度需要提供userId和ipAddress");
                }
                return String.format("%s:%s:%s", methodName, userId, ipAddress);
            case METHOD_ONLY:
                return methodName;
            default:
                throw new IllegalArgumentException("不支持的限流维度: " + dimension);
        }
    }
    
    /**
     * 获取限流统计信息
     * 
     * @param limitKey 限流键
     * @return 限流计数器，如果不存在则返回null
     */
    public RateLimitCounter getCounter(String limitKey) {
        return counters.get(limitKey);
    }
    
    /**
     * 清理过期的计数器
     */
    private void cleanupExpiredCounters() {
        long currentTime = System.currentTimeMillis();
        counters.entrySet().removeIf(entry -> {
            RateLimitCounter counter = entry.getValue();
            return currentTime - counter.getWindowStartTime() >= counter.getWindowSize();
        });
    }
    
    /**
     * 手动清理所有计数器（主要用于测试）
     */
    public void clearAllCounters() {
        counters.clear();
    }
    
    /**
     * 获取当前计数器数量
     */
    public int getCounterCount() {
        return counters.size();
    }
    
    /**
     * 关闭管理器，停止清理任务
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
