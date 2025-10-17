import { defineStore } from 'pinia';
import * as competitionAPI from '@/api/competition';
import stompService from '@/websocket';
import { useUserStore } from './user';
import { toDisplayRecord } from '@/utils/coordinates';
import { 
  normalizeCompetitionStatus,
  isCompetitionStatus,
  shouldAutoConnect
} from '@/utils/competitionStatus';

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
    subscribedTopics: [],
    lastUpdateTime: null,
    currentRound: 1,
    currentShotNumber: 0
  }),
  
  // 计算属性
  getters: {
    isConnected: (state) => state.status === 'connected',
    
    // 获取当前比赛的总射击数
    totalShots: (state) => state.records.length,
    
    // 获取当前用户的得分
    currentUserScore: (state) => {
      const userStore = useUserStore();
      const currentUserId = userStore.userInfo?.id;
      
      if (!currentUserId) return 0;
      
      const userRecords = state.records.filter(record => record.athleteId === parseInt(currentUserId));
      if (userRecords.length === 0) return 0;
      
      const totalScore = userRecords.reduce((sum, record) => sum + (record.score || 0), 0);
      return totalScore;
    },
    
    // 获取当前用户的射击数
    currentUserShots: (state) => {
      const userStore = useUserStore();
      const currentUserId = userStore.userInfo?.id;
      
      if (!currentUserId) return 0;
      
      return state.records.filter(record => record.athleteId === parseInt(currentUserId)).length;
    },
    
    // 获取当前用户的平均分
    currentUserAverage: (state) => {
      const userStore = useUserStore();
      const currentUserId = userStore.userInfo?.id;
      
      if (!currentUserId) return 0;
      
      const userRecords = state.records.filter(record => record.athleteId === parseInt(currentUserId));
      if (userRecords.length === 0) return 0;
      
      const totalScore = userRecords.reduce((sum, record) => sum + (record.score || 0), 0);
      return (totalScore / userRecords.length).toFixed(2);
    },
    
    // 获取当前用户的排名
    currentUserRank: (state) => {
      const userStore = useUserStore();
      const currentUserId = userStore.userInfo?.id;
      
      if (!currentUserId) return null;
      
      const userRank = state.ranking.findIndex(rank => rank.athleteId === parseInt(currentUserId));
      return userRank >= 0 ? userRank + 1 : null;
    },
    
    // 比赛是否正在进行中
    isCompetitionActive: (state) => {
      if (!state.currentCompetition) return false;
      // 允许已创建和进行中的比赛都可以射击
      return isCompetitionStatus(state.currentCompetition.status, 'CREATED') || 
             isCompetitionStatus(state.currentCompetition.status, 'RUNNING');
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
        if (this.currentCompetition?.status) {
          this.currentCompetition.status = normalizeCompetitionStatus(this.currentCompetition.status);
        }
        
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
          // statusResponse.status 是一个对象，真正的状态在 statusResponse.status.status
          const status = statusResponse?.status?.status ?? statusResponse?.status ?? statusResponse;
          const normalizedStatus = normalizeCompetitionStatus(status);
          
          if (this.currentCompetition) {
            this.currentCompetition.status = normalizedStatus;
          }
        
        // 如果比赛处于需要实时连接的状态，连接WebSocket
          if (normalizedStatus && shouldAutoConnect(normalizedStatus)) {
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
      this.subscribedTopics = [];
      
      try {
      // 连接WebSocket
      stompService.connect(() => {
        // 连接成功后，更新状态
        this.status = 'connected';
        console.log('🔌 WebSocket 连接成功');
        
        // 订阅比赛主题
        const topic = `/topic/competition/${id}`;
        console.log(`📡 订阅比赛主题: ${topic}`);
        this.subscription = stompService.subscribe(topic, (message) => {
          // 处理收到的消息
          console.log(`📩 收到主题 ${topic} 的消息:`, message);
          this.handleCompetitionMessage(message);
        });
        this.subscribedTopics = [topic];
        
        // 订阅比赛状态变更主题
        const statusTopic = `/topic/competition/${id}/status`;
        console.log(`📡 订阅状态主题: ${statusTopic}`);
        stompService.subscribe(statusTopic, (message) => {
          // 处理比赛状态变更
          console.log(`📩 收到主题 ${statusTopic} 的消息:`, message);
          this.handleStatusChange(message);
        });
        this.subscribedTopics.push(statusTopic);
        
        console.log(`✅ 已连接并订阅比赛 ${id} 的WebSocket主题`);
      });
      } catch (error) {
        console.warn('⚠️ WebSocket连接失败（后端可能未启动）:', error.message);
        this.status = 'disconnected';
      }
    },
    
    /**
     * 处理比赛WebSocket消息
     * @param {Object} message - 收到的消息
     */
    handleCompetitionMessage(message) {
      try {
        console.log('🔔 收到WebSocket消息:', message);
        
        // 如果消息是新的射击记录
        if (message.type === 'SHOOTING_RECORD') {
          const confirmedRecord = toDisplayRecord(message.data);
          console.log('📊 收到射击记录广播:', confirmedRecord);
          
          // 检查是否已经存在这条记录（去重）
          const existingIndex = this.records.findIndex(record => {
            // 如果有真实的记录ID，直接比较
            if (confirmedRecord.id && record.id && !record.isPending) {
              return record.id === confirmedRecord.id;
            }
            // 如果是临时记录，通过运动员ID、轮次、射击编号匹配
            if (record.isPending) {
              return record.athleteId === confirmedRecord.athleteId &&
                     record.roundNumber === confirmedRecord.roundNumber &&
                     record.shotNumber === confirmedRecord.shotNumber;
            }
            return false;
          });
          
          if (existingIndex >= 0) {
            // 用服务器确认的记录替换临时记录
            console.log('✓ 替换临时记录为服务器确认记录');
            this.records[existingIndex] = confirmedRecord;
          } else {
            // 添加新记录
            console.log('✓ 添加新射击记录:', confirmedRecord.athleteName);
            this.records.push(confirmedRecord);
          }
          
          // 注意：不在这里更新排名，等待后端的 RANKING_UPDATE 消息
          // this.updateRanking();
        } 
        // 如果消息是排名更新
        else if (message.type === 'RANKING_UPDATE') {
          console.log('📈 收到排名更新:', message.data);
          // 直接更新排名
          this.ranking = message.data;
        }
        // 如果消息是比赛状态更新
        else if (message.type === 'COMPETITION_STATUS') {
          console.log('🏁 收到比赛状态更新:', message.data);
          // 更新比赛状态
          if (this.currentCompetition && message.data.status) {
            this.currentCompetition.status = normalizeCompetitionStatus(message.data.status);
          }
        }
        // 如果是错误消息
        else if (message.type === 'error') {
          console.error('❌ 服务器错误:', message.data);
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
        const statusPayload =
          message?.data?.status ??
          message?.status ??
          (typeof message === 'string' ? message : null);

        if (!statusPayload) {
          return;
        }

        const normalizedStatus = normalizeCompetitionStatus(statusPayload);

        // 更新比赛状态
        if (this.currentCompetition) {
          this.currentCompetition.status = normalizedStatus;
        }
        
        // 如果比赛已结束，断开连接
        if (isCompetitionStatus(normalizedStatus, 'COMPLETED')) {
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
          this.currentCompetition.status = 'RUNNING';
        }
        
        // 重置轮次和射击计数
        this.currentRound = 1;
        this.currentShotNumber = 0;
        this.records = [];
        
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
          this.currentCompetition.status = 'COMPLETED';
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
     * 提交射击记录（参考训练模块实现）
     * @param {number|string} competitionId - 比赛ID
     * @param {Object} shotData - 射击数据 {x, y, score}
     * @param {boolean} useHttp - 是否使用HTTP API（备选方案）
     */
    async submitShot(competitionId, shotData, useHttp = false) {
      // 检查比赛是否可以射击
      if (!this.isCompetitionActive) {
        throw new Error('比赛已结束或已取消');
      }

      try {
        // 坐标归一化 (参考训练模块)
        // 假设 shotData.x 和 shotData.y 的范围是 0-200
        const normalizedX = shotData.x / 200;
        const normalizedY = shotData.y / 200;
        
        // 递增射击编号
        this.currentShotNumber++;
        
        // 获取当前用户信息
        const userStore = useUserStore();
        
        // 获取用户ID（后端会根据userId查询athleteId）
        const userId = userStore.userInfo?.id;
        if (!userId) {
          throw new Error('无法获取用户ID，请确认已登录');
        }
        
        // 构建射击记录
        const payload = {
          competitionId: parseInt(competitionId),
          userId: userId, // 发送用户ID，后端会根据userId查询athleteId
          recordType: 'COMPETITION',
          x: normalizedX,
          y: normalizedY,
          score: shotData.score,
          roundNumber: this.currentRound,
          shotNumber: this.currentShotNumber,
          shotAt: new Date().toISOString()
        };

        console.log('📤 发送射击记录 (用户ID: ' + userId + '):', payload);

        // 根据连接状态选择提交方式
        if (this.isConnected && !useHttp) {
          // 优先使用WebSocket
          try {
            stompService.send('/app/competition/shot', payload);
            console.log('✓ 通过WebSocket提交射击记录');
          } catch (wsError) {
            console.warn('WebSocket发送失败，切换到HTTP:', wsError.message);
            // WebSocket失败，降级到HTTP
            useHttp = true;
          }
        }
        
        // 使用HTTP API作为备选方案
        if (!this.isConnected || useHttp) {
          console.log('使用HTTP API提交射击记录');
          const response = await competitionAPI.addCompetitionRecord(payload);
          console.log('✓ HTTP提交成功:', response);
          
          // HTTP成功后，手动添加记录（不更新排名，等待后端广播）
          const confirmedRecord = response.record || response;
          const displayRecord = toDisplayRecord(confirmedRecord);
          this.records.push(displayRecord);
          // 注意：不在这里更新排名，等待后端的 RANKING_UPDATE 消息
          // this.updateRanking();
          
          return displayRecord;
        }
        
        // WebSocket方式 - 本地临时添加记录（等待WebSocket确认）
        const tempRecord = toDisplayRecord({
          ...payload,
          id: Date.now(), // 临时ID
          athleteId: userStore.userInfo?.athleteId ?? null,
          athleteName: userStore.userInfo?.athleteName || userStore.userInfo?.name || userStore.userInfo?.username || '当前用户',
          isPending: true // 标记为待确认
        });
        
        this.records.push(tempRecord);
        
        // 注意：不在这里更新排名，等待后端的 RANKING_UPDATE 消息
        // this.updateRanking();
        
        return tempRecord;

      } catch (error) {
        console.error('提交射击记录失败:', error);
        // 尝试使用HTTP备选方案（如果之前没尝试过）
        if (!useHttp && this.isCompetitionActive) {
          console.log('尝试使用HTTP备选方案...');
          return this.submitShot(competitionId, shotData, true);
        }
        throw new Error('提交射击记录失败：' + error.message);
      }
    },
    
    /**
     * 切换到下一轮
     */
    nextRound() {
      this.currentRound++;
      this.currentShotNumber = 0;
      console.log(`进入第 ${this.currentRound} 轮`);
    },
    
    /**
     * 断开WebSocket连接
     */
    disconnect() {
      if (this.subscribedTopics.length > 0) {
        this.subscribedTopics.forEach(topic => stompService.unsubscribe(topic));
        this.subscribedTopics = [];
      }
      this.subscription = null;
      
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
      this.subscribedTopics = [];
      this.lastUpdateTime = null;
      this.currentRound = 1;
      this.currentShotNumber = 0;
    },
    
    /**
     * 清除错误信息
     */
    clearError() {
      this.error = null;
    }
  }
}); 
