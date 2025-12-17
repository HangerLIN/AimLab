<template>
  <div class="admin-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>数据统计</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>数据统计</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          @change="handleDateChange"
        />
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-section">
      <h2 class="section-title">核心指标</h2>
      <div class="metrics-cards">
        <div class="metric-card" v-for="metric in coreMetrics" :key="metric.key">
          <div class="metric-header">
            <span class="metric-label">{{ metric.label }}</span>
            <el-tag 
              v-if="metric.trend !== 0" 
              :type="metric.trend > 0 ? 'success' : 'danger'" 
              size="small"
            >
              {{ metric.trend > 0 ? '+' : '' }}{{ metric.trend }}%
            </el-tag>
          </div>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-compare">
            较上期 {{ metric.compareText }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 训练趋势图 -->
        <div class="chart-card large">
          <div class="chart-header">
            <h3>训练趋势</h3>
            <el-radio-group v-model="trainingChartType" size="small">
              <el-radio-button value="day">日</el-radio-button>
              <el-radio-button value="week">周</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-body" ref="trainingChartRef" v-loading="chartsLoading.training"></div>
        </div>

        <!-- 运动员分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>运动员级别分布</h3>
          </div>
          <div class="chart-body" ref="athleteLevelChartRef" v-loading="chartsLoading.athleteLevel"></div>
        </div>
      </div>

      <div class="chart-row">
        <!-- 成绩分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>成绩分布</h3>
          </div>
          <div class="chart-body" ref="scoreDistChartRef" v-loading="chartsLoading.scoreDist"></div>
        </div>

        <!-- 比赛统计 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>比赛统计</h3>
          </div>
          <div class="chart-body" ref="competitionChartRef" v-loading="chartsLoading.competition"></div>
        </div>

        <!-- 项目训练量 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>项目训练量</h3>
          </div>
          <div class="chart-body" ref="projectChartRef" v-loading="chartsLoading.project"></div>
        </div>
      </div>
    </div>

    <!-- 数据表格区域 -->
    <div class="tables-section">
      <div class="table-row">
        <!-- 运动员排行榜 -->
        <div class="table-card">
          <div class="table-header">
            <h3>运动员训练排行</h3>
            <el-link type="primary" @click="goToAthleteAnalytics">查看更多</el-link>
          </div>
          <el-table :data="athleteRanking" v-loading="tablesLoading.athleteRanking" stripe>
            <el-table-column type="index" label="排名" width="60">
              <template #default="{ $index }">
                <span :class="['rank-badge', `rank-${$index + 1}`]">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="姓名" min-width="100" />
            <el-table-column prop="level" label="级别" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="getLevelTagType(row.level)">
                  {{ row.level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="trainingCount" label="训练次数" width="100" />
            <el-table-column prop="avgScore" label="平均成绩" width="100">
              <template #default="{ row }">
                <span class="score-value">{{ row.avgScore?.toFixed(1) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 最近比赛 -->
        <div class="table-card">
          <div class="table-header">
            <h3>最近比赛</h3>
            <el-link type="primary" @click="goToCompetitions">查看更多</el-link>
          </div>
          <el-table :data="recentCompetitions" v-loading="tablesLoading.recentCompetitions" stripe>
            <el-table-column prop="name" label="比赛名称" min-width="150" />
            <el-table-column prop="project" label="项目" width="100" />
            <el-table-column prop="participantCount" label="参赛人数" width="90" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="日期" width="110">
              <template #default="{ row }">
                {{ formatDate(row.date) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 系统状态 -->
    <div class="system-section">
      <h2 class="section-title">系统状态</h2>
      <div class="system-cards">
        <div class="system-card">
          <div class="system-icon online">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="system-info">
            <div class="system-label">在线用户</div>
            <div class="system-value">{{ systemStatus.onlineUsers }}</div>
          </div>
        </div>
        <div class="system-card">
          <div class="system-icon storage">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="system-info">
            <div class="system-label">存储使用</div>
            <div class="system-value">{{ systemStatus.storageUsed }}</div>
            <el-progress 
              :percentage="systemStatus.storagePercent" 
              :stroke-width="6"
              :show-text="false"
            />
          </div>
        </div>
        <div class="system-card">
          <div class="system-icon api">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="system-info">
            <div class="system-label">API调用</div>
            <div class="system-value">{{ systemStatus.apiCalls }}/天</div>
          </div>
        </div>
        <div class="system-card">
          <div class="system-icon uptime">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="system-info">
            <div class="system-label">运行时间</div>
            <div class="system-value">{{ systemStatus.uptime }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Refresh, Download, Monitor, Coin, Connection, Timer
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import {
  getDashboardStats,
  getTrainingAnalyticsByTime,
  getTrainingAnalyticsByAthlete,
  getTrainingAnalyticsByProject,
  exportTrainingReport
} from '@/api/admin';

const router = useRouter();

// 日期范围
const dateRange = ref([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  new Date()
]);

const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 7 * 24 * 60 * 60 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近一月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 30 * 24 * 60 * 60 * 1000);
      return [start, end];
    }
  },
  {
    text: '最近三月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 90 * 24 * 60 * 60 * 1000);
      return [start, end];
    }
  }
];

// 核心指标
const coreMetrics = ref([
  { key: 'totalAthletes', label: '运动员总数', value: 0, trend: 0, compareText: '持平' },
  { key: 'totalTrainings', label: '训练总次数', value: 0, trend: 0, compareText: '持平' },
  { key: 'totalCompetitions', label: '比赛总场次', value: 0, trend: 0, compareText: '持平' },
  { key: 'avgScore', label: '平均成绩', value: '0.0', trend: 0, compareText: '持平' }
]);

// 图表类型
const trainingChartType = ref('day');

// 图表引用
const trainingChartRef = ref(null);
const athleteLevelChartRef = ref(null);
const scoreDistChartRef = ref(null);
const competitionChartRef = ref(null);
const projectChartRef = ref(null);

// 图表实例
let trainingChart = null;
let athleteLevelChart = null;
let scoreDistChart = null;
let competitionChart = null;
let projectChart = null;

// 加载状态
const chartsLoading = reactive({
  training: false,
  athleteLevel: false,
  scoreDist: false,
  competition: false,
  project: false
});

const tablesLoading = reactive({
  athleteRanking: false,
  recentCompetitions: false
});

// 表格数据
const athleteRanking = ref([]);
const recentCompetitions = ref([]);

// 系统状态
const systemStatus = reactive({
  onlineUsers: 0,
  storageUsed: '0 GB',
  storagePercent: 0,
  apiCalls: 0,
  uptime: '0天'
});

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

// 获取级别标签类型
const getLevelTagType = (level) => {
  const types = {
    '国家级': 'danger',
    '省级': 'warning',
    '市级': 'primary',
    '业余': 'info'
  };
  return types[level] || 'info';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const types = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  };
  return types[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return texts[status] || status;
};

// 初始化训练趋势图
const initTrainingChart = () => {
  if (!trainingChartRef.value) return;
  
  trainingChart = echarts.init(trainingChartRef.value);
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['训练次数', '平均成绩'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: [
      {
        type: 'value',
        name: '训练次数',
        position: 'left'
      },
      {
        type: 'value',
        name: '平均成绩',
        position: 'right',
        min: 0,
        max: 10.9
      }
    ],
    series: [
      {
        name: '训练次数',
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      },
      {
        name: '平均成绩',
        type: 'line',
        yAxisIndex: 1,
        data: [],
        smooth: true,
        itemStyle: { color: '#f5576c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 87, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 87, 108, 0.05)' }
          ])
        }
      }
    ]
  };
  trainingChart.setOption(option);
};

