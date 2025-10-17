# 🧪 双用户 WebSocket 广播功能测试指南

## 📋 测试目标

验证当两个用户同时参加比赛时，每个用户的射击记录能否通过 WebSocket 实时广播给其他用户。

## 🔧 准备工作

### 后端已创建测试比赛

- **比赛ID**: `18`
- **比赛名称**: "双用户广播测试"
- **状态**: RUNNING（进行中）
- **已报名用户**: 用户A (athleteId=1)

### 环境要求

- 后端服务运行在：`http://localhost:8083`
- 前端服务运行在：`http://localhost:5173`
- 需要两个浏览器窗口（或使用无痕模式）

## 📝 详细测试步骤

### 步骤 1: 准备两个浏览器窗口

**窗口 A (用户 A):**
```
1. 打开 Chrome/Safari 正常窗口
2. 访问 http://localhost:5173
3. 按 F12 打开开发者工具
4. 切换到 Console 标签
```

**窗口 B (用户 B):**
```
1. 打开无痕/隐私窗口 (Cmd+Shift+N / Ctrl+Shift+N)
2. 访问 http://localhost:5173
3. 按 F12 打开开发者工具
4. 切换到 Console 标签
```

### 步骤 2: 用户 A 进入比赛

在窗口 A 中：

1. **刷新页面** (Cmd+Shift+R)
2. 找到比赛列表中的 "双用户广播测试"
3. 点击绿色 **"进入"** 按钮（应该已报名）
4. 进入比赛页面

**预期控制台输出：**
```
🔌 WebSocket 连接成功
📡 订阅比赛主题: /topic/competition/18
�� 订阅状态主题: /topic/competition/18/status
✅ 已连接并订阅比赛 18 的WebSocket主题
```

### 步骤 3: 用户 B 报名并进入比赛

在窗口 B 中：

1. **刷新页面** (Cmd+Shift+R)
2. 找到 "双用户广播测试"
3. 点击蓝色 **"参与"** 按钮报名
4. 报名成功后，点击绿色 **"进入"** 按钮
5. 进入比赛页面

**预期控制台输出：**
```
🔌 WebSocket 连接成功
📡 订阅比赛主题: /topic/competition/18
📡 订阅状态主题: /topic/competition/18/status
✅ 已连接并订阅比赛 18 的WebSocket主题
```

### 步骤 4: 用户 A 射击测试

在窗口 A 中：

1. 点击射击靶的任意位置
2. 观察控制台输出

**预期窗口 A 控制台输出：**
```
发送射击记录: {competitionId: 18, recordType: "COMPETITION", x: 0.45, y: 0.52, score: 8, ...}
✓ 通过WebSocket提交射击记录
📩 收到主题 /topic/competition/18 的消息: {type: "SHOOTING_RECORD", data: {...}}
🔔 收到WebSocket消息: {type: "SHOOTING_RECORD", data: {...}}
📊 收到射击记录广播: {id: 123, athleteId: 1, score: 8, ...}
✓ 替换临时记录为服务器确认记录
📩 收到主题 /topic/competition/18 的消息: {type: "RANKING_UPDATE", data: [...]}
📈 收到排名更新: [...]
```

**🌟 关键：同时观察窗口 B 控制台输出：**
```
📩 收到主题 /topic/competition/18 的消息: {type: "SHOOTING_RECORD", data: {...}}
🔔 收到WebSocket消息: {type: "SHOOTING_RECORD", data: {...}}
📊 收到射击记录广播: {id: 123, athleteId: 1, score: 8, athleteName: "立体几何"}
✓ 添加新射击记录: 立体几何  ⭐⭐⭐ 关键！
📩 收到主题 /topic/competition/18 的消息: {type: "RANKING_UPDATE", data: [...]}
📈 收到排名更新: [...]
```

**预期界面显示：**
- 窗口 A: 看到自己的射击点和得分
- 窗口 B: **也能看到用户 A 的射击点和得分** ⭐

### 步骤 5: 用户 B 射击测试

在窗口 B 中：

1. 点击射击靶的任意位置
2. 观察两个窗口的控制台输出

**预期窗口 B 控制台输出：**
```
发送射击记录: {competitionId: 18, ...}
✓ 通过WebSocket提交射击记录
📩 收到主题 /topic/competition/18 的消息: {type: "SHOOTING_RECORD", data: {...}}
✓ 替换临时记录为服务器确认记录
```

