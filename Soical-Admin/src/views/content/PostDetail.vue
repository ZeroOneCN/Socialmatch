<template>
  <div class="post-detail-container">
    <div class="page-header">
      <el-button type="primary" link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回列表</span>
      </el-button>
      <h1 class="page-title">动态详情</h1>
    </div>

    <el-card v-loading="loading" class="post-card">
      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :size="40" :src="post.avatar" />
        <div class="user-detail">
          <div class="nickname">{{ post.nickname || post.username }}</div>
          <div class="user-id">ID: {{ post.userId }}</div>
        </div>
      </div>

      <!-- 动态内容 -->
      <div class="post-content">
        <div class="text-content">{{ post.content }}</div>
        
        <!-- 图片展示 -->
        <div class="image-grid" v-if="postImages && postImages.length > 0">
          <el-image
            v-for="(img, index) in postImages"
            :key="index"
            :src="img"
            fit="cover"
            :preview-src-list="postImages"
            class="post-image"
          />
        </div>

        <!-- 分享的原始动态 -->
        <div class="shared-post" v-if="post.isShared && post.originalPost">
          <div class="shared-content">
            <div class="shared-author">
              {{ post.originalPost.nickname || post.originalPost.username }}:
            </div>
            <div class="shared-text">{{ post.originalPost.content }}</div>
            <el-image
              v-if="getOriginalPostImages && getOriginalPostImages.length > 0"
              :src="getOriginalPostImages[0]"
              fit="cover"
              class="shared-image"
            />
          </div>
        </div>
      </div>

      <!-- 动态信息 -->
      <div class="post-info">
        <div class="info-item">
          <el-icon><Location /></el-icon>
          <span>{{ post.city || '未知地区' }}</span>
        </div>
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(post.createTime) }}</span>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="post-stats">
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ post.likeCount || 0 }} 点赞</span>
        </div>
        <div class="stat-item">
          <el-icon><ChatLineRound /></el-icon>
          <span>{{ post.commentCount || 0 }} 评论</span>
        </div>
        <div class="stat-item">
          <el-icon><Share /></el-icon>
          <span>{{ post.shareCount || 0 }} 分享</span>
        </div>
      </div>

      <!-- 状态和操作 -->
      <div class="post-actions">
        <el-tag :type="getStatusType(post.status)" effect="light">
          {{ getStatusText(post.status) }}
        </el-tag>
        <div class="action-buttons">
          <el-button
            v-if="post.status !== 2"
            type="warning"
            plain
            @click="handleReview(2)"
          >
            <el-icon><Warning /></el-icon>
            <span>标记违规</span>
          </el-button>
          <el-button
            v-if="post.status === 2"
            type="success"
            plain
            @click="handleReview(1)"
          >
            <el-icon><Check /></el-icon>
            <span>恢复正常</span>
          </el-button>
          <el-button
            v-if="post.status !== 0"
            type="danger"
            plain
            @click="handleDelete"
          >
            <el-icon><Delete /></el-icon>
            <span>删除动态</span>
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="comment-card">
      <template #header>
        <div class="card-header">
          <span>评论列表</span>
          <el-button type="primary" link @click="refreshComments">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </el-button>
        </div>
      </template>

      <div v-if="commentLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="comments.length === 0" class="empty-data">
        <el-empty description="暂无评论" />
      </div>
      
      <el-table
        v-else
        :data="comments"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="commentId" label="ID" width="80" />
        <el-table-column label="用户信息" width="180">
          <template #default="scope">
            <div class="user-info">
              <div class="nickname">{{ scope.row.nickname || scope.row.username }}</div>
              <div class="user-id">ID: {{ scope.row.userId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status !== 0"
              size="small"
              type="danger"
              plain
              @click="handleDeleteComment(scope.row)"
            >
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 违规处理对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="违规处理"
      width="500px"
      destroy-on-close
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="违规原因" required>
          <el-select v-model="rejectForm.reason" placeholder="请选择违规原因" style="width: 100%">
            <el-option label="含有违规内容" value="含有违规内容" />
            <el-option label="含有广告信息" value="含有广告信息" />
            <el-option label="含有敏感词" value="含有敏感词" />
            <el-option label="含有色情内容" value="含有色情内容" />
            <el-option label="含有暴力内容" value="含有暴力内容" />
            <el-option label="含有政治敏感内容" value="含有政治敏感内容" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" v-if="rejectForm.reason === '其他'">
          <el-input
            v-model="rejectForm.remark"
            type="textarea"
            placeholder="请填写详细原因"
            rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Location, 
  Calendar, 
  Star, 
  ChatLineRound, 
  Share, 
  Warning, 
  Check, 
  Delete,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, reviewPost, deletePost, getComments, deleteComment } from '@/api/content'

const route = useRoute()
const router = useRouter()
const postId = route.params.id

// 加载状态
const loading = ref(false)
const commentLoading = ref(false)

// 动态数据
const post = ref({})

// 处理图片显示
const postImages = computed(() => {
  if (!post.value.images) return [];
  
  // 如果images是字符串，尝试解析JSON
  if (typeof post.value.images === 'string') {
    try {
      return JSON.parse(post.value.images);
    } catch (e) {
      console.error('解析图片JSON失败:', e);
      return [];
    }
  }
  
  // 如果已经是数组，直接返回
  if (Array.isArray(post.value.images)) {
    return post.value.images;
  }
  
  return [];
});

// 获取原始动态图片
const getOriginalPostImages = computed(() => {
  if (!post.value.originalPost || !post.value.originalPost.images) return [];

  // 如果原始动态的images是字符串，尝试解析JSON
  if (typeof post.value.originalPost.images === 'string') {
    try {
      return JSON.parse(post.value.originalPost.images);
    } catch (e) {
      console.error('解析原始动态图片JSON失败:', e);
      return [];
    }
  }
  
  // 如果已经是数组，直接返回
  if (Array.isArray(post.value.originalPost.images)) {
    return post.value.originalPost.images;
  }
  
  return [];
});

// 评论数据
const comments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 违规处理对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: '',
  remark: ''
})

