import request from '@/utils/request';

/**
 * 获取所有身份认证列表
 */
export function getAllIdentityVerifications() {
  console.log('Calling API: getAllIdentityVerifications');
  return request({
    url: '/api/admin/verification/identity/all',
    method: 'get'
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in getAllIdentityVerifications:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
}

/**
 * 获取所有教育认证列表
 */
export function getAllEducationVerifications() {
  console.log('Calling API: getAllEducationVerifications');
  return request({
    url: '/api/admin/verification/education/all',
    method: 'get'
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in getAllEducationVerifications:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
}

/**
 * 获取待审核的身份认证列表
 */
export function getPendingIdentityVerifications() {
  console.log('Calling API: getPendingIdentityVerifications');
  return request({
    url: '/api/admin/verification/identity/pending',
    method: 'get'
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in getPendingIdentityVerifications:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
}

/**
 * 获取待审核的教育认证列表
 */
export function getPendingEducationVerifications() {
  console.log('Calling API: getPendingEducationVerifications');
  return request({
    url: '/api/admin/verification/education/pending',
    method: 'get'
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in getPendingEducationVerifications:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
}

/**
 * 获取认证详情
 * @param {number} verificationId 认证ID
 */
export function getVerificationDetail(verificationId) {
  console.log('Calling API: getVerificationDetail', verificationId);
  return request({
    url: `/api/admin/verification/detail/${verificationId}`,
    method: 'get'
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in getVerificationDetail:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
}

/**
 * 审核认证申请
 * @param {object} data 审核信息
 */
export function auditVerification(data) {
  console.log('Calling API: auditVerification', data);
  return request({
    url: '/api/admin/verification/audit',
    method: 'post',
    data
  }).then(response => {
    console.log('API response success:', response);
    return response;
  }).catch(error => {
    console.error('API error in auditVerification:', error);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
    throw error;
  });
} 