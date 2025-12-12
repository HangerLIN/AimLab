#!/bin/bash

# MCP 调试命令快速参考
# 使用方法: source debug-commands.sh 或 bash debug-commands.sh

set -e

PROJECT_ROOT="/Users/hangerlin/AimLab"
BACKEND_LOG="$PROJECT_ROOT/backend.log"
FRONTEND_LOG="$PROJECT_ROOT/shooting-frontend/frontend.log"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# ============================================
# 1. 状态检查
# ============================================

check_status() {
    print_info "检查服务状态..."
    echo ""
    
    # 检查后端
    if curl -s http://localhost:8083/api/athletes > /dev/null 2>&1; then
        print_success "后端服务运行正常 (http://localhost:8083)"
    else
        print_error "后端服务未响应"
    fi
    
    # 检查前端
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        print_success "前端服务运行正常 (http://localhost:5173)"
    else
        print_error "前端服务未响应"
    fi
    
    echo ""
    print_info "进程信息："
    ps aux | grep -E "java|vite|node" | grep -v grep || print_warning "未找到运行的进程"
}

# ============================================
# 2. 日志查看
# ============================================

tail_backend() {
    print_info "显示后端日志（最后 50 行）..."
    tail -50 "$BACKEND_LOG"
}

tail_frontend() {
    print_info "显示前端日志（最后 50 行）..."
    tail -50 "$FRONTEND_LOG"
}

follow_backend() {
    print_info "实时监控后端日志（按 Ctrl+C 停止）..."
    tail -f "$BACKEND_LOG"
}

follow_frontend() {
    print_info "实时监控前端日志（按 Ctrl+C 停止）..."
    tail -f "$FRONTEND_LOG"
}

# ============================================
# 3. API 测试
# ============================================

test_api() {
    print_info "测试后端 API..."
    echo ""
    
    print_info "测试: GET /api/athletes"
    curl -s http://localhost:8083/api/athletes | head -20
    echo ""
    
    print_info "测试: GET /api/competitions"
    curl -s http://localhost:8083/api/competitions | head -20
    echo ""
    
    print_info "测试: GET /api/training-sessions"
    curl -s http://localhost:8083/api/training-sessions | head -20
}

test_websocket() {
    print_info "测试 WebSocket 连接..."
    
    if command -v websocat &> /dev/null; then
        print_info "使用 websocat 连接 ws://localhost:8083/ws"
        timeout 5 websocat ws://localhost:8083/ws || true
    else
        print_warning "websocat 未安装，请使用浏览器开发者工具测试 WebSocket"
        print_info "在浏览器 Console 中运行："
        echo "const ws = new WebSocket('ws://localhost:8083/ws');"
        echo "ws.onopen = () => console.log('Connected');"
        echo "ws.onerror = (e) => console.error('Error:', e);"
    fi
}

# ============================================
# 4. 服务管理
# ============================================

stop_all() {
    print_warning "停止所有服务..."
    pkill -f "spring-boot:run" || print_warning "后端进程未找到"
    pkill -f "vite" || print_warning "前端进程未找到"
    sleep 1
    print_success "所有服务已停止"
}

start_backend() {
    print_info "启动后端服务..."
    cd "$PROJECT_ROOT"
    mvn clean spring-boot:run -DskipTests 2>&1 | tee backend.log &
    print_success "后端启动命令已发送（后台运行）"
    print_info "等待 10 秒让服务启动..."
    sleep 10
    check_status
}

start_frontend() {
    print_info "启动前端服务..."
    cd "$PROJECT_ROOT/shooting-frontend"
    npm install > /dev/null 2>&1
    npm run dev 2>&1 | tee frontend.log &
    print_success "前端启动命令已发送（后台运行）"
    print_info "等待 5 秒让服务启动..."
    sleep 5
    check_status
}

start_all() {
    print_info "启动所有服务..."
    start_backend &
    start_frontend &
    wait
}

restart_all() {
    print_warning "重启所有服务..."
    stop_all
    sleep 2
    start_all
}

# ============================================
# 5. 清理和诊断
# ============================================

clean_logs() {
    print_warning "清理日志文件..."
    > "$BACKEND_LOG"
    > "$PROJECT_ROOT/shooting-frontend/frontend.log"
    print_success "日志已清理"
}

diagnose() {
    print_info "运行诊断..."
    echo ""
    
    print_info "Java 版本："
    java -version 2>&1 | head -3
    echo ""
    
    print_info "Node.js 版本："
    node -v
    npm -v
    echo ""
    
    print_info "Maven 版本："
    mvn -version | head -3
    echo ""
    
    print_info "端口占用情况："
    lsof -i :8083 2>/dev/null || print_warning "端口 8083 未被占用"
    lsof -i :5173 2>/dev/null || print_warning "端口 5173 未被占用"
    echo ""
    
    check_status
}

# ============================================
# 6. 帮助菜单
# ============================================

show_help() {
    cat << EOF
${BLUE}MCP 调试命令快速参考${NC}

${GREEN}状态检查:${NC}
  check_status          - 检查所有服务状态
  diagnose              - 运行完整诊断

${GREEN}日志查看:${NC}
  tail_backend          - 显示后端日志（最后 50 行）
  tail_frontend         - 显示前端日志（最后 50 行）
  follow_backend        - 实时监控后端日志
  follow_frontend       - 实时监控前端日志

${GREEN}API 测试:${NC}
  test_api              - 测试后端 API 端点
  test_websocket        - 测试 WebSocket 连接

${GREEN}服务管理:${NC}
  start_backend         - 启动后端服务
  start_frontend        - 启动前端服务
  start_all             - 启动所有服务
  stop_all              - 停止所有服务
  restart_all           - 重启所有服务

${GREEN}维护:${NC}
  clean_logs            - 清理日志文件

${GREEN}其他:${NC}
  show_help             - 显示此帮助信息

${YELLOW}使用示例:${NC}
  source debug-commands.sh
  check_status
  follow_backend
  test_api

EOF
}

# ============================================
# 主菜单
# ============================================

if [ $# -eq 0 ]; then
    show_help
else
    case "$1" in
        check_status|status)
            check_status
            ;;
        tail_backend|tb)
            tail_backend
            ;;
        tail_frontend|tf)
            tail_frontend
            ;;
        follow_backend|fb)
            follow_backend
            ;;
        follow_frontend|ff)
            follow_frontend
            ;;
        test_api|api)
            test_api
            ;;
        test_websocket|ws)
            test_websocket
            ;;
        start_backend|sb)
            start_backend
            ;;
        start_frontend|sf)
            start_frontend
            ;;
        start_all|sa)
            start_all
            ;;
        stop_all|stop)
            stop_all
            ;;
        restart_all|restart|ra)
            restart_all
            ;;
        clean_logs|clean)
            clean_logs
            ;;
        diagnose|diag)
            diagnose
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
fi
