#!/bin/bash
# AimLab 一键部署脚本
# 用法: ./deploy.sh [all|backend|frontend|start|stop|restart|status]

set -e

# 加载配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/config.sh"

# ==================== 环境检查 ====================
check_java() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            log_info "Java 版本: $(java -version 2>&1 | head -n 1)"
            return 0
        fi
    fi
    log_error "需要 Java 17 或更高版本"
    return 1
}

check_node() {
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [ "$NODE_VERSION" -ge 16 ]; then
            log_info "Node.js 版本: $(node -v)"
            return 0
        fi
    fi
    log_error "需要 Node.js 16 或更高版本"
    return 1
}

check_maven() {
    if command -v mvn &> /dev/null; then
        log_info "Maven 版本: $(mvn -v | head -n 1)"
        return 0
    fi
    log_error "需要安装 Maven"
    return 1
}

check_nginx() {
    if command -v nginx &> /dev/null; then
        log_info "Nginx 版本: $(nginx -v 2>&1)"
        return 0
    fi
    log_warn "Nginx 未安装，跳过 Nginx 配置"
    return 1
}

check_env() {
    log_step "检查环境..."
    check_java
    check_node
    check_maven
}

# ==================== 构建函数 ====================
build_frontend() {
    log_step "构建前端..."
    cd "${FRONTEND_DIR}"
    
    # 安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    
    # 构建
    log_info "执行前端构建..."
    npm run build
    
    log_info "前端构建完成: ${FRONTEND_DIR}/dist"
}

build_backend() {
    log_step "构建后端..."
    cd "${BACKEND_DIR}"
    
    log_info "执行 Maven 打包..."
    mvn clean package -DskipTests -q
    
    log_info "后端构建完成: ${BACKEND_DIR}/target/${JAR_NAME}"
}

