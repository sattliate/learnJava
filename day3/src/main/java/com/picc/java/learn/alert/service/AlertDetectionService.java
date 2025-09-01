package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.OrderAlertConfig;
import com.picc.java.learn.alert.dto.AlertRecordDTO;
import com.picc.java.learn.alert.dto.OrderDTO;
import com.picc.java.learn.alert.dto.OrderTimeRangeStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 预警检测服务
 * 专门负责预警检测的业务逻辑
 * 
 * @author learn-java
 * @since 2025-08-31
 */
@Slf4j
@Service
public class AlertDetectionService {

    @Autowired
    private OrderAlertConfig orderAlertConfig;

    @Autowired
    private MockDatabaseService mockDatabaseService;

    @Autowired
    private MockRedisService mockRedisService;

    @Autowired
    private OrderSpeedService orderSpeedService;

    @Autowired
    private AlertNotificationService alertNotificationService;

    @Autowired
    private AlertQuietModeService alertQuietModeService;

    /**
     * 检测订单预警
     * 
     * @param orderDTO 订单DTO
     */
    public void checkOrderAlert(OrderDTO orderDTO) {
        try {
            log.info("开始预警检测: orderId={}, memberPhone={}, province={}, channel={}", 
                    orderDTO.getOrderId(), orderDTO.getMemberPhone(), 
                    orderDTO.getProvince(), orderDTO.getChannel());

            // 1. 检查省份和渠道是否在监控范围内
            if (!isInMonitorScope(orderDTO.getProvince(), orderDTO.getChannel())) {
                log.info("订单不在监控范围内，跳过预警检测: province={}, channel={}", 
                        orderDTO.getProvince(), orderDTO.getChannel());
                return;
            }

            // 2. 获取Redis Key
            String redisKey = orderDTO.getRedisKey();
            
            // 3. 增加订单数量
            Integer currentCount = mockRedisService.incrementOrderCount(
                redisKey, 
                1, 
                orderAlertConfig.getTimeWindowSeconds()
            );

            // 4. 检查是否超过阈值
            Integer threshold = orderAlertConfig.getThresholds();
            if (currentCount > threshold) {
                log.warn("检测到订单数量超过阈值: memberPhone={}, province={}, channel={}, currentCount={}, threshold={}", 
                        orderDTO.getMemberPhone(), orderDTO.getProvince(), 
                        orderDTO.getChannel(), currentCount, threshold);

                // 5. 创建预警记录
                createAlertRecord(orderDTO, currentCount, threshold);
            }

            log.info("预警检测完成: orderId={}, currentCount={}, threshold={}", 
                    orderDTO.getOrderId(), currentCount, threshold);

        } catch (Exception e) {
            log.error("预警检测失败: orderId={}", orderDTO.getOrderId(), e);
        }
    }

    /**
     * 检查订单是否在监控范围内
     * 
     * @param province 省份
     * @param channel 渠道
     * @return 是否在监控范围内
     */
    private boolean isInMonitorScope(String province, String channel) {
        // 检查省份是否匹配
        if (!orderAlertConfig.getProvince().equals(province)) {
            return false;
        }

        // 检查渠道是否在监控范围内
        List<String> sourceList = orderAlertConfig.getSourceList();
        return sourceList.contains(channel);
    }

