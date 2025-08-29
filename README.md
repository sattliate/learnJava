# Learn Java 项目

这是一个Java学习项目，采用Maven多模块结构，包含多个学习模块。

## 项目结构

```
learn-java/                    # 父项目根目录
├── pom.xml                    # 父项目POM文件
├── day1/                      # Day1子项目：Hello World
│   ├── pom.xml               # Day1项目POM文件
│   ├── src/                  # 源代码目录
│   │   ├── main/
│   │   │   ├── java/        # Java源代码
│   │   │   └── resources/   # 配置文件
│   │   └── test/            # 测试代码
│   └── target/              # 编译输出目录
├── day2/                      # Day2子项目：Rate Limit
│   ├── pom.xml               # Day2项目POM文件
│   ├── src/                  # 源代码目录
│   │   ├── main/
│   │   │   ├── java/        # Java源代码
│   │   │   └── resources/   # 配置文件
│   │   └── test/            # 测试代码
│   └── target/              # 编译输出目录
└── README.md                 # 项目说明文档
```

## 子项目说明

### Day1 - Hello World
- **项目名称**: `day1-hello-world`
- **端口**: 8080
- **功能**: Spring Boot基础学习，包含Hello World示例
- **主要特性**: 
  - Spring Boot Web Starter
  - 基础配置学习
  - 控制器示例

### Day2 - Rate Limit
- **项目名称**: `day2-rate-limit`
- **端口**: 8081
- **功能**: 限流器实现学习
- **主要特性**:
  - 本地限流实现
  - 分布式限流（Redis）
  - AOP切面编程
  - 多种限流策略

## 技术栈

- **Java**: 1.8
- **Maven**: 3.x
- **Spring Boot**: 2.7.18
- **Redis**: 用于分布式限流
- **AOP**: 面向切面编程

## 快速开始

### 1. 编译整个项目
```bash
mvn clean compile
```

### 2. 运行Day1项目
```bash
cd day1
mvn spring-boot:run
```

### 3. 运行Day2项目
```bash
cd day2
mvn spring-boot:run
```

### 4. 访问应用
- Day1: http://localhost:8080
- Day2: http://localhost:8081

## 开发说明

### 父项目配置
- 统一管理依赖版本
- 统一管理插件版本
- 定义公共属性

### 子项目配置
- 继承父项目配置
- 可以覆盖父项目配置
- 独立的依赖管理

## 注意事项

1. 确保Java 1.8环境已正确配置
2. 如果使用Day2的分布式限流功能，需要启动Redis服务
3. 每个子项目可以独立开发和部署
4. 父项目主要用于统一管理和配置

## 学习路径

1. **Day1**: 学习Spring Boot基础配置和使用
2. **Day2**: 学习限流算法和分布式限流实现
3. **后续**: 可以继续添加更多学习模块

## 贡献

欢迎提交Issue和Pull Request来改进这个学习项目！ 
