<template>
  <div class="training-page">
    <h1>射击训练</h1>
    
    <!-- 加载中状态 -->
    <div v-if="isLoadingSession" class="loading-session">
      <div class="loading-spinner"></div>
      <p>正在加载训练场次...</p>
    </div>
    
    <!-- 已完成的训练场次 - 显示结果 -->
    <div v-else-if="isCompletedSession" class="completed-training">
      <div class="session-result">
        <div class="result-header">
          <h2>{{ trainingStore.currentSession?.sessionName || trainingStore.currentSession?.name || '训练场次' }}</h2>
          <span class="status-badge completed">已完成</span>
        </div>
        
        <div class="result-time">
          <p>开始时间: {{ formatDateTime(trainingStore.currentSession?.startTime) }}</p>
          <p>结束时间: {{ formatDateTime(trainingStore.currentSession?.endTime) }}</p>
        </div>
        
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-label">射击次数:</span>
            <span class="stat-value">{{ trainingStore.totalShots }} 次</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总得分:</span>
            <span class="stat-value">{{ trainingStore.totalScore.toFixed(1) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">平均分数:</span>
            <span class="stat-value">{{ trainingStore.averageScore.toFixed(2) }}</span>
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
        
        <!-- 显示射击靶图（只读模式） -->
        <ShootingTarget 
          :records="trainingStore.currentRecords" 
          :readonly="true"
        />
        
        <div class="action-buttons">
          <button 
            @click="downloadReport" 
            class="btn btn-primary"
            :disabled="trainingStore.isLoading"
          >
            {{ trainingStore.isLoading ? '生成中...' : '下载训练报告' }}
          </button>
          <button 
            @click="goBack" 
            class="btn btn-secondary ml-2"
          >
            返回列表
          </button>
          <button 
            @click="startNewTraining" 
            class="btn btn-success ml-2"
          >
            开始新训练
          </button>
        </div>
      </div>
    </div>
    
    <!-- 未开始训练时显示表单 -->
    <div v-else-if="!trainingStore.isTraining" class="start-training-form">
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
        <span class="status-badge in-progress">进行中</span>
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
        @shot="handleAddRecord"
      />
      
      <!-- 最后得分显示 -->
      <div class="last-score-display" v-if="trainingStore.totalShots > 0">
        <h3>最后得分: <span class="score-value">{{ trainingStore.lastScore.toFixed(1) }}</span></h3>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <!-- 调试信息：显示当前 loading 状态 -->
        <div v-if="trainingStore.isLoading" class="loading-indicator">
          ⏳ 加载中...
        </div>
        <button 
          @click.stop.prevent="endTraining" 
          class="btn btn-danger"
          type="button"
        >
          {{ trainingStore.isLoading ? '处理中...' : '结束训练' }}
        </button>
        <button 
          v-if="trainingStore.totalShots > 0"
          @click.stop.prevent="downloadCurrentReport" 
          class="btn btn-secondary ml-2"
          :disabled="!trainingStore.currentSession?.id"
          type="button"
        >
          {{ trainingStore.isLoading ? '生成中...' : '下载训练报告' }}
        </button>
        <!-- 紧急重置按钮 -->
        <button 
          v-if="trainingStore.isLoading"
          @click.stop.prevent="resetLoadingState" 
          class="btn btn-warning ml-2"
          type="button"
        >
          重置状态
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTrainingStore } from '@/store/modules/training';
import ShootingTarget from '@/components/ShootingTarget.vue';

export default {
  name: 'TrainingView',
  
  components: {
    ShootingTarget
  },
  
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  
  setup(props) {
    // 获取路由
    const route = useRoute();
    const router = useRouter();
    
    // 获取训练状态管理
    const trainingStore = useTrainingStore();
    
    // 表单数据
    const sessionName = ref('');
    
    // 是否正在加载指定场次
    const isLoadingSession = ref(false);
    
    // 是否是已完成的训练场次
    const isCompletedSession = computed(() => {
      return trainingStore.currentSession && trainingStore.currentSession.endTime !== null;
    });
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return '--';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };
    
    // 加载指定的训练场次
    const loadSessionById = async (sessionId) => {
      if (!sessionId) return;
      
      isLoadingSession.value = true;
      try {
        await trainingStore.loadSession(sessionId);
      } catch (error) {
        console.error('加载训练场次失败:', error);
        alert('加载训练场次失败: ' + error.message);
        router.push('/');
      } finally {
        isLoadingSession.value = false;
      }
    };
    
    // 开始训练
    const startTraining = async () => {
      if (!sessionName.value.trim()) return;
      
      try {
        await trainingStore.startSession(sessionName.value);
        sessionName.value = ''; // 清空表单
      } catch (error) {
        console.error('开始训练失败:', error);
      }
    };
    
    // 处理添加射击记录
    const handleAddRecord = (record) => {
      trainingStore.addRecord(record);
    };
    
    // 结束训练
    const endTraining = async () => {
      console.log('🔴 结束训练按钮被点击');
      console.log('当前 isLoading 状态:', trainingStore.isLoading);
      console.log('当前 session:', trainingStore.currentSession);
      
      // 如果正在加载中，先重置状态
      if (trainingStore.isLoading) {
        console.log('⚠️ 检测到 isLoading 为 true，先重置');
        trainingStore.isLoading = false;
      }
      
      if (!trainingStore.currentSession?.id) {
        console.error('❌ 无法获取训练场次ID');
        alert('错误：无法获取当前训练场次ID。请刷新页面后重试。');
        return;
      }

      const sessionId = trainingStore.currentSession.id;
      const shotCount = trainingStore.totalShots;
      console.log('训练场次ID:', sessionId, '射击次数:', shotCount);

      if (!confirm('确定要结束当前训练吗？结束后将无法继续添加射击记录。')) {
        console.log('用户取消了结束训练');
        return;
      }
      
      try {
        console.log('正在调用 endSession...');
        await trainingStore.endSession();
        console.log('✅ endSession 调用成功');
        
        // 提示用户训练已结束，并询问是否下载报告
        if (shotCount > 0) {
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
        
        // 跳转回首页
        router.push('/');
      } catch (error) {
        console.error('❌ 结束训练失败:', error);
        alert('结束训练失败：' + error.message);
        // 确保重置 loading 状态
        trainingStore.isLoading = false;
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
    
    // 下载已完成训练的报告
    const downloadReport = async () => {
      if (!trainingStore.currentSession?.id) {
        alert('无法获取训练会话信息');
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
    
    // 返回列表
    const goBack = () => {
      trainingStore.resetSession();
      router.push('/');
    };
    
    // 开始新训练
    const startNewTraining = () => {
      trainingStore.resetSession();
      // 如果当前在带 id 的路由，跳转到不带 id 的路由
      if (route.params.id) {
        router.push('/training');
      }
    };
    
    // 重置 loading 状态（紧急按钮）
    const resetLoadingState = () => {
      console.log('🔄 手动重置 loading 状态');
      trainingStore.isLoading = false;
      trainingStore.error = null;
    };
    
    // 监听路由参数变化
    watch(() => route.params.id, (newId) => {
      if (newId) {
        loadSessionById(newId);
      } else {
        // 如果没有 id 参数，重置状态以显示开始新训练的表单
        trainingStore.resetSession();
      }
    });
    
    // 组件挂载时检查是否有 id 参数
    onMounted(() => {
      const sessionId = props.id || route.params.id;
      if (sessionId) {
        loadSessionById(sessionId);
      }
    });
    
    return {
      trainingStore,
      sessionName,
      isLoadingSession,
      isCompletedSession,
      formatDateTime,
      startTraining,
      handleAddRecord,
      endTraining,
      downloadCurrentReport,
      downloadReport,
      goBack,
      startNewTraining,
      resetLoadingState
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

.loading-session {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e5e7eb;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: #218838;
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
  width: 100%;
}

.session-info h2 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 28px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 15px;
}

.status-badge.in-progress {
  background-color: #fef3c7;
  color: #92400e;
}

.status-badge.completed {
  background-color: #dcfce7;
  color: #166534;
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

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
}

.loading-indicator {
  width: 100%;
  text-align: center;
  padding: 10px;
  background-color: #fff3cd;
  border-radius: 4px;
  margin-bottom: 10px;
  color: #856404;
  font-weight: bold;
}

/* 已完成训练的样式 */
.completed-training {
  width: 100%;
}

.session-result {
  background-color: #f8f9fa;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.result-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
}

.result-time {
  background-color: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.result-time p {
  margin: 5px 0;
  color: #6c757d;
  font-size: 14px;
}
</style> 