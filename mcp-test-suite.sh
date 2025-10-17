#!/bin/bash

# AimLab 系统完整测试套件
# 基于 Cursor + MCP 测试指南

set -e  # 遇到错误立即退出

BASE_URL="http://localhost:8083"
TOKEN=""
ATHLETE_ID=""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 执行测试并记录结果
run_test() {
    local test_name=$1
    local test_command=$2
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "\n${BLUE}[测试 $TOTAL_TESTS] $test_name${NC}"
    
    if eval "$test_command"; then
        print_success "$test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        print_error "$test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# ========================================
# 第一阶段：系统健康检查
# ========================================
print_header "第一阶段：系统启动与基础验证"

run_test "健康检查端点" \
    "curl -s $BASE_URL/api/auth/test | jq -e '.success == true'"

# ========================================
# 第二阶段：用户认证系统测试
# ========================================
print_header "第二阶段：用户认证系统测试"

# 2.1 用户登录
print_info "测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test123"}')

echo "$LOGIN_RESPONSE" | jq .

if echo "$LOGIN_RESPONSE" | jq -e '.success == true' > /dev/null; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.tokenInfo.tokenValue')
    print_success "登录成功，Token: ${TOKEN:0:20}..."
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    print_error "登录失败"
    FAILED_TESTS=$((FAILED_TESTS + 1))
    exit 1
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# ========================================
# 第三阶段：运动员档案管理测试
# ========================================
print_header "第三阶段：运动员档案管理测试"

# 3.1 获取当前用户档案
print_info "测试获取当前用户档案..."
PROFILE_RESPONSE=$(curl -s -X GET $BASE_URL/api/athletes/profile \
    -H "aimlab-token: $TOKEN")
echo "$PROFILE_RESPONSE" | jq .

if echo "$PROFILE_RESPONSE" | jq -e '.success == true' > /dev/null; then
    ATHLETE_ID=$(echo "$PROFILE_RESPONSE" | jq -r '.athlete.id')
    print_success "获取档案成功，运动员ID: $ATHLETE_ID"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    print_info "档案不存在，将创建新档案"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# 3.2 更新运动员档案
print_info "测试更新运动员档案..."
UPDATE_RESPONSE=$(curl -s -X PUT $BASE_URL/api/athletes/profile \
    -H "Content-Type: application/json" \
    -H "aimlab-token: $TOKEN" \
    -d '{
        "name": "MCP测试运动员",
        "gender": "MALE",
        "birthDate": "1990-05-15",
        "level": "国家一级"
    }')
echo "$UPDATE_RESPONSE" | jq .

run_test "更新运动员档案" \
    "echo '$UPDATE_RESPONSE' | jq -e '.success == true'"

# 3.3 获取完整个人资料
print_info "测试获取完整个人资料（含历史记录）..."
FULL_PROFILE=$(curl -s -X GET $BASE_URL/api/athletes/my-profile \
    -H "aimlab-token: $TOKEN")
echo "$FULL_PROFILE" | jq .

run_test "获取完整个人资料" \
    "echo '$FULL_PROFILE' | jq -e '.success == true'"

# ========================================
# 第四阶段：训练管理系统测试
# ========================================
print_header "第四阶段：训练管理系统测试"

# 4.1 开始新训练场次
print_info "测试开始新训练场次..."
SESSION_NAME="MCP自动化测试_$(date +%s)"
START_SESSION=$(curl -s -X POST $BASE_URL/api/training/sessions/start \
    -H "Content-Type: application/json" \
    -H "aimlab-token: $TOKEN" \
    -d "{\"name\": \"$SESSION_NAME\"}")
echo "$START_SESSION" | jq .

SESSION_ID=$(echo "$START_SESSION" | jq -r '.session.id // empty')
if [ -n "$SESSION_ID" ]; then
    print_success "训练场次创建成功，ID: $SESSION_ID"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    print_error "创建训练场次失败"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# 4.2 添加射击记录
if [ -n "$SESSION_ID" ]; then
    print_info "测试添加射击记录..."
    
    for i in {1..5}; do
        X=$(echo "scale=4; 0.$RANDOM" | bc)
        Y=$(echo "scale=4; 0.$RANDOM" | bc)
        SCORE=$(echo "scale=1; $RANDOM % 100 / 10" | bc)
        
        RECORD_RESPONSE=$(curl -s -X POST $BASE_URL/api/training/records \
            -H "Content-Type: application/json" \
            -H "aimlab-token: $TOKEN" \
            -d "{
                \"trainingSessionId\": $SESSION_ID,
                \"x\": $X,
                \"y\": $Y,
                \"score\": $SCORE,
                \"recordType\": \"TRAINING\"
            }")
        
        if echo "$RECORD_RESPONSE" | jq -e '.success == true' > /dev/null; then
            print_success "添加第 $i 条射击记录 (得分: $SCORE 环)"
        else
            print_error "添加第 $i 条射击记录失败"
            echo "$RECORD_RESPONSE" | jq .
        fi
    done
fi

# 4.3 获取训练记录
if [ -n "$SESSION_ID" ]; then
    print_info "测试获取训练记录..."
    RECORDS=$(curl -s -X GET "$BASE_URL/api/training/sessions/$SESSION_ID/records" \
        -H "aimlab-token: $TOKEN")
    echo "$RECORDS" | jq .
    
    run_test "获取训练记录" \
        "echo '$RECORDS' | jq -e '.success == true'"
