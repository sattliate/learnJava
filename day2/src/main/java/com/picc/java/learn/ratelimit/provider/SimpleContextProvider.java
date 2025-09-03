package com.picc.java.learn.ratelimit.provider;

import com.picc.java.learn.ratelimit.core.ContextProvider;

/**
 * 简单上下文提供者
 * 用于测试和演示，实际项目中应该根据具体的框架实现
 * 
 * @author learn
 * @since 1.0
 */
public class SimpleContextProvider implements ContextProvider {
    
    /**
     * 当前用户ID（用于测试）
     */
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    
    /**
     * 当前IP地址（用于测试）
     */
    private static final ThreadLocal<String> currentIpAddress = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     * 
     * @param userId 用户ID
     */
    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }
    
    /**
     * 设置当前IP地址
     * 
     * @param ipAddress IP地址
     */
    public static void setCurrentIpAddress(String ipAddress) {
        currentIpAddress.set(ipAddress);
    }
    
    /**
     * 清理当前线程的上下文
     */
    public static void clear() {
        currentUserId.remove();
        currentIpAddress.remove();
    }
    
    @Override
    public String getCurrentUserId() {
        return currentUserId.get();
    }
    
    @Override
    public String getClientIpAddress() {
        return currentIpAddress.get();
    }
    
    @Override
    public String getRequestId() {
        return "req_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    @Override
    public String getUserAgent() {
        return "SimpleContextProvider/1.0";
    }
}
