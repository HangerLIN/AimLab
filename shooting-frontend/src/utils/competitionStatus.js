const STATUS_META = {
  CREATED: { text: '已创建', type: 'info' },
  RUNNING: { text: '进行中', type: 'warning' },
  PAUSED: { text: '已暂停', type: 'info' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELED: { text: '已取消', type: 'danger' },
  UNKNOWN: { text: '未知', type: 'info' }
};

/**
 * 归一化比赛状态字符串，兼容后端返回的各种别名
 * @param {string} status
 * @returns {string}
 */
export const normalizeCompetitionStatus = (status) => {
  if (!status && status !== 0) return 'UNKNOWN';
  const upper = String(status).trim().toUpperCase();
  
  if (upper === 'STARTED' || upper === 'ACTIVE') return 'RUNNING';
  if (upper === 'FINISHED') return 'COMPLETED';
  if (upper === 'CANCELLED') return 'CANCELED';
  
  return STATUS_META[upper] ? upper : 'UNKNOWN';
};

/**
 * 获取状态对应的展示元数据
 * @param {string} status
 * @returns {{text: string, type: string}}
 */
export const getCompetitionStatusMeta = (status) => {
  const normalized = normalizeCompetitionStatus(status);
  return STATUS_META[normalized] || STATUS_META.UNKNOWN;
};

/**
 * 判断状态是否匹配目标状态列表
 * @param {string} status
 * @param {string|string[]} targets
 * @returns {boolean}
 */
export const isCompetitionStatus = (status, targets) => {
  const normalized = normalizeCompetitionStatus(status);
  const list = Array.isArray(targets) ? targets : [targets];
  return list.includes(normalized);
};

/**
 * 在需要自动建立实时连接时使用的状态判断
 * @param {string} status
 * @returns {boolean}
 */
export const shouldAutoConnect = (status) =>
  isCompetitionStatus(status, ['RUNNING', 'PAUSED']);

export { STATUS_META as COMPETITION_STATUS_META };
