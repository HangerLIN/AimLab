import axios from 'axios';
import { useUserStore } from '@/store/modules/user';
import router from '@/router';
import { ElLoading, ElMessage } from 'element-plus';

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// 加载实例
let loadingInstance;

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 显示全屏加载动画
    loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 尝试从 localStorage 获取 token
    const token = localStorage.getItem('aimlab-token');
    
    // 如果 token 存在，在请求头中添加 aimlab-token
    if (token) {
      config.headers['aimlab-token'] = token;
    }
    
    return config;
  },
  error => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close();
    }
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  // 成功回调 - 处理业务逻辑
  response => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close();
    }
    
    const res = response.data;
    
    // 直接返回响应数据，让组件自行处理success字段
    // 不在这里统一处理错误消息，给组件更多控制权
    return res;
  },
  // 失败回调 - 处理网络错误
  error => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close();
    }
    
    if (error.response) {
      // 对于400错误，如果有业务数据则直接返回，让组件自行处理
      if (error.response.status === 400 && error.response.data) {
        return error.response.data;
      }
      
      // 处理 401 未授权错误
      if (error.response.status === 401) {
        // 清除用户 token
        const userStore = useUserStore();
        userStore.clearToken();
        
        // 显示错误信息
        ElMessage.error('登录已过期，请重新登录');
        
        // 跳转到登录页
        router.push('/login');
      } else if (error.response.status === 500) {
        // 服务器错误
        ElMessage.error('服务器错误，请稍后再试');
      } else {
        // 处理其他错误
        ElMessage.error(error.response.data?.message || '请求失败');
      }
    } else if (error.request) {
      // 请求发送但没有收到响应
      ElMessage.error('网络错误，请检查您的连接');
    } else {
      // 请求设置时发生错误
      ElMessage.error('请求错误: ' + error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
