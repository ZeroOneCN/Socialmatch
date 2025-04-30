<template>
  <div class="verification-center">
    <div class="page-header">
      <button class="back-icon-button" @click="goBack">
        <i class="icon-arrow-left"></i>
      </button>
      <h1>认证中心</h1>
      <p class="header-desc">通过认证获得更多信任和特权</p>
    </div>
    
    <div class="verification-cards">
      <!-- 身份认证卡片 -->
      <div class="verification-card">
        <div class="card-header">
          <div class="card-title">
            <i class="icon-id-card"></i>
            <span>身份认证</span>
          </div>
          <div class="card-status" :class="identityStatusClass">{{ identityStatusText }}</div>
        </div>
        <div class="card-content">
          <p>通过身份认证，您的账号将获得实名认证标识，增加账号可信度</p>
          
          <template v-if="verificationStatus.identityStatus === 'approved'">
            <div class="verified-info">
              <div class="info-item">
                <label>真实姓名：</label>
                <span>{{ verificationStatus.realName }}</span>
              </div>
            </div>
          </template>
          
          <template v-else-if="verificationStatus.identityStatus === 'rejected'">
            <div class="reject-reason">
              <p>拒绝原因：{{ identityRejection }}</p>
            </div>
          </template>
        </div>
        <div class="card-footer">
          <button 
            v-if="verificationStatus.identityStatus === 'not_submitted' || verificationStatus.identityStatus === 'rejected'" 
            @click="showIdentityForm = true" 
            class="btn-primary"
          >
            {{ verificationStatus.identityStatus === 'rejected' ? '重新提交' : '去认证' }}
          </button>
          <button 
            v-else-if="verificationStatus.identityStatus === 'pending'" 
            class="btn-disabled"
            disabled
          >
            审核中
          </button>
          <div v-else class="verified-tag">
            <i class="icon-check"></i>
            <span>已认证</span>
          </div>
        </div>
      </div>
      
      <!-- 教育认证卡片 -->
      <div class="verification-card">
        <div class="card-header">
          <div class="card-title">
            <i class="icon-school"></i>
            <span>教育认证</span>
          </div>
          <div class="card-status" :class="educationStatusClass">{{ educationStatusText }}</div>
        </div>
        <div class="card-content">
          <p>通过教育认证，您可以获得校园认证标识，匹配更多同校同学</p>
          
          <template v-if="verificationStatus.educationStatus === 'approved'">
            <div class="verified-info">
              <div class="info-item">
                <label>学校：</label>
                <span>{{ verificationStatus.school }}</span>
              </div>
            </div>
          </template>
          
          <template v-else-if="verificationStatus.educationStatus === 'rejected'">
            <div class="reject-reason">
              <p>拒绝原因：{{ educationRejection }}</p>
            </div>
          </template>
        </div>
        <div class="card-footer">
          <button 
            v-if="verificationStatus.educationStatus === 'not_submitted' || verificationStatus.educationStatus === 'rejected'" 
            @click="showEducationForm = true" 
            class="btn-primary"
          >
            {{ verificationStatus.educationStatus === 'rejected' ? '重新提交' : '去认证' }}
          </button>
          <button 
            v-else-if="verificationStatus.educationStatus === 'pending'" 
            class="btn-disabled"
            disabled
          >
            审核中
          </button>
          <div v-else class="verified-tag">
            <i class="icon-check"></i>
            <span>已认证</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 身份认证表单 -->
    <div class="verification-form-modal" v-if="showIdentityForm">
      <div class="modal-content">
        <div class="modal-header">
          <h2>身份认证</h2>
          <button class="btn-close" @click="showIdentityForm = false">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitIdentity">
            <div class="form-group">
              <label>真实姓名</label>
              <input 
                type="text" 
                v-model="identityForm.realName" 
                required 
                placeholder="请输入您的真实姓名"
              />
            </div>
            <div class="form-group">
              <label>身份证号</label>
              <input 
                type="text" 
                v-model="identityForm.idNumber" 
                required 
                placeholder="请输入您的身份证号"
                pattern="(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)"
                title="请输入有效的身份证号"
              />
            </div>
            <div class="form-group">
              <label>身份证正面照片</label>
              <div class="upload-box">
                <input 
                  type="file" 
                  @change="handleIdFrontUpload" 
                  accept="image/*"
                  ref="idFrontInput"
                />
                <div class="preview" v-if="identityForm.idCardFront">
                  <img :src="identityForm.idCardFront" alt="身份证正面" />
                </div>
                <div class="upload-btn" v-else>
                  <i class="icon-upload"></i>
                  <span>上传身份证正面</span>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>身份证背面照片</label>
              <div class="upload-box">
                <input 
                  type="file" 
                  @change="handleIdBackUpload" 
                  accept="image/*"
                  ref="idBackInput"
                />
                <div class="preview" v-if="identityForm.idCardBack">
                  <img :src="identityForm.idCardBack" alt="身份证背面" />
                </div>
                <div class="upload-btn" v-else>
                  <i class="icon-upload"></i>
                  <span>上传身份证背面</span>
                </div>
              </div>
            </div>
            <div class="form-footer">
              <button type="button" class="btn-cancel" @click="showIdentityForm = false">取消</button>
              <button type="submit" class="btn-submit" :disabled="identitySubmitting">提交</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 教育认证表单 -->
    <div class="verification-form-modal" v-if="showEducationForm">
      <div class="modal-content">
        <div class="modal-header">
          <h2>教育认证</h2>
          <button class="btn-close" @click="showEducationForm = false">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitEducation">
            <div class="form-group">
              <label>学校</label>
              <input 
                type="text" 
                v-model="educationForm.school" 
                required 
                placeholder="请输入您的学校名称"
              />
            </div>
            <div class="form-group">
              <label>学院</label>
              <input 
                type="text" 
                v-model="educationForm.college" 
                placeholder="请输入您的学院名称(选填)"
              />
            </div>
            <div class="form-group">
              <label>专业</label>
              <input 
                type="text" 
                v-model="educationForm.major" 
                required 
                placeholder="请输入您的专业"
              />
            </div>
            <div class="form-group">
              <label>学号</label>
              <input 
                type="text" 
                v-model="educationForm.studentId" 
                required 
                placeholder="请输入您的学号"
              />
            </div>
            <div class="form-group">
              <label>入学年份</label>
              <input 
                type="number" 
                v-model="educationForm.enrollmentYear" 
                required 
                placeholder="请输入您的入学年份"
                min="1950"
                max="2100"
              />
            </div>
            <div class="form-group">
              <label>学生证封面照片</label>
              <div class="upload-box">
                <input 
                  type="file" 
                  @change="handleStudentFrontUpload" 
                  accept="image/*"
                  ref="studentFrontInput"
                />
                <div class="preview" v-if="educationForm.studentCardFront">
                  <img :src="educationForm.studentCardFront" alt="学生证封面" />
                </div>
                <div class="upload-btn" v-else>
                  <i class="icon-upload"></i>
                  <span>上传学生证封面</span>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>学生证内页照片</label>
              <div class="upload-box">
                <input 
                  type="file" 
                  @change="handleStudentInsideUpload" 
                  accept="image/*"
                  ref="studentInsideInput"
                />
                <div class="preview" v-if="educationForm.studentCardInside">
                  <img :src="educationForm.studentCardInside" alt="学生证内页" />
                </div>
                <div class="upload-btn" v-else>
                  <i class="icon-upload"></i>
                  <span>上传学生证内页</span>
                </div>
              </div>
            </div>
            <div class="form-footer">
              <button type="button" class="btn-cancel" @click="showEducationForm = false">取消</button>
              <button type="submit" class="btn-submit" :disabled="educationSubmitting">提交</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useUserStore } from '../../stores/user'; // 改用Pinia store
