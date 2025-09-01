package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 订单速度信息DTO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSpeedInfoDTO {
    
    /**
     * Redis Key
     */
    private String key;
    
    /**
     * 时间窗口（小时）
     */
    private int timeWindowHours;
    
    /**
     * 窗口开始时间
     */
    private Date windowStartTime;
    
    /**
     * 窗口结束时间
     */
    private Date windowEndTime;
    
    /**
     * 订单数量
     */
    private long orderCount;
    
    /**
     * 每小时订单数
     */
    private double ordersPerHour;
    
    /**
     * 每分钟订单数
     */
    private double ordersPerMinute;
    
    /**
     * 每秒订单数
     */
    private double ordersPerSecond;
    
    /**
     * 订单ID列表
     */
    private List<String> orderIds;

    @Override
    public String toString() {
        return String.format("OrderSpeedInfoDTO{key='%s', timeWindow=%dh, orderCount=%d, ordersPerHour=%.2f, ordersPerMinute=%.2f}", 
                key, timeWindowHours, orderCount, ordersPerHour, ordersPerMinute);
    }
}
