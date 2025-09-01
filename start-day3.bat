@echo off
echo ========================================
echo 启动 Day3 订单预警模块
echo ========================================
echo.

cd day3

echo 正在编译项目...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ 编译失败！
    pause
    exit /b 1
)

echo ✅ 编译成功！
echo.
echo 正在启动应用...
echo 访问地址: http://localhost:8082/day3
echo 测试页面: http://localhost:8082/day3/index.html
echo.

call mvn spring-boot:run

pause
