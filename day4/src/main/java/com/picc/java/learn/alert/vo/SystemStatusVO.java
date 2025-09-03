package com.picc.java.learn.alert.vo;

import lombok.Data;

/**
 * 系统状态查询请求VO
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
public class SystemStatusVO {
    
    private Boolean includeDetails = false;
    private Boolean includeMetrics = false;
    private Boolean includeCache = true;
}
