<template>
  <div class="statistics-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">📊 统计报表</span>
        </div>
      </template>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 月度报表 -->
        <el-tab-pane label="月度报表" name="monthly">
          <div class="report-content">
            <div class="filter-group">
              <el-segmented v-model="monthlyReportType" :options="reportTypeOptions" @change="loadMonthlyReport"></el-segmented>
              <el-date-picker 
                v-model="monthlyDate" 
                type="month"
                placeholder="选择月份"
                @change="loadMonthlyReport"
              ></el-date-picker>
            </div>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与人数</div>
                  <div class="stat-value">{{ monthlyReport.totalParticipants || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与次数</div>
                  <div class="stat-value">{{ monthlyReport.totalParticipations || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ monthlyReport.averageScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最高分</div>
                  <div class="stat-value">{{ monthlyReport.maxScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最低分</div>
                  <div class="stat-value">{{ monthlyReport.minScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">总分</div>
                  <div class="stat-value">{{ monthlyReport.totalScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 季度报表 -->
        <el-tab-pane label="季度报表" name="quarterly">
          <div class="report-content">
            <div class="filter-group">
              <el-input-number v-model="quarterlyYear" :min="2020" :max="2100" placeholder="年份"></el-input-number>
              <el-select v-model="quarterlyQuarter" placeholder="选择季度" style="width: 120px" @change="loadQuarterlyReport">
                <el-option label="Q1 (1-3月)" value="1"></el-option>
                <el-option label="Q2 (4-6月)" value="2"></el-option>
                <el-option label="Q3 (7-9月)" value="3"></el-option>
                <el-option label="Q4 (10-12月)" value="4"></el-option>
              </el-select>
              <el-segmented v-model="quarterlyReportType" :options="reportTypeOptions" @change="loadQuarterlyReport"></el-segmented>
              <el-button type="primary" @click="loadQuarterlyReport">查询</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与人数</div>
                  <div class="stat-value">{{ quarterlyReport.totalParticipants || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与次数</div>
                  <div class="stat-value">{{ quarterlyReport.totalParticipations || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ quarterlyReport.averageScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最高分</div>
                  <div class="stat-value">{{ quarterlyReport.maxScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 年度报表 -->
        <el-tab-pane label="年度报表" name="yearly">
          <div class="report-content">
            <div class="filter-group">
              <el-input-number v-model="yearlyYear" :min="2020" :max="2100" placeholder="年份"></el-input-number>
              <el-segmented v-model="yearlyReportType" :options="reportTypeOptions" @change="loadYearlyReport"></el-segmented>
              <el-button type="primary" @click="loadYearlyReport">查询</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与人数</div>
                  <div class="stat-value">{{ yearlyReport.totalParticipants || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与次数</div>
                  <div class="stat-value">{{ yearlyReport.totalParticipations || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ yearlyReport.averageScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最高分</div>
                  <div class="stat-value">{{ yearlyReport.maxScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最低分</div>
                  <div class="stat-value">{{ yearlyReport.minScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">总分</div>
                  <div class="stat-value">{{ yearlyReport.totalScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 自定义范围 -->
        <el-tab-pane label="自定义范围" name="custom">
          <div class="report-content">
            <div class="filter-group">
              <el-date-picker 
                v-model="customDateRange" 
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              ></el-date-picker>
              <el-segmented v-model="customReportType" :options="reportTypeOptions"></el-segmented>
              <el-button type="primary" @click="loadCustomReport">查询</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与人数</div>
                  <div class="stat-value">{{ customReport.totalParticipants || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">参与次数</div>
                  <div class="stat-value">{{ customReport.totalParticipations || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ customReport.averageScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最高分</div>
                  <div class="stat-value">{{ customReport.maxScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">最低分</div>
                  <div class="stat-value">{{ customReport.minScore || 0 }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="stat-card">
                  <div class="stat-label">总分</div>
                  <div class="stat-value">{{ customReport.totalScore || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 加载状态 -->
    <el-dialog v-model="loading" :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <div style="text-align: center; padding: 20px;">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statisticsAPI } from '@/api/ranking'
import { ElMessage } from 'element-plus'

const activeTab = ref('monthly')
const loading = ref(false)

const reportTypeOptions = [
  { label: '训练', value: 'TRAINING' },
  { label: '比赛', value: 'COMPETITION' },
  { label: '综合', value: 'OVERALL' }
]

// 月度报表
const monthlyReport = ref({})
const monthlyReportType = ref('OVERALL')
const monthlyDate = ref(new Date())

// 季度报表
const quarterlyReport = ref({})
const quarterlyReportType = ref('OVERALL')
const quarterlyYear = ref(new Date().getFullYear())
const quarterlyQuarter = ref('4')

// 年度报表
const yearlyReport = ref({})
const yearlyReportType = ref('OVERALL')
const yearlyYear = ref(new Date().getFullYear())

// 自定义范围
const customReport = ref({})
const customReportType = ref('OVERALL')
const customDateRange = ref([
  new Date(new Date().getFullYear(), new Date().getMonth(), 1),
  new Date()
])

// 加载月度报表
const loadMonthlyReport = async () => {
  if (!monthlyDate.value) return
  loading.value = true
  try {
    const year = monthlyDate.value.getFullYear()
    const month = monthlyDate.value.getMonth() + 1
    let response
    if (monthlyReportType.value === 'TRAINING') {
      response = await statisticsAPI.getMonthlyTrainingStatistics(year, month)
    } else if (monthlyReportType.value === 'COMPETITION') {
      response = await statisticsAPI.getMonthlyCompetitionStatistics(year, month)
    } else {
      response = await statisticsAPI.getMonthlyTrainingStatistics(year, month)
    }
    monthlyReport.value = response.data
  } catch (error) {
    ElMessage.error('加载月度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载季度报表
const loadQuarterlyReport = async () => {
  loading.value = true
  try {
    const response = await statisticsAPI.getQuarterlyStatistics(quarterlyYear.value, quarterlyQuarter.value, quarterlyReportType.value)
    quarterlyReport.value = response.data
  } catch (error) {
    ElMessage.error('加载季度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载年度报表
const loadYearlyReport = async () => {
  loading.value = true
  try {
    const response = await statisticsAPI.getYearlyStatistics(yearlyYear.value, yearlyReportType.value)
    yearlyReport.value = response.data
  } catch (error) {
    ElMessage.error('加载年度报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载自定义范围报表
const loadCustomReport = async () => {
  if (!customDateRange.value || customDateRange.value.length !== 2) {
    ElMessage.warning('请选择完整的日期范围')
    return
  }
  loading.value = true
  try {
    const startDate = customDateRange.value[0].toISOString()
    const endDate = customDateRange.value[1].toISOString()
    const response = await statisticsAPI.getCustomStatistics(startDate, endDate, customReportType.value)
    customReport.value = response.data
  } catch (error) {
    ElMessage.error('加载自定义范围报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 标签页切换处理
const handleTabChange = (tab) => {
  if (tab === 'monthly' && Object.keys(monthlyReport.value).length === 0) {
    loadMonthlyReport()
  }
}

// 页面加载时
onMounted(() => {
  loadMonthlyReport()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.report-content {
  padding: 20px;
}

.filter-group {
  margin-bottom: 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card:nth-child(2) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card:nth-child(3) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card:nth-child(4) {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-input-number) {
  width: 100px;
}

:deep(.el-segmented) {
  background-color: #f0f0f0;
}
</style>
