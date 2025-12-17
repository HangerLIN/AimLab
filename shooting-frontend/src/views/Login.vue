<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>射击训练平台</h1>
        <p>{{ getHeaderText }}</p>
      </div>
      
      <!-- 通用错误提示 -->
      <el-alert 
        v-if="generalError" 
        :title="generalError" 
        type="error"
        :closable="true"
        @close="generalError = ''"
        class="error-alert"
      />
      
      <!-- 成功提示 -->
      <el-alert 
        v-if="successMessage" 
        :title="successMessage" 
        type="success"
        :closable="true"
        @close="successMessage = ''"
        class="success-alert"
      />
      
      <!-- 登录/注册表单 -->
      <el-form 
        v-if="!isForgotPasswordMode" 
        :model="loginForm" 
        :rules="rules" 
        ref="loginFormRef" 
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <!-- 注册模式下的额外字段 -->
        <el-form-item v-if="isRegisterMode" prop="confirmPassword">
          <el-input 
            v-model="loginForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item v-if="isRegisterMode" prop="name">
          <el-input 
            v-model="loginForm.name"
            placeholder="真实姓名"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item v-if="!isRegisterMode">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <el-link type="primary" class="forgot-password" @click="switchToForgotPassword">忘记密码?</el-link>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="isLoading" 
            @click="isRegisterMode ? handleRegister() : handleLogin()" 
            class="login-button"
            size="large"
            round
          >
            {{ isLoading ? (isRegisterMode ? '注册中...' : '登录中...') : (isRegisterMode ? '注册' : '登录') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 忘记密码表单 -->
      <el-form 
        v-else 
        :model="forgotForm" 
        :rules="forgotRules" 
        ref="forgotFormRef" 
        class="login-form"
      >
        <!-- 步骤1：输入用户名 -->
        <template v-if="forgotStep === 1">
          <el-form-item prop="username">
            <el-input 
              v-model="forgotForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="isLoading" 
              @click="checkUsername"
              class="login-button"
              size="large"
              round
            >
              {{ isLoading ? '检查中...' : '下一步' }}
            </el-button>
          </el-form-item>
        </template>
        
        <!-- 步骤2：验证真实姓名（如果有）或直接重置 -->
        <template v-else-if="forgotStep === 2">
          <div class="forgot-info" v-if="needRealNameVerify">
            <p>请输入您注册时填写的真实姓名进行验证</p>
          </div>
          <div class="forgot-info" v-else>
            <p>该账户未设置真实姓名，将直接重置为默认密码</p>
          </div>
          
          <el-form-item v-if="needRealNameVerify" prop="realName">
            <el-input 
              v-model="forgotForm.realName"
              placeholder="请输入真实姓名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item v-if="needRealNameVerify" prop="newPassword">
            <el-input 
              v-model="forgotForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item v-if="needRealNameVerify" prop="confirmNewPassword">
            <el-input 
              v-model="forgotForm.confirmNewPassword"
              type="password"
              placeholder="请确认新密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="isLoading" 
              @click="handleResetPassword"
              class="login-button"
              size="large"
              round
            >
              {{ isLoading ? '重置中...' : (needRealNameVerify ? '重置密码' : '确认重置') }}
            </el-button>
          </el-form-item>
        </template>
      </el-form>
      
      <div class="login-footer">
        <template v-if="isForgotPasswordMode">
          <p><el-link type="primary" @click="switchToLogin">返回登录</el-link></p>
        </template>
        <template v-else-if="!isRegisterMode">
          <p>还没有账户? <el-link type="primary" @click="switchToRegister">注册</el-link></p>
        </template>
        <template v-else>
          <p>已有账户? <el-link type="primary" @click="switchToLogin">登录</el-link></p>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

export default {
  name: 'LoginView',
  
  components: {
    User,
    Lock
  },
  
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const loginFormRef = ref(null);
    const forgotFormRef = ref(null);
    const isLoading = ref(false);
    const isRegisterMode = ref(false);
    const isForgotPasswordMode = ref(false);
    const forgotStep = ref(1);
    const needRealNameVerify = ref(false);
    const generalError = ref('');
    const successMessage = ref('');
    
    // 登录表单数据
    const loginForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      remember: false
    });
    
    // 忘记密码表单数据
    const forgotForm = reactive({
      username: '',
      realName: '',
      newPassword: '',
      confirmNewPassword: ''
    });
    
    // 计算标题文本
    const getHeaderText = computed(() => {
      if (isForgotPasswordMode.value) {
        return forgotStep.value === 1 ? '找回密码' : '重置密码';
      }
      return isRegisterMode.value ? '创建新账户' : '登录您的账户';
    });
    
    // 表单验证规则
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== loginForm.password) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
      ]
    };
    
    // 忘记密码表单验证规则
    const forgotRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
      ],
      confirmNewPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== forgotForm.newPassword) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    };
    
    // 处理登录
    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      
      try {
        generalError.value = '';
        successMessage.value = '';
        
        await loginFormRef.value.validate();
        
        isLoading.value = true;
        const success = await userStore.login({
          username: loginForm.username,
          password: loginForm.password
        });
        
        if (success) {
          ElMessage.success('登录成功');
          router.push('/');
        }
      } catch (error) {
        if (error.fieldErrors) {
          for (const [field, message] of Object.entries(error.fieldErrors)) {
            ElMessage.error(message);
          }
        } else if (error.message) {
          generalError.value = error.message;
          ElMessage.error(error.message);
        } else {
          ElMessage.error('登录失败，请检查表单');
        }
      } finally {
        isLoading.value = false;
      }
    };
    
    // 处理注册
    const handleRegister = async () => {
      if (!loginFormRef.value) return;
      
      try {
        generalError.value = '';
        successMessage.value = '';
        
        await loginFormRef.value.validate();
        
        isLoading.value = true;
        await userStore.register({
          username: loginForm.username,
          password: loginForm.password,
          name: loginForm.name
        });
        
        ElMessage.success('注册成功，请登录');
        switchToLogin();
      } catch (error) {
        if (error.fieldErrors) {
          for (const [field, message] of Object.entries(error.fieldErrors)) {
            ElMessage.error(message);
          }
        } else if (error.message) {
          generalError.value = error.message;
          ElMessage.error(error.message);
        } else {
          ElMessage.error('注册失败，请重试');
        }
      } finally {
        isLoading.value = false;
      }
    };
    
    // 检查用户名（找回密码第一步）
    const checkUsername = async () => {
      if (!forgotForm.username.trim()) {
        ElMessage.error('请输入用户名');
        return;
      }
      
      try {
        isLoading.value = true;
        generalError.value = '';
        
        const response = await fetch('/api/auth/check-realname', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: forgotForm.username })
        });
        
        const result = await response.json();
        
        if (result.success) {
          needRealNameVerify.value = result.hasRealName;
          forgotStep.value = 2;
        } else {
          generalError.value = result.message || '用户不存在';
          ElMessage.error(result.message || '用户不存在');
        }
      } catch (error) {
        generalError.value = '检查失败，请重试';
        ElMessage.error('检查失败，请重试');
      } finally {
        isLoading.value = false;
      }
    };
    
    // 重置密码
    const handleResetPassword = async () => {
      try {
        isLoading.value = true;
        generalError.value = '';
        
        // 如果需要验证真实姓名，先验证表单
        if (needRealNameVerify.value) {
          if (!forgotForm.realName.trim()) {
            ElMessage.error('请输入真实姓名');
            isLoading.value = false;
            return;
          }
          if (!forgotForm.newPassword || forgotForm.newPassword.length < 6) {
            ElMessage.error('新密码长度至少为6个字符');
            isLoading.value = false;
            return;
          }
          if (forgotForm.newPassword !== forgotForm.confirmNewPassword) {
            ElMessage.error('两次输入的密码不一致');
            isLoading.value = false;
            return;
          }
        }
        
        const response = await fetch('/api/auth/reset-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: forgotForm.username,
            realName: needRealNameVerify.value ? forgotForm.realName : null,
            newPassword: needRealNameVerify.value ? forgotForm.newPassword : null
          })
        });
        
        const result = await response.json();
        
        if (result.success) {
          successMessage.value = result.message;
          ElMessage.success(result.message);
          
          // 延迟后返回登录页
          setTimeout(() => {
            switchToLogin();
          }, 2000);
        } else {
          generalError.value = result.message;
          ElMessage.error(result.message);
        }
      } catch (error) {
        generalError.value = '重置失败，请重试';
        ElMessage.error('重置失败，请重试');
      } finally {
        isLoading.value = false;
      }
    };
    
    // 切换到注册模式
    const switchToRegister = () => {
      isRegisterMode.value = true;
      isForgotPasswordMode.value = false;
      generalError.value = '';
      successMessage.value = '';
      resetForms();
    };
    
    // 切换到登录模式
    const switchToLogin = () => {
      isRegisterMode.value = false;
      isForgotPasswordMode.value = false;
      forgotStep.value = 1;
      generalError.value = '';
      successMessage.value = '';
      resetForms();
    };
    
    // 切换到忘记密码模式
    const switchToForgotPassword = () => {
      isForgotPasswordMode.value = true;
      isRegisterMode.value = false;
      forgotStep.value = 1;
      needRealNameVerify.value = false;
      generalError.value = '';
      successMessage.value = '';
      resetForms();
    };
    
    // 重置表单
    const resetForms = () => {
      loginForm.username = '';
      loginForm.password = '';
      loginForm.confirmPassword = '';
      loginForm.name = '';
      loginForm.remember = false;
      
      forgotForm.username = '';
      forgotForm.realName = '';
      forgotForm.newPassword = '';
      forgotForm.confirmNewPassword = '';
    };
    
    return {
      loginForm,
      forgotForm,
      loginFormRef,
      forgotFormRef,
      rules,
      forgotRules,
      isLoading,
      isRegisterMode,
      isForgotPasswordMode,
      forgotStep,
      needRealNameVerify,
      generalError,
      successMessage,
      getHeaderText,
      handleLogin,
      handleRegister,
      checkUsername,
      handleResetPassword,
      switchToRegister,
      switchToLogin,
      switchToForgotPassword
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  margin: 0;
  font-size: 28px;
  color: #4CAF50;
}

.login-header p {
  margin-top: 10px;
  color: #666;
}

.error-alert {
  margin-bottom: 20px;
}

.success-alert {
  margin-bottom: 20px;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  color: #666;
}

.forgot-password {
  float: right;
}

.forgot-info {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.forgot-info p {
  margin: 0;
  color: #1e40af;
  font-size: 14px;
}
</style>
