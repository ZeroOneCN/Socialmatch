<template>
  <div class="frequency-prefs-page">
    <van-nav-bar
      title="同频偏好设置"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="content-container">
      <div class="page-description">
        设置您的同频匹配偏好，帮助系统找到与您更匹配的人
      </div>
      
      <div class="form-container">
        <van-cell-group inset title="兴趣偏好">
          <div class="hobby-tags-container">
            <div class="hobby-tags-header">
              <span>兴趣爱好</span>
              <span class="tag-hint">(选择3-5个)</span>
            </div>
            <div class="hobby-tags">
              <div 
                v-for="(tag, index) in allHobbyTags" 
                :key="index"
                :class="['hobby-tag', {'hobby-tag-selected': selectedHobbies.includes(tag)}]"
                @click="toggleHobby(tag)"
              >
                {{ tag }}
              </div>
            </div>
          </div>
        </van-cell-group>
        
        <van-cell-group inset title="位置设置">
          <van-cell 
            title="位置选择" 
            is-link 
            @click="showCityPicker = true"
            :value="preferences.city || '选择您感兴趣的城市'"
          />
          
          <van-switch-cell
            v-model="preferences.nearbyOnly"
            title="仅查看附近"
            size="24px"
            active-color="#07c160"
          />
          
          <van-field
            v-if="preferences.nearbyOnly"
            v-model="preferences.maxDistance"
            label="最大距离"
            type="number"
            suffix="公里"
            placeholder="输入最大匹配距离"
          />
        </van-cell-group>
        
        <van-cell-group inset title="年龄范围">
          <van-cell title="意向年龄">
            <template #value>
              <div>{{ preferences.ageRange[0] }} - {{ preferences.ageRange[1] }} 岁</div>
            </template>
          </van-cell>
          <div class="slider-container">
            <van-slider
              v-model="preferences.ageRange"
              range
              :min="18"
              :max="60"
              :step="1"
              bar-height="4px"
              active-color="#1989fa"
            >
              <template #button>
                <div class="slider-button">{{ preferences.ageRange[0] }}</div>
              </template>
              <template #right-button>
                <div class="slider-button">{{ preferences.ageRange[1] }}</div>
              </template>
            </van-slider>
          </div>
        </van-cell-group>
        
        <van-cell-group inset title="教育与职业">
          <van-cell
            title="学历要求"
            is-link
            @click="showEducationPicker = true"
            :value="educationOptions.find(opt => opt.value === preferences.education)?.text || '选择学历要求'"
          />
          
          <van-field
            v-model="preferences.occupation"
            label="职业意向"
            placeholder="填写您感兴趣的职业"
            maxlength="50"
            show-word-limit
          />
        </van-cell-group>
        
        <van-cell-group inset title="其他设置">
          <van-switch-cell
            v-model="preferences.verifiedOnly"
            title="仅显示已认证用户"
            size="24px"
            active-color="#07c160"
          />
        </van-cell-group>
      </div>
      
      <div class="button-container">
        <van-button type="primary" block round @click="savePreferences">
          保存设置
        </van-button>
      </div>
    </div>
    
    <!-- 城市选择器 -->
    <van-popup v-model:show="showCityPicker" position="bottom" round>
      <van-area
        title="选择城市"
        :area-list="areaList"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
        :value="preferences.cityCode"
      />
    </van-popup>
    
    <!-- 学历选择器 -->
    <van-popup v-model:show="showEducationPicker" position="bottom" round>
      <van-picker
        title="选择学历要求"
        :columns="educationOptions"
        @confirm="onEducationConfirm"
        @cancel="showEducationPicker = false"
        show-toolbar
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { areaList } from '@vant/area-data'
import * as userApi from '../../api/user'
import * as commonApi from '../../api/common'

const router = useRouter()
const errors = ref({})

// 偏好设置数据
const preferences = ref({
  interests: '',
  city: '',
  cityCode: '',
  provinceCode: '',
  nearbyOnly: false,
  maxDistance: 10,
  ageRange: [20, 40],
  education: 0,
  occupation: '',
  occupationCode: '',
  verifiedOnly: false
})