// 获取动态详情
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(postId)
    post.value = res
    
    // 修改用户信息判断逻辑
    if (!post.value.userId) {
      post.value.username = '已删除用户'
      post.value.nickname = '已删除用户'
      post.value.avatar = '/default-avatar.png'
    } else {
      // 确保显示昵称
      post.value.nickname = post.value.nickname || post.value.username
    }
    
    // 处理原始动态的用户信息（如果是转发的动态）
    if (post.value.isShared && post.value.originalPost) {
      if (!post.value.originalPost.userId) {
        post.value.originalPost.username = '已删除用户'
        post.value.originalPost.nickname = '已删除用户'
      } else {
        post.value.originalPost.nickname = post.value.originalPost.nickname || post.value.originalPost.username
      }
    }
    
    console.log('获取到的动态详情:', res)
  } catch (error) {
    console.error('获取动态详情失败:', error)
    ElMessage.error(error.message || '获取动态详情失败')
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  commentLoading.value = true
  try {
    const res = await getComments({
      postId,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (res && res.records) {
      // 处理评论列表中的用户信息
      comments.value = res.records.map(comment => {
        if (!comment.userId) {
          comment.username = '已删除用户'
          comment.nickname = '已删除用户'
        } else {
          comment.nickname = comment.nickname || comment.username
        }
        return comment
      })
      total.value = res.total
      console.log('获取到的评论列表:', res.records)
    } else {
      comments.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
    comments.value = []
    total.value = 0
  } finally {
    commentLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.back()
}

// 刷新评论
const refreshComments = () => {
  currentPage.value = 1
  fetchComments()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchComments()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchComments()
}

// 处理审核
const handleReview = (status) => {
  if (status === 2) {
    // 标记为违规
    rejectForm.reason = ''
    rejectForm.remark = ''
    rejectDialogVisible.value = true
  } else {
    // 恢复为正常
    ElMessageBox.confirm('确定要将该动态恢复为正常状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        ElMessage.info('正在处理...')
        await reviewPost(postId, 1)
        ElMessage.success({
          message: '操作成功，已将动态恢复为正常状态',
          duration: 3000
        })
        fetchPostDetail()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error({
          message: '操作失败: ' + (error.message || '未知错误'),
          duration: 5000
        })
      }
    }).catch(() => {})
  }
}

// 确认违规
const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请选择违规原因')
    return
  }
  
  if (rejectForm.reason === '其他' && !rejectForm.remark) {
    ElMessage.warning('请填写详细原因')
    return
  }
  
  const reason = rejectForm.reason === '其他' ? rejectForm.remark : rejectForm.reason
  
  try {
    ElMessage.info('正在提交审核结果...')
    await reviewPost(postId, 2, reason)
    ElMessage.success({
      message: '操作成功，已将动态标记为违规',
      duration: 3000
    })
    rejectDialogVisible.value = false
    fetchPostDetail()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error({
      message: '操作失败: ' + (error.message || '未知错误'),
      duration: 5000
    })
  }
}

