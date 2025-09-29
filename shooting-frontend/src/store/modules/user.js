import { defineStore } from 'pinia';
import apiClient from '@/api';
import router from '@/router';

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    token: localStorage.getItem('aimlab-token') || null,
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
        // 调用实际的登录API
        const response = await fetch('http://localhost:8083/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username: credentials.username,
            password: credentials.password
          })
        });
        
        const result = await response.json();
        
        if (!result.success) {
          throw new Error(result.message || '登录失败');
        }
        
        // 保存 token 和用户信息
        this.token = result.tokenInfo.tokenValue;
        this.userInfo = {
          id: result.tokenInfo.loginId,
          username: credentials.username,
          name: credentials.username,
          role: 'athlete',
          tokenInfo: result.tokenInfo
        };
        
        // 保存到本地存储
        localStorage.setItem('aimlab-token', result.tokenInfo.tokenValue);
        localStorage.setItem('user-info', JSON.stringify(this.userInfo));
        
        return true;
      } catch (error) {
        console.error('登录失败:', error);
        throw error;
      }
    },
    
    // 登出
    logout() {
      // 清除状态
      this.token = null;
      this.userInfo = {};
      
      // 清除本地存储
      localStorage.removeItem('aimlab-token');
      localStorage.removeItem('user-info');
      
      // 跳转到登录页
      router.push('/login');
    },
    
    // 清除 token（供 API 拦截器使用）
    clearToken() {
      this.token = null;
      localStorage.removeItem('aimlab-token');
    }
  }
}); 