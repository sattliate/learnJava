# Day4 订单预警风险管控系统

## 🚨 项目概述

Day4是基于Day3订单预警系统的风险管控增强版本，新增了智能风险识别、实时预警监控、自动化阻断管理等核心功能。系统采用多维度权重计算算法，能够自动识别风险行为并采取相应的管控措施。

## ✨ 新功能亮点

### 🔍 智能风险管控
- **多维度权重计算**：基于省份、渠道、电话号码等维度的智能风险评估
- **实时风险监控**：24小时不间断监控，及时发现异常行为
- **自动化阻断机制**：达到风险阈值时自动阻断相关渠道

### 📊 预警次数显示
- **第N次预警**：支持显示"第3次预警通知"等预警次数统计
- **预警历史追踪**：完整记录每次预警的详细信息
- **预警趋势分析**：分析预警频率和模式变化

### ⚖️ 权重表管理
- **独立维度管理**：省份、渠道、电话号码分别管理
- **权重计算规则**：支持多种风险行为的权重计算
- **动态权重调整**：根据风险行为自动调整权重值

### 🚫 阻断管理
- **智能阻断**：基于风险权重自动阻断高风险渠道
- **阻断解除**：支持管理员手动解除阻断
- **阻断轨迹**：完整记录阻断和解除的操作历史

## 🏗️ 系统架构

### 核心组件
```
Day4 风险管控系统
├── 配置管理层 (RiskControlConfig)
├── 数据模型层 (DTOs)
│   ├── AlertDimensionDTO - 预警维度
│   ├── WeightRecordDTO - 权重记录
│   └── BlockRecordDTO - 阻断记录
├── 业务逻辑层 (Services)
│   ├── WeightCalculationService - 权重计算
│   ├── BlockControlService - 阻断控制
│   └── AlertDetectionService - 预警检测
├── 控制层 (Controllers)
│   └── RiskControlController - 风险管控API
└── 前端界面层
    ├── 风险管控管理界面
    ├── API测试平台
    └── 系统导航首页
```

### 技术栈
- **后端**：Spring Boot 2.7.x + Java 8+
- **前端**：HTML5 + CSS3 + JavaScript (ES6+)
- **数据存储**：内存存储 (ConcurrentHashMap)
- **构建工具**：Maven 3.6+
- **开发工具**：Lombok, Slf4j

## 📋 功能特性

### 风险权重计算规则

#### 1. 身份证修改风险
- **阈值**：当天超过3次
- **权重**：首次超过+30，以后每次+10
- **配置**：支持YAML配置调整

#### 2. 手机号修改风险
- **阈值**：当天超过3次
- **权重**：首次超过+30，以后每次+10
- **配置**：支持YAML配置调整

#### 3. 注销重注册风险
- **阈值**：当天超过2次
- **权重**：首次超过+30，以后每次+10
- **配置**：支持YAML配置调整

#### 4. 同手机号注册风险
- **阈值**：当天超过3次
- **权重**：首次超过+10，以后每次+10
- **配置**：支持YAML配置调整

#### 5. 特殊产品风险
- **产品列表**：红包、停车券、代金券、话费、肯德基、麦当劳、奈雪、喜茶、霸王茶姬
- **权重**：每次订单+2
- **配置**：支持YAML配置调整

#### 6. 无有效保单风险
- **权重**：每次+5
- **配置**：支持YAML配置调整

### 阻断规则

#### 1. 权重阻断
- **阈值**：累加权重超过60分
- **动作**：自动阻断当前渠道
- **通知**：通知相关人员

#### 2. 连续预警阻断
- **阈值**：连续预警超过3次
- **动作**：自动阻断当前渠道
- **配置**：支持YAML配置调整

#### 3. 订单数量阻断
- **阈值**：1小时内预警订单超过50个
- **动作**：自动阻断当前渠道
- **配置**：支持YAML配置调整

#### 4. 连续天数阻断
- **阈值**：连续5天中有3天存在预警
- **动作**：自动阻断当前渠道
- **配置**：支持YAML配置调整

## 🚀 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- 现代浏览器 (Chrome, Firefox, Safari, Edge)

### 启动步骤

