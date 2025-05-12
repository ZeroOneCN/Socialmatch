<template>
  <div class="page-container">
    <div class="header">
      <h1 class="page-title">社区</h1>
      <div class="filter-bar" v-if="isLoggedIn">
        <van-button 
          size="small" 
          plain 
          icon="fire-o" 
          class="filter-btn"
          :class="{ 'active': activeFilter === 'hot' }"
          @click="switchFilter('hot')"
        >
          热门
        </van-button>
        <van-button 
          size="small" 
          plain 
          icon="friends-o" 
          class="filter-btn"
          :class="{ 'active': activeFilter === 'follow' }"
          @click="switchFilter('follow')"
        >
          关注
        </van-button>
        <van-button 
          size="small" 
          plain 
          icon="location-o" 
          class="filter-btn"
          :class="{ 'active': activeFilter === 'nearby' }"
          @click="switchFilter('nearby')"
        >
          附近
        </van-button>
      </div>
    </div>
    
    <div class="content-container">
      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <van-loading color="#1989fa" size="36px" />
        <p class="loading-text">正在加载社区内容...</p>
      </div>
      
      <!-- 未登录提示 -->
      <div class="login-prompt" v-else-if="!isLoggedIn">
        <van-empty image="network" description="">
          <template #description>
            <p class="login-text">登录后查看社区内容</p>
          </template>
          <van-button type="primary" round size="large" @click="goToLogin" class="login-btn">立即登录</van-button>
        </van-empty>
      </div>
      
      <!-- 内容为空提示 -->
      <div class="empty-state" v-else-if="posts.length === 0 && !loading">
        <van-empty description="暂无内容">
          <template #description>
            <p>{{ getEmptyText() }}</p>
          </template>
          <van-button round type="primary" size="small" @click="refresh">刷新</van-button>
        </van-empty>
      </div>
      
      <!-- 帖子列表 -->
      <div class="post-list" v-else>
        <van-pull-refresh 
          v-model="refreshing" 
          @refresh="refresh"
          :disabled="!isAtTop"
          :pulling-text="isAtTop ? '下拉刷新' : ''"
          :loosing-text="isAtTop ? '释放刷新' : ''"
        >
          <van-list
            v-model:loading="loadingMore"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadMore"
          >
            <div class="post-item" v-for="post in posts" :key="post.postId">
              <div class="post-header">
                <div class="user-info">
                  <van-image
                    round
                    width="40"
                    height="40"
                    :src="post.avatar || 'https://img01.yzcdn.cn/vant/cat.jpeg'"
                    class="avatar"
                  />
                  <div class="user-detail">
                    <div class="nickname">{{ post.nickname || '用户' }}</div>
                    <div class="post-time">{{ formatTime(post.createTime) }}</div>
                  </div>
                </div>
                <van-button 
                  icon="ellipsis" 
                  size="mini" 
                  plain 
                  class="more-btn"
                  @click="showPostActions(post)"
                />
              </div>
              
              <div class="post-content" @click="viewPostDetail(post.postId)">
                <div class="post-text">{{ post.content }}</div>
                
                <!-- 图片展示 -->
                <div class="post-images" v-if="post.images && post.images.length > 0">
                  <van-image-preview-group>
                    <div class="image-grid" :class="getImageGridClass(post.images.length)">
                      <van-image
                        v-for="(image, index) in post.images"
                        :key="index"
                        fit="cover"
                        :src="image"
                        class="post-image"
                        @click.stop="previewImage(post.images, index)"
                      />
                    </div>
                  </van-image-preview-group>
                </div>
                
                <!-- 分享的原始动态 -->
                <div class="shared-post" v-if="post.isShared && post.originalPost">
                  <div class="shared-content">
                    <div class="shared-author">
                      {{ post.originalPost.nickname || '用户' }}:
                    </div>
                    <div class="shared-text">{{ post.originalPost.content }}</div>
                    
                    <!-- 原动态的图片 (最多显示一张) -->
                    <van-image
                      v-if="post.originalPost.images && post.originalPost.images.length > 0"
                      fit="cover"
                      :src="post.originalPost.images[0]"
                      class="shared-image"
                    />
                  </div>
                </div>
              </div>
              
              <!-- 帖子位置信息 -->
              <div class="post-info" v-if="post.city">
                <van-icon name="location-o" size="14" class="location-icon" />
                <span class="city-text">{{ post.city }}</span>
              </div>
              
              <!-- 帖子操作栏 -->
              <div class="post-actions">
                <div class="action-item" @click="likePost(post)">
                  <van-icon :name="post.liked ? 'like' : 'like-o'" :color="post.liked ? '#ee0a24' : ''" />
                  <span :class="{ 'active-count': post.liked }">{{ post.likeCount || 0 }}</span>
                </div>
                <div class="action-item" @click="commentPost(post)">
                  <van-icon name="comment-o" />
                  <span>{{ post.commentCount || 0 }}</span>
                </div>
                <div class="action-item" @click="sharePost(post)">
                  <van-icon name="share-o" />
                  <span>{{ post.shareCount || 0 }}</span>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
    
    <!-- 发布按钮 -->
    <van-button 
      icon="plus" 
      type="primary" 
      size="normal" 
      class="fab-button"
      @click="showPostForm"
      v-if="isLoggedIn"
    />
    
    <!-- 帖子操作菜单 -->
    <van-action-sheet
      v-model:show="showActionsSheet"
      :actions="postActions"
      cancel-text="取消"
      close-on-click-action
      @select="onActionSelected"
    />
    
    <!-- 举报对话框 -->
    <report-dialog
      :visible="showReportDialog"
      :type="1"
      :targetId="currentPost?.postId"
      @close="showReportDialog = false"
      @success="onReportSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { showToast, showDialog, showImagePreview } from 'vant'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import * as postApi from '../api/post'
