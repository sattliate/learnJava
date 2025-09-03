package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.OrderAlertConfig;
import com.picc.java.learn.alert.dto.AlertRecordDTO;
import com.picc.java.learn.alert.dto.OrderDTO;
import com.picc.java.learn.alert.dto.WeightRecordDTO;
import com.picc.java.learn.alert.dto.BlockRecordDTO;
import com.picc.java.learn.alert.service.MockDatabaseService;
import com.picc.java.learn.alert.service.MockRedisService;
import com.picc.java.learn.alert.service.AlertNotificationService;
import com.picc.java.learn.alert.service.WeightCalculationService;
import com.picc.java.learn.alert.service.BlockControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    private AlertNotificationService alertNotificationService;

    @Autowired
    private WeightCalculationService weightCalculationService;

    @Autowired
    private BlockControlService blockControlService;

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
     * 构建预警维度key
     * 
     * @param orderDTO 订单DTO
     * @return 预警维度key
     */
    private String buildAlertKey(OrderDTO orderDTO) {
        return String.format("%s-%s-%s-%s", 
                orderDTO.getMemberPhone(), 
                orderDTO.getProvince(), 
                orderDTO.getChannel(), 
                orderDTO.getRegisterId());
    }

    /**
     * 执行风险管控逻辑
     * 
     * @param alertKey 预警维度key
     * @param orderDTO 订单DTO
     * @param newAlertCount 新的预警次数
     * @return 权重记录
     */
    private WeightRecordDTO executeRiskControl(String alertKey, OrderDTO orderDTO, int newAlertCount) {
        // 1. 获取或创建权重记录
        WeightRecordDTO weightRecord = blockControlService.getOrCreateWeightRecord(alertKey);
        
        // 2. 重置当天统计数据（如果需要）
        weightCalculationService.resetDailyCountsIfNeeded(weightRecord);
        
        // 3. 计算各种风险权重
        calculateRiskWeights(weightRecord, orderDTO);
        
        // 4. 计算总权重和风险等级
        double totalWeight = weightCalculationService.calculateTotalWeight(weightRecord);
        weightRecord.setAlertCount(newAlertCount);
        weightRecord.setLastAlertTime(LocalDateTime.now());
        if (weightRecord.getFirstAlertTime() == null) {
            weightRecord.setFirstAlertTime(LocalDateTime.now());
        }
        
        // 5. 检查是否需要阻断渠道
        checkAndBlockChannel(alertKey, weightRecord, totalWeight);
        
        log.info("风险管控完成: alertKey={}, 总权重={}, 风险等级={}, 是否阻断={}", 
                alertKey, totalWeight, weightRecord.getRiskLevel(), 
                blockControlService.isChannelBlocked(alertKey));
        
        return weightRecord;
    }

    /**
     * 检查并阻断渠道
     * 
     * @param alertKey 预警维度key
     * @param weightRecord 权重记录
     * @param totalWeight 总权重
     */
    private void checkAndBlockChannel(String alertKey, WeightRecordDTO weightRecord, double totalWeight) {
        if (blockControlService.shouldBlockChannel(alertKey, weightRecord)) {
            String blockReason = String.format("风险权重超标: %.2f分", totalWeight);
            String blockType = "WEIGHT";
            
            if (weightRecord.getContinuousAlertDays() >= 3) {
                blockType = "CONTINUOUS_ALERT";
                blockReason = String.format("连续预警%d天", weightRecord.getContinuousAlertDays());
            } else if (weightRecord.getAlertOrderCount() >= 50) {
                blockType = "ORDER_COUNT";
                blockReason = String.format("预警订单数量%d个", weightRecord.getAlertOrderCount());
            }
            
            BlockRecordDTO blockRecord = blockControlService.blockChannel(alertKey, blockReason, blockType, weightRecord);
            if (blockRecord != null) {
                log.warn("🚫 渠道已被阻断: alertKey={}, 原因={}, 权重={}", alertKey, blockReason, totalWeight);
            }
        }
    }

    /**
     * 构建预警记录
     * 
     * @param orderDTO 订单DTO
     * @param currentCount 当前订单数量
     * @param threshold 阈值
     * @param newAlertCount 新的预警次数
     * @param lastAlertTime 上次预警时间
     * @return 预警记录
     */
    private AlertRecordDTO buildAlertRecord(OrderDTO orderDTO, Integer currentCount, Integer threshold, 
                                          int newAlertCount, LocalDateTime lastAlertTime) {
        return AlertRecordDTO.builder()
                .memberPhone(orderDTO.getMemberPhone())
                .memberName(orderDTO.getMemberName())
                .province(orderDTO.getProvince())
                .channel(orderDTO.getChannel())
                .registerId(orderDTO.getRegisterId())
                .currentOrderCount(currentCount)
                .threshold(threshold)
                .alertTime(LocalDateTime.now())
                .alertStatus("PENDING")
                .alertSpeed(0.0)
                .timeIntervalMinutes(0L)
                .lastAlertTime(lastAlertTime)
                .alertCount(newAlertCount)
                .build();
    }

    /**
     * 创建简化的预警记录（异常情况下使用）
     * 
     * @param orderDTO 订单DTO
     * @param currentCount 当前订单数量
     * @param threshold 阈值
     */
    private void createSimpleAlertRecord(OrderDTO orderDTO, Integer currentCount, Integer threshold) {
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
            String alertKey = buildAlertKey(orderDTO);
            int newAlertCount = getCurrentAlertCount(alertKey) + 1;
            LocalDateTime lastAlertTime = getLastAlertTime(orderDTO.getMemberPhone(), orderDTO.getProvince(), orderDTO.getChannel());
            
            // 执行风险管控
            WeightRecordDTO weightRecord = executeRiskControl(alertKey, orderDTO, newAlertCount);
            
            // 创建并保存预警记录
            AlertRecordDTO alertRecord = buildAlertRecord(orderDTO, currentCount, threshold, newAlertCount, lastAlertTime);
            AlertRecordDTO savedAlert = mockDatabaseService.saveAlertRecord(alertRecord);
            
            log.info("🚨 预警记录创建成功: alertId={}, memberPhone={}, province={}, channel={}, currentCount={}, threshold={}", 
                    savedAlert.getAlertId(), savedAlert.getMemberPhone(), savedAlert.getProvince(), 
                    savedAlert.getChannel(), savedAlert.getCurrentOrderCount(), savedAlert.getThreshold());

            // 发送预警通知
            alertNotificationService.sendAlertNotification(savedAlert);
            
        } catch (Exception e) {
            log.error("创建预警记录失败: memberPhone={}, province={}, channel={}", 
                    orderDTO.getMemberPhone(), orderDTO.getProvince(), orderDTO.getChannel(), e);
            
            // 创建简化的预警记录
            createSimpleAlertRecord(orderDTO, currentCount, threshold);
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
     * 计算风险权重
     * 这里简化处理，实际项目中需要从其他系统获取真实数据
     * 
     * @param weightRecord 权重记录
     * @param orderDTO 订单DTO
     */
    private void calculateRiskWeights(WeightRecordDTO weightRecord, OrderDTO orderDTO) {
        try {
            // 1. 计算身份证修改权重（模拟数据）
            // 实际项目中需要查询身份证修改记录
            int idCardModifyCount = getRandomCount(0, 5);
            weightCalculationService.calculateIdCardModifyWeight(weightRecord, idCardModifyCount);
            
            // 2. 计算手机号修改权重（模拟数据）
            // 实际项目中需要查询手机号修改记录
            int phoneModifyCount = getRandomCount(0, 5);
            weightCalculationService.calculatePhoneModifyWeight(weightRecord, phoneModifyCount);
            
            // 3. 计算注销重注册权重（模拟数据）
            // 实际项目中需要查询注销重注册记录
            int reRegisterCount = getRandomCount(0, 3);
            weightCalculationService.calculateReRegisterWeight(weightRecord, reRegisterCount);
            
            // 4. 计算同手机号注册权重（模拟数据）
            // 实际项目中需要查询同手机号注册记录
            int samePhoneRegisterCount = getRandomCount(0, 5);
            weightCalculationService.calculateSamePhoneRegisterWeight(weightRecord, samePhoneRegisterCount);
            
            // 5. 计算特殊产品权重
            // 这里使用订单ID作为产品名称的模拟
            String productName = "订单" + orderDTO.getOrderId();
            weightCalculationService.calculateSpecialProductWeight(weightRecord, productName);
            
            // 6. 计算无有效保单权重（模拟数据）
            // 实际项目中需要查询保单信息
            boolean hasValidPolicy = Math.random() > 0.3; // 70%的概率有有效保单
            weightCalculationService.calculateNoPolicyWeight(weightRecord, hasValidPolicy);
            
            // 7. 计算连续预警权重
            // 这里使用预警次数作为连续天数的模拟
            int continuousDays = Math.min(weightRecord.getAlertCount(), 7);
            weightCalculationService.calculateContinuousAlertWeight(weightRecord, continuousDays);
            
            // 8. 计算预警订单数量权重
            // 使用当前订单数量
            weightCalculationService.calculateAlertOrderCountWeight(weightRecord, orderDTO.getOrderAmount().intValue());
            
            // 9. 计算连续天数权重（模拟数据）
            // 实际项目中需要查询连续天数的预警记录
            int alertDaysInPeriod = getRandomCount(0, 5);
            weightCalculationService.calculateContinuousDaysWeight(weightRecord, alertDaysInPeriod);
            
            log.debug("风险权重计算完成: alertKey={}, 身份证修改={}, 手机号修改={}, 注销重注册={}, 同手机号注册={}, 特殊产品={}, 无有效保单={}, 连续预警={}, 预警订单数量={}, 连续天数={}", 
                    weightRecord.getAlertKey(),
                    weightRecord.getIdCardModifyCount(),
                    weightRecord.getPhoneModifyCount(),
                    weightRecord.getReRegisterCount(),
                    weightRecord.getSamePhoneRegisterCount(),
                    weightRecord.getSpecialProductCount(),
                    weightRecord.getNoPolicyCount(),
                    weightRecord.getContinuousAlertDays(),
                    weightRecord.getAlertOrderCount(),
                    weightRecord.getAlertDaysInPeriod());
                    
        } catch (Exception e) {
            log.error("计算风险权重失败: alertKey={}", weightRecord.getAlertKey(), e);
        }
    }

    /**
     * 获取当前预警次数
     * 
     * @param alertKey 预警维度key
     * @return 当前预警次数
     */
    private int getCurrentAlertCount(String alertKey) {
        try {
            // 从数据库查询该预警维度的预警记录数量
            List<AlertRecordDTO> alerts = mockDatabaseService.getAlertRecordsByAlertKey(alertKey);
            return alerts.size();
        } catch (Exception e) {
            log.warn("获取预警次数失败: alertKey={}", alertKey, e);
            return 0;
        }
    }

    /**
     * 获取随机数量（用于模拟数据）
     * 
     * @param min 最小值
     * @param max 最大值
     * @return 随机数量
     */
    private int getRandomCount(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

}
