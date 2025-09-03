package com.picc.java.learn.alert.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 风险管控配置实体类
 * 用于注入YAML配置文件中的风险管控参数
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "club.alert.risk-control")
public class RiskControlConfig {

    /**
     * 是否启用风险管控
     */
    private boolean enabled = true;

    /**
     * 权重阈值配置
     */
    private Weight weight = new Weight();

    /**
     * 身份证修改风险配置
     */
    private RiskRule idCardModify = new RiskRule();

    /**
     * 手机号修改风险配置
     */
    private RiskRule phoneModify = new RiskRule();

    /**
     * 注销重注册风险配置
     */
    private RiskRule reRegister = new RiskRule();

    /**
     * 同手机号注册风险配置
     */
    private RiskRule samePhoneRegister = new RiskRule();

    /**
     * 特殊产品风险配置
     */
    private SpecialProduct specialProduct = new SpecialProduct();

    /**
     * 无有效保单风险配置
     */
    private NoPolicy noPolicy = new NoPolicy();

    /**
     * 连续预警阻断配置
     */
    private ContinuousAlert continuousAlert = new ContinuousAlert();

    /**
     * 订单数量阻断配置
     */
    private OrderCount orderCount = new OrderCount();

    /**
     * 连续天数阻断配置
     */
    private ContinuousDays continuousDays = new ContinuousDays();

    /**
     * 阻断管理配置
     */
    private BlockManagement blockManagement = new BlockManagement();

    /**
     * 权重阈值配置
     */
    @Data
    public static class Weight {
        /**
         * 阻断阈值（超过此值将阻断渠道）
         */
        private int blockThreshold = 60;

        /**
         * 高风险阈值
         */
        private int highRiskThreshold = 40;

        /**
         * 中风险阈值
         */
        private int mediumRiskThreshold = 20;

        /**
         * 低风险阈值
         */
        private int lowRiskThreshold = 10;
    }

    /**
     * 风险规则配置
     */
    @Data
    public static class RiskRule {
        /**
         * 当天超过此次数开始累加权重
         */
        private int dailyThreshold = 3;

        /**
         * 超过阈值后每次累加权重
         */
        private int weightIncrement = 10;

        /**
         * 首次超过阈值累加权重
         */
        private int firstExceedWeight = 30;
    }

    /**
     * 特殊产品风险配置
     */
    @Data
    public static class SpecialProduct {
        /**
         * 每次订单累加权重
         */
        private int weightPerOrder = 2;

        /**
         * 特殊产品关键词列表
         */
        private List<String> keywords = Stream.of("红包", "停车券", "代金券", "话费", "肯德基", "麦当劳", "奈雪", "喜茶", "霸王茶姬").collect(Collectors.toList());

        /**
         * 检查产品名称是否包含特殊关键词
         * 
         * @param productName 产品名称
         * @return 是否包含特殊关键词
         */
        public boolean containsSpecialKeyword(String productName) {
            if (productName == null || productName.trim().isEmpty()) {
                return false;
            }
            return keywords.stream().anyMatch(keyword -> productName.contains(keyword));
        }
    }

    /**
     * 无有效保单风险配置
     */
    @Data
    public static class NoPolicy {
        /**
         * 每次累加权重
         */
        private int weightPerIncident = 5;
    }

    /**
     * 连续预警阻断配置
     */
    @Data
    public static class ContinuousAlert {
        /**
         * 连续预警超过此次数将阻断渠道
         */
        private int blockThreshold = 3;
    }

    /**
     * 订单数量阻断配置
     */
    @Data
    public static class OrderCount {
        /**
         * 预警订单数量超过此值将阻断渠道
         */
        private int blockThreshold = 50;

        /**
         * 统计时间窗口（小时）
         */
        private int timeWindow = 12;
    }

    /**
     * 连续天数阻断配置
     */
    @Data
    public static class ContinuousDays {
        /**
         * 连续统计天数
         */
        private int totalDays = 5;

        /**
         * 有预警的天数阈值
         */
        private int alertDaysThreshold = 3;
    }

    /**
     * 阻断管理配置
     */
    @Data
    public static class BlockManagement {
        /**
         * 是否启用阻断功能
         */
        private boolean enabled = true;

        /**
         * 阻断解除权限角色（管理岗位）
         */
        private List<String> unblockRoles = Arrays.asList("ADMIN", "MANAGER", "SUPERVISOR");

        /**
         * 是否记录解除轨迹
         */
        private boolean logUnblockTrail = true;

        /**
         * 检查用户是否有解除阻断权限
         * 
         * @param userRole 用户角色
         * @return 是否有权限
         */
        public boolean hasUnblockPermission(String userRole) {
            return unblockRoles.contains(userRole);
        }
    }
}
