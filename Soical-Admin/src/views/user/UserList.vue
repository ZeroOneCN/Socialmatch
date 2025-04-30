<template>
  <div class="user-list-container">
    <h1 class="page-title">用户管理</h1>
    
    <!-- 搜索和过滤 -->
    <div class="table-operations">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="搜索用户名/昵称/手机号" clearable @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 120px;">
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 用户列表表格 -->
    <el-card shadow="never" class="table-card">
      <el-table 
        v-loading="loading" 
        :data="userList" 
        stripe 
        border 
        style="width: 100%"
        @sort-change="handleSortChange"
        :default-sort="{prop: 'userId', order: 'ascending'}"
      >
        <el-table-column prop="userId" label="用户ID" width="80" align="center" sortable="custom" :sort-orders="['ascending', 'descending', null]" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || scope.row.profileAvatar" :alt="scope.row.username">
              {{ scope.row.username.substring(0, 1).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="profile.nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.gender === 1" type="primary" effect="plain">男</el-tag>
            <el-tag v-else-if="scope.row.gender === 2" type="danger" effect="plain">女</el-tag>
            <el-tag v-else type="info" effect="plain">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="profile.education" label="学历" width="100">
          <template #default="scope">
            <span>{{ getEducationText(scope.row.profile?.education) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="profile.occupation" label="职业" width="120" />
        <el-table-column prop="profile.location" label="地区" width="120" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="150" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button type="warning" link @click="handleResetPassword(scope.row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordDialog.visible" title="重置密码" width="400px">
      <div class="reset-password-content">
        <p>确定要重置用户 <b>{{ passwordDialog.username }}</b> 的密码吗？</p>
        <p class="warning">重置后将生成随机密码，请记录并告知用户。</p>
      </div>
      <template #footer>
        <el-button @click="passwordDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="passwordDialog.loading" @click="confirmResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
    
    <!-- 新密码显示对话框 -->
    <el-dialog v-model="newPasswordDialog.visible" title="密码已重置" width="400px">
      <div class="new-password-content">
        <p>用户 <b>{{ passwordDialog.username }}</b> 的密码已重置为：</p>
        <div class="password-display">
          {{ newPasswordDialog.password }}
          <el-button type="primary" link size="small" @click="copyPassword">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
        </div>
        <p class="warning">请妥善保管此密码并及时通知用户！</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="newPasswordDialog.visible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, DocumentCopy } from '@element-plus/icons-vue';
import * as userApi from '../../api/user';

const router = useRouter();

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  sortBy: 'userId',
  sortOrder: 'asc'
});

// 数据相关
const loading = ref(false);
const userList = ref([]);
const total = ref(0);

// 重置密码对话框
const passwordDialog = reactive({
  visible: false,
  loading: false,
  userId: null,
  username: ''
});

// 新密码显示对话框
const newPasswordDialog = reactive({
  visible: false,
  password: ''
});

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const response = await userApi.getUserList(queryParams);
    userList.value = response.records || [];
    total.value = response.total || 0;
  } catch (error) {
    console.error('获取用户列表失败', error);
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 查询
const handleSearch = () => {
  queryParams.page = 1;
  fetchUserList();
};

// 重置查询
const resetQuery = () => {
  queryParams.keyword = '';
  queryParams.status = null;
  queryParams.page = 1;
  fetchUserList();
};

// 排序变化
const handleSortChange = (column) => {
  if (column.prop) {
    queryParams.sortBy = column.prop;
    queryParams.sortOrder = column.order === 'ascending' ? 'asc' : 'desc';
    fetchUserList();
  }
};

// 分页大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size;
  fetchUserList();
};

// 页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page;
  fetchUserList();
};

// 状态变化
const handleStatusChange = async (row) => {
  try {
    await userApi.updateUserStatus(row.userId, row.status);
    ElMessage.success(`已${row.status === 1 ? '启用' : '禁用'}该用户账号`);
  } catch (error) {
    console.error('更新用户状态失败', error);
    ElMessage.error('更新用户状态失败');
    // 状态回滚
    row.status = row.status === 1 ? 0 : 1;
  }
};

// 查看详情
const handleViewDetail = (row) => {
  router.push(`/user/detail/${row.userId}`);
};

// 重置密码
const handleResetPassword = (row) => {
  passwordDialog.userId = row.userId;
  passwordDialog.username = row.username;
  passwordDialog.visible = true;
};

// 确认重置密码
const confirmResetPassword = async () => {
  passwordDialog.loading = true;
  try {
    const response = await userApi.resetUserPassword(passwordDialog.userId);
    passwordDialog.visible = false;
    
    // 显示新密码
    newPasswordDialog.password = response.password;
    newPasswordDialog.visible = true;
  } catch (error) {
    console.error('重置密码失败', error);
    ElMessage.error('重置密码失败');
  } finally {
    passwordDialog.loading = false;
  }
};

// 复制密码
const copyPassword = () => {
  navigator.clipboard.writeText(newPasswordDialog.password)
    .then(() => {
      ElMessage.success('密码已复制到剪贴板');
    })
    .catch(() => {
      ElMessage.error('复制失败，请手动复制');
    });
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString();
};

// 获取学历文本
const getEducationText = (education) => {
  const educationMap = {
    0: '未知',
    1: '大专',
    2: '本科',
    3: '硕士',
    4: '博士'
  };
  return educationMap[education] || '未知';
};

// 初始化
onMounted(() => {
  fetchUserList();
});
</script>

<style lang="scss" scoped>
.user-list-container {
  padding: 20px;
  
  .page-title {
    margin-top: 0;
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
  }
  
  .table-operations {
    margin-bottom: 20px;
    
    .filter-form {
      display: flex;
      align-items: center;
    }
  }
  
  .table-card {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .reset-password-content,
  .new-password-content {
    text-align: center;
    
    .warning {
      color: #E6A23C;
      font-size: 14px;
      margin-top: 10px;
    }
    
    .password-display {
      background-color: #f5f7fa;
      padding: 10px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 18px;
      margin: 15px 0;
      display: flex;
      justify-content: center;
      align-items: center;
      
      .el-button {
        margin-left: 10px;
      }
    }
  }
}
</style> 