package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.dto.OrderSpeedInfoDTO;
import com.picc.java.learn.alert.dto.OrderTimeRangeStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单速度服务
 * 专门负责订单速度计算和统计的业务逻辑
 * 
 * @author learn-java
 * @since 2025-08-31
 */
@Slf4j
@Service
public class OrderSpeedService {

    @Autowired
    private MockRedisService mockRedisService;

    /**
     * 获取指定时间范围内的订单统计信息
     */
    public OrderTimeRangeStatsDTO getOrderStatsInTimeRange(String key, long startTime, long endTime) {
        try {
            // 获取时间范围内的订单数量
            long orderCount = mockRedisService.zcount(key, startTime, endTime);
            
            // 获取时间范围内的订单ID列表
            List<String> orderIds = mockRedisService.zrangeByScore(key, startTime, endTime);
            
            // 计算时间间隔（分钟）
            long timeIntervalMinutes = (endTime - startTime) / (60 * 1000);
            
            // 计算速度（订单数量/时间间隔）
            double speed = timeIntervalMinutes > 0 ? (double) orderCount / timeIntervalMinutes : 0.0;
            
            OrderTimeRangeStatsDTO stats = OrderTimeRangeStatsDTO.builder()
                    .key(key)
                    .startTime(startTime)
                    .endTime(endTime)
                    .orderCount(orderCount)
                    .orderIds(orderIds)
                    .timeIntervalMinutes(timeIntervalMinutes)
                    .speed(speed)
                    .build();
            
            log.info("获取时间范围内订单统计: key={}, startTime={}, endTime={}, orderCount={}, speed={:.2f}", 
                    key, startTime, endTime, orderCount, speed);
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取时间范围内订单统计失败: key={}, startTime={}, endTime={}", key, startTime, endTime, e);
            return OrderTimeRangeStatsDTO.builder()
                    .key(key)
                    .startTime(startTime)
                    .endTime(endTime)
                    .orderCount(0)
                    .orderIds(new ArrayList<>())
                    .timeIntervalMinutes(0)
                    .speed(0.0)
                    .build();
        }
    }

    /**
     * 计算指定时间窗口内的订单速度
     */
    public OrderSpeedInfoDTO calculateOrderSpeed(String key, int timeWindowHours) {
        long now = System.currentTimeMillis();
        long windowStart = now - (timeWindowHours * 60 * 60 * 1000L);

        // 获取时间窗口内的订单数量
        long orderCount = mockRedisService.zcount(key, windowStart, now);
        
        // 获取时间窗口内的订单列表（用于计算时间分布）
        List<String> orderIds = mockRedisService.zrangeByScore(key, windowStart, now);
        
        // 计算速度
        double hours = timeWindowHours;
        double ordersPerHour = hours > 0 ? (double) orderCount / hours : 0.0;
        double ordersPerMinute = ordersPerHour / 60.0;
        double ordersPerSecond = ordersPerMinute / 60.0;

        OrderSpeedInfoDTO speedInfo = OrderSpeedInfoDTO.builder()
                .key(key)
                .timeWindowHours(timeWindowHours)
                .windowStartTime(new Date(windowStart))
                .windowEndTime(new Date(now))
                .orderCount(orderCount)
                .ordersPerHour(ordersPerHour)
                .ordersPerMinute(ordersPerMinute)
                .ordersPerSecond(ordersPerSecond)
                .orderIds(orderIds)
                .build();

        log.info("计算订单速度: key={}, timeWindow={}h, orderCount={}, ordersPerHour={:.2f}", 
                key, timeWindowHours, orderCount, ordersPerHour);

        return speedInfo;
    }

    /**
     * 计算多个时间窗口的订单速度
     */
    public List<OrderSpeedInfoDTO> calculateOrderSpeedMultiWindow(String key, int... timeWindows) {
        List<OrderSpeedInfoDTO> speedInfos = new ArrayList<>();
        
        for (int timeWindow : timeWindows) {
            OrderSpeedInfoDTO speedInfo = calculateOrderSpeed(key, timeWindow);
            speedInfos.add(speedInfo);
        }

        return speedInfos;
    }

    /**
     * 检测异常订单速度
     */
    public boolean isAbnormalSpeed(String key, int timeWindowHours, int threshold) {
        OrderSpeedInfoDTO speedInfo = calculateOrderSpeed(key, timeWindowHours);
        return speedInfo.getOrderCount() > threshold;
    }

    /**
     * 获取订单速度趋势分析
     */
    public Map<String, Object> getSpeedTrendAnalysis(String key, int timeWindowHours) {
        OrderSpeedInfoDTO currentSpeed = calculateOrderSpeed(key, timeWindowHours);
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("currentSpeed", currentSpeed);
        trend.put("isAbnormal", currentSpeed.getOrderCount() > 10); // 阈值可配置
        trend.put("trendDirection", "stable"); // 可扩展趋势分析逻辑
        
        return trend;
    }
}
