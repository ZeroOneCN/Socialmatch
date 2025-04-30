<template>
  <div class="app-layout">
    <!-- Main content -->
    <div class="content-area">
      <transition name="fade" mode="out-in">
        <router-view></router-view>
      </transition>
    </div>
    
    <!-- Bottom tab bar -->
    <van-tabbar 
      v-model="activeTab" 
      route 
      fixed 
      safe-area-inset-bottom
      class="custom-tabbar"
    >
      <van-tabbar-item to="/recommend" class="tab-item">
        <template #icon="props">
          <div class="tab-icon-wrapper">
            <van-icon :name="props.active ? 'fire' : 'fire-o'" class="tab-icon" :class="{ 'active': props.active }" />
            <div class="tab-indicator" :class="{ 'active-indicator': props.active }"></div>
          </div>
        </template>
        <span :class="{ 'active-text': activeTab === 0 }">推荐</span>
      </van-tabbar-item>
      
      <van-tabbar-item to="/message" class="tab-item">
        <template #icon="props">
          <div class="tab-icon-wrapper">
            <van-icon :name="props.active ? 'chat' : 'chat-o'" class="tab-icon" :class="{ 'active': props.active }" />
            <div class="tab-indicator" :class="{ 'active-indicator': props.active }"></div>
          </div>
        </template>
        <span :class="{ 'active-text': activeTab === 1 }">消息</span>
      </van-tabbar-item>
      
      <van-tabbar-item to="/community" class="tab-item">
        <template #icon="props">
          <div class="tab-icon-wrapper">
            <van-icon :name="props.active ? 'friends' : 'friends-o'" class="tab-icon" :class="{ 'active': props.active }" />
            <div class="tab-indicator" :class="{ 'active-indicator': props.active }"></div>
          </div>
        </template>
        <span :class="{ 'active-text': activeTab === 2 }">社区</span>
      </van-tabbar-item>
      
      <van-tabbar-item to="/profile" class="tab-item">
        <template #icon="props">
          <div class="tab-icon-wrapper">
            <van-icon :name="props.active ? 'user' : 'user-o'" class="tab-icon" :class="{ 'active': props.active }" />
            <div class="tab-indicator" :class="{ 'active-indicator': props.active }"></div>
          </div>
        </template>
        <span :class="{ 'active-text': activeTab === 3 }">我的</span>
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '../stores/theme'

// 初始激活的标签页
const activeTab = ref(0)
const route = useRoute()
const themeStore = useThemeStore()

// 根据当前路由设置激活的标签页
const setActiveTabByRoute = () => {
  const path = route.path
  if (path.includes('/recommend')) {
    activeTab.value = 0
  } else if (path.includes('/message')) {
    activeTab.value = 1
  } else if (path.includes('/community')) {
    activeTab.value = 2
  } else if (path.includes('/profile')) {
    activeTab.value = 3
  }
}

// 监听路由变化
watch(() => route.path, setActiveTabByRoute)

// 组件挂载时设置激活的标签页
onMounted(() => {
  setActiveTabByRoute()
  console.log('TabBarLayout mounted, current route:', route.path)
})
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.content-area {
  flex: 1;
  overflow-y: visible;
  padding-bottom: calc(var(--tabbar-height) + var(--safe-area-bottom));
}

/* 自定义TabBar样式 */
.custom-tabbar {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  background-color: var(--bg-primary);
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.05);
  transition: background-color var(--transition-normal) var(--ease-out);
  padding: 6px 0;
}

/* 自定义TabBar项样式 */
.tab-item {
  position: relative;
  transition: transform var(--transition-fast) var(--ease-out);
}

.tab-item:active {
  transform: scale(0.95);
}

.tab-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.tab-icon {
  font-size: 22px;
  margin-bottom: 4px;
  transition: all var(--transition-normal) var(--ease-out);
  color: var(--text-tertiary);
}

.tab-icon.active {
  color: var(--primary-color);
  transform: translateY(-2px) scale(1.1);
}

.tab-indicator {
  position: absolute;
  bottom: -6px;
  width: 0;
  height: 3px;
  border-radius: 3px;
  background-color: var(--primary-color);
  transition: width var(--transition-normal) var(--ease-out);
}

.active-indicator {
  width: 16px;
}

.active-text {
  color: var(--primary-color);
  font-weight: 500;
  transition: color var(--transition-normal) var(--ease-out);
}

/* 暗黑模式下的调整 */
[data-theme="dark"] .custom-tabbar {
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.2);
}
</style> 