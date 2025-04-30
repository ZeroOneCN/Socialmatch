import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getAdminInfo } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const user = ref(null)
  const error = ref('')
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  
  // Actions
  // 登录方法
  async function login(username, password) {
    try {
      error.value = ''
      const response = await loginApi({ username, password })
      token.value = response.token
      localStorage.setItem('admin_token', token.value)
      
      // 获取用户信息
      await fetchUserInfo()
      
      return true
    } catch (err) {
      console.error('登录失败', err)
      error.value = err.message || '登录失败，请检查用户名和密码'
      return false
    }
  }
  
  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return null
    
    try {
      const userInfo = await getAdminInfo()
      user.value = userInfo
      return userInfo
    } catch (err) {
      console.error('获取用户信息失败', err)
      return null
    }
  }
  
  // 登出方法
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('admin_token')
  }
  
  // 初始化 - 如果有token则自动获取用户信息
  if (token.value) {
    fetchUserInfo()
  }
  
  return {
    token,
    user,
    error,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo
  }
}) 