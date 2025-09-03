package com.picc.java.learn.alert.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 订单预警配置实体类
 * 用于注入YAML配置文件中的预警参数
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "club.alert")
public class OrderAlertConfig {

    /**
     * 省份代码
     */
    private String province;

    /**
     * 阈值配置
     */
    private Integer thresholds;

    /**
     * 时间窗口（小时）
     */
    private Integer timeWindow = 12;

    /**
     * 渠道配置（多个渠道用逗号分隔）
     */
    private String source;

    /**
     * 预警管理员配置
     */
    private List<Manager> managers;

    /**
     * 获取时间窗口（毫秒）
     * 
     * @return 时间窗口毫秒数
     */
    public Long getTimeWindowMillis() {
        return timeWindow * 60 * 60 * 1000L;
    }

    /**
     * 获取时间窗口（秒）
     * 
     * @return 时间窗口秒数
     */
    public Integer getTimeWindowSeconds() {
        return timeWindow * 60 * 60;
    }

    /**
     * 获取渠道列表
     * 
     * @return 渠道列表
     */
    public List<String> getSourceList() {
        if (source == null || source.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(source.split(","));
    }

    /**
     * 预警管理员配置
     */
    @Data
    public static class Manager {
        /**
         * 管理员ID
         */
        private Integer id;

        /**
         * 接收人列表
         */
        private List<Receiver> receiver;
    }

    /**
     * 接收人配置
     */
    @Data
    public static class Receiver {
        /**
         * 手机号
         */
        private String phone;

        /**
         * 姓名
         */
        private String name;

        /**
         * eBan用户ID
         */
        private String eBanUserId;
    }
}
