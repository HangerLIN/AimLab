#!/bin/bash

# AimLab 管理员功能测试脚本
# 使用 curl 测试管理员相关的API接口

BASE_URL="http://localhost:8083"
API_PREFIX="/api"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  AimLab 管理员功能测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. 测试登录 - 创建管理员用户并登录
echo -e "${YELLOW}测试 1: 用户注册和登录${NC}"
echo "-----------------------------------"

# 注册管理员账号
echo -e "📝 注册管理员账号..."
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_mcp_test3",
    "password": "admin123456",
    "role": "ADMIN"
  }')

echo "注册响应: $REGISTER_RESPONSE"
echo ""

# 登录获取Token
echo -e "🔑 管理员登录..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_mcp_test3",
    "password": "admin123456"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"tokenValue":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 登录失败，未获取到token${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 登录成功，Token: ${TOKEN:0:20}...${NC}"
echo ""

# 2. 测试获取仪表盘数据
echo -e "${YELLOW}测试 2: 获取仪表盘统计数据${NC}"
echo "-----------------------------------"
DASHBOARD_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/admin/dashboard" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json")

echo "仪表盘响应:"
echo "$DASHBOARD_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$DASHBOARD_RESPONSE"
echo ""

if echo "$DASHBOARD_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 仪表盘数据获取成功${NC}"
else
    echo -e "${RED}❌ 仪表盘数据获取失败${NC}"
fi
echo ""

# 3. 测试获取用户列表
echo -e "${YELLOW}测试 3: 获取用户列表${NC}"
echo "-----------------------------------"
USERS_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/admin/users?page=1&size=10" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json")

echo "用户列表响应:"
echo "$USERS_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$USERS_RESPONSE"
echo ""

if echo "$USERS_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 用户列表获取成功${NC}"
    USER_COUNT=$(echo "$USERS_RESPONSE" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "用户总数: ${BLUE}${USER_COUNT}${NC}"
else
    echo -e "${RED}❌ 用户列表获取失败${NC}"
fi
echo ""

# 4. 测试创建新用户
echo -e "${YELLOW}测试 4: 创建新用户${NC}"
echo "-----------------------------------"
CREATE_USER_RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/admin/users" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user_mcp",
    "password": "testpass123",
    "role": "user",
    "status": 1
  }')

echo "创建用户响应:"
echo "$CREATE_USER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CREATE_USER_RESPONSE"
echo ""

NEW_USER_ID=$(echo "$CREATE_USER_RESPONSE" | grep -o '"userId":[0-9]*' | cut -d':' -f2)

if [ -n "$NEW_USER_ID" ]; then
    echo -e "${GREEN}✅ 用户创建成功，用户ID: ${NEW_USER_ID}${NC}"
else
    echo -e "${RED}❌ 用户创建失败${NC}"
fi
echo ""

# 5. 测试获取运动员列表
echo -e "${YELLOW}测试 5: 获取运动员列表${NC}"
echo "-----------------------------------"
ATHLETES_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/admin/athletes" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json")

echo "运动员列表响应:"
echo "$ATHLETES_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ATHLETES_RESPONSE"
echo ""

if echo "$ATHLETES_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 运动员列表获取成功${NC}"
else
    echo -e "${RED}❌ 运动员列表获取失败${NC}"
fi
echo ""

# 6. 测试导出运动员档案（CSV）
echo -e "${YELLOW}测试 6: 导出运动员档案 (CSV格式)${NC}"
echo "-----------------------------------"
EXPORT_FILE="athletes_export_test.csv"
HTTP_CODE=$(curl -s -o "$EXPORT_FILE" -w "%{http_code}" \
  -X GET "${BASE_URL}${API_PREFIX}/admin/athletes/export?format=csv" \
  -H "aimlab-token: $TOKEN")

if [ "$HTTP_CODE" -eq 200 ]; then
    FILE_SIZE=$(wc -c < "$EXPORT_FILE" | tr -d ' ')
    echo -e "${GREEN}✅ CSV导出成功${NC}"
    echo -e "文件大小: ${BLUE}${FILE_SIZE} bytes${NC}"
    echo "文件内容预览:"
    head -5 "$EXPORT_FILE"
    rm -f "$EXPORT_FILE"
else
    echo -e "${RED}❌ CSV导出失败，HTTP状态码: ${HTTP_CODE}${NC}"
