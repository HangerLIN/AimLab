#!/bin/bash

# 设置基本URL
BASE_URL="http://localhost:8083"

# 设置输出文件
OUTPUT_FILE="api_test_results.txt"

# 清空输出文件
> $OUTPUT_FILE

# 测试函数
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "测试: $description" | tee -a $OUTPUT_FILE
    echo "请求: $method $endpoint" | tee -a $OUTPUT_FILE
    
    if [ -n "$data" ]; then
        echo "请求数据: $data" | tee -a $OUTPUT_FILE
        response=$(curl -s -X "$method" -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    else
        response=$(curl -s -X "$method" "$BASE_URL$endpoint")
    fi
    
    echo "响应: $response" | tee -a $OUTPUT_FILE
    echo "----------------------------------------" | tee -a $OUTPUT_FILE
    echo "" | tee -a $OUTPUT_FILE
}

echo "开始API测试..." | tee -a $OUTPUT_FILE
echo "时间: $(date)" | tee -a $OUTPUT_FILE
echo "----------------------------------------" | tee -a $OUTPUT_FILE
echo "" | tee -a $OUTPUT_FILE

# 健康检查
test_api "GET" "/actuator/health" "" "健康检查"

# 认证接口
test_api "POST" "/api/auth/register" '{"username":"testuser", "password":"password123", "name":"测试用户", "role":"ATHLETE"}' "用户注册"
test_api "POST" "/api/auth/login" '{"username":"testuser", "password":"password123"}' "用户登录"

# 用户接口
test_api "POST" "/api/users/register" '{"username":"testuser2", "password":"password123", "name":"测试用户2", "role":"ATHLETE"}' "用户注册(用户接口)"
test_api "POST" "/api/users/login" '{"username":"testuser2", "password":"password123"}' "用户登录(用户接口)"

# 运动员接口
test_api "GET" "/api/athletes/profile" "" "获取运动员档案(未登录)"
test_api "POST" "/api/athletes/profile" '{"name":"测试运动员", "gender":"MALE", "birthDate":"1990-01-01", "height":180, "weight":70, "team":"测试队伍"}' "创建运动员档案(未登录)"
test_api "GET" "/api/athletes/1" "" "获取ID为1的运动员信息"
test_api "GET" "/api/athletes/1/profile" "" "获取ID为1的运动员个人资料"
test_api "GET" "/api/athletes/my-profile" "" "获取当前登录用户的运动员个人资料"

# 比赛接口
test_api "GET" "/api/competitions" "" "获取所有比赛"
test_api "GET" "/api/competitions/1" "" "获取ID为1的比赛详情"
test_api "GET" "/api/competitions/upcoming" "" "获取即将举行的比赛"

# 训练接口
test_api "GET" "/api/training" "" "获取训练列表"
test_api "GET" "/api/training/1" "" "获取ID为1的训练详情"

# 射击记录接口
test_api "GET" "/api/shooting-records" "" "获取所有射击记录"
test_api "GET" "/api/shooting-records/1" "" "获取ID为1的射击记录"

echo "API测试完成" | tee -a $OUTPUT_FILE
echo "时间: $(date)" | tee -a $OUTPUT_FILE
