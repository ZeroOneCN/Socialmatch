<template>
  <div class="report-dialog" v-if="visible">
    <div class="report-dialog-mask" @click="handleClose"></div>
    <div class="report-dialog-content">
      <div class="report-dialog-header">
        <h3>举报{{ typeText }}</h3>
        <span class="close-btn" @click="handleClose">×</span>
      </div>
      
      <div class="report-dialog-body">
        <div class="report-reason-title">请选择举报原因：</div>
        <div class="report-reason-list">
          <div 
            v-for="item in reasonList" 
            :key="item.id" 
            class="report-reason-item"
            :class="{ 'selected': selectedReason === item.reason }"
            @click="selectedReason = item.reason; selectedReasonId = item.id"
          >
            {{ item.reason }}
          </div>
        </div>
        
        <div class="report-description">
          <div class="report-description-title">补充说明（选填）：</div>
          <textarea 
            v-model="description" 
            placeholder="请补充详细说明，有助于我们更快处理" 
            maxlength="200"
            rows="3"
          ></textarea>
          <div class="text-count">{{ description.length }}/200</div>
        </div>
        
        <div class="report-image-upload">
          <div class="report-image-title">上传图片凭证（选填，最多3张）：</div>
          <div class="image-upload-area">
            <div 
              v-for="(img, index) in uploadedImages" 
              :key="index"
              class="image-preview-item"
            >
              <img :src="img" alt="证据图片" />
              <span class="delete-icon" @click="removeImage(index)">×</span>
            </div>
            
            <div 
              v-if="uploadedImages.length < 3"
              class="image-upload-btn"
              @click="triggerImageUpload"
            >
              <i class="upload-icon">+</i>
              <input 
                type="file" 
                ref="imageInput"
                accept="image/*"
                style="display: none"
                @change="handleImageChange"
              />
            </div>
          </div>
        </div>
      </div>
      
      <div class="report-dialog-footer">
        <button class="cancel-btn" @click="handleClose">取消</button>
        <button 
          class="submit-btn" 
          :disabled="!selectedReason || isSubmitting"
          @click="submitReport"
        >
          {{ isSubmitting ? '提交中...' : '提交' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { getReportReasons, submitReport as apiSubmitReport } from '../api/report';
import { showToast } from '../utils/toast';
import { useUserStore } from '../stores/user';

export default {
  name: 'ReportDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    type: {
      type: Number,
      required: true,
      validator: (value) => [1, 2, 3].includes(value) // 1-动态，2-评论，3-用户
    },
    targetId: {
      type: [Number, String],
      required: true
    }
  },
  emits: ['close', 'success'],
  setup(props, { emit }) {
    const userStore = useUserStore();
    const reasonList = ref([]);
    const selectedReason = ref('');
    const selectedReasonId = ref(null);
    const description = ref('');
    const uploadedImages = ref([]);
    const isSubmitting = ref(false);
    const imageInput = ref(null);
    
    const typeText = computed(() => {
      switch (props.type) {
        case 1: return '动态';
        case 2: return '评论';
        case 3: return '用户';
        default: return '';
      }
    });
    
    // 获取举报原因列表
    const fetchReasonList = async () => {
      try {
        const { data } = await getReportReasons(props.type);
        if (data && data.length > 0) {
          reasonList.value = data;
        } else {
          // 如果API没有返回数据，使用默认原因列表
          setDefaultReasons();
        }
      } catch (error) {
        console.error('获取举报原因失败', error);
        showToast('加载举报原因失败，已使用默认选项');
        // API调用失败时使用默认原因列表
        setDefaultReasons();
      }
    };
    
    // 设置默认原因列表
    const setDefaultReasons = () => {
      const defaultReasons = {
        1: [ // 动态举报原因
          { id: 101, reason: '色情低俗' },
          { id: 102, reason: '政治敏感' },
          { id: 103, reason: '商业广告' },
          { id: 104, reason: '人身攻击' },
          { id: 105, reason: '违法违规' },
          { id: 106, reason: '虚假信息' },
          { id: 107, reason: '其他' }
        ],
        2: [ // 评论举报原因
          { id: 201, reason: '色情低俗' },
          { id: 202, reason: '政治敏感' },
          { id: 203, reason: '商业广告' },
          { id: 204, reason: '人身攻击' },
          { id: 205, reason: '违法违规' },
          { id: 206, reason: '其他' }
        ],
        3: [ // 用户举报原因
          { id: 301, reason: '冒充他人' },
          { id: 302, reason: '发布不良信息' },
          { id: 303, reason: '诈骗行为' },
          { id: 304, reason: '骚扰他人' },
          { id: 305, reason: '违法违规' },
          { id: 306, reason: '其他' }
        ]
      };
      
      reasonList.value = defaultReasons[props.type] || [{ id: 999, reason: '其他' }];
    };
    
    // 当type变化时重新获取原因列表
    watch(() => props.type, fetchReasonList);
    
    // 组件挂载时获取原因列表
    onMounted(fetchReasonList);
    
    // 关闭对话框
    const handleClose = () => {
      emit('close');
    };
    
    // 触发图片上传
    const triggerImageUpload = () => {
      imageInput.value.click();
    };
    
    // 处理图片上传
    const handleImageChange = (event) => {
      const file = event.target.files[0];
      if (!file) return;
      
      if (!file.type.includes('image')) {
        showToast('请上传图片文件');
        return;
      }
      
      if (file.size > 5 * 1024 * 1024) {
        showToast('图片大小不能超过5MB');
        return;
      }
      
      const reader = new FileReader();
      reader.onload = (e) => {
        uploadedImages.value.push(e.target.result);
      };
      reader.readAsDataURL(file);
      
      // 清空input，允许重复选择同一文件
      event.target.value = '';
    };
    
    // 移除图片
    const removeImage = (index) => {
      uploadedImages.value.splice(index, 1);
    };
    
    // 提交举报
    const submitReport = async () => {
      if (!selectedReason.value) {
        showToast('请选择举报原因');
        return;
      }
      
      try {
        isSubmitting.value = true;
        
        // 从用户store获取用户ID
        const userId = userStore.userId || 0;
        
        // 确保数据结构与后端一致
        const reportData = {
          reportType: props.type,
          targetId: props.targetId,
          reporterId: userId,
          reason: selectedReason.value,
          content: description.value,
          // 图片处理，确保使用正确的格式
          images: uploadedImages.value.length > 0 ? uploadedImages.value : null
        };
        
        console.log('提交举报数据:', reportData);
        
        await apiSubmitReport(reportData);
        
        showToast('举报成功，我们会尽快处理');
        emit('success');
        
        // 重置表单
        selectedReason.value = '';
        selectedReasonId.value = null;
        description.value = '';
        uploadedImages.value = [];
        
      } catch (error) {
        console.error('提交举报失败', error);
        showToast('提交举报失败，请重试');
      } finally {
        isSubmitting.value = false;
      }
    };
    
    return {
      reasonList,
      selectedReason,
      selectedReasonId,
      description,
      uploadedImages,
      isSubmitting,
      imageInput,
      typeText,
      handleClose,
      triggerImageUpload,
      handleImageChange,
      removeImage,
      submitReport
    };
  }
}
</script>

<style scoped>
.report-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.report-dialog-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.report-dialog-content {
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.report-dialog-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  font-size: 22px;
  color: #999;
  cursor: pointer;
}

.report-dialog-body {
  padding: 15px;
  overflow-y: auto;
  flex: 1;
}

.report-reason-title,
.report-description-title,
.report-image-title {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
}

.report-reason-list {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.report-reason-item {
  padding: 8px 12px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-right: 10px;
  margin-bottom: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.report-reason-item.selected {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.report-description {
  margin-bottom: 15px;
}

textarea {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 10px;
  font-size: 14px;
  resize: none;
}

.text-count {
  text-align: right;
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
}

.image-preview-item,
.image-upload-btn {
  width: 80px;
  height: 80px;
  margin-right: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-icon {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.image-upload-btn {
  border: 1px dashed #ddd;
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-icon {
  font-size: 24px;
  color: #999;
}

.report-dialog-footer {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}

button {
  padding: 8px 15px;
  border-radius: 4px;
  border: none;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  margin-right: 10px;
}

.submit-btn {
  background-color: #007bff;
  color: white;
}

.submit-btn:disabled {
  background-color: #b0d4ff;
  cursor: not-allowed;
}
</style> 