// 初始化运动员级别分布图
const initAthleteLevelChart = () => {
  if (!athleteLevelChartRef.value) return;
  
  athleteLevelChart = echarts.init(athleteLevelChartRef.value);
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: { show: false },
        data: []
      }
    ],
    color: ['#f56c6c', '#e6a23c', '#409eff', '#909399']
  };
  athleteLevelChart.setOption(option);
};

// 初始化成绩分布图
const initScoreDistChart = () => {
  if (!scoreDistChartRef.value) return;
  
  scoreDistChart = echarts.init(scoreDistChartRef.value);
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['<8.0', '8.0-8.5', '8.5-9.0', '9.0-9.5', '9.5-10.0', '10.0+']
    },
    yAxis: {
      type: 'value',
      name: '人次'
    },
    series: [
      {
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#11998e' },
            { offset: 1, color: '#38ef7d' }
          ])
        },
        barWidth: '60%'
      }
    ]
  };
  scoreDistChart.setOption(option);
};

// 初始化比赛统计图
const initCompetitionChart = () => {
  if (!competitionChartRef.value) return;
  
  competitionChart = echarts.init(competitionChartRef.value);
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['已完成', '进行中', '待开始'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        data: [],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '进行中',
        type: 'bar',
        stack: 'total',
        data: [],
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '待开始',
        type: 'bar',
        stack: 'total',
        data: [],
        itemStyle: { color: '#909399' }
      }
    ]
  };
  competitionChart.setOption(option);
};

