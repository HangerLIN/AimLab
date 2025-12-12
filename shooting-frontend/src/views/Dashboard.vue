<template>
  <div class="dashboard-container">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner animate-fade-in">
      <div class="banner-content">
        <div class="banner-text">
          <h1 class="banner-title">
            <span class="greeting">{{ greeting }}</span>
            <span class="wave-emoji">👋</span>
          </h1>
          <p class="banner-subtitle">欢迎使用射击训练平台，开始您的精准射击之旅</p>
        </div>
        <div class="banner-stats">
          <div class="stat-item">
            <div class="stat-icon training">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ trainingSessions.length }}</span>
              <span class="stat-label">训练场次</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon competition">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
                <path d="M4 22h16"/>
                <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
                <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ competitions.length }}</span>
              <span class="stat-label">比赛数量</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ activeCompetitions }}</span>
              <span class="stat-label">进行中</span>
            </div>
          </div>
        </div>
      </div>
      <div class="banner-decoration">
        <div class="target-rings">
          <div class="ring ring-1"></div>
          <div class="ring ring-2"></div>
          <div class="ring ring-3"></div>
          <div class="ring ring-center"></div>
        </div>
      </div>
    </section>

    <!-- 主要内容区域 -->
    <div class="dashboard-grid">
      <!-- 训练场次卡片 -->
      <section class="dashboard-card training-card animate-slide-up" style="animation-delay: 0.1s">
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon training">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <circle cx="12" cy="12" r="6"/>
                <circle cx="12" cy="12" r="2"/>
              </svg>
            </div>
            <div class="header-text">
              <h2>训练场次</h2>
              <p class="header-subtitle">管理您的训练记录</p>
            </div>
          </div>
          <button class="refresh-btn" @click="loadTrainingSessions" :class="{ loading: loadingTraining }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ spinning: loadingTraining }">
              <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
            </svg>
            <span>刷新</span>
          </button>
        </div>
        
        <div class="card-body">
          <div v-if="loadingTraining" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>
          <template v-else>
            <div v-if="trainingSessions.length > 0" class="training-list">
              <div 
                v-for="session in trainingSessions.slice(0, 5)" 
                :key="session.id" 
                class="training-item"
                @click="viewTraining(session)"
              >
                <div class="item-main">
                  <div class="item-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <polyline points="12 6 12 12 16 14"/>
                    </svg>
                  </div>
                  <div class="item-info">
                    <h4>{{ session.sessionName || '训练场次' }}</h4>
                    <span class="item-date">{{ formatDate(session.createdAt) }}</span>
                  </div>
                </div>
                <div class="item-status">
                  <span class="status-badge" :class="getTrainingStatusClass(session)">
                    {{ getTrainingStatusText(session) }}
                  </span>
                  <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <div class="empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10"/>
                  <circle cx="12" cy="12" r="6"/>
                  <circle cx="12" cy="12" r="2"/>
                </svg>
              </div>
              <h4>暂无训练记录</h4>
              <p>开始您的第一次训练吧</p>
              <router-link to="/training" class="start-btn">
                开始训练
              </router-link>
            </div>
          </template>
        </div>
      </section>

      <!-- 比赛列表卡片 -->
      <section class="dashboard-card competition-card animate-slide-up" style="animation-delay: 0.2s">
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon competition">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
                <path d="M4 22h16"/>
                <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
                <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>
              </svg>
            </div>
            <div class="header-text">
              <h2>比赛列表</h2>
              <p class="header-subtitle">参与和管理比赛</p>
            </div>
          </div>
          <div class="header-actions">
            <button class="action-btn primary" @click="showCreateCompetition">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              <span>创建比赛</span>
            </button>
            <button class="refresh-btn" @click="loadCompetitions" :class="{ loading: loadingCompetitions }">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ spinning: loadingCompetitions }">
                <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
                <path d="M21 3v5h-5"/>
              </svg>
            </button>
          </div>
        </div>
        
        <div class="card-body">
          <div v-if="loadingCompetitions" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>
          <template v-else>
            <div v-if="competitions.length > 0" class="competition-list">
              <div 
                v-for="competition in competitions" 
                :key="competition.id" 
                class="competition-item"
              >
                <div class="competition-main">
                  <div class="competition-info">
                    <h4 class="competition-name">{{ competition.name }}</h4>
                    <div class="competition-meta">
                      <span class="meta-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                          <line x1="16" y1="2" x2="16" y2="6"/>
                          <line x1="8" y1="2" x2="8" y2="6"/>
                          <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        {{ formatDate(competition.createdAt) }}
                      </span>
                      <span class="meta-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"/>
                          <polyline points="12 6 12 12 16 14"/>
                        </svg>
                        {{ competition.roundsCount || 3 }} 轮
                      </span>
                    </div>
                  </div>
                  <span class="status-badge" :class="getCompetitionStatusClass(competition.status)">
                    {{ getCompetitionStatusText(competition.status) }}
                  </span>
                </div>
                
                <div class="competition-actions">
                  <!-- 管理按钮组 -->
                  <div class="admin-actions" v-if="isCompetitionStatus(competition.status, 'CREATED')">
                    <button class="action-btn success small" @click="startCompetition(competition)">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polygon points="5 3 19 12 5 21 5 3"/>
                      </svg>
                      开始
                    </button>
                  </div>
                  
                  <div class="admin-actions" v-if="isCompetitionStatus(competition.status, 'RUNNING')">
                    <button class="action-btn warning small" @click="pauseCompetition(competition)">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="6" y="4" width="4" height="16"/>
                        <rect x="14" y="4" width="4" height="16"/>
                      </svg>
                      暂停
                    </button>
                    <button class="action-btn danger small" @click="completeCompetition(competition)">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                      </svg>
                      完成
                    </button>
                  </div>
                  
                  <div class="admin-actions" v-if="isCompetitionStatus(competition.status, 'PAUSED')">
                    <button class="action-btn info small" @click="resumeCompetition(competition)">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polygon points="5 3 19 12 5 21 5 3"/>
                      </svg>
                      恢复
                    </button>
                    <button class="action-btn danger small" @click="completeCompetition(competition)">
                      完成
                    </button>
                  </div>
                  
                  <!-- 参与按钮 -->
                  <button 
                    class="join-btn"
                    :class="{ 
                      enrolled: competition.isEnrolled,
                      disabled: isJoinButtonDisabled(competition)
                    }"
                    @click="joinCompetition(competition)"
                    :disabled="isJoinButtonDisabled(competition)"
                  >
                    <svg v-if="competition.isEnrolled" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                      <polyline points="10 17 15 12 10 7"/>
                      <line x1="15" y1="12" x2="3" y2="12"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                      <circle cx="8.5" cy="7" r="4"/>
                      <line x1="20" y1="8" x2="20" y2="14"/>
                      <line x1="23" y1="11" x2="17" y2="11"/>
                    </svg>
                    {{ getJoinButtonText(competition) }}
                  </button>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <div class="empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
                  <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
                  <path d="M4 22h16"/>
                  <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>
                </svg>
              </div>
              <h4>暂无比赛</h4>
              <p>创建一场新比赛开始竞技</p>
              <button class="start-btn" @click="showCreateCompetition">
                创建比赛
              </button>
            </div>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getTrainingSessions } from '@/api/training';
