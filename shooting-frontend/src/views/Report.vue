<template>
  <div class="report-page">
    <div v-if="isLoading" class="loading">
      <p>正在生成报告...</p>
    </div>
    
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadReportData" class="btn btn-primary">重试</button>
    </div>
    
    <div v-else-if="report" class="report-content">
      <div class="report-header">
        <h1>{{ report.title || '训练报告' }}</h1>
        <p class="date">{{ formatDate(report.date) }}</p>
      </div>
      
      <!-- 统计摘要信息 -->
      <div class="stats-summary">
        <div class="stats-card">
          <div class="stat-item">
            <span class="stat-label">总射击数</span>
            <span class="stat-value">{{ report.totalShots }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">平均环数</span>
            <span class="stat-value">{{ report.averageScore.toFixed(2) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">最高环数</span>
            <span class="stat-value">{{ report.highestScore }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">最低环数</span>
            <span class="stat-value">{{ report.lowestScore }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总环数</span>
            <span class="stat-value">{{ report.totalScore }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">10环次数</span>
            <span class="stat-value">{{ report.tenCount }}</span>
          </div>
        </div>
      </div>
      
      <!-- 散点图：弹着点分布 -->
      <div class="chart-container">
        <h2>弹着点分布</h2>
        <div class="chart-wrapper">
          <ScatterChart 
            :chartData="scatterChartData" 
            :chartOptions="scatterChartOptions"
          />
        </div>
      </div>
      
      <!-- 柱状图：环数分布 -->
      <div class="chart-container">
        <h2>环数分布</h2>
        <div class="chart-wrapper">
          <BarChart 
            :chartData="barChartData" 
            :chartOptions="barChartOptions"
          />
        </div>
      </div>
      
      <!-- 折线图：成绩趋势 -->
      <div class="chart-container">
        <h2>成绩趋势</h2>
        <div class="chart-wrapper">
          <LineChart 
            :chartData="lineChartData" 
            :chartOptions="lineChartOptions"
          />
        </div>
      </div>
      
      <!-- 射击记录表格 -->
      <div class="records-table-container">
        <h2>射击记录</h2>
        <table class="records-table">
          <thead>
            <tr>
              <th>#</th>
              <th>时间</th>
              <th>环数</th>
              <th>X坐标</th>
              <th>Y坐标</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in report.records" :key="index">
              <td>{{ index + 1 }}</td>
              <td>{{ formatTime(record.timestamp) }}</td>
              <td :class="getScoreClass(record.score)">{{ record.score }}</td>
              <td>{{ record.x.toFixed(2) }}</td>
              <td>{{ record.y.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 下载按钮 -->
      <div class="actions">
        <button @click="downloadPdf" class="btn btn-primary">
          下载PDF报告
        </button>
      </div>
    </div>
    
    <div v-else class="no-data">
      <p>未找到报告数据</p>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import * as trainingAPI from '@/api/training';
import * as competitionAPI from '@/api/competition';
import { Scatter as ScatterChart, Bar as BarChart, Line as LineChart } from 'vue-chartjs';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { toDisplayRecords } from '@/utils/coordinates';

// 注册Chart.js组件
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend);

export default {
  name: 'ReportView',
  
  components: {
    ScatterChart,
    BarChart,
    LineChart
  },
  
  setup() {
    const route = useRoute();
    const report = ref(null);
    const isLoading = ref(false);
    const error = ref(null);
    
    // 获取报告类型和ID
    const reportType = computed(() => route.query.type || 'training');
    const reportId = computed(() => route.params.id);
    
    // 加载报告数据
    const loadReportData = async () => {
      isLoading.value = true;
      error.value = null;
      
      try {
        let data;
        
        // 根据报告类型调用不同的API
        if (reportType.value === 'competition') {
          data = await competitionAPI.getCompetitionResults(reportId.value);
        } else {
          data = await trainingAPI.getTrainingReport(reportId.value);
        }
        
        // 处理报告数据
        processReportData(data);
      } catch (err) {
        error.value = err.message || '加载报告数据失败';
        console.error('加载报告数据失败:', err);
      } finally {
        isLoading.value = false;
      }
    };
    
    // 处理报告数据
    const processReportData = (data) => {
      // 如果没有记录，则返回
      if (!data || !data.records || data.records.length === 0) {
        report.value = null;
        return;
      }
      
      // 计算统计信息
      const records = toDisplayRecords(data.records || []);
      const scores = records.map(record => record.score);
      
      const totalScore = scores.reduce((sum, score) => sum + score, 0);
      const averageScore = totalScore / records.length;
      const highestScore = Math.max(...scores);
      const lowestScore = Math.min(...scores);
      const tenCount = scores.filter(score => score === 10).length;
      
      // 构建报告对象
      report.value = {
        title: data.name || (reportType.value === 'competition' ? '比赛报告' : '训练报告'),
        date: data.date || data.startTime || new Date().toISOString(),
        records,
        totalShots: records.length,
        totalScore,
        averageScore,
        highestScore,
        lowestScore,
        tenCount
      };
    };
    
    // 下载PDF报告
    const downloadPdf = async () => {
      try {
        isLoading.value = true;
        
        let response;
        if (reportType.value === 'competition') {
          response = await competitionAPI.downloadCompetitionResultsPdf(reportId.value);
        } else {
          response = await trainingAPI.downloadTrainingReportPdf(reportId.value);
        }
        
        // 创建Blob对象并下载
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${reportType.value}_report_${reportId.value}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      } catch (err) {
        error.value = err.message || '下载PDF报告失败';
        console.error('下载PDF报告失败:', err);
      } finally {
        isLoading.value = false;
      }
    };
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };
    
    // 格式化时间
    const formatTime = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };
    
    // 获取分数对应的CSS类
    const getScoreClass = (score) => {
      if (score >= 10) return 'score-10';
      if (score >= 9) return 'score-9';
      if (score >= 8) return 'score-8';
      if (score >= 7) return 'score-7';
      return 'score-low';
    };
    
    // 散点图数据
    const scatterChartData = computed(() => {
      if (!report.value) return { datasets: [] };
      
      return {
        datasets: [{
          label: '弹着点',
          data: report.value.records.map(record => ({
            x: record.x,
            y: record.y
          })),
          backgroundColor: report.value.records.map(record => {
            // 根据环数设置不同的颜色
            if (record.score >= 10) return 'rgba(255, 0, 0, 0.8)';
            if (record.score >= 9) return 'rgba(255, 165, 0, 0.8)';
            if (record.score >= 8) return 'rgba(255, 255, 0, 0.8)';
            if (record.score >= 7) return 'rgba(0, 128, 0, 0.8)';
            return 'rgba(0, 0, 255, 0.8)';
          }),
          pointRadius: 8,
          pointHoverRadius: 10
        }]
      };
    });
    
    // 散点图选项
    const scatterChartOptions = computed(() => {
      return {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            min: 0,
            max: 200,
            grid: {
              color: 'rgba(200, 200, 200, 0.3)'
            },
            title: {
              display: true,
              text: 'X坐标'
            }
          },
          y: {
            min: 0,
            max: 200,
            grid: {
              color: 'rgba(200, 200, 200, 0.3)'
            },
            title: {
              display: true,
              text: 'Y坐标'
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: (context) => {
                const index = context.dataIndex;
                const record = report.value.records[index];
                return [
                  `环数: ${record.score}`,
                  `X: ${record.x.toFixed(2)}`,
                  `Y: ${record.y.toFixed(2)}`
                ];
              }
            }
          },
          legend: {
            display: false
          }
        }
      };
    });
    
    // 柱状图数据
    const barChartData = computed(() => {
      if (!report.value) return { labels: [], datasets: [] };
      
      // 统计各环数的射击次数
      const scoreCounts = Array(11).fill(0); // 0-10环
      report.value.records.forEach(record => {
        if (record.score >= 0 && record.score <= 10) {
          scoreCounts[record.score]++;
        }
      });
      
      return {
        labels: ['0环', '1环', '2环', '3环', '4环', '5环', '6环', '7环', '8环', '9环', '10环'],
        datasets: [{
          label: '射击次数',
          data: scoreCounts,
          backgroundColor: [
            'rgba(128, 128, 128, 0.7)', // 0环
            'rgba(128, 128, 128, 0.7)', // 1环
            'rgba(128, 128, 128, 0.7)', // 2环
            'rgba(128, 128, 128, 0.7)', // 3环
            'rgba(128, 128, 128, 0.7)', // 4环
            'rgba(128, 128, 128, 0.7)', // 5环
            'rgba(128, 128, 128, 0.7)', // 6环
            'rgba(0, 128, 0, 0.7)',     // 7环
            'rgba(255, 255, 0, 0.7)',   // 8环
            'rgba(255, 165, 0, 0.7)',   // 9环
            'rgba(255, 0, 0, 0.7)'      // 10环
          ]
        }]
      };
    });
    
    // 柱状图选项
    const barChartOptions = computed(() => {
      return {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: '射击次数'
            }
          }
        },
        plugins: {
          legend: {
            display: false
          }
        }
      };
    });
    
    // 折线图数据
    const lineChartData = computed(() => {
      if (!report.value) return { labels: [], datasets: [] };
      
      // 获取每次射击的环数和序号
      const scores = report.value.records.map(record => record.score);
      const labels = Array.from({ length: scores.length }, (_, i) => `#${i + 1}`);
      
      // 计算累计平均分
      const cumulativeAvg = [];
      let sum = 0;
      for (let i = 0; i < scores.length; i++) {
        sum += scores[i];
        cumulativeAvg.push(sum / (i + 1));
      }
      
      return {
        labels,
        datasets: [
          {
            label: '每次环数',
            data: scores,
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.1
          },
          {
            label: '累计平均分',
            data: cumulativeAvg,
            borderColor: 'rgba(255, 99, 132, 1)',
            backgroundColor: 'rgba(255, 99, 132, 0.2)',
            borderDash: [5, 5],
            tension: 0.1
          }
        ]
      };
    });
    
    // 折线图选项
    const lineChartOptions = computed(() => {
      return {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            min: 0,
            max: 10,
            title: {
              display: true,
              text: '环数'
            }
          }
        }
      };
    });
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadReportData();
    });
    
    return {
      report,
      isLoading,
      error,
      reportType,
      reportId,
      loadReportData,
      downloadPdf,
      formatDate,
      formatTime,
      getScoreClass,
      scatterChartData,
      scatterChartOptions,
      barChartData,
      barChartOptions,
      lineChartData,
      lineChartOptions
    };
  }
};
</script>

