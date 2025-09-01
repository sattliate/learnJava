# API接口修改说明

## 修改概述

根据用户要求，对 `OrderController` 进行了以下修改：

1. **去掉不必要的 `ResponseEntity` 包装**：直接返回 `ApiResponse<T>` 类型
2. **统一使用 POST 方法**：所有接口都改为 POST 请求
3. **使用 `ApiRequest` 接收参数**：前端传过来的参数都通过请求类进行接收
4. **添加参数验证**：使用 `@Valid` 注解进行参数验证

## 修改详情

### 1. 创建订单接口

**修改前：**
```java
@PostMapping
public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderVO orderVO)
```

**修改后：**
```java
@PostMapping
public ApiResponse<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request)
```

**请求示例：**
```json
{
  "memberPhone": "13800138000",
  "memberName": "张三",
  "province": "北京",
  "channel": "14",
  "registerId": "12345",
  "orderAmount": "1000.00",
  "orderStatus": "CREATED"
}
```

### 2. 查询会员订单接口

**修改前：**
```java
@GetMapping("/member/{memberPhone}")
public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByMemberPhone(@PathVariable String memberPhone)
```

**修改后：**
```java
@PostMapping("/member")
public ApiResponse<List<OrderDTO>> getOrdersByMemberPhone(@Valid @RequestBody MemberOrderQueryRequest request)
```

**请求示例：**
```json
{
  "memberPhone": "13800138000"
}
```

### 3. 搜索订单接口

**修改前：**
```java
@GetMapping("/search")
public ResponseEntity<ApiResponse<List<OrderDTO>>> searchOrders(@RequestParam String province, @RequestParam String channel)
```

**修改后：**
```java
@PostMapping("/search")
public ApiResponse<List<OrderDTO>> searchOrders(@Valid @RequestBody OrderSearchRequest request)
```

**请求示例：**
```json
{
  "province": "北京",
  "channel": "14"
}
```

### 4. 订单速度查询接口

**修改前：**
```java
@GetMapping("/speed")
public ResponseEntity<ApiResponse<OrderSpeedInfo>> getOrderSpeed(@RequestParam String memberPhone, ...)
```

**修改后：**
```java
@PostMapping("/speed")
public ApiResponse<OrderSpeedInfo> getOrderSpeed(@Valid @RequestBody OrderSpeedRequest request)
```

**请求示例：**
```json
{
  "memberPhone": "13800138000",
  "channel": "14",
  "registerId": "12345",
  "timeWindow": 12
}
```

### 5. 多时间窗口速度查询接口

**修改前：**
```java
@GetMapping("/speed/multi")
public ResponseEntity<ApiResponse<List<OrderSpeedInfo>>> getOrderSpeedMultiWindow(@RequestParam String memberPhone, ...)
```

**修改后：**
```java
@PostMapping("/speed/multi")
public ApiResponse<List<OrderSpeedInfo>> getOrderSpeedMultiWindow(@Valid @RequestBody OrderSpeedMultiRequest request)
```

**请求示例：**
```json
{
  "memberPhone": "13800138000",
  "channel": "14",
  "registerId": "12345",
  "timeWindows": "1,6,12,24"
}
```

## 新增的请求类

在 `OrderController` 中新增了以下内部类：

1. `CreateOrderRequest` - 创建订单请求
2. `MemberOrderQueryRequest` - 会员订单查询请求
3. `OrderSearchRequest` - 订单搜索请求
4. `OrderCountRequest` - 订单数量查询请求
5. `OrderSpeedRequest` - 订单速度查询请求
6. `OrderSpeedMultiRequest` - 多时间窗口速度查询请求
7. `TimeRangeStatsRequest` - 时间范围统计请求
8. `RecentStatsRequest` - 最近统计请求
9. `AlertQueryRequest` - 预警查询请求
10. `SystemStatusRequest` - 系统状态查询请求

## 优势

1. **统一性**：所有接口都使用 POST 方法和 JSON 格式
2. **类型安全**：使用强类型的请求类，避免参数错误
3. **验证支持**：支持参数验证和错误提示
4. **扩展性**：请求类可以轻松添加新字段
5. **文档友好**：请求类本身就是很好的API文档

## 测试

运行 `test-api-requests.bat` 可以测试所有修改后的接口。

## 注意事项

1. 前端需要将 GET 请求改为 POST 请求
2. 参数需要放在请求体中，而不是URL参数
3. 需要设置 `Content-Type: application/json` 头
4. 所有接口都返回 `ApiResponse<T>` 格式的响应

