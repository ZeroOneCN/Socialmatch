<template>
  <div class="post-detail-page">
    <van-nav-bar
      title="帖子详情"
      left-arrow
      @click-left="goBack"
    >
      <template #right>
        <van-icon name="ellipsis" @click="showPostOptions" />
      </template>
    </van-nav-bar>
    
    <div class="content-container">
      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <van-loading color="#1989fa" size="36px" />
        <p class="loading-text">加载中...</p>
      </div>
      
      <!-- 内容为空提示 -->
      <div class="empty-state" v-else-if="!post">
        <van-empty description="帖子不存在或已删除">
          <van-button round type="primary" size="small" @click="goBack">返回</van-button>
        </van-empty>
      </div>
      
      <!-- 帖子详情 -->
      <div class="post-detail" v-else>
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
        </div>
        
        <div class="post-content">
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
          <div class="action-item" @click="likePost">
            <van-icon :name="post.liked ? 'like' : 'like-o'" :color="post.liked ? '#ee0a24' : ''" />
            <span :class="{ 'active-count': post.liked }">{{ post.likeCount || 0 }}</span>
          </div>
          <div class="action-item">
            <van-icon name="comment-o" />
            <span>{{ post.commentCount || 0 }}</span>
          </div>
          <div class="action-item" @click="sharePost">
            <van-icon name="share-o" />
            <span>{{ post.shareCount || 0 }}</span>
          </div>
          <div class="action-item" @click="showPostOptions">
            <van-icon name="more-o" />
          </div>
        </div>
      </div>
      
      <!-- 评论区 -->
      <div class="comment-section" v-if="post">
        <div class="section-title">评论 ({{ post.commentCount || 0 }})</div>
        
        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.commentId">
            <van-image
              round
              width="32"
              height="32"
              :src="comment.avatar || 'https://img01.yzcdn.cn/vant/cat.jpeg'"
              class="comment-avatar"
            />
            <div class="comment-content">
              <div class="comment-user">{{ comment.nickname || '用户' }}</div>
              <div class="comment-text">
                <template v-if="comment.replyNickname">
                  <span class="reply-tag">回复</span>
                  <span class="reply-nickname">@{{ comment.replyNickname }}</span>：
                </template>
                {{ comment.content }}
              </div>
              <div class="comment-footer">
                <div class="comment-actions">
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <span class="reply-btn" @click="prepareReply(comment)">回复</span>
                  <span class="report-btn" @click="showCommentOptions(comment)">更多</span>
                </div>
                <van-button 
                  v-if="isCurrentUserComment(comment)" 
                  size="mini" 
                  type="danger" 
                  plain
                  class="delete-btn"
                  @click="confirmDeleteComment(comment)"
                >
                  删除
                </van-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 无评论状态 -->
        <div class="no-comments" v-else>
          <van-empty description="暂无评论，快来抢沙发吧" />
        </div>
      </div>
    </div>
    
    <!-- 评论输入框 -->
    <div class="comment-bar" v-if="post">
      <div class="reply-info" v-if="replyTo">
        回复 <span class="reply-nickname">@{{ replyTo.nickname }}</span>
        <van-icon name="cross" class="cancel-reply" @click="cancelReply" />
      </div>
      <van-field
        v-model="commentText"
        :placeholder="replyTo ? '回复 @' + replyTo.nickname : '说点什么...'"
        :border="false"
        class="comment-input"
      >
        <template #button>
          <van-button 
            size="small" 
            type="primary" 
            class="send-btn"
            :disabled="!commentText.trim()"
            @click="submitComment"
          >
            发送
          </van-button>
        </template>
      </van-field>
    </div>
    
    <!-- 举报对话框 -->
    <report-dialog
      v-if="showReportDialog"
      :visible="showReportDialog"
      :type="reportType"
      :targetId="reportTargetId"
      @close="closeReportDialog"
      @success="handleReportSuccess"
    />
    
    <!-- 动态操作菜单 -->
    <van-action-sheet
      v-model:show="showActionSheet"
      :actions="actionSheetOptions"
      @select="onActionSelect"
      cancel-text="取消"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { showToast, showImagePreview, showDialog } from 'vant'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import * as postApi from '../../api/post'
import * as commentApi from '../../api/comment'
import ReportDialog from '../../components/ReportDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postId = ref(route.params.postId)

// 帖子数据
const post = ref(null)
const loading = ref(true)
const comments = ref([])
const commentText = ref('')
const replyTo = ref(null) // 当前回复对象

// 举报相关
const showReportDialog = ref(false)
const reportType = ref(1) // 1-动态，2-评论
const reportTargetId = ref(null)

// 操作菜单
const showActionSheet = ref(false)
const currentComment = ref(null)
const actionSheetOptions = computed(() => {
  if (reportType.value === 1) {
    // 动态操作菜单
    return [
      { name: '举报', color: '#ee0a24' }
    ]
  } else {
    // 评论操作菜单
    return [
      { name: '举报', color: '#ee0a24' }
    ]
  }
})

// 监听路由参数变化
watch(
  () => route.params.postId,
  (newPostId) => {
    if (newPostId) {
      postId.value = newPostId
      // 重置状态
      post.value = null
      comments.value = []
      replyTo.value = null
      commentText.value = ''
      // 重新获取数据
      fetchPostDetail()
    }
  }
)

