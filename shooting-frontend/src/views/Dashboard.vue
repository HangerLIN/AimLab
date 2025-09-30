<template>
  <div class="dashboard-container">
    <h1>射击训练平台</h1>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>训练场次</h3>
              <el-button type="primary" @click="loadTrainingSessions">刷新</el-button>
            </div>
          </template>
          
          <div v-if="trainingSessions.length > 0">
            <el-table :data="trainingSessions" style="width: 100%">
              <el-table-column prop="sessionName" label="名称" />
              <el-table-column label="日期">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="scope">
                  <el-tag :type="getTrainingStatusType(scope.row)">
                    {{ getTrainingStatusText(scope.row) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="viewTraining(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无训练场次" />
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>比赛列表</h3>
              <div>
                <el-button type="success" @click="showCreateCompetition">创建比赛</el-button>
                <el-button type="primary" @click="loadCompetitions" style="margin-left: 10px;">刷新</el-button>
              </div>
            </div>
          </template>
          
          <div v-if="competitions.length > 0">
            <el-table :data="competitions" style="width: 100%">
              <el-table-column prop="name" label="名称" />
              <el-table-column label="日期">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="scope">
                  <el-tag :type="getCompetitionStatusType(scope.row.status)">
                    {{ getCompetitionStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="joinCompetition(scope.row)"
                    :disabled="scope.row.status === 'COMPLETED' || scope.row.status === 'CANCELED'"
                  >
                    参与
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无比赛" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>测试API加载和消息</h3>
            </div>
          </template>
          
          <div class="test-buttons">
            <el-button type="success" @click="testSuccessRequest">测试成功请求</el-button>
            <el-button type="danger" @click="testErrorRequest">测试错误请求</el-button>
            <el-button type="warning" @click="testBusinessError">测试业务错误</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getTrainingSessions } from '@/api/training';
import { getCompetitionList, registerForCompetition, getCurrentAthlete, createCompetition } from '@/api/competition';
import apiClient from '@/api/index';

export default {
  name: 'DashboardView',
  
  setup() {
    const router = useRouter();
    const trainingSessions = ref([]);
    const competitions = ref([]);
    
    // 自动登录函数
    const autoLogin = async () => {
      try {
        const response = await fetch('http://localhost:8083/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username: 'test',
            password: 'test123'
          })
        });
        
        const result = await response.json();
        if (result.success && result.tokenInfo) {
          // 保存token到localStorage
          localStorage.setItem('aimlab-token', result.tokenInfo.tokenValue);
          console.log('自动登录成功，token已保存');
        }
      } catch (error) {
        console.error('自动登录失败:', error);
      }
    };
    
    // 加载训练场次
    const loadTrainingSessions = async () => {
      try {
        const response = await getTrainingSessions();
        console.log('训练场次数据:', response);
        trainingSessions.value = response.sessions || [];
      } catch (error) {
        console.error('加载训练场次失败:', error);
        trainingSessions.value = [];
      }
    };
    
    // 加载比赛列表
    const loadCompetitions = async () => {
      try {
        const response = await getCompetitionList();
        console.log('比赛列表数据:', response);
        competitions.value = response.competitions || [];
      } catch (error) {
        console.error('加载比赛列表失败:', error);
        competitions.value = [];
      }
    };
    
    // 测试成功请求
    const testSuccessRequest = async () => {
      try {
        await apiClient.get('/test/success');
      } catch (error) {
        // 错误已在拦截器中处理
      }
    };
    
    // 测试错误请求
    const testErrorRequest = async () => {
      try {
        await apiClient.get('/test/error');
      } catch (error) {
        // 错误已在拦截器中处理
      }
    };
    
    // 测试业务错误
    const testBusinessError = async () => {
      try {
        await apiClient.get('/test/business-error');
      } catch (error) {
        // 错误已在拦截器中处理
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
          status: 'CREATED'
        };
        
        const response = await createCompetition(competitionData);
        
        if (response.success) {
          alert(`比赛创建成功！${response.message}`);
          // 重新加载比赛列表
          await loadCompetitions();
        } else {
          alert(`比赛创建失败：${response.message}`);
        }
      } catch (error) {
        console.error('创建比赛失败:', error);
        let errorMessage = '创建比赛失败';
        if (error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        alert(errorMessage);
      }
    };
    
    // 日期格式化
    const formatDate = (dateString) => {
      if (!dateString) return '--';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };
    
    // 训练状态类型
    const getTrainingStatusType = (session) => {
      if (!session.endTime) return 'warning'; // 进行中
      return 'success'; // 已完成
    };
    
    // 训练状态文本
    const getTrainingStatusText = (session) => {
      if (!session.endTime) return '进行中';
      return '已完成';
    };
    
    // 比赛状态类型
    const getCompetitionStatusType = (status) => {
      switch (status) {
        case 'CREATED': return 'info';
        case 'STARTED': return 'warning';
        case 'COMPLETED': return 'success';
        case 'CANCELED': return 'danger';
        default: return 'info';
      }
    };
    
    // 比赛状态文本
    const getCompetitionStatusText = (status) => {
      switch (status) {
        case 'CREATED': return '已创建';
        case 'STARTED': return '进行中';
        case 'COMPLETED': return '已完成';
        case 'CANCELED': return '已取消';
        default: return '未知';
      }
    };
    
    // 查看训练
    const viewTraining = (training) => {
      console.log('查看训练:', training);
      // 跳转到训练详情页面或训练页面
      router.push('/training');
    };
    
    // 参与比赛
    const joinCompetition = async (competition) => {
      if (!competition || !competition.id) {
        console.error('比赛数据无效:', competition);
        return;
      }
      
      try {
        // 获取当前用户的运动员信息
        const athleteResponse = await getCurrentAthlete();
        
        // 检查是否成功获取运动员信息
        if (!athleteResponse.success || !athleteResponse.athlete || !athleteResponse.athlete.id) {
          ElMessage.warning('请先创建运动员档案');
          // 跳转到档案页面
          router.push('/profile');
          return;
        }
        
        const athlete = athleteResponse.athlete;
        
        // 报名参赛
        const enrollResponse = await registerForCompetition(competition.id, [athlete.id]);
        
        if (enrollResponse.success) {
          ElMessage.success(`报名成功！${enrollResponse.message || ''}`);
          // 重新加载比赛列表以更新状态
          await loadCompetitions();
        // 跳转到比赛页面
        router.push(`/competition/${competition.id}`);
      } else {
          ElMessage.error(`报名失败：${enrollResponse.message || '未知错误'}`);
        }
      } catch (error) {
        console.error('参与比赛失败:', error);
        let errorMessage = '参与比赛失败';
        if (error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        alert(errorMessage);
      }
    };
    
    // 初始化：先自动登录，再加载数据
    const initializeApp = async () => {
      await autoLogin();
      await loadTrainingSessions();
      await loadCompetitions();
    };
    
    initializeApp();
    
    return {
      trainingSessions,
      competitions,
      loadTrainingSessions,
      loadCompetitions,
      joinCompetition,
      viewTraining,
      showCreateCompetition,
      formatDate,
      getTrainingStatusType,
      getTrainingStatusText,
      getCompetitionStatusType,
      getCompetitionStatusText,
      testSuccessRequest,
      testErrorRequest,
      testBusinessError
    };
  }
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.test-buttons {
  display: flex;
  gap: 10px;
}
</style>
