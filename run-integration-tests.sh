#!/bin/bash

# 集成测试执行脚本
# 用于运行管理员、训练分析和比赛服务的集成测试

echo "=========================================="
echo "  AimLab 集成测试执行脚本"
echo "=========================================="
echo ""

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到 Maven，请先安装 Maven${NC}"
    exit 1
fi

# 检查 MySQL 是否运行
echo -e "${YELLOW}检查 MySQL 服务...${NC}"
if ! pgrep -x "mysqld" > /dev/null; then
    echo -e "${RED}警告: MySQL 服务未运行，测试可能失败${NC}"
    echo "请先启动 MySQL 服务"
    read -p "是否继续? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 创建测试报告目录
REPORT_DIR="test-reports"
mkdir -p $REPORT_DIR

echo ""
echo -e "${GREEN}开始运行集成测试...${NC}"
echo ""

# 运行所有集成测试
echo "=========================================="
echo "运行全部集成测试"
echo "=========================================="
mvn test -Dtest=*IntegrationTest 2>&1 | tee $REPORT_DIR/all-tests.log

TEST_RESULT=${PIPESTATUS[0]}

echo ""
echo "=========================================="

if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
else
    echo -e "${RED}✗ 部分测试失败，请查看日志${NC}"
fi

echo "=========================================="
echo ""

# 提取测试结果摘要
echo "测试结果摘要："
echo "----------------------------------------"
grep -E "Tests run:|BUILD SUCCESS|BUILD FAILURE" $REPORT_DIR/all-tests.log | tail -5

echo ""
echo "详细测试日志已保存到: $REPORT_DIR/all-tests.log"
echo ""

# 可选：只运行特定的测试类
echo "=========================================="
echo "可用的单独测试命令："
echo "----------------------------------------"
echo "1. 管理员控制器测试:"
echo "   mvn test -Dtest=AdminControllerIntegrationTest"
echo ""
echo "2. 训练分析服务测试:"
echo "   mvn test -Dtest=TrainingAnalyticsServiceIntegrationTest"
echo ""
echo "3. 比赛服务测试:"
echo "   mvn test -Dtest=CompetitionServiceIntegrationTest"
echo "=========================================="

exit $TEST_RESULT


