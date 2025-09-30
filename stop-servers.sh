#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    射击训练平台 - 停止服务器${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. 停止后端服务器
echo -e "${YELLOW}[1/2] 正在停止后端服务器...${NC}"
BACKEND_PIDS=$(ps aux | grep -E "mvn.*spring-boot:run|java.*ShootingTrainingCompetitionSystemApplication" | grep -v grep | awk '{print $2}')

if [ -z "$BACKEND_PIDS" ]; then
    echo -e "${YELLOW}  ⚠ 后端服务器未运行${NC}"
else
    for PID in $BACKEND_PIDS; do
        echo -e "  → 终止进程 PID: $PID"
        kill $PID 2>/dev/null
    done
    sleep 2
    # 强制终止仍在运行的进程
    for PID in $BACKEND_PIDS; do
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "  → 强制终止进程 PID: $PID"
            kill -9 $PID 2>/dev/null
        fi
    done
    echo -e "${GREEN}  ✓ 后端服务器已停止${NC}"
fi
echo ""

# 2. 停止前端服务器
echo -e "${YELLOW}[2/2] 正在停止前端服务器...${NC}"
FRONTEND_PIDS=$(lsof -ti:5173 2>/dev/null)

if [ -z "$FRONTEND_PIDS" ]; then
    echo -e "${YELLOW}  ⚠ 前端服务器未运行${NC}"
else
    for PID in $FRONTEND_PIDS; do
        echo -e "  → 终止进程 PID: $PID"
        kill $PID 2>/dev/null
    done
    sleep 1
    # 强制终止仍在运行的进程
    FRONTEND_PIDS=$(lsof -ti:5173 2>/dev/null)
    if [ ! -z "$FRONTEND_PIDS" ]; then
        for PID in $FRONTEND_PIDS; do
            echo -e "  → 强制终止进程 PID: $PID"
            kill -9 $PID 2>/dev/null
        done
    fi
    echo -e "${GREEN}  ✓ 前端服务器已停止${NC}"
fi
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ 所有服务已停止${NC}"
echo -e "${BLUE}========================================${NC}"
echo "" 