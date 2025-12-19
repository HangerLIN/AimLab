#!/bin/bash
# AimLab 部署配置文件

# ==================== 应用配置 ====================
APP_NAME="aimlab"
APP_VERSION="0.0.1-SNAPSHOT"

# ==================== 路径配置 ====================
# 项目根目录（自动检测）
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# 前端目录
FRONTEND_DIR="${PROJECT_ROOT}/shooting-frontend"
# 后端目录
BACKEND_DIR="${PROJECT_ROOT}"

# 部署目录（生产环境）
DEPLOY_DIR="/opt/aimlab"
FRONTEND_DEPLOY_DIR="${DEPLOY_DIR}/frontend"
BACKEND_DEPLOY_DIR="${DEPLOY_DIR}/backend"
LOG_DIR="${DEPLOY_DIR}/logs"

# ==================== 服务端口 ====================
BACKEND_PORT=8083
FRONTEND_PORT=80

# ==================== 数据库配置 ====================
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="shooting_system_0"
DB_USER="root"
DB_PASSWORD=""

# ==================== JVM 配置 ====================
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# ==================== JAR 包名称 ====================
JAR_NAME="shooting-training-competition-system-${APP_VERSION}.jar"

# ==================== Nginx 配置 ====================
NGINX_CONF_DIR="/etc/nginx/conf.d"
NGINX_SITE_CONF="${NGINX_CONF_DIR}/${APP_NAME}.conf"

# ==================== 颜色输出 ====================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ==================== 工具函数 ====================
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}
