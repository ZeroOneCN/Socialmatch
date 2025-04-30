import axios from '../utils/request';

/**
 * 管理员登录
 * @param {Object} data - 登录参数
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise<Object>} - 返回登录结果，包含token
 */
export function login(data) {
  return axios.post('/api/admin/login', data);
}

/**
 * 获取管理员信息
 * @returns {Promise<Object>} - 返回管理员信息
 */
export function getAdminInfo() {
  return axios.get('/api/admin/current');
}

/**
 * 更新管理员信息
 * @param {Object} data - 管理员信息
 * @returns {Promise<Object>} - 更新结果
 */
export function updateAdminInfo(data) {
  return axios.put('/api/admin/info', data);
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise<Object>} - 修改结果
 */
export function changePassword(data) {
  return axios.put('/api/admin/password', data);
} 