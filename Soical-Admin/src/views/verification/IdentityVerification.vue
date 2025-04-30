<template>
  <div class="verification-card">
    <div class="card-header">
      <h2>身份认证管理</h2>
    </div>
    
    <div class="card-content">
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
                <button class="btn-view" @click="viewVerification(item.verificationId)">查看</button>
                <button v-if="item.status === 'pending'" class="btn-approve" @click="approveVerification(item.verificationId)">通过</button>
                <button v-if="item.status === 'pending'" class="btn-reject" @click="showRejectDialog(item.verificationId)">拒绝</button>
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
          <h2>身份认证详情</h2>
          <button class="btn-close" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="!currentVerification" class="loading-state">
            <p>加载中，请稍候...</p>
          </div>
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
              <span class="detail-label">提交时间：</span>
              <span>{{ formatDate(currentVerification.createTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态：</span>
              <span class="status-tag" :class="getStatusClass(currentVerification.status)">
                {{ getStatusText(currentVerification.status) }}
              </span>
            </div>
            
            <div v-if="currentVerification.details">
              <h3 class="detail-section-title">身份信息</h3>
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
            
            <div v-if="currentVerification.status === 'rejected'">
              <div class="detail-row">
                <span class="detail-label">拒绝原因：</span>
                <span class="reject-reason">{{ currentVerification.rejectReason || '无' }}</span>
              </div>
            </div>
            
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
            <textarea v-model="rejectReason" placeholder="请输入拒绝原因，将展示给用户" rows="4" maxlength="200"></textarea>
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
import { ref, reactive, computed, onMounted } from 'vue';
import {
  getPendingIdentityVerifications,
  getVerificationDetail,
  auditVerification,
  getAllIdentityVerifications
} from '@/api/verification';

export default {
  name: 'IdentityVerification',
  setup() {
    const statusFilter = ref('all');
    const searchText = ref('');
    const verifications = ref([]);
    const showDetailModal = ref(false);
    const showRejectModal = ref(false);
    const currentVerification = ref(null);
    const pendingVerificationId = ref(null);
    const rejectReason = ref('');
    
    // 获取认证列表
    const fetchVerifications = async () => {
      try {
        let response;
        if (statusFilter.value === 'pending') {
          response = await getPendingIdentityVerifications();
        } else {
          response = await getAllIdentityVerifications();
        }
        
        if (response && Array.isArray(response)) {
          verifications.value = response;
        } else if (response && response.data && Array.isArray(response.data)) {
          verifications.value = response.data;
        } else {
          verifications.value = [];
        }
      } catch (error) {
        console.error('获取认证列表失败', error);
        verifications.value = [];
      }
    };
    
    // 过滤后的认证列表
    const filteredVerifications = computed(() => {
      let result = verifications.value;
      
      if (statusFilter.value !== 'all') {
        result = result.filter(item => item.status === statusFilter.value);
      }
      
      if (searchText.value) {
        const keyword = searchText.value.trim().toLowerCase();
        result = result.filter(item => 
          item.userId?.toString().includes(keyword) ||
          item.verificationId?.toString().includes(keyword)
        );
      }
      
      return result;
    });
    
    // 查看认证详情
    const viewVerification = async (verificationId) => {
      try {
        currentVerification.value = null;
        showDetailModal.value = true;
        
        const response = await getVerificationDetail(verificationId);
        if (response && response.data) {
          currentVerification.value = response.data;
        } else if (response) {
          currentVerification.value = response;
        }
      } catch (error) {
        console.error('获取认证详情失败', error);
        alert('获取认证详情失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 通过认证
    const approveVerification = async (verificationId) => {
      if (!confirm('确认通过此认证申请？')) return;
      
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
    
    onMounted(() => {
      fetchVerifications();
    });
    
    return {
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
.verification-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 20px;
  overflow: hidden;
}

.card-header {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.card-content {
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: #606266;
}

.filter-group select {
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  min-width: 120px;
}

.search-box {
  display: flex;
  flex: 1;
  max-width: 300px;
}

.search-box input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px 0 0 4px;
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
  padding: 6px 12px;
  border: 1px solid #1890ff;
  border-radius: 4px;
}

.refresh-btn i {
  margin-right: 4px;
}

.table-wrapper {
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
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  color: #999;
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

.loading-state {
  text-align: center;
  padding: 30px 0;
  color: #909399;
}
</style> 