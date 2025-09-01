# 🔥 IntelliJ IDEA 热更新设置指南

## 🎯 目标
在 IDEA 中启用 Spring Boot DevTools 热更新功能，实现代码修改后自动重启应用。

## ⚙️ 必要设置

### **设置1：启用自动编译**

1. **打开设置**：
   - `File` → `Settings` (Windows)
   - `IntelliJ IDEA` → `Preferences` (Mac)
   - 快捷键：`Ctrl+Alt+S`

2. **导航到编译器设置**：
   - `Build, Execution, Deployment` → `Compiler`

3. **勾选以下选项**：
   ```
   ☑️ Build project automatically
   ☑️ Compile independent modules in parallel
   ```

### **设置2：启用运行时编译**

1. **在设置中搜索**：`Advanced Settings`
2. **找到并勾选**：
   ```
   ☑️ Allow auto-make to start even if developed application is currently running
   ☑️ Compile independent modules in parallel
   ```

### **设置3：Maven 设置**

1. **导航到**：`Build, Execution, Deployment` → `Build Tools` → `Maven`
2. **确保以下设置**：
   ```
   ☑️ Use Maven wrapper (如果使用 Maven Wrapper)
   ☑️ Import Maven projects automatically
   ☑️ Sources and documentation
   ```

### **设置4：项目 SDK 设置**

1. **右键点击项目** → `Open Module Settings`
2. **在 `Project` 标签页**：
   ```
   Project SDK: 1.8 (BellSoft LibericaJDK-8)
   Project language level: 8
   ```

## 🚀 启动应用

### **方法1：使用 Maven 工具窗口**

1. **打开 Maven 工具窗口**：
   - `View` → `Tool Windows` → `Maven`
   - 或者按 `Alt+4`

2. **展开项目**：
   ```
   learn-java
   └── day1
       └── Lifecycle
           ├── clean
           ├── compile
           └── spring-boot:run
   ```

3. **双击 `spring-boot:run`** 启动应用

### **方法2：使用运行配置**

1. **创建运行配置**：
   - `Run` → `Edit Configurations...`
   - 点击 `+` 号 → `Maven`

2. **配置参数**：
   ```
   Name: Day1 DevTools
   Working directory: $MODULE_DIR$/day1
   Command line: spring-boot:run -Dspring.profiles.active=devtools
   ```

3. **点击运行按钮** ▶️

### **方法3：使用终端**

1. **打开 IDEA 内置终端**：
   - `View` → `Tool Windows` → `Terminal`
   - 或者按 `Alt+F12`

2. **运行命令**：
   ```bash
   cd day1
   mvn spring-boot:run -Dspring.profiles.active=devtools
   ```

## 🧪 测试热更新

### **测试步骤**

1. **启动应用**，等待看到：
   ```
   The following profiles are active: devtools
   Started Day1Application in X.XXX seconds
   ```

2. **修改代码**：
   - 打开 `DevToolsDemoController.java`
   - 修改 `welcomeMessage` 变量值
   - 保存文件 (`Ctrl+S`)

3. **观察控制台输出**，应该看到：
   ```
   [INFO] Restarting application...
   [INFO] Restarted application in X.XXX seconds
   ```

4. **验证修改**：
   - 访问：`http://localhost:8081/day1/devtools/welcome`
   - 应该看到修改后的内容

## 🔍 常见问题排查

### **问题1：修改代码后没有自动重启**

**解决方案**：
1. 检查是否启用了 `Build project automatically`
2. 检查 DevTools 依赖是否正确添加
3. 检查 `application-devtools.yml` 配置

### **问题2：编译失败**

**解决方案**：
1. 检查 Java SDK 版本设置
2. 运行 `mvn clean compile` 查看详细错误
3. 检查 Maven 配置

### **问题3：端口被占用**

**解决方案**：
1. 检查端口 8081 是否被占用
2. 修改 `application-devtools.yml` 中的端口配置
3. 或者停止其他占用端口的应用

## 📋 完整设置检查清单

- [ ] 启用自动编译 (`Build project automatically`)
- [ ] 启用并行编译 (`Compile independent modules in parallel`)
- [ ] 启用运行时编译 (`Allow auto-make to start even if developed application is currently running`)
- [ ] 配置正确的 Java SDK (Java 8)
- [ ] 确保 DevTools 依赖已添加
- [ ] 确保 `application-devtools.yml` 配置正确
- [ ] 使用正确的 Profile 启动应用 (`devtools`)

## 🎉 成功标志

热更新正常工作后，你应该看到：

1. ✅ 修改代码后保存文件
2. ✅ 控制台显示重启信息：`Restarting application...`
3. ✅ 应用在 1-2 秒内重启完成
4. ✅ 修改的代码立即生效
5. ✅ 无需手动重启应用

现在按照这个指南设置 IDEA，就可以享受热更新的便利了！🚀
