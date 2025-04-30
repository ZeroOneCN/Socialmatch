<template>
  <div class="page-container">
    <div class="header">
      <van-nav-bar
        :title="isShareMode ? '转发动态' : '发布动态'"
        left-text="取消"
        right-text="发布"
        @click-left="goBack"
        @click-right="submitPost"
        :right-disabled="!isValid"
      />
    </div>
    
    <div class="content-container">
      <van-form @submit="submitPost">
        <van-field
          v-model="content"
          rows="5"
          autosize
          type="textarea"
          maxlength="500"
          :placeholder="isShareMode ? '说说你的想法...' : '分享你的想法...'"
          show-word-limit
        />
        
        <!-- 分享模式下显示原始帖子提示 -->
        <div class="share-info" v-if="isShareMode">
          <p class="share-tip">正在转发 @{{ originalUserName }} 的动态</p>
        </div>
        
        <h3 class="section-title" v-if="!isShareMode">添加图片</h3>
        
        <!-- 使用原生file input实现图片上传 -->
        <div class="image-uploader" v-if="!isShareMode">
          <!-- 已选择的图片预览 -->
          <div class="image-preview" v-if="imagePreviewUrls.length > 0">
            <div 
              v-for="(url, index) in imagePreviewUrls" 
              :key="index" 
              class="preview-item"
            >
              <img :src="url" class="preview-image" />
              <div class="preview-delete" @click="deleteImage(index)">
                <van-icon name="cross" />
              </div>
            </div>
            
            <!-- 添加更多图片按钮 -->
            <div 
              v-if="imagePreviewUrls.length < 9" 
              class="add-more-btn"
              @click="triggerFileInput"
            >
              <van-icon name="plus" />
            </div>
          </div>
          
          <!-- 未选择图片时显示上传按钮 -->
          <div 
            v-else 
            class="uploader-slot"
            @click="triggerFileInput"
          >
            <van-icon name="photograph" size="24" />
            <span class="uploader-text">点击添加图片</span>
          </div>
          
          <!-- 隐藏的文件输入框 -->
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            multiple
            class="file-input"
            @change="onFileChange"
          />
        </div>
        
        <div class="location-section" v-if="cityName">
          <van-icon name="location-o" />
          <span class="location-text">{{ cityName }}</span>
          <van-icon name="cross" @click="removeLocation" />
        </div>
        
        <div class="actions">
          <div class="action-item" @click="getCurrentLocation">
            <van-icon name="aim" />
            <span>获取当前位置</span>
          </div>
        </div>
      </van-form>
    </div>
    
    <van-toast id="custom-toast" />
    <van-action-sheet
      v-model:show="showLocationSheet"
      title="选择位置"
      :actions="locationActions"
      cancel-text="取消"
      close-on-click-action
      @select="onLocationSelected"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '../../stores/user'
import * as postApi from '../../api/post'
import http from '../../api/http'
import { getCurrentLocationInfo } from '../../utils/mapUtils'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const content = ref('')
const cityName = ref('')
const fileInput = ref(null)
const selectedFiles = ref([])
const imagePreviewUrls = ref([])

// 分享相关
const isShareMode = ref(false)
const originalPostId = ref(null)
const originalUserName = ref('')

// UI控制
const showLocationSheet = ref(false)
const isSubmitting = ref(false)

// 表单验证
const isValid = computed(() => {
  return content.value.trim().length > 0
})

// 位置选项
const locationActions = [
  { name: '北京', value: '北京' },
  { name: '上海', value: '上海' },
  { name: '广州', value: '广州' },
  { name: '深圳', value: '深圳' },
  { name: '杭州', value: '杭州' },
  { name: '成都', value: '成都' },
  { name: '西安', value: '西安' },
  { name: '武汉', value: '武汉' },
  { name: '南京', value: '南京' },
  { name: '重庆', value: '重庆' }
]

// 返回上一页
const goBack = () => {
  if (content.value || selectedFiles.value.length > 0) {
    showDialog({
      title: '放弃编辑',
      message: '内容尚未发布，是否放弃编辑？',
      showCancelButton: true
    }).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

// 触发文件输入框点击
const triggerFileInput = () => {
  // 手动触发文件输入框点击
  fileInput.value.click()
  console.log('触发文件选择')
}

// 文件选择变化处理
const onFileChange = (event) => {
  const files = event.target.files
  console.log('选择的文件:', files)
  
  if (!files || files.length === 0) return
  
  // 检查文件数量限制
  if (selectedFiles.value.length + files.length > 9) {
    showToast('最多只能上传9张图片')
    return
  }
  
  // 处理选择的文件
  Array.from(files).forEach(file => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      showToast('只能上传图片文件')
      return
    }
    
    // 验证文件大小
    if (file.size > 5 * 1024 * 1024) {
      showToast('图片大小不能超过5MB')
      return
    }
    
    // 添加到已选择的文件列表
    selectedFiles.value.push(file)
    
    // 创建预览URL
    const previewUrl = URL.createObjectURL(file)
    imagePreviewUrls.value.push(previewUrl)
  })
  
  // 重置文件输入框，以便可以再次选择相同的文件
  event.target.value = ''
}

// 删除选择的图片
const deleteImage = (index) => {
  showDialog({
    title: '提示',
    message: '是否删除这张图片？',
    showCancelButton: true
  }).then(() => {
    // 释放预览URL资源
    URL.revokeObjectURL(imagePreviewUrls.value[index])
    
    // 从数组中移除
    imagePreviewUrls.value.splice(index, 1)
    selectedFiles.value.splice(index, 1)
  }).catch(() => {})
}

