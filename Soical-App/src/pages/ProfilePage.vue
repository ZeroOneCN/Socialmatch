<template>
  <div class="page-container">
    <div class="header">
      <h1 class="page-title">个人中心</h1>
    </div>
    
    <div class="profile-container">
      <!-- 用户基本信息卡片 -->
      <div class="user-card" @click="goToEditProfile">
        <div class="user-info">
          <img :src="avatar || '/avatar-placeholder.png'" class="avatar" alt="用户头像" />
          <div class="user-details">
            <h2 class="nickname">
              {{ displayName }}
              <!-- 认证标识 -->
              <span class="verification-badge identity" :class="{'badge-verified': isIdentityVerified, 'badge-unverified': !isIdentityVerified}" title="身份认证状态">
                <van-icon name="certificate" />
              </span>
              <span class="verification-badge education" :class="{'badge-verified': isEducationVerified, 'badge-unverified': !isEducationVerified}" title="教育认证状态">
                <van-icon name="award" />
              </span>
            </h2>
            <p class="username">@{{ username }}</p>
            
            <!-- 用户简介 -->
            <p class="bio" v-if="userBio">{{ userBio }}</p>
          </div>
        </div>
        
        <van-icon name="arrow" class="arrow-icon" />
      </div>
      
      <!-- 用户数据统计 -->
      <div class="stats-card">
        <div class="stats-row">
          <div class="stat-item" @click="goToMyPosts">
            <div class="stat-value">{{ stats.posts }}</div>
            <div class="stat-label">动态</div>
          </div>
          <div class="stat-item" @click="goToFollowers">
            <div class="stat-value">{{ stats.followers }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item" @click="goToFollowing">
            <div class="stat-value">{{ stats.following }}</div>
            <div class="stat-label">关注</div>
          </div>
        </div>
      </div>
      
      <!-- 同频系统卡片 -->
      <div class="feature-card frequency-card">
        <div class="card-header">
          <van-icon name="friends-o" class="card-icon" />
          <span class="card-title">同频匹配</span>
        </div>
        
        <div class="frequency-content">
          <div class="frequency-stats">
            <div class="frequency-stat">
              <span class="frequency-number">{{ frequencyStats.matches || 0 }}</span>
              <span>匹配人数</span>
            </div>
            <div class="frequency-stat">
              <span class="frequency-number">{{ frequencyStats.rate || 0 }}%</span>
              <span>匹配率</span>
            </div>
            <div class="frequency-stat">
              <span class="frequency-number">{{ frequencyStats.potentialMatches || 0 }}</span>
              <span>潜在匹配</span>
            </div>
          </div>
          
          <van-button round type="primary" size="small" class="frequency-btn" @click="goToMatches">
            查看我的匹配
          </van-button>
        </div>
      </div>
      
      <!-- 功能模块列表 -->
      <div class="feature-card">
        <div class="card-header">
          <van-icon name="apps-o" class="card-icon" />
          <span class="card-title">功能与服务</span>
        </div>
        
        <div class="menu-list">
          <div class="menu-item" @click="goToVerificationCenter">
            <van-icon name="certificate" class="menu-icon" />
            <span class="menu-text">认证中心</span>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="goToLikes">
            <van-icon name="like-o" class="menu-icon" />
            <span class="menu-text">我的点赞</span>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="goToMatchLikes">
            <van-icon name="fire-o" class="menu-icon" />
            <span class="menu-text">喜欢记录</span>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
        </div>
      </div>
      
      <!-- 设置模块 -->
      <div class="feature-card">
        <div class="card-header">
          <van-icon name="setting-o" class="card-icon" />
          <span class="card-title">设置中心</span>
        </div>
        
        <div class="menu-list">
          <div class="menu-item" @click="goToFrequencySettings">
            <van-icon name="cluster-o" class="menu-icon" />
            <span class="menu-text">同频推荐偏好设置</span>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
          <div class="menu-item" @click="goToSettings">
            <van-icon name="setting-o" class="menu-icon" />
            <span class="menu-text">账号设置</span>
            <van-icon name="arrow" class="menu-arrow" />
          </div>
        </div>
      </div>
      
      <!-- 退出登录按钮 -->
      <div class="action-buttons">
        <van-button round type="danger" block @click="handleLogout">
          退出登录
        </van-button>
      </div>
      
      <!-- 未登录提示 -->
      <div class="login-notice" v-if="!isLoggedIn">
        <van-empty image="error" description="请先登录以查看个人资料" />
        <van-button round type="primary" block @click="goToLogin">
          立即登录
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '../stores/user'
import * as userApi from '../api/user'

// 路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 用户信息
const username = computed(() => userStore.username || '用户')

// 直接从服务器获取最新昵称的方法
const fetchLatestNickname = async () => {
  try {
    // 直接调用API获取最新用户资料
    const response = await userApi.getCurrentUserProfile();
    if (response && response.data && response.data.nickname) {
      console.log('直接从服务器获取的最新昵称:', response.data.nickname);
      return response.data.nickname;
    }
  } catch (error) {
    console.error('直接获取昵称失败:', error);
  }
  return null;
};

// 用于显示的昵称
const displayName = computed(() => {
  console.log('显示名称计算 - nickname:', userStore.nickname, 'username:', userStore.username);
  return userStore.nickname || userStore.username || '用户';
})

// 认证状态
const isIdentityVerified = computed(() => 
  userStore.verifications.identityStatus === 'approved'
)

const isEducationVerified = computed(() => 
  userStore.verifications.educationStatus === 'approved'
)

// 刷新显示的昵称
const refreshNickname = async () => {
  const latestNickname = await fetchLatestNickname();
  if (latestNickname) {
    // 更新store中的昵称
    userStore.setNickname(latestNickname);
    console.log('昵称已更新为:', latestNickname);
  }
};

const avatar = computed(() => userStore.avatar)
const userBio = ref('') // 用户简介

// 用户统计数据
const stats = ref({
  posts: 0,
  followers: 0,
  following: 0
})

// 同频统计数据
const frequencyStats = ref({
  matches: 0,
  rate: 0,
  potentialMatches: 0
})

// 获取用户资料
const fetchUserProfile = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await userApi.getCurrentUserProfile()
    if (response && response.data) {
      userBio.value = response.data.selfIntro || ''
    }
  } catch (error) {
    console.error('获取用户资料失败', error)
  }
}