// 显示选择器
const showCityPicker = ref(false)
const showEducationPicker = ref(false)

// 学历选项（与数据库定义对应）
const educationOptions = [
  { text: '不限', value: 0 },
  { text: '高中及以上', value: 1 },
  { text: '大专及以上', value: 2 },
  { text: '本科及以上', value: 3 },
  { text: '硕士及以上', value: 4 },
  { text: '博士及以上', value: 5 }
]

// 兴趣爱好相关
const selectedHobbies = ref([])
const allHobbyTags = ref([
  '电影', '旅行', '健身', '音乐', '阅读', '摄影', '设计', '游戏', '美食', '跑步',
  '瑜伽', '舞蹈', '绘画', '写作', '编程', '烹饪', '科技', '动漫', '宠物', '咖啡'
])

// 切换兴趣爱好选中状态
const toggleHobby = (tag) => {
  const index = selectedHobbies.value.indexOf(tag)
  if (index > -1) {
    // 取消选择
    selectedHobbies.value.splice(index, 1)
  } else {
    // 新增选择(最多5个)
    if (selectedHobbies.value.length >= 5) {
      showToast('最多选择5个兴趣爱好')
      return
    }
    selectedHobbies.value.push(tag)
  }
}

// 获取当前用户的偏好设置
const fetchPreferences = async () => {
  try {
    const response = await userApi.getFrequencyPreferences()
    if (response.data) {
      // 合并服务器返回的数据到本地状态
      preferences.value = { 
        ...preferences.value, 
        ...response.data,
        // 确保年龄范围是数组格式
        ageRange: [
          response.data.minAge || 20,
          response.data.maxAge || 40
        ]
      }
      // 处理兴趣爱好
      if (response.data.interests) {
        selectedHobbies.value = response.data.interests.split(',').map(h => h.trim()).filter(h => h)
      }
    }
  } catch (error) {
    console.error('获取偏好设置失败', error)
    showToast('获取偏好设置失败')
  }
}

// 保存偏好设置
const savePreferences = async () => {
  // 表单验证
  errors.value = {}
  
  if (selectedHobbies.value.length < 3) {
    showToast('请至少选择3个兴趣爱好')
    return
  }
  
  try {
    const preferencesData = {
      ...preferences.value,
      interests: selectedHobbies.value.join(','),
      minAge: preferences.value.ageRange[0],
      maxAge: preferences.value.ageRange[1]
    }
    await userApi.updateFrequencyPreferences(preferencesData)
    showSuccessToast('设置保存成功')
    setTimeout(() => {
      goBack()
    }, 1000)
  } catch (error) {
    console.error('保存偏好设置失败', error)
    showFailToast('保存失败，请稍后再试')
  }
}

// 城市选择确认
const onCityConfirm = (values) => {
  const [province, city] = values
  preferences.value.city = city?.name || ''
  preferences.value.cityCode = city?.code || ''
  preferences.value.provinceCode = province?.code || ''
  showCityPicker.value = false
}

// 学历选择确认
const onEducationConfirm = (value) => {
  preferences.value.education = value.value
  showEducationPicker.value = false
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  fetchPreferences()
})
</script>

<style scoped>
.frequency-prefs-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 30px;
}

.content-container {
  padding: 20px;
}

.page-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.form-container {
  margin-bottom: 24px;
}

.button-container {
  margin-top: 30px;
  padding: 0 20px;
}

.slider-container {
  padding: 20px 16px;
}

.slider-button {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background-color: #1989fa;
  color: white;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 兴趣爱好标签样式 */
.hobby-tags-container {
  padding: 16px;
}

.hobby-tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tag-hint {
  font-size: 12px;
  color: #999;
}

.hobby-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hobby-tag {
  padding: 8px 16px;
  border-radius: 20px;
  background-color: #f5f5f5;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.hobby-tag-selected {
  background-color: #e6f7ff;
  color: #1989fa;
  border: 1px solid #91d5ff;
}
</style> 