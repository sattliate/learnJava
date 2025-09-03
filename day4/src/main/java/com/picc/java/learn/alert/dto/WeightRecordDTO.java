package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 权重记录DTO类
 * 记录各种风险行为的权重计算，支持风险管控
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightRecordDTO {

    /**
     * 权重记录ID
     */
    private Long weightId;

    /**
     * 预警维度ID
     */
    private Long dimensionId;

    /**
     * 预警维度Key
     */
    private String alertKey;

    /**
     * 预警次数
     */
    private Integer alertCount = 0;

    /**
     * 总权重值
     */
    private Double totalWeight = 0.0;

    /**
     * 风险等级（LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-极高风险）
     */
    private String riskLevel = "LOW";

    /**
     * 身份证修改次数（当天）
     */
    private Integer idCardModifyCount = 0;

    /**
     * 身份证修改权重
     */
    private Double idCardModifyWeight = 0.0;

    /**
     * 手机号修改次数（当天）
     */
    private Integer phoneModifyCount = 0;

    /**
     * 手机号修改权重
     */
    private Double phoneModifyWeight = 0.0;

    /**
     * 注销重注册次数（当天）
     */
    private Integer reRegisterCount = 0;

    /**
     * 注销重注册权重
     */
    private Double reRegisterWeight = 0.0;

    /**
     * 同手机号注册次数（当天）
     */
    private Integer samePhoneRegisterCount = 0;

    /**
     * 同手机号注册权重
     */
    private Double samePhoneRegisterWeight = 0.0;

    /**
     * 特殊产品订单次数
     */
    private Integer specialProductCount = 0;

    /**
     * 特殊产品权重
     */
    private Double specialProductWeight = 0.0;

    /**
     * 无有效保单次数
     */
    private Integer noPolicyCount = 0;

    /**
     * 无有效保单权重
     */
    private Double noPolicyWeight = 0.0;

    /**
     * 连续预警天数
     */
    private Integer continuousAlertDays = 0;

    /**
     * 连续预警权重
     */
    private Double continuousAlertWeight = 0.0;

    /**
     * 预警订单数量（X小时内）
     */
    private Integer alertOrderCount = 0;

    /**
     * 预警订单数量权重
     */
    private Double alertOrderCountWeight = 0.0;

    /**
     * 连续天数中有预警的天数
     */
    private Integer alertDaysInPeriod = 0;

    /**
     * 连续天数权重
     */
    private Double continuousDaysWeight = 0.0;

    /**
     * 最后预警时间
     */
    private LocalDateTime lastAlertTime;

    /**
     * 首次预警时间
     */
    private LocalDateTime firstAlertTime;

    /**
     * 统计日期（用于自然日统计）
     */
    private LocalDate statisticsDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 计算总权重
     * 
     * @return 总权重值
     */
    public Double calculateTotalWeight() {
        double total = 0.0;
        
        total += idCardModifyWeight != null ? idCardModifyWeight : 0.0;
        total += phoneModifyWeight != null ? phoneModifyWeight : 0.0;
        total += reRegisterWeight != null ? reRegisterWeight : 0.0;
        total += samePhoneRegisterWeight != null ? samePhoneRegisterWeight : 0.0;
        total += specialProductWeight != null ? specialProductWeight : 0.0;
        total += noPolicyWeight != null ? noPolicyWeight : 0.0;
        total += continuousAlertWeight != null ? continuousAlertWeight : 0.0;
        total += alertOrderCountWeight != null ? alertOrderCountWeight : 0.0;
        total += continuousDaysWeight != null ? continuousDaysWeight : 0.0;
        
        this.totalWeight = Math.round(total * 100.0) / 100.0; // 保留两位小数
        return this.totalWeight;
    }

    /**
     * 计算风险等级
     * 
     * @return 风险等级
     */
    public String calculateRiskLevel() {
        if (totalWeight == null || totalWeight <= 0) {
            this.riskLevel = "LOW";
            return this.riskLevel;
        }
        
        if (totalWeight <= 20) {
            this.riskLevel = "LOW";           // 低风险：0-20分
        } else if (totalWeight <= 40) {
            this.riskLevel = "MEDIUM";        // 中风险：21-40分
        } else if (totalWeight <= 60) {
            this.riskLevel = "HIGH";          // 高风险：41-60分
        } else {
            this.riskLevel = "CRITICAL";      // 极高风险：61分以上
        }
        
        return this.riskLevel;
    }

    /**
     * 检查是否达到阻断阈值
     * 
     * @param blockThreshold 阻断阈值
     * @return 是否达到阻断阈值
     */
    public boolean isBlockThresholdReached(int blockThreshold) {
        return totalWeight != null && totalWeight >= blockThreshold;
    }

    /**
     * 获取权重详情描述
     * 
     * @return 权重详情描述
     */
    public String getWeightDetailDescription() {
        StringBuilder description = new StringBuilder();
        description.append(String.format("总权重：%.2f，风险等级：%s\n", totalWeight, riskLevel));
        
        if (idCardModifyWeight > 0) {
            description.append(String.format("身份证修改：%d次，权重%.2f\n", idCardModifyCount, idCardModifyWeight));
        }
        
        if (phoneModifyWeight > 0) {
            description.append(String.format("手机号修改：%d次，权重%.2f\n", phoneModifyCount, phoneModifyWeight));
        }
        
        if (reRegisterWeight > 0) {
            description.append(String.format("注销重注册：%d次，权重%.2f\n", reRegisterCount, reRegisterWeight));
        }
        
        if (samePhoneRegisterWeight > 0) {
            description.append(String.format("同手机号注册：%d次，权重%.2f\n", samePhoneRegisterCount, samePhoneRegisterWeight));
        }
        
        if (specialProductWeight > 0) {
            description.append(String.format("特殊产品：%d次，权重%.2f\n", specialProductCount, specialProductWeight));
        }
        
        if (noPolicyWeight > 0) {
            description.append(String.format("无有效保单：%d次，权重%.2f\n", noPolicyCount, noPolicyWeight));
        }
        
        if (continuousAlertWeight > 0) {
            description.append(String.format("连续预警：%d天，权重%.2f\n", continuousAlertDays, continuousAlertWeight));
        }
        
        if (alertOrderCountWeight > 0) {
            description.append(String.format("预警订单数量：%d个，权重%.2f\n", alertOrderCount, alertOrderCountWeight));
        }
        
        if (continuousDaysWeight > 0) {
            description.append(String.format("连续天数预警：%d天，权重%.2f\n", alertDaysInPeriod, continuousDaysWeight));
        }
        
        return description.toString();
    }

    /**
     * 重置当天统计数据
     */
    public void resetDailyCounts() {
        this.idCardModifyCount = 0;
        this.phoneModifyCount = 0;
        this.reRegisterCount = 0;
        this.samePhoneRegisterCount = 0;
        this.statisticsDate = LocalDate.now();
        this.updateTime = LocalDateTime.now();
    }
}
