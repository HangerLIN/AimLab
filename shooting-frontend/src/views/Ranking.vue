<template>
  <div class="ranking-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <div class="trophy-glow"></div>
          <span class="trophy">🏆</span>
        </div>
        <div class="header-text">
          <h1>排行榜</h1>
          <p>记录每一次突破，见证荣耀时刻</p>
        </div>
      </div>
      <div class="header-stats">
        <div class="mini-stat">
          <span class="mini-value">{{ overallRanking.length }}</span>
          <span class="mini-label">参赛选手</span>
        </div>
        <div class="mini-stat">
          <span class="mini-value">{{ topScore }}</span>
          <span class="mini-label">最高分</span>
        </div>
      </div>
    </div>

    <!-- 排行榜类型切换 -->
    <div class="tab-container">
      <div class="tab-wrapper">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          :class="['tab-btn', { active: activeTab === tab.value }]"
          @click="switchTab(tab.value)"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-text">{{ tab.label }}</span>
        </button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <transition name="slide-fade">
      <div class="filter-section" v-if="activeTab !== 'overall'">
        <div class="filter-card">
          <template v-if="activeTab === 'by-level'">
            <div class="filter-item">
              <label>运动员等级</label>
              <el-select v-model="selectedLevel" placeholder="选择等级" @change="loadLevelRanking" class="custom-select">
                <el-option v-for="level in levels" :key="level" :label="level" :value="level">
                  <span class="level-option">
                    <span class="level-dot" :style="{ background: getLevelColor(level) }"></span>
                    {{ level }}
                  </span>
                </el-option>
              </el-select>
            </div>
          </template>
          <template v-if="activeTab === 'monthly'">
            <div class="filter-item">
              <label>选择月份</label>
              <el-date-picker 
                v-model="monthlyDate" 
                type="month"
                placeholder="选择月份"
                format="YYYY年MM月"
                value-format="YYYY-MM"
                @change="loadMonthlyRanking"
                class="custom-date-picker"
              />
            </div>
          </template>
        </div>
      </div>
    </transition>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 前三名展示 -->
      <div class="podium-section" v-if="currentRanking.length >= 3">
        <div class="podium">
          <!-- 第二名 -->
          <div class="podium-item second" @click="showAthleteDetail(currentRanking[1])">
            <div class="avatar-wrapper">
              <div class="avatar silver">
                <span>{{ getInitial(currentRanking[1]?.athleteName) }}</span>
              </div>
              <div class="medal">🥈</div>
            </div>
            <div class="podium-info">
              <h3>{{ currentRanking[1]?.athleteName }}</h3>
              <p class="score">{{ currentRanking[1]?.averageScore }} 分</p>
              <span class="level-tag">{{ currentRanking[1]?.athleteLevel }}</span>
            </div>
            <div class="podium-stand">
              <span class="rank-num">2</span>
            </div>
          </div>

          <!-- 第一名 -->
          <div class="podium-item first" @click="showAthleteDetail(currentRanking[0])">
            <div class="crown">👑</div>
            <div class="avatar-wrapper">
              <div class="avatar gold">
                <span>{{ getInitial(currentRanking[0]?.athleteName) }}</span>
              </div>
              <div class="medal">🥇</div>
            </div>
            <div class="podium-info">
              <h3>{{ currentRanking[0]?.athleteName }}</h3>
              <p class="score">{{ currentRanking[0]?.averageScore }} 分</p>
              <span class="level-tag">{{ currentRanking[0]?.athleteLevel }}</span>
            </div>
            <div class="podium-stand first-stand">
              <span class="rank-num">1</span>
            </div>
          </div>

          <!-- 第三名 -->
          <div class="podium-item third" @click="showAthleteDetail(currentRanking[2])">
            <div class="avatar-wrapper">
              <div class="avatar bronze">
                <span>{{ getInitial(currentRanking[2]?.athleteName) }}</span>
              </div>
              <div class="medal">🥉</div>
            </div>
            <div class="podium-info">
              <h3>{{ currentRanking[2]?.athleteName }}</h3>
              <p class="score">{{ currentRanking[2]?.averageScore }} 分</p>
              <span class="level-tag">{{ currentRanking[2]?.athleteLevel }}</span>
            </div>
            <div class="podium-stand">
              <span class="rank-num">3</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 排行榜列表 -->
      <div class="ranking-list-section">
        <div class="section-header">
          <h2>完整排名</h2>
          <div class="view-toggle">
            <button :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </button>
            <button :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </button>
          </div>
        </div>

        <!-- 列表视图 -->
        <transition-group name="list" tag="div" class="ranking-list" v-if="viewMode === 'list'">
          <div 
            v-for="(item, index) in currentRanking.slice(3)" 
            :key="item.athleteId"
            class="ranking-item"
            :style="{ '--delay': index * 0.05 + 's' }"
            @click="showAthleteDetail(item)"
          >
            <div class="rank-badge" :class="getRankClass(item.rank)">
              {{ item.rank }}
            </div>
            <div class="athlete-info">
              <div class="athlete-avatar" :style="{ background: getAvatarGradient(item.rank) }">
                {{ getInitial(item.athleteName) }}
              </div>
              <div class="athlete-details">
                <h4>{{ item.athleteName }}</h4>
                <div class="athlete-meta">
                  <span class="level">{{ item.athleteLevel }}</span>
                  <span class="gender">{{ item.gender === 'MALE' ? '♂' : '♀' }}</span>
                </div>
              </div>
            </div>
            <div class="score-section">
              <div class="main-score">
                <span class="score-value">{{ item.averageScore }}</span>
                <span class="score-label">平均分</span>
              </div>
              <div class="score-details">
                <div class="detail-item">
                  <span class="detail-value">{{ item.maxScore }}</span>
                  <span class="detail-label">最高</span>
                </div>
                <div class="detail-item">
                  <span class="detail-value">{{ item.participationCount }}</span>
                  <span class="detail-label">场次</span>
                </div>
                <div class="detail-item">
                  <span class="detail-value">{{ item.winRate || 0 }}%</span>
                  <span class="detail-label">胜率</span>
                </div>
              </div>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: getProgressWidth(item.averageScore) }"></div>
            </div>
          </div>
        </transition-group>

        <!-- 网格视图 -->
        <div class="ranking-grid" v-else>
          <div 
            v-for="(item, index) in currentRanking.slice(3)" 
            :key="item.athleteId"
            class="grid-item"
            :style="{ '--delay': index * 0.03 + 's' }"
            @click="showAthleteDetail(item)"
          >
            <div class="grid-rank">{{ item.rank }}</div>
            <div class="grid-avatar" :style="{ background: getAvatarGradient(item.rank) }">
              {{ getInitial(item.athleteName) }}
            </div>
            <h4>{{ item.athleteName }}</h4>
            <p class="grid-level">{{ item.athleteLevel }}</p>
            <div class="grid-score">{{ item.averageScore }}</div>
            <div class="grid-stats">
              <span>最高: {{ item.maxScore }}</span>
              <span>场次: {{ item.participationCount }}</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="currentRanking.length === 0 && !loading">
          <div class="empty-icon">📊</div>
          <h3>暂无排名数据</h3>
          <p>当前筛选条件下没有找到相关数据</p>
        </div>
      </div>
    </div>

    <!-- 加载遮罩 -->
    <transition name="fade">
      <div class="loading-overlay" v-if="loading">
        <div class="loader">
          <div class="loader-ring"></div>
          <span>加载中...</span>
        </div>
      </div>
    </transition>

    <!-- 运动员详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="selectedAthlete?.athleteName + ' 的详细信息'"
      width="500px"
      class="athlete-dialog"
    >
      <div class="athlete-detail" v-if="selectedAthlete">
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: getAvatarGradient(selectedAthlete.rank) }">
            {{ getInitial(selectedAthlete.athleteName) }}
          </div>
          <div class="detail-info">
            <h2>{{ selectedAthlete.athleteName }}</h2>
            <p>{{ selectedAthlete.athleteLevel }} · {{ selectedAthlete.gender === 'MALE' ? '男' : '女' }}</p>
          </div>
          <div class="detail-rank">
            <span class="rank-label">当前排名</span>
            <span class="rank-value">#{{ selectedAthlete.rank }}</span>
          </div>
        </div>
        <div class="detail-stats">
          <div class="stat-item">
            <div class="stat-icon">📈</div>
            <div class="stat-content">
              <span class="stat-value">{{ selectedAthlete.averageScore }}</span>
              <span class="stat-label">平均分</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🎯</div>
            <div class="stat-content">
              <span class="stat-value">{{ selectedAthlete.maxScore }}</span>
              <span class="stat-label">最高分</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🏅</div>
            <div class="stat-content">
              <span class="stat-value">{{ selectedAthlete.participationCount }}</span>
              <span class="stat-label">参赛次数</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🏆</div>
            <div class="stat-content">
              <span class="stat-value">{{ selectedAthlete.winRate || 0 }}%</span>
              <span class="stat-label">胜率</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { rankingAPI } from '@/api/ranking'
