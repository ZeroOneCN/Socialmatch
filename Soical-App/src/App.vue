<template>
  <div class="app-container">
    <!-- 路由出口 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue';
import { useUserStore } from './stores/user';
import { useThemeStore } from './stores/theme';
import { useMessageStore } from './stores/message';

const userStore = useUserStore();
const themeStore = useThemeStore();
const messageStore = useMessageStore();

onMounted(async () => {
  console.log('应用启动');
  
  // 检查用户是否已登录
  if (userStore.isLoggedIn) {
    console.log('用户已登录，尝试连接WebSocket和启动状态心跳');
    
    try {
      // 连接WebSocket
      await messageStore.connectWebSocket();
      
      // 启动用户在线状态心跳检查
      messageStore.startUserStatusHeartbeat();
      
      console.log('应用启动时WebSocket连接和状态心跳已启动');
    } catch (error) {
      console.error('应用启动时连接WebSocket失败:', error);
    }
  } else {
    console.log('用户未登录，跳过WebSocket连接');
  }
  
  // 初始化主题
  themeStore.initTheme();
  
  // 恢复用户登录状态
  await userStore.restoreLoginState();
});

// 应用销毁前清理
onBeforeUnmount(() => {
  // 停止用户状态心跳
  messageStore.stopUserStatusHeartbeat();
  
  // 如果WebSocket已连接，断开连接
  if (messageStore.isWebSocketConnected) {
    messageStore.disconnectWebSocket();
  }
  
  console.log('应用销毁，已清理WebSocket和状态心跳');
});
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

#app {
  font-family: system-ui, Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
  width: 100%;
}

.app-container {
  height: 100%;
  width: 100%;
}

.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
