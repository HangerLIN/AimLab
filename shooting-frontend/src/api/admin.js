import apiClient from './index';

// ==================== 仪表盘统计 ====================

/**
 * 获取管理后台仪表盘统计数据
 */
export const getDashboardStats = () => {
  return apiClient.get('/admin/dashboard');
};

// ==================== 用户管理 ====================

/**
 * 获取用户列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.role - 角色筛选
 * @param {string} params.status - 状态筛选
 */
export const getUserList = (params) => {
  return apiClient.get('/admin/users', { params });
};

/**
 * 创建用户
 * @param {Object} data - 用户数据
 */
export const createUser = (data) => {
  return apiClient.post('/admin/users', data);
};

/**
 * 更新用户信息
 * @param {number} userId - 用户ID
 * @param {Object} data - 更新数据
 */
export const updateUser = (userId, data) => {
  return apiClient.put(`/admin/users/${userId}`, data);
};

/**
 * 删除用户
 * @param {number} userId - 用户ID
 */
export const deleteUser = (userId) => {
  return apiClient.delete(`/admin/users/${userId}`);
};

/**
 * 重置用户密码
 * @param {number} userId - 用户ID
 * @param {string} newPassword - 新密码，默认为 123456
 */
export const resetUserPassword = (userId, newPassword = '123456') => {
  return apiClient.post(`/admin/users/${userId}/reset-password`, { password: newPassword });
};

/**
 * 强制踢出用户
 * @param {number} userId - 用户ID
 */
export const kickUser = (userId) => {
  return apiClient.post(`/admin/users/${userId}/kick`);
};

// ==================== 运动员管理 ====================

/**
 * 获取运动员列表
 * @param {Object} params - 查询参数
 */
export const getAthleteList = (params) => {
  return apiClient.get('/admin/athletes', { params });
};

/**
 * 获取待审批运动员列表
 */
export const getPendingAthletes = () => {
  return apiClient.get('/admin/athletes/pending');
};

/**
 * 获取已审批运动员列表
 */
export const getApprovedAthletes = () => {
  return apiClient.get('/admin/athletes/approved');
};

/**
 * 审批通过运动员
 * @param {number} athleteId - 运动员ID
 */
export const approveAthlete = (athleteId) => {
  return apiClient.put(`/admin/athletes/${athleteId}/approve`);
};

/**
 * 拒绝运动员审批
 * @param {number} athleteId - 运动员ID
 * @param {string} reason - 拒绝原因
 */
export const rejectAthlete = (athleteId, reason) => {
  return apiClient.put(`/admin/athletes/${athleteId}/reject`, null, {
    params: { reason }
  });
};

/**
 * 更新运动员信息
 * @param {number} athleteId - 运动员ID
 * @param {Object} data - 更新数据
 */
export const updateAthlete = (athleteId, data) => {
  return apiClient.put(`/admin/athletes/${athleteId}`, data);
};

/**
 * 删除运动员
 * @param {number} athleteId - 运动员ID
 */
export const deleteAthlete = (athleteId) => {
  return apiClient.delete(`/admin/athletes/${athleteId}`);
};

/**
 * 导出运动员数据
 * @param {string} format - 导出格式 (csv/excel)
 */
export const exportAthletes = (format = 'csv') => {
  return apiClient.get('/admin/athletes/export', {
    params: { format },
    responseType: 'blob'
  });
};

/**
 * 导入运动员数据
 * @param {FormData} formData - 包含文件的表单数据
 */
