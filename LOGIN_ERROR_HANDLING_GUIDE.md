# 登录功能错误处理机制完整指南

## 📋 概述

本文档详细说明了射击训练平台登录功能中实现的完整错误处理机制。系统在三个层次进行验证和错误处理：
1. **前端表单验证** - 客户端即时反馈
2. **后端字段验证** - 服务器端验证
3. **业务逻辑验证** - 用户认证验证

---

## 🎯 错误处理类型

### 1. 用户名/密码为空错误

#### 场景
用户提交空用户名或空密码

#### 前端表现
- 通过 Element Plus Form 组件的 `required` 验证规则
- 显示红色错误提示："请输入用户名" / "请输入密码"
- 提交按钮处于加载状态时立即验证

#### 后端响应
```json
{
  "success": false,
  "message": "表单验证失败",
  "fieldErrors": {
    "username": "用户名不能为空",
    "password": "密码不能为空"
  }
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第127-134行)
- **后端**: `/src/main/java/com/aimlab/controller/AuthController.java` (第194-202行)

---

### 2. 用户不存在错误

#### 场景
用户输入的用户名在数据库中不存在

#### 提示信息
**"用户不存在，请先注册"**

#### 前端表现
- 在表单上方显示红色警告框 (`el-alert`)
- 同时显示 ElMessage 错误提示
- 保留用户名输入，清除密码输入

#### 后端响应
```json
{
  "success": false,
  "message": "用户不存在，请先注册",
  "errorCode": "USER_NOT_FOUND"
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第172-179行)
- **后端**: 
  - `/src/main/java/com/aimlab/service/UserService.java` (第86-88行)
  - `/src/main/java/com/aimlab/controller/AuthController.java` (第111-114行)

#### 工作流程
```
用户输入用户名 → 后端通过 userMapper.findByUsername() 查询
    ↓
查询结果为 null
    ↓
抛出 RuntimeException("用户不存在，请先注册")
    ↓
AuthController 捕获异常并设置 errorCode = "USER_NOT_FOUND"
    ↓
返回 fieldErrors 和错误消息到前端
    ↓
前端显示警告框和错误提示
```

---

### 3. 密码错误提示

#### 场景
用户输入的密码与数据库中的加密密码不匹配

#### 提示信息
**"密码错误，请重新输入"**

#### 前端表现
- 在表单上方显示红色警告框
- 显示 ElMessage 错误提示
- 保留用户名，清除密码输入框
- 建议用户重新输入

#### 后端响应
```json
{
  "success": false,
  "message": "密码错误，请重新输入",
  "errorCode": "WRONG_PASSWORD"
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第180-187行)
- **后端**: 
  - `/src/main/java/com/aimlab/service/UserService.java` (第93-95行)
  - `/src/main/java/com/aimlab/controller/AuthController.java` (第111-114行)

#### 密码验证流程
```
用户输入密码 → BCryptPasswordEncoder.matches() 验证
    ↓
密码匹配成功 → 继续
密码匹配失败 → 抛出 RuntimeException("密码错误，请重新输入")
    ↓
AuthController 捕获异常并设置 errorCode = "WRONG_PASSWORD"
    ↓
返回错误消息到前端
```

---

### 4. 用户名长度验证

#### 前端验证 (表单级别)
- **最小长度**: 3 个字符
- **最大长度**: 20 个字符
- **错误消息**: "用户名长度应在3-20个字符之间"

#### 后端验证 (字段验证)
```java
if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
    throw new RuntimeException("用户名长度应在3-20个字符之间");
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第128-129行)
- **后端**: `/src/main/java/com/aimlab/service/UserService.java` (第41-44行)

---

### 5. 密码长度验证

#### 前端验证
- **最小长度**: 6 个字符
- **错误消息**: "密码长度至少为6个字符"

