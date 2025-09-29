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
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="date" label="日期" />
              <el-table-column prop="status" label="状态" />
              <el-table-column label="操作">
                <template #default>
                  <el-button size="small" type="primary">查看</el-button>
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
              <el-button type="primary" @click="loadCompetitions">刷新</el-button>
            </div>
          </template>
          
          <div v-if="competitions.length > 0">
            <el-table :data="competitions" style="width: 100%">
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="createdAt" label="日期" />
              <el-table-column prop="status" label="状态" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="joinCompetition(scope.row)">参与</el-button>
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
import { getTrainingSessions } from '@/api/training';
import { getCompetitionList } from '@/api/competition';
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
        trainingSessions.value = response.sessions || [];
      } catch (error) {
        console.error('加载训练场次失败:', error);
      }
    };
    
    // 加载比赛列表
    const loadCompetitions = async () => {
      try {
        const response = await getCompetitionList();
        competitions.value = response.competitions || [];
      } catch (error) {
        console.error('加载比赛列表失败:', error);
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
    
    // 参与比赛
    const joinCompetition = (competition) => {
      if (competition && competition.id) {
        console.log('参与比赛:', competition);
        // 跳转到比赛页面
        router.push(`/competition/${competition.id}`);
      } else {
        console.error('比赛数据无效:', competition);
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
