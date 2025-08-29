# 简化限流模块迁移指南

## 概述

这是一个简化的限流模块，专注于核心的限流逻辑，使用内存存储和简单的模拟数据。适合学习和迁移到实际项目中。

## 项目结构

```
com.picc.java.learn/
├── ratelimit/              # 限流核心模块
│   ├── RateLimit.java          # 限流注解
│   ├── RateLimitException.java # 限流异常
│   ├── RateLimitCounter.java   # 限流计数器
│   ├── RateLimitManager.java   # 限流管理器
│   └── RateLimitAspect.java    # 限流切面
├── dto/                   # 数据传输对象
│   └── UserDTO.java           # 用户DTO
├── vo/                    # 视图对象
│   ├── UserVO.java            # 用户VO
│   └── ResultVO.java          # 统一响应结果
├── service/               # 服务层
│   └── UserService.java       # 用户服务
├── api/                   # API层
│   └── UserController.java    # 用户控制器
└── context/               # 上下文
    └── SimpleUserContextProvider.java # 用户上下文提供者
```

## 核心文件说明

### 1. 限流注解 (@RateLimit)
```java
@RateLimit(timeWindow = 60, maxRequests = 100, dimension = RateLimit.LimitDimension.USER_IP)
public UserVO getUserInfo(String userId) {
    // 业务逻辑
}
```

**参数说明：**
- `timeWindow`: 时间窗口（秒）
- `maxRequests`: 最大请求次数
- `dimension`: 限流维度（USER_IP 或 METHOD_ONLY）
- `message`: 限流时的错误消息

### 2. 限流维度
- **USER_IP**: 按用户+IP限流，不同用户或不同IP有独立的计数器
- **METHOD_ONLY**: 按方法限流，所有请求共享同一个计数器

## 迁移步骤

### 1. 复制核心文件

**必需文件：**
- `RateLimit.java` - 限流注解
- `RateLimitException.java` - 限流异常
- `RateLimitCounter.java` - 限流计数器
- `RateLimitManager.java` - 限流管理器
- `RateLimitAspect.java` - 限流切面

### 2. 在服务方法上添加注解

```java
@Service
public class UserService {
    
    @RateLimit(timeWindow = 60, maxRequests = 100, dimension = RateLimit.LimitDimension.USER_IP)
    public UserVO getUserInfo(String userId) {
        // 业务逻辑
        return userInfo;
    }
    
    @RateLimit(timeWindow = 60, maxRequests = 20, dimension = RateLimit.LimitDimension.METHOD_ONLY)
    public boolean login(String username, String password) {
        // 登录逻辑
        return loginResult;
    }
}
```

### 3. 实现用户上下文提供者

根据你的项目框架实现：

**Spring Boot 示例：**
```java
@Component
public class SpringUserContextProvider implements RateLimitAspect.UserContextProvider {
    
    @Override
    public String getCurrentUserId() {
        // 从Spring Security获取用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
    
    @Override
    public String getClientIpAddress() {
        // 从HttpServletRequest获取IP
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getRemoteAddr();
    }
}
```

**其他框架示例：**
```java
public class CustomUserContextProvider implements RateLimitAspect.UserContextProvider {
    
    private final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private final ThreadLocal<String> currentIpAddress = new ThreadLocal<>();
    
    @Override
    public String getCurrentUserId() {
        return currentUserId.get();
    }
    
    @Override
    public String getClientIpAddress() {
        return currentIpAddress.get();
    }
    
    // 提供设置方法
    public void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }
    
    public void setCurrentIpAddress(String ipAddress) {
        currentIpAddress.set(ipAddress);
    }
}
```

### 4. 配置AOP切面

**Spring Boot 配置：**
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect(RateLimitManager.getInstance(), new SpringUserContextProvider());
    }
}
```

**手动配置：**
```java
// 创建限流切面
RateLimitAspect aspect = new RateLimitAspect(
    RateLimitManager.getInstance(), 
    new CustomUserContextProvider()
);
```

### 5. 处理限流异常

在控制器中捕获限流异常：

```java
@RestController
public class UserController {
    
    @GetMapping("/user/{userId}")
    public ResultVO<UserVO> getUserInfo(@PathVariable String userId) {
        try {
            UserVO userVO = userService.getUserInfo(userId);
            return ResultVO.success(userVO);
        } catch (RateLimitException e) {
            return ResultVO.rateLimit(e.getMessage());
        } catch (Exception e) {
            return ResultVO.error(e.getMessage());
        }
    }
}
```

## 使用示例

### 1. 基本使用

```java
@Service
public class OrderService {
    
    @RateLimit(timeWindow = 300, maxRequests = 5, dimension = RateLimit.LimitDimension.USER_IP)
    public boolean createOrder(String userId, OrderDTO order) {
        // 创建订单逻辑
        return orderResult;
    }
}
```

### 2. 手动限流

```java
@Service
public class PaymentService {
    
    @Autowired
    private RateLimitManager rateLimitManager;
    
    public boolean processPayment(String userId) {
        // 手动限流检查
        String limitKey = rateLimitManager.generateLimitKey(
            RateLimit.LimitDimension.USER_IP, 
            "processPayment", 
            userId, 
            getClientIp()
        );
        
        rateLimitManager.tryAccess(limitKey, 60, 10, "支付请求过于频繁");
        
        // 支付逻辑
        return paymentResult;
    }
}
```

## 扩展功能

### 1. 替换存储实现

如果需要分布式限流，可以修改 `RateLimitManager` 中的存储实现：

```java
// 将 ConcurrentHashMap 替换为 Redis 或其他分布式存储
private final RedisTemplate<String, RateLimitCounter> counters = new RedisTemplate<>();
```

### 2. 添加监控

```java
public class MonitoredRateLimitManager extends RateLimitManager {
    
    @Override
    public void tryAccess(String limitKey, int timeWindow, int maxRequests, String message) {
        try {
            super.tryAccess(limitKey, timeWindow, maxRequests, message);
            // 记录成功指标
            recordSuccess(limitKey);
        } catch (RateLimitException e) {
            // 记录限流指标
            recordRateLimit(limitKey);
            throw e;
        }
    }
}
```

## 注意事项

1. **线程安全**: 当前实现使用 `ConcurrentHashMap` 和 `AtomicInteger`，保证线程安全
2. **内存管理**: 定时清理过期的计数器，避免内存泄漏
3. **性能考虑**: 限流检查是内存操作，性能很高
4. **单机限制**: 当前实现仅适用于单机环境，集群环境需要分布式存储

## 常见问题

**Q: 如何支持集群环境？**
A: 将 `RateLimitManager` 中的 `ConcurrentHashMap` 替换为 Redis 或其他分布式存储。

**Q: 如何处理存储异常？**
A: 在 `RateLimitManager.tryAccess()` 方法中添加异常处理逻辑。

**Q: 如何添加自定义限流算法？**
A: 修改 `RateLimitCounter.tryIncrement()` 方法实现你的算法。

**Q: 如何集成到现有框架？**
A: 参考上面的 Spring Boot 示例，根据你的框架调整实现方式。
