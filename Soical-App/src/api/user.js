import axios from 'axios'
import { API_BASE_URL } from '@/config/api'
import { getToken } from '@/utils/auth'

// 创建axios实例
const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

// 请求拦截器：添加token
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应
service.interceptors.response.use(
  response => {
    const res = response.data
    return res
  },
  error => {
    console.error('响应拦截器错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 获取用户信息
 * @param {number} userId 用户ID，不传则获取当前用户
 * @param {Object} extraParams 额外的查询参数
 */
export function getUserInfo(userId, extraParams = {}) {
  console.log('调用getUserInfo: userId =', userId, '额外参数 =', extraParams);
  return service({
    url: userId ? `/api/user/${userId}` : '/api/user/current',
    method: 'get',
    params: {
      _t: Date.now(), // 添加时间戳避免缓存
      ...extraParams
    }
  }).then(response => {
    console.log('getUserInfo响应:', response);
    
    // 检查响应中的昵称与用户资料中的昵称是否一致
    if (response && response.data && response.data.profile) {
      console.log('响应中用户数据: nickname =', response.data.nickname, 
                 'profile.nickname =', response.data.profile.nickname);
    }
    
    return response;
  }).catch(error => {
    console.error('getUserInfo失败:', error);
    throw error;
  });
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return service({
    url: '/api/user/current',
    method: 'get'
  })
}

/**
 * 获取用户资料
 * @param {number} userId 用户ID，不传则获取当前用户
 */
export function getUserProfile(userId) {
  return service({
    url: userId ? `/api/user/profile/${userId}` : '/api/user/profile',
    method: 'get'
  })
}

/**
 * 获取当前用户资料
 */
export function getCurrentUserProfile() {
  return service({
    url: '/api/user/profile',
    method: 'get',
    params: {
      _t: Date.now() // 添加时间戳避免缓存
    }
  })
}

/**
 * 更新用户资料
 * @param {Object} data 用户资料数据
 */
export function updateUserProfile(data) {
  console.log('API 调用: updateUserProfile', data);
  return service({
    url: '/api/user/profile',
    method: 'put',
    data
  }).then(response => {
    console.log('API 响应: updateUserProfile', response);
    return response;
  }).catch(error => {
    console.error('API 错误: updateUserProfile', error);
    throw error;
  });
}

/**
 * 更新用户基本信息
 * @param {Object} data 用户基本信息
 */
export function updateUserBasicInfo(data) {
  return service({
    url: '/api/user/basic',
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 * @param {FormData} formData 包含文件的FormData
 */
export function uploadAvatar(formData) {
  return service({
    url: '/api/user/avatar',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 获取用户相册
 */
export function getPhotos() {
  return service({
    url: '/api/user/photos',
    method: 'get'
  })
}

/**
 * 上传照片到相册
 * @param {FormData} formData 包含文件的FormData
 */
export function uploadPhoto(formData) {
  return service({
    url: '/api/user/photos',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 删除相册照片
 * @param {string} photoUrl 照片URL
 */
export function deletePhoto(photoUrl) {
  return service({
    url: '/api/user/photos',
    method: 'delete',
    data: { photoUrl }
  })
}

/**
 * 修改用户密码
 * @param {Object} data 包含旧密码和新密码
 */
export function changePassword(data) {
  return service({
    url: '/api/user/password',
    method: 'put',
    data
  })
}

/**
 * 注销当前用户
 */
export function logout() {
  return service({
    url: '/api/auth/logout',
    method: 'post'
  })
}

/**
 * 获取用户在线状态
 * @param {Array|Number} userIds 用户ID或用户ID数组
 */
export function getUserOnlineStatus(userIds) {
  // 确保传入的是数组
  const ids = Array.isArray(userIds) ? userIds : [userIds];
  
  console.log('获取用户在线状态, userIds:', ids);
  
  return service({
    url: '/api/user/online-status',
    method: 'get',
    params: { userIdsStr: ids.join(',') }
  });
}

/**
 * 获取用户统计数据
 * @param {number} userId 用户ID，不传则获取当前用户
 */
export function getUserStats(userId) {
  return service({
    url: userId ? `/api/user/${userId}/stats` : '/api/user/stats',
    method: 'get',
    params: {
      _t: Date.now() // 添加时间戳避免缓存
    }
  });
}

/**
 * 获取同频匹配统计数据
 * @returns {Promise<{matches: number, newMatches: number, conversations: number, rate: number, potentialMatches: number}>} 匹配统计数据
 */
export function getFrequencyStats() {
  return service({
    url: '/api/user/frequency/stats',
    method: 'get'
  });
}

/**
 * 获取同频匹配列表
 * @param {Object} params 分页参数
 * @returns {Promise<{records: Array<{matchId: number, userId: number, nickname: string, avatar: string, age: number, similarity: number, location: string, occupation: string}>}>} 匹配列表数据
 */
export function getFrequencyMatches(params) {
  return service({
    url: '/api/user/frequency/matches',
    method: 'get',
    params
  });
}

/**
 * 获取频率偏好设置
 */
export function getFrequencyPreferences() {
  return service({
    url: '/api/user/frequency/preferences',
    method: 'get'
  });
}

/**
 * 更新频率偏好设置
 * @param {Object} data 偏好设置数据
 */
export function updateFrequencyPreferences(data) {
  return service({
    url: '/api/user/frequency/preferences',
    method: 'put',
    data
  });
}

export default {
  getUserInfo,
  getCurrentUser,
  getUserProfile,
  getCurrentUserProfile,
  updateUserProfile,
  updateUserBasicInfo,
  uploadAvatar,
  getPhotos,
  uploadPhoto,
  deletePhoto,
  changePassword,
  logout,
  getUserOnlineStatus,
  getUserStats,
  getFrequencyStats,
  getFrequencyMatches,
  getFrequencyPreferences,
  updateFrequencyPreferences
} 