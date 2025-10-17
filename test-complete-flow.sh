#!/bin/bash

set -e

echo "=========================================="
echo "完整的 Token 认证测试"
echo "=========================================="
echo ""

# 生成随机用户名
USERNAME="testuser_$(date +%s)"
PASSWORD="test123456"

# 步骤 1: 注册用户
echo "步骤 1: 注册新用户 ($USERNAME)"
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"name\":\"测试用户\",\"role\":\"ATHLETE\"}")

echo "$REGISTER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$REGISTER_RESPONSE"
echo ""

# 步骤 2: 登录获取 token
echo "步骤 2: 登录获取 token"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESPONSE"

# 提取 token
TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('tokenInfo', {}).get('tokenValue', ''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，无法获取 token"
    exit 1
fi

echo ""
echo "✓ 成功获取 token: ${TOKEN:0:40}..."
echo ""

# 步骤 3: 使用 token 访问需要认证的接口
echo "步骤 3: 使用有效 token 访问 /api/athletes/profile"
PROFILE_RESPONSE=$(curl -s -w "\n[HTTP_STATUS:%{http_code}]" http://localhost:8083/api/athletes/profile \
  -H "aimlab-token: $TOKEN")

echo "$PROFILE_RESPONSE"
echo ""

# 检查是否成功或返回档案不存在（这也是正常的）
if echo "$PROFILE_RESPONSE" | grep -q '"success":false.*"运动员档案不存在"'; then
    echo "✓ Token 有效！但用户尚未创建运动员档案（这是正常的）"
    echo ""
    
    # 步骤 4: 创建运动员档案
    echo "步骤 4: 创建运动员档案"
    CREATE_PROFILE_RESPONSE=$(curl -s -w "\n[HTTP_STATUS:%{http_code}]" -X POST http://localhost:8083/api/athletes/profile \
      -H "Content-Type: application/json" \
      -H "aimlab-token: $TOKEN" \
      -d "{\"name\":\"测试运动员\",\"gender\":\"MALE\",\"birthDate\":\"2000-01-01\",\"height\":175,\"weight\":70,\"dominantHand\":\"RIGHT\"}")
    
    echo "$CREATE_PROFILE_RESPONSE"
    echo ""
    
    if echo "$CREATE_PROFILE_RESPONSE" | grep -q '"success":true'; then
        echo "✓ 运动员档案创建成功！"
        echo ""
        
        # 步骤 5: 再次获取档案
        echo "步骤 5: 再次获取运动员档案"
        PROFILE_RESPONSE2=$(curl -s -w "\n[HTTP_STATUS:%{http_code}]" http://localhost:8083/api/athletes/profile \
          -H "aimlab-token: $TOKEN")
        
        echo "$PROFILE_RESPONSE2"
        echo ""
        
        if echo "$PROFILE_RESPONSE2" | grep -q '"success":true'; then
            echo "✓ 成功获取运动员档案！"
        fi
    fi
elif echo "$PROFILE_RESPONSE" | grep -q '"success":true'; then
    echo "✓ Token 有效！成功获取运动员档案"
elif echo "$PROFILE_RESPONSE" | grep -q "NOT_LOGIN"; then
    echo "❌ Token 认证失败"
else
    echo "⚠️  未知响应"
fi

echo ""

# 步骤 6: 使用无效 token 测试
echo "步骤 6: 使用无效 token 测试（应该返回 401）"
INVALID_RESPONSE=$(curl -s -w "\n[HTTP_STATUS:%{http_code}]" http://localhost:8083/api/athletes/profile \
  -H "aimlab-token: invalid-token-12345")

echo "$INVALID_RESPONSE"
echo ""

if echo "$INVALID_RESPONSE" | grep -q "NOT_LOGIN"; then
    echo "✓ 正确拒绝了无效 token"
else
    echo "⚠️  无效 token 处理异常"
fi

echo ""
echo "=========================================="
echo "✅ 所有测试完成！"
echo "=========================================="







