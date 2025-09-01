package com.picc.java.learn.alert.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 多时间窗口速度监控请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class MultiTimeWindowSpeedVO {
    
    @NotBlank(message = "会员手机号不能为空")
    private String memberPhone;
    
    @NotBlank(message = "省份代码不能为空")
    private String province;
    
    @NotNull(message = "渠道不能为空")
    private Integer channel;
    
    @NotBlank(message = "注册ID不能为空")
    private String registerId;
    
    @NotBlank(message = "时间窗口不能为空")
    private String timeWindows; // 逗号分隔的时间窗口，如 "1,6,12,24"
}
