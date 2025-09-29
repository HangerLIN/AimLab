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
          实时连接已建立
        </span>
        <span v-else-if="competitionStore.status === 'connecting'" class="connecting">
          正在连接...
        </span>
        <span v-else class="disconnected">
          未连接
          <button @click="reconnect" class="btn btn-sm">重新连接</button>
        </span>
      </div>
      
      <!-- 比赛内容主体 -->
      <div class="main-content">
        <!-- 射击靶 -->
        <div class="target-section">
          <ShootingTarget 
            :records="competitionStore.records"
            :interactive="false"
            :size="350"
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
                <span class="stat-label">排名</span>
                <span class="stat-value">{{ competitionStore.currentUserRank || '-' }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">射击数</span>
                <span class="stat-value">{{ userShotCount }}</span>
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
    
    // 组件挂载时
    onMounted(async () => {
      // 加载比赛数据
      await loadCompetitionData();
      
      // 如果比赛正在进行中，连接WebSocket
      if (competitionStore.isCompetitionActive) {
        competitionStore.connectAndSubscribe(competitionId.value);
      }
    });
    
    // 组件卸载时
    onUnmounted(() => {
      // 断开WebSocket连接
      competitionStore.disconnect();
    });
    
    return {
      competitionStore,
      currentUserId,
      isAdmin,
      userShotCount,
      reloadData,
      reconnect,
      startCompetition,
      endCompetition,
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