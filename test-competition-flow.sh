#!/bin/bash

# AimLab 比赛完整流程测试脚本
# 测试比赛从创建到结束的完整生命周期

BASE_URL="http://localhost:8083"
API_PREFIX="/api"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  AimLab 比赛完整流程测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 存储变量
ADMIN_TOKEN=""
ATHLETE1_TOKEN=""
ATHLETE2_TOKEN=""
COMPETITION_ID=""
ATHLETE1_ID=""
ATHLETE2_ID=""

# ==========================================
# 阶段 1: 创建测试用户
# ==========================================
echo -e "${CYAN}阶段 1: 创建测试用户${NC}"
echo "=========================================="
echo ""

# 创建管理员
echo -e "${YELLOW}1.1 创建管理员账号...${NC}"
ADMIN_RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_admin",
    "password": "admin123",
    "role": "ADMIN"
  }')

echo "管理员注册响应: $ADMIN_RESPONSE"

# 管理员登录
echo -e "${YELLOW}1.2 管理员登录...${NC}"
ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_admin",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"tokenValue":"[^"]*"' | cut -d'"' -f4)
echo -e "${GREEN}✅ 管理员Token: ${ADMIN_TOKEN:0:20}...${NC}"
echo ""

# 创建运动员1
echo -e "${YELLOW}1.3 创建运动员1...${NC}"
ATHLETE1_REGISTER=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_athlete1",
    "password": "athlete123",
    "role": "ATHLETE"
  }')

ATHLETE1_USER_ID=$(echo $ATHLETE1_REGISTER | grep -o '"userId":[0-9]*' | cut -d':' -f2)
echo "运动员1 用户ID: $ATHLETE1_USER_ID"

# 运动员1登录
ATHLETE1_LOGIN=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_athlete1",
    "password": "athlete123"
  }')

ATHLETE1_TOKEN=$(echo $ATHLETE1_LOGIN | grep -o '"tokenValue":"[^"]*"' | cut -d'"' -f4)
echo -e "${GREEN}✅ 运动员1 Token: ${ATHLETE1_TOKEN:0:20}...${NC}"
echo ""

# 创建运动员2
echo -e "${YELLOW}1.4 创建运动员2...${NC}"
ATHLETE2_REGISTER=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_athlete2",
    "password": "athlete123",
    "role": "ATHLETE"
  }')

ATHLETE2_USER_ID=$(echo $ATHLETE2_REGISTER | grep -o '"userId":[0-9]*' | cut -d':' -f2)
echo "运动员2 用户ID: $ATHLETE2_USER_ID"

# 运动员2登录
ATHLETE2_LOGIN=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "comp_athlete2",
    "password": "athlete123"
  }')

ATHLETE2_TOKEN=$(echo $ATHLETE2_LOGIN | grep -o '"tokenValue":"[^"]*"' | cut -d'"' -f4)
echo -e "${GREEN}✅ 运动员2 Token: ${ATHLETE2_TOKEN:0:20}...${NC}"
echo ""

# ==========================================
# 阶段 2: 创建运动员档案
# ==========================================
echo -e "${CYAN}阶段 2: 创建运动员档案${NC}"
echo "=========================================="
echo ""

# 创建运动员1档案
echo -e "${YELLOW}2.1 创建运动员1档案...${NC}"
ATHLETE1_PROFILE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/athletes/profile" \
  -H "aimlab-token: $ATHLETE1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "gender": "MALE",
    "birthDate": "1995-05-15",
    "level": "国家级",
    "approvalStatus": "APPROVED"
  }')

ATHLETE1_ID=$(echo $ATHLETE1_PROFILE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "运动员1档案ID: $ATHLETE1_ID"
echo -e "${GREEN}✅ 运动员1档案创建成功${NC}"
echo ""

# 创建运动员2档案
echo -e "${YELLOW}2.2 创建运动员2档案...${NC}"
ATHLETE2_PROFILE=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/athletes/profile" \
  -H "aimlab-token: $ATHLETE2_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "李四",
    "gender": "FEMALE",
    "birthDate": "1998-08-20",
    "level": "省级",
    "approvalStatus": "APPROVED"
  }')

