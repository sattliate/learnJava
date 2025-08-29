@echo off
echo 正在编译Java文件...

REM 创建输出目录
if not exist "build\classes" mkdir "build\classes"

REM 编译主代码
javac -encoding UTF-8 -d build/classes -cp . src/main/java/com/picc/java/learn/ratelimit/*.java

REM 编译测试代码
javac -encoding UTF-8 -d build/classes -cp . src/test/java/com/picc/java/learn/ratelimit/*.java

if %ERRORLEVEL% EQU 0 (
    echo 编译成功！
    echo 编译输出目录: build/classes
) else (
    echo 编译失败！
)

pause
