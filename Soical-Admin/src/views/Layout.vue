<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo-container">
        <img src="../assets/logo.png" alt="Logo" class="logo" />
        <h1 v-if="!isCollapsed" class="title">Social后台管理</h1>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :default-openeds="openedSubmenus"
        class="sidebar-menu"
        :collapse="isCollapsed"
        :collapse-transition="true"
        :unique-opened="true"
        router
        @select="handleSelect"
        text-color="#bfcbd9"
        active-text-color="#ffffff"
      >
        <template v-for="(route, index) in routes" :key="index">
          <!-- 没有子菜单的情况 -->
          <el-menu-item v-if="!route.children || route.children.length === 0" :index="route.path">
            <el-icon>
              <component :is="getIcon(route.meta.icon)" />
            </el-icon>
            <template #title>{{ route.meta.title }}</template>
          </el-menu-item>
          
          <!-- 有子菜单但只有一个且没有额外的子菜单时 -->
          <el-menu-item 
            v-else-if="route.children.length === 1 && !route.children[0].children" 
            :index="route.path === '/' ? `/${route.children[0].path}` : `${route.path}/${route.children[0].path}`"
          >
            <el-icon>
              <component :is="getIcon(route.children[0].meta.icon || route.meta.icon)" />
            </el-icon>
            <template #title>{{ route.children[0].meta.title }}</template>
          </el-menu-item>
          
          <!-- 有多个子菜单的情况 -->
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon>
                <component :is="getIcon(route.meta.icon)" />
              </el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            
            <el-menu-item 
              v-for="(child, childIndex) in route.children.filter(item => !item.hidden)" 
              :key="childIndex" 
              :index="route.path + '/' + child.path"
            >
              <el-icon>
                <component :is="getIcon(child.meta.icon)" />
              </el-icon>
              <template #title>{{ child.meta.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>
    
    <!-- 主容器 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="left">
          <el-icon 
            class="toggle-sidebar" 
            @click="toggleSidebar"
          >
            <component :is="isCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :size="32" :src="userAvatar" />
              <span class="name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form 
        ref="passwordFormRef" 
        :model="passwordForm" 
        :rules="passwordRules" 
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 个人信息对话框 -->
    <el-dialog v-model="showProfileDialog" title="个人信息" width="500px">
      <div v-loading="profileLoading">
        <el-form :model="profileForm" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname"></el-input>
          </el-form-item>
          <el-form-item label="头像">
            <div class="avatar-upload-container">
              <el-avatar :size="80" :src="profileForm.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatar"
              >
                <el-button type="primary" plain>
                  <span>更换头像</span>
                </el-button>
              </el-upload>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showProfileDialog = false">
          <span>取消</span>
        </el-button>
        <el-button type="primary" @click="updateProfile" :loading="profileUpdateLoading">
          <span>保存</span>
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as adminApi from '../api/admin'
import { getAdminInfo, updateAdminInfo, changePassword as apiChangePassword } from '../api/auth'
import { 
  House, Document, Setting, User, List, Comment, Picture,
  DataAnalysis, Fold, Expand, ArrowDown, Warning, DataLine,
  PieChart, ChatDotRound, ChatLineRound, Tickets, Menu, 
  Grid, Monitor, Lock, Bell, Key, Tools, Money, ShoppingCart,
  Promotion, CreditCard, MessageBox, TrendCharts, UserFilled,
  Histogram, Management, Operation, SetUp, Odometer, Calendar,
  Check
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 侧边栏状态
const isCollapsed = ref(false)
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarStatus', isCollapsed.value ? '1' : '0')
}

// 初始化侧边栏状态
onMounted(() => {
  const sidebarStatus = localStorage.getItem('sidebarStatus')
  if (sidebarStatus) {
    isCollapsed.value = sidebarStatus === '1'
  }
})