#### 1. 编译项目
```bash
cd day4
mvn clean compile
```

#### 2. 启动应用
```bash
mvn spring-boot:run
```

#### 3. 访问系统
- **主页面**：http://localhost:8080/day3/index-day4.html
- **风险管控管理**：http://localhost:8080/day3/risk-control.html
- **API测试平台**：http://localhost:8080/day3/risk-control-test.html

### 使用说明

#### 1. 风险管控管理
- 查看预警维度信息
- 分析权重记录数据
- 管理渠道阻断状态
- 解除渠道阻断

#### 2. API测试平台
- 测试系统状态
- 验证接口功能
- 执行完整流程测试
- 导出系统数据

#### 3. 快速操作
- 系统状态检查
- 加载系统统计
- 导出系统数据
- 清空测试数据

## ⚙️ 配置说明

### 主要配置参数

```yaml
club:
  alert:
    risk-control:
      enabled: true                    # 是否启用风险管控
      weight:
        threshold: 60                  # 累加权重阻断阈值
      
      # 身份证修改风险配置
      id-card-modify:
        threshold: 3                   # 当天修改次数阈值
        initial-weight: 30             # 首次超过阈值累加权重
        additional-weight: 10          # 以后每多修改一次累加权重
      
      # 手机号修改风险配置
      phone-modify:
        threshold: 3
        initial-weight: 30
        additional-weight: 10
      
      # 注销重注册风险配置
      re-register:
        threshold: 2
        initial-weight: 30
        additional-weight: 10
      
      # 同手机号注册风险配置
      same-phone-register:
        threshold: 3
        initial-weight: 10
        additional-weight: 10
      
      # 特殊产品名称列表
      special-products:
        - 红包
        - 停车券
        - 代金券
        - 话费
        - 肯德基
        - 麦当劳
        - 奈雪
        - 喜茶
        - 霸王茶姬
      
      no-valid-policy-weight: 5       # 无有效保单权重
      
      # 阻断规则配置
      blocking:
        consecutive-alerts:            # 连续预警阻断
          enabled: true
          threshold: 3                 # 连续预警次数阈值
        order-quantity:                # 预警订单数量阻断
          enabled: true
          time-window-hours: 1         # 时间窗口（小时）
          threshold: 50                # 订单数量阈值
        continuous-days:               # 连续M天N天预警阻断
          enabled: true
          m-days: 5                   # 连续天数M
          n-days-with-alerts: 3       # 存在预警的天数N
```

## 🔧 API接口

### 风险管控接口

#### 1. 查询预警维度
- **接口**：`POST /day3/risk-control/dimensions`
- **功能**：获取所有预警维度信息
- **返回**：预警维度列表

#### 2. 查询权重记录
- **接口**：`POST /day3/risk-control/weight-records`
- **功能**：获取所有权重记录
- **返回**：权重记录列表

#### 3. 查询阻断记录
- **接口**：`POST /day3/risk-control/block-records`
- **功能**：获取所有阻断记录
- **返回**：阻断记录列表

#### 4. 查询维度详情
- **接口**：`POST /day3/risk-control/dimension-detail`
- **参数**：`{ "alertKey": "预警维度Key" }`
- **功能**：获取特定预警维度的详细信息
- **返回**：维度、权重、阻断的完整信息

#### 5. 解除渠道阻断
- **接口**：`POST /day3/risk-control/unblock`
- **参数**：
  ```json
  {
    "alertKey": "预警维度Key",
    "unblockUser": "解除人",
    "unblockUserRole": "用户角色",
    "unblockReason": "解除原因"
  }
  ```
- **功能**：解除指定渠道的阻断状态
- **返回**：操作结果

#### 6. 获取系统配置
- **接口**：`POST /day3/risk-control/config`
- **功能**：获取风险管控系统配置
- **返回**：系统配置信息

#### 7. 获取统计信息
- **接口**：`POST /day3/risk-control/statistics`
- **功能**：获取系统统计信息
- **返回**：统计数据

#### 8. 清空所有数据
- **接口**：`POST /day3/risk-control/clear-all-data`
- **功能**：清空所有风险管控数据（仅用于测试）
- **返回**：操作结果

## 📊 数据模型

