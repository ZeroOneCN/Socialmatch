import request from '../utils/request';

/**
 * 关注用户
 * @param {number} userId - 要关注的用户ID
 * @returns {Promise} - 返回请求Promise
 */
export const followUser = (userId) => {
  return request({
    url: `/user/follow/${userId}`,
    method: 'post'
  });
};

/**
 * 取消关注用户
 * @param {number} userId - 要取消关注的用户ID
 * @returns {Promise} - 返回请求Promise
 */
export const unfollowUser = (userId) => {
  return request({
    url: `/user/follow/${userId}`,
    method: 'delete'
  });
};

/**
 * 检查是否关注了指定用户
 * @param {number} userId - 目标用户ID
 * @returns {Promise} - 返回请求Promise，data为boolean值
 */
export const checkFollowStatus = (userId) => {
  return request({
    url: `/user/follow/status/${userId}`,
    method: 'get'
  });
};

/**
 * 获取关注列表
 * @param {number} userId - 用户ID
 * @param {number} page - 页码，默认1
 * @param {number} pageSize - 每页条数，默认20
 * @returns {Promise} - 返回请求Promise
 */
export function getFollowingList(userId, page = 1, pageSize = 20) {
  console.log(`调用获取关注列表API - 用户ID: ${userId}, 页码: ${page}, 每页条数: ${pageSize}`);
  
  if (!userId) {
    console.error('缺少用户ID参数');
    return Promise.reject(new Error('缺少用户ID参数'));
  }
  
  return request({
    url: `/user/follow/following/${userId}`,
    method: 'get',
    params: { page, pageSize }
  }).then(response => {
    console.log('关注列表API响应:', response);
    return response;
  });
}

/**
 * 获取粉丝列表
 * @param {number} userId - 用户ID
 * @param {number} page - 页码，默认1
 * @param {number} pageSize - 每页条数，默认20
 * @returns {Promise} - 返回请求Promise
 */
export function getFollowerList(userId, page = 1, pageSize = 20) {
  console.log(`调用获取粉丝列表API - 用户ID: ${userId}, 页码: ${page}, 每页条数: ${pageSize}`);
  
  if (!userId) {
    console.error('缺少用户ID参数');
    return Promise.reject(new Error('缺少用户ID参数'));
  }
  
  return request({
    url: `/user/follow/followers/${userId}`,
    method: 'get',
    params: { page, pageSize }
  }).then(response => {
    console.log('粉丝列表API响应:', response);
    return response;
  });
}

/**
 * 批量检查关注状态
 * @param {number[]} userIds - 用户ID数组
 * @returns {Promise} - 返回请求Promise
 */
export function batchCheckFollowStatus(userIds) {
  if (!userIds || !userIds.length) {
    return Promise.resolve({ data: {} });
  }
  
  return request({
    url: '/user/follow/status/batch',
    method: 'post',
    data: { userIds }
  });
}

export default {
  followUser,
  unfollowUser,
  checkFollowStatus,
  getFollowingList,
  getFollowerList,
  batchCheckFollowStatus
} 