import { getCurrentLocationInfo } from '../utils/mapUtils'
import ReportDialog from '../components/ReportDialog.vue'

const router = useRouter()
const userStore = useUserStore()

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 帖子数据
const posts = ref([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 筛选相关
const activeFilter = ref('community') // 'community', 'hot', 'follow', 'nearby'

// 操作菜单相关
const showActionsSheet = ref(false)
const currentPost = ref(null)
const postActions = ref([
  { name: '举报', color: '#ee0a24' },
])

// 举报相关
const showReportDialog = ref(false)

// 添加滚动位置判断
const isAtTop = ref(true)

// 监听滚动事件
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  isAtTop.value = scrollTop < 10
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '刚刚'
  const date = new Date(timestamp)
  return formatDistance(date, new Date(), { addSuffix: true, locale: zhCN })
}

// 获取空状态文本
const getEmptyText = () => {
  switch (activeFilter.value) {
    case 'community':
      return '暂无社区内容，去发布一条动态吧'
    case 'hot':
      return '暂无热门内容，稍后再来看看吧'
    case 'follow':
      return '暂无关注的用户动态，去发现更多有趣的人吧'
    case 'nearby':
      return '附近暂无动态，成为第一个发布者吧'
    default:
      return '暂无内容'
  }
}

// 获取社区帖子
const fetchPosts = async () => {
  if (!isLoggedIn.value) return
  
  loading.value = true
  
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    let response
    
    console.log('当前筛选条件:', activeFilter.value)
    console.log('请求参数:', params)
    
    // 根据筛选条件选择不同的API
    switch (activeFilter.value) {
      case 'hot':
        response = await postApi.getHotPosts(params)
        break
      case 'follow':
        // 确保用户已登录
        if (!userStore.userId) {
          showToast('请先登录')
          return
        }
        console.log('获取关注的用户动态，用户ID:', userStore.userId)
        response = await postApi.getFollowedPosts(params)
        console.log('关注的用户动态响应:', response)
        break
      case 'nearby':
        // 如果有城市信息则附加
        if (userStore.userProfile && userStore.userProfile.city) {
          params.city = userStore.userProfile.city
        }
        response = await postApi.getNearbyPosts(params)
        break
      case 'community':
      default:
        response = await postApi.getCommunityPosts(params)
    }
    
    // 处理响应数据格式
    const data = response.data || response
    console.log('处理后的数据:', data)
    
    if (refreshing.value) {
      posts.value = data.records || []
    } else {
      posts.value = [...posts.value, ...(data.records || [])]
    }
    
    // 判断是否加载完毕
    if (!data.records || data.records.length < pageSize.value) {
      finished.value = true
    }
    
    page.value++
  } catch (error) {
    console.error('获取社区帖子失败', error)
    showToast({
      type: 'fail',
      message: '获取内容失败，请稍后再试'
    })
  } finally {
    loading.value = false
    refreshing.value = false
    loadingMore.value = false
  }
}

// 切换筛选条件
const switchFilter = (filter) => {
  console.log('切换筛选条件:', filter)
  if (activeFilter.value === filter) return
  activeFilter.value = filter
  page.value = 1
  posts.value = []
  finished.value = false
  
  if (filter === 'nearby') {
    // 如果用户资料中已有城市信息，直接使用
    if (userStore.userInfo && userStore.userInfo.city) {
      fetchPosts()
      return
    }
    
    // 尝试获取当前位置
    getCurrentLocation()
  } else {
    fetchPosts()
  }
}

// 获取当前位置
const getCurrentLocation = async () => {
  if (!navigator.geolocation) {
    showToast('您的浏览器不支持地理位置功能')
    return
  }
  
  loading.value = true
  
  try {
    // 使用高德地图API获取位置信息
    const locationInfo = await getCurrentLocationInfo()
    
    // 更新用户资料中的位置信息
    if (userStore.userInfo) {
      userStore.userInfo.city = locationInfo.city
    }
    
    showToast(`已获取位置: ${locationInfo.city}`)
    fetchPosts()
  } catch (error) {
    console.error('获取位置失败', error)
    showToast('获取位置失败，请稍后再试')
    loading.value = false
  }
}

// 刷新
const refresh = async () => {
  if (!isAtTop.value) {
    refreshing.value = false
    return
  }
  
  console.log('刷新数据')
  page.value = 1
  finished.value = false
  await fetchPosts()
}

