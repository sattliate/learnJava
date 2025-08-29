# Day1 - Hello World Spring Boot 项目

## 项目简介

这是一个学习Spring Boot基础知识的示例项目，展示了Spring Boot的核心功能和基本配置。

## 项目结构

```
day1/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/picc/java/learn/
│   │   │       ├── Day1Application.java          # Spring Boot主启动类
│   │   │       ├── HelloWorld.java               # 原始的Hello World类
│   │   │       ├── controller/
│   │   │       │   └── HelloController.java      # Web控制器
│   │   │       └── config/
│   │   │           └── AppConfig.java            # 应用配置类
│   │   └── resources/
│   │       ├── application.yml                   # 主配置文件
│   │       └── application-dev.yml               # 开发环境配置
│   └── test/
│       └── java/
│           └── com/picc/java/learn/
│               └── Day1ApplicationTests.java     # 测试类
├── pom.xml                                       # Maven项目配置
└── README.md                                     # 项目说明文档
```

## 技术栈

- **Java**: 8+
- **Spring Boot**: 2.7.18
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code

## 核心依赖说明

### Spring Boot Starter Web
- **作用**: 提供Web开发相关依赖
- **包含**: spring-web, spring-webmvc, spring-core, spring-context等
- **用途**: 构建RESTful API和Web应用

### Spring Boot DevTools
- **作用**: 开发时热重载工具
- **功能**: 代码修改后自动重启，提高开发效率
- **注意**: 生产环境会自动排除此依赖

### Spring Boot Configuration Processor
- **作用**: 配置元数据生成
- **功能**: 为自定义配置类生成配置元数据，提供IDE智能提示

## 配置文件说明

### application.yml (主配置)
- 服务器端口: 8080
- 应用名称: day1-hello-world
- 日志配置: 控制台和文件输出
- 自定义配置: app.name, app.version等

### application-dev.yml (开发环境)
- 服务器端口: 8081 (覆盖主配置)
- 调试模式: 启用
- 日志级别: DEBUG

## 主要功能

### 1. Hello World 接口
- **路径**: `GET /day1/hello`
- **功能**: 返回基础欢迎信息
- **示例**: `Hello World from Day1 Hello World v1.0.0!`

### 2. 个性化问候接口
- **路径**: `GET /day1/hello/greeting?name=张三`
- **功能**: 返回个性化问候信息
- **示例**: `Hello 张三! 当前时间: 2025-08-30 12:00:00`

### 3. 应用信息接口
- **路径**: `GET /day1/hello/info`
- **功能**: 返回应用详细信息（JSON格式）
- **包含**: 应用名称、版本、当前时间、状态等

### 4. 健康检查接口
- **路径**: `GET /day1/hello/health`
- **功能**: 返回应用健康状态
- **用途**: 监控应用运行状态

### 5. 创建问候消息接口
- **路径**: `POST /day1/hello/create`
- **功能**: 创建新的问候消息
- **请求体**: `{"name": "张三", "message": "你好世界"}`
- **返回**: 创建的消息信息（包含ID、创建时间等）

### 6. 用户登录接口
- **路径**: `POST /day1/hello/login`
- **功能**: 用户登录验证
- **请求体**: `{"username": "admin", "password": "123456"}`
- **测试账号**: admin/123456
- **返回**: 登录结果和用户信息

### 7. 批量处理接口
- **路径**: `POST /day1/hello/batch`
- **功能**: 批量处理数据
- **请求体**: `{"operation": "process", "items": ["item1", "item2", "item3"]}`
- **返回**: 处理结果统计

## 运行方式

### 方式1: IDE运行
1. 在IDE中打开项目
2. 运行 `Day1Application.java` 的main方法

### 方式2: Maven命令运行
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run

# 打包应用
mvn clean package
```

### 方式3: 打包后运行
```bash
# 打包
mvn clean package

# 运行jar包
java -jar target/day1-hello-world-1.0.0.jar
```

## 访问地址

- **基础地址**: http://localhost:8080/day1
- **Hello接口**: http://localhost:8080/day1/hello
- **问候接口**: http://localhost:8080/day1/hello/greeting?name=张三
- **应用信息**: http://localhost:8080/day1/hello/info
- **健康检查**: http://localhost:8080/day1/hello/health

## 开发环境

- **端口**: 8081 (开发环境)
- **日志级别**: DEBUG
- **热重载**: 启用
- **跨域支持**: 启用

## 测试

### 运行测试
```bash
mvn test
```

### 测试内容
- Spring应用上下文加载测试
- 应用基本信息测试

## 接口测试工具

### 1. Java HTTP测试工具
- **文件**: `HttpTestTool.java`
- **运行**: `test-http.bat`
- **功能**: 交互式测试所有接口，支持POST请求测试
- **特点**: 纯Java实现，无需额外依赖

### 2. CURL测试脚本
- **文件**: `curl-test.bat`
- **运行**: `curl-test.bat`
- **功能**: 使用curl命令测试所有接口
- **要求**: 系统需要安装curl工具

### 3. 测试账号
- **用户名**: admin
- **密码**: 123456
- **用途**: 测试登录接口的成功案例

## 学习要点

### 1. Spring Boot注解
- `@SpringBootApplication`: 主启动类注解
- `@RestController`: REST控制器注解
- `@RequestMapping`: 请求映射注解
- `@GetMapping`: GET请求映射注解
- `@Value`: 配置属性注入注解
- `@Configuration`: 配置类注解
- `@Bean`: Bean定义注解

### 2. 配置文件
- YAML格式配置
- 环境配置分离
- 配置属性绑定

### 3. 依赖管理
- Spring Boot Starter机制
- 自动配置原理
- 版本统一管理

### 4. 项目结构
- 标准Maven项目结构
- Spring Boot推荐包结构
- 配置与代码分离

## 扩展功能

### 可选依赖（已注释）
- **Spring Boot Actuator**: 应用监控和管理
- **Spring Boot Validation**: 数据验证

### 自定义配置
- 应用属性配置类
- CORS跨域配置
- 日期时间格式化器

## 注意事项

1. **Java版本**: 确保使用Java 8或更高版本
2. **端口冲突**: 如果8080/8081端口被占用，请修改配置文件
3. **配置文件**: 开发环境使用dev profile，生产环境使用prod profile
4. **热重载**: 开发工具依赖仅在开发时生效

## 常见问题

### Q: 应用启动失败？
A: 检查Java版本、端口占用、配置文件语法等

### Q: 接口访问404？
A: 检查请求路径、控制器注解、包扫描配置等

### Q: 配置不生效？
A: 检查配置文件语法、属性绑定、Bean定义等

## 下一步学习

- Spring Boot数据访问（JPA/MyBatis）
- Spring Boot安全认证
- Spring Boot缓存机制
- Spring Boot消息队列
- Spring Boot微服务
