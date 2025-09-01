@echo off
echo ========================================
echo 测试预警免打扰机制编译
echo ========================================
echo.

cd day3

echo 正在清理项目...
call mvn clean -q

echo.
echo 正在编译项目...
call mvn compile -q

if %errorlevel% equ 0 (
    echo.
    echo ✅ 编译成功！免打扰机制代码正常
    echo.
    echo 🎯 免打扰机制特性：
echo - 第一次预警：立即发送
echo - 第二次预警：10分钟后发送
echo - 第三次预警：30分钟后发送
echo - 第四次预警：60分钟后发送
echo - 之后：每小时发送一次预警，直到重置记录
    echo.
    echo 🚀 现在可以运行项目测试免打扰机制：
    echo 1. 运行 test-quiet-mode.bat 启动项目
    echo 2. 访问 http://localhost:8082/day3/index.html
    echo 3. 测试免打扰功能
else
    echo.
    echo ❌ 编译失败！请检查错误信息
    echo.
    echo 可能的问题：
    echo 1. Java版本不兼容
    echo 2. 依赖缺失
    echo 3. 代码语法错误
fi

echo.
pause
