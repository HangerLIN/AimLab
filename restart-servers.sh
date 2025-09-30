#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
FRONTEND_DIR="$SCRIPT_DIR/shooting-frontend"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    射击训练平台 - 服务器重启脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. 停止后端服务器
echo -e "${YELLOW}[1/4] 正在停止后端服务器...${NC}"
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
echo -e "${YELLOW}[2/4] 正在停止前端服务器...${NC}"
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

# 3. 启动后端服务器
echo -e "${YELLOW}[3/4] 正在启动后端服务器...${NC}"
cd "$SCRIPT_DIR"
mvn spring-boot:run > app.log 2>&1 &
BACKEND_PID=$!
echo -e "  → 后端服务进程 PID: $BACKEND_PID"
echo -e "  → 日志文件: $SCRIPT_DIR/app.log"
echo -e "${GREEN}  ✓ 后端服务器启动中（端口 8083）${NC}"
echo ""

# 等待后端启动
echo -e "${YELLOW}  ⏳ 等待后端服务器启动...${NC}"
TIMEOUT=30
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    if curl -s http://localhost:8083/api/auth/test > /dev/null 2>&1 || grep -q "Started ShootingTrainingCompetitionSystemApplication" app.log 2>/dev/null; then
        echo -e "${GREEN}  ✓ 后端服务器已就绪！${NC}"
        break
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))
    echo -ne "  ⏳ 已等待 ${ELAPSED}s...\r"
done

if [ $ELAPSED -ge $TIMEOUT ]; then
    echo -e "${YELLOW}  ⚠ 后端启动超时，请检查日志: tail -f app.log${NC}"
fi
echo ""

# 4. 启动前端服务器
echo -e "${YELLOW}[4/4] 正在启动前端服务器...${NC}"
cd "$FRONTEND_DIR"
npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "  → 前端服务进程 PID: $FRONTEND_PID"
echo -e "  → 日志文件: $FRONTEND_DIR/frontend.log"
echo -e "${GREEN}  ✓ 前端服务器启动中（端口 5173）${NC}"
echo ""

# 等待前端启动
echo -e "${YELLOW}  ⏳ 等待前端服务器启动...${NC}"
TIMEOUT=10
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    if lsof -ti:5173 > /dev/null 2>&1; then
        echo -e "${GREEN}  ✓ 前端服务器已就绪！${NC}"
        break
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))
done
echo ""

# 5. 显示服务状态
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}           服务器状态${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ 后端服务器:${NC} http://localhost:8083"
echo -e "  API文档: http://localhost:8083/doc.html"
echo -e "  日志: tail -f $SCRIPT_DIR/app.log"
echo ""
echo -e "${GREEN}✓ 前端服务器:${NC} http://localhost:5173"
echo -e "  日志: tail -f $FRONTEND_DIR/frontend.log"
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}🎯 所有服务已启动！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}提示：${NC}"
echo -e "  - 查看后端日志: ${BLUE}tail -f app.log${NC}"
echo -e "  - 查看前端日志: ${BLUE}tail -f shooting-frontend/frontend.log${NC}"
echo -e "  - 停止所有服务: ${BLUE}./stop-servers.sh${NC}"
echo "" 