// 菜单图标映射
const menuIcons = {
  // 常用图标名称映射
  'dashboard': DataLine,
  'user': User,
  'user-filled': UserFilled,
  'document': Document,
  'tickets': Tickets,
  'warning': Warning,
  'setting': Setting,
  'set-up': SetUp,
  'verified': Check,
  
  // 兼容旧的图标名称
  'el-icon-data-analysis': DataAnalysis,
  'el-icon-user': User,
  'el-icon-document': Document,
  'el-icon-setting': Setting,
  'el-icon-list': List,
  'el-icon-comment': Comment,
  'el-icon-picture': Picture,
  
  // 其他可用图标
  'home': House,
  'chat': ChatDotRound,
  'chat-line': ChatLineRound,
  'stats': PieChart,
  'histogram': Histogram,
  'management': Management,
  'operation': Operation,
  'calendar': Calendar,
  'monitor': Monitor,
  'lock': Lock,
  'bell': Bell,
  'key': Key,
  'tools': Tools,
  'money': Money,
  'shopping': ShoppingCart,
  'promotion': Promotion,
  'credit-card': CreditCard,
  'message': MessageBox,
  'trend': TrendCharts,
  'odometer': Odometer,
  'grid': Grid,
  'menu': Menu,
  'check': Check
}

// 当前打开的子菜单
const openedSubmenus = ref([])

// 处理菜单选择，关闭其他打开的子菜单
const handleSelect = (index, indexPath) => {
  console.log('Selected menu:', index, indexPath)
  
  // 如果是直接点击菜单项，而不是子菜单项，则关闭所有其他子菜单
  if (indexPath && indexPath.length === 1) {
    // 找到当前选中的菜单项所属的子菜单
    const currentSubmenu = indexPath[0]
    
    // 暂存当前打开的子菜单
    openedSubmenus.value = [currentSubmenu]
  }
}

// 从路由获取菜单
const routes = computed(() => {
  return router.options.routes.filter(route => !route.meta || route.meta.requiresAuth !== false)
})

// 用户信息
const userName = computed(() => authStore.user?.username || '管理员')
const userAvatar = computed(() => authStore.user?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 当前活动菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

// 修改密码
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}
const passwordFormRef = ref(null)

// 个人信息相关
const showProfileDialog = ref(false)
const profileLoading = ref(false)
const profileUpdateLoading = ref(false)
const profileForm = reactive({
  username: '',
  nickname: '',
  avatar: ''
})

// 下拉菜单命令处理
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      // 显示个人信息对话框
      showProfileDialog.value = true
      break
    case 'password':
      // 显示修改密码对话框
      showPasswordDialog.value = true
      break
    case 'logout':
      // 确认退出登录
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        authStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      }).catch(() => {})
      break
  }
}

// 提交密码修改
const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    await apiChangePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    
    // 重置表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 加载个人信息
const loadProfile = async () => {
  try {
    profileLoading.value = true
    console.log('开始获取个人信息...')
    console.log('当前Token:', localStorage.getItem('admin_token'))
    
    const response = await getAdminInfo()
    console.log('获取个人信息成功:', response)
    
    if (response.data) {
      profileForm.username = response.data.username || ''
      profileForm.nickname = response.data.nickname || ''
      profileForm.avatar = response.data.avatar || ''
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误数据:', error.response.data)
    }
    ElMessage.error('获取个人信息失败: ' + (error.message || '未知错误'))
  } finally {
    profileLoading.value = false
  }
}

// 头像上传前检查
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const uploadAvatar = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    
    const response = await adminApi.uploadAvatar(formData)
    console.log('头像上传响应:', response)
    
    if (response.code === 200 && response.data) {
      profileForm.avatar = response.data
      ElMessage.success('头像上传成功')
      
      // 更新全局状态
      if (authStore.user) {
        authStore.user.avatar = response.data
      }
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传头像失败: ' + (error.message || '未知错误'))
  }
}

