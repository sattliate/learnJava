package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.RiskControlConfig;
import com.picc.java.learn.alert.dto.WeightRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.BiConsumer;

/**
 * 权重计算服务
 * 实现各种风险行为的权重计算逻辑
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Slf4j
@Service
public class WeightCalculationService {

    @Autowired
    private RiskControlConfig riskControlConfig;

    /**
     * 计算身份证修改权重
     * 当天超过3次，累加权重+30，以后每多修改一次累加权重+10
     * 
     * @param weightRecord 权重记录
     * @param modifyCount 修改次数
     * @return 计算后的权重值
     */
    public Double calculateIdCardModifyWeight(WeightRecordDTO weightRecord, int modifyCount) {
        return calculateRiskWeight(weightRecord, modifyCount, 
                riskControlConfig.getIdCardModify(),
                (record, count) -> {
                    record.setIdCardModifyCount(count);
                    record.setIdCardModifyWeight(calculateWeight(count, riskControlConfig.getIdCardModify()));
                },
                "身份证修改");
    }

    /**
     * 计算手机号修改权重
     * 当天超过3次，累加权重+30，以后每多修改一次累加权重+10
     * 
     * @param weightRecord 权重记录
     * @param modifyCount 修改次数
     * @return 计算后的权重值
     */
    public Double calculatePhoneModifyWeight(WeightRecordDTO weightRecord, int modifyCount) {
        return calculateRiskWeight(weightRecord, modifyCount, 
                riskControlConfig.getPhoneModify(),
                (record, count) -> {
                    record.setPhoneModifyCount(count);
                    record.setPhoneModifyWeight(calculateWeight(count, riskControlConfig.getPhoneModify()));
                },
                "手机号修改");
    }

    /**
     * 计算注销重注册权重
     * 当天超过2次，累加权重+30，以后每多修改一次累加权重+10
     * 
     * @param weightRecord 权重记录
     * @param reRegisterCount 重注册次数
     * @return 计算后的权重值
     */
    public Double calculateReRegisterWeight(WeightRecordDTO weightRecord, int reRegisterCount) {
        return calculateRiskWeight(weightRecord, reRegisterCount, 
                riskControlConfig.getReRegister(),
                (record, count) -> {
                    record.setReRegisterCount(count);
                    record.setReRegisterWeight(calculateWeight(count, riskControlConfig.getReRegister()));
                },
                "注销重注册");
    }

    /**
     * 计算同手机号注册权重
     * 当天超过3次，累加权重+10，以后每多修改一次累加权重+10
     * 
     * @param weightRecord 权重记录
     * @param registerCount 注册次数
     * @return 计算后的权重值
     */
    public Double calculateSamePhoneRegisterWeight(WeightRecordDTO weightRecord, int registerCount) {
        return calculateRiskWeight(weightRecord, registerCount, 
                riskControlConfig.getSamePhoneRegister(),
                (record, count) -> {
                    record.setSamePhoneRegisterCount(count);
                    record.setSamePhoneRegisterWeight(calculateWeight(count, riskControlConfig.getSamePhoneRegister()));
                },
                "同手机号注册");
    }

    /**
     * 计算特殊产品权重
     * 每发一次订单累加权重+2
     * 
     * @param weightRecord 权重记录
     * @param productName 产品名称
     * @return 计算后的权重值
     */
    public Double calculateSpecialProductWeight(WeightRecordDTO weightRecord, String productName) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        RiskControlConfig.SpecialProduct specialProduct = riskControlConfig.getSpecialProduct();
        
        if (specialProduct.containsSpecialKeyword(productName)) {
            double weight = specialProduct.getWeightPerOrder();
            weightRecord.setSpecialProductCount(weightRecord.getSpecialProductCount() + 1);
            weightRecord.setSpecialProductWeight(weightRecord.getSpecialProductWeight() + weight);
            
            log.info("特殊产品权重计算: 产品名称={}, 权重增量={}, 累计权重={}", 
                    productName, weight, weightRecord.getSpecialProductWeight());
            
            return weightRecord.getSpecialProductWeight();
        }
        
        return weightRecord.getSpecialProductWeight();
    }

    /**
     * 计算无有效保单权重
     * 每发一次订单累加权重+5
     * 
     * @param weightRecord 权重记录
     * @param hasValidPolicy 是否有有效保单
     * @return 计算后的权重值
     */
    public Double calculateNoPolicyWeight(WeightRecordDTO weightRecord, boolean hasValidPolicy) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        if (!hasValidPolicy) {
            double weight = riskControlConfig.getNoPolicy().getWeightPerIncident();
            weightRecord.setNoPolicyCount(weightRecord.getNoPolicyCount() + 1);
            weightRecord.setNoPolicyWeight(weightRecord.getNoPolicyWeight() + weight);
            
            log.info("无有效保单权重计算: 权重增量={}, 累计权重={}", 
                    weight, weightRecord.getNoPolicyWeight());
            
            return weightRecord.getNoPolicyWeight();
        }
        
        return weightRecord.getNoPolicyWeight();
    }

    /**
     * 计算连续预警权重
     * 
     * @param weightRecord 权重记录
     * @param continuousDays 连续预警天数
     * @return 计算后的权重值
     */
    public Double calculateContinuousAlertWeight(WeightRecordDTO weightRecord, int continuousDays) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        // 连续预警天数越多，权重越高
        double weight = continuousDays * 5.0; // 每天+5分
        
        weightRecord.setContinuousAlertDays(continuousDays);
        weightRecord.setContinuousAlertWeight(weight);
        
        log.info("连续预警权重计算: 连续天数={}, 权重={}", continuousDays, weight);
        
        return weight;
    }

    /**
     * 计算预警订单数量权重
     * 
     * @param weightRecord 权重记录
     * @param alertOrderCount 预警订单数量
     * @return 计算后的权重值
     */
    public Double calculateAlertOrderCountWeight(WeightRecordDTO weightRecord, int alertOrderCount) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        // 预警订单数量越多，权重越高
        double weight = alertOrderCount * 0.5; // 每个订单+0.5分
        
        weightRecord.setAlertOrderCount(alertOrderCount);
        weightRecord.setAlertOrderCountWeight(weight);
        
        log.info("预警订单数量权重计算: 订单数量={}, 权重={}", alertOrderCount, weight);
        
        return weight;
    }

    /**
     * 计算连续天数权重
     * 
     * @param weightRecord 权重记录
     * @param alertDaysInPeriod 连续天数中有预警的天数
     * @return 计算后的权重值
     */
    public Double calculateContinuousDaysWeight(WeightRecordDTO weightRecord, int alertDaysInPeriod) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        // 有预警的天数越多，权重越高
        double weight = alertDaysInPeriod * 3.0; // 每天+3分
        
        weightRecord.setAlertDaysInPeriod(alertDaysInPeriod);
        weightRecord.setContinuousDaysWeight(weight);
        
        log.info("连续天数权重计算: 预警天数={}, 权重={}", alertDaysInPeriod, weight);
        
        return weight;
    }

    /**
     * 计算总权重
     * 
     * @param weightRecord 权重记录
     * @return 总权重值
     */
    public Double calculateTotalWeight(WeightRecordDTO weightRecord) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        double totalWeight = weightRecord.calculateTotalWeight();
        String riskLevel = weightRecord.calculateRiskLevel();
        
        log.info("总权重计算完成: 总权重={}, 风险等级={}", totalWeight, riskLevel);
        
        return totalWeight;
    }

    /**
     * 检查是否需要重置当天统计数据
     * 
     * @param weightRecord 权重记录
     * @return 是否需要重置
     */
    public boolean shouldResetDailyCounts(WeightRecordDTO weightRecord) {
        if (weightRecord.getStatisticsDate() == null) {
            return true;
        }
        
        LocalDate today = LocalDate.now();
        return !today.equals(weightRecord.getStatisticsDate());
    }

    /**
     * 重置当天统计数据
     * 
     * @param weightRecord 权重记录
     */
    public void resetDailyCountsIfNeeded(WeightRecordDTO weightRecord) {
        if (shouldResetDailyCounts(weightRecord)) {
            weightRecord.resetDailyCounts();
            log.info("重置当天统计数据: alertKey={}", weightRecord.getAlertKey());
        }
    }

    /**
     * 通用风险权重计算方法
     * 
     * @param weightRecord 权重记录
     * @param count 风险行为次数
     * @param rule 风险规则配置
     * @param setter 设置权重记录字段的lambda表达式
     * @param riskType 风险类型名称（用于日志）
     * @return 计算后的权重值
     */
    private Double calculateRiskWeight(WeightRecordDTO weightRecord, int count, 
                                     RiskControlConfig.RiskRule rule,
                                     BiConsumer<WeightRecordDTO, Integer> setter,
                                     String riskType) {
        if (!riskControlConfig.isEnabled()) {
            return 0.0;
        }

        double weight = calculateWeight(count, rule);
        setter.accept(weightRecord, count);
        
        if (weight > 0) {
            log.info("{}权重计算: 次数={}, 阈值={}, 权重={}", 
                    riskType, count, rule.getDailyThreshold(), weight);
        }
        
        return weight;
    }

    /**
     * 计算权重值
     * 
     * @param count 风险行为次数
     * @param rule 风险规则配置
     * @return 权重值
     */
    private double calculateWeight(int count, RiskControlConfig.RiskRule rule) {
        int dailyThreshold = rule.getDailyThreshold();
        int firstExceedWeight = rule.getFirstExceedWeight();
        int weightIncrement = rule.getWeightIncrement();

        if (count <= dailyThreshold) {
            return 0.0;
        }

        // 超过阈值，首次累加权重
        double weight = firstExceedWeight;
        
        // 超过阈值的次数累加权重
        int exceedCount = count - dailyThreshold;
        weight += exceedCount * weightIncrement;
        
        return weight;
    }
}
