import request from '@/utils/request';

/**
 * 获取用户认证状态
 * @param {number} userId 用户ID
 */
export function getUserVerificationStatus(userId) {
  return request({
    url: '/verification/status',
    method: 'get',
    params: { userId }
  });
}

/**
 * 提交身份认证
 * @param {number} userId 用户ID
 * @param {object} data 身份认证信息
 */
export function submitIdentityVerification(userId, data) {
  return request({
    url: '/verification/identity',
    method: 'post',
    params: { userId },
    data
  });
}

/**
 * 提交教育认证
 * @param {number} userId 用户ID
 * @param {object} data 教育认证信息
 */
export function submitEducationVerification(userId, data) {
  return request({
    url: '/verification/education',
    method: 'post',
    params: { userId },
    data
  });
}

/**
 * 获取用户身份认证列表
 * @param {number} userId 用户ID
 */
export function getUserIdentityVerifications(userId) {
  return request({
    url: '/verification/identity/list',
    method: 'get',
    params: { userId }
  });
}

/**
 * 获取用户教育认证列表
 * @param {number} userId 用户ID
 */
export function getUserEducationVerifications(userId) {
  return request({
    url: '/verification/education/list',
    method: 'get',
    params: { userId }
  });
}

/**
 * 获取认证详情
 * @param {number} verificationId 认证ID
 */
export function getVerificationDetail(verificationId) {
  return request({
    url: `/verification/detail/${verificationId}`,
    method: 'get'
  });
}

/**
 * 检查用户是否已认证
 * @param {number} userId 用户ID
 * @param {string} type 认证类型：identity-身份认证，education-教育认证
 */
export function checkUserVerified(userId, type) {
  return request({
    url: '/verification/check',
    method: 'get',
    params: { userId, type }
  });
} 