// 获取帖子详情
const fetchPostDetail = async () => {
  loading.value = true
  try {
    console.log('获取帖子详情，ID:', postId.value)
    const response = await postApi.getPostDetail(postId.value)
    if (!response || !response.data) {
      post.value = null
      return
    }
    post.value = response.data
    // 加载评论
    fetchComments()
  } catch (error) {
    console.error('获取帖子详情失败', error)
    post.value = null
    showToast({
      type: 'fail',
      message: error.response?.status === 404 ? '帖子不存在或已删除' : '获取内容失败，请稍后再试'
    })
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await commentApi.getComments(postId.value)
    comments.value = response.data.records || []
  } catch (error) {
    console.error('获取评论失败', error)
    showToast('获取评论失败，请稍后再试')
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentText.value.trim()) return
  
  try {
    if (replyTo.value) {
      // 回复评论
      await commentApi.replyComment({
        postId: postId.value,
        content: commentText.value,
        parentId: replyTo.value.parentId || replyTo.value.commentId,
        replyUserId: replyTo.value.userId
      })
    } else {
      // 发表新评论
      await commentApi.addComment({
        postId: postId.value,
        content: commentText.value
      })
    }
    
    // 刷新评论列表
    fetchComments()
    
    // 刷新帖子信息（更新评论数）
    fetchPostDetail()
    
    // 清空输入框和回复状态
    commentText.value = ''
    replyTo.value = null
    
    showToast('评论发表成功')
  } catch (error) {
    console.error('发表评论失败', error)
    showToast({
      type: 'fail',
      message: '发表评论失败，请稍后再试'
    })
  }
}

// 准备回复评论
const prepareReply = (comment) => {
  replyTo.value = comment
  // 自动聚焦到输入框
  setTimeout(() => {
    const inputEl = document.querySelector('.comment-input input')
    if (inputEl) {
      inputEl.focus()
    }
  }, 100)
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
}

// 点赞帖子
const likePost = async () => {
  try {
    if (post.value.liked) {
      // 取消点赞
      await postApi.unlikePost(post.value.postId)
      post.value.liked = false
      post.value.likeCount = Math.max(0, (post.value.likeCount || 1) - 1)
    } else {
      // 点赞
      await postApi.likePost(post.value.postId)
      post.value.liked = true
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('点赞操作失败', error)
    showToast('操作失败，请稍后再试')
  }
}

// 分享帖子
const sharePost = () => {
  const url = window.location.href
  
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

// 返回上一页
const goBack = () => {
  router.back()
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

// 检查评论是否是当前用户发表的
const isCurrentUserComment = (comment) => {
  return comment.userId === userStore.userId
}

// 确认删除评论
const confirmDeleteComment = (comment) => {
  showDialog({
    title: '确认删除',
    message: '确定要删除这条评论吗？',
    showCancelButton: true,
    confirmButtonText: '删除',
    confirmButtonColor: '#ee0a24',
  }).then(async () => {
    try {
      await commentApi.deleteComment(comment.commentId)
      
      // 从列表中移除
      comments.value = comments.value.filter(item => item.commentId !== comment.commentId)
      
      // 更新帖子评论数
      if (post.value) {
        post.value.commentCount = Math.max(0, (post.value.commentCount || 1) - 1)
      }
      
      showToast('删除成功')
    } catch (error) {
      console.error('删除评论失败', error)
      showToast('删除失败，请稍后再试')
    }
  }).catch(() => {})
}

// 显示动态操作菜单
const showPostOptions = () => {
  if (!post.value) return
  
  reportType.value = 1
  reportTargetId.value = post.value.postId
  showActionSheet.value = true
}

// 显示评论操作菜单
const showCommentOptions = (comment) => {
  if (!comment) return
  
  reportType.value = 2
  reportTargetId.value = comment.commentId
  currentComment.value = comment
  showActionSheet.value = true
}

// 操作菜单选择处理
const onActionSelect = (action) => {
  if (action.name === '举报') {
    showReportDialog.value = true
  }
}

// 关闭举报对话框
const closeReportDialog = () => {
  showReportDialog.value = false
}

// 举报成功处理
const handleReportSuccess = () => {
  showReportDialog.value = false
  showToast('举报已提交，我们会尽快处理')
}

// 初始化
onMounted(() => {
  fetchPostDetail()
})
</script>

<style scoped>
.post-detail-page {
  padding-bottom: 54px; /* 底部评论框高度 */
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
  line-height: 1.6;
  margin-bottom: 12px;
  white-space: pre-wrap;
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

.comment-section {
  margin-top: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 12px;
  padding-top: 12px;
  border-top: 8px solid #f7f8fa;
}

.comment-list {
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  color: #323233;
  margin-bottom: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-time {
  font-size: 12px;
  color: #969799;
}

.delete-btn {
  font-size: 10px;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
}

.comment-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 8px;
  border-top: 1px solid #ebedf0;
}

.comment-input {
  width: 100%;
}

.send-btn {
  margin-left: 8px;
}

.no-comments {
  padding: 20px 0;
}

.comment-actions {
  display: flex;
  align-items: center;
}

.comment-time {
  font-size: 12px;
  color: #969799;
  margin-right: 10px;
}

.reply-btn {
  font-size: 12px;
  color: #1989fa;
  cursor: pointer;
}

.reply-tag {
  color: #969799;
  font-size: 12px;
  margin-right: 4px;
}

.reply-nickname {
  color: #1989fa;
  font-weight: 500;
}

.delete-btn {
  font-size: 10px;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
}

.reply-info {
  display: flex;
  align-items: center;
  background-color: #f7f8fa;
  padding: 4px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #646566;
}

.cancel-reply {
  margin-left: auto;
  cursor: pointer;
  color: #969799;
}

.report-btn {
  color: #969799;
  font-size: 12px;
  margin-left: 8px;
  cursor: pointer;
}

.report-btn:hover {
  color: #1989fa;
}
</style> 