package com.picc.java.learn.ratelimit;

/**
 * 用户服务接口
 * 演示如何使用@RateLimit注解进行限流控制
 * 
 * @author learn
 * @since 1.0
 */
public interface UserService {
    
    /**
     * 获取用户信息
     * 限流配置：每60秒最多允许100次请求，按用户+IP限流
     */
    @RateLimit(timeWindow = 60, maxRequests = 100, dimension = RateLimit.LimitDimension.USER_IP)
    String getUserInfo(String userId);
    
    /**
     * 更新用户信息
     * 限流配置：每60秒最多允许10次请求，按用户+IP限流
     */
    @RateLimit(timeWindow = 60, maxRequests = 10, dimension = RateLimit.LimitDimension.USER_IP, 
               message = "更新用户信息过于频繁，请稍后再试")
    boolean updateUserInfo(String userId, String userInfo);
    
    /**
     * 删除用户
     * 限流配置：每60秒最多允许5次请求，按用户+IP限流
     */
    @RateLimit(timeWindow = 60, maxRequests = 5, dimension = RateLimit.LimitDimension.USER_IP,
               message = "删除用户操作过于频繁，请稍后再试")
    boolean deleteUser(String userId);
    
    /**
     * 用户登录
     * 限流配置：每60秒最多允许20次请求，仅按方法限流（防止暴力破解）
     */
    @RateLimit(timeWindow = 60, maxRequests = 20, dimension = RateLimit.LimitDimension.METHOD_ONLY,
               message = "登录尝试过于频繁，请稍后再试")
    boolean login(String username, String password);
    
    /**
     * 用户注册
     * 限流配置：每300秒（5分钟）最多允许5次请求，按用户+IP限流
     */
    @RateLimit(timeWindow = 300, maxRequests = 5, dimension = RateLimit.LimitDimension.USER_IP,
               message = "注册过于频繁，请5分钟后再试")
    boolean register(String username, String password, String email);
}
