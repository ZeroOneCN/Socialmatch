<template>
  <div class="user-detail-container">
    <div class="page-header">
      <el-page-header @back="goBack" :title="`用户详情: ${userDetail?.username || ''}`">
        <template #extra>
          <el-button v-if="userDetail?.status === 1" type="danger" @click="handleDisableUser">禁用账号</el-button>
          <el-button v-else type="success" @click="handleEnableUser">启用账号</el-button>
          <el-button type="warning" @click="handleResetPassword">重置密码</el-button>
        </template>
      </el-page-header>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <!-- 用户基本信息 -->
      <el-col :span="8">
        <el-card shadow="never" class="user-card">
          <div class="user-header">
            <el-avatar :size="100" :src="userDetail?.avatar || userDetail?.profile?.avatar">
              {{ userDetail?.username?.substring(0, 1).toUpperCase() }}
            </el-avatar>
            <div class="user-status">
              <el-tag v-if="userDetail?.status === 1" type="success">正常</el-tag>
              <el-tag v-else type="danger">已禁用</el-tag>
            </div>
          </div>
          
          <div class="user-info">
            <div class="info-item">
              <span class="label">用户ID:</span>
              <span class="value">{{ userDetail?.userId }}</span>
            </div>
            <div class="info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ userDetail?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">昵称:</span>
              <span class="value">{{ userDetail?.profile?.nickname || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号:</span>
              <span class="value">{{ userDetail?.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别:</span>
              <span class="value">
                <el-tag v-if="userDetail?.gender === 1" type="primary" effect="plain">男</el-tag>
                <el-tag v-else-if="userDetail?.gender === 2" type="danger" effect="plain">女</el-tag>
                <el-tag v-else type="info" effect="plain">未知</el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">生日:</span>
              <span class="value">{{ formatDate(userDetail?.birthday) || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间:</span>
              <span class="value">{{ formatDate(userDetail?.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间:</span>
              <span class="value">{{ formatDate(userDetail?.updateTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 用户资料 -->
      <el-col :span="8">
        <el-card shadow="never" class="user-profile-card">
          <template #header>
            <div class="card-header">
              <span>用户资料</span>
            </div>
          </template>
          
          <div class="user-profile">
            <div class="info-item">
              <span class="label">学历:</span>
              <span class="value">{{ getEducationText(userDetail?.profile?.education) }}</span>
            </div>
            <div class="info-item">
              <span class="label">职业:</span>
              <span class="value">{{ userDetail?.profile?.occupation || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">所在地:</span>
              <span class="value">{{ userDetail?.profile?.location || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">城市:</span>
              <span class="value">{{ userDetail?.profile?.city || '未设置' }}</span>
            </div>
            <div class="info-item-large">
              <span class="label">自我介绍:</span>
              <div class="value text-area">{{ userDetail?.profile?.selfIntro || '未设置' }}</div>
            </div>
            <div class="info-item-large">
              <span class="label">兴趣爱好:</span>
              <div class="value text-area">{{ userDetail?.profile?.hobbies || '未设置' }}</div>
            </div>
            
            <div class="info-item">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatDate(userDetail?.profile?.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间:</span>
              <span class="value">{{ formatDate(userDetail?.profile?.updateTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 用户统计 -->
      <el-col :span="8">
        <el-card shadow="never" class="user-stats-card">
          <template #header>
            <div class="card-header">
              <span>统计数据</span>
            </div>
          </template>
          
          <div class="user-stats">
            <div class="stats-item">
              <div class="stats-icon bg-primary">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-label">动态数</div>
                <div class="stats-value">{{ userDetail?.stats?.postCount || 0 }}</div>
              </div>
            </div>
            
            <div class="stats-item">
              <div class="stats-icon bg-success">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-label">评论数</div>
                <div class="stats-value">{{ userDetail?.stats?.commentCount || 0 }}</div>
              </div>
            </div>
            
            <div class="stats-item">
              <div class="stats-icon bg-warning">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-label">关注数</div>
                <div class="stats-value">{{ userDetail?.stats?.followingCount || 0 }}</div>
              </div>
            </div>
            
            <div class="stats-item">
              <div class="stats-icon bg-danger">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-label">粉丝数</div>
                <div class="stats-value">{{ userDetail?.stats?.followerCount || 0 }}</div>
              </div>
            </div>
            
            <div class="stats-item">
              <div class="stats-icon bg-info">
                <el-icon><Link /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-label">匹配数</div>
                <div class="stats-value">{{ userDetail?.stats?.matchCount || 0 }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordDialog.visible" title="重置密码" width="400px">
      <div class="reset-password-content">
        <p>确定要重置用户 <b>{{ userDetail?.username }}</b> 的密码吗？</p>
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
        <p>用户 <b>{{ userDetail?.username }}</b> 的密码已重置为：</p>
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
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Document, ChatLineRound, Star, User, Link, DocumentCopy } from '@element-plus/icons-vue';
import * as userApi from '../../api/user';

const route = useRoute();
const router = useRouter();
const userId = ref(route.params.id);

// 数据相关
const loading = ref(false);
const userDetail = ref(null);

// 重置密码对话框
const passwordDialog = reactive({
  visible: false,
  loading: false
});

// 新密码显示对话框
const newPasswordDialog = reactive({
  visible: false,
  password: ''
});

// 获取用户详情
const fetchUserDetail = async () => {
  loading.value = true;
  try {
    const response = await userApi.getUserDetail(userId.value);
    if (!response || response.code === 404) {
      ElMessage.warning('用户不存在或已被删除');
      userDetail.value = null;
      return;
    }
    userDetail.value = response;
  } catch (error) {
    console.error('获取用户详情失败', error);
    ElMessage.warning(error.message || '获取用户详情失败');
    userDetail.value = null;
  } finally {
    loading.value = false;
  }
};

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      userId.value = newId;
      fetchUserDetail();
    }
  }
);

// 返回上一页
const goBack = () => {
  router.back();
};

// 禁用用户
const handleDisableUser = () => {
  ElMessageBox.confirm(`确定要禁用用户 ${userDetail.value.username} 的账号吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.updateUserStatus(userId.value, 0);
      ElMessage.success('用户已禁用');
      userDetail.value.status = 0;
    } catch (error) {
      console.error('禁用用户失败', error);
      ElMessage.error('禁用用户失败');
    }
  }).catch(() => {});
};

// 启用用户
const handleEnableUser = () => {
  ElMessageBox.confirm(`确定要启用用户 ${userDetail.value.username} 的账号吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.updateUserStatus(userId.value, 1);
      ElMessage.success('用户已启用');
      userDetail.value.status = 1;
    } catch (error) {
      console.error('启用用户失败', error);
      ElMessage.error('启用用户失败');
    }
  }).catch(() => {});
};

// 重置密码
const handleResetPassword = () => {
  passwordDialog.visible = true;
};

// 确认重置密码
const confirmResetPassword = async () => {
  passwordDialog.loading = true;
  try {
    const response = await userApi.resetUserPassword(userId.value);
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
  fetchUserDetail();
});
</script>

<style lang="scss" scoped>
.user-detail-container {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .user-card, .user-profile-card, .user-stats-card {
    height: 100%;
    
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: bold;
    }
  }
  
  .user-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 20px;
    
    .user-status {
      margin-top: 10px;
    }
  }
  
  .user-info, .user-profile {
    .info-item {
      display: flex;
      margin-bottom: 15px;
      
      .label {
        width: 100px;
        color: #606266;
        font-weight: bold;
      }
      
      .value {
        flex: 1;
      }
    }
    
    .info-item-large {
      margin-bottom: 15px;
      
      .label {
        display: block;
        margin-bottom: 5px;
        color: #606266;
        font-weight: bold;
      }
      
      .text-area {
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 10px;
        min-height: 60px;
        margin-bottom: 10px;
      }
    }
  }
  
  .user-stats {
    .stats-item {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .stats-icon {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        color: white;
        font-size: 24px;
      }
      
      .stats-info {
        flex: 1;
        
        .stats-label {
          color: #606266;
          font-size: 14px;
          margin-bottom: 5px;
        }
        
        .stats-value {
          font-size: 24px;
          font-weight: bold;
        }
      }
    }
    
    .bg-primary {
      background-color: #409EFF;
    }
    
    .bg-success {
      background-color: #67C23A;
    }
    
    .bg-warning {
      background-color: #E6A23C;
    }
    
    .bg-danger {
      background-color: #F56C6C;
    }
    
    .bg-info {
      background-color: #909399;
    }
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