**🌟 关键：同时观察窗口 A 控制台输出：**
```
📩 收到主题 /topic/competition/18 的消息: {type: "SHOOTING_RECORD", data: {...}}
🔔 收到WebSocket消息: {type: "SHOOTING_RECORD", data: {...}}
📊 收到射击记录广播: {id: 124, athleteId: 2, score: 9, athleteName: "..."}
✓ 添加新射击记录: [用户B的名字]  ⭐⭐⭐ 关键！
```

**预期界面显示：**
- 窗口 A: **能看到用户 B 的射击点** ⭐
- 窗口 B: 看到自己的射击点
- 两个窗口的排名表都显示两个用户

### 步骤 6: 验证排名表

在两个窗口中：

**预期排名表显示：**
```
排名 | 选手         | 总环数 | 平均分 | 射击数
-----|-------------|--------|--------|-------
  1  | 立体几何    |   8    |  8.00  |   1
  2  | [用户B]     |   9    |  9.00  |   1
```

✅ **成功标志**：
- 两个窗口都能看到完整排名
- 排名实时更新
- 无重复记录

## 🔍 故障排查

### 问题 1: 用户 B 看不到用户 A 的射击

**检查步骤：**

1. **检查窗口 B 控制台**
   - ✅ 有 `📩 收到主题的消息` → WebSocket 正常
   - ❌ 没有 → 检查下一步

2. **检查 WebSocket 订阅**
   - 两个窗口都应该显示：`📡 订阅比赛主题: /topic/competition/18`
   - 比赛ID 必须一致

3. **检查后端日志**
   ```bash
   tail -f /Users/hangerlin/AimLab/app.log | grep "广播"
   ```
   
   应该看到：
   ```
   ✓ 广播射击记录 - 比赛ID: 18, 运动员ID: 1, 得分: 8
   ```

4. **检查浏览器 Network 标签**
   - 切换到 WS (WebSocket) 标签
   - 应该看到活跃的 WebSocket 连接
   - 查看 Messages 子标签，应该能看到消息流

### 问题 2: 看到重复记录

**检查步骤：**

1. 查看控制台是否显示：
   - ✅ `✓ 替换临时记录为服务器确认记录` → 正常
   - ❌ `✓ 添加新射击记录: 立体几何` (自己的名字) → 去重失败

2. 检查射击记录的字段：
   ```javascript
   // 控制台输入
   console.log(competitionStore.records)
   ```
   
   检查是否有 `isPending: true` 的记录

### 问题 3: WebSocket 连接失败

**解决方法：**

1. 确认后端服务运行：
   ```bash
   lsof -ti:8083
   ```

2. 确认 WebSocket 端点：
   ```bash
   curl -I http://localhost:8083/ws
   ```

3. 检查 Vite 代理配置：
   ```javascript
   // vite.config.js
   proxy: {
     '/ws': {
       target: 'http://localhost:8083',
       ws: true
     }
   }
   ```

## 📊 成功标准

- ✅ 两个用户都能连接 WebSocket
- ✅ 用户 A 射击后，用户 B **立即**看到
- ✅ 用户 B 射击后，用户 A **立即**看到
- ✅ 排名表实时更新
- ✅ 无重复记录
- ✅ 控制台无错误

## 🎯 测试结果记录

### 用户 A 射击
- [ ] 窗口 A 控制台有 `✓ 通过WebSocket提交射击记录`
- [ ] 窗口 B 控制台有 `📊 收到射击记录广播`
- [ ] 窗口 B 控制台有 `✓ 添加新射击记录: 立体几何`
- [ ] 窗口 B 界面显示用户 A 的射击点

### 用户 B 射击
- [ ] 窗口 B 控制台有 `✓ 通过WebSocket提交射击记录`
- [ ] 窗口 A 控制台有 `📊 收到射击记录广播`
- [ ] 窗口 A 控制台有 `✓ 添加新射击记录: [用户B名字]`
- [ ] 窗口 A 界面显示用户 B 的射击点

### 排名表
- [ ] 两个窗口都显示完整排名
- [ ] 排名数据一致
- [ ] 无重复记录

---

**测试完成后，请告诉我：**
1. 哪些步骤成功了？
2. 哪些步骤失败了？
3. 控制台的完整日志（截图或复制文本）
4. 是否看到 `📊 收到射击记录广播` 和 `✓ 添加新射击记录`？