#### 后端验证
```java
if (user.getPassword().length() < 6) {
    throw new RuntimeException("密码长度至少为6个字符");
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第132-133行)
- **后端**: `/src/main/java/com/aimlab/service/UserService.java` (第48-50行)

---

### 6. 注册 - 用户名已存在

#### 场景
注册时使用已被注册过的用户名

#### 提示信息
**"用户名已存在，请更换用户名"**

#### 前端表现
- 显示红色警告框和 ElMessage 错误提示
- 保留填写的表单数据便于用户修改

#### 后端响应
```json
{
  "success": false,
  "message": "用户名已存在，请更换用户名",
  "errorCode": "REGISTER_FAILED"
}
```

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第198-210行)
- **后端**: `/src/main/java/com/aimlab/service/UserService.java` (第56-59行)

---

### 7. 注册 - 密码确认不一致

#### 场景
用户在注册时输入的两个密码不一致

#### 提示信息
**"两次输入密码不一致"**

#### 前端表现
- 在密码确认字段下显示红色错误提示
- 提交按钮被禁用（表单验证失败）

#### 代码位置
- **前端**: `/shooting-frontend/src/views/Login.vue` (第137-146行)

```javascript
{
  validator: (rule, value, callback) => {
    if (value !== loginForm.password) {
      callback(new Error('两次输入密码不一致'));
    } else {
      callback();
    }
  },
  trigger: 'blur'
}
```

---

## 🔄 错误处理流程图

### 登录流程
```
用户点击"登录"
    ↓
前端表单验证
├─ 用户名不为空？
├─ 密码不为空？
└─ 用户名长度 3-20？
    ↓ (验证通过)
发送 POST /api/auth/login
    ↓
后端 AuthController.login()
    ↓
字段级验证
├─ 用户名为空？→ 返回字段错误
└─ 密码为空？ → 返回字段错误
    ↓ (字段验证通过)
业务逻辑验证 (UserService.login())
    ├─ 用户是否存在？
    │  └─ NO → "用户不存在，请先注册"
    ├─ 密码是否正确？
    │  └─ NO → "密码错误，请重新输入"
    ├─ 账号是否启用？
    │  └─ NO → "账号已被禁用，请联系管理员"
    └─ YES → 生成 Token，返回成功
    ↓
前端接收响应
├─ 成功 → 保存 Token，跳转到首页
└─ 失败 → 显示错误信息
```

### 注册流程
```
用户点击"注册"
    ↓
前端表单验证
├─ 用户名不为空？
├─ 密码不为空？
├─ 确认密码不为空？
├─ 用户名长度 3-20？
├─ 密码长度 >= 6？
├─ 密码与确认密码是否一致？
└─ 姓名不为空？
    ↓ (验证通过)
发送 POST /api/auth/register
    ↓
后端 AuthController.register()
    ↓
字段级验证
├─ 用户名为空？→ 返回字段错误
├─ 密码为空？ → 返回字段错误
├─ 用户名长度？ → 返回字段错误
└─ 密码长度？ → 返回字段错误
    ↓ (字段验证通过)
业务逻辑验证 (UserService.register())
    ├─ 用户名是否已存在？
    │  └─ YES → "用户名已存在，请更换用户名"
    ├─ 加密密码
    ├─ 设置默认角色和状态
    └─ 保存到数据库
    ↓
前端接收响应
├─ 成功 → 显示成功提示，自动切换到登录模式
└─ 失败 → 显示错误信息
```

---

## 📱 前端实现细节

### Login.vue 组件关键代码

#### 1. 通用错误提示框
```vue
<!-- 通用错误提示 -->
<el-alert 
  v-if="generalError" 
  :title="generalError" 
  type="error"
  :closable="true"
  @close="generalError = ''"
  class="error-alert"