import { ElMessage } from 'element-plus'
import { List, Grid } from '@element-plus/icons-vue'

const activeTab = ref('overall')
const loading = ref(false)
const viewMode = ref('list')
const detailVisible = ref(false)
const selectedAthlete = ref(null)

const overallRanking = ref([])
const levelRanking = ref([])
const monthlyRanking = ref([])

const selectedLevel = ref('国家级')
const monthlyDate = ref(new Date())

const tabs = [
  { value: 'overall', label: '全站排行', icon: '🌍' },
  { value: 'by-level', label: '分类排行', icon: '📊' },
  { value: 'monthly', label: '月度排行', icon: '📅' }
]

const levels = ['国家级', '省级', '市级', '业余']

const currentRanking = computed(() => {
  switch (activeTab.value) {
    case 'overall': return overallRanking.value
    case 'by-level': return levelRanking.value
    case 'monthly': return monthlyRanking.value
    default: return []
  }
})

const topScore = computed(() => {
  if (overallRanking.value.length > 0) {
    return Math.max(...overallRanking.value.map(r => r.maxScore || 0))
  }
  return 0
})

const getInitial = (name) => {
  return name ? name.charAt(0) : '?'
}

const getRankClass = (rank) => {
  if (rank <= 3) return 'top-three'
  if (rank <= 10) return 'top-ten'
  return 'normal'
}