import {
  getUserVerificationStatus,
  submitIdentityVerification,
  submitEducationVerification,
  getUserIdentityVerifications,
  getUserEducationVerifications
} from '../../api/verification';
import { uploadImage } from '../../api/upload'; // 假设有此API
import { useRouter } from 'vue-router';

// 使用Pinia Store
const userStore = useUserStore();
const userId = computed(() => userStore.userId);
const router = useRouter(); // Move useRouter hook to the top level

const verificationStatus = reactive({
  userId: null,
  identityStatus: 'not_submitted',
  educationStatus: 'not_submitted',
  realName: '',
  school: ''
});

const identityRejection = ref('');
const educationRejection = ref('');

const showIdentityForm = ref(false);
const showEducationForm = ref(false);

const identitySubmitting = ref(false);
const educationSubmitting = ref(false);

const identityForm = reactive({
  realName: '',
  idNumber: '',
  idCardFront: '',
  idCardBack: ''
});

const educationForm = reactive({
  school: '',
  college: '',
  major: '',
  studentId: '',
  enrollmentYear: new Date().getFullYear(),
  studentCardFront: '',
  studentCardInside: ''
});

// 计算属性：状态文本和样式
const identityStatusText = computed(() => {
  switch(verificationStatus.identityStatus) {
    case 'not_submitted': return '未认证';
    case 'pending': return '审核中';
    case 'approved': return '已认证';
    case 'rejected': return '已拒绝';
    default: return '未认证';
  }
});