/>
```

#### 2. 登录处理函数
```javascript
const handleLogin = async () => {
  try {
    generalError.value = '';
    await loginFormRef.value.validate();  // 前端验证
    
    isLoading.value = true;
    const success = await userStore.login({  // 调用 API
      username: loginForm.username,
      password: loginForm.password
    });
    
    if (success) {
      ElMessage.success('登录成功');
      router.push('/');
    }
  } catch (error) {
    // 处理字段验证错误
    if (error.fieldErrors) {
      for (const [field, message] of Object.entries(error.fieldErrors)) {
        ElMessage.error(message);
      }
    }
    // 处理业务错误
    else if (error.message) {
      generalError.value = error.message;
      ElMessage.error(error.message);
    }
  } finally {
    isLoading.value = false;
  }
};
```

#### 3. Pinia Store 错误处理
```javascript
async login(credentials) {
  const response = await fetch('/api/auth/login', {...});
  const result = await response.json();
  
  if (!result.success) {
    const error = new Error(result.message);
    error.errorCode = result.errorCode;
    error.fieldErrors = result.fieldErrors;
    throw error;  // 抛出包含详细信息的错误
  }
  // ... 继续处理成功的登录
}
```

---

## 🔐 后端实现细节

### AuthController 关键代码

#### 1. 字段验证方法
```java
private Map<String, String> validateLoginForm(String username, String password) {
    Map<String, String> errors = new HashMap<>();
    
    if (username == null || username.trim().isEmpty()) {
        errors.put("username", "用户名不能为空");
    }
    
    if (password == null || password.trim().isEmpty()) {
        errors.put("password", "密码不能为空");
    }
    
    return errors;
}
```

#### 2. 登录端点完整实现
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
    try {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        // 字段级别验证
        Map<String, String> fieldErrors = validateLoginForm(username, password);
        if (!fieldErrors.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "表单验证失败");
            response.put("fieldErrors", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 业务逻辑验证和认证
        SaTokenInfo tokenInfo = userService.login(username, password);
        
        // 返回成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("tokenInfo", tokenInfo);
        
        return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
        // 业务异常处理
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", e.getMessage());
        
        // 根据错误信息设置错误代码
        String message = e.getMessage();
        if (message.contains("用户不存在")) {
            error.put("errorCode", "USER_NOT_FOUND");
        } else if (message.contains("密码错误")) {
            error.put("errorCode", "WRONG_PASSWORD");
        } else if (message.contains("禁用")) {
            error.put("errorCode", "ACCOUNT_DISABLED");
        } else {
            error.put("errorCode", "LOGIN_FAILED");
        }
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

### UserService 关键代码

#### 1. 登录验证逻辑
```java
public SaTokenInfo login(String username, String password) {
    // 验证用户名是否为空
    if (username == null || username.trim().isEmpty()) {
        throw new RuntimeException("用户名不能为空");
    }
    
    // 验证密码是否为空
    if (password == null || password.trim().isEmpty()) {
        throw new RuntimeException("密码不能为空");
    }
    
    // 根据用户名查询用户
    User user = userMapper.findByUsername(username);
    
    // 用户不存在
    if (user == null) {
        throw new RuntimeException("用户不存在，请先注册");
    }
    
    // 密码不匹配
    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new RuntimeException("密码错误，请重新输入");
    }
    
    // 账号被禁用
    if (user.getStatus() != 1) {
        throw new RuntimeException("账号已被禁用，请联系管理员");
    }
    
    // 登录成功
    StpUtil.login(user.getId());
    userMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());
    
    return StpUtil.getTokenInfo();
}
```

---

## 🧪 测试场景

### 测试命令
```bash
bash /Users/hangerlin/AimLab/test-login-errors.sh
```

### 测试覆盖
✅ 用户名为空  
✅ 密码为空  
✅ 用户名和密码都为空  
✅ 用户不存在  
✅ 密码错误  
✅ 注册 - 用户名为空  
✅ 注册 - 密码为空  
✅ 注册 - 用户名太短  
✅ 注册 - 密码太短  
✅ 注册 - 用户名已存在  
✅ 成功登录  

---

## 📊 错误代码对应表

| 错误代码 | 含义 | HTTP 状态 | 用户提示 |
|---------|------|----------|--------|
| USER_NOT_FOUND | 用户不存在 | 400 | 用户不存在，请先注册 |
| WRONG_PASSWORD | 密码错误 | 400 | 密码错误，请重新输入 |
| ACCOUNT_DISABLED | 账号被禁用 | 400 | 账号已被禁用，请联系管理员 |
| LOGIN_FAILED | 登录失败 | 400 | 登录失败，请重试 |
| REGISTER_FAILED | 注册失败 | 400 | 注册失败，具体错误见 message |
| SERVER_ERROR | 服务器错误 | 400 | 登录失败，请重试 |

---

## 🔗 API 端点

### 登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "hangerlin",
  "password": "password123"
}
```

