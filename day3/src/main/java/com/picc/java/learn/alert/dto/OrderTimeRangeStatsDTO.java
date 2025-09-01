package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 时间范围内订单统计信息DTO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeRangeStatsDTO {
    
    /**
     * Redis Key
     */
    private String key;
    
    /**
     * 开始时间
     */
    private long startTime;
    
    /**
     * 结束时间
     */
    private long endTime;
    
    /**
     * 订单数量
     */
    private long orderCount;
    
    /**
     * 订单ID列表
     */
    private List<String> orderIds;
    
    /**
     * 时间间隔（分钟）
     */
    private long timeIntervalMinutes;
    
    /**
     * 速度（订单/分钟）
     */
    private double speed;

    @Override
    public String toString() {
        return String.format("OrderTimeRangeStatsDTO{key='%s', startTime=%d, endTime=%d, orderCount=%d, timeInterval=%dmin, speed=%.2f}", 
                key, startTime, endTime, orderCount, timeIntervalMinutes, speed);
    }
}
