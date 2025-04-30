import request from './http'

/**
 * 提交举报
 * @param {Object} data 举报数据
 * @param {number} data.reportType 举报类型：1-动态，2-评论，3-用户
 * @param {number} data.targetId 被举报对象ID
 * @param {string} data.reason 举报原因
 * @param {string} data.content 详细描述
 * @param {Array} data.images 图片证据
 * @returns {Promise}
 */
export function submitReport(data) {
  return request({
    url: '/reports',
    method: 'post',
    data
  })
}

/**
 * 获取举报原因列表
 * @param {number} type 举报类型：1-动态，2-评论，3-用户
 * @returns {Promise}
 */
export function getReportReasons(type) {
  return request({
    url: '/reports/reasons',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取用户的举报历史
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页数量
 * @returns {Promise}
 */
export function getUserReportHistory(params) {
  return request({
    url: '/user/reports',
    method: 'get',
    params
  })
}

/**
 * 取消举报
 * @param {number} reportId 举报ID
 * @returns {Promise}
 */
export function cancelReport(reportId) {
  return request({
    url: `/reports/${reportId}/cancel`,
    method: 'put'
  })
} 