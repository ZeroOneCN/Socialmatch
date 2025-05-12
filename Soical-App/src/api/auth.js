import axios from 'axios'
import { API_BASE_URL } from '@/config/api'

// 创建axios实例
const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    return res
  },
  error => {
    console.error('Auth API错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 用户登录
 * @param {Object} data 登录信息
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 */
export function login(data) {
  return service({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.phone 手机号
 */
export function register(data) {
  return service({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 */
export function logout() {
  return service({
    url: '/api/auth/logout',
    method: 'post'
  })
}

/**
 * 发送验证码
 * @param {string} phone 手机号
 */
export function sendVerificationCode(phone) {
  return service({
    url: '/api/auth/send-code',
    method: 'post',
    data: { phone }
  })
}

/**
 * 重置密码
 * @param {Object} data 重置密码信息
 * @param {string} data.phone 手机号
 * @param {string} data.code 验证码
 * @param {string} data.newPassword 新密码
 */
export function resetPassword(data) {
  return service({
    url: '/api/auth/reset-password',
    method: 'post',
    data
  })
}

/**
 * 获取安全设置
 * @returns {Promise<Object>} 安全设置信息
 */
export function getSecuritySettings() {
  return service({
    url: '/api/system/settings/security',
    method: 'get'
  })
}

export default {
  login,
  register,
  logout,
  sendVerificationCode,
  getSecuritySettings
} 