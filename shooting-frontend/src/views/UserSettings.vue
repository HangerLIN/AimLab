<template>
  <div class="settings-container">
    <section class="page-header">
      <h1 class="page-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
        </svg>
        账户设置
      </h1>
      <p class="page-subtitle">管理您的账户安全和个人偏好</p>
    </section>

    <div class="settings-content">
      <!-- 密码修改卡片 -->
      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>
          <div class="card-title">
            <h3>修改密码</h3>
            <p>定期更换密码可以提高账户安全性</p>
          </div>
        </div>

        <el-form 
          ref="passwordFormRef" 
          :model="passwordForm" 
          :rules="passwordRules" 
          label-position="top"
          class="password-form"
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              placeholder="请输入当前密码"
              show-password
              size="large"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码（至少6位）"
              show-password
              size="large"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              show-password
              size="large"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleChangePassword" 
              :loading="isSubmitting"
              size="large"
            >
              {{ isSubmitting ? '提交中...' : '确认修改' }}
            </el-button>
            <el-button @click="resetPasswordForm" size="large">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 账户信息卡片 -->
      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon info">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <div class="card-title">
            <h3>账户信息</h3>
            <p>您的基本账户信息</p>
          </div>
        </div>

        <div class="account-info">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">角色</span>
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'success'" size="small">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '运动员' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">关联运动员</span>
            <span class="info-value">{{ userInfo.name || '未关联' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue';
import { useUserStore } from '@/store/modules/user';
import { ElMessage } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import apiClient from '@/api';

export default {
  name: 'UserSettings',
  
  components: {
    Lock
  },
  
  setup() {
    const userStore = useUserStore();
    const passwordFormRef = ref(null);
    const isSubmitting = ref(false);
    
    const userInfo = computed(() => userStore.userInfo);
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    };
    
    const handleChangePassword = async () => {
      if (!passwordFormRef.value) return;
      
      try {
        await passwordFormRef.value.validate();
        
        isSubmitting.value = true;
        
        const response = await apiClient.post('/auth/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
          confirmPassword: passwordForm.confirmPassword
        });
        
        if (response.success) {
          ElMessage.success('密码修改成功');
          resetPasswordForm();
        } else {
          ElMessage.error(response.message || '密码修改失败');
        }
      } catch (error) {
        if (error.message) {
          ElMessage.error(error.message);
        } else if (error.fieldErrors) {
          for (const [field, message] of Object.entries(error.fieldErrors)) {
            ElMessage.error(message);
          }
        } else {
          ElMessage.error('密码修改失败，请重试');
        }
      } finally {
        isSubmitting.value = false;
      }
    };
    
    const resetPasswordForm = () => {
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields();
      }
    };
    
    return {
      userInfo,
      passwordForm,
      passwordFormRef,
      passwordRules,
      isSubmitting,
      handleChangePassword,
      resetPasswordForm
    };
  }
};
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.title-icon {
  width: 32px;
  height: 32px;
  color: #10b981;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1rem;
  margin-top: 8px;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #dcfce7;
  color: #16a34a;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon.info {
  background: #dbeafe;
  color: #2563eb;
}

.card-icon svg {
  width: 24px;
  height: 24px;
}

.card-title h3 {
  margin: 0 0 4px 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
}

.card-title p {
  margin: 0;
  font-size: 0.875rem;
  color: #6b7280;
}

.password-form {
  max-width: 400px;
}

.account-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 0.875rem;
  color: #6b7280;
}

.info-value {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1f2937;
}

@media (max-width: 640px) {
  .settings-container {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .password-form {
    max-width: 100%;
  }
}
</style>
