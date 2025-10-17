#!/bin/bash

echo "=========================================="
echo "测试 Token 认证修复"
echo "=========================================="
echo ""

# 测试 1: 未登录访问 profile 接口
echo "测试 1: 未登录访问 /api/athletes/profile"
echo "预期结果: 返回 401 和友好的错误提示"
echo ""
curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8083/api/athletes/profile
echo ""
echo "=========================================="
echo ""

# 测试 2: 登录获取 token
echo "测试 2: 尝试登录获取 token"
echo ""
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "登录响应:"
echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# 提取 token
TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('tokenInfo', {}).get('tokenValue', ''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
    echo "⚠️  无法获取 token，可能用户不存在或密码错误"
    echo "提示：请先注册用户或使用正确的用户名密码"
else
    echo "✓ 成功获取 token: ${TOKEN:0:30}..."
    echo ""
    echo "=========================================="
    echo ""
    
    # 测试 3: 使用 token 访问 profile 接口
    echo "测试 3: 使用有效 token 访问 /api/athletes/profile"
    echo ""
    PROFILE_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8083/api/athletes/profile \
      -H "aimlab-token: $TOKEN")
    
    echo "响应:"
    echo "$PROFILE_RESPONSE"
    echo ""
    echo "=========================================="
    echo ""
    
    # 测试 4: 使用无效 token
    echo "测试 4: 使用无效 token 访问接口"
    echo ""
    curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8083/api/athletes/profile \
      -H "aimlab-token: invalid-token-12345"
    echo ""
fi

echo "=========================================="
echo "测试完成"
echo "=========================================="







