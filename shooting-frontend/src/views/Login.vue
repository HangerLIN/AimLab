<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>射击训练平台</h1>
        <p>{{ isRegisterMode ? '创建新账户' : '登录您的账户' }}</p>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
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
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item v-if="!isRegisterMode">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <el-link type="primary" class="forgot-password">忘记密码?</el-link>
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
      
      <div class="login-footer">
        <p v-if="!isRegisterMode">
          还没有账户? <el-link type="primary" @click="switchToRegister">注册</el-link>
        </p>
        <p v-else>
          已有账户? <el-link type="primary" @click="switchToLogin">登录</el-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
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
    const isLoading = ref(false);
    const isRegisterMode = ref(false);
    
    // 登录表单数据
    const loginForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      remember: false
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
    
    // 处理登录
    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      
      try {
        // 表单验证
        await loginFormRef.value.validate();
        
        // 开始登录
        isLoading.value = true;
        const success = await userStore.login({
          username: loginForm.username,
          password: loginForm.password
        });
        
        if (success) {
          ElMessage.success('登录成功');
          router.push('/');
        } else {
          ElMessage.error('登录失败，请检查用户名和密码');
        }
      } catch (error) {
        console.error('登录验证失败:', error);
      } finally {
        isLoading.value = false;
      }
    };
    
    // 处理注册
    const handleRegister = async () => {
      if (!loginFormRef.value) return;
      
      try {
        // 表单验证
        await loginFormRef.value.validate();
        
        // 开始注册
        isLoading.value = true;
        await userStore.register({
          username: loginForm.username,
          password: loginForm.password,
          name: loginForm.name
        });
        
        ElMessage.success('注册成功，请登录');
        
        // 切换到登录模式并清空表单
        switchToLogin();
      } catch (error) {
        console.error('注册失败:', error);
        ElMessage.error(error.message || '注册失败，请重试');
      } finally {
        isLoading.value = false;
      }
    };
    
    // 切换到注册模式
    const switchToRegister = () => {
      isRegisterMode.value = true;
      // 清空表单
      loginForm.username = '';
      loginForm.password = '';
      loginForm.confirmPassword = '';
      loginForm.name = '';
      loginForm.remember = false;
    };
    
    // 切换到登录模式
    const switchToLogin = () => {
      isRegisterMode.value = false;
      // 清空表单
      loginForm.username = '';
      loginForm.password = '';
      loginForm.confirmPassword = '';
      loginForm.name = '';
      loginForm.remember = false;
    };
    
    return {
      loginForm,
      loginFormRef,
      rules,
      isLoading,
      isRegisterMode,
      handleLogin,
      handleRegister,
      switchToRegister,
      switchToLogin
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
</style>
