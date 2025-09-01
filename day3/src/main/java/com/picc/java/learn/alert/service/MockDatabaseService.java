package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.dto.AlertRecordDTO;
import com.picc.java.learn.alert.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟数据库服务
 * 用于存储订单和预警记录
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@Service
public class MockDatabaseService {

    /**
     * 订单数据存储（模拟数据库表）
     */
    private final Map<Long, OrderDTO> orderMap = new ConcurrentHashMap<>();

    /**
     * 预警记录存储（模拟数据库表）
     */
    private final Map<Long, AlertRecordDTO> alertMap = new ConcurrentHashMap<>();

    /**
     * 订单ID生成器
     */
    private final AtomicLong orderIdGenerator = new AtomicLong(1);

    /**
     * 预警ID生成器
     */
    private final AtomicLong alertIdGenerator = new AtomicLong(1);

    /**
     * 保存订单
     * 
     * @param orderDTO 订单DTO
     * @return 保存后的订单DTO（包含生成的ID）
     */
    public OrderDTO saveOrder(OrderDTO orderDTO) {
        Long orderId = orderIdGenerator.getAndIncrement();
        orderDTO.setOrderId(orderId);
        orderDTO.setCreateTime(LocalDateTime.now());
        orderDTO.setUpdateTime(LocalDateTime.now());
        
        orderMap.put(orderId, orderDTO);
        
        log.info("保存订单成功: orderId={}, memberPhone={}, province={}, channel={}", 
                orderId, orderDTO.getMemberPhone(), orderDTO.getProvince(), orderDTO.getChannel());
        
        return orderDTO;
    }

    /**
     * 根据ID查询订单
     * 
     * @param orderId 订单ID
     * @return 订单DTO，如果不存在返回null
     */
    public OrderDTO getOrderById(Long orderId) {
        return orderMap.get(orderId);
    }

    /**
     * 根据会员手机号查询订单列表
     * 
     * @param memberPhone 会员手机号
     * @return 订单列表
     */
    public List<OrderDTO> getOrdersByMemberPhone(String memberPhone) {
        List<OrderDTO> orders = new ArrayList<>();
        for (OrderDTO order : orderMap.values()) {
            if (memberPhone.equals(order.getMemberPhone())) {
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * 根据省份和渠道查询订单列表
     * 
     * @param province 省份
     * @param channel 渠道
     * @return 订单列表
     */
    public List<OrderDTO> getOrdersByProvinceAndChannel(String province, String channel) {
        List<OrderDTO> orders = new ArrayList<>();
        for (OrderDTO order : orderMap.values()) {
            if (province.equals(order.getProvince()) && channel.equals(order.getChannel())) {
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * 保存预警记录
     * 
     * @param alertRecordDTO 预警记录DTO
     * @return 保存后的预警记录DTO（包含生成的ID）
     */
    public AlertRecordDTO saveAlertRecord(AlertRecordDTO alertRecordDTO) {
        Long alertId = alertIdGenerator.getAndIncrement();
        alertRecordDTO.setAlertId(alertId);
        alertRecordDTO.setCreateTime(LocalDateTime.now());
        alertRecordDTO.setUpdateTime(LocalDateTime.now());
        
        // 生成预警描述
        if (alertRecordDTO.getAlertDescription() == null) {
            alertRecordDTO.setAlertDescription(alertRecordDTO.generateAlertDescription());
        }
        
        alertMap.put(alertId, alertRecordDTO);
        
        log.info("保存预警记录成功: alertId={}, memberPhone={}, province={}, channel={}, currentCount={}, threshold={}", 
                alertId, alertRecordDTO.getMemberPhone(), alertRecordDTO.getProvince(), 
                alertRecordDTO.getChannel(), alertRecordDTO.getCurrentOrderCount(), alertRecordDTO.getThreshold());
        
        return alertRecordDTO;
    }

    /**
     * 根据ID查询预警记录
     * 
     * @param alertId 预警记录ID
     * @return 预警记录DTO，如果不存在返回null
     */
    public AlertRecordDTO getAlertRecordById(Long alertId) {
        return alertMap.get(alertId);
    }

    /**
     * 根据会员手机号查询预警记录列表
     * 
     * @param memberPhone 会员手机号
     * @return 预警记录列表
     */
    public List<AlertRecordDTO> getAlertRecordsByMemberPhone(String memberPhone) {
        List<AlertRecordDTO> alerts = new ArrayList<>();
        for (AlertRecordDTO alert : alertMap.values()) {
            if (memberPhone.equals(alert.getMemberPhone())) {
                alerts.add(alert);
            }
        }
        return alerts;
    }

    /**
     * 根据省份和渠道查询预警记录列表
     * 
     * @param province 省份
     * @param channel 渠道
     * @return 预警记录列表
     */
    public List<AlertRecordDTO> getAlertRecordsByProvinceAndChannel(String province, String channel) {
        List<AlertRecordDTO> alerts = new ArrayList<>();
        for (AlertRecordDTO alert : alertMap.values()) {
            if (province.equals(alert.getProvince()) && channel.equals(alert.getChannel())) {
                alerts.add(alert);
            }
        }
        return alerts;
    }

    /**
     * 获取所有预警记录
     * 
     * @return 预警记录列表
     */
    public List<AlertRecordDTO> getAllAlertRecords() {
        return new ArrayList<>(alertMap.values());
    }

    /**
     * 获取订单总数
     * 
     * @return 订单总数
     */
    public int getOrderCount() {
        return orderMap.size();
    }

    /**
     * 获取预警记录总数
     * 
     * @return 预警记录总数
     */
    public int getAlertCount() {
        return alertMap.size();
    }

    /**
     * 清空所有数据（用于测试）
     */
    public void clearAll() {
        orderMap.clear();
        alertMap.clear();
        orderIdGenerator.set(1);
        alertIdGenerator.set(1);
        log.info("清空所有模拟数据库数据");
    }
}
