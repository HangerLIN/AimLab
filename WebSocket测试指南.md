# WebSocket 比赛射击系统测试指南

## 📋 测试前准备

### 当前系统状态
- ✅ 后端服务运行：`http://localhost:8083`
- ✅ 前端服务运行：`http://localhost:5173`
- ✅ 数据库有6场比赛（都是CREATED状态）
- ✅ 已有用户和运动员档案

## 🚀 完整测试流程

### 步骤1：登录系统

1. 打开浏览器访问：`http://localhost:5173`
2. 使用以下任一账户登录：
   - **管理员账户**（用于开始比赛）：
     - 用户名：`admin`
     - 密码：（你设置的密码）
   
   - **运动员账户**（用于参与射击）：
     - 用户名：`test`（对应athlete_id=1）
     - 密码：（你设置的密码）

### 步骤2：进入比赛页面

1. 在首页点击"比赛列表"
2. 选择"WebSocket实时射击测试"（ID=6）
3. 点击"参与"进入比赛页面

### 步骤3：开始比赛（需要管理员）

⚠️ **重要**：比赛必须先开始，才能进行射击！

**方法A：使用前端界面（推荐）**
1. 使用管理员账户（admin）登录
2. 进入比赛页面
3. 点击"开始比赛"按钮

**方法B：使用API直接开始**
```bash
# 先登录获取token
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的密码"}'

# 使用返回的token开始比赛
curl -X POST http://localhost:8083/api/competitions/6/start \
  -H "aimlab-token: 你的token"
```

**方法C：直接修改数据库**
```sql
USE shooting_system_0;
UPDATE competitions SET status='RUNNING', started_at=NOW() WHERE id=6;
```

### 步骤4：建立WebSocket连接

1. 确保已登录（localStorage中有token）
2. 在比赛页面查看连接状态
3. 如果显示"未连接"，点击"点击建立连接"按钮

**调试连接状态**：
- 按键盘 `D` 键显示/隐藏调试信息
- 查看连接状态、比赛状态、用户ID等信息

### 步骤5：进行射击测试

1. 确认WebSocket状态为"✓ 实时连接已建立"
2. 确认比赛状态为"进行中"
3. 点击靶子进行射击
4. 观察：
   - 射击点是否显示在靶子上
   - 分数是否正确计算
   - 排名是否实时更新

## 🔧 问题排查

### Q1: 点击靶子后没有反应？

**检查清单**：
- [ ] 比赛状态是否为"RUNNING"？
  ```sql
  SELECT id, name, status FROM competitions WHERE id=6;
  ```
- [ ] WebSocket是否连接？查看页面连接状态
- [ ] 用户是否有运动员档案？
  ```sql
  SELECT a.* FROM athletes a 
  JOIN users u ON a.user_id = u.id 
  WHERE u.username = '你的用户名';
  ```
- [ ] 浏览器控制台是否有错误？按F12查看

### Q2: WebSocket连接失败？

**检查清单**：
- [ ] 是否已登录？检查localStorage中的`aimlab-token`
- [ ] 后端服务是否运行？访问 `http://localhost:8083`
- [ ] 查看后端日志：
  ```bash
  tail -f /Users/hangerlin/AimLab/app.log
  ```

### Q3: 提示"您还没有创建运动员档案"？

**解决方法**：
1. 前往"运动员档案"页面
2. 创建运动员档案
3. 返回比赛页面重试

或使用API创建：
```bash
curl -X POST http://localhost:8083/api/athletes/profile \
  -H "Content-Type: application/json" \
  -H "aimlab-token: 你的token" \
  -d '{
    "name": "测试运动员",
    "gender": "MALE",
    "level": "初级"
  }'
```

## 📊 监控实时数据

### 查看射击记录
```sql
USE shooting_system_0;
SELECT * FROM shooting_records 
WHERE competition_id = 6 
ORDER BY shot_at DESC 
LIMIT 10;
```

### 查看实时排名
```bash
curl http://localhost:8083/api/competitions/6/rankings
```

### 查看WebSocket连接日志
```bash
# 实时查看后端日志
tail -f /Users/hangerlin/AimLab/app.log | grep WebSocket
```

## 🎯 完整测试脚本

```bash
#!/bin/bash

# 1. 开始比赛（使用数据库）
mysql -u root -e "USE shooting_system_0; UPDATE competitions SET status='RUNNING', started_at=NOW() WHERE id=6;"

echo "✅ 比赛已开始"
echo ""
echo "请执行以下操作："
echo "1. 访问 http://localhost:5173"
echo "2. 使用用户名 'test' 登录"
echo "3. 进入比赛ID=6"
echo "4. 点击'点击建立连接'"
echo "5. 点击靶子进行射击"
echo ""
echo "按D键查看调试信息"
```

## 🌟 成功标志

当看到以下现象时，说明WebSocket连接成功：
- ✅ 页面显示"✓ 实时连接已建立"
- ✅ 点击靶子后出现射击点
- ✅ 分数实时更新
- ✅ 排名实时变化
- ✅ 其他用户的射击也能实时看到

## 📝 技术说明

### WebSocket消息格式

**客户端发送射击**：
```json
{
  "competitionId": 6,
  "x": 0.5,
  "y": 0.5,
  "score": 10.5,
  "roundNumber": 1
}
```

**服务器广播射击记录**：
```json
{
  "type": "SHOOTING_RECORD",
  "data": {
    "id": 123,
    "athleteId": 1,
    "competitionId": 6,
    "x": 0.5,
    "y": 0.5,
    "score": 10.5,
    "shotAt": "2025-09-30T14:30:00"
  }
}
```

**服务器广播排名更新**：
```json
{
  "type": "RANKING_UPDATE",
  "data": [
    {
      "athleteId": 1,
      "name": "测试运动员",
      "totalScore": 95.5,
      "shotCount": 10
    }
  ]
}
```

## 🔗 相关文件

- 前端WebSocket配置：`shooting-frontend/src/websocket.js`
- 前端Store：`shooting-frontend/src/store/modules/competition.js`
- 比赛页面：`shooting-frontend/src/views/Competition.vue`
- 后端WebSocket配置：`src/main/java/com/aimlab/config/WebSocketConfig.java`
- 后端消息处理：`src/main/java/com/aimlab/controller/WebSocketController.java` 