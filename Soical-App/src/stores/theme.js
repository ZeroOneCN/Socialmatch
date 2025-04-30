import { defineStore } from 'pinia'

// 检查是否支持暗黑模式和用户偏好
const prefersDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches

// 尝试获取保存的主题设置
const getSavedTheme = () => {
  const savedTheme = localStorage.getItem('theme-mode')
  if (savedTheme) {
    return savedTheme
  }
  return 'auto' // 默认使用自动(跟随系统)模式
}

// 根据当前设置应用主题
const applyTheme = (mode) => {
  // 如果是自动模式，则根据系统设置决定
  if (mode === 'auto') {
    if (prefersDarkMode) {
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.setAttribute('data-theme', 'light')
    }
  } else {
    document.documentElement.setAttribute('data-theme', mode)
  }
}

// 监听系统主题变化
const setupThemeListener = (callback) => {
  if (window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    // 添加变更监听器
    const listener = (event) => {
      callback(event.matches ? 'dark' : 'light')
    }
    
    // 根据浏览器支持使用正确的方法监听变化
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', listener)
    } else {
      mediaQuery.addListener(listener)
    }
    
    // 返回清理函数
    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', listener)
      } else {
        mediaQuery.removeListener(listener)
      }
    }
  }
  return () => {}
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    currentTheme: getSavedTheme(),
    systemTheme: prefersDarkMode ? 'dark' : 'light',
    autoFollowSystem: getSavedTheme() === 'auto'
  }),
  
  getters: {
    // 最终应用的主题
    effectiveTheme: (state) => {
      if (state.currentTheme === 'auto') {
        return state.systemTheme
      }
      return state.currentTheme
    },
    
    isDarkMode: (state) => {
      // 如果是自动模式，根据系统主题决定
      if (state.currentTheme === 'auto') {
        return state.systemTheme === 'dark'
      }
      // 否则根据用户设置决定
      return state.currentTheme === 'dark'
    }
  },
  
  actions: {
    // 初始化主题
    initTheme() {
      applyTheme(this.currentTheme)
      
      // 监听系统主题变化
      setupThemeListener((newTheme) => {
        this.systemTheme = newTheme
        if (this.currentTheme === 'auto') {
          applyTheme('auto')
        }
      })
    },
    
    // 设置主题
    setTheme(theme) {
      this.currentTheme = theme
      localStorage.setItem('theme-mode', theme)
      applyTheme(theme)
    },
    
    // 切换主题
    toggleTheme() {
      if (this.currentTheme === 'light') {
        this.setTheme('dark')
      } else if (this.currentTheme === 'dark') {
        this.setTheme('auto')
      } else {
        this.setTheme('light')
      }
    },
    
    // 设置是否跟随系统
    setAutoFollowSystem(follow) {
      if (follow) {
        this.setTheme('auto')
      } else {
        this.setTheme(this.systemTheme) // 使用当前系统主题作为固定主题
      }
    }
  }
}) 