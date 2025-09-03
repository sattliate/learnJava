package com.picc.java.learn.alert.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 批量创建订单请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class BatchCreateOrderVO {
    
    @NotNull(message = "订单数量不能为空")
    @Min(value = 1, message = "订单数量必须大于0")
    private Integer count;
    
    @NotNull(message = "创建间隔不能为空")
    @Min(value = 100, message = "创建间隔必须大于100毫秒")
    private Integer intervalMs;
    
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