    /**
     * 创建预警记录
     * 
     * @param orderDTO 订单DTO
     * @param currentCount 当前订单数量
     * @param threshold 阈值
     */
    private void createAlertRecord(OrderDTO orderDTO, Integer currentCount, Integer threshold) {
        try {
            // 构建预警维度key
            String alertKey = String.format("%s-%s-%s-%s", 
                    orderDTO.getMemberPhone(), 
                    orderDTO.getProvince(), 
                    orderDTO.getChannel(), 
                    orderDTO.getRegisterId());
            
            // 检查免打扰机制
            if (!alertQuietModeService.canSendAlert(alertKey)) {
                log.info("🚫 预警在免打扰期内，跳过发送: alertKey={}", alertKey);
                return;
            }
            
            // 计算预警速度
            String redisKey = orderDTO.getRedisKey();
            long currentTime = System.currentTimeMillis();
            
            // 获取最近12小时内的订单统计
            long startTime = currentTime - (12 * 60 * 60 * 1000L); // 12小时前
            OrderTimeRangeStatsDTO stats = 
                    orderSpeedService.getOrderStatsInTimeRange(redisKey, startTime, currentTime);
            
            // 计算预警速度（订单数量/时间间隔）
            double alertSpeed = stats.getSpeed();
            long timeIntervalMinutes = stats.getTimeIntervalMinutes();
            
            // 获取上次预警时间（这里简化处理，实际项目中需要查询数据库）
            LocalDateTime lastAlertTime = getLastAlertTime(orderDTO.getMemberPhone(), orderDTO.getProvince(), orderDTO.getChannel());
            
            AlertRecordDTO alertRecord = AlertRecordDTO.builder()
                    .memberPhone(orderDTO.getMemberPhone())
                    .memberName(orderDTO.getMemberName())
                    .province(orderDTO.getProvince())
                    .channel(orderDTO.getChannel())
                    .registerId(orderDTO.getRegisterId())
                    .currentOrderCount(currentCount)
                    .threshold(threshold)
                    .alertTime(LocalDateTime.now())
                    .alertStatus("PENDING")
                    .alertSpeed(alertSpeed)
                    .timeIntervalMinutes(timeIntervalMinutes)
                    .lastAlertTime(lastAlertTime)
                    .build();

            // 保存预警记录
            AlertRecordDTO savedAlert = mockDatabaseService.saveAlertRecord(alertRecord);
            
            log.info("🚨 预警记录创建成功: alertId={}, memberPhone={}, province={}, channel={}, currentCount={}, threshold={}, speed={:.2f}", 
                    savedAlert.getAlertId(), savedAlert.getMemberPhone(), savedAlert.getProvince(), 
                    savedAlert.getChannel(), savedAlert.getCurrentOrderCount(), savedAlert.getThreshold(), alertSpeed);

            // 发送预警通知
            alertNotificationService.sendAlertNotification(savedAlert);
            
            // 记录预警发送（用于免打扰机制）
            alertQuietModeService.recordAlertSent(alertKey);
            
        } catch (Exception e) {
            log.error("创建预警记录失败: memberPhone={}, province={}, channel={}", 
                    orderDTO.getMemberPhone(), orderDTO.getProvince(), orderDTO.getChannel(), e);
            
            // 如果计算速度失败，仍然创建预警记录（不包含速度信息）
            AlertRecordDTO alertRecord = AlertRecordDTO.builder()
                    .memberPhone(orderDTO.getMemberPhone())
                    .memberName(orderDTO.getMemberName())
                    .province(orderDTO.getProvince())
                    .channel(orderDTO.getChannel())
                    .registerId(orderDTO.getRegisterId())
                    .currentOrderCount(currentCount)
                    .threshold(threshold)
                    .alertTime(LocalDateTime.now())
                    .alertStatus("PENDING")
                    .build();

            AlertRecordDTO savedAlert = mockDatabaseService.saveAlertRecord(alertRecord);
            alertNotificationService.sendAlertNotification(savedAlert);
            
            // 记录预警发送（用于免打扰机制）
            String alertKey = String.format("%s-%s-%s-%s", 
                    orderDTO.getMemberPhone(), 
                    orderDTO.getProvince(), 
                    orderDTO.getChannel(), 
                    orderDTO.getRegisterId());
            alertQuietModeService.recordAlertSent(alertKey);
        }
    }

    /**
     * 获取上次预警时间
     * 
     * @param memberPhone 会员手机号
     * @param province 省份
     * @param channel 渠道
     * @return 上次预警时间，如果没有则返回null
     */
    private LocalDateTime getLastAlertTime(String memberPhone, String province, String channel) {
        try {
            // 查询该会员在指定省份和渠道的最后一次预警记录
            List<AlertRecordDTO> alerts = mockDatabaseService.getAlertRecordsByProvinceAndChannel(province, channel);
            
            // 过滤出该会员的预警记录，并按时间排序
            return alerts.stream()
                    .filter(alert -> memberPhone.equals(alert.getMemberPhone()))
                    .sorted((a1, a2) -> a2.getAlertTime().compareTo(a1.getAlertTime()))
                    .findFirst()
                    .map(AlertRecordDTO::getAlertTime)
                    .orElse(null);
                    
        } catch (Exception e) {
            log.warn("获取上次预警时间失败: memberPhone={}, province={}, channel={}", memberPhone, province, channel, e);
            return null;
        }
    }

    /**
     * 检查是否超过预警阈值
     * 
     * @param memberPhone 会员手机号
     * @param province 省份
     * @param channel 渠道
     * @param registerId 注册ID
     * @return 是否超过阈值
     */
    public boolean isThresholdExceeded(String memberPhone, String province, String channel, String registerId) {
        String redisKey = String.format("%s-%s-%s", memberPhone, channel, registerId);
        Integer currentCount = mockRedisService.getOrderCount(redisKey);
        Integer threshold = orderAlertConfig.getThresholds();
        
        return currentCount > threshold;
    }

    /**
     * 获取预警统计信息
     * 
     * @param province 省份
     * @param channel 渠道
     * @return 预警统计信息
     */
    public Map<String, Object> getAlertStatistics(String province, String channel) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<AlertRecordDTO> alerts = mockDatabaseService.getAlertRecordsByProvinceAndChannel(province, channel);
            
            stats.put("totalAlerts", alerts.size());
            stats.put("province", province);
            stats.put("channel", channel);
            stats.put("threshold", orderAlertConfig.getThresholds());
            
            // 按会员分组统计
            Map<String, Long> memberAlertCounts = alerts.stream()
                    .collect(Collectors.groupingBy(AlertRecordDTO::getMemberPhone, Collectors.counting()));
            stats.put("memberAlertCounts", memberAlertCounts);
            
        } catch (Exception e) {
            log.error("获取预警统计信息失败: province={}, channel={}", province, channel, e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}
