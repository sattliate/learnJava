package com.picc.java.learn.alert.controller;


import com.picc.java.learn.alert.dto.AlertDimensionDTO;
import com.picc.java.learn.alert.dto.BlockRecordDTO;
import com.picc.java.learn.alert.dto.WeightRecordDTO;
import com.picc.java.learn.alert.service.BlockControlService;
import com.picc.java.learn.alert.service.WeightCalculationService;
import com.picc.java.learn.alert.config.RiskControlConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 风险管控控制器
 * 提供API接口来查询预警信息和阻断人员，支持阻断解除功能
 * 
 * @author learn-java
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/risk-control")
public class RiskControlController {

    @Autowired
    private BlockControlService blockControlService;

    @Autowired
    private WeightCalculationService weightCalculationService;

    @Autowired
    private RiskControlConfig riskControlConfig;

    /**
     * 查询所有预警维度信息
     * 
     * @return 预警维度列表
     */
    @PostMapping("/dimensions")
    public ApiResponse<List<AlertDimensionDTO>> getAllDimensions() {
        return executeWithExceptionHandling(
            () -> blockControlService.getAllDimensions(),
            "查询预警维度信息",
            dimensions -> log.info("查询预警维度信息成功: 数量={}", dimensions.size())
        );
    }

    /**
     * 查询所有权重记录
     * 
     * @return 权重记录列表
     */
    @PostMapping("/weight-records")
    public ApiResponse<List<WeightRecordDTO>> getAllWeightRecords() {
        return executeWithExceptionHandling(
            () -> blockControlService.getAllWeightRecords(),
            "查询权重记录",
            weightRecords -> log.info("查询权重记录成功: 数量={}", weightRecords.size())
        );
    }

    /**
     * 查询所有阻断记录
     * 
     * @return 阻断记录列表
     */
    @PostMapping("/block-records")
    public ApiResponse<List<BlockRecordDTO>> getAllBlockRecords() {
        return executeWithExceptionHandling(
            () -> blockControlService.getAllBlockRecords(),
            "查询阻断记录",
            blockRecords -> log.info("查询阻断记录成功: 数量={}", blockRecords.size())
        );
    }

    /**
     * 查询特定预警维度的详细信息
     * 
     * @param request 请求参数
     * @return 预警维度详细信息
     */
    @PostMapping("/dimension-detail")
    public ApiResponse<Map<String, Object>> getDimensionDetail(@RequestBody AlertKeyRequest request) {
        return executeWithExceptionHandling(
            () -> {
                String alertKey = request.getAlertKey();
                
                // 获取预警维度
                AlertDimensionDTO dimension = blockControlService.getAllDimensions().stream()
                        .filter(d -> alertKey.equals(d.getAlertKey()))
                        .findFirst()
                        .orElse(null);
                
                if (dimension == null) {
                    throw new RuntimeException("预警维度不存在: " + alertKey);
                }
                
                // 获取权重记录
                WeightRecordDTO weightRecord = blockControlService.getAllWeightRecords().stream()
                        .filter(w -> alertKey.equals(w.getAlertKey()))
                        .findFirst()
                        .orElse(null);
                
                // 获取阻断记录
                BlockRecordDTO blockRecord = blockControlService.getBlockRecord(alertKey);
                
                // 构建详细信息
                Map<String, Object> detail = new HashMap<>();
                detail.put("dimension", dimension);
                detail.put("weightRecord", weightRecord);
                detail.put("blockRecord", blockRecord);
                
                return detail;
            },
            "查询预警维度详细信息",
            detail -> log.info("查询预警维度详细信息成功: alertKey={}", request.getAlertKey())
        );
    }

    /**
     * 解除渠道阻断
     * 
     * @param request 解除阻断请求
     * @return 解除结果
     */
    @PostMapping("/unblock")
    public ApiResponse<Boolean> unblockChannel(@RequestBody UnblockRequest request) {
        return executeWithExceptionHandling(
            () -> {
                String alertKey = request.getAlertKey();
                String unblockUser = request.getUnblockUser();
                String unblockUserRole = request.getUnblockUserRole();
                String unblockReason = request.getUnblockReason();
                
                log.info("开始解除渠道阻断: alertKey={}, 解除人={}, 角色={}, 原因={}", 
                        alertKey, unblockUser, unblockUserRole, unblockReason);
                
                // 检查权限
                if (!riskControlConfig.getBlockManagement().hasUnblockPermission(unblockUserRole)) {
                    log.warn("用户无权限解除阻断: user={}, role={}", unblockUser, unblockUserRole);
                    throw new RuntimeException("用户无权限解除阻断，需要管理岗位权限");
                }
                
                // 执行解除阻断
                boolean success = blockControlService.unblockChannel(alertKey, unblockUser, unblockUserRole, unblockReason);
                
                if (!success) {
                    throw new RuntimeException("渠道阻断解除失败，请检查预警维度是否存在");
                }
                
                return success;
            },
            "解除渠道阻断",
            success -> log.info("渠道阻断解除成功: alertKey={}, 解除人={}", request.getAlertKey(), request.getUnblockUser())
        );
    }

