package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预警记录DTO类
 * 用于存储预警信息到数据库
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecordDTO {

    /**
     * 预警记录ID
     */
    private Long alertId;

    /**
     * 会员手机号
     */
    private String memberPhone;

    /**
     * 会员姓名
     */
    private String memberName;

    /**
     * 省份
     */
    private String province;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 注册ID
     */
    private String registerId;

    /**
     * 当前订单数量
     */
    private Integer currentOrderCount;

    /**
     * 阈值
     */
    private Integer threshold;

    /**
     * 预警时间
     */
    private LocalDateTime alertTime;

    /**
     * 预警状态（PENDING-待处理，PROCESSED-已处理）
     */
    private String alertStatus;

    /**
     * 预警描述
     */
    private String alertDescription;

    /**
     * 预警速度（订单数量/时间间隔）
     */
    private Double alertSpeed;

    /**
     * 时间间隔（分钟）
     */
    private Long timeIntervalMinutes;

    /**
     * 上次预警时间
     */
    private LocalDateTime lastAlertTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 预警次数（第几次预警）
     */
    private Integer alertCount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

        /**
     * 生成预警描述
     *
     * @return 预警描述
     */
    public String generateAlertDescription() {
        StringBuilder description = new StringBuilder();
        
        // 添加预警次数信息
        if (alertCount != null && alertCount > 0) {
            description.append(String.format("第%d次预警：", alertCount));
        }
        
        description.append(String.format("会员 %s 在 %s 省份的 %s 渠道订单数量 %d 超过阈值 %d",
                memberName, province, channel, currentOrderCount, threshold));
        
        // 添加速度信息
        if (alertSpeed != null && timeIntervalMinutes != null) {
            description.append(String.format("，预警速度 %.2f 订单/分钟（时间间隔 %d 分钟）",
                    alertSpeed, timeIntervalMinutes));
        }
        
        return description.toString();
    }
}