// 获取用户统计数据
const fetchUserStats = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await userApi.getUserStats()
    stats.value = response.data || { posts: 0, followers: 0, following: 0 }
  } catch (error) {
    console.error('获取用户统计数据失败', error)
    // 使用模拟数据
    stats.value = { posts: 12, followers: 45, following: 38 }
  }
}

// 获取同频匹配统计数据
const fetchFrequencyStats = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await userApi.getFrequencyStats()
    frequencyStats.value = response.data || { matches: 0, rate: 0, potentialMatches: 0 }
  } catch (error) {
    console.error('获取同频统计数据失败', error)
    showToast('获取匹配数据失败，请稍后重试')
  }
}

// 页面加载时验证登录状态并获取数据
onMounted(async () => {
  // 立即显示缓存数据（如果有的话）
  console.log('页面加载 - 初始nickname:', userStore.nickname);
  
  if (isLoggedIn.value) {
    // 立即获取最新昵称并显示
    await refreshNickname();
    
    // 然后完整刷新所有数据
    await refreshUserData();
  }
  
  // 添加5秒后再次刷新的机制，确保如果有延迟的更新能够被捕获
  setTimeout(async () => {
    if (isLoggedIn.value) {
      console.log('延迟刷新用户数据...');
      // 直接再次获取最新昵称
      await refreshNickname();
    }
  }, 5000);
})