// 初始化项目训练量图
const initProjectChart = () => {
  if (!projectChartRef.value) return;
  
  projectChart = echarts.init(projectChartRef.value);
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: []
    },
    series: [
      {
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      }
    ]
  };
  projectChart.setOption(option);
};

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    const res = await getDashboardStats();
    if (res.success && res.data) {
      const data = res.data;
      
      // 更新核心指标
      coreMetrics.value[0].value = data.totalAthletes || 0;
      coreMetrics.value[1].value = data.totalTrainings || 0;
      coreMetrics.value[2].value = data.totalCompetitions || 0;
      coreMetrics.value[3].value = (data.avgScore || 0).toFixed(1);
      
      // 更新系统状态
      systemStatus.onlineUsers = data.onlineUsers || 0;
      systemStatus.storageUsed = data.storageUsed || '0 GB';
      systemStatus.storagePercent = data.storagePercent || 0;
      systemStatus.apiCalls = data.apiCalls || 0;
      systemStatus.uptime = data.uptime || '0天';
      
      // 更新运动员级别分布
      if (athleteLevelChart && data.athleteLevelDistribution) {
        athleteLevelChart.setOption({
          series: [{
            data: Object.entries(data.athleteLevelDistribution).map(([name, value]) => ({
              name,
              value
            }))
          }]
        });
      }
      
      // 更新运动员排行
      athleteRanking.value = data.athleteRanking || [];
      
      // 更新最近比赛
      recentCompetitions.value = data.recentCompetitions || [];
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error);
  }
};

// 加载训练趋势数据
const loadTrainingTrend = async () => {
  chartsLoading.training = true;
  try {
    const [startDate, endDate] = dateRange.value || [];
    const res = await getTrainingAnalyticsByTime({
      startDate: startDate?.toISOString().split('T')[0],
      endDate: endDate?.toISOString().split('T')[0],
      groupBy: trainingChartType.value
    });
    
    if (res.success && res.data && trainingChart) {
      const data = res.data;
      trainingChart.setOption({
        xAxis: { data: data.dates || [] },
        series: [
          { data: data.trainingCounts || [] },
          { data: data.avgScores || [] }
        ]
      });
    }
  } catch (error) {
    console.error('加载训练趋势失败:', error);
  } finally {
    chartsLoading.training = false;
  }
};

// 加载项目统计
const loadProjectStats = async () => {
  chartsLoading.project = true;
  try {
    const res = await getTrainingAnalyticsByProject({});
    if (res.success && res.data && projectChart) {
      const data = res.data;
      projectChart.setOption({
        yAxis: { data: data.projects || [] },
        series: [{ data: data.counts || [] }]
      });
    }
  } catch (error) {
    console.error('加载项目统计失败:', error);
  } finally {
    chartsLoading.project = false;
  }
};

// 日期变化处理
const handleDateChange = () => {
  loadTrainingTrend();
};

// 刷新数据
const refreshData = () => {
  loadDashboardData();
  loadTrainingTrend();
  loadProjectStats();
};

// 导出报表
const exportReport = async () => {
  try {
    const [startDate, endDate] = dateRange.value || [];
    const res = await exportTrainingReport({
      startDate: startDate?.toISOString().split('T')[0],
      endDate: endDate?.toISOString().split('T')[0],
      format: 'excel'
    });
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `训练报表_${new Date().toISOString().split('T')[0]}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  }
};

// 跳转到运动员分析
const goToAthleteAnalytics = () => {
  router.push('/analytics');
};

// 跳转到比赛列表
const goToCompetitions = () => {
  router.push('/training');
};

// 监听图表类型变化
watch(trainingChartType, () => {
  loadTrainingTrend();
});

// 窗口大小变化时重绘图表
const handleResize = () => {
  trainingChart?.resize();
  athleteLevelChart?.resize();
  scoreDistChart?.resize();
  competitionChart?.resize();
  projectChart?.resize();
};

onMounted(() => {
  // 初始化图表
  initTrainingChart();
  initAthleteLevelChart();
  initScoreDistChart();
  initCompetitionChart();
  initProjectChart();
  
  // 加载数据
  loadDashboardData();
  loadTrainingTrend();
  loadProjectStats();
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  // 销毁图表
  trainingChart?.dispose();
  athleteLevelChart?.dispose();
  scoreDistChart?.dispose();
  competitionChart?.dispose();
  projectChart?.dispose();
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

/* 核心指标卡片 */
.metrics-section {
  margin-bottom: 24px;
}

.metrics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-label {
  font-size: 14px;
  color: #909399;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.metric-compare {
  font-size: 12px;
  color: #909399;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 24px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-card.large {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-body {
  height: 280px;
}

/* 表格区域 */
.tables-section {
  margin-bottom: 24px;
}

.table-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  background: #f5f7fa;
  color: #909399;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
  color: #fff;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
  color: #fff;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: #fff;
}

.score-value {
  font-weight: 600;
  color: #409eff;
}

/* 系统状态 */
.system-section {
  margin-bottom: 24px;
}

.system-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.system-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.system-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.system-icon.online {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.system-icon.storage {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.system-icon.api {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.system-icon.uptime {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.system-info {
  flex: 1;
}

.system-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.system-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .chart-card.large {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
  }
  
  .header-right {
    width: 100%;
    flex-direction: column;
  }
  
  .metrics-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-row {
    grid-template-columns: 1fr;
  }
  
  .table-row {
    grid-template-columns: 1fr;
  }
  
  .system-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .metrics-cards {
    grid-template-columns: 1fr;
  }
  
  .system-cards {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 24px;
  }
}
</style>