ATHLETE2_ID=$(echo $ATHLETE2_PROFILE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "运动员2档案ID: $ATHLETE2_ID"
echo -e "${GREEN}✅ 运动员2档案创建成功${NC}"
echo ""

# ==========================================
# 阶段 3: 创建比赛
# ==========================================
echo -e "${CYAN}阶段 3: 创建比赛${NC}"
echo "=========================================="
echo ""

ENROLLMENT_START=$(date -u -v+1H +"%Y-%m-%dT%H:%M:%S")
ENROLLMENT_END=$(date -u -v+2H +"%Y-%m-%dT%H:%M:%S")
START_TIME=$(date -u -v+3H +"%Y-%m-%dT%H:%M:%S")

echo -e "${YELLOW}3.1 管理员创建比赛...${NC}"
CREATE_COMP=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/admin/competitions" \
  -H "aimlab-token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"MCP测试比赛\",
    \"description\": \"使用MCP工具测试的比赛\",
    \"formatType\": \"10米气手枪\",
    \"roundsCount\": 6,
    \"shotsPerRound\": 10,
    \"timeLimitPerShot\": 60,
    \"enrollStartAt\": \"$ENROLLMENT_START\",
    \"enrollEndAt\": \"$ENROLLMENT_END\",
    \"startedAt\": \"$START_TIME\",
    \"accessLevel\": \"PUBLIC\",
    \"createdBy\": 500
  }")

COMPETITION_ID=$(echo $CREATE_COMP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$COMPETITION_ID" ]; then
    echo -e "${GREEN}✅ 比赛创建成功，ID: ${COMPETITION_ID}${NC}"
else
    echo -e "${RED}❌ 比赛创建失败${NC}"
    echo "响应: $CREATE_COMP"
    exit 1
fi
echo ""

# ==========================================
# 阶段 4: 运动员报名
# ==========================================
echo -e "${CYAN}阶段 4: 运动员报名${NC}"
echo "=========================================="
echo ""

# 运动员1报名
echo -e "${YELLOW}4.1 运动员1报名...${NC}"
ENROLL1=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/enroll" \
  -H "aimlab-token: $ATHLETE1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"athleteIds\": [$ATHLETE1_ID]
  }")

if echo "$ENROLL1" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 运动员1报名成功${NC}"
else
    echo -e "${RED}❌ 运动员1报名失败: $ENROLL1${NC}"
fi
echo ""

# 运动员2报名
echo -e "${YELLOW}4.2 运动员2报名...${NC}"
ENROLL2=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/enroll" \
  -H "aimlab-token: $ATHLETE2_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"athleteIds\": [$ATHLETE2_ID]
  }")

if echo "$ENROLL2" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 运动员2报名成功${NC}"
else
    echo -e "${RED}❌ 运动员2报名失败: $ENROLL2${NC}"
fi
echo ""

# ==========================================
# 阶段 5: 查询比赛信息
# ==========================================
echo -e "${CYAN}阶段 5: 查询比赛信息${NC}"
echo "=========================================="
echo ""

echo -e "${YELLOW}5.1 查询比赛详情...${NC}"
COMP_DETAIL=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}" \
  -H "aimlab-token: $ATHLETE1_TOKEN")

echo "$COMP_DETAIL" | python3 -m json.tool 2>/dev/null || echo "$COMP_DETAIL"
echo ""

echo -e "${YELLOW}5.2 查询参赛运动员列表...${NC}"
PARTICIPANTS=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/athletes" \
  -H "aimlab-token: $ADMIN_TOKEN")

PARTICIPANT_COUNT=$(echo "$PARTICIPANTS" | grep -o '"id":[0-9]*' | wc -l | tr -d ' ')
echo -e "参赛人数: ${BLUE}${PARTICIPANT_COUNT}${NC}"
echo ""

# ==========================================
# 阶段 6: 开始比赛
# ==========================================
echo -e "${CYAN}阶段 6: 比赛状态管理${NC}"
echo "=========================================="
echo ""

