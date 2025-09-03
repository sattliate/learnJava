package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.dto.OrderDTO;

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
        OrderDTO orderDTO = orderVO.toDTO();
        
        // 2. 保存订单到数据库
        OrderDTO savedOrder = mockDatabaseService.saveOrder(orderDTO);
        
        // 3. 异步触发预警检测
        alertDetectionService.checkOrderAlert(savedOrder);
        


        log.info("订单创建成功: orderId={}, memberPhone={}, province={}, channel={}", 
                savedOrder.getOrderId(), savedOrder.getMemberPhone(), 
                savedOrder.getProvince(), savedOrder.getChannel());

        return savedOrder;
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


}
