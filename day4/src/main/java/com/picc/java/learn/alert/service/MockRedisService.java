package com.picc.java.learn.alert.service;


import com.picc.java.learn.alert.common.RedisEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 模拟Redis服务
 * 专注于模拟Redis基础操作，不包含任何业务逻辑
 * 
 * @author learn-java
 * @since 2025-08-31
 */
@Slf4j
@Service
public class MockRedisService {

    /**
     * 统一的数据存储：key -> RedisEntry
     */
    private final Map<String, RedisEntry> dataStore = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public MockRedisService() {
        // 启动定时清理过期数据的任务
        startCleanupTasks();
    }

    // ==================== 基础Redis操作 ====================

    /**
     * 设置key-value，并指定过期时间
     */
    public <T> void set(String key, T value, RedisEntry.RedisDataType dataType, int expireSeconds) {
        long expireTime = expireSeconds > 0 ? 
            System.currentTimeMillis() + (expireSeconds * 1000L) : 0;
            
        RedisEntry entry = RedisEntry.builder()
            .dataType(dataType)
            .value(value)
            .createTime(System.currentTimeMillis())
            .expireTime(expireTime)
            .build();
            
        dataStore.put(key, entry);
        log.debug("设置数据: key={}, type={}, value={}, expireSeconds={}", 
                 key, dataType, value, expireSeconds);
    }

    /**
     * 获取key对应的value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, RedisEntry.RedisDataType expectedType) {
        RedisEntry entry = dataStore.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null && entry.isExpired()) {
                dataStore.remove(key);
            }
            return null;
        }
        
        if (entry.getDataType() != expectedType) {
            log.warn("数据类型不匹配: key={}, expected={}, actual={}", 
                    key, expectedType, entry.getDataType());
            return null;
        }
        
        return (T) entry.getValue();
    }

    /**
     * 删除key
     */
    public boolean delete(String key) {
        RedisEntry removedEntry = dataStore.remove(key);
        boolean success = removedEntry != null;
        log.debug("删除Redis数据: key={}, success={}", key, success);
        return success;
    }

