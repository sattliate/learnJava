package com.picc.java.learn.ratelimit;

import java.lang.reflect.Method;

/**
 * 限流切面
 * 使用AOP拦截带有@RateLimit注解的方法，实现自动限流
 * 
 * 注意：这是一个简化的实现，在实际项目中通常使用Spring AOP或AspectJ
 * 
 * @author learn
 * @since 1.0
 */
public class RateLimitAspect {
    
    /**
     * 限流管理器实例
     */
    private final RateLimitManager rateLimitManager = RateLimitManager.getInstance();
    
    /**
     * 用户上下文提供者（用于获取当前用户信息）
     */
    private final UserContextProvider userContextProvider;
    
    /**
     * IP地址提供者（用于获取客户端IP）
     */
    private final IpAddressProvider ipAddressProvider;
    
    /**
     * 构造函数
     * 
     * @param userContextProvider 用户上下文提供者
     * @param ipAddressProvider IP地址提供者
     */
    public RateLimitAspect(UserContextProvider userContextProvider, IpAddressProvider ipAddressProvider) {
        this.userContextProvider = userContextProvider;
        this.ipAddressProvider = ipAddressProvider;
    }
    
    /**
     * 默认构造函数，使用默认的提供者
     */
    public RateLimitAspect() {
        this(new DefaultUserContextProvider(), new DefaultIpAddressProvider());
    }
    
    /**
     * 限流拦截方法
     * 在实际项目中，这个方法会被AOP框架自动调用
     * 
     * @param method 被拦截的方法
     * @param args 方法参数
     * @param target 目标对象
     * @return 方法执行结果
     * @throws Throwable 如果发生异常
     */
    public Object around(Method method, Object[] args, Object target) throws Throwable {
        // 检查方法是否有@RateLimit注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            // 没有注解，直接执行原方法
            return method.invoke(target, args);
        }
        
        // 获取限流配置
        int timeWindow = rateLimit.timeWindow();
        int maxRequests = rateLimit.maxRequests();
        String message = rateLimit.message();
        RateLimit.LimitDimension dimension = rateLimit.dimension();
        
        // 生成限流键
        String limitKey = generateLimitKey(method, dimension);
        
        // 尝试访问
        rateLimitManager.tryAccess(limitKey, timeWindow, maxRequests, message);
        
        // 限流检查通过，执行原方法
        return method.invoke(target, args);
    }
    
    /**
     * 生成限流键
     * 
     * @param method 方法
     * @param dimension 限流维度
     * @return 限流键
     */
    private String generateLimitKey(Method method, RateLimit.LimitDimension dimension) {
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        switch (dimension) {
            case USER_IP:
                String userId = userContextProvider.getCurrentUserId();
                String ipAddress = ipAddressProvider.getClientIpAddress();
                return RateLimitManager.generateLimitKey(dimension, methodName, userId, ipAddress);
            case METHOD_ONLY:
                return RateLimitManager.generateLimitKey(dimension, methodName, null, null);
            default:
                throw new IllegalArgumentException("不支持的限流维度: " + dimension);
        }
    }
    
    /**
     * 用户上下文提供者接口
     */
    public interface UserContextProvider {
        /**
         * 获取当前用户ID
         * 
         * @return 用户ID，如果未登录则返回null
         */
        String getCurrentUserId();
    }
    
    /**
     * IP地址提供者接口
     */
    public interface IpAddressProvider {
        /**
         * 获取客户端IP地址
         * 
         * @return 客户端IP地址
         */
        String getClientIpAddress();
    }
    
    /**
     * 默认用户上下文提供者
     */
    private static class DefaultUserContextProvider implements UserContextProvider {
        @Override
        public String getCurrentUserId() {
            // 在实际项目中，这里应该从ThreadLocal或Session中获取用户信息
            // 这里返回一个默认值用于演示
            return "default_user";
        }
    }
    
    /**
     * 默认IP地址提供者
     */
    private static class DefaultIpAddressProvider implements IpAddressProvider {
        @Override
        public String getClientIpAddress() {
            // 在实际项目中，这里应该从HttpServletRequest中获取IP地址
            // 这里返回一个默认值用于演示
            return "127.0.0.1";
        }
    }
}
