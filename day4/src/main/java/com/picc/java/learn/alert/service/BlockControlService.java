package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.RiskControlConfig;
import com.picc.java.learn.alert.dto.AlertDimensionDTO;
import com.picc.java.learn.alert.dto.BlockRecordDTO;
import com.picc.java.learn.alert.dto.WeightRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 阻断控制服务
 * 实现渠道阻断和解除逻辑，支持风险管控
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Slf4j
@Service
public class BlockControlService {

    @Autowired
    private RiskControlConfig riskControlConfig;

    /**
     * 预警维度数据存储（模拟数据库表）
     */
    private final Map<String, AlertDimensionDTO> dimensionMap = new ConcurrentHashMap<>();

    /**
     * 权重记录数据存储（模拟数据库表）
     */
    private final Map<String, WeightRecordDTO> weightMap = new ConcurrentHashMap<>();

    /**
     * 阻断记录数据存储（模拟数据库表）
     */
    private final Map<String, BlockRecordDTO> blockMap = new ConcurrentHashMap<>();

    /**
     * 预警维度ID生成器
     */
    private final AtomicLong dimensionIdGenerator = new AtomicLong(1);

    /**
     * 权重记录ID生成器
     */
    private final AtomicLong weightIdGenerator = new AtomicLong(1);

    /**
     * 阻断记录ID生成器
     */
    private final AtomicLong blockIdGenerator = new AtomicLong(1);

    /**
     * 检查渠道是否被阻断
     * 
     * @param alertKey 预警维度key
     * @return 是否被阻断
     */
    public boolean isChannelBlocked(String alertKey) {
        if (!riskControlConfig.isEnabled() || !riskControlConfig.getBlockManagement().isEnabled()) {
            return false;
        }

        AlertDimensionDTO dimension = dimensionMap.get(alertKey);
        if (dimension == null) {
            return false;
        }

        return dimension.isCurrentlyBlocked();
    }

    /**
     * 检查是否需要阻断渠道
     * 
     * @param alertKey 预警维度key
     * @param weightRecord 权重记录
     * @return 是否需要阻断
     */
    public boolean shouldBlockChannel(String alertKey, WeightRecordDTO weightRecord) {
        if (!riskControlConfig.isEnabled() || !riskControlConfig.getBlockManagement().isEnabled()) {
            return false;
        }

        // 检查权重阈值
        if (weightRecord.isBlockThresholdReached(riskControlConfig.getWeight().getBlockThreshold())) {
            log.warn("🚫 权重超标，需要阻断渠道: alertKey={}, 权重={}, 阈值={}", 
                    alertKey, weightRecord.getTotalWeight(), riskControlConfig.getWeight().getBlockThreshold());
            return true;
        }

        // 检查连续预警次数
        if (weightRecord.getContinuousAlertDays() >= riskControlConfig.getContinuousAlert().getBlockThreshold()) {
            log.warn("🚫 连续预警次数超标，需要阻断渠道: alertKey={}, 连续天数={}, 阈值={}", 
                    alertKey, weightRecord.getContinuousAlertDays(), riskControlConfig.getContinuousAlert().getBlockThreshold());
            return true;
        }

        // 检查预警订单数量
        if (weightRecord.getAlertOrderCount() >= riskControlConfig.getOrderCount().getBlockThreshold()) {
            log.warn("🚫 预警订单数量超标，需要阻断渠道: alertKey={}, 订单数量={}, 阈值={}", 
                    alertKey, weightRecord.getAlertOrderCount(), riskControlConfig.getOrderCount().getBlockThreshold());
            return true;
        }

        // 检查连续天数
        if (weightRecord.getAlertDaysInPeriod() >= riskControlConfig.getContinuousDays().getAlertDaysThreshold()) {
            log.warn("🚫 连续天数预警超标，需要阻断渠道: alertKey={}, 预警天数={}, 阈值={}", 
                    alertKey, weightRecord.getAlertDaysInPeriod(), riskControlConfig.getContinuousDays().getAlertDaysThreshold());
            return true;
        }

        return false;
    }

