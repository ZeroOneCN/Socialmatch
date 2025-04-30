import request from '@/utils/request'

// 获取动态列表
export function getPostList(params) {
  return request({
    url: '/api/admin/content/posts',
    method: 'get',
    params
  })
}

// 获取动态详情
export function getPostDetail(postId) {
  return request({
    url: `/api/admin/content/posts/${postId}`,
    method: 'get'
  })
}

// 审核动态
export function reviewPost(postId, status, reason) {
  return request({
    url: `/api/admin/content/posts/${postId}/review`,
    method: 'put',
    params: {
      status,
      reason
    }
  })
}

// 删除动态
export function deletePost(postId) {
  return request({
    url: `/api/admin/content/posts/${postId}`,
    method: 'delete'
  })
}

// 获取内容统计
export function getContentStats() {
  return request({
    url: '/api/admin/content/stats',
    method: 'get'
  })
}

// 获取评论列表
export function getCommentList(postId, params) {
  return request({
    url: `/api/admin/content/posts/${postId}/comments`,
    method: 'get',
    params
  })
}

// 获取评论列表（兼容性函数，同getCommentList）
export function getComments(params) {
  const postId = params.postId;
  delete params.postId;
  return request({
    url: `/api/admin/content/posts/${postId}/comments`,
    method: 'get',
    params
  })
}

// 审核评论
export function reviewComment(commentId, status, reason) {
  return request({
    url: `/api/admin/content/comments/${commentId}/review`,
    method: 'put',
    params: {
      status,
      reason
    }
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
    url: `/api/admin/content/comments/${commentId}`,
    method: 'delete'
  })
}

// 获取内容发布趋势
export function getContentTrend(params) {
  return request({
    url: '/api/admin/content/trend',
    method: 'get',
    params
  })
}

// 获取举报列表
export function getReportList(params) {
  return request({
    url: '/api/admin/content/reports',
    method: 'get',
    params
  })
}

// 获取举报统计
export function getReportStats() {
  return request({
    url: '/api/admin/content/reports/stats',
    method: 'get'
  })
}

// 处理举报
export function processReport(data) {
  return request({
    url: `/api/admin/content/reports/${data.reportId}/process`,
    method: 'put',
    data
  })
} 