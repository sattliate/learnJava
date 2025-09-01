# Day3 - 订单预警模块

## 项目概述

订单预警模块是一个基于Spring Boot的实时监控系统，用于监控会员在特定省份和渠道的订单数量，当超过预设阈值时自动触发预警。

## 核心功能

### 🚨 订单预警
- **实时监控**: 监控会员手机号 + 省份 + 渠道三个维度的订单数量
- **阈值检测**: 当订单数量超过配置阈值时自动触发预警
- **异步处理**: 使用`@Async`异步处理预警检测，不影响订单创建性能
- **通知发送**: 自动发送预警通知给配置的接收人

### ⚡ 订单速度监控
- **Redis Sorted Set**: 使用时间戳作为score存储订单，支持实时速度计算
- **多时间窗口**: 支持1小时、6小时、12小时、24小时等不同时间窗口的速度分析
- **自动清理**: 12小时时间窗口后数据自动过期

### 📊 系统监控
- **实时统计**: 订单总数、预警记录数、Redis键数量等
- **状态监控**: 系统运行状态、性能指标等
- **配置管理**: 预警阈值、时间窗口、接收人等配置

### 🔇 预警免打扰机制

系统实现了智能的预警免打扰机制，平衡预警及时性和干扰性：

- **免打扰时间段**：第一次预警后，下次预警时间间隔依次为10分钟、30分钟、60分钟
- **持续监控模式**：超过3次预警后，使用最后一个免打扰时间段（60分钟）作为预警频率
- **Redis实现**：使用Redis过期key实现免打扰状态，支持持久化和分布式部署
- **配置灵活**：支持YAML配置免打扰参数
- **智能切换**：系统自动在密集预警和规律预警之间切换

## 技术架构

### 后端技术栈
- **Spring Boot 2.7.x**: 主框架
- **Spring Async**: 异步处理
- **Lombok**: 代码简化
- **Bean Validation**: 数据验证
- **Mock Services**: 模拟Redis和数据库服务

### 核心组件
- **OrderService**: 订单服务，处理订单创建和预警检测
- **AlertNotificationService**: 预警通知服务，发送预警消息
- **MockRedisService**: 模拟Redis服务，管理订单计数
- **MockRedisSortedSetService**: 模拟Redis Sorted Set，监控订单速度
- **MockDatabaseService**: 模拟数据库服务，存储订单和预警记录

## 配置说明

### 预警配置 (application.yml)
```yaml
club:
  alert:
    province: 44000000          # 监控省份
    thresholds: 10              # 预警阈值
    time-window: 12            # 时间窗口（小时）
    source: 14,28,39          # 监控渠道
    managers:                   # 预警管理员
      - id: 36
        receiver:               # 接收人列表
          - phone: "18622201499"
            name: "张丽"
            eBanUserId: "12438032-VvhmMFJzjW8jtWyvm0mHomXxqugMo6Bw"
```

### 系统配置
- **端口**: 8082
- **上下文路径**: /day3
- **日志级别**: DEBUG (开发环境)

## API接口

### 订单管理
- `POST /api/orders` - 创建订单
- `GET /api/orders/member/{memberPhone}` - 查询会员订单
- `GET /api/orders/search` - 按省份和渠道搜索订单
- `GET /api/orders/count` - 获取订单总数

### 预警管理
- `GET /api/orders/alerts` - 获取所有预警记录

### 速度监控
- `GET /api/orders/speed` - 获取订单速度信息
- `GET /api/orders/speed/multi` - 获取多时间窗口速度信息

### 系统状态
- `GET /api/orders/status` - 获取系统状态信息

## 测试指南

### 1. 启动项目
```bash
# Windows
start-day3.bat

# Linux/Mac
cd day3
mvn spring-boot:run
```

### 2. 访问测试页面
- 测试页面: http://localhost:8082/day3/index.html
- API文档: http://localhost:8082/day3/api/orders

### 3. 测试预警功能

#### 快速测试（1分钟内创建10个订单）
1. 打开测试页面
2. 在"批量创建订单"区域设置：
   - 订单数量: 10
   - 创建间隔: 1000毫秒（1秒）
   - 省份: 44000000（广东省）
   - 渠道: 14
3. 点击"🚀 开始批量创建"
4. 观察控制台日志，查看预警触发情况

#### 预期结果
- 前9个订单：正常创建，无预警
- 第10个订单：触发预警，创建预警记录
- 控制台显示预警通知发送日志
- 预警记录包含接收人信息

### 4. 测试速度监控
1. 创建多个订单后
2. 使用"订单速度监控"功能
3. 选择不同时间窗口查看速度信息

## 预警流程

### 1. 订单创建
```
用户创建订单 → OrderService.createOrder()
```

### 2. 异步预警检测
```
@Async asyncCheckOrderAlert() → 检查监控范围 → 增加Redis计数 → 检查阈值
```

### 3. 预警触发
```
超过阈值 → 创建预警记录 → 发送预警通知 → 记录日志
```

### 4. 通知发送
```
AlertNotificationService → 构建消息 → 发送给所有接收人 → 记录发送状态
```

## 监控维度

### 订单计数
- **Redis Key**: `memberPhone-channel-registerId`
- **存储方式**: 键值对，带TTL过期时间
- **更新方式**: 每次订单创建时递增计数

### 订单速度
- **Redis Key**: `memberPhone-channel-registerId`
- **存储方式**: Sorted Set，订单ID为member，时间戳为score
- **计算方式**: 统计指定时间窗口内的订单数量

## 开发说明

### 项目结构
```
day3/
├── src/main/java/com/picc/java/learn/alert/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── dto/            # 数据传输对象
│   ├── service/        # 业务服务
│   ├── vo/             # 视图对象
│   └── common/         # 通用类
├── src/main/resources/
│   ├── static/         # 静态资源（测试页面）
│   └── application.yml # 配置文件
└── pom.xml             # Maven配置
```

### 扩展建议
1. **真实Redis集成**: 替换MockRedisService为真实Redis客户端
2. **数据库集成**: 替换MockDatabaseService为MyBatis/JPA
3. **通知服务**: 集成短信、邮件、钉钉等通知渠道
4. **监控告警**: 集成Prometheus、Grafana等监控系统
5. **分布式支持**: 使用Redis分布式锁、消息队列等

## 注意事项

1. **时间窗口**: 当前固定为12小时，可根据业务需求调整
2. **阈值配置**: 预警阈值在YAML中配置，支持动态调整
3. **异步处理**: 预警检测使用异步处理，避免影响订单创建性能
4. **模拟服务**: 当前使用Mock服务，生产环境需要替换为真实服务
5. **错误处理**: 预警通知失败不影响订单创建，只记录错误日志

## 常见问题

### Q: 为什么没有触发预警？
A: 检查以下几点：
- 省份是否匹配配置的`club.alert.province`
- 渠道是否在配置的`club.alert.source`列表中
- 订单数量是否超过`club.alert.thresholds`阈值

### Q: 预警通知发送给谁？
A: 查看`club.alert.managers`配置，每个管理员下的`receiver`列表就是接收人

### Q: 如何调整预警阈值？
A: 修改`application.yml`中的`club.alert.thresholds`值

### Q: 时间窗口可以调整吗？
A: 当前固定为12小时，如需调整需要修改代码中的时间窗口逻辑