const educationStatusText = computed(() => {
  switch(verificationStatus.educationStatus) {
    case 'not_submitted': return '未认证';
    case 'pending': return '审核中';
    case 'approved': return '已认证';
    case 'rejected': return '已拒绝';
    default: return '未认证';
  }
});

const identityStatusClass = computed(() => {
  switch(verificationStatus.identityStatus) {
    case 'pending': return 'status-pending';
    case 'approved': return 'status-approved';
    case 'rejected': return 'status-rejected';
    default: return 'status-default';
  }
});

const educationStatusClass = computed(() => {
  switch(verificationStatus.educationStatus) {
    case 'pending': return 'status-pending';
    case 'approved': return 'status-approved';
    case 'rejected': return 'status-rejected';
    default: return 'status-default';
  }
});

// 获取认证状态
const getVerificationStatus = async () => {
  try {
    const { data } = await getUserVerificationStatus(userId.value);
    Object.assign(verificationStatus, data);
    
    // 更新用户存储中的认证状态
    userStore.$patch({
      verifications: {
        identityStatus: data.identityStatus || 'not_submitted',
        educationStatus: data.educationStatus || 'not_submitted'
      }
    });
    
    // 如果被拒绝，获取拒绝原因
    if (verificationStatus.identityStatus === 'rejected') {
      await getIdentityRejection();
    }
    
    if (verificationStatus.educationStatus === 'rejected') {
      await getEducationRejection();
    }
  } catch (error) {
    console.error('获取认证状态失败', error);
  }
};

// 获取身份认证拒绝原因
const getIdentityRejection = async () => {
  try {
    const { data } = await getUserIdentityVerifications(userId.value);
    if (data && data.length > 0) {
      // 按创建时间降序排序，取最新的一条
      const latestRejection = data.filter(item => item.status === 'rejected')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))[0];
      
      if (latestRejection) {
        identityRejection.value = latestRejection.rejectReason || '未提供拒绝原因';
      }
    }
  } catch (error) {
    console.error('获取身份认证拒绝原因失败', error);
  }
};

// 获取教育认证拒绝原因
const getEducationRejection = async () => {
  try {
    const { data } = await getUserEducationVerifications(userId.value);
    if (data && data.length > 0) {
      // 按创建时间降序排序，取最新的一条
      const latestRejection = data.filter(item => item.status === 'rejected')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))[0];
      
      if (latestRejection) {
        educationRejection.value = latestRejection.rejectReason || '未提供拒绝原因';
      }
    }
  } catch (error) {
    console.error('获取教育认证拒绝原因失败', error);
  }
};

// 文件上传处理
const handleIdFrontUpload = async (event) => {
  const file = event.target.files[0];
  if (file) {
    try {
      const url = await uploadImage(file);
      identityForm.idCardFront = url;
    } catch (error) {
      console.error('上传身份证正面照片失败', error);
    }
  }
};

const handleIdBackUpload = async (event) => {
  const file = event.target.files[0];
  if (file) {
    try {
      const url = await uploadImage(file);
      identityForm.idCardBack = url;
    } catch (error) {
      console.error('上传身份证背面照片失败', error);
    }
  }
};

