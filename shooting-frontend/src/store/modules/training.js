import { defineStore } from 'pinia';
import { useUserStore } from './user';
import * as trainingAPI from '@/api/training';
import { toDisplayRecord, toDisplayRecords } from '@/utils/coordinates';

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
      const totalScore = state.currentRecords.reduce((sum, record) => sum + Number(record.score || 0), 0);
      return totalScore / state.currentRecords.length;
    },
    
    // 获取当前会话的总分数
    totalScore: (state) => {
      return state.currentRecords.reduce((sum, record) => sum + Number(record.score || 0), 0);
    },
    
    // 获取当前会话的最高分数
    maxScore: (state) => {
      if (state.currentRecords.length === 0) return 0;
      return Math.max(...state.currentRecords.map(record => Number(record.score || 0)));
    },
    
    // 获取当前会话的最低分数
    minScore: (state) => {
      if (state.currentRecords.length === 0) return 0;
      return Math.min(...state.currentRecords.map(record => Number(record.score || 0)));
    },
    
    // 获取最后一次射击的得分
    lastScore: (state) => {
      if (state.currentRecords.length === 0) return 0;
      const lastRecord = state.currentRecords[state.currentRecords.length - 1];
      return lastRecord ? Number(lastRecord.score || 0) : 0;
    },
    
    // 获取得分分布统计
    scoreDistribution: (state) => {
      const distribution = {};
      state.currentRecords.forEach(record => {
        const score = Math.floor(Number(record.score || 0));
        distribution[score] = (distribution[score] || 0) + 1;
      });
      return distribution;
    },
    
    // 获取稳定性指数（标准差）
    stabilityIndex: (state) => {
      if (state.currentRecords.length <= 1) return 0;
      
      const scores = state.currentRecords.map(record => Number(record.score || 0));
      const mean = scores.reduce((sum, score) => sum + score, 0) / scores.length;
      const squaredDiffs = scores.map(score => Math.pow(score - mean, 2));
      const variance = squaredDiffs.reduce((sum, diff) => sum + diff, 0) / scores.length;
      
      return Math.sqrt(variance);
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
        console.log('开始训练响应:', response);
        
        // 从响应中提取实际的训练场次数据
        // 优先使用 response.session，如果不存在则检查 response 本身是否包含 id
        let sessionData = response.session;
        if (!sessionData && response.id) {
          sessionData = response;
        }
        
        if (!sessionData || !sessionData.id) {
          console.error('无效的训练场次数据:', response);
          throw new Error('无法创建训练场次: 返回数据无效');
        }
        
        this.currentSession = sessionData;
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
      
      if (!this.currentSession.id) {
        throw new Error('训练场次ID不能为空');
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        // 将x和y坐标归一化到0-1范围（原始范围是0-200）
        const normalizedX = record.x / 200;
        const normalizedY = record.y / 200;
        
        // 补充必要的字段
        const userStore = useUserStore();
        const userId = userStore.userInfo?.id;
        if (!userId) {
          throw new Error('无法获取用户ID，请重新登录后再试');
        }
        const completeRecord = {
          recordType: 'TRAINING',
          trainingSessionId: this.currentSession.id,
          roundNumber: record.roundNumber || 1,
          shotNumber: record.shotNumber || (this.currentRecords.length + 1),
          x: normalizedX,
          y: normalizedY,
          score: record.score,
          userId
        };
        
        // 调用API添加记录
        const response = await trainingAPI.addTrainingRecord(completeRecord);
        
        // 从响应中提取实际的记录数据，并转换为展示坐标
        const recordData = response.record || response;
        const displayRecord = toDisplayRecord(recordData);
        
        // 将返回的记录添加到当前记录列表中
        this.currentRecords.push(displayRecord);
        
        return displayRecord;
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
      
      const sessionId = this.currentSession.id;
      if (!sessionId) {
        console.error('训练场次ID丢失:', this.currentSession);
        throw new Error('训练场次ID无效，无法结束训练');
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await trainingAPI.endTrainingSession(sessionId, notes);
        
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
        const displayReport = {
          ...report,
          records: toDisplayRecords(report?.records)
        };
        this.trainingReport = displayReport;
        return displayReport;
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
        const sessionsResponse = await trainingAPI.getTrainingSessions();
        const sessionList = sessionsResponse?.sessions || [];
        this.trainingHistory = sessionList;
        return sessionList;
      } catch (error) {
        this.error = error.message || '获取训练场次列表失败';
        console.error('获取训练场次列表失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 下载训练报告PDF
     * @param {number|string} sessionId - 训练场次ID
     */
    async downloadReport(sessionId) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await trainingAPI.downloadTrainingReportPdf(sessionId);
        
        // 创建下载链接
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `Training-Report-${sessionId}.pdf`;
        
        // 触发下载
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // 清理URL对象
        window.URL.revokeObjectURL(url);
        
        return true;
      } catch (error) {
        this.error = error.message || '下载训练报告失败';
        console.error('下载训练报告失败:', error);
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
    },
    
    /**
     * 加载指定的训练场次（用于从列表点击进入）
     * @param {number|string} sessionId - 训练场次ID
     */
    async loadSession(sessionId) {
      this.isLoading = true;
      this.error = null;
      
      try {
        // 获取训练场次详情
        const sessionResponse = await trainingAPI.getTrainingSessionDetail(sessionId);
        if (!sessionResponse.success) {
          throw new Error(sessionResponse.message || '获取训练场次失败');
        }
        
        const session = sessionResponse.session;
        this.currentSession = session;
        
        // 获取训练记录
        const recordsResponse = await trainingAPI.getTrainingRecords(sessionId);
        if (recordsResponse.success) {
          this.currentRecords = toDisplayRecords(recordsResponse.records || []);
        } else {
          this.currentRecords = [];
        }
        
        return session;
      } catch (error) {
        this.error = error.message || '加载训练场次失败';
        console.error('加载训练场次失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 检查训练场次是否已完成
     * @returns {boolean}
     */
    isSessionCompleted() {
      return this.currentSession && this.currentSession.endTime !== null;
    },
    
    /**
     * 重置当前状态（用于开始新训练）
     */
    resetSession() {
      this.currentSession = null;
      this.currentRecords = [];
      this.error = null;
    }
  }
});
