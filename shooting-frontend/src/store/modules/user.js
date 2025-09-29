import { defineStore } from 'pinia';
import apiClient from '@/api';
import router from '@/router';

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    token: localStorage.getItem('user-token') || null,
    userInfo: JSON.parse(localStorage.getItem('user-info') || 'null') || {}
  }),
  
  // 计算属性
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  
  // 操作
  actions: {
    // 登录
    async login(credentials) {
      try {
        // 模拟 API 调用，使用硬编码的数据
        // 在实际项目中，这里应该调用真实的 API
        // const response = await apiClient.post('/auth/login', credentials);
        
        // 硬编码的响应数据
        const response = {
          token: 'mock-jwt-token-12345',
          userInfo: {
            id: 1,
            username: credentials.username,
            name: '模拟用户',
            role: 'athlete',
            avatar: 'https://randomuser.me/api/portraits/men/1.jpg'
          }
        };
        
        // 延迟 500ms 模拟网络请求
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 保存 token 和用户信息
        this.token = response.token;
        this.userInfo = response.userInfo;
        
        // 保存到本地存储
        localStorage.setItem('user-token', response.token);
        localStorage.setItem('user-info', JSON.stringify(response.userInfo));
        
        return true;
      } catch (error) {
        console.error('登录失败:', error);
        return false;
      }
    },
    
    // 登出
    logout() {
      // 清除状态
      this.token = null;
      this.userInfo = {};
      
      // 清除本地存储
      localStorage.removeItem('user-token');
      localStorage.removeItem('user-info');
      
      // 跳转到登录页
      router.push('/login');
    },
    
    // 清除 token（供 API 拦截器使用）
    clearToken() {
      this.token = null;
      localStorage.removeItem('user-token');
    }
  }
}); 