@echo off
echo 运行限流模块测试...

REM 检查是否已编译
if not exist "build\classes\com\picc\java\learn\ratelimit\RateLimitTest.class" (
    echo 错误：未找到编译后的文件，请先运行 compile.bat
    pause
    exit /b 1
)

REM 运行测试
java -cp build/classes com.picc.java.learn.ratelimit.RateLimitTest

pause
