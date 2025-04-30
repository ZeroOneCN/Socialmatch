<template>
  <div class="verification-management">
    <div class="page-header">
      <h1>认证管理</h1>
    </div>
    
    <div class="tab-container">
      <div class="tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'identity' }"
          @click="activeTab = 'identity'"
        >
          身份认证
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'education' }"
          @click="activeTab = 'education'"
        >
          教育认证
        </div>
      </div>
      
      <div class="filter-bar">
        <div class="filter-group">
          <label>状态:</label>
          <select v-model="statusFilter">
            <option value="all">全部</option>
            <option value="pending">待审核</option>
            <option value="approved">已通过</option>
            <option value="rejected">已拒绝</option>
          </select>
        </div>
        <div class="search-box">
          <input type="text" v-model="searchText" placeholder="搜索用户ID" @keyup.enter="handleSearch" />
          <button @click="handleSearch">搜索</button>
        </div>
        <div class="refresh-btn" @click="fetchVerifications">
          <i class="icon-refresh"></i>
          刷新
        </div>
      </div>
      
      <div class="table-wrapper">
        <table class="verification-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户ID</th>
              <th>提交时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredVerifications" :key="item.verificationId">
              <td>{{ item.verificationId }}</td>
              <td>{{ item.userId }}</td>
              <td>{{ formatDate(item.createTime) }}</td>
              <td>
                <span class="status-tag" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
              </td>
              <td>
                <button 
                  class="btn-view" 
                  @click="viewVerification(item.verificationId)"
                >
                  查看
                </button>
                <button 
                  v-if="item.status === 'pending'"
                  class="btn-approve" 
                  @click="approveVerification(item.verificationId)"
                >
                  通过
                </button>
                <button 
                  v-if="item.status === 'pending'"
                  class="btn-reject" 
                  @click="showRejectDialog(item.verificationId)"
                >
                  拒绝
                </button>
              </td>
            </tr>
            <tr v-if="filteredVerifications.length === 0">
              <td colspan="5" class="empty-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 认证详情弹窗 -->
    <div class="detail-modal" v-if="showDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>认证详情</h2>
          <button class="btn-close" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 加载中状态 -->
          <div v-if="!currentVerification" class="loading-state">
            <p>加载中，请稍候...</p>
          </div>
          
          <!-- 详情内容 -->
          <div v-else>
            <div class="detail-row">
              <span class="detail-label">认证ID：</span>
              <span>{{ currentVerification.verificationId }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">用户ID：</span>
              <span>{{ currentVerification.userId }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">认证类型：</span>
              <span>{{ currentVerification.type === 'identity' ? '身份认证' : '教育认证' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">提交时间：</span>
              <span>{{ formatDate(currentVerification.createTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态：</span>
              <span class="status-tag" :class="getStatusClass(currentVerification.status)">
                {{ getStatusText(currentVerification.status) }}
              </span>
            </div>
            
            <!-- 身份认证详情 -->
            <div v-if="currentVerification.type === 'identity'">
              <h3 class="detail-section-title">身份信息</h3>
              
              <!-- 认证详情数据 -->
              <div v-if="currentVerification.details">
                <div class="detail-row">
                  <span class="detail-label">真实姓名：</span>
                  <span>{{ currentVerification.details.realName }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">身份证号：</span>
                  <span>{{ currentVerification.details.idNumber }}</span>
                </div>
                <div class="document-images">
                  <div class="document-image">
                    <h4>身份证正面</h4>
                    <img :src="currentVerification.details.idCardFront" alt="身份证正面" />
                  </div>
                  <div class="document-image">
                    <h4>身份证背面</h4>
                    <img :src="currentVerification.details.idCardBack" alt="身份证背面" />
                  </div>
                </div>
              </div>
              
              <!-- 无details数据的错误提示 -->
              <div v-else class="error-message">
                <p>无法加载身份认证详细信息</p>
              </div>
            </div>
            
            <!-- 教育认证详情 -->
            <div v-if="currentVerification.type === 'education'">
              <h3 class="detail-section-title">教育信息</h3>
              
              <!-- 认证详情数据 -->
              <div v-if="currentVerification.details">
                <div class="detail-row">
                  <span class="detail-label">学校：</span>
                  <span>{{ currentVerification.details.school }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">学院：</span>
                  <span>{{ currentVerification.details.college || '未填写' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">专业：</span>
                  <span>{{ currentVerification.details.major }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">学号：</span>
                  <span>{{ currentVerification.details.studentId }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">入学年份：</span>
                  <span>{{ currentVerification.details.enrollmentYear }}</span>
                </div>
                <div class="document-images">
                  <div class="document-image">
                    <h4>学生证封面</h4>
                    <img :src="currentVerification.details.studentCardFront" alt="学生证封面" />
                  </div>
                  <div class="document-image">
                    <h4>学生证内页</h4>
                    <img :src="currentVerification.details.studentCardInside" alt="学生证内页" />
                  </div>
                </div>
              </div>
              
              <!-- 无details数据的错误提示 -->
              <div v-else class="error-message">
                <p>无法加载教育认证详细信息</p>
              </div>
            </div>
            
            <!-- 拒绝原因 -->
            <div v-if="currentVerification.status === 'rejected'">
              <div class="detail-row">
                <span class="detail-label">拒绝原因：</span>
                <span class="reject-reason">{{ currentVerification.rejectReason || '无' }}</span>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div v-if="currentVerification.status === 'pending'" class="action-buttons">
              <button class="btn-approve" @click="approveVerification(currentVerification.verificationId)">通过</button>
              <button class="btn-reject" @click="showRejectDialog(currentVerification.verificationId)">拒绝</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 拒绝理由弹窗 -->
    <div class="reject-modal" v-if="showRejectModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>填写拒绝原因</h2>
          <button class="btn-close" @click="showRejectModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>拒绝原因：</label>
            <textarea 
              v-model="rejectReason" 
              placeholder="请输入拒绝原因，将展示给用户" 
              rows="4"
              maxlength="200"
            ></textarea>
            <div class="char-count">{{ rejectReason.length }}/200</div>
          </div>
          <div class="form-footer">
            <button @click="showRejectModal = false" class="btn-cancel">取消</button>
            <button @click="rejectVerification" class="btn-submit" :disabled="!rejectReason">确认拒绝</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  getPendingIdentityVerifications,
  getPendingEducationVerifications,
  getVerificationDetail,
  auditVerification,
  getAllIdentityVerifications,
  getAllEducationVerifications
} from '@/api/verification';

export default {
  name: 'VerificationManagement',
  props: {
    type: {
      type: String,
      default: 'identity'
    }
  },
  setup(props) {
    const route = useRoute();
    // 状态变量
    const activeTab = ref(props.type || 'identity');
    const statusFilter = ref('pending');
    const searchText = ref('');
    const verifications = ref([]);
    const showDetailModal = ref(false);
    const showRejectModal = ref(false);
    const currentVerification = ref(null);
    const pendingVerificationId = ref(null);
    const rejectReason = ref('');
    
    // 修改路由监听，解决刚进入页面不加载数据的问题
    onMounted(() => {
      console.log('VerificationManagement mounted');
      console.log('Current route path:', route.path);
      console.log('Route props type:', props.type);
      console.log('Route params:', route.params);
      console.log('Route query:', route.query);
      
      // 根据props或路由参数设置activeTab
      if (props.type) {
        console.log('Setting activeTab from props:', props.type);
        activeTab.value = props.type;
      } else if (route.params.type) {
        console.log('Setting activeTab from route params:', route.params.type);
        activeTab.value = route.params.type;
      } else if (route.path.includes('/verification/identity')) {
        console.log('Setting activeTab from path pattern: identity');
        activeTab.value = 'identity';
      } else if (route.path.includes('/verification/education')) {
        console.log('Setting activeTab from path pattern: education');
        activeTab.value = 'education';
      } else {
        console.log('Setting activeTab to default: identity');
        activeTab.value = 'identity'; // 默认显示身份认证
      }
      
      console.log('Active tab set to:', activeTab.value);
      
      // 立即获取数据
      fetchVerifications();
    });
    
    // 监听标签切换，重新获取数据
    watch(activeTab, () => {
      console.log('activeTab changed to:', activeTab.value);
      fetchVerifications();
    });
    
    // 监听状态过滤器变化，重新获取数据
    watch(statusFilter, () => {
      console.log('statusFilter changed to:', statusFilter.value);
      fetchVerifications();
    });
    
    // 获取认证列表
    const fetchVerifications = async () => {
      console.log('Fetching verifications for tab:', activeTab.value, 'with status filter:', statusFilter.value);
      try {
        let response;
        // 根据状态过滤条件选择不同的API
        if (statusFilter.value === 'pending') {
          // 如果只查看待审核的
          if (activeTab.value === 'identity') {
            console.log('Calling getPendingIdentityVerifications API');
            response = await getPendingIdentityVerifications();
          } else {
            console.log('Calling getPendingEducationVerifications API');
            response = await getPendingEducationVerifications();
          }
        } else {
          // 如果查看全部的，包括已通过和已拒绝的
          if (activeTab.value === 'identity') {
            console.log('Calling getAllIdentityVerifications API');
            response = await getAllIdentityVerifications();
          } else {
            console.log('Calling getAllEducationVerifications API');
            response = await getAllEducationVerifications();
          }
        }
        
        console.log('API response:', response);
        
        if (response && Array.isArray(response)) {
          verifications.value = response;
          console.log('Set verifications array:', verifications.value);
        } else if (response && response.data && Array.isArray(response.data)) {
          verifications.value = response.data;
          console.log('Set verifications from response.data:', verifications.value);
        } else {
          console.warn('Empty or invalid response:', response);
          verifications.value = [];
        }
      } catch (error) {
        console.error('获取认证列表失败', error);
        if (error.response) {
          console.error('Error response:', error.response.status, error.response.data);
        }
        verifications.value = [];
      }
    };
    
    // 过滤后的认证列表
    const filteredVerifications = computed(() => {
      let result = verifications.value;
      console.log('Computing filteredVerifications from:', result);
      
      // 按状态过滤
      if (statusFilter.value !== 'all') {
        result = result.filter(item => item.status === statusFilter.value);
      }
      
      // 按搜索关键字过滤
      if (searchText.value) {
        const keyword = searchText.value.trim().toLowerCase();
        result = result.filter(item => 
          item.userId?.toString().includes(keyword) ||
          item.verificationId?.toString().includes(keyword)
        );
      }
      
      console.log('Filtered verifications:', result);
      return result;
    });
    
    // 查看认证详情
    const viewVerification = async (verificationId) => {
      try {
        console.log('查看认证详情, ID:', verificationId);
        // 先清空当前详情，避免显示旧数据
        currentVerification.value = null;
        // 先显示弹窗，给用户立即反馈
        showDetailModal.value = true;
        
        // 获取认证详情
        const response = await getVerificationDetail(verificationId);
        console.log('认证详情API响应:', response);
        
        // 处理不同的数据结构
        if (response && response.data) {
          console.log('从response.data中获取详情');
          currentVerification.value = response.data;
        } else if (response) {
          console.log('使用response作为详情');
          currentVerification.value = response;
        } else {
          console.error('API返回了空数据');
          alert('获取认证详情失败: 返回数据为空');
        }
        
        // 检查details字段
        if (currentVerification.value && !currentVerification.value.details) {
          console.warn('认证详情中缺少details字段:', currentVerification.value);
        } else if (currentVerification.value && currentVerification.value.details) {
          console.log('认证详情包含details字段:', currentVerification.value.details);
        }
      } catch (error) {
        console.error('获取认证详情失败', error);
        if (error.response) {
          console.error('错误状态:', error.response.status);
          console.error('错误数据:', error.response.data);
        }
        // 显示错误提示
        alert('获取认证详情失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 通过认证
    const approveVerification = async (verificationId) => {
      if (!confirm('确认通过此认证申请？')) {
        return;
      }
      
      try {
        await auditVerification({
          verificationId,
          status: 'approved',
          rejectReason: ''
        });
        alert('审核通过成功');
        showDetailModal.value = false;
        fetchVerifications();
      } catch (error) {
        console.error('审核操作失败', error);
        alert('操作失败：' + (error.message || '未知错误'));
      }
    };
    
    // 显示拒绝对话框
    const showRejectDialog = (verificationId) => {
      pendingVerificationId.value = verificationId;
      rejectReason.value = '';
      showRejectModal.value = true;
    };
    
    // 拒绝认证
    const rejectVerification = async () => {
      if (!rejectReason.value.trim()) {
        alert('请填写拒绝原因');
        return;
      }
      
      try {
        await auditVerification({
          verificationId: pendingVerificationId.value,
          status: 'rejected',
          rejectReason: rejectReason.value
        });
        
        alert('已拒绝该认证申请');
        showRejectModal.value = false;
        showDetailModal.value = false;
        fetchVerifications();
      } catch (error) {
        console.error('拒绝认证失败', error);
        alert('操作失败：' + (error.message || '未知错误'));
      }
    };
    
    // 处理搜索
    const handleSearch = () => {
      // 过滤会自动触发
    };
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString();
    };
    
    // 获取状态样式
    const getStatusClass = (status) => {
      switch(status) {
        case 'pending': return 'status-pending';
        case 'approved': return 'status-approved';
        case 'rejected': return 'status-rejected';
        default: return '';
      }
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      switch(status) {
        case 'pending': return '待审核';
        case 'approved': return '已通过';
        case 'rejected': return '已拒绝';
        default: return '未知';
      }
    };
    
    return {
      activeTab,
      statusFilter,
      searchText,
      verifications,
      filteredVerifications,
      showDetailModal,
      showRejectModal,
      currentVerification,
      pendingVerificationId,
      rejectReason,
      fetchVerifications,
      viewVerification,
      approveVerification,
      showRejectDialog,
      rejectVerification,
      handleSearch,
      formatDate,
      getStatusClass,
      getStatusText
    };
  }
};
</script>

<style scoped>
.verification-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.tab-container {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
}

.tab-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.tab-item:hover {
  color: #1890ff;
}

.tab-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.filter-bar {
  display: flex;
  padding: 16px;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
}

.filter-group {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.filter-group label {
  margin-right: 8px;
  font-size: 14px;
}

.filter-group select {
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.search-box {
  display: flex;
  margin-right: 20px;
}

.search-box input {
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px 0 0 4px;
  width: 200px;
}

.search-box button {
  padding: 6px 14px;
  background: #1890ff;
  border: none;
  color: white;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.refresh-btn {
  display: flex;
  align-items: center;
  color: #1890ff;
  cursor: pointer;
}

.refresh-btn i {
  margin-right: 4px;
}

.table-wrapper {
  padding: 16px;
  overflow-x: auto;
}

.verification-table {
  width: 100%;
  border-collapse: collapse;
}

.verification-table th,
.verification-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
}

.verification-table thead tr {
  background-color: #fafafa;
}

.verification-table th {
  font-weight: 500;
  color: #606266;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.empty-data {
  text-align: center;
  color: #999;
  padding: 30px 0;
}

.btn-view, .btn-approve, .btn-reject {
  margin-right: 8px;
  padding: 5px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-view {
  background-color: #f4f4f4;
  color: #606266;
}

.btn-approve {
  background-color: #52c41a;
  color: white;
}

.btn-reject {
  background-color: #ff4d4f;
  color: white;
}

/* 弹窗样式 */
.detail-modal, .reject-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 4px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.detail-row {
  margin-bottom: 16px;
  display: flex;
}

.detail-label {
  width: 100px;
  color: #606266;
  flex-shrink: 0;
}

.detail-section-title {
  margin: 24px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
  font-size: 16px;
  font-weight: 500;
}

.document-images {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.document-image {
  flex: 1;
  min-width: 300px;
}

.document-image h4 {
  margin-bottom: 10px;
  font-weight: 500;
}

.document-image img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.reject-reason {
  color: #ff4d4f;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.action-buttons button {
  padding: 8px 24px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
}

.char-count {
  text-align: right;
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 8px 16px;
  background-color: #f4f4f4;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-submit {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-submit:disabled {
  background-color: #bfbfbf;
  cursor: not-allowed;
}

/* 加载状态样式 */
.loading-state {
  text-align: center;
  padding: 30px 0;
  color: #909399;
}

/* 错误信息样式 */
.error-message {
  background-color: #fff6f6;
  border: 1px solid #ffa39e;
  border-radius: 4px;
  padding: 12px 16px;
  color: #f56c6c;
  margin: 15px 0;
}
</style> 