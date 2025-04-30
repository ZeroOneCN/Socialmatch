<template>
  <div class="login-container">
    <div class="login-form-container">
      <div class="login-header">
        <h1 class="title">Social 管理系统</h1>
        <p class="subtitle">后台管理登录</p>
      </div>
      
      <div class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名"
            class="form-input"
          />
        </div>
        
        <div class="form-item">
          <label>密码</label>
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码"
            class="form-input"
            @keyup.enter="login"
          />
        </div>
        
        <div class="error-message" v-if="error">{{ error }}</div>
        
        <button 
          class="login-button" 
          @click="login"
          :disabled="isLoading"
        >
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 简化表单处理
const username = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)

// 简化登录逻辑
const login = async () => {
  // 简单验证
  if (!username.value) {
    error.value = '请输入用户名'
    return
  }
  
  if (!password.value || password.value.length < 6) {
    error.value = '密码长度至少为6个字符'
    return
  }
  
  try {
    isLoading.value = true
    error.value = ''
    
    console.log('Attempting login with username:', username.value)
    
    // 调用登录API
    const success = await authStore.login(username.value, password.value)
    
    if (success) {
      console.log('Login successful, token:', authStore.token)
      console.log('LocalStorage admin_token:', localStorage.getItem('admin_token'))
      
      // 确保token已正确保存到localStorage
      if (!localStorage.getItem('admin_token')) {
        console.warn('Token not saved to localStorage, saving manually')
        localStorage.setItem('admin_token', authStore.token)
      }
      
      console.log('Redirecting to dashboard')
      router.push('/')
    } else {
      console.error('Login failed:', authStore.error)
      error.value = authStore.error || '登录失败，请检查用户名和密码'
    }
  } catch (e) {
    console.error('Login error:', e)
    error.value = '登录出错，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-form-container {
  width: 380px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.form-input {
  width: 100%;
  height: 40px;
  line-height: 40px;
  padding: 0 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  background-color: #fff;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
}

.error-message {
  color: #f56c6c;
  font-size: 14px;
  margin: 5px 0 15px;
}

.login-button {
  width: 100%;
  height: 40px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.login-button:hover {
  background: #66b1ff;
}

.login-button:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
</style> 