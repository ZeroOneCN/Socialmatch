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
 * 获取所有省份列表
 */
export function getAllProvinces() {
  return service({
    url: '/api/common/provinces',
    method: 'get'
  })
}

/**
 * 获取指定省份的城市列表
 * @param {string} provinceCode 省份代码
 */
export function getCitiesByProvince(provinceCode) {
  return service({
    url: `/api/common/cities/${provinceCode}`,
    method: 'get'
  })
}

/**
 * 获取所有职业列表
 */
export function getAllOccupations() {
  return service({
    url: '/api/common/occupations',
    method: 'get'
  })
}

/**
 * 获取所有学历列表
 */
export function getAllEducations() {
  return service({
    url: '/api/common/educations',
    method: 'get'
  })
}

/**
 * 获取文件上传Token/签名
 * 适用于云存储文件上传
 */
export function getUploadToken(fileType) {
  return service({
    url: '/api/common/upload/token',
    method: 'get',
    params: {
      fileType
    }
  })
}

/**
 * 获取应用配置信息
 */
export function getAppConfig() {
  return service({
    url: '/api/common/config',
    method: 'get'
  })
}

/**
 * 获取省市两级数据
 * @returns {Promise} - 返回请求Promise
 */
export function getProvincesWithCities() {
  return service({
    url: '/api/common/cities/all',
    method: 'get'
  })
}

/**
 * 获取所有职业类别
 * @returns {Promise} - 返回请求Promise
 */
export function getOccupationCategories() {
  return service({
    url: '/api/common/occupation/categories',
    method: 'get'
  })
}

/**
 * 根据职业类别获取职业列表
 * @param {string} category - 职业类别
 * @returns {Promise} - 返回请求Promise
 */
export function getOccupationsByCategory(category) {
  return service({
    url: `/api/common/occupation/list/${category}`,
    method: 'get'
  })
}

/**
 * 获取所有按类别分组的职业
 * @returns {Promise} - 返回请求Promise
 */
export function getAllOccupationsGroupByCategory() {
  return service({
    url: '/api/common/occupation/group',
    method: 'get'
  })
}

/**
 * 根据坐标获取位置信息
 * @param {Object} coords - 坐标对象，包含latitude和longitude
 * @returns {Promise} - 返回位置信息
 */
export function getLocationByCoords(coords) {
  return service({
    url: '/api/common/location/coords',
    method: 'get',
    params: {
      latitude: coords.latitude,
      longitude: coords.longitude
    }
  })
}

/**
 * 通过IP地址获取位置信息
 * @returns {Promise} - 返回位置信息
 */
export function getLocationByIp() {
  return service({
    url: '/api/common/location/ip',
    method: 'get'
  })
}

export default {
  getAllProvinces,
  getCitiesByProvince,
  getProvincesWithCities,
  getOccupationCategories,
  getOccupationsByCategory,
  getAllOccupationsGroupByCategory,
  getAllOccupations,
  getAllEducations,
  getUploadToken,
  getAppConfig,
  getLocationByCoords,
  getLocationByIp
} 