import { 
  getCompetitionList, 
  registerForCompetition, 
  getCurrentAthlete, 
  createCompetition,
  startCompetition as startCompetitionAPI,
  pauseCompetition as pauseCompetitionAPI,
  resumeCompetition as resumeCompetitionAPI,
  endCompetition
} from '@/api/competition';
import { 
  getCompetitionStatusMeta, 
  normalizeCompetitionStatus, 
  isCompetitionStatus 
} from '@/utils/competitionStatus';

export default {
  name: 'DashboardView',
  
  setup() {
    const router = useRouter();
    const trainingSessions = ref([]);
    const competitions = ref([]);
    const loadingTraining = ref(false);
    const loadingCompetitions = ref(false);
    
    // 问候语
    const greeting = computed(() => {
      const hour = new Date().getHours();
      if (hour < 6) return '夜深了';
      if (hour < 12) return '早上好';
      if (hour < 14) return '中午好';
      if (hour < 18) return '下午好';
      return '晚上好';
    });
    
    // 进行中的比赛数量
    const activeCompetitions = computed(() => {
      return competitions.value.filter(c => 
        isCompetitionStatus(c.status, ['RUNNING', 'PAUSED'])
      ).length;
    });
    
    // 加载训练场次
    const loadTrainingSessions = async () => {
      loadingTraining.value = true;
      try {
        const response = await getTrainingSessions();
        trainingSessions.value = response.sessions || [];
      } catch (error) {
        console.error('加载训练场次失败:', error);
        trainingSessions.value = [];
      } finally {
        loadingTraining.value = false;
      }
    };
    
    // 加载比赛列表
    const loadCompetitions = async () => {
      loadingCompetitions.value = true;
      try {
        const response = await getCompetitionList();
        competitions.value = response.competitions || [];
      } catch (error) {
        console.error('加载比赛列表失败:', error);
        competitions.value = [];
      } finally {
        loadingCompetitions.value = false;
      }
    };
    
    // 显示创建比赛对话框
    const showCreateCompetition = () => {
      const name = prompt('请输入比赛名称:');
      if (name && name.trim()) {
        createNewCompetition(name.trim());
      }
    };
    
    // 创建新比赛
    const createNewCompetition = async (name) => {
      try {
        const competitionData = {
          name: name,
          description: '射击训练测试比赛',
          status: 'CREATED',
          roundsCount: 3,
          shotsPerRound: 10,
          timeLimitPerShot: 60
        };
        
        const response = await createCompetition(competitionData);
        
        if (response.success) {
          ElMessage.success('比赛创建成功！');
          await loadCompetitions();
        } else {
          ElMessage.error(`比赛创建失败：${response.message}`);
        }
      } catch (error) {
        console.error('创建比赛失败:', error);
        ElMessage.error('创建比赛失败');
      }
    };
    
    // 日期格式化
    const formatDate = (dateString) => {
      if (!dateString) return '--';
      const date = new Date(dateString);
      const now = new Date();
      const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
      
      if (diffDays === 0) {
        return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
      } else if (diffDays === 1) {
        return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
      } else if (diffDays < 7) {
        return `${diffDays}天前`;
      }
      
      return date.toLocaleDateString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    };
    
    // 训练状态样式类
    const getTrainingStatusClass = (session) => {
      if (!session.endTime) return 'status-warning';
      return 'status-success';
    };
    
    // 训练状态文本
    const getTrainingStatusText = (session) => {
      if (!session.endTime) return '进行中';
      return '已完成';
    };
    
    // 比赛状态样式类
    const getCompetitionStatusClass = (status) => {
      const normalized = normalizeCompetitionStatus(status);
      const classMap = {
        'CREATED': 'status-info',
        'RUNNING': 'status-success',
        'PAUSED': 'status-warning',
        'COMPLETED': 'status-default',
        'CANCELED': 'status-danger'
      };
      return classMap[normalized] || 'status-default';
    };
    
    // 比赛状态文本
    const getCompetitionStatusText = (status) => {
      return getCompetitionStatusMeta(status).text;
    };
    
    // 查看训练
    const viewTraining = (training) => {
      if (!training || !training.id) {
        router.push('/training');
        return;
      }
      
      // 根据训练状态跳转到不同页面
      // 如果 endTime 为空，说明训练进行中；否则训练已完成
      router.push(`/training/${training.id}`);
    };
    
    // 参与比赛
    const joinCompetition = async (competition) => {
      if (!competition || !competition.id) return;
      
      if (competition.isEnrolled) {
        router.push(`/competition/${competition.id}`);
        return;
      }
      
      try {
        const athleteResponse = await getCurrentAthlete();
        
        if (!athleteResponse.success || !athleteResponse.athlete?.id) {
          ElMessage.warning('请先创建运动员档案');
          router.push('/profile');
          return;
        }
        
        const enrollResponse = await registerForCompetition(competition.id, [athleteResponse.athlete.id]);
        
        if (enrollResponse.success) {
          ElMessage.success('报名成功！');
          await loadCompetitions();
          router.push(`/competition/${competition.id}`);
        } else {
          ElMessage.error(`报名失败：${enrollResponse.message || '未知错误'}`);
        }
      } catch (error) {
        console.error('参与比赛失败:', error);
        ElMessage.error('参与比赛失败');
      }
    };
    
    // 获取参与按钮文本
    const getJoinButtonText = (competition) => {
      if (competition.isEnrolled) return '进入';
      const status = normalizeCompetitionStatus(competition.status);
      if (status === 'CREATED') return '报名';
      if (status === 'RUNNING' || status === 'PAUSED') return '已开始';
      if (status === 'COMPLETED') return '已结束';
      return '报名';
    };
    
    // 判断参与按钮是否禁用
    const isJoinButtonDisabled = (competition) => {
      if (competition.isEnrolled && competition.status !== 'CANCELED') return false;
      return !isCompetitionStatus(competition.status, 'CREATED');
    };
    
    // 开始比赛
    const startCompetition = async (competition) => {
      try {
        const response = await startCompetitionAPI(competition.id);
        if (response.success) {
          ElMessage.success(`比赛"${competition.name}"已开始！`);
          await loadCompetitions();
        } else {
          ElMessage.error(`开始比赛失败：${response.message}`);
        }
      } catch (error) {
        ElMessage.error('开始比赛失败');
      }
    };
    
    // 暂停比赛
    const pauseCompetition = async (competition) => {
      try {
        const response = await pauseCompetitionAPI(competition.id);
        if (response.success) {
          ElMessage.success(`比赛"${competition.name}"已暂停！`);
          await loadCompetitions();
        } else {
          ElMessage.error(`暂停比赛失败：${response.message}`);
        }
      } catch (error) {
        ElMessage.error('暂停比赛失败');
      }
    };
    
    // 恢复比赛
    const resumeCompetition = async (competition) => {
      try {
        const response = await resumeCompetitionAPI(competition.id);
        if (response.success) {
          ElMessage.success(`比赛"${competition.name}"已恢复！`);
          await loadCompetitions();
        } else {
          ElMessage.error(`恢复比赛失败：${response.message}`);
        }
      } catch (error) {
        ElMessage.error('恢复比赛失败');
      }
    };
    
    // 完成比赛
    const completeCompetition = async (competition) => {
      try {
        const response = await endCompetition(competition.id);
        if (response.success) {
          ElMessage.success(`比赛"${competition.name}"已完成！`);
          await loadCompetitions();
        } else {
          ElMessage.error(`完成比赛失败：${response.message}`);
        }
      } catch (error) {
        ElMessage.error('完成比赛失败');
      }
    };
    
    // 初始化
    onMounted(() => {
      loadTrainingSessions();
      loadCompetitions();
    });
    
    return {
      trainingSessions,
      competitions,
      loadingTraining,
      loadingCompetitions,
      greeting,
      activeCompetitions,
      loadTrainingSessions,
      loadCompetitions,
      showCreateCompetition,
      formatDate,
      getTrainingStatusClass,
      getTrainingStatusText,
      getCompetitionStatusClass,
      getCompetitionStatusText,
      viewTraining,
      joinCompetition,
      getJoinButtonText,
      isJoinButtonDisabled,
      isCompetitionStatus,
      startCompetition,
      pauseCompetition,
      resumeCompetition,
      completeCompetition
    };
  }
};
</script>