// 获取当前位置
const getCurrentLocation = async () => {
  showToast({
    type: 'loading',
    message: '获取位置中...',
    forbidClick: true,
    duration: 0
  })
  
  try {
    const locationInfo = await getCurrentLocationInfo()
    cityName.value = locationInfo.city
    
    showToast({
      type: 'success',
      message: `已获取位置: ${locationInfo.city}`
    })
  } catch (error) {
    console.error('获取位置失败', error)
    showToast({
      type: 'fail',
      message: '获取位置失败，请手动选择'
    })
  }
}

// 位置选择回调
const onLocationSelected = (action) => {
  cityName.value = action.value
}

// 移除位置
const removeLocation = () => {
  cityName.value = ''
}

// 提交动态
const submitPost = async () => {
  if (!isValid.value || isSubmitting.value) return
  
  isSubmitting.value = true
  showToast({
    type: 'loading',
    message: isShareMode.value ? '转发中...' : '发布中...',
    forbidClick: true,
    duration: 0
  })
  
  try {
    let response;
    
    if (isShareMode.value) {
      // 分享模式，使用分享API
      const shareData = {
        originalPostId: originalPostId.value,
        content: content.value
      };
      
      console.log('转发动态:', shareData);
      response = await postApi.sharePost(shareData);
    } else {
      // 普通发布模式
      // 准备表单数据
      const formData = new FormData()
      formData.append('content', content.value)
      
      // 处理图片
      if (selectedFiles.value.length > 0) {
        console.log(`准备上传${selectedFiles.value.length}张图片`)
        
        selectedFiles.value.forEach((file, index) => {
          formData.append('images', file)
          console.log(`添加图片${index}:`, file.name, file.type, file.size)
        })
      }
      
      // 添加城市信息
      if (cityName.value) {
        formData.append('city', cityName.value)
      }
      
      console.log('发送动态数据:', { 
        content: content.value, 
        imagesCount: selectedFiles.value.length,
        city: cityName.value
      })
      
      // 使用API函数
      response = await postApi.createPost({
        content: content.value,
        images: selectedFiles.value,
        city: cityName.value
      });
    }
    
    console.log('操作成功，响应:', response)
    
    showToast({
      type: 'success',
      message: isShareMode.value ? '转发成功' : '发布成功',
      duration: 1500,
      onClose: () => {
        router.replace('/community')
      }
    })
  } catch (error) {
    console.error(isShareMode.value ? '转发失败' : '发布失败', error)
    showToast({
      type: 'fail',
      message: (isShareMode.value ? '转发' : '发布') + '失败，请重试: ' + (error.message || '未知错误')
    })
  } finally {
    isSubmitting.value = false
  }
}

// 在组件挂载时设置文件上传的相关处理
onMounted(() => {
  // 检查是否是分享模式
  if (route.query.share === 'true') {
    isShareMode.value = true
    originalPostId.value = route.query.postId
    originalUserName.value = route.query.originalUserName || '用户'
    
    // 如果有预填内容，设置为"转发: "加上内容前缀
    if (route.query.content) {
      content.value = `//@${originalUserName.value}: ${route.query.content}`
    }
    
    console.log('初始化分享模式:', { 
      originalPostId: originalPostId.value,
      originalUserName: originalUserName.value
    })
  }

  // 确保图片上传控件正确初始化
  console.log('发布动态页面已挂载，图片上传控件准备就绪')
  
  // 测试上传组件是否可用
  const fileInput = document.querySelector('.file-input')
  if (fileInput) {
    console.log('文件上传输入框已找到')
  } else {
    console.warn('未找到文件上传输入框，上传组件可能未正确渲染')
  }
})
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 999;
  background-color: #fff;
}

.content-container {
  padding: 16px;
}

.section-title {
  margin-top: 16px;
  margin-bottom: 8px;
  font-size: 15px;
  color: #323233;
  font-weight: normal;
}

.image-uploader {
  margin-bottom: 16px;
}

/* 隐藏文件输入框 */
.file-input {
  display: none;
}

.uploader-slot {
  width: 90px;
  height: 90px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f7f8fa;
  border-radius: 4px;
  cursor: pointer;
  border: 1px dashed #dcdee0;
}

.uploader-slot:hover {
  background-color: #f2f3f5;
}

.uploader-text {
  margin-top: 8px;
  font-size: 12px;
  color: #969799;
}

/* 图片预览区域 */
.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-item {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-delete {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 0 0 0 4px;
  cursor: pointer;
}

.add-more-btn {
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f7f8fa;
  border-radius: 4px;
  cursor: pointer;
  border: 1px dashed #dcdee0;
}

.add-more-btn:hover {
  background-color: #f2f3f5;
}

.actions {
  display: flex;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f2f2f2;
}

.action-item {
  display: flex;
  align-items: center;
  margin-right: 16px;
  color: #646566;
  cursor: pointer;
}

.action-item .van-icon {
  margin-right: 4px;
  font-size: 18px;
}

.location-section {
  display: flex;
  align-items: center;
  margin-top: 16px;
  padding: 8px 12px;
  background-color: #f7f8fa;
  border-radius: 4px;
  color: #323233;
}

.location-text {
  flex: 1;
  margin: 0 8px;
}

.share-info {
  margin: 12px 0;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 8px;
  border-left: 3px solid #1989fa;
}

.share-tip {
  color: #1989fa;
  font-size: 14px;
  margin: 0;
}
</style> 