<template>
  <div class="statistics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <div class="chart-glow"></div>
          <span class="chart-icon">📊</span>
        </div>
        <div class="header-text">
          <h1>统计报表</h1>
          <p>数据驱动决策，洞察训练趋势</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="export-btn" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 报表类型选择 -->
    <div class="report-type-section">
      <div class="type-cards">
        <div 
          v-for="type in reportTypes" 
          :key="type.value"
          :class="['type-card', { active: activeReportType === type.value }]"
          @click="switchReportType(type.value)"
        >
          <div class="type-icon">{{ type.icon }}</div>
          <div class="type-info">
            <h3>{{ type.label }}</h3>
            <p>{{ type.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <!-- 数据类型 -->
          <div class="filter-group">
            <label>数据类型</label>
            <div class="segment-control">
              <button 
                v-for="opt in dataTypeOptions" 
                :key="opt.value"
                :class="{ active: currentDataType === opt.value }"
                @click="currentDataType = opt.value; loadCurrentReport()"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <!-- 时间选择 -->
          <div class="filter-group" v-if="activeReportType === 'monthly'">
            <label>选择月份</label>
            <el-date-picker 
              v-model="monthlyDate" 
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              @change="loadMonthlyReport"
              class="dark-picker"
            />
          </div>

          <div class="filter-group" v-if="activeReportType === 'quarterly'">
            <label>选择季度</label>
            <div class="quarter-select">
              <el-input-number v-model="quarterlyYear" :min="2020" :max="2030" controls-position="right" />
              <el-select v-model="quarterlyQuarter" placeholder="季度" @change="loadQuarterlyReport">
                <el-option label="Q1 (1-3月)" :value="1" />
                <el-option label="Q2 (4-6月)" :value="2" />
                <el-option label="Q3 (7-9月)" :value="3" />
                <el-option label="Q4 (10-12月)" :value="4" />
              </el-select>
            </div>
          </div>

          <div class="filter-group" v-if="activeReportType === 'yearly'">
            <label>选择年份</label>
            <el-input-number v-model="yearlyYear" :min="2020" :max="2030" controls-position="right" @change="loadYearlyReport" />
          </div>

          <div class="filter-group" v-if="activeReportType === 'custom'">
            <label>自定义范围</label>
            <el-date-picker 
              v-model="customDateRange" 
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="loadCustomReport"
              class="dark-picker"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-section">
      <div class="metrics-grid">
        <div class="metric-card participants">
          <div class="metric-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number" ref="participantsRef">{{ animatedMetrics.participants }}</span>
            </div>
            <div class="metric-label">参与人数</div>
          </div>
          <div class="metric-trend up">
            <el-icon><TrendCharts /></el-icon>
            <span>+12%</span>
          </div>
          <div class="metric-bg"></div>
        </div>

        <div class="metric-card participations">
          <div class="metric-icon">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number">{{ animatedMetrics.participations }}</span>
            </div>
            <div class="metric-label">参与次数</div>
          </div>
          <div class="metric-trend up">
            <el-icon><TrendCharts /></el-icon>
            <span>+8%</span>
          </div>
          <div class="metric-bg"></div>
        </div>

        <div class="metric-card average">
          <div class="metric-icon">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number">{{ animatedMetrics.averageScore }}</span>
            </div>
            <div class="metric-label">平均分</div>
          </div>
          <div class="metric-trend up">
            <el-icon><TrendCharts /></el-icon>
            <span>+5%</span>
          </div>
          <div class="metric-bg"></div>
        </div>

        <div class="metric-card highest">
          <div class="metric-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number">{{ animatedMetrics.maxScore }}</span>
            </div>
            <div class="metric-label">最高分</div>
          </div>
          <div class="metric-bg"></div>
        </div>

        <div class="metric-card lowest">
          <div class="metric-icon">
            <el-icon><Bottom /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number">{{ animatedMetrics.minScore }}</span>
            </div>
            <div class="metric-label">最低分</div>
          </div>
          <div class="metric-bg"></div>
        </div>

        <div class="metric-card total">
          <div class="metric-icon">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">
              <span class="number">{{ animatedMetrics.totalScore }}</span>
            </div>
            <div class="metric-label">总分</div>
          </div>
          <div class="metric-bg"></div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 成绩趋势图 -->
        <div class="chart-card large">
          <div class="chart-header">
            <h3>成绩趋势</h3>
            <div class="chart-legend">
              <span class="legend-item"><span class="dot avg"></span>平均分</span>
              <span class="legend-item"><span class="dot max"></span>最高分</span>
            </div>
          </div>
          <div class="chart-body">
            <div ref="trendChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 成绩分布图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>成绩分布</h3>
          </div>
          <div class="chart-body">
            <div ref="distributionChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 等级占比图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>等级占比</h3>
          </div>
          <div class="chart-body">
            <div ref="levelChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 参与度分析 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>参与度分析</h3>
          </div>
          <div class="chart-body">
            <div ref="participationChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 性别分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>性别分布</h3>
          </div>
          <div class="chart-body">
            <div ref="genderChartRef" class="chart-container"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>详细数据</h3>
          <div class="table-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索运动员..."
              prefix-icon="Search"
              class="search-input"
            />
          </div>
        </div>
        <div class="table-body">
          <el-table 
            :data="filteredTableData" 
            stripe 
            style="width: 100%"
            :header-cell-style="{ background: '#f8fafc', color: '#334155' }"
          >
            <el-table-column prop="rank" label="排名" width="80" align="center">
              <template #default="{ row }">
                <div :class="['rank-cell', getRankClass(row.rank)]">
                  {{ row.rank }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="athleteName" label="运动员" min-width="120">
              <template #default="{ row }">
                <div class="athlete-cell">
                  <div class="athlete-avatar" :style="{ background: getAvatarColor(row.rank) }">
                    {{ row.athleteName?.charAt(0) }}
                  </div>
                  <span>{{ row.athleteName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="athleteLevel" label="等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelTagType(row.athleteLevel)" size="small">
                  {{ row.athleteLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="averageScore" label="平均分" width="100" align="center">
              <template #default="{ row }">
                <span class="score-cell">{{ row.averageScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maxScore" label="最高分" width="100" align="center" />
            <el-table-column prop="participationCount" label="参赛次数" width="100" align="center" />
            <el-table-column prop="totalScore" label="总分" width="100" align="center" />
            <el-table-column label="表现" width="150">
              <template #default="{ row }">
                <div class="performance-bar">
                  <div class="bar-fill" :style="{ width: getPerformanceWidth(row.averageScore) }"></div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 加载遮罩 -->
    <transition name="fade">
      <div class="loading-overlay" v-if="loading">
        <div class="loader">
          <div class="loader-ring"></div>
          <span>加载中...</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { statisticsAPI, rankingAPI } from '@/api/ranking'
import { ElMessage } from 'element-plus'
import { User, Tickets, DataLine, Trophy, Bottom, Coin, TrendCharts, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 状态
const loading = ref(false)
const activeReportType = ref('monthly')
const currentDataType = ref('TRAINING')
const searchKeyword = ref('')

// 时间选择
const monthlyDate = ref(new Date())
const quarterlyYear = ref(new Date().getFullYear())
const quarterlyQuarter = ref(Math.ceil((new Date().getMonth() + 1) / 3))
const yearlyYear = ref(new Date().getFullYear())
const customDateRange = ref([
  new Date(new Date().getFullYear(), new Date().getMonth(), 1),
  new Date()
])

// 报表数据
const currentReport = ref({})
const rankingData = ref([])

// 动画数值
const animatedMetrics = ref({
  participants: 0,
  participations: 0,
  averageScore: 0,
  maxScore: 0,
  minScore: 0,
  totalScore: 0
})

// 图表引用
const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const levelChartRef = ref(null)
const participationChartRef = ref(null)
const genderChartRef = ref(null)

let trendChart = null
let distributionChart = null
let levelChart = null
let participationChart = null
let genderChart = null

// 配置
const reportTypes = [
  { value: 'monthly', label: '月度报表', desc: '按月统计数据', icon: '📅' },
  { value: 'quarterly', label: '季度报表', desc: '按季度统计数据', icon: '📊' },
  { value: 'yearly', label: '年度报表', desc: '按年统计数据', icon: '📈' },
  { value: 'custom', label: '自定义', desc: '自定义时间范围', icon: '⚙️' }
]

const dataTypeOptions = [
  { value: 'TRAINING', label: '训练' },
  { value: 'COMPETITION', label: '比赛' },
  { value: 'OVERALL', label: '综合' }
]

// 计算属性
const filteredTableData = computed(() => {
  if (!searchKeyword.value) return rankingData.value
  return rankingData.value.filter(item => 
    item.athleteName?.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 方法
const switchReportType = (type) => {
  activeReportType.value = type
  loadCurrentReport()
}

const loadCurrentReport = () => {
  switch (activeReportType.value) {
    case 'monthly': loadMonthlyReport(); break
    case 'quarterly': loadQuarterlyReport(); break
    case 'yearly': loadYearlyReport(); break
    case 'custom': loadCustomReport(); break
  }
}

const loadMonthlyReport = async () => {
  if (!monthlyDate.value) return
  loading.value = true
  try {
    const date = new Date(monthlyDate.value)
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    
    let response
    if (currentDataType.value === 'TRAINING') {
      response = await statisticsAPI.getMonthlyTrainingStatistics(year, month)
    } else if (currentDataType.value === 'COMPETITION') {
      response = await statisticsAPI.getMonthlyCompetitionStatistics(year, month)
    } else {
      response = await statisticsAPI.getMonthlyTrainingStatistics(year, month)
    }
    currentReport.value = response.data || response
    
    // 加载排名数据
    const rankingResponse = await rankingAPI.getMonthlyRanking(year, month, 50)
    rankingData.value = rankingResponse.data || rankingResponse
    
    animateMetrics()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载月度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadQuarterlyReport = async () => {
  loading.value = true
  try {
    const response = await statisticsAPI.getQuarterlyStatistics(
      quarterlyYear.value, 
      quarterlyQuarter.value, 
      currentDataType.value
    )
    currentReport.value = response.data || response
    
    // 加载排名数据
    const rankingResponse = await rankingAPI.getOverallRanking(50)
    rankingData.value = rankingResponse.data || rankingResponse
    
    animateMetrics()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载季度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadYearlyReport = async () => {
  loading.value = true
  try {
    const response = await statisticsAPI.getYearlyStatistics(yearlyYear.value, currentDataType.value)
    currentReport.value = response.data || response
    
    const rankingResponse = await rankingAPI.getOverallRanking(50)
    rankingData.value = rankingResponse.data || rankingResponse
    
    animateMetrics()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载年度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadCustomReport = async () => {
  if (!customDateRange.value || customDateRange.value.length !== 2) return
  loading.value = true
  try {
    const startDate = customDateRange.value[0].toISOString()
    const endDate = customDateRange.value[1].toISOString()
    const response = await statisticsAPI.getCustomStatistics(startDate, endDate, currentDataType.value)
    currentReport.value = response.data || response
    
    const rankingResponse = await rankingAPI.getOverallRanking(50)
    rankingData.value = rankingResponse.data || rankingResponse
    
    animateMetrics()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载自定义报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const animateMetrics = () => {
  const report = currentReport.value
  const targets = {
    participants: report.totalParticipants || 0,
    participations: report.totalParticipations || 0,
    averageScore: report.averageScore || 0,
    maxScore: report.maxScore || 0,
    minScore: report.minScore || 0,
    totalScore: report.totalScore || 0
  }
  
  const duration = 1000
  const steps = 60
  const interval = duration / steps
  
  Object.keys(targets).forEach(key => {
    const start = animatedMetrics.value[key]
    const end = targets[key]
    const increment = (end - start) / steps
    let current = start
    let step = 0
    
    const timer = setInterval(() => {
      step++
      current += increment
      if (step >= steps) {
        animatedMetrics.value[key] = key === 'averageScore' ? end.toFixed(2) : Math.round(end)
        clearInterval(timer)
      } else {
        animatedMetrics.value[key] = key === 'averageScore' ? current.toFixed(2) : Math.round(current)
      }
    }, interval)
  })
}

const initCharts = () => {
  nextTick(() => {
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
    }
    if (distributionChartRef.value) {
      distributionChart = echarts.init(distributionChartRef.value)
    }
    if (levelChartRef.value) {
      levelChart = echarts.init(levelChartRef.value)
    }
    if (participationChartRef.value) {
      participationChart = echarts.init(participationChartRef.value)
    }
    if (genderChartRef.value) {
      genderChart = echarts.init(genderChartRef.value)
    }
    updateCharts()
  })
}

const updateCharts = () => {
  updateTrendChart()
  updateDistributionChart()
  updateLevelChart()
  updateParticipationChart()
  updateGenderChart()
}

const updateTrendChart = () => {
  if (!trendChart) return
  
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  const avgData = [7.2, 7.5, 7.8, 8.0, 8.2, 8.5, 8.3, 8.6, 8.8, 8.7, 8.9, 9.0]
  const maxData = [9.0, 9.2, 9.3, 9.5, 9.4, 9.6, 9.5, 9.7, 9.8, 9.7, 9.9, 10.0]
  
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
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
      data: months,
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      min: 6,
      max: 10,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '平均分',
        type: 'line',
        smooth: true,
        data: avgData,
        lineStyle: { color: '#667eea', width: 3 },
        itemStyle: { color: '#667eea' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102,126,234,0.4)' },
            { offset: 1, color: 'rgba(102,126,234,0)' }
          ])
        }
      },
      {
        name: '最高分',
        type: 'line',
        smooth: true,
        data: maxData,
        lineStyle: { color: '#f5576c', width: 3 },
        itemStyle: { color: '#f5576c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,87,108,0.4)' },
            { offset: 1, color: 'rgba(245,87,108,0)' }
          ])
        }
      }
    ]
  })
}

const updateDistributionChart = () => {
  if (!distributionChart) return
  
  distributionChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'outside',
        color: '#333',
        formatter: '{b}: {c}'
      },
      data: [
        { value: 15, name: '9-10分', itemStyle: { color: '#43e97b' } },
        { value: 35, name: '8-9分', itemStyle: { color: '#4facfe' } },
        { value: 30, name: '7-8分', itemStyle: { color: '#667eea' } },
        { value: 15, name: '6-7分', itemStyle: { color: '#f093fb' } },
        { value: 5, name: '6分以下', itemStyle: { color: '#f5576c' } }
      ]
    }]
  })
}

const updateLevelChart = () => {
  if (!levelChart) return
  
  const levelData = {}
  rankingData.value.forEach(item => {
    const level = item.athleteLevel || '未知'
    levelData[level] = (levelData[level] || 0) + 1
  })
  
  const data = Object.entries(levelData).map(([name, value]) => ({ name, value }))
  
  levelChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
    },
    series: [{
      type: 'pie',
      radius: '70%',
      data: data.length > 0 ? data : [
        { value: 40, name: '国家级', itemStyle: { color: '#FFD700' } },
        { value: 30, name: '省级', itemStyle: { color: '#C0C0C0' } },
        { value: 20, name: '市级', itemStyle: { color: '#CD7F32' } },
        { value: 10, name: '业余', itemStyle: { color: '#4facfe' } }
      ],
      label: {
        color: '#333',
        formatter: '{b}\n{d}%'
      },
      itemStyle: {
        borderRadius: 5,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  })
}

const updateParticipationChart = () => {
  if (!participationChart) return
  
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const data = [120, 200, 150, 80, 70, 180, 220]
  
  participationChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
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
      data: days,
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [{
      type: 'bar',
      data: data,
      itemStyle: {
        borderRadius: [5, 5, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#38f9d7' }
        ])
      }
    }]
  })
}

const updateGenderChart = () => {
  if (!genderChart) return
  
  let maleCount = 0
  let femaleCount = 0
  rankingData.value.forEach(item => {
    if (item.gender === 'MALE') maleCount++
    else femaleCount++
  })
  
  genderChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
    },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: maleCount || 60, name: '男', itemStyle: { color: '#4facfe' } },
        { value: femaleCount || 40, name: '女', itemStyle: { color: '#f093fb' } }
      ],
      label: {
        color: '#333',
        formatter: '{b}: {c}人'
      },
      itemStyle: {
        borderRadius: 5,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  })
}

