# Cursor 配置指南

## 概述
Cursor 是一个AI驱动的代码编辑器，通过配置文件可以自定义AI助手的行为和规则。

## 配置文件说明

### 1. .cursorrules 文件
这是最重要的配置文件，定义了AI助手的行为规则：

**位置**: 项目根目录下的 `.cursorrules` 文件

**作用**:
- 定义代码风格和标准
- 设置项目特定的规则
- 指导AI如何生成和修改代码
- 确保代码一致性

**已配置的规则**:
- Java 8+ 最佳实践
- Spring Boot 约定
- Maven 项目结构
- RESTful API 设计
- 代码质量要求
- 测试标准

### 2. .cursorignore 文件
告诉Cursor AI忽略某些文件：

**位置**: 项目根目录下的 `.cursorignore` 文件

**作用**:
- 排除构建输出文件
- 忽略IDE配置文件
- 排除日志文件
- 提高AI分析效率

## 如何使用

### 1. 基本使用
- 在Cursor中打开项目
- AI会自动读取 `.cursorrules` 文件
- 所有代码生成和修改都会遵循这些规则

### 2. 自定义规则
你可以编辑 `.cursorrules` 文件来添加或修改规则：

```bash
# 编辑规则文件
code .cursorrules
```

### 3. 规则示例

#### 添加新的代码风格规则
```
## 新增规则
- 使用 Stream API 处理集合
- 优先使用 Optional 处理可能为null的值
- 使用 Builder 模式创建复杂对象
```

#### 添加项目特定规则
```
## 业务规则
- 所有订单相关操作必须记录审计日志
- 用户操作必须进行权限检查
- 敏感数据必须加密存储
```

## 高级配置

### 1. 模块特定规则
可以为不同的模块设置不同的规则：

```
## day3 模块规则
- 使用 Redis 进行缓存
- 实现订单超时处理
- 使用定时任务处理积压订单

## day4 模块规则  
- 实现风险控制逻辑
- 使用滑动窗口算法
- 添加实时监控功能
```

### 2. 技术栈特定规则
```
## Redis 使用规则
- 使用连接池管理连接
- 设置合理的过期时间
- 处理连接异常

## Spring Boot 规则
- 使用 @Transactional 管理事务
- 使用 @Async 处理异步任务
- 使用 @Cacheable 实现缓存
```

## 最佳实践

### 1. 规则编写
- 使用清晰、具体的描述
- 提供代码示例
- 保持规则的一致性
- 定期更新规则

### 2. 规则组织
- 按功能模块分组
- 使用注释说明规则目的
- 保持规则的逻辑顺序

### 3. 团队协作
- 团队成员共享相同的规则文件
- 定期审查和更新规则
- 记录规则变更历史

## 故障排除

### 1. 规则不生效
- 检查文件位置是否正确
- 确认文件格式正确
- 重启Cursor编辑器

### 2. 规则冲突
- 检查规则是否有矛盾
- 使用优先级排序
- 简化复杂规则

### 3. 性能问题
- 优化 `.cursorignore` 文件
- 减少不必要的规则
- 使用更具体的规则描述

## 更新日志

### v1.0 (当前版本)
- 基础Java项目规则
- Spring Boot最佳实践
- Maven项目结构支持
- 代码质量要求
- 测试标准

## 参考资源
- [Cursor官方文档](https://cursor.sh/docs)
- [Java编码规范](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot最佳实践](https://spring.io/guides)
- [Maven项目结构](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