    /**
     * 检查key是否存在
     */
    public boolean exists(String key) {
        RedisEntry entry = dataStore.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            dataStore.remove(key);
            return false;
        }
        return true;
    }

    /**
     * 获取key的剩余生存时间（秒）
     */
    public long getTtl(String key) {
        RedisEntry entry = dataStore.get(key);
        if (entry == null) {
            return 0;
        }
        return entry.getTtl();
    }

    // ==================== 订单数量操作 ====================

    /**
     * 设置订单数量
     */
    public void setOrderCount(String key, Integer count, Integer expireSeconds) {
        set(key, count, RedisEntry.RedisDataType.ORDER_COUNT, expireSeconds);
        log.info("设置订单数量: key={}, count={}, expireSeconds={}", key, count, expireSeconds);
    }

    /**
     * 获取订单数量
     */
    public Integer getOrderCount(String key) {
        Integer count = get(key, RedisEntry.RedisDataType.ORDER_COUNT);
        if (count == null) {
            return 0;
        }
        log.debug("获取订单数量: key={}, count={}", key, count);
        return count;
    }

    /**
     * 增加订单数量
     */
    public Integer incrementOrderCount(String key, Integer increment, Integer expireSeconds) {
        Integer currentCount = getOrderCount(key);
        Integer newCount = currentCount + increment;
        
        setOrderCount(key, newCount, expireSeconds);
        
        log.info("增加订单数量: key={}, increment={}, newCount={}", key, increment, newCount);
        return newCount;
    }

    // ==================== 通用字符串操作 ====================

    /**
     * 设置字符串值，并指定过期时间
     */
    public void setEx(String key, String value, int expireSeconds) {
        set(key, value, RedisEntry.RedisDataType.REDIS_STRING, expireSeconds);
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        return get(key, RedisEntry.RedisDataType.REDIS_STRING);
    }

    // ==================== Sorted Set基础操作 ====================

    /**
     * 添加成员到Sorted Set
     */
    public void zadd(String key, String member, long score) {
        @SuppressWarnings("unchecked")
        TreeMap<Long, Set<String>> sortedSet = get(key, RedisEntry.RedisDataType.SORTED_SET);
        
        if (sortedSet == null) {
            sortedSet = new TreeMap<>();
        }
        
        sortedSet.computeIfAbsent(score, s -> new HashSet<>()).add(member);
        
        // 重新存储，保持过期时间
        RedisEntry existingEntry = dataStore.get(key);
        long expireTime = existingEntry != null ? existingEntry.getExpireTime() : 0;
        
        RedisEntry entry = RedisEntry.builder()
            .dataType(RedisEntry.RedisDataType.SORTED_SET)
            .value(sortedSet)
            .createTime(System.currentTimeMillis())
            .expireTime(expireTime)
            .build();
            
        dataStore.put(key, entry);
        
        log.debug("添加成员到Sorted Set: key={}, member={}, score={}", key, member, score);
    }

    /**
     * 添加订单到Sorted Set（使用当前时间）
     */
    public void zaddOrder(String key, String orderId) {
        long timestamp = System.currentTimeMillis();
        zadd(key, orderId, timestamp);
    }

    /**
     * 获取指定分数范围内的成员数量
     */
    public long zcount(String key, long minScore, long maxScore) {
        @SuppressWarnings("unchecked")
        TreeMap<Long, Set<String>> sortedSet = get(key, RedisEntry.RedisDataType.SORTED_SET);
        
        if (sortedSet == null) {
            return 0;
        }

        long count = 0;
        for (Map.Entry<Long, Set<String>> entry : sortedSet.entrySet()) {
            long score = entry.getKey();
            if (score >= minScore && score <= maxScore) {
                count += entry.getValue().size();
            }
        }

        log.debug("获取分数范围内成员数量: key={}, min={}, max={}, count={}", 
                key, minScore, maxScore, count);
        return count;
    }

    /**
     * 获取指定分数范围内的成员列表
     */
    public List<String> zrangeByScore(String key, long minScore, long maxScore) {
        @SuppressWarnings("unchecked")
        TreeMap<Long, Set<String>> sortedSet = get(key, RedisEntry.RedisDataType.SORTED_SET);
        
        if (sortedSet == null) {
            return new ArrayList<>();
        }

        List<String> members = new ArrayList<>();
        for (Map.Entry<Long, Set<String>> entry : sortedSet.entrySet()) {
            long score = entry.getKey();
            if (score >= minScore && score <= maxScore) {
                members.addAll(entry.getValue());
            }
        }

        log.debug("获取分数范围内成员: key={}, min={}, max={}, members={}", 
                key, minScore, maxScore, members);
        return members;
    }

    /**
     * 获取Sorted Set中的成员总数
     */
    public long zcard(String key) {
        @SuppressWarnings("unchecked")
        TreeMap<Long, Set<String>> sortedSet = get(key, RedisEntry.RedisDataType.SORTED_SET);
        
        if (sortedSet == null) {
            return 0;
        }

        long count = sortedSet.values().stream()
                .mapToLong(Set::size)
                .sum();

        log.debug("获取Sorted Set成员总数: key={}, count={}", key, count);
        return count;
    }

    /**
     * 移除指定分数范围内的成员
     */
    public long zremrangeByScore(String key, long minScore, long maxScore) {
        @SuppressWarnings("unchecked")
        TreeMap<Long, Set<String>> sortedSet = get(key, RedisEntry.RedisDataType.SORTED_SET);
        
        if (sortedSet == null) {
            return 0;
        }

        long removedCount = 0;
        Iterator<Map.Entry<Long, Set<String>>> iterator = sortedSet.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Long, Set<String>> entry = iterator.next();
            long score = entry.getKey();
            if (score >= minScore && score <= maxScore) {
                removedCount += entry.getValue().size();
                iterator.remove();
            }
        }

        // 如果还有剩余数据，更新存储
        if (!sortedSet.isEmpty()) {
            RedisEntry existingEntry = dataStore.get(key);
            long expireTime = existingEntry != null ? existingEntry.getExpireTime() : 0;
            
            RedisEntry newEntry = RedisEntry.builder()
                .dataType(RedisEntry.RedisDataType.SORTED_SET)
                .value(sortedSet)
                .createTime(System.currentTimeMillis())
                .expireTime(expireTime)
                .build();
                
            dataStore.put(key, newEntry);
        } else {
            // 如果没有剩余数据，删除整个key
            dataStore.remove(key);
        }

        log.info("移除分数范围内成员: key={}, min={}, max={}, removedCount={}", 
                key, minScore, maxScore, removedCount);
        return removedCount;
    }

    // ==================== 基础Redis操作完成 ====================

    // ==================== 业务方法（从MockRedisSortedSetService合并） ====================







    // ==================== 业务方法完成 ====================

    // ==================== 系统管理方法 ====================

    /**
     * 启动定时清理任务
     */
    private void startCleanupTasks() {
        // 数据清理任务：每分钟执行
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredData();
            } catch (Exception e) {
                log.error("清理过期数据时发生错误", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
        
        // 系统维护任务：每5分钟执行
        scheduler.scheduleAtFixedRate(() -> {
            try {
                systemMaintenance();
            } catch (Exception e) {
                log.error("系统维护时发生错误", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        int cleanedCount = 0;
        Iterator<Map.Entry<String, RedisEntry>> iterator = dataStore.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, RedisEntry> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            log.info("清理过期数据完成，共清理 {} 个", cleanedCount);
        }
    }

    /**
     * 系统维护任务
     */
    private void systemMaintenance() {
        // 清理12小时前的订单数据
        long cutoffTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000L);
        int cleanedCount = 0;
        
        for (String key : dataStore.keySet()) {
            RedisEntry entry = dataStore.get(key);
            if (entry != null && entry.getDataType() == RedisEntry.RedisDataType.SORTED_SET) {
                long removed = zremrangeByScore(key, 0, cutoffTime);
                if (removed > 0) {
                    cleanedCount += removed;
                }
            }
        }
        
        if (cleanedCount > 0) {
            log.info("系统维护完成，清理过期订单记录 {} 个", cleanedCount);
        }
    }

    /**
     * 获取当前存储的Key数量（用于调试）
     */
    public int getKeyCount() {
        return dataStore.size();
    }

    /**
     * 获取存储统计信息（用于监控）
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalKeys", dataStore.size());
        
        // 按数据类型统计
        Map<RedisEntry.RedisDataType, Integer> typeStats = new HashMap<>();
        for (RedisEntry entry : dataStore.values()) {
            typeStats.merge(entry.getDataType(), 1, Integer::sum);
        }
        stats.put("typeStats", typeStats);
        
        return stats;
    }

    /**
     * 清空所有数据（用于测试）
     */
    public void clearAll() {
        dataStore.clear();
        log.info("清空所有模拟Redis数据");
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("MockRedisService已关闭");
    }
}
