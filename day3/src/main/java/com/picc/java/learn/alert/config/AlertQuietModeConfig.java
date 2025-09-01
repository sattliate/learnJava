package com.picc.java.learn.alert.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 预警免打扰机制配置
 * 管理预警发送频率和免打扰策略
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "club.alert.quiet-mode")
public class AlertQuietModeConfig {

    /**
     * 免打扰时间段（分钟）
     * 第一次预警后，下次预警时间间隔依次为：10分钟、30分钟、60分钟
     * 超过这些时间段后，使用最后一个时间段（60分钟）作为预警频率
     */
    private List<Integer> quietPeriods = Arrays.asList(10, 30, 60);

    /**
     * 最大预警次数
     * 超过此次数后，使用最后一个免打扰时间段作为预警频率
     */
    private int maxAlertCount = 3;

    /**
     * 是否启用免打扰机制
     */
    private boolean enabled = true;

    /**
     * Redis key前缀
     */
    private String redisKeyPrefix = "quiet_mode";

    /**
     * 预警记录过期时间（分钟）
     * 使用最后一个免打扰时间段作为过期时间
     */
    private int expireMinutes = 60;

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * 获取最后一个免打扰时间段
     * 
     * @return 最后一个免打扰时间段（分钟）
     */
    public int getLastQuietPeriod() {
        if (quietPeriods == null || quietPeriods.isEmpty()) {
            return 60; // 默认60分钟
        }
        return quietPeriods.get(quietPeriods.size() - 1);
    }

    /**
     * 获取指定预警次数对应的免打扰时间段
     * 
     * @param alertCount 预警次数（从1开始）
     * @return 对应的免打扰时间段（分钟），如果超出范围返回最后一个时间段
     */
    public int getQuietPeriodForAlertCount(int alertCount) {
        if (quietPeriods == null || quietPeriods.isEmpty()) {
            return 60; // 默认60分钟
        }
        
        if (alertCount <= 0) {
            return 0; // 第一次预警，无免打扰
        }
        
        if (alertCount > quietPeriods.size()) {
            return getLastQuietPeriod(); // 超出范围，使用最后一个时间段
        }
        
        return quietPeriods.get(alertCount - 1);
    }

    /**
     * 检查是否超过最大预警次数
     * 
     * @param alertCount 当前预警次数
     * @return 是否超过最大预警次数
     */
    public boolean isOverMaxAlertCount(int alertCount) {
        return alertCount >= maxAlertCount;
    }

    /**
     * 生成Redis key
     * 
     * @param alertKey 预警维度key
     * @return 完整的Redis key
     */
    public String generateRedisKey(String alertKey) {
        return redisKeyPrefix + ":" + alertKey;
    }
}