// 处理删除
const handleDelete = () => {
  ElMessageBox.confirm('确定要删除该动态吗？删除后不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      ElMessage.info('正在删除动态...')
      await deletePost(postId)
      ElMessage.success({
        message: '删除动态成功',
        duration: 3000
      })
      router.back()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error({
        message: '删除失败: ' + (error.message || '未知错误'),
        duration: 5000
      })
    }
  }).catch(() => {})
}

// 处理删除评论
const handleDeleteComment = (comment) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      ElMessage.info('正在删除评论...')
      const res = await deleteComment(comment.commentId)
      ElMessage.success({
        message: '删除评论成功',
        duration: 3000
      })
      // 刷新评论列表
      fetchComments()
      // 刷新动态详情，更新评论数
      fetchPostDetail()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error({
        message: '删除评论失败: ' + (error.message || '未知错误'),
        duration: 5000
      })
    }
  }).catch(() => {})
}

// 状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'    // 已删除
    case 1: return 'success' // 正常
    case 2: return 'warning' // 违规
    default: return 'info'
  }
}

// 状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已删除'
    case 1: return '正常'
    case 2: return '违规'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.post-detail-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  
  .page-title {
    margin: 0 0 0 16px;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}

.post-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    
    .user-detail {
      margin-left: 12px;
      
      .nickname {
        font-weight: 500;
        margin-bottom: 4px;
      }
      
      .user-id {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .post-content {
    margin-bottom: 16px;
    
    .text-content {
      font-size: 16px;
      line-height: 1.6;
      margin-bottom: 16px;
      white-space: pre-wrap;
      word-break: break-word;
    }
    
    .image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 8px;
      margin-bottom: 16px;
      
      .post-image {
        width: 100%;
        aspect-ratio: 1;
        border-radius: 8px;
        overflow: hidden;
      }
    }
    
    .shared-post {
      background-color: #f7f8fa;
      border-radius: 8px;
      padding: 12px;
      margin-top: 12px;
      
      .shared-author {
        font-weight: 500;
        margin-bottom: 8px;
      }
      
      .shared-text {
        color: #606266;
        margin-bottom: 8px;
      }
      
      .shared-image {
        width: 100%;
        max-height: 200px;
        border-radius: 4px;
      }
    }
  }
  
  .post-info {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
    
    .info-item {
      display: flex;
      align-items: center;
      color: #606266;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
  
  .post-stats {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;
    
    .stat-item {
      display: flex;
      align-items: center;
      color: #606266;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
  
  .post-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .action-buttons {
      display: flex;
      gap: 8px;
    }
  }
}

.comment-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid #ebeef5;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
    
    .cell {
      padding: 12px 16px;
    }
    
    th.el-table__cell {
      background-color: #f5f7fa;
      color: #303133;
      font-weight: 600;
    }
  }
  
  .user-info {
    .nickname {
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .user-id {
      font-size: 12px;
      color: #909399;
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.loading-container {
  padding: 20px;
}

.empty-data {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .post-detail-container {
    padding: 16px;
  }
  
  .post-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
  
  .comment-card {
    :deep(.el-card__header) {
      padding: 12px 16px;
    }
    
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
}
</style> 