# ==================== 部署函数 ====================
deploy_frontend() {
    log_step "部署前端..."
    
    # 创建部署目录
    sudo mkdir -p "${FRONTEND_DEPLOY_DIR}"
    
    # 复制构建产物
    if [ -d "${FRONTEND_DIR}/dist" ]; then
        sudo rm -rf "${FRONTEND_DEPLOY_DIR}"/*
        sudo cp -r "${FRONTEND_DIR}/dist"/* "${FRONTEND_DEPLOY_DIR}/"
        log_info "前端已部署到: ${FRONTEND_DEPLOY_DIR}"
    else
        log_error "前端构建产物不存在，请先执行构建"
        return 1
    fi
}

deploy_backend() {
    log_step "部署后端..."
    
    # 创建部署目录
    sudo mkdir -p "${BACKEND_DEPLOY_DIR}"
    sudo mkdir -p "${LOG_DIR}"
    
    # 复制 JAR 包
    if [ -f "${BACKEND_DIR}/target/${JAR_NAME}" ]; then
        sudo cp "${BACKEND_DIR}/target/${JAR_NAME}" "${BACKEND_DEPLOY_DIR}/"
        log_info "后端已部署到: ${BACKEND_DEPLOY_DIR}/${JAR_NAME}"
    else
        log_error "JAR 包不存在，请先执行构建"
        return 1
    fi
}

deploy_nginx() {
    if check_nginx; then
        log_step "配置 Nginx..."
        sudo cp "${SCRIPT_DIR}/aimlab.conf" "${NGINX_SITE_CONF}"
        sudo nginx -t && sudo systemctl reload nginx
        log_info "Nginx 配置已更新"
    fi
}

deploy_service() {
    log_step "配置系统服务..."
    sudo cp "${SCRIPT_DIR}/aimlab.service" /etc/systemd/system/
    sudo systemctl daemon-reload
    log_info "系统服务已配置"
}

# ==================== 服务管理 ====================
start_service() {
    log_step "启动服务..."
    if systemctl is-active --quiet aimlab; then
        log_warn "服务已在运行"
    else
        sudo systemctl start aimlab
        sleep 3
        if systemctl is-active --quiet aimlab; then
            log_info "服务启动成功"
        else
            log_error "服务启动失败，查看日志: journalctl -u aimlab -f"
            return 1
        fi
    fi
}

stop_service() {
    log_step "停止服务..."
    if systemctl is-active --quiet aimlab; then
        sudo systemctl stop aimlab
        log_info "服务已停止"
    else
        log_warn "服务未在运行"
    fi
}

restart_service() {
    log_step "重启服务..."
    sudo systemctl restart aimlab
    sleep 3
    if systemctl is-active --quiet aimlab; then
        log_info "服务重启成功"
    else
        log_error "服务重启失败"
        return 1
    fi
}

status_service() {
    log_step "服务状态..."
    systemctl status aimlab --no-pager || true
}

# ==================== 一键部署 ====================
deploy_all() {
    log_step "========== 开始一键部署 =========="
    
    check_env
    
    build_frontend
    build_backend
    
    deploy_frontend
    deploy_backend
    deploy_service
    deploy_nginx
    
    restart_service
    
    log_step "========== 部署完成 =========="
    echo ""
    log_info "前端地址: http://localhost"
    log_info "后端地址: http://localhost:${BACKEND_PORT}"
    log_info "日志目录: ${LOG_DIR}"
    echo ""
    log_info "常用命令:"
    echo "  查看状态: sudo systemctl status aimlab"
    echo "  查看日志: tail -f ${LOG_DIR}/app.log"
    echo "  重启服务: sudo systemctl restart aimlab"
}

# ==================== 本地开发模式 ====================
dev_start() {
    log_step "启动本地开发环境..."
    
    # 启动后端
    log_info "启动后端服务..."
    cd "${BACKEND_DIR}"
    nohup mvn spring-boot:run > "${PROJECT_ROOT}/backend.log" 2>&1 &
    echo $! > "${PROJECT_ROOT}/backend.pid"
    
    # 启动前端
    log_info "启动前端开发服务器..."
    cd "${FRONTEND_DIR}"
    nohup npm run dev > "${PROJECT_ROOT}/frontend.log" 2>&1 &
    echo $! > "${PROJECT_ROOT}/frontend.pid"
    
    sleep 5
    log_info "开发环境已启动"
    log_info "前端: http://localhost:5173"
    log_info "后端: http://localhost:${BACKEND_PORT}"
}

dev_stop() {
    log_step "停止本地开发环境..."
    
    if [ -f "${PROJECT_ROOT}/backend.pid" ]; then
        kill $(cat "${PROJECT_ROOT}/backend.pid") 2>/dev/null || true
        rm "${PROJECT_ROOT}/backend.pid"
        log_info "后端已停止"
    fi
    
    if [ -f "${PROJECT_ROOT}/frontend.pid" ]; then
        kill $(cat "${PROJECT_ROOT}/frontend.pid") 2>/dev/null || true
        rm "${PROJECT_ROOT}/frontend.pid"
        log_info "前端已停止"
    fi
}

# ==================== 帮助信息 ====================
show_help() {
    echo "AimLab 部署脚本"
    echo ""
    echo "用法: $0 <命令>"
    echo ""
    echo "命令:"
    echo "  all       一键部署（构建+部署+启动）"
    echo "  build     仅构建前后端"
    echo "  frontend  仅构建和部署前端"
    echo "  backend   仅构建和部署后端"
    echo "  start     启动服务"
    echo "  stop      停止服务"
    echo "  restart   重启服务"
    echo "  status    查看服务状态"
    echo "  dev       启动本地开发环境"
    echo "  dev-stop  停止本地开发环境"
    echo "  help      显示帮助信息"
    echo ""
}

# ==================== 主入口 ====================
case "${1:-all}" in
    all)
        deploy_all
        ;;
    build)
        check_env
        build_frontend
        build_backend
        ;;
    frontend)
        check_node
        build_frontend
        deploy_frontend
        deploy_nginx
        ;;
    backend)
        check_java
        check_maven
        build_backend
        deploy_backend
        restart_service
        ;;
    start)
        start_service
        ;;
    stop)
        stop_service
        ;;
    restart)
        restart_service
        ;;
    status)
        status_service
        ;;
    dev)
        dev_start
        ;;
    dev-stop)
        dev_stop
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "未知命令: $1"
        show_help
        exit 1
        ;;
esac