<style scoped>
/* ========== 容器 ========== */
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* ========== 欢迎横幅 ========== */
.welcome-banner {
  background: linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%);
  border-radius: 24px;
  padding: 40px;
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(16, 185, 129, 0.3);
}

.banner-content {
  position: relative;
  z-index: 2;
  flex: 1;
}

.banner-title {
  color: white;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.wave-emoji {
  display: inline-block;
  animation: wave 2s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-10deg); }
}

.banner-subtitle {
  color: rgba(255, 255, 255, 0.85);
  font-size: 1.1rem;
  margin-bottom: 32px;
}

.banner-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 16px 24px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.stat-icon.training { background: rgba(59, 130, 246, 0.3); }
.stat-icon.competition { background: rgba(245, 158, 11, 0.3); }
.stat-icon.active { background: rgba(239, 68, 68, 0.3); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  color: white;
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.875rem;
  margin-top: 4px;
}

/* 装饰性靶环 */
.banner-decoration {
  position: absolute;
  right: -60px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.15;
}

.target-rings {
  position: relative;
  width: 300px;
  height: 300px;
}

.ring {
  position: absolute;
  border: 3px solid white;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring-1 { width: 280px; height: 280px; }
.ring-2 { width: 200px; height: 200px; }
.ring-3 { width: 120px; height: 120px; }
.ring-center { 
  width: 40px; 
  height: 40px; 
  background: white;
}

/* ========== 主网格布局 ========== */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 24px;
}

