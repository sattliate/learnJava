package com.picc.java.learn.ratelimit;

/**
 * 限流异常
 * 当请求超过限流阈值时抛出此异常
 * 
 * @author learn
 * @since 1.0
 */
public class RateLimitException extends RuntimeException {
    
    /**
     * 限流键（用于标识限流维度）
     */
    private final String limitKey;
    
    /**
     * 当前请求次数
     */
    private final int currentCount;
    
    /**
     * 最大允许请求次数
     */
    private final int maxRequests;
    
    /**
     * 时间窗口（秒）
     */
    private final int timeWindow;
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param limitKey 限流键
     * @param currentCount 当前请求次数
     * @param maxRequests 最大允许请求次数
     * @param timeWindow 时间窗口
     */
    public RateLimitException(String message, String limitKey, int currentCount, int maxRequests, int timeWindow) {
        super(message);
        this.limitKey = limitKey;
        this.currentCount = currentCount;
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
    }
    
    /**
     * 获取限流键
     */
    public String getLimitKey() {
        return limitKey;
    }
    
    /**
     * 获取当前请求次数
     */
    public int getCurrentCount() {
        return currentCount;
    }
    
    /**
     * 获取最大允许请求次数
     */
    public int getMaxRequests() {
        return maxRequests;
    }
    
    /**
     * 获取时间窗口
     */
    public int getTimeWindow() {
        return timeWindow;
    }
    
    @Override
    public String toString() {
        return String.format("RateLimitException{limitKey='%s', currentCount=%d, maxRequests=%d, timeWindow=%d, message='%s'}", 
                limitKey, currentCount, maxRequests, timeWindow, getMessage());
    }
}
