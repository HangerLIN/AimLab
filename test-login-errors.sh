#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  登录错误处理机制测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

BASE_URL="http://localhost:8083/api/auth"

# 测试 1: 用户名为空
echo -e "${YELLOW}[1] 测试用户名为空${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "", "password": "password123"}' | jq '.' 
echo ""

# 测试 2: 密码为空
echo -e "${YELLOW}[2] 测试密码为空${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": ""}' | jq '.'
echo ""

# 测试 3: 用户名和密码都为空
echo -e "${YELLOW}[3] 测试用户名和密码都为空${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "", "password": ""}' | jq '.'
echo ""

# 测试 4: 用户不存在
echo -e "${YELLOW}[4] 测试用户不存在${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "nonexistent_user_xyz", "password": "password123"}' | jq '.'
echo ""

# 测试 5: 密码错误
echo -e "${YELLOW}[5] 测试密码错误（假设用户 hangerlin 存在）${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "hangerlin", "password": "wrongpassword"}' | jq '.'
echo ""

# 测试 6: 注册 - 用户名为空
echo -e "${YELLOW}[6] 测试注册 - 用户名为空${NC}"
curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "", "password": "password123"}' | jq '.'
echo ""

# 测试 7: 注册 - 密码为空
echo -e "${YELLOW}[7] 测试注册 - 密码为空${NC}"
curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": ""}' | jq '.'
echo ""

# 测试 8: 注册 - 用户名太短
echo -e "${YELLOW}[8] 测试注册 - 用户名太短（少于3个字符）${NC}"
curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "ab", "password": "password123"}' | jq '.'
echo ""

# 测试 9: 注册 - 密码太短
echo -e "${YELLOW}[9] 测试注册 - 密码太短（少于6个字符）${NC}"
curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser123", "password": "pass"}' | jq '.'
echo ""

# 测试 10: 注册 - 用户名已存在
echo -e "${YELLOW}[10] 测试注册 - 用户名已存在（使用 hangerlin）${NC}"
curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "hangerlin", "password": "password123", "name": "Test User"}' | jq '.'
echo ""

# 测试 11: 成功登录
echo -e "${YELLOW}[11] 测试成功登录（使用有效凭证）${NC}"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "hangerlin", "password": "hangerlin"}' | jq '.'
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ 所有测试完成${NC}"
echo -e "${BLUE}========================================${NC}"
