<template>
  <div class="competition-page">
    <!-- 比赛信息头部 -->
    <div v-if="competitionStore.currentCompetition" class="competition-header">
      <h1>{{ competitionStore.currentCompetition.name }}</h1>
      <div class="competition-info">
        <span class="status" :class="competitionStore.isCompetitionActive ? 'active' : 'inactive'">
          {{ competitionStore.isCompetitionActive ? '进行中' : '未开始' }}
        </span>
        <span class="date">{{ formatDate(competitionStore.currentCompetition.startTime) }}</span>
      </div>
    </div>
    
    <!-- 加载中状态 -->
    <div v-if="competitionStore.isLoading" class="loading">
      加载中...
    </div>
    
    <!-- 错误信息 -->
    <div v-else-if="competitionStore.error" class="error">
      <p>{{ competitionStore.error }}</p>
      <button @click="reloadData" class="btn btn-primary">重试</button>
    </div>
    
    <!-- 比赛内容 -->
    <div v-else-if="competitionStore.currentCompetition" class="competition-content">
      <!-- 连接状态 -->
      <div class="connection-status" :class="competitionStore.status">
        <span v-if="competitionStore.status === 'connected'" class="connected">
          ✓ 实时连接已建立
        </span>
        <span v-else-if="competitionStore.status === 'connecting'" class="connecting">
          ⏳ 正在连接...
        </span>
        <span v-else class="disconnected">
          ✗ 未连接
          <button @click="reconnect" class="btn btn-sm">点击建立连接</button>
        </span>
      </div>
      
      <!-- 调试信息 -->
      <div v-if="showDebugInfo" class="debug-info">
        <p><strong>调试信息:</strong></p>
        <p>连接状态: {{ competitionStore.status }}</p>
        <p>比赛状态: {{ competitionStore.currentCompetition?.status }}</p>
        <p>是否活跃: {{ competitionStore.isCompetitionActive }}</p>
        <p>靶子可交互: {{ competitionStore.isCompetitionActive && !competitionStore.isLoading }}</p>
        <p>比赛ID: {{ competitionId }}</p>
        <p>当前用户ID: {{ currentUserId }}</p>
        <p>射击记录数: {{ competitionStore.records.length }}</p>
        <p>排名数据数: {{ competitionStore.ranking.length }}</p>
        <p>当前轮次: {{ competitionStore.currentRound }}</p>
      </div>
      
      <!-- 比赛内容主体 -->
      <div class="main-content">
        <!-- 射击靶 -->
        <div class="target-section">
          <ShootingTarget 
            :records="competitionStore.records"
            :interactive="competitionStore.isCompetitionActive && !competitionStore.isLoading"
            :size="350"
            @shot="handleShot"
          />
        </div>
        
        <!-- 排名区域 -->
        <div class="ranking-section">
          <RealTimeRanking 
            :rankingData="competitionStore.ranking"
            :currentUserId="currentUserId"
          />
          
          <!-- 个人统计 -->
          <div class="personal-stats">
            <h4>我的数据</h4>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">总分</span>
                <span class="stat-value">{{ competitionStore.currentUserScore }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">平均分</span>
                <span class="stat-value">{{ competitionStore.currentUserAverage }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">排名</span>
                <span class="stat-value">{{ competitionStore.currentUserRank || '-' }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">射击数</span>
                <span class="stat-value">{{ competitionStore.currentUserShots }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">当前轮次</span>
                <span class="stat-value">第 {{ competitionStore.currentRound }} 轮</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 管理员操作区 -->
      <div v-if="isAdmin" class="admin-controls">
        <button 
          v-if="!competitionStore.isCompetitionActive" 
          @click="startCompetition" 
          class="btn btn-success"
          :disabled="competitionStore.isLoading"
        >
          开始比赛
        </button>
        <button 
          v-else 
          @click="endCompetition" 
          class="btn btn-danger"
          :disabled="competitionStore.isLoading"
        >
          结束比赛
        </button>
      </div>
    </div>
    
    <!-- 比赛不存在 -->
    <div v-else class="not-found">
      <h2>比赛不存在或已被删除</h2>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useCompetitionStore } from '@/store/modules/competition';
import { useUserStore } from '@/store/modules/user';
import ShootingTarget from '@/components/ShootingTarget.vue';
import RealTimeRanking from '@/components/RealTimeRanking.vue';

export default {
  name: 'CompetitionView',
  
  components: {
    ShootingTarget,
    RealTimeRanking
  },
  
  setup() {
    const route = useRoute();
    const competitionStore = useCompetitionStore();
    const userStore = useUserStore();
    
    // 获取比赛ID
    const competitionId = computed(() => route.params.id);
    
    // 当前用户ID
    const currentUserId = computed(() => userStore.userInfo?.id || 1);
    
    // 是否为管理员
    const isAdmin = computed(() => userStore.userInfo?.role === 'admin');
    
    // 显示调试信息（按D键切换）
    const showDebugInfo = ref(false);
    
    // 用户射击数量
    const userShotCount = computed(() => {
      return competitionStore.records.filter(
        record => record.athleteId === currentUserId.value
      ).length;
    });
    
    // 加载比赛数据
    const loadCompetitionData = async () => {
      try {
        await competitionStore.fetchInitialData(competitionId.value);
      } catch (error) {
        console.error('加载比赛数据失败:', error);
        // 错误已在store中处理，不需要额外操作
      }
    };
    
    // 重新加载数据
    const reloadData = () => {
      competitionStore.clearError();
      loadCompetitionData();
    };
    
    // 重新连接WebSocket
    const reconnect = () => {
      competitionStore.connectAndSubscribe(competitionId.value);
    };
    
    // 开始比赛
    const startCompetition = async () => {
      try {
        await competitionStore.startCompetition(competitionId.value);
      } catch (error) {
        console.error('开始比赛失败:', error);
      }
    };
    
    // 结束比赛
    const endCompetition = async () => {
      try {
        await competitionStore.endCompetition(competitionId.value);
      } catch (error) {
        console.error('结束比赛失败:', error);
      }
    };
    
    // 处理射击
    const handleShot = async (shotData) => {
      console.log('🎯 收到射击事件:', shotData);
      console.log('📊 比赛状态:', competitionStore.currentCompetition?.status);
      console.log('✓ 比赛是否活跃:', competitionStore.isCompetitionActive);
      
      if (!competitionStore.isCompetitionActive) {
        console.warn('⚠️ 比赛未活跃，无法射击');
        ElMessage.warning('比赛尚未开始或已结束');
        return;
      }
      
      try {
        console.log('📤 开始提交射击记录...');
        await competitionStore.submitShot(competitionId.value, shotData);
        console.log('✅ 射击记录提交成功');
        ElMessage.success(`射击成功！得分：${shotData.score}环`);
      } catch (error) {
        console.error('❌ 射击记录失败:', error);
        ElMessage.error('射击记录失败：' + error.message);
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
    
    // 切换调试信息
    const toggleDebugInfo = (event) => {
      if (event.key === 'd' || event.key === 'D') {
        showDebugInfo.value = !showDebugInfo.value;
      }
    };
    
    // 组件挂载时
    onMounted(async () => {
      // 加载比赛数据
      await loadCompetitionData();
      
      // 如果比赛正在进行中，连接WebSocket
      if (competitionStore.isCompetitionActive) {
        competitionStore.connectAndSubscribe(competitionId.value);
      }
      
      // 添加键盘监听器
      window.addEventListener('keydown', toggleDebugInfo);
    });
    
    // 组件卸载时
    onUnmounted(() => {
      // 断开WebSocket连接
      competitionStore.disconnect();
      
      // 移除键盘监听器
      window.removeEventListener('keydown', toggleDebugInfo);
    });
    
    return {
      competitionStore,
      competitionId,
      currentUserId,
      isAdmin,
      showDebugInfo,
      userShotCount,
      reloadData,
      reconnect,
      startCompetition,
      endCompetition,
      handleShot,
      formatDate
    };
  }
};
</script>

<style scoped>
.competition-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.competition-header {
  margin-bottom: 30px;
  text-align: center;
}

.competition-header h1 {
  margin-bottom: 10px;
  color: #333;
}

.competition-info {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #666;
}

.status {
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 4px;
}

.status.active {
  background-color: #e8f5e9;
  color: #4CAF50;
}

.status.inactive {
  background-color: #f5f5f5;
  color: #9e9e9e;
}

.loading, .error, .not-found {
  text-align: center;
  padding: 50px;
  margin: 20px 0;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.error {
  color: #f44336;
}

.connection-status {
  text-align: center;
  margin-bottom: 20px;
  padding: 8px;
  border-radius: 4px;
}

.connection-status.connected {
  background-color: #e8f5e9;
  color: #4CAF50;
}

.connection-status.connecting {
  background-color: #fff8e1;
  color: #ff9800;
}

.connection-status.disconnected {
  background-color: #ffebee;
  color: #f44336;
}

.debug-info {
  margin: 20px auto;
  padding: 15px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  max-width: 600px;
  font-family: monospace;
  font-size: 12px;
}

.debug-info p {
  margin: 5px 0;
  color: #333;
}

.debug-info strong {
  color: #2196F3;
}

.main-content {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  margin-bottom: 30px;
}

.target-section {
  flex: 1;
  min-width: 350px;
  display: flex;
  justify-content: center;
}

.ranking-section {
  flex: 1;
  min-width: 350px;
}

.personal-stats {
  margin-top: 30px;
  padding: 15px;
  background-color: #e3f2fd;
  border-radius: 8px;
}

.personal-stats h4 {
  text-align: center;
  margin-bottom: 15px;
  color: #1976d2;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
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

.admin-controls {
  margin-top: 30px;
  text-align: center;
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

.btn-success {
  background-color: #4CAF50;
  color: white;
}

.btn-danger {
  background-color: #f44336;
  color: white;
}

.btn-sm {
  padding: 5px 10px;
  font-size: 14px;
}

.btn:hover {
  opacity: 0.9;
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .target-section, .ranking-section {
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style> 