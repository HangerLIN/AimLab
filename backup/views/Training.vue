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
      <div class="session-info">
        <h3>{{ trainingStore.currentSession?.name || '当前训练' }}</h3>
        <p>已记录射击: {{ trainingStore.totalShots }} 次</p>
        <p>平均分数: {{ trainingStore.averageScore.toFixed(2) }}</p>
      </div>
      
      <ShootingTarget 
        :records="trainingStore.currentRecords" 
        @add-record="handleAddRecord"
      />
      
      <button @click="endTraining" class="btn btn-danger mt-4">
        结束训练
      </button>
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
      try {
        await trainingStore.endSession();
        // 可以在这里添加成功提示或跳转到报告页面
      } catch (error) {
        console.error('结束训练失败:', error);
        // 这里可以添加错误提示
      }
    };
    
    return {
      trainingStore,
      sessionName,
      startTraining,
      handleAddRecord,
      endTraining
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

.btn-danger:hover {
  background-color: #d32f2f;
}

.training-in-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.session-info {
  margin-bottom: 20px;
  text-align: center;
}

.mt-4 {
  margin-top: 20px;
}
</style> 