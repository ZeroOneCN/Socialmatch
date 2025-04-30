<template>
  <div class="report-list-container">
    <div class="page-header">
      <h1 class="page-title">举报管理</h1>
      <div class="header-actions">
        <el-button size="small" type="primary" plain @click="fetchReportList">
          <el-icon><Refresh /></el-icon>
          <span>刷新数据</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="举报类型" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="动态" :value="1" />
            <el-option label="评论" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="处理状态" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="举报内容/举报人" clearable />
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

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalReports }}</div>
              <div class="stat-label">举报总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingReports }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon processed">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.processedRate }}%</div>
              <div class="stat-label">处理率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 举报列表 -->
    <el-card shadow="hover" class="table-card">
      <el-table
        v-loading="loading"
        :data="reportList"
        border
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="reportId" label="ID" width="80" sortable="custom" />
        <el-table-column label="举报类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.reportType === 1" type="danger">动态</el-tag>
            <el-tag v-else-if="scope.row.reportType === 2" type="warning">评论</el-tag>
            <el-tag v-else-if="scope.row.reportType === 3" type="info">用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterId" label="举报人" width="120" />
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column prop="content" label="举报内容" show-overflow-tooltip>
          <template #default="scope">
            <div class="report-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已处理</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">已忽略</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="primary"
              link
              @click="handleView(scope.row)"
            >
              <el-icon><View /></el-icon>
              <span>查看</span>
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="warning"
              link
              @click="handleViolation(scope.row)"
            >
              <el-icon><Warning /></el-icon>
              <span>违规</span>
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="info"
              link
              @click="handleIgnore(scope.row)"
            >
              <el-icon><CircleClose /></el-icon>
              <span>忽略</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 违规处理对话框 -->
    <el-dialog
      v-model="violationDialogVisible"
      title="违规处理"
      width="500px"
      destroy-on-close
    >
      <el-form :model="violationForm" label-width="80px">
        <el-form-item label="违规原因" required>
          <el-select v-model="violationForm.reason" placeholder="请选择违规原因" style="width: 100%">
            <el-option label="含有违规内容" value="含有违规内容" />
            <el-option label="含有广告信息" value="含有广告信息" />
            <el-option label="含有敏感词" value="含有敏感词" />
            <el-option label="含有色情内容" value="含有色情内容" />
            <el-option label="含有暴力内容" value="含有暴力内容" />
            <el-option label="含有政治敏感内容" value="含有政治敏感内容" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" v-if="violationForm.reason === '其他'">
          <el-input
            v-model="violationForm.remark"
            type="textarea"
            placeholder="请填写详细原因"
            rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="violationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmViolation" :loading="processing">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看内容对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="currentReport?.type === 1 ? '查看动态内容' : '查看评论内容'"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentReport" class="report-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ currentReport.targetId }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentReport.targetUser }}</el-descriptions-item>
          <el-descriptions-item label="举报原因">{{ currentReport.reason }}</el-descriptions-item>
          <el-descriptions-item label="内容">{{ currentReport.content }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentReport.targetTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-actions">
          <el-button type="warning" @click="handleViolation(currentReport)">
            <el-icon><Warning /></el-icon>
            <span>标记违规</span>
          </el-button>
          <el-button type="info" @click="handleIgnore(currentReport)">
            <el-icon><CircleClose /></el-icon>
            <span>忽略举报</span>
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  View, 
  Search, 
  Refresh, 
  RefreshRight, 
  Warning,
  CircleCheck,
  CircleClose,
  Clock
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, processReport, getReportStats } from '@/api/content'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  type: null,
  status: null,
  keyword: '',
  sortBy: 'id',
  sortOrder: 'descending'
})

// 统计数据
const stats = reactive({
  totalReports: 0,
  pendingReports: 0,
  processedRate: 0
})

