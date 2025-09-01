package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.dto.OrderDTO;
import com.picc.java.learn.alert.dto.OrderSpeedInfoDTO;
import com.picc.java.learn.alert.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务
 * 处理订单创建和异步预警检测
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private MockDatabaseService mockDatabaseService;

    @Autowired
    private MockRedisService mockRedisService;

    @Autowired
    private OrderSpeedService orderSpeedService;

    @Autowired
    private AlertDetectionService alertDetectionService;

    /**
     * 创建订单
     * 
     * @param orderVO 订单VO
     * @return 创建后的订单DTO
     */
    public OrderDTO createOrder(OrderVO orderVO) {
        log.info("开始创建订单: memberPhone={}, province={}, channel={}", 
                orderVO.getMemberPhone(), orderVO.getProvince(), orderVO.getChannel());

        // 1. 转换为DTO
        OrderDTO orderDTO = convertToDTO(orderVO);
        
        // 2. 保存订单到数据库
        OrderDTO savedOrder = mockDatabaseService.saveOrder(orderDTO);
        
        // 3. 异步触发预警检测
        alertDetectionService.checkOrderAlert(savedOrder);
        
        // 4. 记录订单到Redis Sorted Set（用于速度监控）
        mockRedisService.zaddOrder(savedOrder.getRedisKey(), String.valueOf(savedOrder.getOrderId()));

        log.info("订单创建成功: orderId={}, memberPhone={}, province={}, channel={}", 
                savedOrder.getOrderId(), savedOrder.getMemberPhone(), 
                savedOrder.getProvince(), savedOrder.getChannel());

        return savedOrder;
    }

    /**
     * 转换VO到DTO
     * 
     * @param orderVO 订单VO
     * @return 订单DTO
     */
    private OrderDTO convertToDTO(OrderVO orderVO) {
        return OrderDTO.builder()
                .memberPhone(orderVO.getMemberPhone())
                .memberName(orderVO.getMemberName())
                .province(orderVO.getProvince())
                .channel(String.valueOf(orderVO.getChannel()))
                .registerId(orderVO.getRegisterId())
                .orderAmount(orderVO.getOrderAmount())
                .orderStatus(orderVO.getOrderStatus())
                .build();
    }











    /**
     * 根据ID查询订单
     * 
     * @param memberPhone 会员手机号
     * @return 订单列表
     */
    public List<OrderDTO> getOrdersByMemberPhone(String memberPhone) {
        return mockDatabaseService.getOrdersByMemberPhone(memberPhone);
    }

    /**
     * 根据省份和渠道查询订单列表
     * 
     * @param province 省份
     * @param channel 渠道
     * @return 订单列表
     */
    public List<OrderDTO> getOrdersByProvinceAndChannel(String province, String channel) {
        return mockDatabaseService.getOrdersByProvinceAndChannel(province, channel);
    }

    /**
     * 获取订单总数
     * 
     * @return 订单总数
     */
    public int getOrderCount() {
        return mockDatabaseService.getOrderCount();
    }

    /**
     * 获取订单速度信息
     * 
     * @param memberPhone 会员手机号
     * @param channel 渠道
     * @param registerId 注册ID
     * @param timeWindowHours 时间窗口（小时）
     * @return 订单速度信息
     */
    public OrderSpeedInfoDTO getOrderSpeedInfo(String memberPhone, String channel, String registerId, int timeWindowHours) {
        String redisKey = memberPhone + "-" + channel + "-" + registerId;
        return orderSpeedService.calculateOrderSpeed(redisKey, timeWindowHours);
    }

    /**
     * 获取多个时间窗口的订单速度信息
     * 
     * @param memberPhone 会员手机号
     * @param channel 渠道
     * @param registerId 注册ID
     * @param timeWindows 时间窗口数组
     * @return 多个时间窗口的速度信息
     */
    public List<OrderSpeedInfoDTO> getOrderSpeedMultiWindow(String memberPhone, String channel, String registerId, int... timeWindows) {
        String redisKey = memberPhone + "-" + channel + "-" + registerId;
        return orderSpeedService.calculateOrderSpeedMultiWindow(redisKey, timeWindows);
    }
}
