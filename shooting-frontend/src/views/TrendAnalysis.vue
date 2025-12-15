<template>
  <div class="trend-container">
    <!-- 页面标题 -->
    <section class="page-header">
      <h1 class="page-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
        </svg>
        历史趋势分析
      </h1>
      <p class="page-subtitle">追踪训练成绩变化趋势，预测未来表现</p>
    </section>

    <!-- 筛选条件 -->
    <section class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <label>选择运动员</label>
            <el-select v-model="selectedAthleteId" placeholder="请选择运动员" filterable>
              <el-option v-for="athlete in athletes" :key="athlete.id" 
                :label="athlete.name" :value="athlete.id"/>
            </el-select>
          </div>
          
          <div class="filter-item">
            <label>分析周数</label>
            <el-select v-model="weeks">
              <el-option :value="4" label="最近4周"/>
              <el-option :value="8" label="最近8周"/>
              <el-option :value="12" label="最近12周"/>
              <el-option :value="24" label="最近24周"/>
            </el-select>
          </div>
          
          <div class="filter-item">
            <label>项目类型</label>
            <el-select v-model="projectType" placeholder="全部项目" clearable>
              <el-option label="10米气步枪" value="10米气步枪"/>
              <el-option label="10米气手枪" value="10米气手枪"/>
              <el-option label="50米步枪" value="50米步枪"/>
            </el-select>
          </div>
        </div>
        
        <div class="filter-actions">
          <el-button type="primary" @click="loadTrendData" :loading="loading" :disabled="!selectedAthleteId">
            分析趋势
          </el-button>
          <el-button @click="exportData" :disabled="!trendData">
            导出Excel
          </el-button>
        </div>
      </div>
    </section>

    <!-- 趋势分析结果 -->
    <section class="result-section" v-if="trendData">
      <!-- 摘要卡片 -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="summary-icon" :class="'trend-' + trendData.trendDirection?.toLowerCase()">
            <svg v-if="trendData.trendDirection === 'UP'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
              <polyline points="17 6 23 6 23 12"/>
            </svg>
            <svg v-else-if="trendData.trendDirection === 'DOWN'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/>
              <polyline points="17 18 23 18 23 12"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </div>
          <div class="summary-info">
            <span class="summary-label">趋势方向</span>
            <span class="summary-value">{{ getTrendText(trendData.trendDirection) }}</span>
          </div>
        </div>
        
        <div class="summary-card">
          <div class="summary-info">
            <span class="summary-label">期间平均</span>
            <span class="summary-value">{{ trendData.summary?.periodAverage?.toFixed(2) || '-' }} 环</span>
          </div>
        </div>
        
        <div class="summary-card">
          <div class="summary-info">
            <span class="summary-label">最佳周</span>
            <span class="summary-value highlight">
              {{ trendData.summary?.bestWeek || '-' }}
              <small>({{ trendData.summary?.periodHighest?.toFixed(2) || '-' }}环)</small>
            </span>
          </div>
        </div>
        
        <div class="summary-card">
          <div class="summary-info">
            <span class="summary-label">进步幅度</span>
            <span class="summary-value" :class="getImprovementClass(trendData.summary?.improvement)">
              {{ formatImprovement(trendData.summary?.improvement) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 趋势图表 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>成绩趋势图</h3>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-dot actual"></span>实际成绩</span>
            <span class="legend-item"><span class="legend-dot moving-avg"></span>移动平均</span>
            <span class="legend-item"><span class="legend-dot prediction"></span>预测值</span>
          </div>
        </div>
        <div class="chart-body">
          <canvas ref="trendChartRef"></canvas>
        </div>
      </div>

      <!-- 详细数据表格 -->
      <div class="data-table-card">
        <h3>周度详细数据</h3>
        <el-table :data="allTrendData" stripe>
          <el-table-column prop="period" label="周期" width="120" fixed>
            <template #default="{ row }">
              <el-tag v-if="row.isPredicted" type="warning" size="small">预测</el-tag>
              <span v-else>{{ row.period }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="avgScore" label="平均环数" width="100">
            <template #default="{ row }">
              <span :class="{ 'predicted-value': row.isPredicted }">
                {{ row.avgScore?.toFixed(2) || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="movingAverage" label="移动平均" width="100">
            <template #default="{ row }">{{ row.movingAverage?.toFixed(2) || '-' }}</template>
          </el-table-column>
          <el-table-column prop="maxScore" label="最高环数" width="100"/>
          <el-table-column prop="minScore" label="最低环数" width="100"/>
          <el-table-column prop="stabilityIndex" label="稳定性" width="100">
            <template #default="{ row }">{{ row.stabilityIndex?.toFixed(2) || '-' }}</template>
          </el-table-column>
          <el-table-column prop="totalShots" label="射击数" width="100"/>
          <el-table-column prop="sessionCount" label="场次" width="80"/>
        </el-table>
      </div>
    </section>

    <!-- 空状态 -->
    <section class="empty-section" v-else-if="!loading">
      <div class="empty-content">
        <h3>暂无趋势数据</h3>
        <p>请选择运动员后点击"分析趋势"</p>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import Chart from 'chart.js/auto';
import { getTrendAnalysis, exportTrendData, getAthleteList } from '@/api/analytics';

export default {
  name: 'TrendAnalysis',
  
  setup() {
    const loading = ref(false);
    const athletes = ref([]);
    const selectedAthleteId = ref(null);
    const weeks = ref(12);
    const projectType = ref('');
    const trendData = ref(null);
    const trendChartRef = ref(null);
    let trendChart = null;
    
    const allTrendData = computed(() => {
      if (!trendData.value) return [];
      return [
        ...(trendData.value.trendData || []),
        ...(trendData.value.predictions || [])
      ];
    });
    
    const loadAthletes = async () => {
      try {
        const response = await getAthleteList();
        if (response.success) {
          athletes.value = response.athletes || [];
        }
      } catch (error) {
        console.error('加载运动员列表失败:', error);
        ElMessage.error('加载运动员列表失败');
      }
    };
    
    const loadTrendData = async () => {
      if (!selectedAthleteId.value) {
        ElMessage.warning('请先选择运动员');
        return;
      }
      
      loading.value = true;
      try {
        const response = await getTrendAnalysis(
          selectedAthleteId.value, weeks.value, projectType.value || null
        );
        
        if (response.success) {
          trendData.value = response.data;
          await nextTick();
          renderTrendChart();
        } else {
          ElMessage.error(response.message || '加载失败');
        }
      } catch (error) {
        ElMessage.error('加载趋势数据失败');
      } finally {
        loading.value = false;
      }
    };
    
    const renderTrendChart = () => {
      if (trendChart) trendChart.destroy();
      if (!trendChartRef.value || !trendData.value) return;
      
      const ctx = trendChartRef.value.getContext('2d');
      const historicalData = trendData.value.trendData || [];
      const predictions = trendData.value.predictions || [];
      
      const labels = [
        ...historicalData.map(d => d.period),
        ...predictions.map(d => d.period)
      ];
      
      const actualScores = [
        ...historicalData.map(d => d.avgScore),
        ...predictions.map(() => null)
      ];
      
      const movingAverages = [
        ...historicalData.map(d => d.movingAverage),
        ...predictions.map(() => null)
      ];
      
      const predictedScores = [
        ...historicalData.map(() => null),
        ...predictions.map(d => d.predictedScore)
      ];
      
      // 连接线
      if (historicalData.length > 0 && predictions.length > 0) {
        predictedScores[historicalData.length - 1] = historicalData[historicalData.length - 1].avgScore;
      }
      
      trendChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: '实际成绩',
              data: actualScores,
              borderColor: 'rgba(16, 185, 129, 1)',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              fill: true,
              tension: 0.3,
              pointRadius: 5
            },
            {
              label: '移动平均',
              data: movingAverages,
              borderColor: 'rgba(59, 130, 246, 1)',
              borderWidth: 2,
              borderDash: [5, 5],
              fill: false,
              tension: 0.3,
              pointRadius: 3
            },
            {
              label: '预测值',
              data: predictedScores,
              borderColor: 'rgba(245, 158, 11, 1)',
              backgroundColor: 'rgba(245, 158, 11, 0.1)',
              borderWidth: 2,
              borderDash: [10, 5],
              fill: true,
              tension: 0.3,
              pointRadius: 5,
              pointStyle: 'triangle'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: { intersect: false, mode: 'index' },
          plugins: { legend: { display: false } },
          scales: {
            y: { min: 0, max: 10, ticks: { stepSize: 1 }, title: { display: true, text: '平均环数' } },
            x: { title: { display: true, text: '周期' } }
          }
        }
      });
    };
    
    const exportData = async () => {
      if (!selectedAthleteId.value) return;
      
      try {
        const response = await exportTrendData(
          selectedAthleteId.value, weeks.value, projectType.value || null
        );
        
        const blob = new Blob([response], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `趋势分析_${trendData.value?.athleteName || 'athlete'}_${new Date().toISOString().split('T')[0]}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        ElMessage.success('导出成功');
      } catch (error) {
        ElMessage.error('导出失败');
      }
    };
    
    const getTrendText = (direction) => {
      const map = { 'UP': '上升趋势 ↑', 'DOWN': '下降趋势 ↓', 'STABLE': '保持稳定 →' };
      return map[direction] || '未知';
    };
    
    const formatImprovement = (value) => {
      if (value === null || value === undefined) return '-';
      const prefix = value > 0 ? '+' : '';
      return `${prefix}${value.toFixed(2)} 环`;
    };
    
    const getImprovementClass = (value) => {
      if (value === null || value === undefined) return '';
      if (value > 0) return 'improvement-positive';
      if (value < 0) return 'improvement-negative';
      return '';
    };
    
    onMounted(() => {
      loadAthletes();
    });
    
    return {
      loading, athletes, selectedAthleteId, weeks, projectType,
      trendData, trendChartRef, allTrendData,
      loadTrendData, exportData, getTrendText, formatImprovement, getImprovementClass
    };
  }
};
</script>

<style scoped>
.trend-container { max-width: 1400px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { display: flex; align-items: center; gap: 12px; font-size: 1.75rem; font-weight: 700; color: #1f2937; }
.title-icon { width: 32px; height: 32px; color: #10b981; }
.page-subtitle { color: #6b7280; font-size: 1rem; margin-top: 8px; }

.filter-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.filter-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 20px; }
.filter-item { display: flex; flex-direction: column; gap: 8px; }
.filter-item label { font-weight: 500; color: #374151; font-size: 0.875rem; }
.filter-actions { display: flex; gap: 12px; justify-content: flex-end; }

.result-section { margin-top: 24px; }
.summary-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
.summary-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); display: flex; align-items: center; gap: 16px; }
.summary-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.summary-icon svg { width: 24px; height: 24px; }
.summary-icon.trend-up { background: #dcfce7; color: #16a34a; }
.summary-icon.trend-down { background: #fee2e2; color: #dc2626; }
.summary-icon.trend-stable { background: #dbeafe; color: #2563eb; }
.summary-info { display: flex; flex-direction: column; }
.summary-label { font-size: 0.875rem; color: #6b7280; }
.summary-value { font-size: 1.25rem; font-weight: 600; color: #1f2937; }
.summary-value.highlight { color: #10b981; }
.summary-value small { font-size: 0.75rem; font-weight: normal; color: #6b7280; }

.chart-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); margin-bottom: 24px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.chart-header h3 { font-size: 1.1rem; font-weight: 600; color: #1f2937; margin: 0; }
.chart-legend { display: flex; gap: 16px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 0.875rem; color: #6b7280; }
.legend-dot { width: 12px; height: 12px; border-radius: 50%; }
.legend-dot.actual { background: rgba(16, 185, 129, 1); }
.legend-dot.moving-avg { background: rgba(59, 130, 246, 1); }
.legend-dot.prediction { background: rgba(245, 158, 11, 1); }
.chart-body { height: 400px; position: relative; }

.data-table-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.data-table-card h3 { font-size: 1.1rem; font-weight: 600; color: #1f2937; margin: 0 0 16px 0; }
.predicted-value { color: #f59e0b; font-style: italic; }

.improvement-positive { color: #16a34a; }
.improvement-negative { color: #dc2626; }

.empty-section { display: flex; justify-content: center; align-items: center; min-height: 400px; }
.empty-content { text-align: center; }
.empty-content h3 { font-size: 1.25rem; color: #374151; margin-bottom: 8px; }
.empty-content p { color: #6b7280; }

@media (max-width: 768px) {
  .trend-container { padding: 16px; }
  .summary-cards { grid-template-columns: repeat(2, 1fr); }
  .filter-row { grid-template-columns: 1fr; }
  .chart-legend { flex-wrap: wrap; }
}
</style>
