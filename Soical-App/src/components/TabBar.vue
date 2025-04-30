<template>
  <div class="tab-bar">
    <div class="tab-bar-content">
      <div 
        v-for="(item, index) in tabItems" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === item.path }"
        @click="handleTabClick(item.path)"
      >
        <el-icon class="tab-icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="tab-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, ChatDotRound, Compass, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const currentTab = ref(route.path)

const tabItems = [
  { path: '/home', label: '首页', icon: House },
  { path: '/chat', label: '消息', icon: ChatDotRound },
  { path: '/discover', label: '发现', icon: Compass },
  { path: '/profile', label: '我的', icon: User }
]

const handleTabClick = (path) => {
  if (currentTab.value !== path) {
    router.push(path)
  }
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  currentTab.value = newPath
})
</script>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: transparent;
  z-index: 100;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .tab-bar-content {
    display: flex;
    align-items: center;
    justify-content: space-around;
    width: 100%;
    max-width: 500px;
    height: 50px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 25px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 0 10px;
    transition: all 0.3s ease;
    
    .tab-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      padding: 0 15px;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
      
      .tab-icon {
        font-size: 22px;
        margin-bottom: 2px;
        transition: all 0.3s ease;
        color: #666;
      }
      
      .tab-label {
        font-size: 12px;
        color: #666;
        transition: all 0.3s ease;
      }
      
      &.active {
        .tab-icon {
          color: #409EFF;
          transform: scale(1.1);
        }
        
        .tab-label {
          color: #409EFF;
          font-weight: 500;
        }
        
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 4px;
          height: 4px;
          background: #409EFF;
          border-radius: 50%;
          transition: all 0.3s ease;
        }
      }
      
      &:hover {
        .tab-icon {
          transform: scale(1.1);
        }
        
        .tab-label {
          font-weight: 500;
        }
      }
    }
  }
}

// 添加页面内容区域底部padding，防止内容被底部导航栏遮挡
:deep(.app-container) {
  padding-bottom: 70px;
}
</style> 