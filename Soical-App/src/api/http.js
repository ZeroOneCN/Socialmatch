import axios from 'axios'
import { showToast } from 'vant'
import { getToken } from '../utils/auth'
import router from '../router'

// 创建axios实例
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api', // API的基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
http.interceptors.request.use(
  config => {
    // 如果有token，添加到请求头
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加调试日志
    console.log(`发送${config.method.toUpperCase()}请求到 ${config.url}`, 
      config.params || config.data || {});
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  response => {
    const res = response.data
    console.log(`收到${response.config.method.toUpperCase()}响应来自 ${response.config.url}:`, res)
    
    // 处理二进制/文件响应
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response.data
    }
    
    // 处理不同类型的响应结构
    // 1. 标准结构: { code: 200, data: xxx, message: 'xxx' }
    if (res.code !== undefined) {
      if (res.code !== 200) {
        showToast({
          type: 'fail',
          message: res.message || '请求失败'
        })
        
        // 如果是401状态码，说明token无效，需要重新登录
        if (res.code === 401) {
          // 跳转到登录页
          router.push('/login')
        }
        
        return Promise.reject(new Error(res.message || '请求失败'))
      } else {
        return res
      }
    }
    // 2. 基于success的结构: { success: true, data: xxx, message: 'xxx' }
    else if (res.success !== undefined) {
      if (!res.success) {
        showToast({
          type: 'fail',
          message: res.message || '请求失败'
        })
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return res
    }
    // 3. 直接返回数据(没有包装结构)
    else {
      return { success: true, code: 200, data: res }
    }
  },
  error => {
    // 详细记录错误
    console.error('HTTP请求错误:', error);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    } else if (error.request) {
      console.error('没有收到响应, 请求是:', error.request);
    }
    
    // 处理响应错误
    let message = error.message
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          // 跳转到登录页
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || error.response.data?.error || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请检查网络'
    } else if (error.message.includes('Network Error')) {
      message = '网络错误，请检查网络连接'
    }
    
    showToast({
      type: 'fail',
      message: message
    })
    return Promise.reject(error)
  }
)

export default http 