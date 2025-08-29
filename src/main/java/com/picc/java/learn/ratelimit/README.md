# 限流模块 (Rate Limit Module)

## 概述

这是一个基于注解的Java限流模块，支持精细化限流控制，可以按用户+IP或仅按方法进行限流。

## 功能特性

### ✅ 核心功能
- **注解驱动**: 使用`@RateLimit`注解轻松实现限流
- **多维度限流**: 支持用户+IP维度和方法级别限流
- **时间窗口控制**: 可配置的时间窗口和最大请求次数
- **线程安全**: 使用原子操作和并发集合保证线程安全
- **自动清理**: 定时清理过期的限流计数器

### 🎯 限流维度

#### 1. 用户+IP维度 (USER_IP)
- 相同用户 + 相同IP = 同一限流维度
- 不同用户 = 不同限流维度
- 相同用户 + 不同IP = 不同限流维度
- 适用于：防止单个用户恶意刷接口

#### 2. 方法级别 (METHOD_ONLY)
- 所有请求共享同一个限流维度
- 适用于：防止暴力破解、全局接口保护

## 快速开始

### 1. 添加注解到接口方法

```java
@RateLimit(timeWindow = 60, maxRequests = 100, dimension = RateLimit.LimitDimension.USER_IP)
String getUserInfo(String userId);

@RateLimit(timeWindow = 60, maxRequests = 20, dimension = RateLimit.LimitDimension.METHOD_ONLY)
boolean login(String username, String password);
```

### 2. 配置AOP切面

```java
// 在Spring项目中配置AOP
@Aspect
@Component
public class RateLimitAspect {
    // 实现限流逻辑
}
```

### 3. 运行测试

```bash
# 编译
javac -encoding UTF-8 -cp . src/main/java/com/picc/java/learn/ratelimit/*.java

# 运行测试
java -cp src/main/java com.picc.java.learn.ratelimit.RateLimitTest
```

## 核心组件

### 1. @RateLimit 注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int timeWindow() default 60;           // 时间窗口（秒）
    int maxRequests() default 100;         // 最大请求次数
    LimitDimension dimension() default LimitDimension.USER_IP;  // 限流维度
    String message() default "请求过于频繁，请稍后再试";  // 限流消息
}
```

### 2. RateLimitManager 限流管理器
- 单例模式，全局管理所有限流计数器
- 支持并发访问
- 自动清理过期计数器

### 3. RateLimitCounter 限流计数器
- 记录特定维度的请求次数
- 支持时间窗口重置
- 线程安全的原子操作

### 4. RateLimitException 限流异常
- 包含详细的限流信息
- 支持自定义错误消息

## 使用示例

### 示例1: 用户信息接口限流
```java
@RateLimit(timeWindow = 60, maxRequests = 100, dimension = RateLimit.LimitDimension.USER_IP)
public String getUserInfo(String userId) {
    // 业务逻辑
    return userInfo;
}
```

### 示例2: 登录接口限流（防止暴力破解）
```java
@RateLimit(timeWindow = 60, maxRequests = 20, dimension = RateLimit.LimitDimension.METHOD_ONLY)
public boolean login(String username, String password) {
    // 登录逻辑
    return loginResult;
}
```

### 示例3: 敏感操作限流
```java
@RateLimit(timeWindow = 300, maxRequests = 5, dimension = RateLimit.LimitDimension.USER_IP,
           message = "操作过于频繁，请5分钟后再试")
public boolean deleteUser(String userId) {
    // 删除用户逻辑
    return deleteResult;
}
```

## 测试用例

运行 `RateLimitTest` 类可以测试以下场景：

1. **基本限流功能**: 验证限流的基本工作
2. **用户+IP维度限流**: 验证不同用户+IP组合的独立限流
3. **方法级别限流**: 验证全局方法限流
4. **并发限流测试**: 验证多线程环境下的限流效果
5. **时间窗口重置**: 验证时间窗口过期后的重置功能

## 配置说明

### 注解参数详解

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| timeWindow | int | 60 | 时间窗口大小（秒） |
| maxRequests | int | 100 | 时间窗口内最大请求次数 |
| dimension | LimitDimension | USER_IP | 限流维度 |
| message | String | "请求过于频繁，请稍后再试" | 限流时的错误消息 |

### 限流维度说明

| 维度 | 说明 | 适用场景 |
|------|------|----------|
| USER_IP | 按用户+IP限流 | 防止单个用户恶意刷接口 |
| METHOD_ONLY | 仅按方法限流 | 防止暴力破解、全局保护 |

## 性能考虑

1. **内存使用**: 使用ConcurrentHashMap存储计数器，支持自动清理
2. **线程安全**: 使用AtomicInteger保证计数操作的原子性
3. **清理机制**: 定时清理过期计数器，避免内存泄漏
4. **并发性能**: 支持高并发访问，无锁竞争

## 扩展功能

### 自定义用户上下文提供者
```java
public class CustomUserContextProvider implements RateLimitAspect.UserContextProvider {
    @Override
    public String getCurrentUserId() {
        // 从ThreadLocal或Session中获取用户ID
        return getCurrentUser().getId();
    }
}
```

### 自定义IP地址提供者
```java
public class CustomIpAddressProvider implements RateLimitAspect.IpAddressProvider {
    @Override
    public String getClientIpAddress() {
        // 从HttpServletRequest中获取客户端IP
        return request.getRemoteAddr();
    }
}
```

## 注意事项

1. **时间精度**: 当前实现使用固定时间窗口，不支持滑动窗口
2. **分布式环境**: 当前实现仅适用于单机环境，分布式环境需要Redis等外部存储
3. **异常处理**: 限流异常需要在上层进行统一处理
4. **监控告警**: 建议添加限流监控和告警机制

## 版本信息

- **版本**: 1.0
- **作者**: learn
- **创建时间**: 2024年
- **Java版本**: 8+
