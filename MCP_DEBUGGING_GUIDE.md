# MCP 前后端调试指南

## 项目状态

✅ **后端服务** - Spring Boot 运行在 `http://localhost:8083`
✅ **前端服务** - Vite 开发服务器运行在 `http://localhost:5173`

---

## 快速访问

| 服务 | 地址 | 用途 |
|------|------|------|
| 前端应用 | http://localhost:5173 | 用户界面 |
| 后端 API | http://localhost:8083 | REST API 端点 |
| API 文档 | http://localhost:8083/doc.html | Knife4j API 文档 |
| WebSocket | ws://localhost:8083/ws | WebSocket 连接 |

---

## 使用 MCP 工具进行调试

### 1. 查看实时日志

**后端日志**：
```bash
tail -f /Users/hangerlin/AimLab/backend.log
```

**前端日志**：
```bash
tail -f /Users/hangerlin/AimLab/shooting-frontend/frontend.log
```

### 2. 测试 API 端点

使用 curl 测试后端 API：

```bash
# 获取所有运动员
curl -X GET http://localhost:8083/api/athletes

# 获取所有比赛
curl -X GET http://localhost:8083/api/competitions

# 获取训练会话
curl -X GET http://localhost:8083/api/training-sessions
```

### 3. WebSocket 调试

测试 WebSocket 连接：
```bash
# 使用 websocat（需要安装）
websocat ws://localhost:8083/ws

# 或使用 Node.js
node -e "
const WebSocket = require('ws');
const ws = new WebSocket('ws://localhost:8083/ws');
ws.on('open', () => console.log('Connected'));
ws.on('message', (msg) => console.log('Message:', msg));
"
```

### 4. 浏览器开发者工具调试

#### 前端调试步骤：
1. 打开 http://localhost:5173
2. 按 `F12` 打开开发者工具
3. 查看 **Console** 标签页的错误信息
4. 查看 **Network** 标签页的 API 请求
5. 查看 **Application** 标签页的 localStorage/sessionStorage

#### 后端调试步骤：
1. 访问 http://localhost:8083/doc.html 查看 API 文档
2. 在 Knife4j 界面中测试 API 端点
3. 查看后端日志了解请求处理情况

---

## 常见调试场景

### 场景 1：前端无法连接后端

**检查清单**：
- [ ] 后端服务是否运行：`curl http://localhost:8083/api/athletes`
- [ ] 前端是否有 CORS 错误（查看浏览器 Console）
- [ ] 后端配置是否允许跨域请求
- [ ] 网络连接是否正常

**解决方案**：
```bash
# 检查后端是否响应
curl -v http://localhost:8083/api/athletes

# 查看后端日志
tail -50 /Users/hangerlin/AimLab/backend.log
```

### 场景 2：WebSocket 连接失败

**检查清单**：
- [ ] WebSocket 端点是否正确：`ws://localhost:8083/ws`
- [ ] 后端 WebSocket 配置是否启用
- [ ] 浏览器是否支持 WebSocket
- [ ] 防火墙是否阻止连接

**调试代码**（在浏览器 Console 中运行）：
```javascript
const ws = new WebSocket('ws://localhost:8083/ws');
ws.onopen = () => console.log('WebSocket 已连接');
ws.onerror = (e) => console.error('WebSocket 错误:', e);
ws.onmessage = (e) => console.log('收到消息:', e.data);
```

### 场景 3：API 返回错误

**调试步骤**：
1. 打开浏览器开发者工具 → Network 标签
2. 执行触发错误的操作
3. 查看失败的请求
4. 检查响应状态码和错误信息
5. 查看后端日志了解详细错误

---

## 项目结构

```
/Users/hangerlin/AimLab/
├── src/                          # 后端源代码
│   ├── main/java/com/aimlab/    # Java 源文件
│   └── main/resources/           # 配置文件和 SQL 映射
├── shooting-frontend/            # 前端项目
│   ├── src/                      # Vue 3 源代码
│   ├── public/                   # 静态资源
│   └── package.json              # 前端依赖
├── pom.xml                       # 后端依赖配置
├── backend.log                   # 后端日志
└── shooting-frontend/frontend.log # 前端日志
```

