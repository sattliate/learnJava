@echo off
echo ========================================
echo 测试修改后的API接口
echo ========================================
echo.

echo 1. 测试创建订单接口...
curl -X POST http://localhost:8082/api/orders ^
  -H "Content-Type: application/json" ^
  -d "{\"memberPhone\":\"13800138000\",\"memberName\":\"张三\",\"province\":\"北京\",\"channel\":\"14\",\"registerId\":\"12345\",\"orderAmount\":\"1000.00\",\"orderStatus\":\"CREATED\"}"

echo.
echo.
echo 2. 测试查询会员订单接口...
curl -X POST http://localhost:8082/api/orders/member ^
  -H "Content-Type: application/json" ^
  -d "{\"memberPhone\":\"13800138000\"}"

echo.
echo.
echo 3. 测试搜索订单接口...
curl -X POST http://localhost:8082/api/orders/search ^
  -H "Content-Type: application/json" ^
  -d "{\"province\":\"北京\",\"channel\":\"14\"}"

echo.
echo.
echo 4. 测试订单速度查询接口...
curl -X POST http://localhost:8082/api/orders/speed ^
  -H "Content-Type: application/json" ^
  -d "{\"memberPhone\":\"13800138000\",\"channel\":\"14\",\"registerId\":\"12345\",\"timeWindow\":12}"

echo.
echo.
echo 5. 测试多时间窗口速度查询接口...
curl -X POST http://localhost:8082/api/orders/speed/multi ^
  -H "Content-Type: application/json" ^
  -d "{\"memberPhone\":\"13800138000\",\"channel\":\"14\",\"registerId\":\"12345\",\"timeWindows\":\"1,6,12,24\"}"

echo.
echo.
echo 6. 测试系统状态接口...
curl -X POST http://localhost:8082/api/orders/status ^
  -H "Content-Type: application/json" ^
  -d "{\"includeDetails\":true,\"includeMetrics\":false,\"includeCache\":true}"

echo.
echo.
echo ✅ API测试完成！
echo.
pause

