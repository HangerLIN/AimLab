import apiClient from './index';

/**
 * 开始新的训练场次
 * @param {string} sessionName - 训练场次名称
 * @returns {Promise} - 返回请求的 Promise
 */
export function startNewSession(sessionName) {
  return apiClient.post('/api/training/sessions/start', { name: sessionName });
}

/**
 * 添加训练射击记录
 * @param {Object} recordData - 射击记录数据
 * @returns {Promise} - 返回请求的 Promise
 */
export function addTrainingRecord(recordData) {
  return apiClient.post('/api/training/records', recordData);
}

/**
 * 获取训练报告
 * @param {number|string} sessionId - 训练场次ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getTrainingReport(sessionId) {
  return apiClient.get(`/api/training/sessions/${sessionId}/report`);
}

/**
 * 结束训练场次
 * @param {number|string} sessionId - 训练场次ID
 * @param {string} notes - 训练备注
 * @returns {Promise} - 返回请求的 Promise
 */
export function endTrainingSession(sessionId, notes = '') {
  return apiClient.post(`/api/training/sessions/${sessionId}/end`, { notes });
}

/**
 * 获取训练场次列表
 * @returns {Promise} - 返回请求的 Promise
 */
export function getTrainingSessions() {
  return apiClient.get('/api/training/sessions');
}

/**
 * 获取训练场次详情
 * @param {number|string} sessionId - 训练场次ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getTrainingSessionDetail(sessionId) {
  return apiClient.get(`/api/training/sessions/${sessionId}`);
}

/**
 * 获取训练场次的射击记录
 * @param {number|string} sessionId - 训练场次ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getTrainingRecords(sessionId) {
  return apiClient.get(`/api/training/sessions/${sessionId}/records`);
}

/**
 * 下载训练报告PDF
 * @param {number|string} sessionId - 训练场次ID
 * @returns {Promise} - 返回请求的 Promise，包含二进制数据
 */
export function downloadTrainingReportPdf(sessionId) {
  return apiClient.get(`/api/training/sessions/${sessionId}/report/pdf`, {
    responseType: 'blob'
  });
} 