package com.picc.java.learn.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 用于标记需要进行限流控制的方法
 * 
 * @author learn
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 时间窗口，单位：秒
     * 默认60秒
     */
    int timeWindow() default 60;
    
    /**
     * 在时间窗口内允许的最大请求次数
     * 默认100次
     */
    int maxRequests() default 100;
    
    /**
     * 限流维度
     * USER_IP: 按用户+IP限流（默认）
     * METHOD_ONLY: 仅按方法限流
     */
    LimitDimension dimension() default LimitDimension.USER_IP;
    
    /**
     * 限流时的错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流维度枚举
     */
    enum LimitDimension {
        /**
         * 按用户+IP限流
         * 相同用户+相同IP在同一个限流维度
         */
        USER_IP,
        
        /**
         * 仅按方法限流
         * 所有请求共享同一个限流维度
         */
        METHOD_ONLY
    }
}
