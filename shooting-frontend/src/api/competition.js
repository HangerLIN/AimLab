import apiClient from './index';

/**
 * 获取比赛列表
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCompetitionList() {
  return apiClient.get('/competitions');
}

/**
 * 获取比赛详情
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCompetitionDetails(id) {
  return apiClient.get(`/competitions/${id}`);
}

/**
 * 开始比赛
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function startCompetition(id) {
  return apiClient.post(`/competitions/${id}/start`);
}

/**
 * 结束比赛
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function endCompetition(id) {
  return apiClient.post(`/competitions/${id}/complete`);
}

/**
 * 获取比赛实时排名
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getLiveRanking(id) {
  return apiClient.get(`/competitions/${id}/rankings`);
}

/**
 * 获取比赛状态
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCompetitionStatus(id) {
  return apiClient.get(`/competitions/${id}/status`);
}

/**
 * 获取比赛结果
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCompetitionResults(id) {
  return apiClient.get(`/competitions/${id}/results`);
}

/**
 * 报名参加比赛
 * @param {number|string} id - 比赛ID
 * @param {Array} athleteIds - 运动员ID列表
 * @returns {Promise} - 返回请求的 Promise
 */
export function registerForCompetition(id, athleteIds = []) {
  return apiClient.post(`/competitions/${id}/enroll`, { athleteIds });
}

/**
 * 获取当前用户的运动员信息
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCurrentAthlete() {
  return apiClient.get('/athletes/profile');
}

/**
 * 取消比赛报名
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function cancelCompetitionRegistration(id) {
  return apiClient.delete(`/competitions/${id}/enroll`);
}

/**
 * 获取参赛选手列表
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getCompetitionParticipants(id) {
  return apiClient.get(`/competitions/${id}/athletes`);
}

/**
 * 创建新比赛
 * @param {Object} competitionData - 比赛数据
 * @returns {Promise} - 返回请求的 Promise
 */
export function createCompetition(competitionData) {
  return apiClient.post('/competitions', competitionData);
}

/**
 * 更新比赛信息
 * @param {number|string} id - 比赛ID
 * @param {Object} competitionData - 比赛数据
 * @returns {Promise} - 返回请求的 Promise
 */
export function updateCompetition(id, competitionData) {
  return apiClient.put(`/competitions/${id}`, competitionData);
}

/**
 * 删除比赛
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function deleteCompetition(id) {
  return apiClient.delete(`/competitions/${id}`);
}

/**
 * 下载比赛结果PDF
 * @param {number|string} id - 比赛ID
 * @returns {Promise} - 返回请求的 Promise，包含二进制数据
 */
export function downloadCompetitionResultsPdf(id) {
  return apiClient.get(`/competitions/${id}/results/pdf`, {
    responseType: 'blob'
  });
}

// 注意：addCompetitionRecord 由 WebSocket 实现，不在此处定义 HTTP API