---

## 后端主要模块

| 模块 | 路径 | 功能 |
|------|------|------|
| 用户管理 | `/api/users` | 用户认证和管理 |
| 运动员管理 | `/api/athletes` | 运动员信息管理 |
| 训练管理 | `/api/training-sessions` | 训练会话管理 |
| 比赛管理 | `/api/competitions` | 比赛信息管理 |
| 射击记录 | `/api/shooting-records` | 射击成绩记录 |
| WebSocket | `/ws` | 实时通信 |

---

## 前端主要页面

| 页面 | 路由 | 功能 |
|------|------|------|
| 登录 | `/login` | 用户认证 |
| 首页 | `/` | 仪表板 |
| 运动员管理 | `/athletes` | 管理运动员信息 |
| 训练管理 | `/training` | 管理训练会话 |
| 比赛管理 | `/competitions` | 管理比赛 |
| 成绩分析 | `/analytics` | 数据分析和报表 |

---

## 重启服务

### 重启后端
```bash
# 杀死现有进程
pkill -f "spring-boot:run"

# 重新启动
cd /Users/hangerlin/AimLab && mvn clean spring-boot:run -DskipTests 2>&1 | tee backend.log &
```

### 重启前端
```bash
# 杀死现有进程
pkill -f "vite"

# 重新启动
cd /Users/hangerlin/AimLab/shooting-frontend && npm run dev 2>&1 | tee frontend.log &
```

### 同时重启两个服务
```bash
# 杀死所有进程
pkill -f "spring-boot:run"
pkill -f "vite"

# 等待进程完全关闭
sleep 2

# 重新启动
cd /Users/hangerlin/AimLab && mvn clean spring-boot:run -DskipTests 2>&1 | tee backend.log &
cd /Users/hangerlin/AimLab/shooting-frontend && npm run dev 2>&1 | tee frontend.log &
```

---

## 常用 MCP 工具命令

### 查看文件内容
```bash
# 查看特定行范围
read_file /Users/hangerlin/AimLab/src/main/resources/application.yml

# 查看日志
read_file /Users/hangerlin/AimLab/backend.log
```

### 搜索代码
```bash
# 搜索特定关键字
grep_search "WebSocketConfig" /Users/hangerlin/AimLab/src

# 查找特定文件
find_by_name "*.java" /Users/hangerlin/AimLab/src
```

### 编辑文件
```bash
# 使用 edit 工具修改文件
# 使用 multi_edit 工具进行多个编辑
```

---

## 性能监控

### 后端性能
- 查看 Spring Boot 启动时间：查看 `backend.log` 中的 "Started in X seconds"
- 监控 WebSocket 连接：查看日志中的 "WebSocketSession" 统计

### 前端性能
- 打开浏览器开发者工具 → Performance 标签
- 记录性能指标
- 分析加载时间

---

## 故障排除

### 问题：后端无法启动
**解决方案**：
1. 检查 Java 版本：`java -version`（需要 Java 17+）
2. 检查 Maven：`mvn -version`
3. 清理构建：`mvn clean`
4. 查看详细错误：`tail -100 backend.log`

### 问题：前端无法启动
**解决方案**：
1. 检查 Node.js：`node -v`（需要 Node 14+）
2. 清理依赖：`rm -rf node_modules && npm install`
3. 查看详细错误：`tail -100 shooting-frontend/frontend.log`

### 问题：端口被占用
**解决方案**：
```bash
# 查找占用端口的进程
lsof -i :8083  # 后端
lsof -i :5173  # 前端

# 杀死进程
kill -9 <PID>
```

---

## 下一步

1. 打开浏览器预览查看前端应用
2. 在 Knife4j 文档中测试 API 端点
3. 使用浏览器开发者工具监控网络请求
4. 查看日志文件排查问题
5. 根据需要修改代码并观察热重载效果

