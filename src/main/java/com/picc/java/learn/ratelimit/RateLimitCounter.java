package com.picc.java.learn.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流计数器
 * 用于记录特定限流维度的请求次数和时间窗口
 * 
 * @author learn
 * @since 1.0
 */
public class RateLimitCounter {
    
    /**
     * 时间窗口开始时间（毫秒）
     */
    private final long windowStartTime;
    
    /**
     * 时间窗口大小（毫秒）
     */
    private final long windowSize;
    
    /**
     * 当前时间窗口内的请求次数
     */
    private final AtomicInteger requestCount;
    
    /**
     * 最大允许请求次数
     */
    private final int maxRequests;
    
    /**
     * 构造函数
     * 
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 最大允许请求次数
     */
    public RateLimitCounter(int windowSize, int maxRequests) {
        this.windowStartTime = System.currentTimeMillis();
        this.windowSize = windowSize * 1000L; // 转换为毫秒
        this.maxRequests = maxRequests;
        this.requestCount = new AtomicInteger(0);
    }
    
    /**
     * 尝试增加请求计数
     * 
     * @return true 如果请求被允许，false 如果请求被限流
     */
    public boolean tryIncrement() {
        // 检查是否需要重置时间窗口
        if (isWindowExpired()) {
            resetWindow();
        }
        
        // 检查是否超过最大请求次数
        int currentCount = requestCount.get();
        if (currentCount >= maxRequests) {
            return false;
        }
        
        // 尝试增加计数
        return requestCount.incrementAndGet() <= maxRequests;
    }
    
    /**
     * 获取当前请求次数
     */
    public int getCurrentCount() {
        return requestCount.get();
    }
    
    /**
     * 获取最大允许请求次数
     */
    public int getMaxRequests() {
        return maxRequests;
    }
    
    /**
     * 获取时间窗口剩余时间（毫秒）
     */
    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - windowStartTime;
        return Math.max(0, windowSize - elapsed);
    }
    
    /**
     * 检查时间窗口是否已过期
     */
    private boolean isWindowExpired() {
        return System.currentTimeMillis() - windowStartTime >= windowSize;
    }
    
    /**
     * 重置时间窗口
     */
    private void resetWindow() {
        // 这里使用简单的重置策略
        // 在实际应用中，可能需要更复杂的滑动窗口实现
        requestCount.set(0);
    }
    
    /**
     * 获取时间窗口开始时间
     */
    public long getWindowStartTime() {
        return windowStartTime;
    }
    
    /**
     * 获取时间窗口大小（毫秒）
     */
    public long getWindowSize() {
        return windowSize;
    }
    
    @Override
    public String toString() {
        return String.format("RateLimitCounter{currentCount=%d, maxRequests=%d, remainingTime=%dms, windowStartTime=%d}", 
                getCurrentCount(), maxRequests, getRemainingTime(), windowStartTime);
    }
}
