package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 阻断记录DTO类
 * 记录渠道阻断和解除的轨迹，支持阻断管理
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockRecordDTO {

    /**
     * 阻断记录ID
     */
    private Long blockId;

    /**
     * 预警维度ID
     */
    private Long dimensionId;

    /**
     * 预警维度Key
     */
    private String alertKey;

    /**
     * 阻断原因
     */
    private String blockReason;

    /**
     * 阻断类型（WEIGHT-权重超标，CONTINUOUS_ALERT-连续预警，ORDER_COUNT-订单数量，CONTINUOUS_DAYS-连续天数）
     */
    private String blockType;

    /**
     * 阻断时的权重值
     */
    private Double blockWeight;

    /**
     * 阻断时的风险等级
     */
    private String blockRiskLevel;

    /**
     * 阻断时间
     */
    private LocalDateTime blockTime;

    /**
     * 阻断解除时间
     */
    private LocalDateTime unblockTime;

    /**
     * 阻断解除人
     */
    private String unblockUser;

    /**
     * 阻断解除人角色
     */
    private String unblockUserRole;

    /**
     * 阻断解除原因
     */
    private String unblockReason;

    /**
     * 阻断解除后的权重值
     */
    private Double unblockWeight;

    /**
     * 阻断解除后的风险等级
     */
    private String unblockRiskLevel;

    /**
     * 阻断状态（BLOCKED-已阻断，UNBLOCKED-已解除，EXPIRED-已过期）
     */
    private String blockStatus = "BLOCKED";

    /**
     * 阻断时长（分钟）
     */
    private Long blockDurationMinutes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取阻断状态描述
     * 
     * @return 阻断状态描述
     */
    public String getBlockStatusDescription() {
        switch (blockStatus) {
            case "BLOCKED":
                return "已阻断";
            case "UNBLOCKED":
                return "已解除";
            case "EXPIRED":
                return "已过期";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取阻断类型描述
     * 
     * @return 阻断类型描述
     */
    public String getBlockTypeDescription() {
        switch (blockType) {
            case "WEIGHT":
                return "权重超标";
            case "CONTINUOUS_ALERT":
                return "连续预警";
            case "ORDER_COUNT":
                return "订单数量";
            case "CONTINUOUS_DAYS":
                return "连续天数";
            default:
                return "未知类型";
        }
    }

    /**
     * 计算阻断时长
     * 
     * @return 阻断时长（分钟）
     */
    public Long calculateBlockDuration() {
        if (blockTime == null) {
            return 0L;
        }
        
        LocalDateTime endTime = unblockTime != null ? unblockTime : LocalDateTime.now();
        this.blockDurationMinutes = java.time.Duration.between(blockTime, endTime).toMinutes();
        return this.blockDurationMinutes;
    }

    /**
     * 获取阻断时长描述
     * 
     * @return 阻断时长描述
     */
    public String getBlockDurationDescription() {
        long minutes = calculateBlockDuration();
        if (minutes == 0) {
            return "未阻断";
        }
        
        if (minutes < 60) {
            return minutes + "分钟";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        if (remainingMinutes == 0) {
            return hours + "小时";
        }
        
        return hours + "小时" + remainingMinutes + "分钟";
    }

    /**
     * 检查是否可以解除阻断
     * 
     * @return 是否可以解除阻断
     */
    public boolean canUnblock() {
        return "BLOCKED".equals(blockStatus);
    }

    /**
     * 解除阻断
     * 
     * @param unblockUser 解除人
     * @param unblockUserRole 解除人角色
     * @param unblockReason 解除原因
     * @param unblockWeight 解除后权重
     * @param unblockRiskLevel 解除后风险等级
     */
    public void unblock(String unblockUser, String unblockUserRole, String unblockReason, 
                       Double unblockWeight, String unblockRiskLevel) {
        this.unblockTime = LocalDateTime.now();
        this.unblockUser = unblockUser;
        this.unblockUserRole = unblockUserRole;
        this.unblockReason = unblockReason;
        this.unblockWeight = unblockWeight;
        this.unblockRiskLevel = unblockRiskLevel;
        this.blockStatus = "UNBLOCKED";
        this.updateTime = LocalDateTime.now();
        
        // 计算阻断时长
        calculateBlockDuration();
    }

    /**
     * 获取阻断详情描述
     * 
     * @return 阻断详情描述
     */
    public String getBlockDetailDescription() {
        StringBuilder description = new StringBuilder();
        description.append(String.format("阻断类型：%s\n", getBlockTypeDescription()));
        description.append(String.format("阻断原因：%s\n", blockReason));
        description.append(String.format("阻断时间：%s\n", blockTime));
        description.append(String.format("阻断状态：%s\n", getBlockStatusDescription()));
        
        if (blockWeight != null) {
            description.append(String.format("阻断时权重：%.2f\n", blockWeight));
        }
        
        if (blockRiskLevel != null) {
            description.append(String.format("阻断时风险等级：%s\n", blockRiskLevel));
        }
        
        if (unblockTime != null) {
            description.append(String.format("解除时间：%s\n", unblockTime));
            description.append(String.format("解除人：%s（%s）\n", unblockUser, unblockUserRole));
            description.append(String.format("解除原因：%s\n", unblockReason));
            
            if (unblockWeight != null) {
                description.append(String.format("解除后权重：%.2f\n", unblockWeight));
            }
            
            if (unblockRiskLevel != null) {
                description.append(String.format("解除后风险等级：%s\n", unblockRiskLevel));
            }
        }
        
        description.append(String.format("阻断时长：%s", getBlockDurationDescription()));
        
        return description.toString();
    }
}
