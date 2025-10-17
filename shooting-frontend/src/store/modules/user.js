import { defineStore } from 'pinia';
import apiClient from '@/api';
import router from '@/router';

const normalizeUserInfo = (info) => {
  if (!info || typeof info !== 'object') {
    return {};
  }
  const normalized = { ...info };
  if (normalized.role) {
    normalized.role = String(normalized.role).toUpperCase();
  }
  if (!normalized.name) {
    normalized.name = normalized.username || '';
  }
  if (normalized.athleteId !== undefined && normalized.athleteId !== null) {
    const numericId = Number(normalized.athleteId);
    normalized.athleteId = Number.isNaN(numericId) ? normalized.athleteId : numericId;
  }
  return normalized;
};

const loadStoredUserInfo = () => {
  try {
    const stored = JSON.parse(localStorage.getItem('user-info') || 'null');
    return normalizeUserInfo(stored);
  } catch (error) {
    console.warn('读取本地用户信息失败:', error);
    return {};
  }
};

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    token: localStorage.getItem('aimlab-token') || null,
    userInfo: loadStoredUserInfo()
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
        // 调用实际的登录API（使用代理路径）
        const response = await fetch('/api/auth/login', {
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
        
        // 保存 token
        this.token = result.tokenInfo.tokenValue;
        
        // 先写入基础信息，确保页面立即可用
        const basicInfo = normalizeUserInfo({
          id: result.tokenInfo.loginId,
          username: credentials.username
        });
        this.userInfo = basicInfo;
        localStorage.setItem('user-info', JSON.stringify(basicInfo));
        
        // 保存到本地存储
        localStorage.setItem('aimlab-token', result.tokenInfo.tokenValue);
        
        // 加载用户详细信息
        await this.loadUserProfile();
        
        return true;
      } catch (error) {
        console.error('登录失败:', error);
        throw error;
      }
    },
    
    // 用户注册
    async register(credentials) {
      try {
        const response = await fetch('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username: credentials.username,
            password: credentials.password,
            name: credentials.name || credentials.username,
            role: 'ATHLETE'
          })
        });
        
        const result = await response.json();
        
        if (!result.success) {
          throw new Error(result.message || '注册失败');
        }
        
        return true;
      } catch (error) {
        console.error('注册失败:', error);
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
      this.userInfo = {};
      localStorage.removeItem('aimlab-token');
      localStorage.removeItem('user-info');
    },
    
    /**
     * 加载当前登录用户信息
     */
    async loadUserProfile() {
      if (!this.token) {
        return null;
      }
      
      try {
        const response = await apiClient.get('/users/me');
        
        if (response && response.success && response.user) {
          const normalized = normalizeUserInfo(response.user);
          this.userInfo = normalized;
          localStorage.setItem('user-info', JSON.stringify(normalized));
          return normalized;
        }
        
        return null;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return null;
      }
    }
  }
});
