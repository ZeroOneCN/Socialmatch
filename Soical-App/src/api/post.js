import http from './http'

/**
 * 获取推荐的热门帖子
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} - 返回请求Promise
 */
export function getHotPosts(params) {
  return http.get('/post/list/hot', { params })
}

/**
 * 获取关注用户的帖子
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} - 返回请求Promise
 */
export function getFollowedPosts(params) {
  return http.get('/post/list/followed', { params })
}

/**
 * 获取附近的帖子
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {string} [params.city] - 城市
 * @returns {Promise} - 返回请求Promise
 */
export function getNearbyPosts(params) {
  return http.get('/post/list/nearby', { params })
}

/**
 * 获取社区帖子
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} - 返回请求Promise
 */
export function getCommunityPosts(params) {
  return http.get('/post/list/community', { params })
}

/**
 * 获取用户的帖子
 * @param {number} userId - 用户ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} - 返回请求Promise
 */
export function getUserPosts(userId, params) {
  return http.get(`/post/user/${userId}`, { params })
}

/**
 * 获取用户点赞的帖子
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} - 返回请求Promise
 */
export function getLikedPosts(params) {
  console.log('调用获取点赞帖子API，参数:', params);
  return http.get('/post/list/liked', { params });
}

/**
 * 获取帖子详情
 * @param {number} postId - 帖子ID
 * @returns {Promise} - 返回请求Promise
 */
export function getPostDetail(postId) {
  if (!postId) {
    console.error('获取帖子详情失败: 缺少帖子ID');
    return Promise.reject(new Error('缺少帖子ID'));
  }
  
  console.log(`获取帖子详情, ID: ${postId}`);
  return http.get(`/post/${postId}`).then(response => {
    if (!response.data) {
      throw new Error('帖子不存在或已删除');
    }
    return response;
  });
}

/**
 * 创建帖子
 * @param {Object} data - 帖子数据
 * @param {string} data.content - 帖子内容
 * @param {Array} [data.images] - 图片文件数组
 * @returns {Promise} - 返回请求Promise
 */
export function createPost(data) {
  const formData = new FormData()
  formData.append('content', data.content)
  
  if (data.images && data.images.length > 0) {
    data.images.forEach(image => {
      if (image instanceof File || image instanceof Blob) {
        formData.append('images', image)
      } else if (typeof image === 'object' && image.file) {
        formData.append('images', image.file)
      }
    })
  }
  
  if (data.city) {
    formData.append('city', data.city)
  }
  
  return http.post('/post/create', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除帖子
 * @param {number} postId - 帖子ID
 * @returns {Promise} - 返回请求Promise
 */
export function deletePost(postId) {
  return http.delete(`/post/${postId}`)
}

/**
 * 点赞帖子
 * @param {number} postId - 帖子ID
 * @returns {Promise} - 返回请求Promise
 */
export function likePost(postId) {
  return http.post(`/post/like/${postId}`)
}

/**
 * 取消点赞帖子
 * @param {number} postId - 帖子ID
 * @returns {Promise} - 返回请求Promise
 */
export function unlikePost(postId) {
  if (!postId) {
    console.error('取消点赞失败: 缺少帖子ID');
    return Promise.reject(new Error('缺少帖子ID'));
  }
  
  console.log(`调用取消点赞API, 帖子ID: ${postId}`);
  return http.delete(`/post/like/${postId}`);
}

/**
 * 分享帖子
 * @param {Object} data - 分享数据
 * @param {number} data.originalPostId - 原始帖子ID
 * @param {string} data.content - 分享内容
 * @returns {Promise} - 返回请求Promise
 */
export function sharePost(data) {
  return http.post('/post/share', data)
}

export default {
  getHotPosts,
  getFollowedPosts,
  getNearbyPosts,
  getCommunityPosts,
  getUserPosts,
  getLikedPosts,
  getPostDetail,
  createPost,
  deletePost,
  likePost,
  unlikePost,
  sharePost
} 