/**
 * API配置文件
 * 定义API基础URL和其他API相关的常量
 */

// API基础URL，根据环境变量自动选择
export const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://83.229.124.87:8080/' // 生产环境API地址
  : 'http://localhost:8080';      // 开发环境API地址

// API超时时间（毫秒）
export const API_TIMEOUT = 15000;

// API版本
export const API_VERSION = 'v1';

// 上传文件的最大大小（字节）
export const MAX_UPLOAD_SIZE = 5 * 1024 * 1024; // 5MB

// 默认分页大小
export const DEFAULT_PAGE_SIZE = 10;

// 是否在控制台打印API请求日志
export const LOG_API_REQUESTS = process.env.NODE_ENV !== 'production';

export default {
  API_BASE_URL,
  API_TIMEOUT,
  API_VERSION,
  MAX_UPLOAD_SIZE,
  DEFAULT_PAGE_SIZE,
  LOG_API_REQUESTS
}; 