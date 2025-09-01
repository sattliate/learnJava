package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.AlertQuietModeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预警免打扰服务
 * 管理预警发送时间和频率控制，避免频繁发送预警消息
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@Service
public class AlertQuietModeService {

    @Autowired
    private AlertQuietModeConfig quietModeConfig;

    @Autowired
    private MockRedisService mockRedisService; // 使用现有的MockRedisService

    /**
     * 检查是否可以发送预警
     * 
     * @param alertKey 预警维度key
     * @return 是否可以发送预警
     */
    public boolean canSendAlert(String alertKey) {
        if (!quietModeConfig.isEnabled()) {
            return true; // 免打扰机制关闭，允许发送
        }

        // 检查Redis中是否存在免打扰key
        String quietModeKey = getQuietModeKey(alertKey);
        String quietModeValue = mockRedisService.getString(quietModeKey);
        
        if (quietModeValue == null) {
            // 没有免打扰记录，允许发送预警
            log.debug("预警 {} 无免打扰记录，允许发送", alertKey);
            return true;
        }

        // 解析免打扰记录
        AlertRecord record = parseAlertRecord(quietModeValue);
        if (record == null) {
            log.warn("预警 {} 免打扰记录格式错误，允许发送", alertKey);
            return true;
        }

        // 检查是否超过最大预警次数
        if (record.getAlertCount() >= quietModeConfig.getMaxAlertCount()) {
            // 超过最大预警次数，使用最后一个免打扰时间段作为预警频率
            int lastPeriodMinutes = quietModeConfig.getLastQuietPeriod();
            long nextAlertTime = record.getLastAlertTime() + TimeUnit.MINUTES.toMillis(lastPeriodMinutes);
            long currentTime = System.currentTimeMillis();
            
            if (currentTime < nextAlertTime) {
                log.debug("预警 {} 超过最大次数，使用{}分钟间隔，未到下次预警时间，当前时间={}, 下次预警时间={}", 
                        alertKey, lastPeriodMinutes, currentTime, nextAlertTime);
                return false;
            }
            
            log.debug("预警 {} 超过最大次数，使用{}分钟间隔，可以发送预警", alertKey, lastPeriodMinutes);
            return true;
        }

        // 检查是否到达下次预警时间
        long nextAlertTime = calculateNextAlertTime(record);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime < nextAlertTime) {
            log.debug("预警 {} 未到下次预警时间，当前时间={}, 下次预警时间={}", 
                    alertKey, currentTime, nextAlertTime);
            return false;
        }