// 刷新用户数据的方法
const refreshUserData = async () => {
  try {
    console.log('正在刷新用户数据...');
    console.log('刷新前用户信息 - nickname:', userStore.nickname, 'username:', userStore.username);
    
    // 先清除userStore中的一些缓存
    userStore.clearProfileCache();
    
    // 从服务器获取最新的用户信息
    await userStore.fetchUserInfo(true); // 传入true表示强制刷新
    
    console.log('获取用户信息后 - nickname:', userStore.nickname, 'username:', userStore.username);
    
    // 直接从服务器获取最新昵称并更新
    await refreshNickname();
    
    // 然后获取其他数据
    await fetchUserProfile();
    await fetchUserStats();
    await fetchFrequencyStats();
    
    console.log('用户数据刷新完成 - 最终nickname:', userStore.nickname);
  } catch (error) {
    console.error('刷新用户数据失败', error);
  }
};

// 导航函数
const goToLogin = () => {
  router.push('/login')
}

const goToEditProfile = () => {
  router.push('/settings/profile')
}

const goToMyPosts = () => {
  router.push('/user/posts')
}

const goToLikes = () => {
  router.push('/user/likes')
}

const goToFollowers = () => {
  router.push(`/user/profile/${userStore.userId}/followers`)
}

const goToFollowing = () => {
  router.push(`/user/profile/${userStore.userId}/following`)
}

const goToSettings = () => {
  router.push('/settings')
}

const goToFrequencySettings = () => {
  router.push('/settings/frequency')
}

const goToMatches = () => {
  router.push('/match/list')
}

const goToMatchLikes = () => {
  router.push('/match/likes')
}

const goToVerificationCenter = () => {
  router.push('/verification')
}

// 处理退出登录
const handleLogout = () => {
  showDialog({
    title: '退出登录',
    message: '确定要退出登录吗？',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    showCancelButton: true
  }).then(() => {
    userStore.logout()
    showToast({
      type: 'success',
      message: '已退出登录'
    })
    router.push('/recommend')
  }).catch(() => {
    // 取消退出
  })
}
</script>

<style scoped>
.page-container {
  padding-bottom: 60px;
  background-color: #f8f8f8;
  min-height: 100vh;
}

.header {
  padding: 16px 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #eee;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.profile-container {
  padding: 16px;
}

/* 用户卡片样式 */
.user-card {
  padding: 20px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 16px;
  border: 2px solid #f0f0f0;
}

.user-details {
  flex: 1;
}

.nickname {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.username {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
}

.bio {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  word-break: break-all;
  overflow-wrap: break-word;
  max-width: 100%;
}

.arrow-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #ccc;
}

/* 统计卡片样式 */
.stats-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  padding: 16px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 功能卡片样式 */
.feature-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  overflow: hidden;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 18px;
  margin-right: 8px;
  color: #1989fa;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 同频卡片样式 */
.frequency-content {
  padding: 16px;
}

.frequency-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}

.frequency-stat {
  text-align: center;
}

.frequency-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
  margin-bottom: 4px;
}

.frequency-btn {
  width: 100%;
}

/* 菜单列表样式 */
.menu-list {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  position: relative;
}

.menu-item:not(:last-child) {
  border-bottom: 1px solid #f6f6f6;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #1989fa;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-arrow {
  color: #ccc;
}

/* 操作按钮样式 */
.action-buttons {
  margin-top: 24px;
  margin-bottom: 16px;
}

/* 登录提示样式 */
.login-notice {
  margin-top: 40px;
  padding: 20px;
  border-radius: 12px;
  background-color: #fff;
  text-align: center;
}

.verification-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 5px;
  font-size: 16px;
  color: #fff;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  vertical-align: middle;
}

.verification-badge.identity.badge-verified {
  background-color: #52c41a;
}

.verification-badge.education.badge-verified {
  background-color: #722ed1;
}

.verification-badge.badge-unverified {
  background-color: #d9d9d9;
  color: #ffffff;
}
</style>