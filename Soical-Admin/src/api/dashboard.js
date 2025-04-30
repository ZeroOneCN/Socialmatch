import request from '@/utils/request'

/**
 * 获取用户统计数据
 * @returns {Promise<any>} 用户统计数据
 */
export function getUserStats() {
  return request({
    url: '/api/admin/statistics/users',
    method: 'get'
  })
}

/**
 * 获取内容统计数据
 * @returns {Promise<any>} 内容统计数据
 */
export function getContentStats() {
  return request({
    url: '/api/admin/statistics/content',
    method: 'get'
  })
}

/**
 * 获取系统概要信息
 * @returns {Promise<any>} 系统概要信息
 */
export function getSystemSummary() {
  return request({
    url: '/api/admin/statistics/system',
    method: 'get'
  })
}

/**
 * 获取用户匹配统计数据
 * @returns {Promise<any>} 用户匹配统计数据
 */
export function getMatchStats() {
  return request({
    url: '/api/admin/statistics/match-stats',
    method: 'get'
  })
}

/**
 * 获取用户性别分布数据
 * @returns {Promise<any>} 用户性别分布数据
 */
export function getUserGenderDistribution() {
  return request({
    url: '/api/admin/statistics/user-gender',
    method: 'get'
  })
}

/**
 * 获取用户增长趋势数据
 * @param {string} type - 时间范围：week或month
 * @returns {Promise<any>} 用户增长趋势数据
 */
export function getUserGrowthTrend(type) {
  return request({
    url: '/api/admin/statistics/user-growth',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取匹配趋势数据
 * @param {string} type - 时间范围：week或month
 * @returns {Promise<any>} 匹配趋势数据
 */
export function getMatchTrend(type) {
  return request({
    url: '/api/admin/statistics/match-trend',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取内容发布趋势数据
 * @param {string} type - 时间范围：week或month
 * @returns {Promise<any>} 内容发布趋势数据
 */
export function getContentPublishTrend(type) {
  return request({
    url: '/api/admin/statistics/content-trend',
    method: 'get',
    params: { type }
  })
} 