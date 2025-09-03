package com.picc.java.learn.alert.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis数据实体
 * 统一管理不同类型的数据和过期时间
 * 
 * @author learn-java
 * @since 2025-08-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisEntry {
    
    /**
     * 数据类型
     */
    private RedisDataType dataType;
    
    /**
     * 数据值
     */
    private Object value;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private long createTime;
    
    /**
     * 过期时间（毫秒时间戳，0表示永不过期）
     */
    private long expireTime;
    
    /**
     * 判断是否已过期
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
    
    /**
     * 获取剩余生存时间（秒）
     * 
     * @return 剩余生存时间，-1表示永不过期，0表示已过期
     */
    public long getTtl() {
        if (expireTime <= 0) {
            return -1; // 永不过期
        }
        long ttl = expireTime - System.currentTimeMillis();
        return ttl > 0 ? ttl / 1000 : 0;
    }
    
    /**
     * 获取剩余生存时间（毫秒）
     * 
     * @return 剩余生存时间，-1表示永不过期，0表示已过期
     */
    public long getTtlMillis() {
        if (expireTime <= 0) {
            return -1; // 永不过期
        }
        long ttl = expireTime - System.currentTimeMillis();
        return ttl > 0 ? ttl : 0;
    }
    
    /**
     * Redis数据类型枚举
     */
    public enum RedisDataType {
        ORDER_COUNT,      // 订单数量 (Integer)
        REDIS_STRING,     // 通用字符串 (String)
        SORTED_SET,       // 有序集合 (TreeMap<Long, Set<String>>)
        HASH,            // 哈希表 (Map<String, Object>)
        LIST,            // 列表 (List<Object>)
        ALERT_RECORD     // 预警记录 (Object)
    }
    
    @Override
    public String toString() {
        return String.format("RedisEntry{type=%s, value=%s, createTime=%d, expireTime=%d, ttl=%ds}", 
                dataType, value, createTime, expireTime, getTtl());
    }
}