    /**
     * 获取风险管控配置信息
     * 
     * @return 风险管控配置
     */
    @PostMapping("/config")
    public ApiResponse<RiskControlConfig> getRiskControlConfig() {
        return executeWithExceptionHandling(
            () -> riskControlConfig,
            "查询风险管控配置",
            config -> log.info("查询风险管控配置成功")
        );
    }

    /**
     * 获取风险管控统计信息
     * 
     * @return 风险管控统计信息
     */
    @PostMapping("/statistics")
    public ApiResponse<Map<String, Object>> getRiskControlStatistics() {
        return executeWithExceptionHandling(
            () -> {
                List<AlertDimensionDTO> dimensions = blockControlService.getAllDimensions();
                List<WeightRecordDTO> weightRecords = blockControlService.getAllWeightRecords();
                List<BlockRecordDTO> blockRecords = blockControlService.getAllBlockRecords();
                
                // 统计信息
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("totalDimensions", dimensions.size());
                statistics.put("totalWeightRecords", weightRecords.size());
                statistics.put("totalBlockRecords", blockRecords.size());
                
                // 阻断统计
                long blockedCount = dimensions.stream().filter(AlertDimensionDTO::isCurrentlyBlocked).count();
                statistics.put("currentlyBlocked", blockedCount);
                
                // 风险等级统计
                Map<String, Long> riskLevelStats = weightRecords.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                WeightRecordDTO::getRiskLevel,
                                java.util.stream.Collectors.counting()
                        ));
                statistics.put("riskLevelStats", riskLevelStats);
                
                // 权重分布统计
                long highRiskCount = weightRecords.stream()
                        .filter(w -> w.getTotalWeight() != null && w.getTotalWeight() >= 40)
                        .count();
                statistics.put("highRiskCount", highRiskCount);
                
                return statistics;
            },
            "查询风险管控统计信息",
            statistics -> log.info("查询风险管控统计信息成功")
        );
    }

    /**
     * 清空所有风险管控数据（仅用于测试）
     * 
     * @return 清空结果
     */
    @PostMapping("/clear-all-data")
    public ApiResponse<Boolean> clearAllData() {
        return executeWithExceptionHandling(
            () -> {
                log.warn("清空所有风险管控数据");
                blockControlService.clearAllData();
                return true;
            },
            "清空风险管控数据",
            result -> log.info("所有风险管控数据已清空")
        );
    }

    /**
     * 预警维度Key请求
     */
    public static class AlertKeyRequest {
        private String alertKey;

        public String getAlertKey() {
            return alertKey;
        }

        public void setAlertKey(String alertKey) {
            this.alertKey = alertKey;
        }
    }

    /**
     * 解除阻断请求
     */
    public static class UnblockRequest {
        private String alertKey;
        private String unblockUser;
        private String unblockUserRole;
        private String unblockReason;

        public String getAlertKey() {
            return alertKey;
        }

        public void setAlertKey(String alertKey) {
            this.alertKey = alertKey;
        }

        public String getUnblockUser() {
            return unblockUser;
        }

        public void setUnblockUser(String unblockUser) {
            this.unblockUser = unblockUser;
        }

        public String getUnblockUserRole() {
            return unblockUserRole;
        }

        public void setUnblockUserRole(String unblockUserRole) {
            this.unblockUserRole = unblockUserRole;
        }

        public String getUnblockReason() {
            return unblockReason;
        }

        public void setUnblockReason(String unblockReason) {
            this.unblockReason = unblockReason;
        }
    }

    /**
     * API响应包装类
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, "操作成功", data);
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    /**
     * 通用异常处理方法
     * 
     * @param supplier 业务逻辑执行器
     * @param operation 操作名称
     * @param successCallback 成功回调
     * @return API响应
     */
    private <T> ApiResponse<T> executeWithExceptionHandling(Supplier<T> supplier, String operation, Consumer<T> successCallback) {
        try {
            T result = supplier.get();
            if (successCallback != null) {
                successCallback.accept(result);
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("{}失败", operation, e);
            return ApiResponse.error(operation + "失败: " + e.getMessage());
        }
    }

    /**
     * 通用异常处理方法（无成功回调）
     * 
     * @param supplier 业务逻辑执行器
     * @param operation 操作名称
     * @return API响应
     */
    private <T> ApiResponse<T> executeWithExceptionHandling(Supplier<T> supplier, String operation) {
        return executeWithExceptionHandling(supplier, operation, null);
    }
}