/* ========== 卡片通用样式 ========== */
.dashboard-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.dashboard-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon svg {
  width: 24px;
  height: 24px;
}

.header-icon.training {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.header-icon.competition {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.header-text h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 4px 0 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 刷新按钮 */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f1f5f9;
  border: none;
  padding: 10px 16px;
  border-radius: 10px;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn:hover {
  background: #e2e8f0;
  color: #475569;
}

.refresh-btn svg {
  width: 18px;
  height: 18px;
}

.refresh-btn svg.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 操作按钮 */
.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn.primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.action-btn.success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
}

.action-btn.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.action-btn.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.action-btn.info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.action-btn.small {
  padding: 6px 12px;
  font-size: 0.8rem;
}

.action-btn.small svg {
  width: 14px;
  height: 14px;
}

/* ========== 卡片内容 ========== */
.card-body {
  padding: 24px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: #9ca3af;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: #f1f5f9;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-icon svg {
  width: 40px;
  height: 40px;
  color: #94a3b8;
}

.empty-state h4 {
  font-size: 1.1rem;
  color: #374151;
  margin-bottom: 8px;
}

.empty-state p {
  color: #9ca3af;
  margin-bottom: 20px;
}

.start-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 12px 28px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.25s ease;
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

/* ========== 训练列表 ========== */
.training-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.training-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.training-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.item-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.item-icon {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-icon svg {
  width: 20px;
  height: 20px;
  color: #2563eb;
}

.item-info h4 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.item-date {
  font-size: 0.8rem;
  color: #6b7280;
}

.item-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.arrow-icon {
  width: 18px;
  height: 18px;
  color: #9ca3af;
  transition: transform 0.25s ease;
}

.training-item:hover .arrow-icon {
  transform: translateX(4px);
  color: #6b7280;
}

/* ========== 比赛列表 ========== */
.competition-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.competition-item {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.25s ease;
}

.competition-item:hover {
  background: #f1f5f9;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.competition-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.competition-name {
  font-size: 1.05rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.competition-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: #6b7280;
}

.meta-item svg {
  width: 14px;
  height: 14px;
}

.competition-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.admin-actions {
  display: flex;
  gap: 8px;
}

/* 参与按钮 */
.join-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  border: 2px solid #10b981;
  background: white;
  color: #10b981;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-left: auto;
}

.join-btn svg {
  width: 18px;
  height: 18px;
}

.join-btn:hover:not(.disabled) {
  background: #10b981;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.join-btn.enrolled {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-color: transparent;
}

.join-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  border-color: #d1d5db;
  color: #9ca3af;
}

