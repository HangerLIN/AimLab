<template>
  <div class="ranking-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">🏆 排行榜</span>
        </div>
      </template>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 全站排行 -->
        <el-tab-pane label="全站排行" name="overall">
          <div class="ranking-content">
            <el-table :data="overallRanking" stripe style="width: 100%">
              <el-table-column prop="rank" label="排名" width="60" align="center">
                <template #default="{ row }">
                  <div :class="getRankClass(row.rank)">
                    {{ row.rank }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="athleteName" label="运动员" min-width="120"></el-table-column>
              <el-table-column prop="athleteLevel" label="等级" width="100"></el-table-column>
              <el-table-column prop="gender" label="性别" width="80">
                <template #default="{ row }">
                  {{ row.gender === 'MALE' ? '男' : '女' }}
                </template>
              </el-table-column>
              <el-table-column prop="averageScore" label="平均分" width="100" align="center"></el-table-column>
              <el-table-column prop="maxScore" label="最高分" width="100" align="center"></el-table-column>
              <el-table-column prop="participationCount" label="参赛次数" width="100" align="center"></el-table-column>
              <el-table-column prop="totalScore" label="总分" width="100" align="center"></el-table-column>
              <el-table-column prop="winRate" label="胜率" width="80" align="center">
                <template #default="{ row }">
                  {{ row.winRate ? row.winRate + '%' : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 分类排行 -->
        <el-tab-pane label="分类排行" name="by-level">
          <div class="ranking-content">
            <div class="filter-group">
              <el-select v-model="selectedLevel" placeholder="选择运动员等级" @change="loadLevelRanking">
                <el-option label="国家级" value="国家级"></el-option>
                <el-option label="省级" value="省级"></el-option>
                <el-option label="市级" value="市级"></el-option>
                <el-option label="业余" value="业余"></el-option>
              </el-select>
            </div>
            <el-table :data="levelRanking" stripe style="width: 100%">
              <el-table-column prop="rank" label="排名" width="60" align="center">
                <template #default="{ row }">
                  <div :class="getRankClass(row.rank)">
                    {{ row.rank }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="athleteName" label="运动员" min-width="120"></el-table-column>
              <el-table-column prop="gender" label="性别" width="80">
                <template #default="{ row }">
                  {{ row.gender === 'MALE' ? '男' : '女' }}
                </template>
              </el-table-column>
              <el-table-column prop="averageScore" label="平均分" width="100" align="center"></el-table-column>
              <el-table-column prop="maxScore" label="最高分" width="100" align="center"></el-table-column>
              <el-table-column prop="participationCount" label="参赛次数" width="100" align="center"></el-table-column>
              <el-table-column prop="totalScore" label="总分" width="100" align="center"></el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 月度排行 -->
        <el-tab-pane label="月度排行" name="monthly">
          <div class="ranking-content">
            <div class="filter-group">
              <el-date-picker 
                v-model="monthlyDate" 
                type="month"
                placeholder="选择月份"
                @change="loadMonthlyRanking"
              ></el-date-picker>
            </div>
            <el-table :data="monthlyRanking" stripe style="width: 100%">
              <el-table-column prop="rank" label="排名" width="60" align="center">
                <template #default="{ row }">
                  <div :class="getRankClass(row.rank)">
                    {{ row.rank }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="athleteName" label="运动员" min-width="120"></el-table-column>
              <el-table-column prop="athleteLevel" label="等级" width="100"></el-table-column>
              <el-table-column prop="averageScore" label="平均分" width="100" align="center"></el-table-column>
              <el-table-column prop="maxScore" label="最高分" width="100" align="center"></el-table-column>
              <el-table-column prop="participationCount" label="参赛次数" width="100" align="center"></el-table-column>
              <el-table-column prop="totalScore" label="总分" width="100" align="center"></el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 加载状态 -->
    <el-dialog v-model="loading" :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <div style="text-align: center; padding: 20px;">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { rankingAPI } from '@/api/ranking'
import { ElMessage } from 'element-plus'

const activeTab = ref('overall')
const loading = ref(false)

const overallRanking = ref([])
const levelRanking = ref([])
const monthlyRanking = ref([])

const selectedLevel = ref('')
const monthlyDate = ref(new Date())

// 根据排名获取样式类
const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

// 加载全站排行
const loadOverallRanking = async () => {
  loading.value = true
  try {
    const response = await rankingAPI.getOverallRanking(100)
    overallRanking.value = response.data
  } catch (error) {
    ElMessage.error('加载排行榜失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载分类排行
const loadLevelRanking = async () => {
  if (!selectedLevel.value) {
    levelRanking.value = []
    return
  }
  loading.value = true
  try {
    const response = await rankingAPI.getRankingByLevel(selectedLevel.value, 50)
    levelRanking.value = response.data
  } catch (error) {
    ElMessage.error('加载分类排行失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载月度排行
const loadMonthlyRanking = async () => {
  if (!monthlyDate.value) return
  loading.value = true
  try {
    const year = monthlyDate.value.getFullYear()
    const month = monthlyDate.value.getMonth() + 1
    const response = await rankingAPI.getMonthlyRanking(year, month, 50)
    monthlyRanking.value = response.data
  } catch (error) {
    ElMessage.error('加载月度排行失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 标签页切换处理
const handleTabChange = (tab) => {
  if (tab === 'overall' && overallRanking.value.length === 0) {
    loadOverallRanking()
  } else if (tab === 'by-level' && levelRanking.value.length === 0) {
    selectedLevel.value = '国家级'
    loadLevelRanking()
  } else if (tab === 'monthly' && monthlyRanking.value.length === 0) {
    loadMonthlyRanking()
  }
}

// 页面加载时
onMounted(() => {
  loadOverallRanking()
})
</script>

<style scoped>
.ranking-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.ranking-content {
  padding: 20px;
}

.filter-group {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.rank-gold {
  color: #ffc900;
  font-weight: bold;
  font-size: 16px;
}

.rank-silver {
  color: #c0c0c0;
  font-weight: bold;
  font-size: 16px;
}

.rank-bronze {
  color: #cd7f32;
  font-weight: bold;
  font-size: 16px;
}

.rank-normal {
  font-size: 14px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