**成功响应 (200)**
```json
{
  "success": true,
  "message": "登录成功",
  "tokenInfo": {
    "tokenName": "aimlab-token",
    "tokenValue": "xxx-xxx-xxx",
    "loginId": "123",
    ...
  }
}
```

**失败响应 (400)**
```json
{
  "success": false,
  "message": "用户不存在，请先注册",
  "errorCode": "USER_NOT_FOUND",
  "fieldErrors": {}
}
```

### 注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "name": "用户名"
}
```

**成功响应 (200)**
```json
{
  "success": true,
  "message": "注册成功",
  "userId": 123
}
```

**失败响应 (400)**
```json
{
  "success": false,
  "message": "用户名已存在，请更换用户名",
  "errorCode": "REGISTER_FAILED"
}
```

---

## 📝 修改摘要

### 后端修改
1. **UserService.java**
   - 增强 `login()` 方法：分别验证用户存在和密码正确性
   - 增强 `register()` 方法：添加字段验证和长度检查

2. **AuthController.java**
   - 优化 `login()` 端点：添加字段验证和错误代码处理
   - 优化 `register()` 端点：添加字段验证
   - 新增 `validateLoginForm()` 和 `validateRegisterForm()` 验证方法

### 前端修改
1. **Login.vue**
   - 添加通用错误提示框 (el-alert)
   - 增强 `handleLogin()` 函数：区分字段验证和业务错误
   - 增强 `handleRegister()` 函数：详细的错误处理
   - 改进错误消息展示

2. **user.js (Pinia Store)**
   - 增强错误对象：包含 `errorCode` 和 `fieldErrors`
   - 完善 `login()` 和 `register()` 方法的错误传递

---

## ✨ 特性亮点

✅ **清晰明确的错误消息** - 用户能快速定位问题  
✅ **多层次验证** - 前端即时反馈，后端双重验证  
✅ **字段级错误** - 可同时显示多个字段的错误  
✅ **业务级错误** - 准确的认证错误提示  
✅ **错误代码** - 便于前端精准处理不同的错误类型  
✅ **用户友好** - 提示信息自然，建议性强  

---

## 🚀 快速开始

### 1. 启动服务
```bash
cd /Users/hangerlin/AimLab
bash restart-servers.sh
```

### 2. 访问应用
```
http://localhost:5173
```

### 3. 测试错误处理
```bash
bash test-login-errors.sh
```

### 4. 查看日志
```bash
# 后端日志
tail -f app.log

# 前端日志
tail -f shooting-frontend/frontend.log
```

---

## 📞 文件位置

| 文件 | 路径 |
|------|------|
| 后端服务类 | `/src/main/java/com/aimlab/service/UserService.java` |
| 后端控制器 | `/src/main/java/com/aimlab/controller/AuthController.java` |
| 前端登录组件 | `/shooting-frontend/src/views/Login.vue` |
| 前端 Store | `/shooting-frontend/src/store/modules/user.js` |
| 测试脚本 | `/test-login-errors.sh` |
| 本指南 | `/LOGIN_ERROR_HANDLING_GUIDE.md` |

---

**最后更新**: 2025-12-15  
**状态**: ✅ 完整实现并通过测试
