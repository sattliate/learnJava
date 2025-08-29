package com.picc.java.learn.ratelimit;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 演示限流注解的实际使用效果
 * 
 * @author learn
 * @since 1.0
 */
public class UserServiceImpl implements UserService {
    
    /**
     * 模拟用户数据存储
     */
    private final Map<String, String> userDatabase = new HashMap<>();
    
    /**
     * 模拟用户信息存储
     */
    private final Map<String, String> userInfoDatabase = new HashMap<>();
    
    public UserServiceImpl() {
        // 初始化一些测试数据
        userDatabase.put("user1", "password1");
        userDatabase.put("user2", "password2");
        userInfoDatabase.put("user1", "用户1的信息");
        userInfoDatabase.put("user2", "用户2的信息");
    }
    
    @Override
    public String getUserInfo(String userId) {
        // 模拟业务逻辑处理时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String userInfo = userInfoDatabase.get(userId);
        if (userInfo == null) {
            return "用户不存在: " + userId;
        }
        return userInfo;
    }
    
    @Override
    public boolean updateUserInfo(String userId, String userInfo) {
        // 模拟业务逻辑处理时间
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (!userDatabase.containsKey(userId)) {
            return false;
        }
        
        userInfoDatabase.put(userId, userInfo);
        return true;
    }
    
    @Override
    public boolean deleteUser(String userId) {
        // 模拟业务逻辑处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (!userDatabase.containsKey(userId)) {
            return false;
        }
        
        userDatabase.remove(userId);
        userInfoDatabase.remove(userId);
        return true;
    }
    
    @Override
    public boolean login(String username, String password) {
        // 模拟业务逻辑处理时间
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String storedPassword = userDatabase.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
    
    @Override
    public boolean register(String username, String password, String email) {
        // 模拟业务逻辑处理时间
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (userDatabase.containsKey(username)) {
            return false; // 用户已存在
        }
        
        userDatabase.put(username, password);
        userInfoDatabase.put(username, "新用户信息");
        return true;
    }
    
    /**
     * 获取用户数据库大小（用于测试）
     */
    public int getUserCount() {
        return userDatabase.size();
    }
    
    /**
     * 清空用户数据（用于测试）
     */
    public void clearUsers() {
        userDatabase.clear();
        userInfoDatabase.clear();
    }
}
