package com.picc.java.learn.alert.vo;

import lombok.Data;

/**
 * 预警查询请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class AlertQueryVO {
    
    private String memberPhone;
    private String province;
    private Integer channel;
    private String registerId;
    private String alertStatus;
    private Integer page = 1;
    private Integer size = 10;
}
