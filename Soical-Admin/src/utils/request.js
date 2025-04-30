import axios from 'axios';
import { useAuthStore } from '../stores/auth';

// 创建axios实例
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000, // 请求超时时间
});

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 获取token的方式改为直接从localStorage获取，避免Pinia store初始化问题
    let token;
    try {
      // 首先尝试从Pinia store获取
      const authStore = useAuthStore();
      token = authStore.token;
      console.log('Got token from Pinia store:', token);
    } catch (e) {
      // 如果失败，从localStorage获取
      console.warn('Failed to get token from Pinia store, getting from localStorage instead');
      token = localStorage.getItem('admin_token');
      console.log('Got token from localStorage:', token);
    }
    
    // 如果有token则带上
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('Added token to request headers');
    } else {
      console.warn('No token available for request');
    }
    
    console.log(`Sending ${config.method.toUpperCase()} request to ${config.url}`, config);
    
    return config;
  },
  error => {
    console.error('请求错误', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    console.log(`Received response from ${response.config.url}:`, response);
    const res = response.data;
    
    // 如果返回的状态码不是200，说明接口出错
    if (res.code !== undefined && res.code !== 200) {
      // 如果是未授权或token失效，清空登录状态并跳转登录页
      if (res.code === 401) {
        console.warn('Token expired or unauthorized, redirecting to login page');
        try {
          const authStore = useAuthStore();
          authStore.logout();
        } catch (e) {
          console.warn('Failed to use Pinia store for logout, removing token directly');
          localStorage.removeItem('admin_token');
        }
        window.location.href = '/login';
      }
      
      console.error('接口错误', res.message || '未知错误', res);
      return Promise.reject(new Error(res.message || '未知错误'));
    } else {
      // 判断响应数据的结构，进行适当处理
      if (res.data !== undefined) {
        // 标准格式：{ code: 200, data: xxx, message: 'xxx' }
        console.log('Standard API response with data field:', res.data);
        return res.data;
      } else {
        // res 本身就是数据
        console.log('API response is data itself:', res);
        return res;
      }
    }
  },
  error => {
    // 处理HTTP错误状态码
    console.error('Response error:', error);
    
    if (error.response) {
      console.error('Response error details:', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers,
        config: error.config
      });
      
      if (error.response.status === 401) {
        console.warn('401 Unauthorized error, redirecting to login');
        try {
          const authStore = useAuthStore();
          authStore.logout();
        } catch (e) {
          console.warn('Failed to use Pinia store for logout, removing token directly');
          localStorage.removeItem('admin_token');
        }
        window.location.href = '/login';
      }
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Request setup error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default instance; 