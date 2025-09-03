package com.picc.java.learn.alert.controller;

import com.picc.java.learn.alert.service.MockRedisService;
import com.picc.java.learn.alert.vo.*;
import com.picc.java.learn.alert.common.ApiResponse;
import com.picc.java.learn.alert.dto.AlertRecordDTO;
import com.picc.java.learn.alert.dto.OrderDTO;
import com.picc.java.learn.alert.dto.OrderSpeedInfoDTO;
import com.picc.java.learn.alert.dto.OrderTimeRangeStatsDTO;
import com.picc.java.learn.alert.service.AlertQuietModeService;
import com.picc.java.learn.alert.service.MockDatabaseService;

import com.picc.java.learn.alert.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 订单API控制器
 * 提供订单创建和预警查询接口
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlertQuietModeService alertQuietModeService;

    @Autowired
    private MockDatabaseService mockDatabaseService;
    @Autowired
    MockRedisService mockRedisService;

    /**
     * 创建订单
     * POST /api/orders
     * 
     * @param request 创建订单请求
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<OrderDTO> createOrder(@Valid @RequestBody CreateOrderVO request) {
        try {
            log.info("接收到创建订单请求: memberPhone={}, province={}, channel={}", 
                    request.getMemberPhone(), request.getProvince(), request.getChannel());

            // 转换为OrderVO
            OrderVO orderVO = new OrderVO();
            orderVO.setMemberPhone(request.getMemberPhone());
            orderVO.setMemberName(request.getMemberName());
            orderVO.setProvince(request.getProvince());
            orderVO.setChannel(request.getChannel());
            orderVO.setRegisterId(request.getRegisterId());
            orderVO.setOrderAmount(request.getAmount());
            orderVO.setOrderStatus("CREATED");

            // 创建订单
            OrderDTO createdOrder = orderService.createOrder(orderVO);

            log.info("订单创建成功: orderId={}", createdOrder.getOrderId());
            return ApiResponse.success(createdOrder, "订单创建成功");

        } catch (Exception e) {
            log.error("创建订单失败", e);
            return ApiResponse.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 根据会员手机号查询订单列表
     * POST /api/orders/member
     * 
     * @param request 查询请求
     * @return 订单列表
     */
    @PostMapping("/member")
    public ApiResponse<List<OrderDTO>> getOrdersByMemberPhone(@Valid @RequestBody OrderQueryVO request) {
        try {
            log.info("查询会员订单: memberPhone={}", request.getMemberPhone());

            List<OrderDTO> orders = orderService.getOrdersByMemberPhone(request.getMemberPhone());

            return ApiResponse.success(orders, "查询成功");

        } catch (Exception e) {
            log.error("查询会员订单失败: memberPhone={}", request.getMemberPhone(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据省份和渠道查询订单列表
     * POST /api/orders/search
     * 
     * @param request 搜索请求
     * @return 订单列表
     */
    @PostMapping("/search")
    public ApiResponse<List<OrderDTO>> searchOrders(@Valid @RequestBody OrderQueryVO request) {
        try {
            log.info("搜索订单: province={}, channel={}", request.getProvince(), request.getChannel());

            List<OrderDTO> orders = orderService.getOrdersByProvinceAndChannel(request.getProvince(), request.getChannel().toString());

            return ApiResponse.success(orders, "搜索成功");

        } catch (Exception e) {
            log.error("搜索订单失败: province={}, channel={}", request.getProvince(), request.getChannel(), e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单总数
     * POST /api/orders/count
     * 
     * @param request 查询请求
     * @return 订单总数
     */
    @PostMapping("/count")
    public ApiResponse<Integer> getOrderCount(@RequestBody(required = false) OrderQueryVO request) {
        try {
            int count = orderService.getOrderCount();
            return ApiResponse.success(count, "查询成功");

        } catch (Exception e) {
            log.error("获取订单总数失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单速度信息
     * POST /api/orders/speed
     * 
     * @param request 速度查询请求
     * @return 订单速度信息
     */
    @PostMapping("/speed")
    public ApiResponse<OrderSpeedInfoDTO> getOrderSpeed(@Valid @RequestBody SpeedMonitorVO request) {
        try {
            log.info("查询订单速度: memberPhone={}, channel={}, registerId={}, timeWindow={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), request.getTimeWindowHours());

                        OrderSpeedInfoDTO speedInfo = 
                    orderService.getOrderSpeedInfo(request.getMemberPhone(), request.getChannel().toString(), 
                            request.getRegisterId(), request.getTimeWindowHours());

            return ApiResponse.success(speedInfo, "查询成功");

        } catch (Exception e) {
            log.error("查询订单速度失败: memberPhone={}, channel={}, registerId={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取多个时间窗口的订单速度信息
     * POST /api/orders/speed/multi
     * 
     * @param request 多时间窗口速度查询请求
     * @return 多个时间窗口的速度信息
     */
    @PostMapping("/speed/multi")
    public ApiResponse<List<OrderSpeedInfoDTO>> getOrderSpeedMultiWindow(@Valid @RequestBody MultiTimeWindowSpeedVO request) {
        try {
            log.info("查询多时间窗口订单速度: memberPhone={}, channel={}, registerId={}, timeWindows={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), request.getTimeWindows());

            // 解析时间窗口参数
            String[] timeWindowArray = request.getTimeWindows().split(",");
            int[] timeWindowInts = new int[timeWindowArray.length];
            for (int i = 0; i < timeWindowArray.length; i++) {
                timeWindowInts[i] = Integer.parseInt(timeWindowArray[i].trim());
            }

            List<OrderSpeedInfoDTO> speedInfos = 
                    orderService.getOrderSpeedMultiWindow(request.getMemberPhone(), request.getChannel().toString(), 
                            request.getRegisterId(), timeWindowInts);

            return ApiResponse.success(speedInfos, "查询成d功");

        } catch (Exception e) {
            log.error("查询多时间窗口订单速度失败: memberPhone={}, channel={}, registerId={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定时间范围内的订单统计
     * POST /api/orders/stats/time-range
     * 
     * @param request 时间范围统计请求
     * @return 订单统计信息
     */
    @PostMapping("/stats/time-range")
    public ApiResponse<OrderTimeRangeStatsDTO> getOrderStatsInTimeRange(@Valid @RequestBody TimeRangeStatsVO request) {
        try {
            log.info("查询时间范围内订单统计: memberPhone={}, channel={}, registerId={}, startTime={}, endTime={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), request.getStartTime(), request.getEndTime());

            String redisKey = request.getMemberPhone() + "-" + request.getChannel() + "-" + request.getRegisterId();
            OrderTimeRangeStatsDTO stats = 
                    mockRedisService.getOrderStatsInTimeRange(redisKey, request.getStartTime(), request.getEndTime());

            return ApiResponse.success(stats, "查询成功");

        } catch (Exception e) {
            log.error("查询时间范围内订单统计失败: memberPhone={}, channel={}, registerId={}", 
                    request.getMemberPhone(), request.getChannel(), request.getRegisterId(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近N小时的订单统计
     * POST /api/orders/stats/recent
     * 
     * @param request 最近统计请求
     * @return 订单统计信息
     */
    @PostMapping("/stats/recent")
    public ApiResponse<OrderTimeRangeStatsDTO> getRecentOrderStats(@Valid @RequestBody RecentStatsVO request) {
        try {
            log.info("查询最近{}小时订单统计: memberPhone={}, channel={}, registerId={}", 
                    request.getHours(), request.getMemberPhone(), request.getChannel(), request.getRegisterId());

            String redisKey = request.getMemberPhone() + "-" + request.getChannel() + "-" + request.getRegisterId();
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (request.getHours() * 60 * 60 * 1000L);
            
            OrderTimeRangeStatsDTO stats = 
                    mockRedisService.getOrderStatsInTimeRange(redisKey, startTime, endTime);

            return ApiResponse.success(stats, "查询成功");

        } catch (Exception e) {
            log.error("查询最近{}小时订单统计失败: memberPhone={}, channel={}, registerId={}", 
                    request.getHours(), request.getMemberPhone(), request.getChannel(), request.getRegisterId(), e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有预警记录
     * POST /api/orders/alerts
     * 
     * @param request 预警查询请求
     * @return 预警记录列表
     */
    @PostMapping("/alerts")
    public ApiResponse<List<AlertRecordDTO>> getAllAlerts(@RequestBody(required = false) AlertQueryVO request) {
        try {
            List<AlertRecordDTO> alerts = mockDatabaseService.getAllAlertRecords();
            return ApiResponse.success(alerts, "查询成功");

        } catch (Exception e) {
            log.error("获取预警记录失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统状态信息
     * POST /api/orders/status
     * 
     * @param request 系统状态查询请求
     * @return 系统状态信息
     */
    @PostMapping("/status")
    public ApiResponse<Object> getSystemStatus(@RequestBody(required = false) SystemStatusVO request) {
        try {
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            
            // 订单统计
            status.put("orderCount", mockDatabaseService.getOrderCount());
            status.put("alertCount", mockDatabaseService.getAlertCount());
            
            // Redis状态
            status.put("redisKeyCount", mockRedisService.getKeyCount());
            
            // 系统信息
            status.put("timestamp", System.currentTimeMillis());
            status.put("status", "RUNNING");

            return ApiResponse.success(status, "查询成功");

        } catch (Exception e) {
            log.error("获取系统状态失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }





    // ==================== 免打扰机制接口 ====================

    /**
     * 获取预警免打扰状态
     */
    @PostMapping("/alerts/quiet-mode/status")
    public ApiResponse<AlertQuietModeService.AlertStatus> getAlertQuietModeStatus(@RequestBody AlertKeyRequest request) {
        try {
            AlertQuietModeService.AlertStatus status = alertQuietModeService.getAlertStatus(request.getAlertKey());
            return ApiResponse.success(status, "预警免打扰状态查询成功");
        } catch (Exception e) {
            log.error("获取预警免打扰状态失败: alertKey={}", request.getAlertKey(), e);
            return ApiResponse.error("获取预警免打扰状态失败: " + e.getMessage());
        }
    }

    /**
     * 重置预警免打扰记录
     */
    @PostMapping("/alerts/quiet-mode/reset/{alertKey}")
    public ApiResponse<String> resetAlertQuietMode(@PathVariable String alertKey) {
        try {
            alertQuietModeService.resetAlertRecord(alertKey);
            return ApiResponse.success("预警免打扰记录重置成功", "预警记录已重置，可以重新发送预警");
        } catch (Exception e) {
            log.error("重置预警免打扰记录失败: alertKey={}", alertKey, e);
            return ApiResponse.error("重置预警免打扰记录失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有预警免打扰记录
     */
    @PostMapping("/alerts/quiet-mode/clear")
    public ApiResponse<String> clearAllAlertQuietMode() {
        try {
            alertQuietModeService.clearAllRecords();
            return ApiResponse.success("所有预警免打扰记录清空成功", "所有预警记录已清空，可以重新发送预警");
        } catch (Exception e) {
            log.error("清空所有预警免打扰记录失败", e);
            return ApiResponse.error("清空所有预警免打扰记录失败: " + e.getMessage());
        }
    }
}
