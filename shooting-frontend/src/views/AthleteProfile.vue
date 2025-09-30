<template>
  <div class="athlete-profile">
    <div class="profile-header">
      <h1 class="page-title">运动员基础信息</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 无档案提示 -->
    <div v-else-if="!profile" class="no-profile">
      <el-empty description="暂无运动员档案">
        <el-button type="primary" @click="showCreateDialog = true">创建档案</el-button>
      </el-empty>
    </div>

    <!-- 档案内容 -->
    <div v-else class="profile-content">
      <!-- 基本信息卡片 -->
      <div class="info-card main-card">
        <h2 class="card-title">运动员基础信息</h2>
        <div class="card-content">
          <div class="basic-info-section">
            <!-- 头像区域 -->
            <div class="avatar-section">
              <div class="avatar-container">
                <el-avatar :size="150" :src="avatarUrl" class="athlete-avatar">
                  <span class="avatar-text">{{ profile.name ? profile.name.charAt(0) : '?' }}</span>
                </el-avatar>
              </div>
              <div class="tags-container">
                <el-tag v-if="profile.level" type="danger" size="large" class="level-tag">
                  {{ profile.level }}
                </el-tag>
                <el-tag v-if="genderDisplay" type="primary" size="large">
                  {{ genderDisplay }}
                </el-tag>
              </div>
            </div>

            <!-- 详细信息区域 -->
            <div class="details-section">
              <div class="info-grid">
                <div class="info-item">
                  <label class="info-label">姓名：</label>
                  <span class="info-value">{{ profile.name || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label class="info-label">性别：</label>
                  <span class="info-value">{{ genderDisplay || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label class="info-label">出生日期：</label>
                  <span class="info-value">{{ formatDate(profile.birthDate) || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label class="info-label">年龄：</label>
                  <span class="info-value">{{ calculateAge(profile.birthDate) || '-' }}</span>
                </div>
                <div class="info-item">
                  <label class="info-label">等级：</label>
                  <span class="info-value highlight">{{ profile.level || '未评级' }}</span>
                </div>
                <div class="info-item">
                  <label class="info-label">注册时间：</label>
                  <span class="info-value">{{ formatDateTime(profile.createdAt) }}</span>
                </div>
              </div>
              
              <div class="action-buttons">
                <el-button type="primary" @click="showEditDialog = true">编辑档案</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计数据区域 -->
      <div class="statistics-section">
        <!-- 生涯统计 -->
        <div class="info-card stats-card">
          <h2 class="card-title">生涯统计</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon career">📊</div>
              <div class="stat-content">
                <div class="stat-value">{{ profile.careerTotalShots || 0 }}</div>
                <div class="stat-label">总射击次数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon career">🎯</div>
              <div class="stat-content">
                <div class="stat-value">{{ formatScore(profile.careerAverageScore) }}</div>
                <div class="stat-label">平均环数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon career">⭐</div>
              <div class="stat-content">
                <div class="stat-value">{{ formatScore(profile.careerBestScore) }}</div>
                <div class="stat-label">最高环数</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 比赛统计 -->
        <div class="info-card stats-card">
          <h2 class="card-title">比赛统计</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon competition">🏆</div>
              <div class="stat-content">
                <div class="stat-value">{{ profile.totalCompetitions || 0 }}</div>
                <div class="stat-label">参赛总数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon competition">🥇</div>
              <div class="stat-content">
                <div class="stat-value">{{ profile.competitionsWon || 0 }}</div>
                <div class="stat-label">冠军次数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon competition">🥉</div>
              <div class="stat-content">
                <div class="stat-value">{{ profile.competitionsTopThree || 0 }}</div>
                <div class="stat-label">前三名次数</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 训练统计 -->
        <div class="info-card stats-card">
          <h2 class="card-title">训练统计</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon training">📅</div>
              <div class="stat-content">
                <div class="stat-value">{{ profile.totalTrainingSessions || 0 }}</div>
                <div class="stat-label">训练场次</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon training">⏱️</div>
              <div class="stat-content">
                <div class="stat-value">{{ formatTrainingTime(profile.totalTrainingMinutes) }}</div>
                <div class="stat-label">训练时长</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon training">📈</div>
              <div class="stat-content">
                <div class="stat-value">{{ winRate }}%</div>
                <div class="stat-label">夺冠率</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑档案对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑运动员档案" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="editForm.birthDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="等级">
          <el-input v-model="editForm.level" placeholder="请输入等级" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 创建档案对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建运动员档案" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="createForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-select v-model="createForm.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="createForm.birthDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="等级">
          <el-input v-model="createForm.level" placeholder="请输入等级" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMyAthleteProfile, createAthleteProfile, updateAthleteProfile } from '@/api/athlete';

export default {
  name: 'AthleteProfile',
  
  setup() {
    const loading = ref(true);
    const profile = ref(null);
    const showEditDialog = ref(false);
    const showCreateDialog = ref(false);
    
    // 编辑表单
    const editForm = ref({
      name: '',
      gender: '',
      birthDate: '',
      level: ''
    });
    
    // 创建表单
    const createForm = ref({
      name: '',
      gender: '',
      birthDate: '',
      level: ''
    });
    
    // 默认头像（可以根据性别或其他属性自定义）
    const avatarUrl = computed(() => {
      // 这里可以根据实际情况返回头像URL
      return '';
    });
    
    // 性别显示
    const genderDisplay = computed(() => {
      const genderMap = {
        'MALE': '男',
        'FEMALE': '女',
        'UNKNOWN': '未知'
      };
      return profile.value ? genderMap[profile.value.gender] : '';
    });
    
    // 计算夺冠率
    const winRate = computed(() => {
      if (!profile.value || !profile.value.totalCompetitions || profile.value.totalCompetitions === 0) {
        return '0.00';
      }
      const rate = (profile.value.competitionsWon / profile.value.totalCompetitions) * 100;
      return rate.toFixed(2);
    });
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '';
      return new Date(date).toLocaleDateString('zh-CN');
    };
    
    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '';
      return new Date(datetime).toLocaleString('zh-CN');
    };
    
    // 计算年龄
    const calculateAge = (birthDate) => {
      if (!birthDate) return null;
      const today = new Date();
      const birth = new Date(birthDate);
      let age = today.getFullYear() - birth.getFullYear();
      const monthDiff = today.getMonth() - birth.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
      }
      return age + '岁';
    };
    
    // 格式化分数
    const formatScore = (score) => {
      if (score === null || score === undefined) return '-';
      return parseFloat(score).toFixed(1);
    };
    
    // 格式化训练时长
    const formatTrainingTime = (minutes) => {
      if (!minutes) return '0小时';
      const hours = Math.floor(minutes / 60);
      const mins = minutes % 60;
      if (hours === 0) return `${mins}分钟`;
      if (mins === 0) return `${hours}小时`;
      return `${hours}小时${mins}分钟`;
    };
    
    // 加载档案数据
    const loadProfile = async () => {
      loading.value = true;
      try {
        const response = await getMyAthleteProfile();
        console.log('获取档案响应:', response);
        
        // 检查响应的success字段
        if (response && response.success && response.profile) {
          console.log('档案数据:', response.profile);
          profile.value = response.profile;
        } else {
          // 档案不存在或其他业务错误
          console.log('档案不存在或响应格式错误');
          profile.value = null;
        }
      } catch (error) {
        console.error('加载运动员档案失败:', error);
        profile.value = null;
      } finally {
        loading.value = false;
      }
    };
    
    // 打开编辑对话框
    const openEditDialog = () => {
      editForm.value = {
        name: profile.value.name || '',
        gender: profile.value.gender || '',
        birthDate: profile.value.birthDate || '',
        level: profile.value.level || ''
      };
      showEditDialog.value = true;
    };
    
    // 处理更新
    const handleUpdate = async () => {
      try {
        const response = await updateAthleteProfile(editForm.value);
        if (response && response.success) {
          ElMessage.success('档案更新成功');
          showEditDialog.value = false;
          await loadProfile();
        } else {
          ElMessage.error(response.message || '档案更新失败');
        }
      } catch (error) {
        console.error('更新档案失败:', error);
        ElMessage.error('档案更新失败');
      }
    };
    
    // 处理创建
    const handleCreate = async () => {
      if (!createForm.value.name || !createForm.value.gender) {
        ElMessage.warning('请填写必填项');
        return;
      }
      
      try {
        const response = await createAthleteProfile(createForm.value);
        console.log('创建档案响应:', response);
        
        if (response && response.success) {
          ElMessage.success('档案创建成功');
          showCreateDialog.value = false;
          // 添加延迟以确保后端事务已提交
          setTimeout(async () => {
            await loadProfile();
          }, 500);
        } else {
          ElMessage.error(response.message || '档案创建失败');
        }
      } catch (error) {
        console.error('创建档案失败:', error);
        ElMessage.error('档案创建失败');
      }
    };
    
    // 监听编辑对话框变化
    const handleEditDialogChange = (val) => {
      if (val) {
        openEditDialog();
      }
    };
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadProfile();
    });
    
    return {
      loading,
      profile,
      showEditDialog,
      showCreateDialog,
      editForm,
      createForm,
      avatarUrl,
      genderDisplay,
      winRate,
      formatDate,
      formatDateTime,
      calculateAge,
      formatScore,
      formatTrainingTime,
      handleUpdate,
      handleCreate
    };
  }
};
</script>

<style scoped>
.athlete-profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.loading-container {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.no-profile {
  background: white;
  padding: 60px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0 0 24px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #4CAF50;
}

.basic-info-section {
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  min-width: 180px;
}

.avatar-container {
  position: relative;
}

.athlete-avatar {
  border: 4px solid #4CAF50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.avatar-text {
  font-size: 60px;
  font-weight: bold;
  color: white;
}

.tags-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.level-tag {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
}

.details-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-label {
  font-weight: 600;
  color: #666;
  min-width: 90px;
}

.info-value {
  color: #333;
  font-size: 15px;
}

.info-value.highlight {
  color: #4CAF50;
  font-weight: bold;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 12px;
}

.statistics-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.stats-card {
  padding: 24px;
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-icon.career {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.competition {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.training {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .statistics-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .basic-info-section {
    flex-direction: column;
    align-items: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .statistics-section {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    justify-content: center;
  }
}
</style> 