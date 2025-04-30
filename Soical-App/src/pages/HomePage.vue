<template>
  <div class="page-container">
    <div class="home-container">
      <h1 class="page-title">首页</h1>
      
      <div class="text-center">
        <p>欢迎回来，{{ displayName }}</p>
        
        <div style="margin: 24px 16px;">
          <van-button round type="danger" block @click="handleLogout">
            退出登录
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '../stores/user'

// 路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 获取用户显示名（优先显示昵称，其次是用户名）
const displayName = computed(() => userStore.nickname || userStore.username || '用户')

// 处理退出登录
const handleLogout = () => {
  userStore.logout()
  showToast({
    type: 'success',
    message: '已退出登录'
  })
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 400px;
  margin: 0 auto;
  margin-top: 60px;
}
</style> 