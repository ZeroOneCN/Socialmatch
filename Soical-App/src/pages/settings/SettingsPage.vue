<template>
  <div class="settings-page">
    <van-nav-bar
      title="设置"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="content-container">
      <van-cell-group inset>
        <van-cell title="个人资料" is-link to="/settings/profile" />
        <!--<van-cell title="账号安全" is-link to="/settings/security" />-->
        <van-cell title="同频偏好设置" is-link to="/settings/frequency" />
        <!--<van-cell title="隐私设置" is-link to="/settings/privacy" />-->
      </van-cell-group>
      
      <!--<van-cell-group inset title="显示设置">
        <van-cell center title="外观主题">
          <template #right-icon>
            <div class="theme-toggle">
              <van-button 
                size="small" 
                :type="effectiveTheme === 'light' ? 'primary' : 'default'" 
                class="theme-btn" 
                @click="setTheme('light')"
              >
                <van-icon name="sunny-o" />
                亮色
              </van-button>
              <van-button 
                size="small" 
                :type="effectiveTheme === 'dark' ? 'primary' : 'default'" 
                class="theme-btn" 
                @click="setTheme('dark')"
              >
                <van-icon name="moon-o" />
                暗色
              </van-button>
              <van-button 
                size="small" 
                :type="currentTheme === 'auto' ? 'primary' : 'default'" 
                class="theme-btn" 
                @click="setTheme('auto')"
              >
                <van-icon name="setting-o" />
                跟随系统
              </van-button>
            </div>
          </template>
        </van-cell>
      </van-cell-group>-->
      
      <!--<van-cell-group inset title="通知设置">
        <van-cell title="消息通知" is-link to="/settings/notifications" />
        <van-cell title="社区通知" is-link to="/settings/community-notifications" />
      </van-cell-group>-->
      
      <van-cell-group inset title="其他">
        <van-cell title="清除缓存" @click="clearCache" />
        <van-cell title="关于我们" is-link to="/about" />
        <van-cell title="退出登录" @click="confirmLogout" clickable />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { showDialog, showToast, showSuccessToast } from 'vant'
import { useUserStore } from '../../stores/user'
import { useThemeStore } from '../../stores/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 主题相关计算属性
const currentTheme = computed(() => themeStore.currentTheme)
const effectiveTheme = computed(() => themeStore.effectiveTheme)

// 设置主题
const setTheme = (theme) => {
  themeStore.setTheme(theme)
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 清除缓存
const clearCache = () => {
  showDialog({
    title: '确认清除',
    message: '确定清除缓存数据吗？这不会删除您的账号或个人信息。',
    showCancelButton: true,
    confirmButtonText: '清除',
    cancelButtonText: '取消'
  }).then(() => {
    // 清除缓存的逻辑
    setTimeout(() => {
      showSuccessToast('缓存已清除')
    }, 500)
  }).catch(() => {
    // 取消操作
  })
}

// 确认退出登录
const confirmLogout = () => {
  showDialog({
    title: '确认退出',
    message: '确定要退出登录吗？',
    showCancelButton: true,
    confirmButtonText: '退出',
    cancelButtonText: '取消'
  }).then(() => {
    userStore.logout()
    showToast('已退出登录')
    router.push('/recommend')
  }).catch(() => {
    // 取消操作
  })
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding-bottom: 30px;
  transition: background-color var(--transition-normal) var(--ease-out);
}

.content-container {
  padding: 16px;
}

.van-cell-group {
  margin-bottom: 16px;
  border-radius: var(--border-radius);
  overflow: hidden;
}

.theme-toggle {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  transition: color var(--transition-normal) var(--ease-out);
}

.theme-btn {
  border-radius: var(--border-radius-sm);
  font-size: 12px;
  display: flex;
  align-items: center;
  transition: all var(--transition-normal) var(--ease-out);
}

.theme-btn .van-icon {
  margin-right: 4px;
  font-size: 14px;
}
</style> 