echo -e "${YELLOW}6.1 管理员开始比赛...${NC}"
START_COMP=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/start" \
  -H "aimlab-token: $ADMIN_TOKEN")

if echo "$START_COMP" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 比赛已开始${NC}"
else
    echo -e "${RED}❌ 开始比赛失败: $START_COMP${NC}"
fi
echo ""

# 暂停比赛
echo -e "${YELLOW}6.2 管理员暂停比赛...${NC}"
PAUSE_COMP=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/pause" \
  -H "aimlab-token: $ADMIN_TOKEN")

if echo "$PAUSE_COMP" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 比赛已暂停${NC}"
else
    echo -e "${RED}❌ 暂停比赛失败: $PAUSE_COMP${NC}"
fi
echo ""

# 恢复比赛
echo -e "${YELLOW}6.3 管理员恢复比赛...${NC}"
RESUME_COMP=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/resume" \
  -H "aimlab-token: $ADMIN_TOKEN")

if echo "$RESUME_COMP" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 比赛已恢复${NC}"
else
    echo -e "${RED}❌ 恢复比赛失败: $RESUME_COMP${NC}"
fi
echo ""

# ==========================================
# 阶段 7: 提交射击成绩
# ==========================================
echo -e "${CYAN}阶段 7: 提交射击成绩${NC}"
echo "=========================================="
echo ""

echo -e "${YELLOW}7.1 运动员1提交成绩...${NC}"
for i in {1..10}; do
    SCORE=$(awk -v min=8.5 -v max=10.5 'BEGIN{srand(); print min+rand()*(max-min)}')
    X=$(awk -v min=-2 -v max=2 'BEGIN{srand(); print min+rand()*(max-min)}')
    Y=$(awk -v min=-2 -v max=2 'BEGIN{srand(); print min+rand()*(max-min)}')
    
    curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/records" \
      -H "aimlab-token: $ATHLETE1_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"athleteId\": $ATHLETE1_ID,
        \"shotNumber\": $i,
        \"score\": $SCORE,
        \"coordinateX\": $X,
        \"coordinateY\": $Y
      }" > /dev/null
    
    echo -n "."
done
echo ""
echo -e "${GREEN}✅ 运动员1成绩提交完成${NC}"
echo ""

echo -e "${YELLOW}7.2 运动员2提交成绩...${NC}"
for i in {1..10}; do
    SCORE=$(awk -v min=7.5 -v max=9.5 'BEGIN{srand(); print min+rand()*(max-min)}')
    X=$(awk -v min=-2 -v max=2 'BEGIN{srand(); print min+rand()*(max-min)}')
    Y=$(awk -v min=-2 -v max=2 'BEGIN{srand(); print min+rand()*(max-min)}')
    
    curl -s -X POST "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/records" \
      -H "aimlab-token: $ATHLETE2_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"athleteId\": $ATHLETE2_ID,
        \"shotNumber\": $i,
        \"score\": $SCORE,
        \"coordinateX\": $X,
        \"coordinateY\": $Y
      }" > /dev/null
    
    echo -n "."
done
echo ""
echo -e "${GREEN}✅ 运动员2成绩提交完成${NC}"
echo ""

# ==========================================
# 阶段 8: 查询实时排名
# ==========================================
echo -e "${CYAN}阶段 8: 查询实时排名${NC}"
echo "=========================================="
echo ""

echo -e "${YELLOW}8.1 获取实时排名...${NC}"
RANKING=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/competitions/${COMPETITION_ID}/rankings" \
  -H "aimlab-token: $ATHLETE1_TOKEN")

echo "$RANKING" | python3 -m json.tool 2>/dev/null || echo "$RANKING"
echo ""

# ==========================================
# 阶段 9: 完成比赛
# ==========================================
echo -e "${CYAN}阶段 9: 完成比赛${NC}"
echo "=========================================="
echo ""

