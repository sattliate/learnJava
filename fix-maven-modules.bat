@echo off
echo ========================================
echo 修复Maven多模块项目结构
echo ========================================
echo.

echo 正在清理Maven项目...
cd /d "%~dp0"

echo.
echo 1. 清理所有模块的target目录...
if exist "day1\target" rmdir /s /q "day1\target"
if exist "day2\target" rmdir /s /q "day2\target"
if exist "day3\target" rmdir /s /q "day3\target"

echo.
echo 2. 清理根目录target...
if exist "target" rmdir /s /q "target"

echo.
echo 3. 清理Maven本地仓库缓存...
call mvn dependency:purge-local-repository -q

echo.
echo 4. 重新编译根项目...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ 根项目编译失败！
    pause
    exit /b 1
)

echo.
echo 5. 重新编译所有子模块...
call mvn clean compile -q -pl day1,day2,day3
if %errorlevel% neq 0 (
    echo ❌ 子模块编译失败！
    pause
    exit /b 1
)

echo.
echo ✅ Maven多模块项目结构修复完成！
echo.
echo 请在IDEA中执行以下操作：
echo 1. 右键点击项目根目录
echo 2. 选择 "Maven" -> "Reload Project"
echo 3. 或者选择 "Maven" -> "Reimport"
echo.
echo 如果问题仍然存在，请尝试：
echo 1. 关闭IDEA
echo 2. 删除 .idea 目录和所有 .iml 文件
echo 3. 重新打开项目
echo.

pause
