<template>
  <div class="compare-container">
    <!-- 页面标题 -->
    <section class="page-header">
      <h1 class="page-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <path d="M20 8v6"/><path d="M23 11h-6"/>
        </svg>
        运动员对比分析
      </h1>
      <p class="page-subtitle">横向对比同级别运动员的关键训练指标</p>
    </section>

    <!-- 筛选条件 -->
    <section class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <label>运动员级别</label>
            <el-select v-model="filters.level" placeholder="选择级别" clearable>
              <el-option v-for="level in levels" :key="level" :label="level" :value="level"/>
            </el-select>
          </div>
          
          <div class="filter-item">
            <label>时间范围</label>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :shortcuts="dateShortcuts"
            />
          </div>
          
          <div class="filter-item">
            <label>项目类型</label>
            <el-select v-model="filters.projectType" placeholder="全部项目" clearable>
              <el-option label="10米气步枪" value="10米气步枪"/>
              <el-option label="10米气手枪" value="10米气手枪"/>
              <el-option label="50米步枪" value="50米步枪"/>
            </el-select>
          </div>
          
          <div class="filter-item">
            <label>对比人数</label>
            <el-input-number v-model="filters.maxCount" :min="2" :max="10"/>
          </div>
        </div>
        
        <div class="filter-actions">
          <el-button type="primary" @click="loadCompareData" :loading="loading">
            开始对比
          </el-button>
          <el-button @click="exportData" :disabled="!compareData.length">
            导出Excel
          </el-button>
        </div>
      </div>
    </section>

    <!-- 图表展示区域 -->
    <section class="charts-section" v-if="compareData.length">
      <div class="charts-grid">
        <!-- 雷达图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>综合能力雷达图</h3>
            <p class="chart-desc">多维度能力对比，面积越大综合能力越强</p>
          </div>
          <div class="chart-body">
            <canvas ref="radarChartRef"></canvas>
          </div>
        </div>
        
        <!-- 柱状图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>成绩对比</h3>
            <div class="chart-controls">
              <el-radio-group v-model="barChartMetric" size="small" @change="updateBarChart">
                <el-radio-button label="avgScore">平均环数</el-radio-button>
                <el-radio-button label="perfectRate">10环率</el-radio-button>
                <el-radio-button label="highScoreRate">高分率</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-body">
            <canvas ref="barChartRef"></canvas>
          </div>
        </div>
      </div>
      
      <!-- 数据表格 -->
      <div class="data-table-card">
        <h3>详细数据</h3>
        <el-table :data="compareData" stripe>
          <el-table-column prop="athleteName" label="姓名" width="120" fixed/>
          <el-table-column prop="athleteLevel" label="级别" width="100"/>
          <el-table-column prop="avgScore" label="平均环数" width="100">
            <template #default="{ row }">
              <span class="score-value">{{ row.avgScore?.toFixed(2) || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="maxScore" label="最高环数" width="100"/>
          <el-table-column prop="minScore" label="最低环数" width="100"/>
          <el-table-column prop="stabilityIndex" label="稳定性指数" width="110">
            <template #default="{ row }">
              <el-tag :type="getStabilityType(row.stabilityIndex)" size="small">
                {{ row.stabilityIndex?.toFixed(2) || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="perfectRate" label="10环率(%)" width="100">
            <template #default="{ row }">{{ row.perfectRate?.toFixed(1) || '-' }}%</template>
          </el-table-column>
          <el-table-column prop="highScoreRate" label="9环以上率(%)" width="120">
            <template #default="{ row }">{{ row.highScoreRate?.toFixed(1) || '-' }}%</template>
          </el-table-column>
          <el-table-column prop="totalShots" label="射击总数" width="100"/>
          <el-table-column prop="sessionCount" label="训练场次" width="100"/>
        </el-table>
      </div>
    </section>

    <!-- 空状态 -->
    <section class="empty-section" v-else-if="!loading">
      <div class="empty-content">
        <h3>暂无对比数据</h3>
        <p>请选择筛选条件后点击"开始对比"</p>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import Chart from 'chart.js/auto';
import { compareAthletes, exportCompareData, getAvailableLevels } from '@/api/analytics';

export default {
  name: 'AthleteCompare',
  
  setup() {
    const loading = ref(false);
    const compareData = ref([]);
    const levels = ref(['国家级', '省级', '市级', '业余']);
    const barChartMetric = ref('avgScore');
    
    const radarChartRef = ref(null);
    const barChartRef = ref(null);
    let radarChart = null;
    let barChart = null;
    
    const filters = reactive({
      level: '',
      dateRange: null,
      projectType: '',
      maxCount: 5
    });
    
    const dateShortcuts = [
      { text: '最近一周', value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 7 * 24 * 3600 * 1000);
        return [start, end];
      }},
      { text: '最近一月', value: () => {
        const end = new Date();
        const start = new Date();
        start.setMonth(start.getMonth() - 1);
        return [start, end];
      }},
      { text: '最近三月', value: () => {
        const end = new Date();
        const start = new Date();
        start.setMonth(start.getMonth() - 3);
        return [start, end];
      }}
    ];
    
    const loadCompareData = async () => {
      loading.value = true;
      try {
        const params = {
          level: filters.level || null,
          projectType: filters.projectType || null,
          maxCount: filters.maxCount
        };
        
        if (filters.dateRange && filters.dateRange.length === 2) {
          params.startTime = filters.dateRange[0].toISOString();
          params.endTime = filters.dateRange[1].toISOString();
        }
        
        const response = await compareAthletes(params);
        if (response.success) {
          compareData.value = response.data || [];
          await nextTick();
          renderCharts();
        } else {
          ElMessage.error(response.message || '加载失败');
        }
      } catch (error) {
        ElMessage.error('加载对比数据失败');
      } finally {
        loading.value = false;
      }
    };
    
    const renderCharts = () => {
      renderRadarChart();
      renderBarChart();
    };
    
    const renderRadarChart = () => {
      if (radarChart) radarChart.destroy();
      if (!radarChartRef.value || !compareData.value.length) return;
      
      const ctx = radarChartRef.value.getContext('2d');
      const labels = ['平均成绩', '最高成绩', '稳定性', '10环率', '高分率', '训练量'];
      const colors = [
        'rgba(16, 185, 129, 0.7)',
        'rgba(59, 130, 246, 0.7)',
        'rgba(245, 158, 11, 0.7)',
        'rgba(239, 68, 68, 0.7)',
        'rgba(139, 92, 246, 0.7)'
      ];
      
      const datasets = compareData.value.map((athlete, index) => ({
        label: athlete.athleteName,
        data: [
          (athlete.avgScore || 0) * 10,
          (athlete.maxScore || 0) * 10,
          athlete.stabilityScore || 0,
          athlete.perfectRate || 0,
          athlete.highScoreRate || 0,
          Math.min((athlete.sessionCount || 0) * 5, 100)
        ],
        backgroundColor: colors[index % colors.length].replace('0.7', '0.2'),
        borderColor: colors[index % colors.length],
        borderWidth: 2
      }));
      
      radarChart = new Chart(ctx, {
        type: 'radar',
        data: { labels, datasets },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { r: { beginAtZero: true, max: 100, ticks: { stepSize: 20 } } },
          plugins: { legend: { position: 'bottom' } }
        }
      });
    };
    
    const renderBarChart = () => {
      if (barChart) barChart.destroy();
      if (!barChartRef.value || !compareData.value.length) return;
      
      const ctx = barChartRef.value.getContext('2d');
      const labels = compareData.value.map(a => a.athleteName);
      const metricLabels = {
        avgScore: '平均环数',
        perfectRate: '10环率(%)',
        highScoreRate: '9环以上率(%)'
      };
      const data = compareData.value.map(a => a[barChartMetric.value] || 0);
      const colors = [
        'rgba(16, 185, 129, 0.8)',
        'rgba(59, 130, 246, 0.8)',
        'rgba(245, 158, 11, 0.8)',
        'rgba(239, 68, 68, 0.8)',
        'rgba(139, 92, 246, 0.8)'
      ];
      
      barChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: metricLabels[barChartMetric.value],
            data,
            backgroundColor: data.map((_, i) => colors[i % colors.length]),
            borderRadius: 8
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true, max: barChartMetric.value === 'avgScore' ? 10 : 100 } }
        }
      });
    };
    
    const updateBarChart = () => renderBarChart();
    
    const exportData = async () => {
      try {
        const params = {
          level: filters.level || null,
          projectType: filters.projectType || null,
          maxCount: filters.maxCount
        };
        if (filters.dateRange && filters.dateRange.length === 2) {
          params.startTime = filters.dateRange[0].toISOString();
          params.endTime = filters.dateRange[1].toISOString();
        }
        
        const response = await exportCompareData(params);
        const blob = new Blob([response], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `运动员对比分析_${new Date().toISOString().split('T')[0]}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        ElMessage.success('导出成功');
      } catch (error) {
        ElMessage.error('导出失败');
      }
    };
    
    const getStabilityType = (value) => {
      if (value === null || value === undefined) return 'info';
      if (value < 1) return 'success';
      if (value < 2) return 'warning';
      return 'danger';
    };
    
    onMounted(async () => {
      try {
        const response = await getAvailableLevels();
        if (response.success && response.levels) {
          levels.value = response.levels;
        }
      } catch (error) {
        console.error('获取级别列表失败:', error);
      }
    });
    
    return {
      loading, compareData, levels, filters, dateShortcuts,
      barChartMetric, radarChartRef, barChartRef,
      loadCompareData, updateBarChart, exportData, getStabilityType
    };
  }
};
</script>

<style scoped>
.compare-container { max-width: 1400px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { display: flex; align-items: center; gap: 12px; font-size: 1.75rem; font-weight: 700; color: #1f2937; }
.title-icon { width: 32px; height: 32px; color: #10b981; }
.page-subtitle { color: #6b7280; font-size: 1rem; margin-top: 8px; }

.filter-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.filter-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 20px; }
.filter-item { display: flex; flex-direction: column; gap: 8px; }
.filter-item label { font-weight: 500; color: #374151; font-size: 0.875rem; }
.filter-actions { display: flex; gap: 12px; justify-content: flex-end; }

.charts-section { margin-top: 24px; }
.charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(500px, 1fr)); gap: 24px; margin-bottom: 24px; }
.chart-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.chart-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.chart-header h3 { font-size: 1.1rem; font-weight: 600; color: #1f2937; margin: 0; }
.chart-desc { color: #6b7280; font-size: 0.875rem; margin-top: 4px; }
.chart-body { height: 350px; position: relative; }

.data-table-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.data-table-card h3 { font-size: 1.1rem; font-weight: 600; color: #1f2937; margin: 0 0 16px 0; }
.score-value { font-weight: 600; color: #10b981; }

.empty-section { display: flex; justify-content: center; align-items: center; min-height: 400px; }
.empty-content { text-align: center; }
.empty-content h3 { font-size: 1.25rem; color: #374151; margin-bottom: 8px; }
.empty-content p { color: #6b7280; }

@media (max-width: 768px) {
  .compare-container { padding: 16px; }
  .charts-grid { grid-template-columns: 1fr; }
  .filter-row { grid-template-columns: 1fr; }
}
</style>
