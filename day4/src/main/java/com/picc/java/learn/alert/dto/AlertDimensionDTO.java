package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预警维度DTO类
 * 将省份、渠道、电话号码抽离为独立字段，支持风险管控
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDimensionDTO {

    /**
     * 预警维度ID
     */
    private Long dimensionId;

    /**
     * 会员手机号
     */
    private String memberPhone;

    /**
     * 省份代码
     */
    private String province;

    /**
     * 渠道代码
     */
    private String channel;

    /**
     * 注册ID
     */
    private String registerId;

    /**
     * 预警维度Key（格式：memberPhone-province-channel-registerId）
     */
    private String alertKey;

    /**
     * 是否被阻断
     */
    private Boolean isBlocked = false;

    /**
     * 阻断原因
     */
    private String blockReason;

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
     * 阻断解除原因
     */
    private String unblockReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 生成预警维度Key
     * 
     * @return 预警维度Key
     */
    public String generateAlertKey() {
        if (memberPhone == null || province == null || channel == null || registerId == null) {
            return null;
        }
        return String.format("%s-%s-%s-%s", memberPhone, province, channel, registerId);
    }

    /**
     * 检查是否被阻断
     * 
     * @return 是否被阻断
     */
    public boolean isCurrentlyBlocked() {
        return Boolean.TRUE.equals(isBlocked) && blockTime != null && unblockTime == null;
    }

    /**
     * 获取阻断状态描述
     * 
     * @return 阻断状态描述
     */
    public String getBlockStatusDescription() {
        if (!Boolean.TRUE.equals(isBlocked)) {
            return "正常";
        }
        
        if (unblockTime != null) {
            return String.format("已解除阻断（%s 解除，原因：%s）", 
                    unblockTime, unblockReason != null ? unblockReason : "未说明");
        }
        
        if (blockTime != null) {
            return String.format("已阻断（%s 阻断，原因：%s）", 
                    blockTime, blockReason != null ? blockReason : "未说明");
        }
        
        return "阻断状态异常";
    }

    /**
     * 获取阻断时长（分钟）
     * 
     * @return 阻断时长，如果未阻断或已解除返回0
     */
    public long getBlockDurationMinutes() {
        if (!Boolean.TRUE.equals(isBlocked) || blockTime == null) {
            return 0;
        }
        
        LocalDateTime endTime = unblockTime != null ? unblockTime : LocalDateTime.now();
        return java.time.Duration.between(blockTime, endTime).toMinutes();
    }

    /**
     * 获取阻断时长描述
     * 
     * @return 阻断时长描述
     */
    public String getBlockDurationDescription() {
        long minutes = getBlockDurationMinutes();
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
}
