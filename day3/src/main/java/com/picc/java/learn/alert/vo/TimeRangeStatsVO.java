package com.picc.java.learn.alert.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 时间范围统计请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class TimeRangeStatsVO {
    
    @NotBlank(message = "会员手机号不能为空")
    private String memberPhone;
    
    @NotBlank(message = "省份代码不能为空")
    private String province;
    
    @NotNull(message = "渠道不能为空")
    private Integer channel;
    
    @NotBlank(message = "注册ID不能为空")
    private String registerId;
    
    @NotNull(message = "开始时间不能为空")
    private Long startTime;
    
    @NotNull(message = "结束时间不能为空")
    private Long endTime;
}