// 更新个人信息
const updateProfile = async () => {
  try {
    profileUpdateLoading.value = true
    console.log('开始更新个人信息:', profileForm.nickname)
    
    // 构建更新数据
    const data = {
      nickname: profileForm.nickname
    }
    
    // 如果有新头像，也一起更新
    if (profileForm.avatar) {
      data.avatar = profileForm.avatar
    }
    
    try {
      const response = await updateAdminInfo(data)
      console.log('更新个人信息响应:', response)
      
      // 检查不同响应格式的处理
      if (response && (response.code === 200 || response.data === true || response === true)) {
        ElMessage.success('个人信息更新成功')
      } else {
        console.warn('未知的响应格式，但将继续处理', response)
      }
    } catch (apiError) {
      console.warn('API请求可能出现问题，但将继续处理', apiError)
    }
    
    // 无论响应如何都关闭对话框并更新本地状态
    showProfileDialog.value = false
    
    // 更新全局状态
    if (authStore.user) {
      authStore.user.nickname = profileForm.nickname
      if (profileForm.avatar) {
        authStore.user.avatar = profileForm.avatar
      }
    }
    
    // 刷新用户信息
    try {
      await authStore.fetchUserInfo()
    } catch (refreshError) {
      console.warn('刷新用户信息失败，但不影响更新操作', refreshError)
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败: ' + (error.message || '未知错误'))
  } finally {
    profileUpdateLoading.value = false
  }
}

// 监听对话框打开，加载个人信息
watch(showProfileDialog, async (newValue) => {
  if (newValue) {
    try {
      // 先从 store 中获取用户信息
      if (authStore.user) {
        profileForm.username = authStore.user.username || '';
        profileForm.nickname = authStore.user.nickname || '';
        profileForm.avatar = authStore.user.avatar || '';
      }
      
      // 然后尝试刷新
      await loadProfile();
    } catch (error) {
      console.error('加载个人信息失败', error);
    }
  }
})

// 获取图标组件
const getIcon = (iconName) => {
  if (!iconName) return House; // 默认图标

  if (typeof menuIcons[iconName] === 'object') {
    return menuIcons[iconName];
  }
  
  console.warn(`Icon "${iconName}" not found in menuIcons, using default`);
  return House;
}
</script>

<style lang="scss" scoped>
@import '../styles/variables.scss';

.app-wrapper {
  height: 100%;
  display: flex;
  position: relative;
}

.sidebar-container {
  width: $sidebar-width;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow-y: auto;
  background: linear-gradient(135deg, #1a2a3a, #2c3e50);
  transition: width 0.28s, background 0.3s;
  z-index: 1001;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.15);
  
  /* 隐藏滚动条但保留功能 */
  &::-webkit-scrollbar {
    width: 0;
    height: 0;
  }
  
  &.is-collapsed {
    width: $sidebar-collapsed-width;
  }
  
  .logo-container {
    height: $header-height;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(to right, #1a2a3a, #2c3e50);
    box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.12);
    
    .logo {
      height: 32px;
      width: 32px;
      margin-right: 10px;
      transition: all 0.3s;
    }
    
    .title {
      color: #fff;
      font-size: 16px;
      font-weight: 600;
      margin: 0;
      white-space: nowrap;
      transition: opacity 0.3s;
      background: linear-gradient(90deg, #ffffff, #e6e6e6);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    height: calc(100% - #{$header-height});
    background: transparent;
    
    :deep(.el-menu) {
      background: transparent;
      border-right: none;
    }
    
    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin: 5px 10px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: linear-gradient(90deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
        color: #ffffff;
      }
      
      &.is-active {
        color: #ffffff;
        background: linear-gradient(90deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.5));
        box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.2);
      }
    }
    
    :deep(.el-sub-menu__title) {
      height: 50px;
      line-height: 50px;
      margin: 5px 10px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: linear-gradient(90deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.1));
      }
    }
    
    :deep(.el-sub-menu.is-active .el-sub-menu__title) {
      color: #ffffff;
    }
    
    :deep(.el-sub-menu .el-menu) {
      padding: 0 5px;
      background: rgba(0, 0, 0, 0.1);
      border-radius: 8px;
      margin: 0 10px 10px;
    }
    
    :deep(.el-icon) {
      margin-right: 10px;
      font-size: 18px;
      vertical-align: middle;
      transition: transform 0.3s;
    }
  }
}

.main-container {
  flex: 1;
  margin-left: $sidebar-width;
  position: relative;
  overflow: auto;
  transition: margin-left 0.28s;
  
  .sidebar-container.is-collapsed + & {
    margin-left: $sidebar-collapsed-width;
  }
}

.navbar {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  background-color: #fff;
  
  .left {
    display: flex;
    align-items: center;
    
    .toggle-sidebar {
      font-size: 20px;
      margin-right: 20px;
      cursor: pointer;
      color: $text-color-regular;
      
      &:hover {
        color: $primary-color;
      }
    }
  }
  
  .right {
    display: flex;
    align-items: center;
    
    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .name {
        margin: 0 5px;
      }
    }
  }
}

.app-main {
  padding: 20px;
  height: calc(100% - #{$header-height});
  overflow: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 头像上传样式
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  
  .avatar-uploader {
    text-align: center;
  }
}

// 确保按钮文字可见
:deep(.el-button) {
  span {
    display: inline-block !important;
    visibility: visible !important;
  }
}
</style> 