const exportReport = () => {
  ElMessage.success('报表导出功能开发中...')
}

const getRankClass = (rank) => {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

const getAvatarColor = (rank) => {
  const colors = [
    'linear-gradient(135deg, #FFD700 0%, #FFA500 100%)',
    'linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%)',
    'linear-gradient(135deg, #CD7F32 0%, #8B4513 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  ]
  return colors[Math.min(rank - 1, colors.length - 1)]
}

const getLevelTagType = (level) => {
  const types = {
    '国家级': 'warning',
    '省级': 'info',
    '市级': 'success',
    '业余': ''
  }
  return types[level] || ''
}

const getPerformanceWidth = (score) => {
  return Math.min((score / 10) * 100, 100) + '%'
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  trendChart?.resize()
  distributionChart?.resize()
  levelChart?.resize()
  participationChart?.resize()
  genderChart?.resize()
}

onMounted(() => {
  loadMonthlyReport()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  distributionChart?.dispose()
  levelChart?.dispose()
  participationChart?.dispose()
  genderChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 50%, #dfe6f0 100%);
  padding: 20px;
  color: #333;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  background: #fff;
  border-radius: 20px;
  margin-bottom: 30px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(102,126,234,0.2) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.chart-icon {
  font-size: 48px;
  position: relative;
  z-index: 1;
}

.header-text h1 {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.header-text p {
  color: #888;
  margin: 0;
  font-size: 14px;
}

.export-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
}

/* 报表类型选择 */
.report-type-section {
  margin-bottom: 30px;
}

.type-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.type-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-card:hover {
  background: #f8f9fc;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.type-card.active {
  background: linear-gradient(135deg, rgba(102,126,234,0.1) 0%, rgba(118,75,162,0.1) 100%);
  border-color: rgba(102,126,234,0.5);
  box-shadow: 0 4px 20px rgba(102,126,234,0.2);
}

.type-icon {
  font-size: 32px;
}

.type-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.type-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 30px;
}

.filter-card {
  padding: 25px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.filter-row {
  display: flex;
  gap: 30px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-group label {
  font-size: 12px;
  color: #888;
}

.segment-control {
  display: flex;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 4px;
}

.segment-control button {
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: #888;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.segment-control button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.quarter-select {
  display: flex;
  gap: 10px;
}

/* 核心指标卡片 */
.metrics-section {
  margin-bottom: 30px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 15px;
}

.metric-card {
  position: relative;
  padding: 25px;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.metric-card.participants {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-card.participations {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-card.average {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-card.highest {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-card.lowest {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.metric-card.total {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.metric-icon {
  font-size: 24px;
  opacity: 0.9;
}

.metric-content {
  flex: 1;
}

.metric-value .number {
  font-size: 28px;
  font-weight: 700;
}

.metric-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 5px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  opacity: 0.9;
}

.metric-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 30px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.chart-card.large {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
}

.chart-legend {
  display: flex;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #888;
}

.legend-item .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-item .dot.avg {
  background: #667eea;
}

.legend-item .dot.max {
  background: #f5576c;
}

.chart-body {
  padding: 20px;
}

.chart-container {
  width: 100%;
  height: 250px;
}

/* 表格区域 */
.table-section {
  margin-bottom: 30px;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
}

.search-input {
  width: 250px;
}

.table-body {
  padding: 20px;
}

.rank-cell {
  width: 35px;
  height: 35px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin: 0 auto;
  background: #f0f0f0;
}

.rank-cell.gold {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
}

.rank-cell.silver {
  background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%);
  color: #fff;
}

.rank-cell.bronze {
  background: linear-gradient(135deg, #CD7F32 0%, #8B4513 100%);
  color: #fff;
}

.athlete-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.athlete-avatar {
  width: 35px;
  height: 35px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
}

.score-cell {
  font-weight: 700;
  color: #667eea;
}

.performance-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 1s ease;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loader {
  text-align: center;
}

.loader-ring {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(0,0,0,0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-card.large {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .type-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-card.large {
    grid-column: span 1;
  }

  .filter-row {
    flex-direction: column;
    gap: 15px;
  }
}

/* 自定义 Element Plus 样式 */
:deep(.el-select) {
  --el-fill-color-blank: #fff;
  --el-text-color-regular: #333;
  --el-border-color: #dcdfe6;
}

:deep(.el-input__wrapper) {
  background: #fff !important;
  border-color: #dcdfe6 !important;
}

:deep(.el-input__inner) {
  color: #333 !important;
}

:deep(.el-input-number) {
  --el-fill-color-blank: #fff;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #f8fafc;
  --el-table-text-color: #333;
}

:deep(.el-table__body-wrapper) {
  background: #fff;
  border-radius: 0 0 12px 12px;
}

:deep(.dark-picker .el-input__wrapper) {
  background: #fff !important;
}
</style>