const getAvatarGradient = (rank) => {
  const gradients = [
    'linear-gradient(135deg, #FFD700 0%, #FFA500 100%)',
    'linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%)',
    'linear-gradient(135deg, #CD7F32 0%, #8B4513 100%)',
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  ]
  return gradients[Math.min(rank - 1, gradients.length - 1)] || gradients[gradients.length - 1]
}

const getLevelColor = (level) => {
  const colors = {
    '国家级': '#FFD700',
    '省级': '#C0C0C0',
    '市级': '#CD7F32',
    '业余': '#4facfe'
  }
  return colors[level] || '#667eea'
}

const getProgressWidth = (score) => {
  const maxScore = 10
  return Math.min((score / maxScore) * 100, 100) + '%'
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'overall' && overallRanking.value.length === 0) {
    loadOverallRanking()
  } else if (tab === 'by-level' && levelRanking.value.length === 0) {
    loadLevelRanking()
  } else if (tab === 'monthly' && monthlyRanking.value.length === 0) {
    loadMonthlyRanking()
  }
}

const showAthleteDetail = (athlete) => {
  selectedAthlete.value = athlete
  detailVisible.value = true
}

const loadOverallRanking = async () => {
  loading.value = true
  try {
    const response = await rankingAPI.getOverallRanking(100)
    overallRanking.value = response.data || response
  } catch (error) {
    ElMessage.error('加载排行榜失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadLevelRanking = async () => {
  if (!selectedLevel.value) return
  loading.value = true
  try {
    const response = await rankingAPI.getRankingByLevel(selectedLevel.value, 50)
    levelRanking.value = response.data || response
  } catch (error) {
    ElMessage.error('加载分类排行失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadMonthlyRanking = async () => {
  if (!monthlyDate.value) return
  loading.value = true
  try {
    const date = new Date(monthlyDate.value)
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const response = await rankingAPI.getMonthlyRanking(year, month, 50)
    monthlyRanking.value = response.data || response
  } catch (error) {
    ElMessage.error('加载月度排行失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOverallRanking()
})
</script>

<style scoped>
.ranking-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 50%, #dfe6f0 100%);
  padding: 20px;
  color: #333;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  background: linear-gradient(135deg, #fff 0%, #fefefe 100%);
  border-radius: 20px;
  margin-bottom: 30px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trophy-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,215,0,0.4) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.trophy {
  font-size: 48px;
  position: relative;
  z-index: 1;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.header-text h1 {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.header-text p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.header-stats {
  display: flex;
  gap: 30px;
}

.mini-stat {
  text-align: center;
  padding: 15px 25px;
  background: #f8f9fc;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.05);
}

.mini-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #FFD700;
}

.mini-label {
  font-size: 12px;
  color: #888;
}

/* 标签页切换 */
.tab-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.tab-wrapper {
  display: flex;
  gap: 10px;
  background: #fff;
  padding: 8px;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  background: transparent;
  color: #666;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.tab-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(102,126,234,0.4);
}

.tab-icon {
  font-size: 18px;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 30px;
}

.filter-card {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 12px;
  color: #888;
}

/* 领奖台 */
.podium-section {
  margin-bottom: 40px;
}

.podium {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 20px;
  padding: 40px 20px;
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.podium-item:hover {
  transform: translateY(-10px);
}

.podium-item.first {
  order: 2;
}

.podium-item.second {
  order: 1;
}

.podium-item.third {
  order: 3;
}

.crown {
  font-size: 32px;
  margin-bottom: 10px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 15px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

.avatar.gold {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  width: 100px;
  height: 100px;
  font-size: 40px;
}

.avatar.silver {
  background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%);
}

.avatar.bronze {
  background: linear-gradient(135deg, #CD7F32 0%, #8B4513 100%);
}

.medal {
  position: absolute;
  bottom: -5px;
  right: -5px;
  font-size: 24px;
}

.podium-info {
  text-align: center;
  margin-bottom: 15px;
}

.podium-info h3 {
  font-size: 18px;
  margin: 0 0 5px 0;
}

.podium-info .score {
  font-size: 24px;
  font-weight: 700;
  color: #FFD700;
  margin: 0 0 8px 0;
}

.level-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(0,0,0,0.05);
  border-radius: 20px;
  font-size: 12px;
  color: #666;
}

.podium-stand {
  width: 120px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px 10px 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.podium-stand.first-stand {
  width: 140px;
  height: 80px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
}

.rank-num {
  font-size: 28px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
}

/* 排行榜列表 */
.ranking-list-section {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-header h2 {
  font-size: 20px;
  margin: 0;
}

.view-toggle {
  display: flex;
  gap: 5px;
  background: #f5f5f5;
  padding: 5px;
  border-radius: 10px;
}

.view-toggle button {
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: #999;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-toggle button.active {
  background: #fff;
  color: #333;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

/* 列表项 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
  transform: translateX(-20px);
  position: relative;
  overflow: hidden;
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.ranking-item:hover {
  background: #fff;
  border-color: rgba(102,126,234,0.3);
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.rank-badge {
  width: 45px;
  height: 45px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  background: #e8eaed;
  color: #666;
}

.rank-badge.top-three {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
}

.rank-badge.top-ten {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.athlete-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.athlete-avatar {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

.athlete-details h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.athlete-meta {
  display: flex;
  gap: 10px;
  font-size: 13px;
  color: #888;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 30px;
}

.main-score {
  text-align: center;
}

.score-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #FFD700;
}

.score-label {
  font-size: 11px;
  color: #999;
}

.score-details {
  display: flex;
  gap: 20px;
}

.detail-item {
  text-align: center;
}

.detail-value {
  display: block;
  font-size: 16px;
  font-weight: 600;
}

.detail-label {
  font-size: 11px;
  color: #999;
}

.progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #e8eaed;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 1s ease;
}

/* 网格视图 */
.ranking-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.grid-item {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 25px;
  text-align: center;
  border: 1px solid rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeIn 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
  position: relative;
}

@keyframes fadeIn {
  to { opacity: 1; }
}

.grid-item:hover {
  background: #fff;
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.grid-rank {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 30px;
  height: 30px;
  background: #e8eaed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #666;
}

.grid-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.grid-item h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.grid-level {
  color: #888;
  font-size: 13px;
  margin: 0 0 15px 0;
}

.grid-score {
  font-size: 32px;
  font-weight: 700;
  color: #FFD700;
  margin-bottom: 10px;
}

.grid-stats {
  display: flex;
  justify-content: center;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.empty-state p {
  color: #888;
  margin: 0;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loader {
  text-align: center;
}

.loader-ring {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(0,0,0,0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 运动员详情弹窗 */
.athlete-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.detail-avatar {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
}

.detail-info h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.detail-info p {
  margin: 0;
  color: #666;
}

.detail-rank {
  margin-left: auto;
  text-align: center;
  padding: 15px 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  color: #fff;
}

.rank-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
}

.rank-value {
  font-size: 28px;
  font-weight: 700;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 15px;
}

.stat-icon {
  font-size: 28px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-content .stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-content .stat-label {
  font-size: 12px;
  color: #999;
}

/* 动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .header-stats {
    width: 100%;
    justify-content: center;
  }

  .podium {
    flex-direction: column;
    align-items: center;
  }

  .podium-item {
    order: unset !important;
  }

  .ranking-item {
    flex-wrap: wrap;
  }

  .score-section {
    width: 100%;
    justify-content: space-around;
    margin-top: 15px;
  }

  .tab-wrapper {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* 自定义 Element Plus 样式 */
:deep(.el-select) {
  --el-fill-color-blank: #fff;
  --el-text-color-regular: #333;
  --el-border-color: #dcdfe6;
}

:deep(.el-input__wrapper) {
  background: #fff !important;
  border-color: #dcdfe6 !important;
}

:deep(.el-input__inner) {
  color: #333 !important;
}

:deep(.el-date-editor) {
  --el-fill-color-blank: #fff;
}

:deep(.athlete-dialog .el-dialog) {
  border-radius: 20px;
}

:deep(.athlete-dialog .el-dialog__header) {
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
}

:deep(.athlete-dialog .el-dialog__body) {
  padding: 0;
}
</style>