<style scoped>
.report-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 50px;
  margin: 20px 0;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.error {
  color: #f44336;
}

.report-header {
  text-align: center;
  margin-bottom: 30px;
}

.report-header h1 {
  margin-bottom: 10px;
  color: #333;
}

.date {
  color: #666;
  font-style: italic;
}

.stats-summary {
  margin-bottom: 40px;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.chart-container {
  margin-bottom: 40px;
}

.chart-container h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.chart-wrapper {
  height: 400px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.records-table-container {
  margin-bottom: 40px;
}

.records-table-container h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.records-table {
  width: 100%;
  border-collapse: collapse;
}

.records-table th,
.records-table td {
  padding: 10px;
  text-align: center;
  border-bottom: 1px solid #ddd;
}

.records-table th {
  background-color: #4CAF50;
  color: white;
  font-weight: bold;
}

.records-table tr:nth-child(even) {
  background-color: #f2f2f2;
}

.score-10 {
  font-weight: bold;
  color: #f44336;
}

.score-9 {
  font-weight: bold;
  color: #ff9800;
}

.score-8 {
  font-weight: bold;
  color: #ffc107;
}

.score-7 {
  font-weight: bold;
  color: #4caf50;
}

.score-low {
  color: #2196f3;
}

.actions {
  text-align: center;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #2196F3;
  color: white;
  text-decoration: none;
}

.btn:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .stats-card {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-wrapper {
    height: 300px;
  }
}
</style> 
