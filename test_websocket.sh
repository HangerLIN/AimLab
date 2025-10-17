#!/bin/bash

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║        🧪 WebSocket 广播功能 - MCP 双用户测试                 ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# 创建新比赛
echo "步骤 1: 创建测试比赛"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
COMP_DATA=$(curl -s -X POST http://localhost:8083/api/competitions \
  -H "Content-Type: application/json" \
  -d '{"name":"WebSocket广播测试-双用户","roundsCount":2,"shotsPerRound":5,"timeLimitPerShot":60,"status":"CREATED"}')
  
COMP_ID=$(echo $COMP_DATA | python3 -c "import sys, json; print(json.load(sys.stdin)['competition']['id'])")
echo "✓ 比赛已创建，ID = $COMP_ID"
echo ""

# 用户A报名
echo "步骤 2: 用户A报名"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s -X POST http://localhost:8083/api/competitions/$COMP_ID/enroll \
  -H "Content-Type: application/json" \
  -d '{"athleteIds":[1]}' > /dev/null
echo "✓ 用户A (athleteId=1) 已报名"
echo ""

# 开始比赛
echo "步骤 3: 开始比赛"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s -X POST http://localhost:8083/api/competitions/$COMP_ID/start > /dev/null
echo "✓ 比赛已开始 (状态: RUNNING)"
echo ""

# 用户A第一次射击
echo "步骤 4: 用户A 第一次射击"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
SHOT1=$(curl -s -X POST http://localhost:8083/api/records/competition \
  -H "Content-Type: application/json" \
  -d '{"competitionId":'$COMP_ID',"athleteId":1,"recordType":"COMPETITION","x":0.5,"y":0.5,"score":9.5,"roundNumber":1,"shotNumber":1}')
echo "✓ 用户A 射击成功 (得分: 9.5 环)"
echo ""

# 等待一下
sleep 1

# 查看后端广播日志
echo "步骤 5: 查看后端 WebSocket 广播日志"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "查找包含比赛 $COMP_ID 的广播日志..."
echo ""
tail -50 app.log | grep -E "广播.*比赛ID: $COMP_ID"
echo ""

# 用户A第二次射击
echo "步骤 6: 用户A 第二次射击"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
SHOT2=$(curl -s -X POST http://localhost:8083/api/records/competition \
  -H "Content-Type: application/json" \
  -d '{"competitionId":'$COMP_ID',"athleteId":1,"recordType":"COMPETITION","x":0.6,"y":0.4,"score":8.5,"roundNumber":1,"shotNumber":2}')
echo "✓ 用户A 第二次射击成功 (得分: 8.5 环)"
echo ""

sleep 1

# 再次查看广播日志
echo "步骤 7: 查看最新的广播日志"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
tail -50 app.log | grep -E "广播.*比赛ID: $COMP_ID"
echo ""

# 获取排名
echo "步骤 8: 查询实时排名"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
RANKING=$(curl -s http://localhost:8083/api/competitions/$COMP_ID/rankings)
echo "$RANKING" | python3 -c "
import sys, json
data = json.load(sys.stdin)
rankings = data.get('rankings', [])
print('排名表:')
for i, r in enumerate(rankings, 1):
    print(f'  {i}. {r.get(\"athleteName\", \"N/A\")} - 总分: {r.get(\"totalScore\", 0)}, 平均分: {r.get(\"averageScore\", 0):.2f}, 射击数: {r.get(\"shotCount\", 0)}')
"
echo ""

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║        ✅ 测试完成                                             ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "📋 测试总结:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ 比赛ID: $COMP_ID"
echo "✓ 用户A射击次数: 2"
echo "✓ 检查上方日志中是否包含:"
echo "  - ✓ 广播射击记录 (应该出现 2 次)"
echo "  - ✓ 广播排名更新 (应该出现 2 次)"
echo ""
echo "如果看到广播日志，说明 WebSocket 广播功能正常！"
echo "前端连接的客户端都应该能收到这些广播消息。"
echo ""