fi

# 4.4 获取训练报告
if [ -n "$SESSION_ID" ]; then
    print_info "测试获取训练报告..."
    REPORT=$(curl -s -X GET "$BASE_URL/api/training/sessions/$SESSION_ID/report" \
        -H "aimlab-token: $TOKEN")
    echo "$REPORT" | jq .
    
    run_test "获取训练报告" \
        "echo '$REPORT' | jq -e 'has(\"totalShots\")'"
fi

# 4.5 结束训练场次
if [ -n "$SESSION_ID" ]; then
    print_info "测试结束训练场次..."
    END_SESSION=$(curl -s -X POST "$BASE_URL/api/training/sessions/$SESSION_ID/end" \
        -H "Content-Type: application/json" \
        -H "aimlab-token: $TOKEN" \
        -d '{"notes": "MCP自动化测试完成"}')
    echo "$END_SESSION" | jq .
    
    run_test "结束训练场次" \
        "echo '$END_SESSION' | jq -e '.success == true'"
fi

# ========================================
# 第五阶段：比赛管理系统测试
# ========================================
print_header "第五阶段：比赛管理系统测试"

# 5.1 创建比赛
print_info "测试创建比赛..."
COMPETITION_NAME="MCP自动化测试比赛_$(date +%s)"
CREATE_COMP=$(curl -s -X POST $BASE_URL/api/competitions \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$COMPETITION_NAME\",
        \"description\": \"MCP工具自动化测试比赛\",
        \"roundsCount\": 3,
        \"shotsPerRound\": 10,
        \"timeLimitPerShot\": 60
    }")
echo "$CREATE_COMP" | jq .

COMP_ID=$(echo "$CREATE_COMP" | jq -r '.competition.id // empty')
if [ -n "$COMP_ID" ]; then
    print_success "比赛创建成功，ID: $COMP_ID"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    print_error "创建比赛失败"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# 5.2 获取比赛列表
print_info "测试获取比赛列表..."
COMP_LIST=$(curl -s -X GET $BASE_URL/api/competitions)
echo "$COMP_LIST" | jq '.competitions | length' | xargs echo "比赛数量:"

run_test "获取比赛列表" \
    "echo '$COMP_LIST' | jq -e '.success == true'"

# 5.3 运动员报名参赛
if [ -n "$COMP_ID" ] && [ -n "$ATHLETE_ID" ]; then
    print_info "测试运动员报名参赛..."
    ENROLL=$(curl -s -X POST "$BASE_URL/api/competitions/$COMP_ID/enroll" \
        -H "Content-Type: application/json" \
        -d "{\"athleteIds\": [$ATHLETE_ID]}")
    echo "$ENROLL" | jq .
    
    run_test "运动员报名参赛" \
        "echo '$ENROLL' | jq -e '.success == true'"
fi

# 5.4 获取参赛运动员列表
if [ -n "$COMP_ID" ]; then
    print_info "测试获取参赛运动员列表..."
    ATHLETES=$(curl -s -X GET "$BASE_URL/api/competitions/$COMP_ID/athletes")
    echo "$ATHLETES" | jq .
    
    run_test "获取参赛运动员列表" \
        "echo '$ATHLETES' | jq -e '.success == true'"
fi

# 5.5 开始比赛
if [ -n "$COMP_ID" ]; then
    print_info "测试开始比赛..."
    START_COMP=$(curl -s -X POST "$BASE_URL/api/competitions/$COMP_ID/start")
    echo "$START_COMP" | jq .
    
    run_test "开始比赛" \
        "echo '$START_COMP' | jq -e '.success == true'"
fi

# 5.6 获取实时排名
if [ -n "$COMP_ID" ]; then
    sleep 2  # 等待比赛状态更新
    print_info "测试获取实时排名..."
    RANKING=$(curl -s -X GET "$BASE_URL/api/competitions/$COMP_ID/rankings")
    echo "$RANKING" | jq .
    
    run_test "获取实时排名" \
        "echo '$RANKING' | jq -e '.success == true'"
fi

# 5.7 完成比赛
if [ -n "$COMP_ID" ]; then
    print_info "测试完成比赛..."
    COMPLETE_COMP=$(curl -s -X POST "$BASE_URL/api/competitions/$COMP_ID/complete")
    echo "$COMPLETE_COMP" | jq .
    
    run_test "完成比赛" \
        "echo '$COMPLETE_COMP' | jq -e '.success == true'"
fi

# 5.8 获取比赛结果
if [ -n "$COMP_ID" ]; then
    sleep 1
    print_info "测试获取比赛结果..."
    RESULTS=$(curl -s -X GET "$BASE_URL/api/competitions/$COMP_ID/results")
    echo "$RESULTS" | jq .
    
    run_test "获取比赛结果" \
        "echo '$RESULTS' | jq -e '.success == true'"
fi

# ========================================
# 测试总结
# ========================================
print_header "测试执行总结"

echo -e "${BLUE}总测试数：${NC} $TOTAL_TESTS"
echo -e "${GREEN}通过：${NC} $PASSED_TESTS"
echo -e "${RED}失败：${NC} $FAILED_TESTS"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 所有测试通过！${NC}\n"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  有 $FAILED_TESTS 个测试失败${NC}\n"
    exit 1
fi

