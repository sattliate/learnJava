# Redis Sorted Set 使用指南

## 概述

Redis Sorted Set（有序集合）是Redis中一个非常重要的数据结构，它结合了Set和Hash的特性：
- 每个元素都是唯一的（Set特性）
- 每个元素都关联一个分数（score），用于排序（Hash特性）
- 元素按照分数从小到大排序

## 基本概念

### 数据结构
```
key: "leaderboard"
value: [
    {"member": "user1", "score": 100},
    {"member": "user2", "score": 200},
    {"member": "user3", "score": 150}
]
```

### 特点
- **唯一性**：成员（member）不能重复
- **有序性**：按分数（score）排序
- **分数可重复**：不同成员可以有相同分数
- **支持范围查询**：可以按分数范围或排名范围查询

## 常用命令

### 1. 添加元素

```bash
# 添加单个元素
ZADD leaderboard 100 "user1"

# 添加多个元素
ZADD leaderboard 200 "user2" 150 "user3" 300 "user4"

# 添加元素并返回添加的数量
ZADD leaderboard NX 100 "user1"  # NX: 只添加新元素
ZADD leaderboard XX 100 "user1"  # XX: 只更新已存在的元素
ZADD leaderboard CH 100 "user1"  # CH: 返回修改的元素数量
```

### 2. 查询元素

```bash
# 获取元素分数
ZSCORE leaderboard "user1"

# 获取元素排名（从0开始，升序）
ZRANK leaderboard "user1"

# 获取元素排名（从0开始，降序）
ZREVRANK leaderboard "user1"

# 获取元素数量
ZCARD leaderboard

# 获取指定分数范围的元素数量
ZCOUNT leaderboard 100 200
```

### 3. 范围查询

```bash
# 按分数范围查询（升序）
ZRANGEBYSCORE leaderboard 100 200

# 按分数范围查询（降序）
ZREVRANGEBYSCORE leaderboard 200 100

# 按排名范围查询（升序）
ZRANGE leaderboard 0 2

# 按排名范围查询（降序）
ZREVRANGE leaderboard 0 2

# 带分数显示
ZRANGE leaderboard 0 2 WITHSCORES
```

### 4. 删除元素

```bash
# 删除指定元素
ZREM leaderboard "user1"

# 按排名范围删除
ZREMRANGEBYRANK leaderboard 0 2

# 按分数范围删除
ZREMRANGEBYSCORE leaderboard 100 200
```

### 5. 分数操作

```bash
# 增加分数
ZINCRBY leaderboard 50 "user1"

# 减少分数
ZINCRBY leaderboard -50 "user1"
```

## 实际应用场景

### 1. 排行榜系统

```bash
# 用户得分排行榜
ZADD game_leaderboard 1000 "player1"
ZADD game_leaderboard 1500 "player2"
ZADD game_leaderboard 800 "player3"

# 获取前3名
ZREVRANGE game_leaderboard 0 2 WITHSCORES

# 获取第1名的分数
ZSCORE game_leaderboard "player2"
```

### 2. 时间排序

```bash
# 用户登录时间排序
ZADD user_login_time 1640995200 "user1"  # 2022-01-01 00:00:00
ZADD user_login_time 1641081600 "user2"  # 2022-01-02 00:00:00
ZADD user_login_time 1641168000 "user3"  # 2022-01-03 00:00:00

# 获取最近登录的用户
ZREVRANGE user_login_time 0 2
```

### 3. 权重计算

```bash
# 文章热度排序（阅读量 + 点赞数 * 10 + 评论数 * 5）
ZADD article_hot 100 "article1"  # 100阅读量
ZINCRBY article_hot 50 "article1"  # +5点赞
ZINCRBY article_hot 15 "article1"  # +3评论

# 获取最热门的文章
ZREVRANGE article_hot 0 9
```

### 4. 延迟队列

```bash
# 任务延迟队列
ZADD task_queue 1640995200 "task1"  # 2022-01-01执行
ZADD task_queue 1641081600 "task2"  # 2022-01-02执行

# 获取到期的任务
ZRANGEBYSCORE task_queue 0 1640995200
```

## 高级操作

### 1. 集合运算

```bash
# 交集
ZINTERSTORE result 2 set1 set2

# 并集
ZUNIONSTORE result 2 set1 set2

# 差集
ZDIFFSTORE result 2 set1 set2
```

### 2. 聚合函数

```bash
# 使用SUM聚合（默认）
ZUNIONSTORE result 2 set1 set2 AGGREGATE SUM

# 使用MIN聚合
ZUNIONSTORE result 2 set1 set2 AGGREGATE MIN

# 使用MAX聚合
ZUNIONSTORE result 2 set1 set2 AGGREGATE MAX
```

### 3. 权重设置

```bash
# 设置权重
ZUNIONSTORE result 2 set1 set2 WEIGHTS 2 1
```

## 性能优化建议

### 1. 内存优化

```bash
# 使用压缩列表（小集合）
# Redis会自动选择合适的数据结构

# 定期清理过期数据
ZREMRANGEBYSCORE leaderboard 0 100
```

### 2. 查询优化

```bash
# 避免大范围查询
# 不推荐：ZRANGE leaderboard 0 10000
# 推荐：ZRANGE leaderboard 0 99

# 使用LIMIT限制结果数量
ZRANGEBYSCORE leaderboard 100 200 LIMIT 0 10
```

### 3. 批量操作

```bash
# 批量添加（减少网络往返）
ZADD leaderboard 100 "user1" 200 "user2" 300 "user3"

# 批量删除
ZREM leaderboard "user1" "user2" "user3"
```

## 注意事项

### 1. 分数精度

```bash
# 分数是64位浮点数
ZADD leaderboard 3.14159 "pi"
ZADD leaderboard 2.71828 "e"

# 注意浮点数精度问题
ZSCORE leaderboard "pi"  # 可能返回3.1415899999999999
```

### 2. 内存占用

```bash
# 每个元素占用内存
# member: 字符串长度 + 8字节
# score: 8字节
# 额外开销: 约16字节

# 估算内存使用
# 1000个元素，平均member长度10字节
# 总内存 ≈ 1000 * (10 + 8 + 8 + 16) = 42KB
```

### 3. 过期策略

```bash
# 设置过期时间
EXPIRE leaderboard 3600  # 1小时后过期

# 定期清理过期数据
ZREMRANGEBYSCORE leaderboard 0 1640995200
```

## 最佳实践

### 1. 命名规范

```bash
# 使用冒号分隔的命名空间
ZADD user:score:game1 100 "user1"
ZADD user:score:game2 200 "user1"

# 使用有意义的键名
ZADD daily:active:users:20220101 1640995200 "user1"
```

### 2. 错误处理

```bash
# 检查元素是否存在
EXISTS leaderboard

# 检查元素是否在集合中
ZSCORE leaderboard "user1"  # 返回nil表示不存在

# 使用NX避免覆盖
ZADD leaderboard NX 100 "user1"
```

### 3. 监控和维护

```bash
# 监控集合大小
ZCARD leaderboard

# 监控内存使用
MEMORY USAGE leaderboard

# 定期清理过期数据
ZREMRANGEBYSCORE leaderboard 0 $(date +%s -d '7 days ago')
```

## 总结

Redis Sorted Set是一个功能强大的数据结构，特别适合：
- 排行榜系统
- 时间排序
- 权重计算
- 延迟队列
- 范围查询

通过合理使用各种命令和优化策略，可以构建高性能的应用系统。
