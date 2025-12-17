import apiClient from './index';

/**
 * 获取消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.type - 消息类型（可选）
 * @param {boolean} params.isRead - 是否已读（可选）
 */
export const getMessageList = (params) => {
  return apiClient.get('/messages', { params });
};

/**
 * 获取未读消息数量
 */
export const getUnreadCount = () => {
  return apiClient.get('/messages/unread-count');
};

/**
 * 获取消息详情
 * @param {number} id - 消息ID
 */
export const getMessageDetail = (id) => {
  return apiClient.get(`/messages/${id}`);
};

/**
 * 标记消息为已读
 * @param {number} id - 消息ID
 */
export const markAsRead = (id) => {
  return apiClient.put(`/messages/${id}/read`);
};

/**
 * 标记所有消息为已读
 */
export const markAllAsRead = () => {
  return apiClient.put('/messages/read-all');
};

/**
 * 删除消息
 * @param {number} id - 消息ID
 */
export const deleteMessage = (id) => {
  return apiClient.delete(`/messages/${id}`);
};

/**
 * 批量删除消息
 * @param {number[]} ids - 消息ID列表
 */
export const deleteMessages = (ids) => {
  return apiClient.delete('/messages/batch', { data: { ids } });
};

/**
 * 管理员发送消息
 * @param {Object} data - 消息数据
 * @param {number} data.receiverId - 接收者ID
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export const adminSendMessage = (data) => {
  return apiClient.post('/messages/admin/send', data);
};

/**
 * 管理员批量发送消息
 * @param {Object} data - 消息数据
 * @param {number[]} data.receiverIds - 接收者ID列表
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export const adminSendBatchMessage = (data) => {
  return apiClient.post('/messages/admin/send-batch', data);
};

/**
 * 用户发送消息给管理员
 * @param {Object} data - 消息数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export const sendToAdmin = (data) => {
  return apiClient.post('/messages/send-to-admin', data);
};

/**
 * 获取可发送消息的用户列表（管理员使用）
 * @param {string} keyword - 搜索关键字（可选）
 */
export const getMessageableUsers = (keyword) => {
  const params = keyword ? { keyword } : {};
  return apiClient.get('/messages/admin/users', { params });
};
