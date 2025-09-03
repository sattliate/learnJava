package com.picc.java.learn.alert.service;

import com.picc.java.learn.alert.config.OrderAlertConfig;
import com.picc.java.learn.alert.dto.AlertRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预警通知服务
 * 负责发送预警消息给配置的接收人
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Slf4j
@Service
public class AlertNotificationService {

    @Autowired
    private OrderAlertConfig orderAlertConfig;

    /**
     * 发送预警通知
     * 
     * @param alertRecord 预警记录
     */
    public void sendAlertNotification(AlertRecordDTO alertRecord) {
        try {
            log.info("🚨 开始发送预警通知: alertId={}, memberPhone={}, province={}, channel={}", 
                    alertRecord.getAlertId(), alertRecord.getMemberPhone(), 
                    alertRecord.getProvince(), alertRecord.getChannel());

            // 1. 构建预警消息
            String alertMessage = buildAlertMessage(alertRecord);
            
            // 2. 获取预警接收人列表
            List<OrderAlertConfig.Manager> managers = orderAlertConfig.getManagers();
            
            if (managers == null || managers.isEmpty()) {
                log.warn("⚠️ 未配置预警接收人，跳过通知发送");
                return;
            }

            // 3. 发送预警通知给所有配置的接收人
            for (OrderAlertConfig.Manager manager : managers) {
                if (manager.getReceiver() != null) {
                    for (OrderAlertConfig.Receiver receiver : manager.getReceiver()) {
                        sendNotificationToReceiver(receiver, alertMessage, alertRecord);
                    }
                }
            }

            log.info("✅ 预警通知发送完成: alertId={}", alertRecord.getAlertId());

        } catch (Exception e) {
            log.error("❌ 发送预警通知失败: alertId={}", alertRecord.getAlertId(), e);
        }
    }

    /**
     * 构建预警消息
     * 
     * @param alertRecord 预警记录
     * @return 预警消息
     */
    private String buildAlertMessage(AlertRecordDTO alertRecord) {
        StringBuilder message = new StringBuilder();
        
        // 添加预警次数信息
        if (alertRecord.getAlertCount() != null && alertRecord.getAlertCount() > 0) {
            message.append("🚨 第").append(alertRecord.getAlertCount()).append("次订单预警通知\n\n");
        } else {
            message.append("🚨 订单预警通知\n\n");
        }
        
        message.append("📱 会员信息：\n");
        message.append("   手机号：").append(alertRecord.getMemberPhone()).append("\n");
        message.append("   姓名：").append(alertRecord.getMemberName()).append("\n\n");
        
        message.append("📍 订单信息：\n");
        message.append("   省份：").append(alertRecord.getProvince()).append("\n");
        message.append("   渠道：").append(alertRecord.getChannel()).append("\n");
        message.append("   注册ID：").append(alertRecord.getRegisterId()).append("\n\n");
        
        message.append("📊 预警详情：\n");
        message.append("   当前订单数量：").append(alertRecord.getCurrentOrderCount()).append("\n");
        message.append("   预警阈值：").append(alertRecord.getThreshold()).append("\n");
        message.append("   预警时间：").append(formatDateTime(alertRecord.getAlertTime())).append("\n\n");
        
        message.append("⚠️ 预警描述：\n");
        message.append("   ").append(alertRecord.getAlertDescription()).append("\n\n");
        
        message.append("⏰ 时间窗口：").append(orderAlertConfig.getTimeWindow()).append("小时\n");
        message.append("📅 发送时间：").append(formatDateTime(LocalDateTime.now())).append("\n");
        
        return message.toString();
    }

    /**
     * 发送通知给指定接收人
     * 
     * @param receiver 接收人
     * @param message 消息内容
     * @param alertRecord 预警记录
     */
    private void sendNotificationToReceiver(OrderAlertConfig.Receiver receiver, String message, AlertRecordDTO alertRecord) {
        try {
            log.info("📤 发送预警通知给接收人: name={}, phone={}, eBanUserId={}", 
                    receiver.getName(), receiver.getPhone(), receiver.getEBanUserId());

            // 这里模拟发送通知，实际项目中会调用具体的通知服务
            // 比如：短信、邮件、钉钉、企业微信等
            
            // 1. 发送短信通知（模拟）
            sendSmsNotification(receiver.getPhone(), message);
            
            // 2. 发送系统内通知（模拟）
            sendSystemNotification(receiver.getEBanUserId(), message, alertRecord);
            
            // 3. 记录通知发送日志
            logNotificationSent(receiver, alertRecord);

        } catch (Exception e) {
            log.error("❌ 发送通知给接收人失败: name={}, phone={}", receiver.getName(), receiver.getPhone(), e);
        }
    }

    /**
     * 发送短信通知（模拟）
     * 
     * @param phone 手机号
     * @param message 消息内容
     */
    private void sendSmsNotification(String phone, String message) {
        // 实际项目中这里会调用短信服务商的API
        log.info("📱 发送短信通知: phone={}, messageLength={}", phone, message.length());
        
        // 模拟发送延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ 短信通知发送成功: phone={}", phone);
    }

    /**
     * 发送系统内通知（模拟）
     * 
     * @param eBanUserId eBan用户ID
     * @param message 消息内容
     * @param alertRecord 预警记录
     */
    private void sendSystemNotification(String eBanUserId, String message, AlertRecordDTO alertRecord) {
        // 实际项目中这里会调用系统内通知服务
        log.info("💻 发送系统内通知: eBanUserId={}, alertId={}", eBanUserId, alertRecord.getAlertId());
        
        // 模拟发送延迟
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ 系统内通知发送成功: eBanUserId={}", eBanUserId);
    }

    /**
     * 记录通知发送日志
     * 
     * @param receiver 接收人
     * @param alertRecord 预警记录
     */
    private void logNotificationSent(OrderAlertConfig.Receiver receiver, AlertRecordDTO alertRecord) {
        log.info("📝 通知发送日志记录: receiver={}, alertId={}, time={}", 
                receiver.getName(), alertRecord.getAlertId(), formatDateTime(LocalDateTime.now()));
    }

    /**
     * 格式化日期时间
     * 
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取预警配置信息
     * 
     * @return 预警配置信息
     */
    public String getAlertConfigInfo() {
        StringBuilder info = new StringBuilder();
        info.append("📋 预警配置信息：\n");
        info.append("   省份：").append(orderAlertConfig.getProvince()).append("\n");
        info.append("   阈值：").append(orderAlertConfig.getThresholds()).append("\n");
        info.append("   时间窗口：").append(orderAlertConfig.getTimeWindow()).append("小时\n");
        info.append("   渠道：").append(orderAlertConfig.getSource()).append("\n");
        info.append("   预警管理员数量：").append(orderAlertConfig.getManagers() != null ? orderAlertConfig.getManagers().size() : 0).append("\n");
        
        if (orderAlertConfig.getManagers() != null) {
            for (OrderAlertConfig.Manager manager : orderAlertConfig.getManagers()) {
                info.append("   管理员ID：").append(manager.getId()).append("\n");
                if (manager.getReceiver() != null) {
                    info.append("   接收人数量：").append(manager.getReceiver().size()).append("\n");
                    for (OrderAlertConfig.Receiver receiver : manager.getReceiver()) {
                        info.append("     - ").append(receiver.getName()).append(" (").append(receiver.getPhone()).append(")\n");
                    }
                }
            }
        }
        
        return info.toString();
    }
}