/* ========== 状态徽章 ========== */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-warning {
  background: #fef3c7;
  color: #92400e;
}

.status-danger {
  background: #fee2e2;
  color: #991b1b;
}

.status-info {
  background: #dbeafe;
  color: #1e40af;
}

.status-default {
  background: #f1f5f9;
  color: #475569;
}

/* ========== 动画 ========== */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

.animate-slide-up {
  animation: slideUp 0.5s ease-out both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(20px); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-banner {
    padding: 32px;
  }
  
  .banner-title {
    font-size: 2rem;
  }
  
  .banner-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-item {
    padding: 14px 20px;
  }
  
  .banner-decoration {
    display: none;
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 24px;
    border-radius: 20px;
  }
  
  .banner-title {
    font-size: 1.5rem;
  }
  
  .banner-subtitle {
    font-size: 0.95rem;
    margin-bottom: 24px;
  }
  
  .banner-stats {
    gap: 12px;
  }
  
  .stat-item {
    padding: 12px 16px;
    flex: 1;
    min-width: 100px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .competition-main {
    flex-direction: column;
    gap: 12px;
  }
  
  .competition-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .admin-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .join-btn {
    width: 100%;
    justify-content: center;
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 0;
  }
  
  .welcome-banner {
    border-radius: 0;
    margin-bottom: 20px;
  }
  
  .dashboard-card {
    border-radius: 0;
  }
  
  .banner-stats {
    flex-direction: column;
  }
  
  .stat-item {
    width: 100%;
  }
}
</style>
