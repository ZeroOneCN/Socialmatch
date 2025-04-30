import { request } from '../utils/request'

/**
 * 获取推荐用户列表
 * @param {Object} params - 筛选参数
 * @param {Number} params.gender - 性别筛选：1-男，2-女
 * @param {Number} params.ageMin - 最小年龄
 * @param {Number} params.ageMax - 最大年龄
 * @param {String} params.location - 位置筛选
 * @param {String} params.interests - 兴趣标签，多个以逗号分隔
 * @param {Number} params.limit - 推荐数量，默认30
 * @returns {Promise}
 */
export function getRecommendedUsers(params = {}) {
  // 设置默认参数
  const finalParams = {
    limit: 30, // 确保默认请求30个推荐
    ...params
  };

  console.log('[API] 发送推荐用户请求，参数:', finalParams);
  
  return request({
    url: '/match/recommend',
    method: 'get',
    params: finalParams
  }).then(response => {
    console.log('[API] 推荐用户响应状态:', response.code);
    console.log('[API] 推荐用户数量:', response.data?.length || 0);
    return response;
  }).catch(error => {
    console.error('[API] 推荐用户请求失败:', error);
    throw error;
  });
}

/**
 * 发送喜欢请求
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function likeUser(targetUserId) {
  console.log('[API] 发送喜欢用户请求:', targetUserId);
  return request({
    url: `/match/like/${targetUserId}`,
    method: 'post'
  }).then(response => {
    console.log('[API] 喜欢用户响应:', response.code, response.data);
    return response;
  }).catch(error => {
    console.error('[API] 喜欢用户请求失败:', error);
    throw error;
  });
}

/**
 * 发送不喜欢请求
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function dislikeUser(targetUserId) {
  console.log('[API] 发送不喜欢用户请求:', targetUserId);
  return request({
    url: `/match/dislike/${targetUserId}`,
    method: 'post'
  }).then(response => {
    console.log('[API] 不喜欢用户响应:', response.code);
    return response;
  }).catch(error => {
    console.error('[API] 不喜欢用户请求失败:', error);
    throw error;
  });
}

/**
 * 获取匹配列表
 * @param {Object} params - 查询参数
 * @param {Number} params.status - 匹配状态：0-待确认，1-已匹配，2-已解除，3-拒绝
 * @param {Number} params.page - 页码，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @returns {Promise}
 */
export function getMatches(params = {}) {
  return request({
    url: '/match/list',
    method: 'get',
    params
  })
}

/**
 * 获取匹配详情
 * @param {string|number} matchId - 匹配ID
 * @returns {Promise}
 */
export function getMatchDetail(matchId) {
  return request({
    url: `/match/${matchId}`,
    method: 'get'
  })
}

/**
 * 删除匹配
 * @param {string|number} matchId - 要删除的匹配ID
 * @returns {Promise}
 */
export function deleteMatch(matchId) {
  return request({
    url: `/match/cancel/${matchId}`,
    method: 'post'
  })
}

/**
 * 获取用户喜欢的人列表
 * @returns {Promise}
 */
export function getLikesGiven() {
  return request({
    url: '/match/likes-given',
    method: 'get'
  })
}

/**
 * 获取喜欢当前用户的人列表
 * @returns {Promise}
 */
export function getLikesReceived() {
  return request({
    url: '/match/likes-received',
    method: 'get'
  })
}

/**
 * 检查是否匹配
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function checkIfMatched(targetUserId) {
  return request({
    url: `/match/check/${targetUserId}`,
    method: 'get'
  })
}

/**
 * 创建或获取与指定用户的会话
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise}
 */
export function createOrGetConversation(targetUserId) {
  return request({
    url: '/chat/conversation',
    method: 'post',
    data: {
      targetUserId: targetUserId
    }
  })
} 