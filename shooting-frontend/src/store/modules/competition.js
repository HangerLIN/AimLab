import { defineStore } from 'pinia';
import * as competitionAPI from '@/api/competition';
import stompService from '@/websocket';

export const useCompetitionStore = defineStore('competition', {
  // 状态
  state: () => ({
    currentCompetition: null,
    records: [],
    ranking: [],
    status: 'disconnected', // 'disconnected' | 'connecting' | 'connected'
    error: null,
    isLoading: false,
    subscription: null,
    lastUpdateTime: null
  }),
  
  // 计算属性
  getters: {
    isConnected: (state) => state.status === 'connected',
    
    // 获取当前比赛的总射击数
    totalShots: (state) => state.records.length,
    
    // 获取当前用户的得分
    currentUserScore: (state) => {
      // 这里需要从用户store获取当前用户ID，暂时使用硬编码
      const currentUserId = 1; // 实际应用中应该从userStore获取
      
      const userRecords = state.records.filter(record => record.athleteId === currentUserId);
      if (userRecords.length === 0) return 0;
      
      const totalScore = userRecords.reduce((sum, record) => sum + (record.score || 0), 0);
      return totalScore;
    },
    
    // 获取当前用户的排名
    currentUserRank: (state) => {
      // 这里需要从用户store获取当前用户ID，暂时使用硬编码
      const currentUserId = 1; // 实际应用中应该从userStore获取
      
      const userRank = state.ranking.findIndex(rank => rank.athleteId === currentUserId);
      return userRank >= 0 ? userRank + 1 : null;
    },
    
    // 比赛是否正在进行中
    isCompetitionActive: (state) => {
      if (!state.currentCompetition) return false;
      // 检查多种可能的活跃状态
      const activeStatuses = ['ACTIVE', 'STARTED', 'RUNNING'];
      return activeStatuses.includes(state.currentCompetition.status);
    }
  },
  
  // 操作
  actions: {
    /**
     * 获取比赛初始数据
     * @param {number|string} id - 比赛ID
     */
    async fetchInitialData(id) {
      this.isLoading = true;
      this.error = null;
      
      try {
        // 获取比赛详情
        const competitionResponse = await competitionAPI.getCompetitionDetails(id);
        this.currentCompetition = competitionResponse.competition || competitionResponse;
        
        // 获取初始排名（可能为空，如果比赛未开始）
        try {
          const rankingResponse = await competitionAPI.getLiveRanking(id);
          this.ranking = rankingResponse.rankings || rankingResponse.ranking || rankingResponse || [];
        } catch (rankingError) {
          console.log('排名数据暂不可用（比赛可能未开始）:', rankingError.message);
          this.ranking = [];
        }
        
        // 获取比赛状态（可能为空，如果比赛未开始或已结束）
        try {
          const statusResponse = await competitionAPI.getCompetitionStatus(id);
          const status = statusResponse.status || statusResponse;
          
          // 如果比赛正在进行中，连接WebSocket
          if (status && (status.status === 'STARTED' || status.status === 'ACTIVE')) {
            this.connectAndSubscribe(id);
          }
        } catch (statusError) {
          console.log('比赛状态获取失败（比赛可能未开始或已结束）:', statusError.message);
          // 不处理为错误，因为比赛可能确实没有开始
        }
        
        // 更新最后更新时间
        this.lastUpdateTime = new Date();
        
        return { competition: this.currentCompetition, ranking: this.ranking };
      } catch (error) {
        // 如果获取比赛详情失败，这是严重错误
        if (error.response && error.response.status === 404) {
          this.error = '比赛不存在';
        } else {
          this.error = error.message || '获取比赛数据失败';
        }
        console.error('获取比赛数据失败:', error);
        // 不要重新抛出错误，让页面能够显示错误信息
        return { competition: null, ranking: [] };
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 连接WebSocket并订阅比赛主题
     * @param {number|string} id - 比赛ID
     */
    connectAndSubscribe(id) {
      // 如果已经连接，先断开
      if (this.status === 'connected') {
        this.disconnect();
      }
      
      // 更新状态为连接中
      this.status = 'connecting';
      
      // 连接WebSocket
      stompService.connect(() => {
        // 连接成功后，更新状态
        this.status = 'connected';
        
        // 订阅比赛主题
        this.subscription = stompService.subscribe(`/topic/competition/${id}`, (message) => {
          // 处理收到的消息
          this.handleCompetitionMessage(message);
        });
        
        // 订阅比赛状态变更主题
        stompService.subscribe(`/topic/competition/${id}/status`, (message) => {
          // 处理比赛状态变更
          this.handleStatusChange(message);
        });
        
        console.log(`已连接并订阅比赛 ${id} 的WebSocket主题`);
      });
    },
    
    /**
     * 处理比赛WebSocket消息
     * @param {Object} message - 收到的消息
     */
    handleCompetitionMessage(message) {
      try {
        // 如果消息是新的射击记录
        if (message.type === 'SHOT_RECORD') {
          // 添加新记录
          this.records.push(message.data);
          
          // 更新排名
          this.updateRanking();
        } 
        // 如果消息是排名更新
        else if (message.type === 'RANKING_UPDATE') {
          // 直接更新排名
          this.ranking = message.data;
        }
        
        // 更新最后更新时间
        this.lastUpdateTime = new Date();
      } catch (error) {
        console.error('处理比赛WebSocket消息失败:', error);
      }
    },
    
    /**
     * 处理比赛状态变更
     * @param {Object} message - 状态变更消息
     */
    handleStatusChange(message) {
      try {
        // 更新比赛状态
        if (this.currentCompetition) {
          this.currentCompetition.status = message.status;
        }
        
        // 如果比赛已结束，断开连接
        if (message.status === 'FINISHED') {
          this.disconnect();
          
          // 获取最终结果
          if (this.currentCompetition) {
            this.fetchFinalResults(this.currentCompetition.id);
          }
        }
      } catch (error) {
        console.error('处理比赛状态变更失败:', error);
      }
    },
    
    /**
     * 更新排名
     */
    updateRanking() {
      // 如果没有记录，不更新排名
      if (this.records.length === 0) return;
      
      // 按运动员ID分组记录
      const recordsByAthlete = {};
      
      // 遍历所有记录，按运动员ID分组
      this.records.forEach(record => {
        if (!recordsByAthlete[record.athleteId]) {
          recordsByAthlete[record.athleteId] = [];
        }
        recordsByAthlete[record.athleteId].push(record);
      });
      
      // 计算每个运动员的总分和平均分
      const athleteScores = Object.keys(recordsByAthlete).map(athleteId => {
        const records = recordsByAthlete[athleteId];
        const totalScore = records.reduce((sum, record) => sum + (record.score || 0), 0);
        const averageScore = totalScore / records.length;
        
        return {
          athleteId: parseInt(athleteId),
          name: records[0].athleteName || `运动员${athleteId}`,
          totalScore,
          averageScore,
          shotCount: records.length
        };
      });
      
      // 按总分降序排序
      athleteScores.sort((a, b) => b.totalScore - a.totalScore);
      
      // 更新排名
      this.ranking = athleteScores;
    },
    
    /**
     * 获取最终比赛结果
     * @param {number|string} id - 比赛ID
     */
    async fetchFinalResults(id) {
      try {
        const results = await competitionAPI.getCompetitionResults(id);
        // 更新排名为最终结果
        this.ranking = results;
        return results;
      } catch (error) {
        console.error('获取比赛最终结果失败:', error);
        throw error;
      }
    },
    
    /**
     * 开始比赛
     * @param {number|string} id - 比赛ID
     */
    async startCompetition(id) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await competitionAPI.startCompetition(id);
        
        // 更新比赛状态
        if (this.currentCompetition && this.currentCompetition.id === id) {
          this.currentCompetition.status = 'ACTIVE';
        }
        
        // 连接WebSocket
        this.connectAndSubscribe(id);
        
        return true;
      } catch (error) {
        this.error = error.message || '开始比赛失败';
        console.error('开始比赛失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 结束比赛
     * @param {number|string} id - 比赛ID
     */
    async endCompetition(id) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await competitionAPI.endCompetition(id);
        
        // 更新比赛状态
        if (this.currentCompetition && this.currentCompetition.id === id) {
          this.currentCompetition.status = 'FINISHED';
        }
        
        // 断开WebSocket连接
        this.disconnect();
        
        // 获取最终结果
        await this.fetchFinalResults(id);
        
        return true;
      } catch (error) {
        this.error = error.message || '结束比赛失败';
        console.error('结束比赛失败:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    /**
     * 断开WebSocket连接
     */
    disconnect() {
      if (this.subscription) {
        this.subscription = null;
      }
      
      // 更新状态
      this.status = 'disconnected';
      
      // 注意：不要在这里调用stompService.disconnect()
      // 因为可能有其他组件仍在使用WebSocket连接
      // 而是仅取消自己的订阅
    },
    
    /**
     * 重置状态
     */
    resetState() {
      this.currentCompetition = null;
      this.records = [];
      this.ranking = [];
      this.status = 'disconnected';
      this.error = null;
      this.isLoading = false;
      this.subscription = null;
      this.lastUpdateTime = null;
    },
    
    /**
     * 清除错误信息
     */
    clearError() {
      this.error = null;
    }
  }
}); 