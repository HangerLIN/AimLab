import { defineStore } from 'pinia';
import { useUserStore } from './user';
import * as trainingAPI from '@/api/training';

export const useTrainingStore = defineStore('training', {
  // 状态
  state: () => ({
    currentSession: null,
    currentRecords: [],
    trainingHistory: [],
    trainingReport: null,
    isLoading: false,
    error: null
  }),
  
  // 计算属性
  getters: {
    isTraining: (state) => !!state.currentSession,
    
    // 获取当前会话的总射击数
    totalShots: (state) => state.currentRecords.length,
    
    // 获取当前会话的平均分数
    averageScore: (state) => {
      if (state.currentRecords.length === 0) return 0;
      const totalScore = state.currentRecords.reduce((sum, record) => sum + (record.score || 0), 0);
      return totalScore / state.currentRecords.length;
    }
  },
  
  // 操作
  actions: {
    /**
     * 开始新的训练场次
     * @param {string} name - 训练场次名称
     */
    async startSession(name) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await trainingAPI.startNewSession(name);
        this.currentSession = response;
        this.currentRecords = [];
        return response;
      } catch (error) {
        this.error = error.message || '开始训练失败';
        console.error('开始训练失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 添加训练射击记录
     * @param {Object} record - 射击记录数据
     */
    async addRecord(record) {
      if (!this.currentSession) {
        throw new Error('没有正在进行的训练场次');
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        // 补充训练场次ID和运动员ID
        const userStore = useUserStore();
        const completeRecord = {
          ...record,
          trainingSessionId: this.currentSession.id,
          athleteId: userStore.userInfo?.id || 1 // 如果没有用户ID，暂时使用1作为默认值
        };
        
        // 调用API添加记录
        const response = await trainingAPI.addTrainingRecord(completeRecord);
        
        // 将返回的记录添加到当前记录列表中
        this.currentRecords.push(response);
        
        return response;
      } catch (error) {
        this.error = error.message || '添加射击记录失败';
        console.error('添加射击记录失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 结束当前训练场次
     * @param {string} notes - 训练备注
     */
    async endSession(notes = '') {
      if (!this.currentSession) {
        throw new Error('没有正在进行的训练场次');
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await trainingAPI.endTrainingSession(this.currentSession.id, notes);
        
        // 将结束的训练添加到历史记录中
        this.trainingHistory.push({
          ...this.currentSession,
          endTime: new Date(),
          recordCount: this.currentRecords.length
        });
        
        // 清空当前会话
        this.currentSession = null;
        this.currentRecords = [];
        
        return response;
      } catch (error) {
        this.error = error.message || '结束训练失败';
        console.error('结束训练失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 获取训练报告
     * @param {number|string} sessionId - 训练场次ID
     */
    async getReport(sessionId) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const report = await trainingAPI.getTrainingReport(sessionId);
        this.trainingReport = report;
        return report;
      } catch (error) {
        this.error = error.message || '获取训练报告失败';
        console.error('获取训练报告失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 获取训练场次列表
     */
    async getSessionList() {
      this.isLoading = true;
      this.error = null;
      
      try {
        const sessions = await trainingAPI.getTrainingSessions();
        this.trainingHistory = sessions;
        return sessions;
      } catch (error) {
        this.error = error.message || '获取训练场次列表失败';
        console.error('获取训练场次列表失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 清除错误信息
     */
    clearError() {
      this.error = null;
    }
  }
}); 