import http from './http'

/**
 * 发表评论
 * @param {Object} data - 评论数据
 * @param {number} data.postId - 帖子ID
 * @param {string} data.content - 评论内容
 * @returns {Promise} - 返回请求Promise
 */
export function addComment(data) {
  return http.post('/comment/add', null, { 
    params: {
      postId: data.postId,
      content: data.content
    }
  })
}

/**
 * 获取帖子评论列表
 * @param {number} postId - 帖子ID
 * @param {number} page - 页码，默认1
 * @param {number} pageSize - 每页条数，默认20
 * @returns {Promise} - 返回请求Promise
 */
export function getComments(postId, page = 1, pageSize = 20) {
  return http.get(`/comment/list/${postId}`, {
    params: {
      page,
      pageSize
    }
  })
}

/**
 * 删除评论
 * @param {number} commentId - 评论ID
 * @returns {Promise} - 返回请求Promise
 */
export function deleteComment(commentId) {
  return http.delete(`/comment/${commentId}`)
}

/**
 * 回复评论
 * @param {Object} data - 回复数据
 * @param {number} data.postId - 帖子ID
 * @param {string} data.content - 回复内容
 * @param {number} data.parentId - 父评论ID
 * @param {number} data.replyUserId - 被回复用户ID
 * @returns {Promise} - 返回请求Promise
 */
export function replyComment(data) {
  return http.post('/comment/reply', null, {
    params: {
      postId: data.postId,
      content: data.content,
      parentId: data.parentId,
      replyUserId: data.replyUserId
    }
  })
}

export default {
  addComment,
  getComments,
  deleteComment,
  replyComment
} 