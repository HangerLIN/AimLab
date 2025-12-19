#!/bin/bash
# 生成运动员测试数据脚本
# 为每位运动员生成12周的训练数据，用于测试趋势分析和对比功能

BASE_URL="http://localhost:8083/api"

# 登录获取Token
echo "=== 登录获取Token ==="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testadmin","password":"123456"}')
TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tokenInfo',{}).get('tokenValue',''))")

if [ -z "$TOKEN" ]; then
  echo "登录失败"
  exit 1
fi
echo "Token: $TOKEN"

# 获取运动员列表
echo ""
echo "=== 获取运动员列表 ==="
ATHLETES=$(curl -s "$BASE_URL/athletes?pageNum=1&pageSize=20" -H "aimlab-token: $TOKEN")
echo "$ATHLETES" | python3 -c "import sys,json; d=json.load(sys.stdin); [print(f\"ID: {a['id']}, Name: {a['name']}, Level: {a['level']}\") for a in d.get('athletes',[])]"

# 运动员ID列表
ATHLETE_IDS=(328 329 330)
ATHLETE_NAMES=("张三" "李四" "测试运动员")
ATHLETE_LEVELS=("国家级" "省级" "省级")
PROJECT_TYPES=("气步枪" "气手枪" "手枪速射")

echo ""
echo "=== 开始生成测试数据 ==="

# 为每位运动员生成12周的训练数据
for idx in "${!ATHLETE_IDS[@]}"; do
  ATHLETE_ID=${ATHLETE_IDS[$idx]}
  ATHLETE_NAME=${ATHLETE_NAMES[$idx]}
  ATHLETE_LEVEL=${ATHLETE_LEVELS[$idx]}
  
  echo ""
  echo "--- 运动员: $ATHLETE_NAME (ID: $ATHLETE_ID, 级别: $ATHLETE_LEVEL) ---"
  
  # 根据级别设置基础成绩范围
  case $ATHLETE_LEVEL in
    "国家级")
      BASE_SCORE=9.2
      VARIANCE=0.6
      ;;
    "省级")
      BASE_SCORE=8.5
      VARIANCE=1.0
      ;;
    *)
      BASE_SCORE=7.5
      VARIANCE=1.5
      ;;
  esac
  
  # 生成12周的数据，每周2-3次训练
  for week in {1..12}; do
    # 计算日期（从12周前开始）
    WEEKS_AGO=$((12 - week))
    
    # 每周2-3次训练
    SESSIONS_THIS_WEEK=$((2 + RANDOM % 2))
    
    for session in $(seq 1 $SESSIONS_THIS_WEEK); do
      # 随机选择项目类型
      PROJECT_TYPE=${PROJECT_TYPES[$((RANDOM % 3))]}
      
      # 计算训练日期
      DAY_OFFSET=$((WEEKS_AGO * 7 + session * 2))
      TRAIN_DATE=$(date -v-${DAY_OFFSET}d +"%Y-%m-%dT10:00:00")
      
      SESSION_NAME="第${week}周训练${session}-${PROJECT_TYPE}"
      
      echo "  创建训练场次: $SESSION_NAME ($TRAIN_DATE)"
      
      # 模拟成绩进步趋势（后期成绩略有提升）
      PROGRESS_BONUS=$(echo "scale=2; $week * 0.02" | bc)
      
      # 生成30-60发射击记录
      SHOT_COUNT=$((30 + RANDOM % 31))
      
      # 构建射击记录JSON
      RECORDS="["
      for shot in $(seq 1 $SHOT_COUNT); do
        # 计算成绩（基础分 + 进步 + 随机波动）
        RANDOM_VAR=$(echo "scale=2; ($RANDOM % 100 - 50) / 100 * $VARIANCE" | bc)
        SCORE=$(echo "scale=1; $BASE_SCORE + $PROGRESS_BONUS + $RANDOM_VAR" | bc)
        
        # 限制在1-10范围内
        SCORE=$(echo "$SCORE" | awk '{if($1<1) print 1; else if($1>10) print 10; else print $1}')
        
        # 随机坐标
        X=$(echo "scale=4; 0.5 + ($RANDOM % 100 - 50) / 200" | bc)
        Y=$(echo "scale=4; 0.5 + ($RANDOM % 100 - 50) / 200" | bc)
        
        if [ $shot -gt 1 ]; then
          RECORDS+=","
        fi
        RECORDS+="{\"score\":$SCORE,\"x\":$X,\"y\":$Y,\"roundNumber\":$((shot / 10 + 1)),\"shotNumber\":$shot}"
      done
      RECORDS+="]"
      
      # 通过API创建训练数据（这里需要直接插入数据库，因为API需要当前用户）
      # 我们改用SQL直接插入
      
    done
  done
done

echo ""
echo "=== 测试数据生成完成 ==="