### AlertDimensionDTO (预警维度)
```java
public class AlertDimensionDTO {
    private Long dimensionId;          // 预警维度ID
    private String memberPhone;        // 会员手机号
    private String province;           // 省份代码
    private String channel;            // 渠道代码
    private String registerId;         // 注册ID
    private String alertKey;           // 预警维度Key
    private Boolean isBlocked;         // 是否被阻断
    private String blockReason;        // 阻断原因
    private LocalDateTime blockTime;   // 阻断时间
    // ... 其他字段
}
```

### WeightRecordDTO (权重记录)
```java
public class WeightRecordDTO {
    private Long weightId;             // 权重记录ID
    private String alertKey;           // 预警维度Key
    private Integer alertCount;        // 预警次数
    private Double totalWeight;        // 总权重值
    private String riskLevel;          // 风险等级
    private Integer idCardModifyCount; // 身份证修改次数
    private Double idCardModifyWeight; // 身份证修改权重
    // ... 其他风险因子字段
}
```

### BlockRecordDTO (阻断记录)
```java
public class BlockRecordDTO {
    private Long blockId;              // 阻断记录ID
    private String alertKey;           // 预警维度Key
    private String blockReason;        // 阻断原因
    private String blockType;          // 阻断类型
    private LocalDateTime blockTime;   // 阻断时间
    private String blockStatus;        // 阻断状态
    // ... 其他字段
}
```

## 🧪 测试说明

### 测试环境
- 系统使用内存存储，重启后数据会丢失
- 所有风险数据都是模拟生成，用于功能验证
- 支持清空所有数据功能，方便重复测试

### 测试流程
1. **系统状态检查**：验证系统是否正常运行
2. **功能接口测试**：测试各项API接口功能
3. **业务流程测试**：模拟完整的风险管控流程
4. **数据导出测试**：验证数据导出功能

### 测试数据
- 预警维度：模拟生成多个预警维度
- 权重记录：基于风险规则计算权重
- 阻断记录：记录阻断和解除操作

## 🔒 权限管理

### 用户角色
- **ADMIN**：管理员，拥有所有权限
- **MANAGER**：经理，拥有大部分权限
- **SUPERVISOR**：主管，拥有基本权限

### 权限控制
- 解除阻断需要管理岗位权限
- 清空数据需要管理员权限
- 查询功能对所有角色开放

## 📝 开发说明

### 代码结构
```
day4/
├── src/main/java/com/picc/java/learn/alert/
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── dto/             # 数据传输对象
│   └── service/         # 业务服务
├── src/main/resources/
│   ├── static/          # 静态资源
│   └── application.yml  # 配置文件
└── pom.xml              # Maven配置
```

### 扩展开发
- 新增风险因子：在`WeightRecordDTO`中添加字段，在`WeightCalculationService`中实现计算逻辑
- 新增阻断规则：在`RiskControlConfig`中添加配置，在`BlockControlService`中实现判断逻辑
- 新增管理功能：在`RiskControlController`中添加接口，在前端页面中添加对应功能

## 🚨 注意事项

### 生产环境
- 当前使用内存存储，生产环境需要替换为数据库存储
- 风险计算规则需要根据实际业务需求调整
- 阻断阈值需要根据业务风险承受能力设置

### 性能优化
- 大量数据时需要考虑分页查询
- 权重计算可以考虑异步处理
- 阻断判断可以添加缓存机制

### 安全考虑
- 解除阻断操作需要严格的权限验证
- 敏感操作需要记录详细的操作日志
- 外部接口需要添加访问频率限制

## 📞 技术支持

### 联系方式
- 项目维护：学习Java项目组
- 技术支持：通过项目Issues反馈问题
- 功能建议：欢迎提交功能改进建议

### 更新日志
- **v4.0** (2025-01-01)：风险管控增强版发布
  - 新增智能风险识别功能
  - 新增自动化阻断管理
  - 新增完整的Web管理界面
  - 支持预警次数显示
  - 支持权重表管理

## 📄 许可证

本项目仅供学习使用，请勿用于商业用途。

---

**🚨 Day4 订单预警风险管控系统** - 让风险管控更智能、更高效！