// 加载更多
const loadMore = () => {
  if (finished.value) return
  console.log('加载更多数据')
  loadingMore.value = true
  fetchPosts()
}

// 前往登录页
const goToLogin = () => {
  router.push('/login')
}

// 图片网格样式
const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-more'
}

// 预览图片
const previewImage = (images, index) => {
  showImagePreview({
    images,
    startPosition: index
  })
}

// 点赞帖子
const likePost = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录')
    return
  }
  
  try {
    if (post.liked) {
      // 取消点赞
      await postApi.unlikePost(post.postId)
      post.liked = false
      post.likeCount = Math.max(0, (post.likeCount || 1) - 1)
    } else {
      // 点赞
      await postApi.likePost(post.postId)
      post.liked = true
      post.likeCount = (post.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('点赞操作失败', error)
    showToast('操作失败，请稍后再试')
  }
}

// 评论帖子
const commentPost = (post) => {
  router.push(`/post/${post.postId}`)
}

// 分享帖子
const sharePost = (post) => {
  const url = `${window.location.origin}/post/${post.postId}`
  
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(url)
      .then(() => {
        showToast('链接已复制到剪贴板')
      })
      .catch(() => {
        showDialog({
          title: '分享链接',
          message: url,
          confirmButtonText: '确定'
        })
      })
  } else {
    showDialog({
      title: '分享链接',
      message: url,
      confirmButtonText: '确定'
    })
  }
}

// 查看帖子详情
const viewPostDetail = (postId) => {
  router.push(`/post/${postId}`)
}

// 显示帖子操作菜单
const showPostActions = (post) => {
  currentPost.value = post
  
  // 如果是当前用户的帖子，添加删除选项
  if (post.userId === userStore.userId) {
    postActions.value = [
      { name: '删除', color: '#ee0a24' }
    ]
  } else {
    postActions.value = [
      { name: '举报', color: '#ee0a24' }
    ]
  }
  
  showActionsSheet.value = true
}

// 处理操作菜单选择
const onActionSelected = async (action) => {
  if (!currentPost.value) return
  
  if (action.name === '删除') {
    showDialog({
      title: '确认删除',
      message: '确定要删除这条动态吗？',
      showCancelButton: true,
      confirmButtonText: '删除',
      confirmButtonColor: '#ee0a24',
    }).then(async () => {
      try {
        await postApi.deletePost(currentPost.value.postId)
        showToast('删除成功')
        posts.value = posts.value.filter(p => p.postId !== currentPost.value.postId)
      } catch (error) {
        console.error('删除失败', error)
        showToast('删除失败，请稍后再试')
      }
    }).catch(() => {})
  } else if (action.name === '举报') {
    showReportDialog.value = true
  }
}

// 举报成功回调
const onReportSuccess = () => {
  showReportDialog.value = false
  showToast('举报成功，感谢您的反馈')
}

// 显示发布表单
const showPostForm = () => {
  router.push('/post/create')
}

// 添加和移除滚动监听
onMounted(() => {
  if (isLoggedIn.value) {
    fetchPosts()
  }
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 16px 0;
  position: sticky;
  top: 0;
  background-color: #f8f8f8;
  z-index: 10;
}

.filter-bar {
  display: flex;
  gap: 8px;
}

.filter-btn {
  font-size: 12px;
}

.filter-btn.active {
  color: var(--van-primary-color);
  border-color: var(--van-primary-color);
}

.content-container {
  padding: 0 16px;
  flex: 1;
  overflow-y: visible;
  margin-bottom: 60px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-text {
  margin-top: 16px;
  color: #969799;
  font-size: 14px;
}

.login-prompt, .empty-state {
  padding: 60px 0;
}

.login-text {
  margin-bottom: 16px;
}

.post-list {
  padding-bottom: 80px;
}

.post-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  margin-right: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 500;
  font-size: 15px;
  line-height: 1.3;
}

.post-time {
  font-size: 12px;
  color: #969799;
}

.post-content {
  margin-bottom: 12px;
}

.post-text {
  font-size: 15px;
  line-height: 1.5;
  margin-bottom: 12px;
  word-break: break-word;
}

.image-grid {
  display: grid;
  grid-gap: 4px;
  border-radius: 4px;
  overflow: hidden;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid-4 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.grid-more {
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
}

.post-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.shared-post {
  background-color: #f7f8fa;
  border-radius: 8px;
  padding: 10px;
  margin-top: 8px;
}

.shared-author {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.shared-text {
  font-size: 14px;
  color: #646566;
  margin-bottom: 8px;
}

.shared-image {
  width: 100%;
  max-height: 200px;
  border-radius: 4px;
}

.post-info {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #969799;
  margin-bottom: 12px;
}

.location-icon {
  margin-right: 4px;
}

.post-actions {
  display: flex;
  border-top: 1px solid #f2f2f2;
  padding-top: 12px;
}

.action-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #646566;
  font-size: 14px;
}

.active-count {
  color: #ee0a24;
}

.fab-button {
  position: fixed;
  right: 20px;
  bottom: 80px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.more-btn {
  padding: 0;
  border: none;
}

.city-text {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style> 