import apiClient from './index';

/**
 * 获取当前用户的运动员完整档案
 * @returns {Promise} - 返回请求的 Promise
 */
export function getMyAthleteProfile() {
  return apiClient.get('/athletes/my-profile');
}

/**
 * 获取当前用户的运动员基本信息
 * @returns {Promise} - 返回请求的 Promise
 */
export function getMyAthleteBasicInfo() {
  return apiClient.get('/athletes/profile');
}

/**
 * 创建运动员档案
 * @param {Object} athleteData - 运动员信息
 * @returns {Promise} - 返回请求的 Promise
 */
export function createAthleteProfile(athleteData) {
  return apiClient.post('/athletes/profile', athleteData);
}

/**
 * 更新运动员档案
 * @param {Object} athleteData - 运动员信息
 * @returns {Promise} - 返回请求的 Promise
 */
export function updateAthleteProfile(athleteData) {
  return apiClient.put('/athletes/profile', athleteData);
}

/**
 * 根据ID获取运动员信息
 * @param {number|string} athleteId - 运动员ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getAthleteById(athleteId) {
  return apiClient.get(`/athletes/${athleteId}`);
}

/**
 * 获取指定运动员的完整档案
 * @param {number|string} athleteId - 运动员ID
 * @returns {Promise} - 返回请求的 Promise
 */
export function getAthleteProfile(athleteId) {
  return apiClient.get(`/athletes/${athleteId}/profile`);
}

/**
 * 上传运动员头像
 * @param {FormData} formData - 包含头像文件的 FormData
 * @returns {Promise} - 返回请求的 Promise
 */
export function uploadAvatar(formData) {
  return apiClient.post('/athletes/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 获取等级选项列表
 * @returns {Promise} - 返回请求的 Promise
 */
export function getLevelOptions() {
  return apiClient.get('/athletes/levels');
} 