export const importAthletes = (formData) => {
  return apiClient.post('/admin/athletes/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

/**
 * 获取待审批的档案修改列表
 */
export const getPendingModifications = () => {
  return apiClient.get('/admin/athletes/pending-modifications');
};

/**
 * 审批档案修改
 * @param {number} athleteId - 运动员ID
 */
export const approveModification = (athleteId) => {
  return apiClient.put(`/admin/athletes/${athleteId}/approve-modification`);
};

/**
 * 拒绝档案修改
 * @param {number} athleteId - 运动员ID
 * @param {string} reason - 拒绝原因
 */
export const rejectModification = (athleteId, reason) => {
  return apiClient.put(`/admin/athletes/${athleteId}/reject-modification`, null, {
    params: { reason }
  });
};

// ==================== 比赛管理 ====================

/**
 * 创建比赛
 * @param {Object} data - 比赛数据
 */
export const createCompetition = (data) => {
  return apiClient.post('/admin/competitions', data);
};

/**
 * 更新比赛
 * @param {number} id - 比赛ID
 * @param {Object} data - 更新数据
 */
export const updateCompetition = (id, data) => {
  return apiClient.put(`/admin/competitions/${id}`, data);
};

/**
 * 强制结束比赛
 * @param {number} id - 比赛ID
 */
export const forceFinishCompetition = (id) => {
  return apiClient.post(`/admin/competitions/${id}/force-finish`);
};

/**
 * 导出比赛成绩
 * @param {number} id - 比赛ID
 * @param {string} format - 导出格式
 */
export const exportCompetitionResults = (id, format = 'csv') => {
  return apiClient.get(`/admin/competitions/${id}/results/export`, {
    params: { format },
    responseType: 'blob'
  });
};

/**
 * 下载比赛PDF报告
 * @param {number} id - 比赛ID
 */
export const downloadCompetitionPdf = (id) => {
  return apiClient.get(`/admin/competitions/${id}/results/pdf`, {
    responseType: 'blob'
  });
};

// ==================== 训练统计 ====================

/**
 * 获取时间维度训练统计
 * @param {Object} params - 查询参数
 */
export const getTrainingAnalyticsByTime = (params) => {
  return apiClient.get('/admin/training/analytics/time', { params });
};

/**
 * 获取运动员维度训练统计
 * @param {Object} params - 查询参数
 */
export const getTrainingAnalyticsByAthlete = (params) => {
  return apiClient.get('/admin/training/analytics/athlete', { params });
};

/**
 * 获取项目维度训练统计
 * @param {Object} params - 查询参数
 */
export const getTrainingAnalyticsByProject = (params) => {
  return apiClient.get('/admin/training/analytics/project', { params });
};

/**
 * 导出训练报表
 * @param {Object} params - 查询参数
 */
export const exportTrainingReport = (params) => {
  return apiClient.get('/admin/training/analytics/export', {
    params,
    responseType: 'blob'
  });
};

// ==================== 系统设置 ====================

/**
 * 获取基本设置
 */
export const getBasicSettings = () => {
  return apiClient.get('/admin/settings/basic');
};

/**
 * 更新基本设置
 * @param {Object} data - 设置数据
 */
export const updateBasicSettings = (data) => {
  return apiClient.put('/admin/settings/basic', data);
};

/**
 * 上传系统Logo
 * @param {FormData} formData - 包含文件的表单数据
 */
export const uploadSystemLogo = (formData) => {
  return apiClient.post('/admin/settings/logo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

/**
 * 获取公开的系统设置（无需登录）
 */
export const getPublicSettings = () => {
  return apiClient.get('/admin/settings/public/basic');
};

/**
 * 获取系统配置（旧接口，保留兼容）
 */
export const getSystemConfig = () => {
  return apiClient.get('/admin/settings/basic');
};

/**
 * 更新系统配置（旧接口，保留兼容）
 * @param {Object} data - 配置数据
 */
export const updateSystemConfig = (data) => {
  return apiClient.put('/admin/settings/basic', data);
};

/**
 * 获取系统日志
 * @param {Object} params - 查询参数
 */
export const getSystemLogs = (params) => {
  return apiClient.get('/admin/system/logs', { params });
};

/**
 * 清理系统缓存
 */
export const clearSystemCache = () => {
  return apiClient.post('/admin/system/cache/clear');
};
