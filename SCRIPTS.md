# 射击训练平台 - 服务器管理脚本

## 📋 脚本列表

### 1. `restart-servers.sh` - 重启服务器
一键重启前端和后端服务器的便捷脚本。

**功能：**
- 自动停止正在运行的前端和后端进程
- 重新启动后端服务器（端口 8083）
- 重新启动前端服务器（端口 5173）
- 等待服务启动完成
- 显示服务状态和访问地址

**使用方法：**
```bash
./restart-servers.sh
```

**输出示例：**
```
========================================
    射击训练平台 - 服务器重启脚本
========================================

[1/4] 正在停止后端服务器...
  ✓ 后端服务器已停止

[2/4] 正在停止前端服务器...
  ✓ 前端服务器已停止

[3/4] 正在启动后端服务器...
  ✓ 后端服务器已就绪！

[4/4] 正在启动前端服务器...
  ✓ 前端服务器已就绪！

========================================
🎯 所有服务已启动！
========================================
```

---

### 2. `stop-servers.sh` - 停止服务器
一键停止所有正在运行的服务器。

**功能：**
- 停止后端服务器进程
- 停止前端服务器进程
- 显示停止状态

**使用方法：**
```bash
./stop-servers.sh
```

---

## 🔗 服务访问地址

启动后可以访问以下地址：

| 服务 | 地址 | 说明 |
|-----|------|------|
| 前端应用 | http://localhost:5173 | Vue.js 前端界面 |
| 后端 API | http://localhost:8083 | Spring Boot 后端 API |
| API 文档 | http://localhost:8083/doc.html | Knife4j API 文档 |

---

## 📝 日志文件

| 日志文件 | 路径 | 说明 |
|---------|------|------|
| 后端日志 | `app.log` | Spring Boot 应用日志 |
| 前端日志 | `shooting-frontend/frontend.log` | Vite 开发服务器日志 |

**查看日志命令：**
```bash
# 查看后端日志
tail -f app.log

# 查看前端日志
tail -f shooting-frontend/frontend.log
```

---

## 💡 常见用法

### 开发时重启服务器
```bash
./restart-servers.sh
```

### 停止所有服务
```bash
./stop-servers.sh
```

### 仅启动后端
```bash
mvn spring-boot:run > app.log 2>&1 &
```

### 仅启动前端
```bash
cd shooting-frontend && npm run dev > frontend.log 2>&1 &
```

### 检查服务是否运行
```bash
# 检查后端（端口 8083）
lsof -ti:8083

# 检查前端（端口 5173）
lsof -ti:5173

# 查看所有 Java 进程
ps aux | grep java
```

---

## 🐛 故障排除

### 端口被占用
如果端口被占用，可以使用脚本自动停止旧进程：
```bash
./stop-servers.sh
./restart-servers.sh
```

### 后端启动失败
1. 检查 MySQL 数据库是否运行
2. 查看日志文件：`tail -f app.log`
3. 确认端口 8083 未被占用

### 前端启动失败
1. 确认 Node.js 和 npm 已安装
2. 执行 `cd shooting-frontend && npm install`
3. 查看日志文件：`tail -f shooting-frontend/frontend.log`
4. 确认端口 5173 未被占用

---

## 🔧 脚本维护

如果需要修改脚本，请编辑以下文件：
- `restart-servers.sh` - 重启脚本
- `stop-servers.sh` - 停止脚本

修改后记得测试：
```bash
./restart-servers.sh
```

---

**最后更新：** 2025-09-30 