#!/bin/bash

# 显示脚本开始执行的信息
echo "开始执行重启脚本..."

# 查找占用8083端口的进程PID
echo "正在查找占用8083端口的进程..."
PID=$(lsof -ti:8083)

# 如果找到进程则杀掉
if [ -n "$PID" ]; then
    echo "发现进程占用8083端口，PID: $PID，正在终止..."
    kill $PID
    sleep 2
    
    # 再次检查，如果进程仍然存在，使用kill -9强制终止
    if ps -p $PID > /dev/null; then
        echo "进程仍然存在，使用强制终止..."
        kill -9 $PID
        sleep 1
    fi
    echo "端口8083已释放"
else
    echo "没有进程占用8083端口"
fi

# 当前目录
CURR_DIR=$(pwd)
echo "当前工作目录: $CURR_DIR"

# 使用Maven启动应用
echo "正在启动SpringBoot应用..."
# 如果当前目录下有mvnw，优先使用它
if [ -f "./mvnw" ]; then
    ./mvnw spring-boot:run
# 否则使用系统安装的Maven
elif command -v mvn &> /dev/null; then
    mvn -Dmaven.test.skip=true spring-boot:run
else
    echo "错误: 找不到Maven (mvn) 或 Maven Wrapper (mvnw)，无法启动应用"
    exit 1
fi
