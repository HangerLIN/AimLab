#!/bin/bash

API_URL="http://localhost:8083/api"

echo "正在创建测试用户..."

# 1. 注册普通用户
curl -s -X POST "$API_URL/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "password": "123456"
  }' | grep -o '"message":"[^"]*'

# 2. 注册管理员用户
curl -s -X POST "$API_URL/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testadmin",
    "password": "123456"
  }' | grep -o '"message":"[^"]*'

# 3. 登录并获取管理员TOKEN
ADMIN_LOGIN=$(curl -s -X POST "$API_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testadmin",
    "password": "123456"
  }')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"token":"[^"]*' | cut -d'"' -f4)
ADMIN_ID=$(echo $ADMIN_LOGIN | grep -o '"userId":[0-9]*' | cut -d':' -f2)

echo "管理员ID: $ADMIN_ID"

if [ ! -z "$ADMIN_TOKEN" ]; then
  # 4. 更新管理员角色
  curl -s -X PUT "$API_URL/admin/users/$ADMIN_ID" \
    -H 'Content-Type: application/json' \
    -H "Authorization: $ADMIN_TOKEN" \
    -d '{"role": "ADMIN"}' | grep -o '"success":[^,]*'
  
  echo "✓ 测试环境设置完成"
fi
