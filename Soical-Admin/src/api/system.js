import request from './request'

// 获取系统配置列表
export function getConfigList(params) {
  return request({
    url: '/api/admin/config/list',
    method: 'get',
    params
  })
}

// 更新系统配置
export function updateConfig(data) {
  return request({
    url: '/api/admin/config',
    method: 'put',
    data
  })
}

// 获取操作日志
export function getOperationLogs(params) {
  return request({
    url: '/api/admin/logs',
    method: 'get',
    params
  })
}

// 获取系统统计数据
export function getSystemStats() {
  return request({
    url: '/api/admin/system/stats',
    method: 'get'
  })
}

// 获取版本信息
export function getVersionInfo() {
  return request({
    url: '/api/admin/system/version',
    method: 'get'
  })
}

// 安全设置API
export function getSecuritySettings() {
  return request({
    url: '/api/system/settings/security',
    method: 'get'
  })
}

export function updateSecuritySettings(data) {
  return request({
    url: '/api/system/settings/security',
    method: 'post',
    data
  })
}

// 系统信息API
export function getSystemInfo() {
  return request({
    url: '/api/system/info',
    method: 'get'
  })
}

// 系统日志API
export function getSystemLogs(params) {
  return request({
    url: '/api/system/logs',
    method: 'get',
    params
  })
}

// 系统缓存API
export function clearSystemCache(type) {
  return request({
    url: '/api/system/cache/clear',
    method: 'post',
    data: { type }
  })
}

// 系统维护API
export function setMaintenanceMode(enabled, message) {
  return request({
    url: '/api/system/maintenance',
    method: 'post',
    data: { enabled, message }
  })
}

/**
 * 获取AI助手设置
 */
export function getAiSettings() {
  return request({
    url: '/api/admin/ai-config',
    method: 'get'
  });
}

/**
 * 更新AI助手设置
 * @param {Object} data AI助手设置数据
 */
export function updateAiSettings(data) {
  return request({
    url: '/api/admin/ai-config',
    method: 'put',
    data
  });
} 