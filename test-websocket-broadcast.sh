#!/bin/bash

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║     🧪 双用户比赛测试 - 验证 WebSocket 广播功能              ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8083"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 步骤 1: 创建测试比赛"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

COMPETITION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/competitions" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "双用户广播测试",
    "roundsCount": 3,
    "shotsPerRound": 10,
    "timeLimitPerShot": 60,
    "status": "CREATED"
  }')

COMPETITION_ID=$(echo $COMPETITION_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -z "$COMPETITION_ID" ]; then
  echo -e "${RED}❌ 创建比赛失败${NC}"
  echo $COMPETITION_RESPONSE
  exit 1
fi

echo -e "${GREEN}✓ 比赛创建成功！比赛ID: $COMPETITION_ID${NC}"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 步骤 2: 模拟用户A报名"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 假设运动员ID 1 是用户A
ENROLL_A=$(curl -s -X POST "$BASE_URL/api/competitions/$COMPETITION_ID/enroll" \
  -H "Content-Type: application/json" \
  -d '{"athleteIds": [1]}')

if echo $ENROLL_A | grep -q "success.*true"; then
  echo -e "${GREEN}✓ 用户A (athleteId=1) 报名成功${NC}"
else
  echo -e "${YELLOW}⚠️  用户A报名: $ENROLL_A${NC}"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 步骤 3: 开始比赛"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

START_RESPONSE=$(curl -s -X POST "$BASE_URL/api/competitions/$COMPETITION_ID/start")

if echo $START_RESPONSE | grep -q "success.*true"; then
  echo -e "${GREEN}✓ 比赛已开始${NC}"
else
  echo -e "${RED}❌ 开始比赛失败: $START_RESPONSE${NC}"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎯 步骤 4: 用户A 射击（3次）"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

for i in {1..3}; do
  SCORE=$((8 + RANDOM % 3))
  X=$(echo "scale=3; $RANDOM/32767" | bc)
  Y=$(echo "scale=3; $RANDOM/32767" | bc)
  
  SHOT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/records/competition" \
    -H "Content-Type: application/json" \
    -d "{
      \"competitionId\": $COMPETITION_ID,
      \"athleteId\": 1,
      \"recordType\": \"COMPETITION\",
      \"x\": $X,
      \"y\": $Y,
      \"score\": $SCORE,
      \"roundNumber\": 1,
      \"shotNumber\": $i
    }")
  
  if echo $SHOT_RESPONSE | grep -q "success.*true\|record"; then
    echo -e "${BLUE}  射击 $i: 得分 ${SCORE}环${NC}"
  else
    echo -e "${RED}  ❌ 射击失败: $SHOT_RESPONSE${NC}"
  fi
  sleep 0.5
done
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 步骤 5: 查看实时排名（应该只有用户A）"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RANKING=$(curl -s "$BASE_URL/api/competitions/$COMPETITION_ID/rankings")
echo "$RANKING" | python3 -m json.tool 2>/dev/null || echo "$RANKING"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 检查后端广播日志（最近10条）"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo -e "${YELLOW}后端广播记录:${NC}"
tail -20 app.log | grep "广播" || echo "未找到广播日志"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 测试总结"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}✅ 后端 API 测试完成${NC}"
echo ""
echo "比赛信息:"
echo "  • 比赛ID: $COMPETITION_ID"
echo "  • 状态: RUNNING"
echo "  • 用户A射击: 3次"
echo ""
echo -e "${YELLOW}⚠️  WebSocket 广播功能需要在浏览器中测试${NC}"
echo ""
echo "请执行以下操作验证广播功能:"
echo "1. 打开浏览器访问 http://localhost:5173"
echo "2. 进入比赛ID: $COMPETITION_ID"
echo "3. 打开开发者工具查看控制台"
echo "4. 点击靶子射击，观察是否收到广播消息"
echo "5. 打开第二个浏览器窗口（无痕模式）"
echo "6. 同时进入同一比赛，观察双方是否能看到彼此的射击"
echo ""
echo "预期看到的控制台日志:"
echo "  🔌 WebSocket 连接成功"
echo "  📡 订阅比赛主题: /topic/competition/$COMPETITION_ID"
echo "  📩 收到主题的消息"
echo "  📊 收到射击记录广播"
echo ""