    /**
     * 阻断渠道
     * 
     * @param alertKey 预警维度key
     * @param blockReason 阻断原因
     * @param blockType 阻断类型
     * @param weightRecord 权重记录
     * @return 阻断记录
     */
    public BlockRecordDTO blockChannel(String alertKey, String blockReason, String blockType, WeightRecordDTO weightRecord) {
        try {
            log.warn("🚫 开始阻断渠道: alertKey={}, 原因={}, 类型={}", alertKey, blockReason, blockType);

            // 获取或创建预警维度
            AlertDimensionDTO dimension = getOrCreateDimension(alertKey);
            dimension.setIsBlocked(true);
            dimension.setBlockReason(blockReason);
            dimension.setBlockTime(LocalDateTime.now());
            dimension.setUpdateTime(LocalDateTime.now());

            // 创建阻断记录
            BlockRecordDTO blockRecord = BlockRecordDTO.builder()
                    .blockId(blockIdGenerator.getAndIncrement())
                    .dimensionId(dimension.getDimensionId())
                    .alertKey(alertKey)
                    .blockReason(blockReason)
                    .blockType(blockType)
                    .blockWeight(weightRecord.getTotalWeight())
                    .blockRiskLevel(weightRecord.getRiskLevel())
                    .blockTime(LocalDateTime.now())
                    .blockStatus("BLOCKED")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            // 保存阻断记录
            blockMap.put(alertKey, blockRecord);

            log.warn("🚫 渠道阻断成功: alertKey={}, blockId={}, 权重={}, 风险等级={}", 
                    alertKey, blockRecord.getBlockId(), weightRecord.getTotalWeight(), weightRecord.getRiskLevel());

            return blockRecord;

        } catch (Exception e) {
            log.error("❌ 阻断渠道失败: alertKey={}", alertKey, e);
            return null;
        }
    }

    /**
     * 解除渠道阻断
     * 
     * @param alertKey 预警维度key
     * @param unblockUser 解除人
     * @param unblockUserRole 解除人角色
     * @param unblockReason 解除原因
     * @return 是否解除成功
     */
    public boolean unblockChannel(String alertKey, String unblockUser, String unblockUserRole, String unblockReason) {
        try {
            // 检查权限
            if (!riskControlConfig.getBlockManagement().hasUnblockPermission(unblockUserRole)) {
                log.warn("⚠️ 用户无权限解除阻断: user={}, role={}", unblockUser, unblockUserRole);
                return false;
            }

            log.info("🔓 开始解除渠道阻断: alertKey={}, 解除人={}, 角色={}, 原因={}", 
                    alertKey, unblockUser, unblockUserRole, unblockReason);

            // 获取预警维度
            AlertDimensionDTO dimension = dimensionMap.get(alertKey);
            if (dimension == null) {
                log.warn("⚠️ 预警维度不存在: alertKey={}", alertKey);
                return false;
            }

            // 获取阻断记录
            BlockRecordDTO blockRecord = blockMap.get(alertKey);
            if (blockRecord == null || !blockRecord.canUnblock()) {
                log.warn("⚠️ 阻断记录不存在或无法解除: alertKey={}", alertKey);
                return false;
            }

            // 获取权重记录
            WeightRecordDTO weightRecord = weightMap.get(alertKey);
            if (weightRecord == null) {
                log.warn("⚠️ 权重记录不存在: alertKey={}", alertKey);
                return false;
            }

            // 解除阻断
            dimension.setIsBlocked(false);
            dimension.setUnblockTime(LocalDateTime.now());
            dimension.setUnblockUser(unblockUser);
            dimension.setUnblockReason(unblockReason);
            dimension.setUpdateTime(LocalDateTime.now());

            // 更新阻断记录
            blockRecord.unblock(unblockUser, unblockUserRole, unblockReason, 
                               weightRecord.getTotalWeight(), weightRecord.getRiskLevel());

            // 记录解除轨迹
            if (riskControlConfig.getBlockManagement().isLogUnblockTrail()) {
                log.info("📝 阻断解除轨迹记录: alertKey={}, 解除人={}, 角色={}, 原因={}, 解除后权重={}", 
                        alertKey, unblockUser, unblockUserRole, unblockReason, weightRecord.getTotalWeight());
            }

            log.info("🔓 渠道阻断解除成功: alertKey={}, 解除人={}, 解除后权重={}", 
                    alertKey, unblockUser, weightRecord.getTotalWeight());

            return true;

        } catch (Exception e) {
            log.error("❌ 解除渠道阻断失败: alertKey={}", alertKey, e);
            return false;
        }
    }

