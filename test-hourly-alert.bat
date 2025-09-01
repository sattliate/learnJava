@echo off
echo ========================================
echo 测试每小时预警功能
echo ========================================
echo.

cd day3

echo 正在启动Day3项目...
start "Day3 Order Alert System" cmd /k "mvn spring-boot:run"

echo.
echo 等待项目启动...
timeout /t 15 /nobreak >nul

echo.
echo 🚀 项目已启动！现在可以测试每小时预警功能：
echo.
echo 📋 测试步骤：
echo 1. 访问测试页面：http://localhost:8082/day3/index.html
echo 2. 在"预警免打扰状态"部分输入预警维度key：
echo    例如：13800138000-44000000-14-12345
echo 3. 点击"查询免打扰状态"查看当前状态
echo 4. 批量创建10个订单触发第一次预警
echo 5. 等待10分钟后，再次触发预警（第二次预警）
echo 6. 等待30分钟后，再次触发预警（第三次预警）
echo 7. 等待60分钟后，再次触发预警（第四次预警）
echo 8. 之后：每小时自动发送一次预警
echo.
echo 🔧 新的预警机制说明：
echo - 第一次预警：立即发送
echo - 第二次预警：10分钟后发送
echo - 第三次预警：30分钟后发送
echo - 第四次预警：60分钟后发送
echo - 之后：每小时发送一次预警（持续监控）
echo.
echo 💡 关键特性：
echo - 超过3次预警后，系统不会停止，而是转为每小时预警模式
echo - 这样可以持续监控异常情况，避免遗漏重要预警
echo - 预警频率从密集变为规律，平衡了及时性和干扰性
echo - 使用Redis过期key实现免打扰机制，支持持久化和分布式
echo.
echo 🧪 测试建议：
echo - 可以使用"重置免打扰记录"按钮重置特定维度的记录
echo - 观察日志中的"超过最大预警次数，设置每小时预警"信息
echo - 验证每小时预警的时间间隔是否准确
echo.
pause
