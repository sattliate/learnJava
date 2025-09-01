package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.dto.OrderSpeedInfoDTO;
import com.picc.java.learn.alert.dto.OrderTimeRangeStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模拟Redis Sorted Set服务
 * 用于存储订单时间戳和计算订单速度
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@Service
public class MockRedisSortedSetService {

    // 模拟Redis Sorted Set存储：key -> (member -> score)
    private final Map<String, Map<String, Double>> sortedSets = new ConcurrentHashMap<>();
    
    // 模拟Redis key过期时间
    private final Map<String, Long> keyExpireTimes = new ConcurrentHashMap<>();

    /**
     * 添加成员到Sorted Set
     * 
     * @param key Redis key
     * @param member 成员（订单ID）
     */
    public void zadd(String key, String member) {
        long currentTime = System.currentTimeMillis();
        
        // 获取或创建Sorted Set
        Map<String, Double> sortedSet = sortedSets.computeIfAbsent(key, k -> new TreeMap<>());
        
        // 添加成员，时间戳作为score
        sortedSet.put(member, (double) currentTime);
        
        // 设置key过期时间（12小时后）
        long expireTime = currentTime + (12 * 60 * 60 * 1000L);
        keyExpireTimes.put(key, expireTime);
        
        log.debug("添加到Sorted Set: key={}, member={}, score={}", key, member, currentTime);
        
        // 清理过期的key
        cleanupExpiredKeys();
    }

    /**
     * 获取指定时间范围内的订单统计
     * 
     * @param key Redis key
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 时间范围内订单统计
     */
    public OrderTimeRangeStatsDTO getOrderStatsInTimeRange(String key, long startTime, long endTime) {
        Map<String, Double> sortedSet = sortedSets.get(key);
        if (sortedSet == null || sortedSet.isEmpty()) {
            return OrderTimeRangeStatsDTO.builder()
                    .key(key)
                    .startTime(startTime)
                    .endTime(endTime)
                    .orderCount(0)
                    .orderIds(Collections.emptyList())
                    .timeIntervalMinutes(0)
                    .speed(0.0)
                    .build();
        }

        // 过滤时间范围内的订单
        List<String> orderIds = sortedSet.entrySet().stream()
                .filter(entry -> entry.getValue() >= startTime && entry.getValue() <= endTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        long orderCount = orderIds.size();
        long timeIntervalMinutes = (endTime - startTime) / (60 * 1000L);
        double speed = timeIntervalMinutes > 0 ? (double) orderCount / timeIntervalMinutes : 0.0;

        return OrderTimeRangeStatsDTO.builder()
                .key(key)
                .startTime(startTime)
                .endTime(endTime)
                .orderCount(orderCount)
                .orderIds(orderIds)
                .timeIntervalMinutes(timeIntervalMinutes)
                .speed(speed)
                .build();
    }

    /**
     * 计算订单速度信息
     * 
     * @param key Redis key
     * @param timeWindowHours 时间窗口（小时）
     * @return 订单速度信息
     */
    public OrderSpeedInfoDTO calculateOrderSpeed(String key, int timeWindowHours) {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (timeWindowHours * 60 * 60 * 1000L);
        
        OrderTimeRangeStatsDTO stats = getOrderStatsInTimeRange(key, startTime, currentTime);
        
        // 计算各种速度指标
        double ordersPerHour = timeWindowHours > 0 ? (double) stats.getOrderCount() / timeWindowHours : 0.0;
        double ordersPerMinute = ordersPerHour / 60.0;
        double ordersPerSecond = ordersPerMinute / 60.0;
        
        return OrderSpeedInfoDTO.builder()
                .key(key)
                .timeWindowHours(timeWindowHours)
                .windowStartTime(new Date(startTime))
                .windowEndTime(new Date(currentTime))
                .orderCount(stats.getOrderCount())
                .ordersPerHour(ordersPerHour)
                .ordersPerMinute(ordersPerMinute)
                .ordersPerSecond(ordersPerSecond)
                .orderIds(stats.getOrderIds())
                .build();
    }

    /**
     * 计算多个时间窗口的订单速度信息
     * 
     * @param key Redis key
     * @param timeWindows 时间窗口数组（小时）
     * @return 多个时间窗口的速度信息
     */
    public List<OrderSpeedInfoDTO> calculateOrderSpeedMultiWindow(String key, int... timeWindows) {
        List<OrderSpeedInfoDTO> results = new ArrayList<>();
        
        for (int timeWindow : timeWindows) {
            OrderSpeedInfoDTO speedInfo = calculateOrderSpeed(key, timeWindow);
            results.add(speedInfo);
        }
        
        return results;
    }

    /**
     * 获取Sorted Set中的成员数量
     * 
     * @param key Redis key
     * @return 成员数量
     */
    public long zcard(String key) {
        Map<String, Double> sortedSet = sortedSets.get(key);
        return sortedSet != null ? sortedSet.size() : 0;
    }

    /**
     * 获取所有key的数量
     * 
     * @return key数量
     */
    public int getKeyCount() {
        return sortedSets.size();
    }

    /**
     * 清理过期的key
     */
    private void cleanupExpiredKeys() {
        long currentTime = System.currentTimeMillis();
        
        Iterator<Map.Entry<String, Long>> iterator = keyExpireTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (entry.getValue() < currentTime) {
                String expiredKey = entry.getKey();
                sortedSets.remove(expiredKey);
                iterator.remove();
                log.debug("清理过期key: {}", expiredKey);
            }
        }
    }

    /**
     * 获取指定key的所有成员和分数
     * 
     * @param key Redis key
     * @return 成员和分数的映射
     */
    public Map<String, Double> zrangeWithScores(String key) {
        Map<String, Double> sortedSet = sortedSets.get(key);
        return sortedSet != null ? new HashMap<>(sortedSet) : Collections.emptyMap();
    }

    /**
     * 删除指定的key
     * 
     * @param key Redis key
     * @return 是否删除成功
     */
    public boolean del(String key) {
        boolean removed = sortedSets.remove(key) != null;
        keyExpireTimes.remove(key);
        return removed;
    }

    /**
     * 检查key是否存在
     * 
     * @param key Redis key
     * @return 是否存在
     */
    public boolean exists(String key) {
        return sortedSets.containsKey(key);
    }

    /**
     * 设置key过期时间
     * 
     * @param key Redis key
     * @param expireSeconds 过期时间（秒）
     */
    public void expire(String key, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000L);
        keyExpireTimes.put(key, expireTime);
    }

    /**
     * 获取key的剩余生存时间
     * 
     * @param key Redis key
     * @return 剩余生存时间（毫秒），-1表示永不过期，-2表示key不存在
     */
    public long ttl(String key) {
        Long expireTime = keyExpireTimes.get(key);
        if (expireTime == null) {
            return -2; // key不存在
        }
        
        long currentTime = System.currentTimeMillis();
        if (expireTime < currentTime) {
            return -2; // key已过期
        }
        
        return expireTime - currentTime;
    }
}
