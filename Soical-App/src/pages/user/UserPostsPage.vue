<template>
  <div class="user-posts-page">
    <van-nav-bar
      :title="isOwnPosts ? '我的动态' : '用户动态'"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="content-container">
      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <van-loading color="#1989fa" size="36px" />
        <p class="loading-text">正在加载动态...</p>
      </div>
      
      <!-- 内容为空提示 -->
      <div class="empty-state" v-else-if="posts.length === 0 && !loading">
        <van-empty description="暂无动态">
          <template #description>
            <p>{{ isOwnPosts ? '您还没有发布任何动态' : '该用户还没有发布任何动态' }}</p>
          </template>
          <van-button v-if="isOwnPosts" round type="primary" size="small" @click="goCreatePost">发布动态</van-button>
        </van-empty>
      </div>
      
      <!-- 帖子列表 -->
      <div class="post-list" v-else>
        <van-pull-refresh v-model="refreshing" @refresh="refresh">
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
                  v-if="isOwnPosts"
                  icon="delete-o" 
                  size="mini" 
                  plain 
                  class="delete-btn"
                  @click="confirmDeletePost(post)"
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
                <div class="action-item" @click="handleLike(post)">
                  <van-icon :name="post.liked ? 'like' : 'like-o'" :color="post.liked ? '#ee0a24' : ''" />
                  <span :class="{ 'active-count': post.liked }">{{ post.likeCount || 0 }}</span>
                </div>
                <div class="action-item" @click="goToComment(post.postId)">
                  <van-icon name="comment-o" />
                  <span>{{ post.commentCount || 0 }}</span>
                </div>
                <div class="action-item" @click="handleShare(post)">
                  <van-icon name="share-o" />
                  <span>{{ post.shareCount || 0 }}</span>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { showToast, showDialog, showImagePreview } from 'vant'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import * as postApi from '../../api/post'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 获取URL中的用户ID
const targetUserId = computed(() => {
  return route.params.id || userStore.userId
})

// 是否是查看自己的帖子
const isOwnPosts = computed(() => {
  return !route.params.id || String(route.params.id) === String(userStore.userId)
})

// 帖子数据
const posts = ref([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 获取用户帖子
const fetchUserPosts = async () => {
  loading.value = true
  
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    const response = await postApi.getUserPosts(targetUserId.value, params)
    
    // 处理响应数据格式
    const data = response.data || response
    
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
    console.error('获取用户帖子失败', error)
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

// 刷新
const refresh = async () => {
  refreshing.value = true
  page.value = 1
  finished.value = false
  await fetchUserPosts()
}

// 加载更多
const loadMore = () => {
  if (finished.value) return
  loadingMore.value = true
  fetchUserPosts()
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '刚刚'
  const date = new Date(timestamp)
  return formatDistance(date, new Date(), { addSuffix: true, locale: zhCN })
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

// 查看帖子详情
const viewPostDetail = (postId) => {
  router.push(`/post/${postId}`)
}

// 确认删除帖子
const confirmDeletePost = (post) => {
  showDialog({
    title: '确认删除',
    message: '确定要删除这条动态吗？',
    showCancelButton: true,
    confirmButtonText: '删除',
    confirmButtonColor: '#ee0a24',
  }).then(async () => {
    try {
      await postApi.deletePost(post.postId)
      showToast('删除成功')
      posts.value = posts.value.filter(p => p.postId !== post.postId)
    } catch (error) {
      console.error('删除失败', error)
      showToast('删除失败，请稍后再试')
    }
  }).catch(() => {})
}

// 发布新动态
const goCreatePost = () => {
  router.push('/post/create')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理点赞
const handleLike = async (post) => {
  try {
    if (post.liked) {
      // 已点赞，取消点赞
      await postApi.unlikePost(post.postId)
      post.liked = false
      post.likeCount = Math.max(0, (post.likeCount || 1) - 1)
      showToast('已取消点赞')
    } else {
      // 未点赞，添加点赞
      await postApi.likePost(post.postId)
      post.liked = true
      post.likeCount = (post.likeCount || 0) + 1
      showToast('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败', error)
    showToast('操作失败，请稍后再试')
  }
}

// 跳转到评论页面
const goToComment = (postId) => {
  router.push(`/post/${postId}?focus=comment`)
}

// 处理分享
const handleShare = (post) => {
  showDialog({
    title: '分享动态',
    message: '选择分享方式',
    showCancelButton: true,
    showConfirmButton: true,
    confirmButtonText: '转发',
    cancelButtonText: '取消',
  }).then(() => {
    // 转发到自己的动态
    router.push({
      path: '/post/create',
      query: { 
        share: 'true',
        postId: post.postId,
        // 传递其他参数用于预填充分享内容
        content: post.content,
        originalUserId: post.userId,
        originalUserName: post.nickname || '用户'
      }
    })
  }).catch(() => {
    // 取消分享
  })
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserPosts()
})
</script>

<style scoped>
.user-posts-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
}

.content-container {
  padding: 16px;
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

.empty-state {
  padding: 60px 0;
}

.post-list {
  padding-bottom: 60px;
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

.delete-btn {
  color: #ee0a24;
  border: none;
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
</style> 