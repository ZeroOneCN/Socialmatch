import axios from 'axios'
import { useUserStore } from '../stores/user'
import { showToast } from 'vant'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    // 添加认证头
    const token = userStore.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 添加缓存控制头，防止缓存
    config.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    config.headers['Pragma'] = 'no-cache'
    config.headers['Expires'] = '0'
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 调试日志：记录所有API响应
    console.log(`API响应 (${response.config.url}):`, res)
    
    // 如果是直接返回的二进制流或文件，直接返回response
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    
    // 如果服务器直接返回了boolean值（有些接口可能直接返回true/false）
    if (typeof res === 'boolean') {
      return { success: res, data: res, code: 200 }
    }
    
    // AI相关接口特殊处理
    if (response.config.url.includes('/system/settings/ai') || 
        response.config.url.includes('/ai/chat') || 
        response.config.url.includes('/ai/history')) {
      console.log('处理AI相关接口响应:', response.config.url, res);
      // 如果是AI设置接口
      if (response.config.url.includes('/system/settings/ai')) {
        // 对于AI配置接口，直接返回数据，让业务代码处理
        return res;
      }
      
      // 如果是AI聊天或历史接口
      if (res.code === 0 || res.code === 200) {
        return res;
      }
      // 如果没有标准格式，尝试直接返回，让业务层处理
      return res;
    }
    
    // 处理不同格式的API响应
    if (res.code !== undefined) {
      // 标准响应格式：{ code: 200, data: xxx, message: 'xxx' }
      if (res.code !== 200) {
        // AI接口使用code=0作为成功
        if (res.code === 0 && res.data) {
          // 添加success字段方便前端判断
          if (!('success' in res)) {
            res.success = true;
          }
          return res;
        }
        
        console.error(`API错误 (${response.config.url}):`, res)
        showToast(res.message || '请求出错')
        
        // 如果是未授权，清除token并跳转到登录页
        if (res.code === 401) {
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
        }
        
        return Promise.reject(new Error(res.message || '请求出错'))
      }
      
      // 添加success字段方便前端判断（服务端可能没有这个字段）
      if (res.code === 200 && !('success' in res)) {
        res.success = true
      }
      
      return res
    } else if (res.success !== undefined) {
      // 另一种响应格式：{ success: true, data: xxx, message: 'xxx' }
      if (!res.success) {
        console.error(`API错误 (${response.config.url}):`, res)
        showToast(res.message || '请求出错')
        return Promise.reject(new Error(res.message || '请求出错'))
      }
      return res
    } else {
      // 没有标准格式，直接返回原始数据
      return { success: true, data: res, code: 200 }
    }
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误，请稍后重试'
    // 尝试提取更详细的错误信息
    if (error.response) {
      console.log('服务器响应数据:', error.response.data)
      console.log('请求配置:', error.config)
      
      message = error.response.data?.message || 
                error.response.data?.error || 
                `服务器错误 (${error.response.status})`
      
      // 如果是表单提交错误，输出更多调试信息
      if (error.config.headers && 
          (error.config.headers['Content-Type'] === 'multipart/form-data' || 
           error.config.data instanceof FormData)) {
        console.error('表单提交错误:', {
          url: error.config.url,
          method: error.config.method,
          status: error.response.status,
          response: error.response.data
        })
        
        message = '上传失败，请重试'
      }
    } else if (error.request) {
      console.error('没有收到响应:', error.request)
      message = '服务器无响应，请检查网络连接'
    } else if (error.message && error.message.includes('timeout')) {
      message = '请求超时，请重试'
    } 
    
    showToast(message)
    return Promise.reject(error)
  }
)

// 导出请求方法
export function request(config) {
  return service(config)
}

// 导出axios实例
export default service 