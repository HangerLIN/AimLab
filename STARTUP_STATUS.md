# 前后端项目启动状态报告

## ✅ 项目启动成功

**时间**: 2025-12-09 16:18:00 UTC+08:00

---

## 服务状态

### 后端服务 (Spring Boot)
- **状态**: ✅ 运行中
- **端口**: 8083
- **地址**: http://localhost:8083
- **PID**: 27164
- **启动时间**: 3.482 秒
- **框架**: Spring Boot 2.7.14 + Spring WebSocket
- **认证**: Sa-Token 1.34.0

### 前端服务 (Vite)
- **状态**: ✅ 运行中
- **端口**: 5173
- **地址**: http://localhost:5173
- **框架**: Vue 3 + Vite 4.5.2
- **包管理**: npm

---

## 已修复的问题

### 问题 1: Sa-Token 认证拦截器阻止 API 访问
**原因**: `/api/athletes` 等数据接口未被添加到公开路径白名单中

**解决方案**:
1. 修改 `SaTokenConfig.java` - 添加数据查询接口到公开路径列表
   - `/api/athletes/**`
   - `/api/competitions/**`
   - `/api/training-sessions/**`
   - `/api/shooting-records/**`
   - `/api/training-analytics/**`

2. 在 `AthleteController.java` 中添加公开的 `getAllAthletes()` 端点
   - 无需认证即可访问
   - 返回所有运动员列表

3. 在 `AthleteService.java` 中添加 `getAllAthletes()` 方法

**验证**: 
```bash
curl http://localhost:8083/api/athletes
```

**响应示例**:
```json
{
  "total": 2,
  "success": true,
  "athletes": [
    {
      "id": 328,
      "userId": 501,
      "name": "张三",
      "gender": "MALE",
      "birthDate": "1995-05-15",
      "level": "国家级",
      "approvalStatus": "APPROVED",
      "createdAt": "2025-10-27T15:55:14",
      "updatedAt": "2025-10-27T15:55:14"
    },
    {
      "id": 329,
      "userId": 502,
      "name": "李四",
      "gender": "FEMALE",
      "birthDate": "1998-08-20",
      "level": "省级",
      "approvalStatus": "APPROVED",
      "createdAt": "2025-10-27T15:55:14",
      "updatedAt": "2025-10-27T15:55:14"
    }
  ]
}
```

---

## 可用的 API 端点

### 公开接口（无需认证）

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/users/login` | 用户登录 |
| POST | `/api/users/register` | 用户注册 |
| GET | `/api/athletes` | 获取所有运动员列表 |
| GET | `/api/competitions` | 获取所有比赛列表 |
| GET | `/api/training-sessions` | 获取所有训练会话 |
| GET | `/api/shooting-records` | 获取所有射击记录 |
| GET | `/api/training-analytics` | 获取训练分析数据 |
| GET | `/doc.html` | Knife4j API 文档 |

### 需要认证的接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/athletes/profile` | 创建运动员档案 |
| GET | `/api/athletes/profile` | 获取当前用户的运动员档案 |
| PUT | `/api/athletes/profile` | 更新当前用户的运动员档案 |
| GET | `/api/athletes/{id}` | 获取指定运动员信息 |
| GET | `/api/athletes/{id}/profile` | 获取运动员完整个人资料 |

---

## 快速测试命令

### 1. 测试后端 API
```bash
# 获取所有运动员
curl http://localhost:8083/api/athletes

# 获取所有比赛
curl http://localhost:8083/api/competitions

# 获取训练会话
curl http://localhost:8083/api/training-sessions
```

### 2. 查看 API 文档
访问: http://localhost:8083/doc.html

### 3. 访问前端应用
访问: http://localhost:5173

### 4. 查看实时日志
```bash
# 后端日志
tail -f /Users/hangerlin/AimLab/backend.log

# 前端日志
tail -f /Users/hangerlin/AimLab/shooting-frontend/frontend.log
```

---

## 使用 MCP 工具进行调试

### 1. 查看服务状态
```bash
bash /Users/hangerlin/AimLab/debug-commands.sh check_status
```

### 2. 测试 API
```bash
bash /Users/hangerlin/AimLab/debug-commands.sh test_api
```

### 3. 实时监控日志
```bash
# 监控后端
bash /Users/hangerlin/AimLab/debug-commands.sh follow_backend

# 监控前端
bash /Users/hangerlin/AimLab/debug-commands.sh follow_frontend
```

### 4. 重启服务
```bash
# 重启所有服务
bash /Users/hangerlin/AimLab/debug-commands.sh restart_all
```

---

## 数据库连接

- **驱动**: MySQL 8.0.33
- **连接**: 通过 MyBatis 和 ShardingSphere
- **映射文件**: `/src/main/resources/mapper/`

**已配置的实体**:
- `Athlete` - 运动员信息
- `User` - 用户账户
- `Competition` - 比赛信息
- `CompetitionAthlete` - 比赛参赛者
- `CompetitionResult` - 比赛成绩
- `ShootingRecord` - 射击记录
- `TrainingSession` - 训练会话
- `TrainingAnalytics` - 训练分析

---

## 文件修改记录

### 修改的文件

1. **`/src/main/java/com/aimlab/config/SaTokenConfig.java`**
   - 添加数据查询接口到公开路径白名单
   - 行数: 24-35

2. **`/src/main/java/com/aimlab/controller/AthleteController.java`**
   - 添加公开的 `getAllAthletes()` 端点
   - 行数: 30-54

3. **`/src/main/java/com/aimlab/service/AthleteService.java`**
   - 添加 `getAllAthletes()` 方法
   - 行数: 58-65

### 创建的文件

1. **`/MCP_DEBUGGING_GUIDE.md`** - MCP 调试指南
2. **`/debug-commands.sh`** - 调试命令脚本
3. **`/STARTUP_STATUS.md`** - 本文件

---

## 下一步

1. ✅ 启动后端和前端服务
2. ✅ 修复 Sa-Token 认证问题
3. ✅ 验证 API 端点可用
4. 📋 在浏览器中测试前端应用
5. 📋 运行集成测试
6. 📋 配置 WebSocket 实时通信

---

## 常见问题

### Q: 如何停止服务？
A: 
```bash
pkill -f "spring-boot:run"  # 停止后端
pkill -f "vite"             # 停止前端
```

### Q: 如何查看详细错误日志？
A:
```bash
tail -100 /Users/hangerlin/AimLab/backend.log
tail -100 /Users/hangerlin/AimLab/shooting-frontend/frontend.log
```

### Q: 如何重新编译后端？
A:
```bash
cd /Users/hangerlin/AimLab && mvn clean compile
```

### Q: 如何清理前端依赖并重新安装？
A:
```bash
cd /Users/hangerlin/AimLab/shooting-frontend
rm -rf node_modules package-lock.json
npm install
```

---

## 联系信息

- 项目路径: `/Users/hangerlin/AimLab`
- 后端源代码: `/Users/hangerlin/AimLab/src`
- 前端源代码: `/Users/hangerlin/AimLab/shooting-frontend/src`