    /**
     * 获取或创建预警维度
     * 
     * @param alertKey 预警维度key
     * @return 预警维度
     */
    private AlertDimensionDTO getOrCreateDimension(String alertKey) {
        AlertDimensionDTO dimension = dimensionMap.get(alertKey);
        if (dimension == null) {
            // 解析alertKey获取各个字段
            String[] parts = alertKey.split("-");
            if (parts.length == 4) {
                dimension = AlertDimensionDTO.builder()
                        .dimensionId(dimensionIdGenerator.getAndIncrement())
                        .memberPhone(parts[0])
                        .province(parts[1])
                        .channel(parts[2])
                        .registerId(parts[3])
                        .alertKey(alertKey)
                        .isBlocked(false)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                dimensionMap.put(alertKey, dimension);
                log.info("创建新的预警维度: alertKey={}, dimensionId={}", alertKey, dimension.getDimensionId());
            }
        }
        return dimension;
    }

    /**
     * 获取或创建权重记录
     * 
     * @param alertKey 预警维度key
     * @return 权重记录
     */
    public WeightRecordDTO getOrCreateWeightRecord(String alertKey) {
        WeightRecordDTO weightRecord = weightMap.get(alertKey);
        if (weightRecord == null) {
            // 获取预警维度
            AlertDimensionDTO dimension = getOrCreateDimension(alertKey);
            
            weightRecord = WeightRecordDTO.builder()
                    .weightId(weightIdGenerator.getAndIncrement())
                    .dimensionId(dimension.getDimensionId())
                    .alertKey(alertKey)
                    .alertCount(0)
                    .totalWeight(0.0)
                    .riskLevel("LOW")
                    .statisticsDate(LocalDate.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            
            weightMap.put(alertKey, weightRecord);
            log.info("创建新的权重记录: alertKey={}, weightId={}", alertKey, weightRecord.getWeightId());
        }
        return weightRecord;
    }

    /**
     * 获取阻断记录
     * 
     * @param alertKey 预警维度key
     * @return 阻断记录
     */
    public BlockRecordDTO getBlockRecord(String alertKey) {
        return blockMap.get(alertKey);
    }

    /**
     * 获取所有阻断记录
     * 
     * @return 阻断记录列表
     */
    public List<BlockRecordDTO> getAllBlockRecords() {
        return blockMap.values().stream()
                .filter(record -> "BLOCKED".equals(record.getBlockStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有预警维度
     * 
     * @return 预警维度列表
     */
    public List<AlertDimensionDTO> getAllDimensions() {
        return dimensionMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 获取所有权重记录
     * 
     * @return 权重记录列表
     */
    public List<WeightRecordDTO> getAllWeightRecords() {
        return weightMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 清空所有数据（用于测试）
     */
    public void clearAllData() {
        dimensionMap.clear();
        weightMap.clear();
        blockMap.clear();
        dimensionIdGenerator.set(1);
        weightIdGenerator.set(1);
        blockIdGenerator.set(1);
        log.info("清空所有阻断控制数据");
    }
}