const handleStudentFrontUpload = async (event) => {
  const file = event.target.files[0];
  if (file) {
    try {
      const url = await uploadImage(file);
      educationForm.studentCardFront = url;
    } catch (error) {
      console.error('上传学生证封面照片失败', error);
    }
  }
};

const handleStudentInsideUpload = async (event) => {
  const file = event.target.files[0];
  if (file) {
    try {
      const url = await uploadImage(file);
      educationForm.studentCardInside = url;
    } catch (error) {
      console.error('上传学生证内页照片失败', error);
    }
  }
};

// 提交身份认证
const submitIdentity = async () => {
  if (!identityForm.idCardFront || !identityForm.idCardBack) {
    alert('请上传身份证正反面照片');
    return;
  }
  
  identitySubmitting.value = true;
  try {
    await submitIdentityVerification(userId.value, identityForm);
    showIdentityForm.value = false;
    alert('身份认证申请已提交，请等待审核');
    await getVerificationStatus(); // 刷新状态
  } catch (error) {
    console.error('提交身份认证失败', error);
    alert('提交失败：' + (error.message || '未知错误'));
  } finally {
    identitySubmitting.value = false;
  }
};

// 提交教育认证
const submitEducation = async () => {
  if (!educationForm.studentCardFront || !educationForm.studentCardInside) {
    alert('请上传学生证照片');
    return;
  }
  
  educationSubmitting.value = true;
  try {
    await submitEducationVerification(userId.value, educationForm);
    showEducationForm.value = false;
    alert('教育认证申请已提交，请等待审核');
    await getVerificationStatus(); // 刷新状态
  } catch (error) {
    console.error('提交教育认证失败', error);
    alert('提交失败：' + (error.message || '未知错误'));
  } finally {
    educationSubmitting.value = false;
  }
};

const goBack = () => {
  router.go(-1);
};

onMounted(() => {
  getVerificationStatus();
});
</script>

<style scoped>
.verification-center {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  position: relative;
  text-align: center;
  padding: 20px 15px;
  background-color: #fff;
  margin-bottom: 20px;
}

.back-icon-button {
  position: absolute;
  top: 20px;
  left: 15px;
  background: none;
  border: none;
  font-size: 20px;
  color: #333;
  cursor: pointer;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f5f5;
  transition: background-color 0.3s;
}

.back-icon-button:hover {
  background-color: #e0e0e0;
}

.back-icon-button i {
  font-size: 22px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
}

.header-desc {
  color: #666;
}

.verification-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.verification-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.card-title i {
  margin-right: 10px;
  font-size: 24px;
}

.card-status {
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 14px;
}

.status-default {
  background-color: #f0f0f0;
  color: #666;
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

.card-content {
  flex-grow: 1;
  margin-bottom: 20px;
}

.verified-info, .reject-reason {
  margin-top: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.info-item {
  margin-bottom: 5px;
}

.info-item label {
  font-weight: 500;
}

.reject-reason {
  color: #ff4d4f;
}

.card-footer {
  display: flex;
  justify-content: center;
}

button {
  padding: 8px 20px;
  border-radius: 5px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background-color: #40a9ff;
}

.btn-disabled {
  background-color: #f5f5f5;
  color: #d9d9d9;
  cursor: not-allowed;
}

.verified-tag {
  display: flex;
  align-items: center;
  color: #52c41a;
  font-weight: 500;
}

.verified-tag i {
  margin-right: 5px;
}

/* 表单弹窗样式 */
.verification-form-modal {
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
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.upload-box {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  position: relative;
}

.upload-box input[type="file"] {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  opacity: 0;
  cursor: pointer;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #1890ff;
}

.upload-btn i {
  font-size: 24px;
  margin-bottom: 8px;
}

.preview {
  text-align: center;
}

.preview img {
  max-width: 100%;
  max-height: 200px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #666;
  margin-right: 10px;
}

.btn-submit {
  background-color: #1890ff;
  color: white;
}

.btn-submit:disabled {
  background-color: #d9d9d9;
  cursor: not-allowed;
}
</style> 