import axios from '../utils/request';

/**
 * 获取用户列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.keyword 关键词搜索(用户名、昵称、手机号)
 * @param {number} params.status 状态(0-禁用，1-正常，null-全部)
 * @returns {Promise<Object>} 分页结果
 */
export function getUserList(params) {
  return axios.get('/api/admin/user/list', { params });
}

/**
 * 获取用户详情
 * @param {number} userId 用户ID
 * @returns {Promise<Object>} 用户详情
 */
export function getUserDetail(userId) {
  return axios.get(`/api/admin/user/${userId}`);
}

/**
 * 更新用户状态
 * @param {number} userId 用户ID
 * @param {number} status 状态(0-禁用，1-正常)
 * @returns {Promise<boolean>} 是否成功
 */
export function updateUserStatus(userId, status) {
  return axios.put(`/api/admin/user/${userId}/status`, { status });
}

/**
 * 重置用户密码
 * @param {number} userId 用户ID
 * @returns {Promise<Object>} 新密码
 */
export function resetUserPassword(userId) {
  return axios.put(`/api/admin/user/${userId}/password/reset`);
}

/**
 * 获取用户统计数据
 * @returns {Promise<Object>} 统计数据
 */
export function getUserStats() {
  return axios.get('/api/admin/user/stats');
}

// 获取用户注册趋势
export function getUserRegisterTrend(params) {
  return axios.get('/api/admin/user/register/trend', { params });
}

// 获取用户活跃度
export function getUserActivityTrend(params) {
  return axios.get('/api/admin/user/activity/trend', { params });
} 