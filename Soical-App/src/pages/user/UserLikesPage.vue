<template>
  <div class="user-likes-page">
    <van-nav-bar
      title="我的点赞"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="content-container">
      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading && !refreshing && likedPosts.length === 0">
        <van-loading color="#1989fa" size="36px" />
        <p class="loading-text">正在加载内容...</p>
      </div>
      
      <!-- 内容为空提示 -->
      <div class="empty-state" v-else-if="!loading && !error && likedPosts.length === 0">
        <van-empty description="暂无点赞内容">
          <template #description>
            <p>您还没有点赞任何动态</p>
          </template>
          <van-button round type="primary" size="small" @click="goCommunity">浏览社区</van-button>
        </van-empty>
      </div>
      
      <!-- 错误状态 -->
      <div class="error-state" v-else-if="error && likedPosts.length === 0">
        <van-empty image="error" description="加载失败">
          <template #description>
            <p>获取点赞内容失败</p>
          </template>
          <van-button round type="primary" size="small" @click="retryLoad">重新加载</van-button>
        </van-empty>
      </div>
      
      <!-- 帖子列表 -->
      <div class="post-list" v-else ref="postListRef" @scroll="onScroll">
        <van-pull-refresh 
          v-model="refreshing" 
          @refresh="refresh"
          success-text="刷新成功" 
          loading-text="正在刷新..."
          :disabled="!canPullRefresh"
          :head-height="50"
          pull-distance="50"
        >
          <van-list
            v-model:loading="loadingMore"
            :finished="finished"
            finished-text="没有更多了"
            :error="error"
            error-text="加载失败，点击重试"
            @load="loadMore"
            offset="300"
            immediate-check
          >
            <div class="post-item" v-for="post in likedPosts" :key="post.postId">
              <div class="post-header">
                <div class="user-info" @click="goToUserProfile(post.userId)">
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
                  icon="like" 
                  size="mini" 
                  plain 
                  color="#ee0a24"
                  class="unlike-btn"
                  @click="unlikePost(post)"
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
                <div class="action-item" @click="toggleLike(post)">
                  <van-icon name="like" color="#ee0a24" />
                  <span class="active-count">{{ post.likeCount || 0 }}</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { showToast, showImagePreview, showDialog } from 'vant'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import * as postApi from '../../api/post'

const router = useRouter()
const userStore = useUserStore()

// 帖子数据
const likedPosts = ref([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const error = ref(false)
const page = ref(1)
const pageSize = ref(10)

const postListRef = ref(null)
const canPullRefresh = ref(true)
let scrollTimer = null

// 防抖函数
const debounce = (fn, delay) => {
  return (...args) => {
    if (scrollTimer) clearTimeout(scrollTimer)
    scrollTimer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 获取我点赞的帖子
const fetchLikedPosts = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }
  
  // 如果正在加载中，不重复加载
  if (loading.value || loadingMore.value) {
    return;
  }
  
  loading.value = true;
  error.value = false;
  
  try {
    const params = { 
      page: page.value, 
      pageSize: pageSize.value 
    };
    
    console.log(`获取点赞帖子, 页码: ${page.value}, 每页: ${pageSize.value}`);
    const response = await postApi.getLikedPosts(params);
    console.log('点赞帖子响应:', response);
    
    // 处理响应数据
    if (response && response.data) {
      const { records, total } = response.data;
      
      if (refreshing.value) {
        likedPosts.value = records || [];
      } else {
        // 确保不重复添加
        const newPosts = records || [];
        const existingIds = new Set(likedPosts.value.map(p => p.postId));
        const uniqueNewPosts = newPosts.filter(p => !existingIds.has(p.postId));
        likedPosts.value = [...likedPosts.value, ...uniqueNewPosts];
      }
      
      // 判断是否加载完毕
      if (!records || records.length === 0 || likedPosts.value.length >= total) {
        finished.value = true;
        console.log('已加载全部数据，总数:', total);
      } else {
        page.value++;
        console.log('加载下一页，当前页码:', page.value);
      }
    } else {
      console.error('API响应格式异常:', response);
      error.value = true;
    }
  } catch (error) {
    console.error('获取点赞帖子失败', error);
    showToast('获取点赞内容失败，请稍后再试');
    error.value = true;
  } finally {
    loading.value = false;
    refreshing.value = false;
    loadingMore.value = false;
  }
};

// 处理点赞/取消点赞
const toggleLike = async (post) => {
  try {
    if (!post || !post.postId) {
      console.error('无效的帖子ID');
      return;
    }
    
    // 当前在点赞页面，所有帖子都是已点赞状态
    // 只能执行取消点赞操作
    console.log(`取消点赞帖子: ${post.postId}`);
    await postApi.unlikePost(post.postId);
    
    showToast('已取消点赞');
    
    // 从列表中移除
    likedPosts.value = likedPosts.value.filter(p => p.postId !== post.postId);
    
    // 如果列表空了，检查是否需要加载更多
    if (likedPosts.value.length === 0 && !finished.value) {
      loadMore();
    }
  } catch (error) {
    console.error('点赞操作失败', error);
    showToast('操作失败，请稍后再试');
  }
};

// 评论帖子
const commentPost = (post) => {
  if (!post || !post.postId) {
    console.error('无效的帖子ID');
    return;
  }
  
  // 跳转到帖子详情页并聚焦评论区
  router.push(`/post/${post.postId}?focus=comment`);
};

// 分享帖子
const sharePost = (post) => {
  if (!post || !post.postId) {
    console.error('无效的帖子ID');
    return;
  }
  
  const url = `${window.location.origin}/post/${post.postId}`;
  
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(url)
      .then(() => {
        showToast('链接已复制到剪贴板');
        // 更新分享计数（可选）
        post.shareCount = (post.shareCount || 0) + 1;
      })
      .catch(() => {
        showDialog({
          title: '分享链接',
          message: url,
          confirmButtonText: '确定'
        });
      });
  } else {
    showDialog({
      title: '分享链接',
      message: url,
      confirmButtonText: '确定'
    });
  }
};

// 取消点赞（保留原有的方法，但替换内部实现）
const unlikePost = async (post) => {
  await toggleLike(post);
};

// 监听滚动事件
const onScroll = debounce(() => {
  if (!postListRef.value) return
  
  const { scrollTop } = postListRef.value
  // 添加5px的缓冲区，避免过于敏感
  canPullRefresh.value = scrollTop <= 5
}, 100)

// 清理定时器
onUnmounted(() => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
    scrollTimer = null
  }
})

