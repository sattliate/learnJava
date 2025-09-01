package com.picc.java.learn.alert.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 创建订单请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class CreateOrderVO {
    
    @NotBlank(message = "会员手机号不能为空")
    private String memberPhone;
    
    @NotBlank(message = "会员姓名不能为空")
    private String memberName;
    
    @NotBlank(message = "省份代码不能为空")
    private String province;
    
    @NotNull(message = "渠道不能为空")
    private Integer channel;
    
    @NotBlank(message = "注册ID不能为空")
    private String registerId;
    
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal amount;
    
    @NotBlank(message = "商品名称不能为空")
    private String productName;
}
