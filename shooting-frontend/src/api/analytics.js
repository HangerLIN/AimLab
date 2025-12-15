// src/api/analytics.js
import apiClient from './index';

/**
 * 运动员对比分析
 */
export function compareAthletes(params) {
  return apiClient.post('/analytics/compare', params);
}

/**
 * 快速对比分析（GET方式）
 */
export function quickCompare(params) {
  return apiClient.get('/analytics/compare', { params });
}

/**
 * 获取趋势分析数据
 */
export function getTrendAnalysis(athleteId, weeks = 12, projectType = null) {
  const params = { weeks };
  if (projectType) params.projectType = projectType;
  return apiClient.get(`/analytics/trend/${athleteId}`, { params });
}

/**
 * 导出对比分析数据
 */
export function exportCompareData(params) {
  return apiClient.post('/analytics/compare/export', params, {
    responseType: 'blob'
  });
}

/**
 * 导出趋势分析数据
 */
export function exportTrendData(athleteId, weeks = 12, projectType = null) {
  const params = { weeks };
  if (projectType) params.projectType = projectType;
  return apiClient.get(`/analytics/trend/${athleteId}/export`, {
    params,
    responseType: 'blob'
  });
}

/**
 * 获取可用的运动员级别列表
 */
export function getAvailableLevels() {
  return apiClient.get('/analytics/levels');
}

/**
 * 获取运动员列表（用于数据分析模块）
 */
export function getAthleteList() {
  return apiClient.get('/athletes');
}
