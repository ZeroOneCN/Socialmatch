<template>
  <div class="post-list-container">
    <div class="page-header">
      <h1 class="page-title">动态管理</h1>
      <div class="header-actions">
        <el-button size="small" type="primary" plain @click="fetchPostList">
          <el-icon><Refresh /></el-icon>
          <span>刷新数据</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="违规" :value="2" />
            <el-option label="已删除" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/昵称/内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon>
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据统计卡片 -->
    <el-row :gutter="24" class="stats-cards">
      <el-col :span="6" v-for="(card, index) in statsCards" :key="index">
        <el-card shadow="hover" class="stats-card">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 动态列表 -->
    <el-card shadow="hover" class="table-card">
      <div class="table-header">
        <div class="table-title">动态列表</div>
      </div>

      <el-table
        v-loading="loading"
        :data="postList"
        border
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="postId" label="ID" width="80" sortable />
        <el-table-column label="用户信息" width="180">
          <template #default="scope">
            <div class="user-info">
              <div class="user-name">{{ scope.row.nickname || scope.row.username }}</div>
              <div class="user-id">ID: {{ scope.row.userId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容">
          <template #default="scope">
            <div class="post-content">
              <div class="text-content">{{ truncateContent(scope.row.content) }}</div>
              <div class="image-preview" v-if="scope.row.images && scope.row.images.length > 0">
                <el-image
                  v-for="(img, index) in scope.row.images.slice(0, 3)"
                  :key="index"
                  :src="img"
                  fit="cover"
                  :preview-src-list="scope.row.images"
                  class="preview-image"
                />
                <div class="more-images" v-if="scope.row.images.length > 3">
                  +{{ scope.row.images.length - 3 }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="220">
          <template #default="scope">
            <div class="post-stats">
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                <span>点赞 {{ scope.row.likeCount }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><ChatLineRound /></el-icon>
                <span>评论 {{ scope.row.commentCount }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><Share /></el-icon>
                <span>分享 {{ scope.row.shareCount }}</span>
                </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ scope.row.statusText || getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="primary" plain @click.stop="viewPostDetail(scope.row)">
                <el-icon><View /></el-icon>
                <span>查看</span>
              </el-button>
              <el-button
                v-if="scope.row.status !== 2"
                size="small"
                type="warning"
                plain
                @click.stop="handleReview(scope.row, 2)"
              >
                <el-icon><Warning /></el-icon>
                <span>违规</span>
              </el-button>
              <el-button
                v-if="scope.row.status === 2"
                size="small"
                type="success"
                plain
                @click.stop="handleReview(scope.row, 1)"
              >
                <el-icon><Check /></el-icon>
                <span>恢复</span>
              </el-button>
              <el-button
                v-if="scope.row.status !== 0"
                size="small"
                type="danger"
                plain
                @click.stop="handleDelete(scope.row)"
              >
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
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
import { useRouter } from 'vue-router'
import { 
  View, 
  Delete, 
  Check, 
  Warning, 
  Refresh, 
  Search, 
  RefreshRight,
  Star,
  ChatLineRound,
  Share,
  Document,
  UserFilled,
  Warning as WarningIcon,
  Delete as DeleteIcon
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, reviewPost, deletePost, getContentStats } from '@/api/content'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  status: null,
  keyword: ''
})

// 统计卡片数据
const statsCards = reactive([
  {
    label: '动态总数',
    value: 0,
    icon: Document,
    color: '#409EFF'
  },
  {
    label: '正常动态',
    value: 0,
    icon: Check,
    color: '#67C23A'
  },
  {
    label: '违规动态',
    value: 0,
    icon: WarningIcon,
    color: '#E6A23C'
  },
  {
    label: '已删除',
    value: 0,
    icon: DeleteIcon,
    color: '#F56C6C'
  }
])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 排序设置
const sortBy = ref('postId')
const sortOrder = ref('asc')

// 加载状态
const loading = ref(false)

// 动态列表数据
const postList = ref([])

// 违规处理对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  postId: null,
  reason: '',
  remark: ''
})
const currentPost = ref(null)

// 初始化
onMounted(() => {
  fetchPostList()
  fetchContentStats()
})

// 获取动态列表
const fetchPostList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      status: searchForm.status,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    };
    
    const res = await getPostList(params)
    
    postList.value = res.records || []
    total.value = res.total || 0
    
    // 处理数据格式
    postList.value.forEach(post => {
      // 处理图片列表
      if (post.images && typeof post.images === 'string') {
        try {
          post.images = JSON.parse(post.images);
        } catch (e) {
          post.images = [];
        }
      } else if (!post.images) {
        post.images = [];
      }
    });
  } catch (error) {
    console.error('获取动态列表失败:', error)
    ElMessage.error('获取动态列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 获取内容统计
const fetchContentStats = async () => {
  try {
    const res = await getContentStats();
    console.log('获取到的内容统计数据:', res);
    
    if (res) {
      // 设置统计卡片数据
      statsCards[0].value = res.totalPosts || 0;
      statsCards[1].value = res.normalPosts || 0;
      statsCards[2].value = res.violationPosts || 0;
      statsCards[3].value = res.deletedPosts || 0;
    }
  } catch (error) {
    console.error('获取内容统计失败:', error);
    ElMessage.error('获取统计数据失败，请刷新页面重试');
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPostList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.status = null
  searchForm.keyword = ''
  currentPage.value = 1
  fetchPostList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPostList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchPostList()
}

// 查看动态详情
const viewPostDetail = (row) => {
  router.push({ path: `/content/post/${row.postId}` })
}

// 处理行点击
const handleRowClick = (row) => {
  viewPostDetail(row)
}

// 处理审核
const handleReview = (row, status) => {
  if (status === 2) {
    // 标记为违规
    rejectForm.postId = row.postId
    rejectForm.reason = ''
    rejectForm.remark = ''
    currentPost.value = row
    rejectDialogVisible.value = true
  } else {
    // 恢复为正常
    ElMessageBox.confirm('确定要将该动态恢复为正常状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        const res = await reviewPost(row.postId, 1)
        if (res) {
          ElMessage.success('操作成功')
          fetchPostList()
          fetchContentStats()
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
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
    ElMessage.info({
      message: '正在提交审核结果...',
      duration: 2000
    })
    
    await reviewPost(rejectForm.postId, 2, reason)
    
    setTimeout(() => {
      ElMessage.success({
        message: '操作成功，已将动态标记为违规',
        duration: 3000
      })
      
      rejectDialogVisible.value = false
      fetchPostList()
      fetchContentStats()
    }, 500)
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error({
      message: '操作失败: ' + (error.message || '未知错误'),
      duration: 5000
    })
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该动态吗？删除后不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePost(row.postId)
      if (res) {
        ElMessage.success('删除成功')
        fetchPostList()
        fetchContentStats()
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
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

// 截断内容
const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.slice(0, 100) + '...' : content
}

// 处理排序变化
const handleSortChange = (column) => {
  if (column.prop) {
    sortBy.value = column.prop
    sortOrder.value = column.order === 'ascending' ? 'asc' : 'desc'
    fetchPostList()
  }
}
</script>

<style lang="scss" scoped>
.post-list-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .header-actions {
    display: flex;
    align-items: center;
    
    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 5px;
      border-radius: 6px;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.page-title {
  margin-top: 0;
  margin-bottom: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 5px;
    height: 70%;
    width: 4px;
    background-color: #409EFF;
    border-radius: 2px;
  }
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
  }
}

.stats-cards {
  margin-bottom: 24px;
  
  .stats-card {
    height: 100px;
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    overflow: hidden;
    border: none;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
    
    :deep(.el-card__body) {
      padding: 0;
      height: 100%;
    }
    
    .card-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 0 16px;
      box-sizing: border-box;
    }
    
    .card-icon {
      width: 50px;
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 12px;
      margin-right: 16px;
      color: #fff;
      font-size: 24px;
      box-shadow: 0 6px 12px rgba(0, 0, 0, 0.12);
      
      .el-icon {
        font-size: 24px;
      }
    }
    
    .card-info {
      flex: 1;
    }
    
    .card-value {
      font-size: 24px;
      font-weight: 700;
      line-height: 1.2;
      margin-bottom: 8px;
      color: #303133;
    }
    
    .card-label {
      font-size: 14px;
      color: #606266;
    }
  }
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  .table-header {
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .table-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      position: relative;
      padding-left: 10px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 25%;
        height: 50%;
        width: 3px;
        background-color: #409EFF;
        border-radius: 1.5px;
      }
    }
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
    
    .el-table__row {
      cursor: pointer;
      transition: background-color 0.2s;
      
      &:hover {
        td {
          background-color: #ecf5ff;
        }
      }
    }
  }
  
  .user-info {
    .user-name {
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .user-id {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .post-content {
    max-width: 500px;
    
    .text-content {
      margin-bottom: 8px;
      line-height: 1.5;
      word-break: break-all;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .image-preview {
      display: flex;
      align-items: center;
      
      .preview-image {
        width: 60px;
        height: 60px;
        margin-right: 8px;
        border-radius: 4px;
        overflow: hidden;
        object-fit: cover;
      }
      
      .more-images {
        width: 60px;
        height: 60px;
        border-radius: 4px;
        background-color: rgba(0, 0, 0, 0.1);
        display: flex;
        justify-content: center;
        align-items: center;
        color: #606266;
        font-size: 14px;
      }
    }
  }
  
  .post-stats {
    display: flex;
    align-items: center;
    
    .stat-item {
      display: flex;
      align-items: center;
      margin-right: 16px;
      color: #606266;
      
      .el-icon {
        margin-right: 4px;
        font-size: 16px;
      }
    }
  }
  
  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .el-button {
      margin-left: 0;
      margin-right: 0;
      min-width: 70px;
      display: inline-flex;
      justify-content: center;
      align-items: center;
      
      .el-icon {
        margin-right: 4px;
      }
      
      span {
        display: inline-block;
        white-space: nowrap;
        overflow: visible;
      }
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .stats-cards {
    .stats-card {
      .card-icon {
        width: 45px;
        height: 45px;
        font-size: 20px;
        
        .el-icon {
          font-size: 20px;
        }
      }
      
      .card-value {
        font-size: 20px;
      }
    }
  }
}

@media (max-width: 768px) {
  .post-list-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .search-form {
    display: block;
    
    :deep(.el-form-item) {
      display: block;
      margin-right: 0;
    }
  }
}
</style> 