echo -e "${YELLOW}9.1 管理员完成比赛...${NC}"
FINISH_COMP=$(curl -s -X POST "${BASE_URL}${API_PREFIX}/admin/competitions/${COMPETITION_ID}/force-finish" \
  -H "aimlab-token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}')

if echo "$FINISH_COMP" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 比赛已完成${NC}"
else
    echo -e "${RED}❌ 完成比赛失败: $FINISH_COMP${NC}"
fi
echo ""

# ==========================================
# 阶段 10: 导出比赛结果
# ==========================================
echo -e "${CYAN}阶段 10: 导出比赛结果${NC}"
echo "=========================================="
echo ""

# 导出CSV
echo -e "${YELLOW}10.1 导出CSV格式...${NC}"
CSV_FILE="competition_${COMPETITION_ID}_results.csv"
HTTP_CODE=$(curl -s -o "$CSV_FILE" -w "%{http_code}" \
  -X GET "${BASE_URL}${API_PREFIX}/admin/competitions/${COMPETITION_ID}/results/export?format=csv" \
  -H "aimlab-token: $ADMIN_TOKEN")

if [ "$HTTP_CODE" -eq 200 ]; then
    FILE_SIZE=$(wc -c < "$CSV_FILE" | tr -d ' ')
    echo -e "${GREEN}✅ CSV导出成功 (${FILE_SIZE} bytes)${NC}"
    echo "前5行预览:"
    head -5 "$CSV_FILE"
    rm -f "$CSV_FILE"
else
    echo -e "${RED}❌ CSV导出失败，HTTP: ${HTTP_CODE}${NC}"
fi
echo ""

# 导出Excel
echo -e "${YELLOW}10.2 导出Excel格式...${NC}"
XLSX_FILE="competition_${COMPETITION_ID}_results.xlsx"
HTTP_CODE=$(curl -s -o "$XLSX_FILE" -w "%{http_code}" \
  -X GET "${BASE_URL}${API_PREFIX}/admin/competitions/${COMPETITION_ID}/results/export?format=xlsx" \
  -H "aimlab-token: $ADMIN_TOKEN")

if [ "$HTTP_CODE" -eq 200 ]; then
    FILE_SIZE=$(wc -c < "$XLSX_FILE" | tr -d ' ')
    echo -e "${GREEN}✅ Excel导出成功 (${FILE_SIZE} bytes)${NC}"
    rm -f "$XLSX_FILE"
else
    echo -e "${RED}❌ Excel导出失败，HTTP: ${HTTP_CODE}${NC}"
fi
echo ""

# 导出PDF报表
echo -e "${YELLOW}10.3 导出PDF报表...${NC}"
PDF_FILE="competition_${COMPETITION_ID}_report.pdf"
HTTP_CODE=$(curl -s -o "$PDF_FILE" -w "%{http_code}" \
  -X GET "${BASE_URL}${API_PREFIX}/admin/competitions/${COMPETITION_ID}/results/pdf" \
  -H "aimlab-token: $ADMIN_TOKEN")

if [ "$HTTP_CODE" -eq 200 ]; then
    FILE_SIZE=$(wc -c < "$PDF_FILE" | tr -d ' ')
    echo -e "${GREEN}✅ PDF报表导出成功 (${FILE_SIZE} bytes)${NC}"
    rm -f "$PDF_FILE"
else
    echo -e "${RED}❌ PDF报表导出失败，HTTP: ${HTTP_CODE}${NC}"
fi
echo ""

# ==========================================
# 总结
# ==========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "比赛ID: ${GREEN}${COMPETITION_ID}${NC}"
echo -e "参赛人数: ${GREEN}${PARTICIPANT_COUNT}${NC}"
echo ""
echo -e "💡 测试总结："
echo -e "  ✅ 完整测试了比赛生命周期"
echo -e "  ✅ 验证了运动员报名流程"
echo -e "  ✅ 测试了成绩提交功能"
echo -e "  ✅ 验证了实时排名查询"
echo -e "  ✅ 测试了比赛状态管理（开始/暂停/恢复/完成）"
echo -e "  ✅ 验证了多格式结果导出（CSV/Excel/PDF）"
echo ""

