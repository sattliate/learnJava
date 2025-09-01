package com.picc.java.learn.alert.vo;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 订单VO类
 * 用于接收订单创建请求数据
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    /**
     * 会员手机号
     */
    @NotBlank(message = "会员手机号不能为空")
    private String memberPhone;

    /**
     * 会员姓名
     */
    @NotBlank(message = "会员姓名不能为空")
    private String memberName;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 渠道
     */
    @NotNull
    private Integer channel;

    /**
     * 注册ID
     */
    @NotBlank(message = "注册ID不能为空")
    private String registerId;

    /**
     * 订单金额
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal orderAmount;

    /**
     * 订单状态
     */
    private String orderStatus = "CREATED";
}
