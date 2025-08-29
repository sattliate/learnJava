package com.picc.java.learn.ratelimit.core;

/**
 * 上下文提供者接口
 * 用于获取请求相关的上下文信息，如用户ID、IP地址等
 * 
 * @author learn
 * @since 1.0
 */
public interface ContextProvider {
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未登录则返回null
     */
    String getCurrentUserId();
    
    /**
     * 获取客户端IP地址
     * 
     * @return 客户端IP地址
     */
    String getClientIpAddress();
    
    /**
     * 获取请求ID
     * 
     * @return 请求ID，用于请求追踪
     */
    String getRequestId();
    
    /**
     * 获取用户代理信息
     * 
     * @return 用户代理字符串
     */
    String getUserAgent();
}
