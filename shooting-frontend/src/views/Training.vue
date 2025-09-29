<template>
  <div class="training-page">
    <h1>射击训练</h1>
    
    <!-- 未开始训练时显示表单 -->
    <div v-if="!trainingStore.isTraining" class="start-training-form">
      <div class="form-group">
        <label for="sessionName">训练名称</label>
        <input 
          type="text" 
          id="sessionName" 
          v-model="sessionName" 
          placeholder="请输入训练名称"
          class="form-control"
        >
      </div>
      
      <button 
        @click="startTraining" 
        class="btn btn-primary"
        :disabled="!sessionName.trim()"
      >
        开始新训练
      </button>
    </div>
    
    <!-- 训练进行中显示靶子 -->
    <div v-else class="training-in-progress">
      <!-- 错误消息显示 -->
      <div v-if="trainingStore.error" class="error-message">
        <p>❌ {{ trainingStore.error }}</p>
        <button @click="trainingStore.clearError" class="btn btn-sm">清除错误</button>
      </div>
      
      <div class="session-info">
        <h2>{{ trainingStore.currentSession?.sessionName || trainingStore.currentSession?.name || '射击训练' }}</h2>
        <div class="session-id" v-if="trainingStore.currentSession?.id">
          {{ trainingStore.currentSession.id }}
        </div>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-label">已记录射击:</span>
            <span class="stat-value">{{ trainingStore.totalShots }} 次</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">平均分数:</span>
            <span class="stat-value">{{ trainingStore.averageScore.toFixed(2) }}</span>
          </div>
          <div class="stat-item" v-if="trainingStore.totalShots > 0">
            <span class="stat-label">总得分:</span>
            <span class="stat-value">{{ trainingStore.totalScore.toFixed(1) }}</span>
          </div>
          <div class="stat-item" v-if="trainingStore.totalShots > 0">
            <span class="stat-label">最高得分:</span>
            <span class="stat-value highlight-max">{{ trainingStore.maxScore.toFixed(1) }}</span>
          </div>
          <div class="stat-item" v-if="trainingStore.totalShots > 0">
            <span class="stat-label">最低得分:</span>
            <span class="stat-value highlight-min">{{ trainingStore.minScore.toFixed(1) }}</span>
          </div>
          <div class="stat-item" v-if="trainingStore.totalShots > 0">
            <span class="stat-label">稳定性指数:</span>
            <span class="stat-value">{{ trainingStore.stabilityIndex.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <ShootingTarget 
        :records="trainingStore.currentRecords" 
        @add-record="handleAddRecord"
      />
      
      <!-- 最后得分显示 -->
      <div class="last-score-display" v-if="trainingStore.totalShots > 0">
        <h3>最后得分: <span class="score-value">{{ trainingStore.lastScore.toFixed(1) }}</span></h3>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button 
          @click="endTraining" 
          class="btn btn-danger"
          :disabled="trainingStore.isLoading"
        >
          {{ trainingStore.isLoading ? '处理中...' : '结束训练' }}
        </button>
        <button 
          v-if="trainingStore.totalShots > 0"
          @click="downloadCurrentReport" 
          class="btn btn-secondary ml-2"
          :disabled="trainingStore.isLoading || !trainingStore.currentSession?.id"
        >
          {{ trainingStore.isLoading ? '生成中...' : '下载训练报告' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useTrainingStore } from '@/store/modules/training';
import ShootingTarget from '@/components/ShootingTarget.vue';

export default {
  name: 'TrainingView',
  
  components: {
    ShootingTarget
  },
  
  setup() {
    // 获取训练状态管理
    const trainingStore = useTrainingStore();
    
    // 表单数据
    const sessionName = ref('');
    
    // 开始训练
    const startTraining = async () => {
      if (!sessionName.value.trim()) return;
      
      try {
        await trainingStore.startSession(sessionName.value);
        sessionName.value = ''; // 清空表单
      } catch (error) {
        console.error('开始训练失败:', error);
        // 这里可以添加错误提示
      }
    };
    
    // 处理添加射击记录
    const handleAddRecord = (record) => {
      trainingStore.addRecord(record);
    };
    
    // 结束训练
    const endTraining = async () => {
      if (!confirm('确定要结束当前训练吗？结束后将无法继续添加射击记录。')) {
        return;
      }
      
      try {
        const sessionId = trainingStore.currentSession?.id;
        await trainingStore.endSession();
        
        // 提示用户训练已结束，并询问是否下载报告
        if (sessionId && trainingStore.totalShots > 0) {
          const shouldDownload = confirm('训练已结束！是否立即下载训练报告？');
          if (shouldDownload) {
            try {
              await trainingStore.downloadReport(sessionId);
            } catch (error) {
              alert('下载报告失败：' + error.message);
            }
          }
        } else {
          alert('训练已结束！');
        }
      } catch (error) {
        console.error('结束训练失败:', error);
        alert('结束训练失败：' + error.message);
      }
    };
    
    // 下载当前训练报告
    const downloadCurrentReport = async () => {
      if (!trainingStore.currentSession?.id) {
        alert('无法获取当前训练会话信息');
        return;
      }
      
      try {
        await trainingStore.downloadReport(trainingStore.currentSession.id);
        alert('报告下载成功！');
      } catch (error) {
        console.error('下载报告失败:', error);
        alert('下载报告失败：' + error.message);
      }
    };
    
    return {
      trainingStore,
      sessionName,
      startTraining,
      handleAddRecord,
      endTraining,
      downloadCurrentReport
    };
  }
};
</script>

<style scoped>
.training-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.start-training-form {
  margin: 30px 0;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.form-group {
  margin-bottom: 15px;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
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
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-primary:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.btn-danger {
  background-color: #f44336;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #d32f2f;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #545b62;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ml-2 {
  margin-left: 10px;
}

.training-in-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.session-info {
  margin-bottom: 30px;
  text-align: center;
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.session-info h2 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 28px;
}

.session-id {
  font-size: 18px;
  color: #6c757d;
  font-weight: bold;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  background-color: white;
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.highlight-max {
  color: #28a745 !important;
}

.highlight-min {
  color: #dc3545 !important;
}

.last-score-display {
  margin: 30px 0;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  text-align: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.last-score-display h3 {
  margin: 0;
  font-size: 24px;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.mt-4 {
  margin-top: 20px;
}

.error-message {
  background-color: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 15px;
  color: #d00;
}

.error-message p {
  margin: 0 0 10px 0;
}

.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
}
</style> 