fi
echo ""

# 7. 测试导出运动员档案（Excel）
echo -e "${YELLOW}测试 7: 导出运动员档案 (Excel格式)${NC}"
echo "-----------------------------------"
EXPORT_FILE="athletes_export_test.xlsx"
HTTP_CODE=$(curl -s -o "$EXPORT_FILE" -w "%{http_code}" \
  -X GET "${BASE_URL}${API_PREFIX}/admin/athletes/export?format=xlsx" \
  -H "aimlab-token: $TOKEN")

if [ "$HTTP_CODE" -eq 200 ]; then
    FILE_SIZE=$(wc -c < "$EXPORT_FILE" | tr -d ' ')
    echo -e "${GREEN}✅ Excel导出成功${NC}"
    echo -e "文件大小: ${BLUE}${FILE_SIZE} bytes${NC}"
    rm -f "$EXPORT_FILE"
else
    echo -e "${RED}❌ Excel导出失败，HTTP状态码: ${HTTP_CODE}${NC}"
fi
echo ""

# 8. 测试训练统计 - 时间维度
echo -e "${YELLOW}测试 8: 训练统计分析 - 时间维度${NC}"
echo "-----------------------------------"
TRAINING_TIME_RESPONSE=$(curl -s -X GET \
  "${BASE_URL}${API_PREFIX}/admin/training/analytics/time?granularity=DAY" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json")

echo "训练统计响应:"
echo "$TRAINING_TIME_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TRAINING_TIME_RESPONSE"
echo ""

if echo "$TRAINING_TIME_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 训练统计获取成功${NC}"
else
    echo -e "${RED}❌ 训练统计获取失败${NC}"
fi
echo ""

# 9. 测试训练统计 - 运动员维度
echo -e "${YELLOW}测试 9: 训练统计分析 - 运动员维度${NC}"
echo "-----------------------------------"
TRAINING_ATHLETE_RESPONSE=$(curl -s -X GET \
  "${BASE_URL}${API_PREFIX}/admin/training/analytics/athlete" \
  -H "aimlab-token: $TOKEN" \
  -H "Content-Type: application/json")

echo "运动员统计响应:"
echo "$TRAINING_ATHLETE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TRAINING_ATHLETE_RESPONSE"
echo ""

if echo "$TRAINING_ATHLETE_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 运动员统计获取成功${NC}"
else
    echo -e "${RED}❌ 运动员统计获取失败${NC}"
fi
echo ""

# 10. 测试更新用户信息
if [ -n "$NEW_USER_ID" ]; then
    echo -e "${YELLOW}测试 10: 更新用户信息${NC}"
    echo "-----------------------------------"
    UPDATE_USER_RESPONSE=$(curl -s -X PUT \
      "${BASE_URL}${API_PREFIX}/admin/users/${NEW_USER_ID}" \
      -H "aimlab-token: $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "role": "coach",
        "status": 1
      }')

    echo "更新用户响应:"
    echo "$UPDATE_USER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$UPDATE_USER_RESPONSE"
    echo ""

    if echo "$UPDATE_USER_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ 用户信息更新成功${NC}"
    else
        echo -e "${RED}❌ 用户信息更新失败${NC}"
    fi
    echo ""
fi

# 11. 测试重置密码
if [ -n "$NEW_USER_ID" ]; then
    echo -e "${YELLOW}测试 11: 重置用户密码${NC}"
    echo "-----------------------------------"
    RESET_PWD_RESPONSE=$(curl -s -X POST \
      "${BASE_URL}${API_PREFIX}/admin/users/${NEW_USER_ID}/reset-password" \
      -H "aimlab-token: $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "password": "newpassword123"
      }')

    echo "重置密码响应:"
    echo "$RESET_PWD_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESET_PWD_RESPONSE"
    echo ""

    if echo "$RESET_PWD_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ 密码重置成功${NC}"
    else
        echo -e "${RED}❌ 密码重置失败${NC}"
    fi
    echo ""
fi

# 总结
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Token: ${GREEN}$TOKEN${NC}"
echo ""
echo -e "💡 提示："
echo -e "  - 可以使用此token在Knife4j界面中测试其他接口"
echo -e "  - 访问 ${BLUE}http://localhost:8083/doc.html${NC} 查看API文档"
echo -e "  - 在Authorization中填入token即可调用管理员接口"
echo ""