// 刷新处理
const refresh = async () => {
  // 如果不在顶部，不执行刷新
  if (!canPullRefresh.value) {
    refreshing.value = false
    return
  }
  
  console.log('刷新点赞列表')
  refreshing.value = true
  page.value = 1
  finished.value = false
  error.value = false
  await fetchLikedPosts()
}

// 加载更多
const loadMore = async () => {
  if (finished.value || loading.value || loadingMore.value) {
    return;
  }
  
  console.log('加载更多点赞帖子');
  loadingMore.value = true;
  await fetchLikedPosts();
};

// 重试加载
const retryLoad = () => {
  error.value = false;
  loadMore();
};

// 预览图片
const previewImage = (images, index) => {
  showImagePreview({
    images,
    startPosition: index,
    closeable: true
  });
};

// 查看帖子详情
const viewPostDetail = (postId) => {
  router.push(`/post/${postId}`);
};

// 跳转到用户资料页
const goToUserProfile = (userId) => {
  router.push(`/user/profile/${userId}`);
};

// 跳转到社区页面
const goCommunity = () => {
  router.push('/community');
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  try {
    const date = new Date(timestamp);
    return formatDistance(date, new Date(), {
      addSuffix: true,
      locale: zhCN
    });
  } catch (e) {
    console.error('时间格式化错误', e);
    return timestamp;
  }
};

// 获取图片网格样式类
const getImageGridClass = (count) => {
  if (count === 1) return 'single-image';
  if (count === 2) return 'double-image';
  if (count === 3) return 'triple-image';
  if (count === 4) return 'quad-image';
  return 'multi-image';
};

// 页面初始化
onMounted(() => {
  console.log('用户点赞页面加载');
  refresh();
});
</script>

<style scoped>
.user-likes-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.content-container {
  flex: 1;
  height: calc(100vh - 46px); /* 减去导航栏高度 */
  position: relative;
  overflow: hidden;
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

.error-state {
  padding: 60px 0;
  text-align: center;
}

.error-state .van-empty {
  padding: 0;
}

.error-state p {
  margin: 8px 0 16px;
  color: #969799;
  font-size: 14px;
}

.post-list {
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.post-list :deep(.van-pull-refresh) {
  overflow: visible;
  height: 100%;
}

.post-list :deep(.van-pull-refresh__track) {
  overflow: visible;
  height: 100%;
}

.post-list :deep(.van-pull-refresh__head) {
  position: absolute;
  left: 0;
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #969799;
  font-size: 14px;
  z-index: 1;
}

.post-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin: 0 16px 12px;
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

.unlike-btn {
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

.single-image {
  grid-template-columns: 1fr;
}

.double-image {
  grid-template-columns: repeat(2, 1fr);
}

.triple-image {
  grid-template-columns: repeat(3, 1fr);
}

.quad-image {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.multi-image {
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