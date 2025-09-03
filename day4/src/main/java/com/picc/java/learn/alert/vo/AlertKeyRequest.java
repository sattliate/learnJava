package com.picc.java.learn.alert.vo;

import lombok.Data;

/**
 * 预警免打扰状态查询请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class AlertKeyRequest {
    
    /**
     * 预警键值
     */
    private String alertKey;
}