        return true;
    }

    /**
     * 记录预警发送
     * 
     * @param alertKey 预警维度key
     */
    public void recordAlertSent(String alertKey) {
        long currentTime = System.currentTimeMillis();
        
        // 获取现有的免打扰记录
        String quietModeKey = getQuietModeKey(alertKey);
        String existingValue = mockRedisService.getString(quietModeKey);
        
        AlertRecord record;
        if (existingValue == null) {
            // 创建新的预警记录
            record = new AlertRecord();
            record.setAlertKey(alertKey);
            record.setFirstAlertTime(currentTime);
            record.setAlertCount(1);
            record.setLastAlertTime(currentTime);
            record.setNextAlertTime(calculateNextAlertTime(record));
        } else {
            // 更新现有记录
            record = parseAlertRecord(existingValue);
            if (record == null) {
                record = new AlertRecord();
                record.setAlertKey(alertKey);
                record.setFirstAlertTime(currentTime);
                record.setAlertCount(1);
                record.setLastAlertTime(currentTime);
                record.setNextAlertTime(calculateNextAlertTime(record));
            } else {
                record.setAlertCount(record.getAlertCount() + 1);
                record.setLastAlertTime(currentTime);
                record.setNextAlertTime(calculateNextAlertTime(record));
            }
        }

        // 计算过期时间（使用最后一个免打扰时间段作为最大过期时间）
        int maxExpireMinutes = quietModeConfig.getLastQuietPeriod();
        
        // 将记录存储到Redis，设置过期时间
        String recordValue = serializeAlertRecord(record);
        mockRedisService.setEx(quietModeKey, recordValue, maxExpireMinutes * 60); // 转换为秒
        
        log.info("记录预警发送到Redis: key={}, 预警次数={}, 下次预警时间={}, 过期时间={}分钟", 
                alertKey, record.getAlertCount(), record.getNextAlertTime(), maxExpireMinutes);
    }

    /**
     * 计算下次预警时间
     * 
     * @param record 预警记录
     * @return 下次预警时间戳
     */
    private long calculateNextAlertTime(AlertRecord record) {
        int alertCount = record.getAlertCount();
        
        if (alertCount >= quietModeConfig.getQuietPeriods().size()) {
            // 超过免打扰时间段数量，使用最后一个免打扰时间段
            long lastAlertTime = record.getLastAlertTime();
            int lastPeriodMinutes = quietModeConfig.getLastQuietPeriod();
            long nextAlertTime = lastAlertTime + TimeUnit.MINUTES.toMillis(lastPeriodMinutes);
            
            log.debug("超过最大预警次数，使用{}分钟间隔: 预警次数={}, 下次预警时间={}", 
                    lastPeriodMinutes, alertCount, nextAlertTime);
            
            return nextAlertTime;
        }
        
        // 获取对应的免打扰时间段
        int quietPeriodMinutes = quietModeConfig.getQuietPeriodForAlertCount(alertCount);
        long nextAlertTime = record.getFirstAlertTime() + TimeUnit.MINUTES.toMillis(quietPeriodMinutes);
        
        log.debug("计算下次预警时间: 预警次数={}, 免打扰时间段={}分钟, 下次预警时间={}", 
                alertCount, quietPeriodMinutes, nextAlertTime);
        
        return nextAlertTime;
    }

    /**
     * 生成Redis免打扰key
     * 
     * @param alertKey 预警维度key
     * @return Redis key
     */
    private String getQuietModeKey(String alertKey) {
        return quietModeConfig.generateRedisKey(alertKey);
    }

    /**
     * 序列化预警记录为字符串
     * 
     * @param record 预警记录
     * @return 序列化后的字符串
     */
    private String serializeAlertRecord(AlertRecord record) {
        return String.format("%s|%d|%d|%d|%d", 
                record.getAlertKey(),
                record.getFirstAlertTime(),
                record.getAlertCount(),
                record.getLastAlertTime(),
                record.getNextAlertTime());
    }

    /**
     * 解析字符串为预警记录
     * 
     * @param value 序列化后的字符串
     * @return 预警记录，解析失败返回null
     */
    private AlertRecord parseAlertRecord(String value) {
        try {
            String[] parts = value.split("\\|");
            if (parts.length != 5) {
                return null;
            }
            
            AlertRecord record = new AlertRecord();
            record.setAlertKey(parts[0]);
            record.setFirstAlertTime(Long.parseLong(parts[1]));
            record.setAlertCount(Integer.parseInt(parts[2]));
            record.setLastAlertTime(Long.parseLong(parts[3]));
            record.setNextAlertTime(Long.parseLong(parts[4]));
            
            return record;
        } catch (Exception e) {
            log.warn("解析预警记录失败: {}", value, e);
            return null;
        }
    }



    /**
     * 获取预警状态信息
     * 
     * @param alertKey 预警维度key
     * @return 预警状态信息
     */
    public AlertStatus getAlertStatus(String alertKey) {
        String quietModeKey = getQuietModeKey(alertKey);
        String existingValue = mockRedisService.getString(quietModeKey);
        
        if (existingValue == null) {
            return new AlertStatus(alertKey, 0, 0, 0, 0, false, "未触发预警");
        }

        AlertRecord record = parseAlertRecord(existingValue);
        if (record == null) {
            return new AlertStatus(alertKey, 0, 0, 0, 0, false, "记录格式错误");
        }

        long currentTime = System.currentTimeMillis();
        boolean canSend = canSendAlert(alertKey);
        String status = canSend ? "可以发送" : "免打扰中";
        
        return new AlertStatus(
            alertKey,
            record.getAlertCount(),
            record.getFirstAlertTime(),
            record.getLastAlertTime(),
            record.getNextAlertTime(),
            canSend,
            status
        );
    }

    /**
     * 重置预警记录（用于测试）
     * 
     * @param alertKey 预警维度key
     */
    public void resetAlertRecord(String alertKey) {
        String quietModeKey = getQuietModeKey(alertKey);
        mockRedisService.delete(quietModeKey);
        log.info("重置预警记录: {}", alertKey);
    }

    /**
     * 清空所有预警记录（用于测试）
     */
    public void clearAllRecords() {
        // 注意：这里需要实现批量删除逻辑，或者提供模式匹配删除
        // 暂时记录日志，实际项目中需要实现具体的清理逻辑
        log.info("清空所有预警记录功能需要实现批量删除逻辑");
    }

    /**
     * 获取所有预警记录（用于调试）
     * 
     * @return 预警记录Map
     */
    public java.util.Map<String, AlertRecord> getAllAlertRecords() {
        // 注意：这里需要实现批量查询逻辑
        // 暂时返回空Map，实际项目中需要实现具体的查询逻辑
        log.info("获取所有预警记录功能需要实现批量查询逻辑");
        return new java.util.HashMap<>();
    }

    /**
     * 预警记录内部类
     */
    public static class AlertRecord {
        private String alertKey;
        private long firstAlertTime;    // 第一次预警时间
        private int alertCount;         // 预警次数
        private long lastAlertTime;     // 最后一次预警时间
        private long nextAlertTime;     // 下次预警时间

        // Getters and Setters
        public String getAlertKey() { return alertKey; }
        public void setAlertKey(String alertKey) { this.alertKey = alertKey; }

        public long getFirstAlertTime() { return firstAlertTime; }
        public void setFirstAlertTime(long firstAlertTime) { this.firstAlertTime = firstAlertTime; }

        public int getAlertCount() { return alertCount; }
        public void setAlertCount(int alertCount) { this.alertCount = alertCount; }

        public long getLastAlertTime() { return lastAlertTime; }
        public void setLastAlertTime(long lastAlertTime) { this.lastAlertTime = lastAlertTime; }

        public long getNextAlertTime() { return nextAlertTime; }
        public void setNextAlertTime(long nextAlertTime) { this.nextAlertTime = nextAlertTime; }

        @Override
        public String toString() {
            return String.format("AlertRecord{key='%s', count=%d, first=%d, last=%d, next=%d}", 
                    alertKey, alertCount, firstAlertTime, lastAlertTime, nextAlertTime);
        }
    }

    /**
     * 预警状态信息
     */
    public static class AlertStatus {
        private String alertKey;
        private int alertCount;
        private long firstAlertTime;
        private long lastAlertTime;
        private long nextAlertTime;
        private boolean canSend;
        private String status;

        public AlertStatus(String alertKey, int alertCount, long firstAlertTime, 
                         long lastAlertTime, long nextAlertTime, boolean canSend, String status) {
            this.alertKey = alertKey;
            this.alertCount = alertCount;
            this.firstAlertTime = firstAlertTime;
            this.lastAlertTime = lastAlertTime;
            this.nextAlertTime = nextAlertTime;
            this.canSend = canSend;
            this.status = status;
        }

        // Getters
        public String getAlertKey() { return alertKey; }
        public int getAlertCount() { return alertCount; }
        public long getFirstAlertTime() { return firstAlertTime; }
        public long getLastAlertTime() { return lastAlertTime; }
        public long getNextAlertTime() { return nextAlertTime; }
        public boolean isCanSend() { return canSend; }
        public String getStatus() { return status; }

        @Override
        public String toString() {
            return String.format("AlertStatus{key='%s', count=%d, canSend=%s, status='%s'}", 
                    alertKey, alertCount, canSend, status);
        }
    }
}
