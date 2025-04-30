/**
 * 认证相关工具函数
 */

// Token在localStorage中的key
const TOKEN_KEY = 'user_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const TOKEN_EXPIRE_KEY = 'token_expire';

/**
 * 获取用户Token
 * @returns {string} 用户Token，如果未登录返回空字符串
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

/**
 * 设置用户Token
 * @param {string} token 用户Token
 * @param {number} expireIn Token有效期（秒）
 */
export function setToken(token, expireIn = 86400) {
  if (!token) return;
  
  const now = new Date().getTime();
  const expireTime = now + expireIn * 1000;
  
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(TOKEN_EXPIRE_KEY, expireTime.toString());
}

/**
 * 获取刷新Token
 * @returns {string} 刷新Token，如果不存在返回空字符串
 */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || '';
}

/**
 * 设置刷新Token
 * @param {string} refreshToken 刷新Token
 */
export function setRefreshToken(refreshToken) {
  if (!refreshToken) return;
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

/**
 * 检查Token是否过期
 * @returns {boolean} 如果过期返回true，否则返回false
 */
export function isTokenExpired() {
  const tokenExpire = localStorage.getItem(TOKEN_EXPIRE_KEY);
  if (!tokenExpire) return true;
  
  const now = new Date().getTime();
  return now >= parseInt(tokenExpire);
}

/**
 * 清除用户Token信息
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(TOKEN_EXPIRE_KEY);
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 如果已登录返回true，否则返回false
 */
export function isAuthenticated() {
  return !!getToken() && !isTokenExpired();
}

export default {
  getToken,
  setToken,
  getRefreshToken,
  setRefreshToken,
  isTokenExpired,
  removeToken,
  isAuthenticated
}; 