package com.picc.java.learn.alert.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单DTO类
 * 用于MyBatis交互和数据库操作
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    /**
     * 订单ID
     */
    private Long orderId;

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
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取Redis Key
     * 格式：会员手机号-渠道-registerId
     * 
     * @return Redis Key
     */
    public String getRedisKey() {
        return memberPhone + "-" + channel + "-" + registerId;
    }

    /**
     * 获取省份渠道组合Key
     * 格式：省份-渠道
     * 
     * @return 省份渠道组合Key
     */
    public String getProvinceChannelKey() {
        return province.toLowerCase() + "-" + channel.toLowerCase();
    }
}
