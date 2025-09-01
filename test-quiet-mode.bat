@echo off
echo ========================================
echo 测试预警免打扰机制
echo ========================================
echo.

cd day3

echo 正在启动Day3项目...
start "Day3 Order Alert System" cmd /k "mvn spring-boot:run"

echo.
echo 等待项目启动...
timeout /t 15 /nobreak >nul

echo.
echo 🚀 项目已启动！现在可以测试免打扰机制：
echo.
echo 📋 测试步骤：
echo 1. 访问测试页面：http://localhost:8082/day3/index.html
echo 2. 在"预警免打扰状态"部分输入预警维度key：
echo    例如：13800138000-44000000-14-12345
echo 3. 点击"查询免打扰状态"查看当前状态
echo 4. 批量创建10个订单触发预警
echo 5. 再次查询免打扰状态，观察预警次数和下次预警时间
echo 6. 等待10分钟后，再次触发预警（应该会发送第二次预警）
echo 7. 等待30分钟后，再次触发预警（应该会发送第三次预警）
echo 8. 等待60分钟后，再次触发预警（应该不会发送，已达到最大次数）
echo.
echo 🔧 免打扰机制说明：
echo - 第一次预警：立即发送
echo - 第二次预警：10分钟后发送
echo - 第三次预警：30分钟后发送
echo - 第四次预警：60分钟后发送
echo - 之后：每小时发送一次预警，直到重置记录
echo.
echo 💡 提示：
echo - 可以使用"重置免打扰记录"按钮重置特定维度的记录
echo - 可以使用"清空所有免打扰记录"按钮清空所有记录
echo - 预警维度key格式：会员手机号-省份-渠道-registerId
echo.
pause