// 表格数据
const loading = ref(false)
const reportList = ref([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const violationDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const processing = ref(false)
const currentReport = ref(null)
const violationForm = reactive({
  reason: '',
  remark: ''
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true
  try {
    const res = await getReportList({
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      type: searchForm.type,
      status: searchForm.status,
      keyword: searchForm.keyword,
      sortBy: searchForm.sortBy,
      sortOrder: searchForm.sortOrder
    })
    
    console.log('举报列表响应:', res);
    
    // 检查返回的数据结构，兼容不同的响应格式
    if (res.data && res.data.records) {
      // 处理返回的带分页信息的数据
      reportList.value = res.data.records || [];
      pagination.total = res.data.total || 0;
    } else if (res.records) {
      // 处理直接返回的分页对象
      reportList.value = res.records || [];
      pagination.total = res.total || 0;
    } else {
      // 兜底处理
      reportList.value = res || [];
      pagination.total = res.length || 0;
    }
    
    console.log('解析后的举报列表:', reportList.value);
  } catch (error) {
    console.error('Failed to fetch report list:', error)
    ElMessage.error('获取举报列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getReportStats()
    console.log('举报统计响应:', res);
    
    // 检查返回数据结构，兼容不同的响应格式
    if (res.data) {
      Object.assign(stats, res.data)
    } else {
      Object.assign(stats, res)
    }
    
    console.log('解析后的统计数据:', stats);
  } catch (error) {
    console.error('Failed to fetch report stats:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchReportList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    type: null,
    status: null,
    keyword: '',
    sortBy: 'id',
    sortOrder: 'descending'
  })
  pagination.currentPage = 1
  fetchReportList()
}

// 排序变更
const handleSortChange = (column) => {
  if (column.prop) {
    searchForm.sortBy = column.prop
    searchForm.sortOrder = column.order
    fetchReportList()
  }
}

// 查看内容
const handleView = (row) => {
  currentReport.value = row
  viewDialogVisible.value = true
}

// 违规处理
const handleViolation = (row) => {
  currentReport.value = row
  violationForm.reason = '含有违规内容'
  violationForm.remark = ''
  violationDialogVisible.value = true
}

// 确认违规
const confirmViolation = async () => {
  if (!violationForm.reason) {
    ElMessage.warning('请选择违规原因')
    return
  }
  
  if (violationForm.reason === '其他' && !violationForm.remark) {
    ElMessage.warning('请填写详细原因')
    return
  }
  
  processing.value = true
  try {
    const finalReason = violationForm.reason === '其他' 
      ? `其他: ${violationForm.remark}` 
      : violationForm.reason
      
    await processReport({
      reportId: currentReport.value.reportId || currentReport.value.id,
      targetId: currentReport.value.targetId,
      type: currentReport.value.reportType || currentReport.value.type,
      status: 1, // 标记为已处理
      reason: finalReason
    })
    
    ElMessage.success('违规处理成功')
    violationDialogVisible.value = false
    viewDialogVisible.value = false
    fetchReportList()
    fetchStats()
  } catch (error) {
    console.error('Failed to process violation:', error)
    ElMessage.error('处理失败，请重试')
  } finally {
    processing.value = false
  }
}

// 忽略举报
const handleIgnore = (row) => {
  ElMessageBox.confirm(
    '确定要忽略此举报吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await processReport({
        reportId: row.reportId || row.id,
        targetId: row.targetId,
        type: row.reportType || row.type,
        status: 2, // 标记为已忽略
        reason: '管理员判定不违规'
      })
      
      ElMessage.success('已忽略此举报')
      viewDialogVisible.value = false
      fetchReportList()
      fetchStats()
    } catch (error) {
      console.error('Failed to ignore report:', error)
      ElMessage.error('操作失败，请重试')
    }
  }).catch(() => {})
}

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchReportList()
}

// 页码变更
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchReportList()
}

// 页面初始化
onMounted(() => {
  fetchReportList()
  fetchStats()
})
</script>

<style lang="scss" scoped>
.report-list-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .stat-cards {
    margin-bottom: 20px;
    
    .stat-card {
      padding: 10px;
      
      .stat-content {
        display: flex;
        align-items: center;
        width: 100%;
      }
      
      .stat-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60px;
        height: 60px;
        background-color: #F56C6C;
        color: white;
        border-radius: 10px;
        margin-right: 15px;
        font-size: 30px;
        
        &.pending {
          background-color: #E6A23C;
        }
        
        &.processed {
          background-color: #67C23A;
        }
      }
      
      .stat-info {
        flex: 1;
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 5px;
        }
        
        .stat-label {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
  
  .table-card {
    margin-bottom: 20px;
    
    .report-content {
      max-height: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .report-detail {
    .dialog-actions {
      margin-top: 20px;
      display: flex;
      justify-content: center;
      gap: 10px;
    }
  }
}
</style> 