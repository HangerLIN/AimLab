import axios from 'axios';
import { useUserStore } from '@/store/modules/user';
import router from '@/router';

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 尝试从 user store 获取 token
    const userStore = useUserStore();
    const token = userStore.token;
    
    // 如果 token 存在，在请求头中添加 Authorization
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  // 成功回调 - 提取 response.data
  response => {
    return response.data;
  },
  // 失败回调 - 处理错误
  error => {
    if (error.response) {
      // 处理 401 未授权错误
      if (error.response.status === 401) {
        // 清除用户 token
        const userStore = useUserStore();
        userStore.clearToken();
        
        // 跳转到登录页
        router.push('/login');
      } else {
        // 处理其他错误
        alert(error.message || '请求失败');
      }
    } else {
      // 处理网络错误
      alert('网络错误，请检查您的连接');
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 