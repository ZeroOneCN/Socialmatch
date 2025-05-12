<template>
  <div class="profile-settings-page">
    <van-nav-bar
      title="个人资料"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
      class="custom-nav"
    />
    
    <div class="content-container">
      <!-- 头像上传卡片 -->
      <div class="profile-card avatar-card">
        <div class="card-content">
          <div class="avatar-upload">
            <input
              type="file"
              id="avatar-input"
              accept="image/*"
              @change="handleAvatarChange"
              class="hidden-input"
            />
            <label for="avatar-input" class="avatar-label">
              <div class="avatar-wrapper">
                <img :src="userAvatar" class="avatar-preview" alt="头像" />
                <div class="avatar-edit-badge">
                  <van-icon name="photograph" size="16" />
                </div>
              </div>
            </label>
          </div>
        </div>
      </div>

      <!-- 基本信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="user-o" class="card-icon" />
          <span>基本信息</span>
        </div>
        <div class="card-content">
          <van-form>
            <van-field
              v-model="formData.nickname"
              name="nickname"
              label="昵称"
              placeholder="请输入昵称"
              :rules="[{ required: true, message: '请填写昵称' }]"
              class="custom-field"
            />
            
            <van-field
              name="gender"
              label="性别"
              readonly
              class="custom-field"
            >
              <template #input>
                <div v-if="formData.gender > 0" class="gender-display">
                  <div class="gender-selected-display">
                    {{ genderOptions.find(option => option.value === formData.gender)?.text }}
                  </div>
                </div>
                <div v-else class="gender-radio-group">
                  <div 
                    v-for="option in genderOptions.filter(o => o.value === 1 || o.value === 2)" 
                    :key="option.value"
                    :class="['gender-option', {'gender-selected': formData.gender === option.value}]"
                    @click="confirmGenderSelection(option.value)"
                  >
                    <van-icon v-if="option.value === 1" name="manager" class="gender-icon" />
                    <van-icon v-if="option.value === 2" name="user" class="gender-icon" />
                    {{ option.text }}
                  </div>
                </div>
              </template>
            </van-field>
            
            <van-field
              name="birthday"
              label="年龄"
              readonly
              class="custom-field"
            >
              <template #input>
                <div class="age-selector">
                  <div class="age-input-container">
                    <input
                      type="number"
                      v-model="ageInput"
                      class="age-input"
                      min="18"
                      max="80"
                      @input="onAgeChange"
                      placeholder="请输入"
                    />
                    <span class="age-unit">岁</span>
                  </div>
                </div>
              </template>
            </van-field>
          </van-form>
        </div>
      </div>

      <!-- 位置信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="location-o" class="card-icon" />
          <span>位置信息</span>
        </div>
        <div class="card-content">
          <van-form>
            <van-field
              name="location"
              label="所在地"
              readonly
              class="custom-field"
            >
              <template #input>
                <div class="location-display">
                  <div v-if="formData.city" class="location-selected">
                    <van-icon name="location-o" class="location-icon" />
                    <span>{{ formData.city }}</span>
                  </div>
                  <div v-else class="location-placeholder" @click="getCurrentLocation">
                    <span>点击获取当前位置</span>
                    <van-icon name="location-o" class="location-icon" />
                  </div>
                </div>
              </template>
              <template #extra>
                <van-button size="small" type="primary" plain @click="getCurrentLocation" :loading="isLocating">
                  {{ formData.city ? '更新位置' : '获取位置' }}
                </van-button>
              </template>
            </van-field>
          </van-form>
        </div>
      </div>

      <!-- 教育与职业卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="award-o" class="card-icon" />
          <span>教育与职业</span>
        </div>
        <div class="card-content">
          <van-form>
            <van-field
              name="education"
              label="学历"
              readonly
              class="custom-field"
            >
              <template #input>
                <div v-if="formData.education > 0" class="education-display">
                  <div class="education-selected-display">
                    {{ educationOptions.find(option => option.value === formData.education)?.text }}
                  </div>
                </div>
                <div v-else class="education-radio-group">
                  <div 
                    v-for="option in educationOptions.filter(o => o.value > 1)" 
                    :key="option.value"
                    :class="['education-option', {'education-selected': formData.education === option.value}]"
                    @click="setEducation(option.value)"
                  >
                    {{ option.text }}
                  </div>
                </div>
              </template>
              <template #extra>
                <van-button v-if="formData.education > 0" size="small" type="primary" plain @click="showEducationOptions">
                  修改
                </van-button>
              </template>
            </van-field>
            
            <van-field
              v-model="formData.occupation"
              name="occupation"
              label="职业"
              placeholder="请输入您的职业"
              class="custom-field"
              maxlength="15"
              show-word-limit
            />
          </van-form>
        </div>
      </div>

      <!-- 兴趣爱好卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="like-o" class="card-icon" />
          <span>兴趣爱好</span>
          <span class="tag-hint">(选择3-5个)</span>
        </div>
        <div class="card-content">
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
      </div>

      <!-- 自我介绍卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="description" class="card-icon" />
          <span>自我介绍</span>
        </div>
        <div class="card-content">
          <van-form>
            <van-field
              v-model="formData.selfIntro"
              rows="3"
              autosize
              name="selfIntro"
              type="textarea"
              maxlength="15"
              placeholder="简单介绍一下自己吧(15字以内)"
              show-word-limit
              class="custom-field intro-field"
            />
          </van-form>
        </div>
      </div>

      <!-- 认证状态卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <van-icon name="certificate" class="card-icon" />
          <span>认证状态</span>
        </div>
        <div class="card-content">
          <div class="verification-status-list">
            <div class="verification-status-item">
              <div class="verification-type">
                <van-icon name="idcard" class="verification-icon" />
                <span>身份认证</span>
              </div>
              <div class="verification-status-badges">
                <div class="verification-badge" :class="identityStatusClass">
                  {{ identityStatusText }}
                </div>
                <span class="verification-icon-badge identity" 
                  :class="{'badge-verified': userStore.verifications.identityStatus === 'approved', 'badge-unverified': userStore.verifications.identityStatus !== 'approved'}">
                  <van-icon name="certificate" />
                </span>
              </div>
            </div>
            
            <div class="verification-status-item">
              <div class="verification-type">
                <van-icon name="award" class="verification-icon" />
                <span>教育认证</span>
              </div>
              <div class="verification-status-badges">
                <div class="verification-badge" :class="educationStatusClass">
                  {{ educationStatusText }}
                </div>
                <span class="verification-icon-badge education" 
                  :class="{'badge-verified': userStore.verifications.educationStatus === 'approved', 'badge-unverified': userStore.verifications.educationStatus !== 'approved'}">
                  <van-icon name="award" />
                </span>
              </div>
            </div>
          </div>
          
          <div class="verification-action">
            <van-button 
              size="small" 
              type="primary" 
              plain 
              @click="goToVerificationCenter"
            >
              前往认证中心
            </van-button>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-button-wrapper">
        <van-button round block type="primary" native-type="submit" @click="onSubmit" class="submit-button">
          保存修改
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeMount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showLoadingToast, closeToast, showDialog } from 'vant'
import { useUserStore } from '../../stores/user'
import * as userApi from '../../api/user'
import * as commonApi from '../../api/common'
import { areaList } from '@vant/area-data'
import { getCurrentLocationInfo } from '../../utils/mapUtils'

const router = useRouter()
const userStore = useUserStore()

// 弹出选择器控制
const showGenderSelector = ref(false)
const showDatePicker = ref(false)
const showEducationPicker = ref(false)
const showCityPicker = ref(false)
const showOccupationPicker = ref(false)

// 基础选项数据
const genderOptions = [
  { text: '未设置', value: 0 },
  { text: '男', value: 1 },
  { text: '女', value: 2 },
  { text: '保密', value: 3 }
]
const educationOptions = [
  { text: '未设置', value: 0 },
  { text: '高中及以下', value: 1 },
  { text: '大专', value: 2 },
  { text: '本科', value: 3 },
  { text: '硕士', value: 4 },
  { text: '博士及以上', value: 5 }
]
const occupationColumns = ref([])

// 用户头像
const userAvatar = ref('')

// 表单数据
const formData = ref({
  nickname: '',
  gender: 0,
  birthday: '',
  location: '',
  city: '',
  cityCode: '',
  provinceCode: '',
  education: 0,
  occupation: '',
  selfIntro: ''
})

// 兴趣爱好
const selectedHobbies = ref([])
const allHobbyTags = ref([
  '电影', '旅行', '健身', '音乐', '阅读', '摄影', '设计', '游戏', '美食', '跑步',
  '瑜伽', '舞蹈', '绘画', '写作', '编程', '烹饪', '科技', '动漫', '宠物', '咖啡'
])

// 年龄输入
const ageInput = ref('');

// 在脚本顶部添加区域数据检查
const customAreaList = ref(null);

// 添加定位相关状态
const isLocating = ref(false)

// 验证状态
const identityStatusText = computed(() => {
  switch(userStore.verifications.identityStatus) {
    case 'approved': return '已认证';
    case 'pending': return '审核中';
    case 'rejected': return '已拒绝';
    default: return '未认证';
  }
});

const educationStatusText = computed(() => {
  switch(userStore.verifications.educationStatus) {
    case 'approved': return '已认证';
    case 'pending': return '审核中';
    case 'rejected': return '已拒绝';
    default: return '未认证';
  }
});

const identityStatusClass = computed(() => {
  switch(userStore.verifications.identityStatus) {
    case 'approved': return 'status-approved';
    case 'pending': return 'status-pending';
    case 'rejected': return 'status-rejected';
    default: return 'status-default';
  }
});

const educationStatusClass = computed(() => {
  switch(userStore.verifications.educationStatus) {
    case 'approved': return 'status-approved';
    case 'pending': return 'status-pending';
    case 'rejected': return 'status-rejected';
    default: return 'status-default';
  }
});

// 在onBeforeMount钩子中检查areaList
onBeforeMount(() => {
  console.log('区域数据检查:', areaList);
  
  // 检查areaList是否有效
  if (!areaList || !areaList.province_list || Object.keys(areaList.province_list).length === 0) {
    console.warn('官方区域数据无效，使用备用数据');
    
    // 创建简化的区域数据作为备选
    customAreaList.value = {
      province_list: {
        '110000': '北京市',
        '120000': '天津市',
        '130000': '河北省',
        '140000': '山西省',
        '150000': '内蒙古自治区',
        '310000': '上海市',
        '320000': '江苏省',
        '330000': '浙江省',
        '340000': '安徽省',
        '350000': '福建省',
        '440000': '广东省',
        '500000': '重庆市',
        '510000': '四川省',
        '610000': '陕西省'
      },
      city_list: {
        '110100': '北京市',
        '120100': '天津市',
        '130100': '石家庄市',
        '130200': '唐山市',
        '130300': '秦皇岛市',
        '310100': '上海市',
        '320100': '南京市',
        '320200': '无锡市',
        '320500': '苏州市',
        '330100': '杭州市',
        '330200': '宁波市',
        '340100': '合肥市',
        '350100': '福州市',
        '350200': '厦门市',
        '440100': '广州市',
        '440300': '深圳市',
        '500100': '重庆市',
        '510100': '成都市',
        '610100': '西安市'
      },
      county_list: {}
    };
  } else {
    console.log('使用官方区域数据');
    customAreaList.value = areaList;
  }
});

// 根据年龄生成生日
const onAgeChange = () => {
  if (!ageInput.value) return;
  
  let age = parseInt(ageInput.value, 10);
  console.log('输入的年龄:', age);
  
  // 限制年龄范围 18-80
  if (age < 18) {
    age = 18;
    ageInput.value = '18';
  } else if (age > 80) {
    age = 80;
    ageInput.value = '80';
  }
  
  // 根据年龄计算出生年
  const currentYear = new Date().getFullYear();
  const birthYear = currentYear - age;
  
  // 设置为该年的1月1日
  formData.value.birthday = `${birthYear}-01-01`;
  console.log('根据年龄设置的生日:', formData.value.birthday);
};

// 页面初始化
onMounted(async () => {
  try {
    // 设置默认头像
    userAvatar.value = userStore.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    
    // 先获取用户资料
    await fetchUserProfile()
    
    // 如果有生日信息，计算年龄
    if (formData.value.birthday) {
      try {
        const birthYear = parseInt(formData.value.birthday.split('-')[0], 10);
        const currentYear = new Date().getFullYear();
        if (!isNaN(birthYear) && birthYear > 1900) {
          ageInput.value = String(currentYear - birthYear);
          console.log('根据生日计算的年龄:', ageInput.value);
        }
      } catch (e) {
        console.error('计算年龄失败:', e);
      }
    }
    
    // 再获取职业数据
    await fetchOccupations()
    
    // 打印初始化后的状态
    console.log('页面初始化完成:', {
      formData: formData.value,
      selectedHobbies: selectedHobbies.value,
      occupationColumns: occupationColumns.value,
      ageInput: ageInput.value
    })
  } catch (error) {
    console.error('初始化个人资料页面失败:', error)
    showToast('页面初始化失败，请稍后重试')
  }
})

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    showLoadingToast({
      message: '加载中...',
      forbidClick: true,
      duration: 0
    })
    
    const response = await userApi.getCurrentUserProfile()
    closeToast()
    
    if (!response || response.code !== 200) {
      console.error('获取用户资料失败:', response)
      showToast('获取资料失败')
      return
    }
    
    const profile = response.data || {}
    console.log('获取到用户资料:', profile, '性别原始值:', profile.gender, '类型:', typeof profile.gender)
    
    // 设置表单数据，保证数值类型正确
    formData.value = {
      nickname: profile.nickname || userStore.nickname || userStore.username || '',
      gender: profile.gender !== undefined && profile.gender !== null ? parseInt(profile.gender, 10) : 0,
      birthday: profile.birthday || '',
      city: profile.city || '',
      cityCode: profile.cityCode || '',
      provinceCode: profile.provinceCode || '',
      education: profile.education !== undefined && profile.education !== null ? parseInt(profile.education, 10) : 0,
      occupation: profile.occupation || '',
      selfIntro: profile.selfIntro || ''
    }
    
    console.log('初始化后的表单数据:', formData.value, '性别值:', formData.value.gender, '类型:', typeof formData.value.gender)
    
    // 设置头像
    if (profile.avatar) {
      userAvatar.value = profile.avatar
    }
    
    // 设置兴趣爱好
    if (profile.hobbies) {
      try {
        const hobbiesArray = profile.hobbies.split(',').map(item => item.trim()).filter(Boolean)
        selectedHobbies.value = hobbiesArray
      } catch (error) {
        console.error('解析兴趣爱好失败:', error)
      }
    }
  } catch (error) {
    closeToast()
    console.error('获取用户资料失败', error)
    showToast('获取资料失败，请稍后重试')
  }
}

// 获取职业列表
const fetchOccupations = async () => {
  try {
    const response = await commonApi.getAllOccupations()
    console.log('获取职业列表响应:', response) // 添加日志
    
    if (response?.data && Array.isArray(response.data)) {
      // 构建职业选择器数据
      occupationColumns.value = response.data.map(item => ({
        text: item.name,
        value: item.code
      }))
      console.log('处理后的职业列表:', occupationColumns.value) // 添加日志
    } else {
      console.warn('使用默认职业数据') // 添加警告日志
      // 设置默认职业数据
      occupationColumns.value = [
        { text: '软件工程师', value: 'SE001' },
        { text: '产品经理', value: 'PM001' },
        { text: '市场营销', value: 'MK001' },
        { text: '财务会计', value: 'AC001' },
        { text: '人力资源', value: 'HR001' },
        { text: '教师', value: 'TC001' },
        { text: '医生', value: 'DR001' },
        { text: '学生', value: 'ST001' }
      ]
    }
  } catch (error) {
    console.error('获取职业数据失败:', error)
    // 设置默认职业数据
    occupationColumns.value = [
      { text: '软件工程师', value: 'SE001' },
      { text: '产品经理', value: 'PM001' },
      { text: '市场营销', value: 'MK001' },
      { text: '财务会计', value: 'AC001' },
      { text: '人力资源', value: 'HR001' },
      { text: '教师', value: 'TC001' },
      { text: '医生', value: 'DR001' },
      { text: '学生', value: 'ST001' }
    ]
  }
}

// 表单提交
const onSubmit = async () => {
  try {
    // 验证必填项
    if (!formData.value.nickname) {
      showToast('请填写昵称')
      return
    }
    
    // 如果性别未设置，提示用户设置
    if (formData.value.gender === 0) {
      showToast('请选择性别')
      return
    }
    
    if (selectedHobbies.value.length < 3) {
      showToast('请至少选择3个兴趣爱好')
      return
    }
    
    // 验证自我介绍长度
    if (formData.value.selfIntro && formData.value.selfIntro.length > 15) {
      showToast('自我介绍不能超过15个字')
      return
    }
    
    // 显示加载中
    showLoadingToast({
      message: '保存中...',
      forbidClick: true,
      duration: 0
    })
    
    // 确保数据类型正确
    // 构建提交数据
    const profileData = {
      // 从表单中获取的数据
      nickname: formData.value.nickname,
      gender: parseInt(formData.value.gender, 10), // 确保为整数
      birthday: formData.value.birthday,
      city: formData.value.city,
      cityCode: formData.value.cityCode || '',
      provinceCode: formData.value.provinceCode || '',
      education: Number(formData.value.education),
      occupation: formData.value.occupation || '',
      selfIntro: formData.value.selfIntro || '',
      
      // 其他数据
      hobbies: selectedHobbies.value.join(','),
      avatar: userAvatar.value,
      userId: userStore.userId // 用户ID
    }
    
    console.log('提交用户资料:', profileData, '其中性别值:', profileData.gender, '类型:', typeof profileData.gender);
    
    // 调用API保存资料
    const response = await userApi.updateUserProfile(profileData)
    
    closeToast()
    
    // 记录完整响应
    console.log('保存资料API响应:', response)
    
    if (response && (response.code === 200 || response.success)) {
      // 更新状态管理
      if (profileData.nickname) {
        userStore.setNickname(profileData.nickname)
      }
      
      if (profileData.avatar) {
        userStore.setAvatar(profileData.avatar)
      }
      
      // 强制刷新用户数据
      try {
        await userStore.fetchUserInfo();
      } catch (e) {
        console.error('刷新用户数据失败', e);
      }
      
      showSuccessToast('保存成功')
      setTimeout(() => {
        router.back()
      }, 1500)
    } else {
      console.error('保存资料失败:', response)
      showToast('保存失败: ' + (response?.message || '未知错误'))
    }
  } catch (error) {
    closeToast()
    console.error('保存资料失败', error)
    showToast('保存失败，请稍后重试')
  }
}

// 日期格式化函数
const dateFormatter = (type, val) => {
  if (type === 'year') {
    return `${val}年`;
  }
  if (type === 'month') {
    return `${val}月`;
  }
  if (type === 'day') {
    return `${val}日`;
  }
  return val;
}

// 设置学历
const setEducation = (educationValue) => {
  console.log('选择学历:', educationValue);
  formData.value.education = educationValue;
}

// 显示学历选项
const showEducationOptions = () => {
  // 重置学历以显示选项
  formData.value.education = 0;
}

const onCityConfirm = (values) => {
  console.log('选择的城市原始数据:', values);
  
  try {
    // 兼容不同版本的返回格式
    let province, city;
    
    if (Array.isArray(values)) {
      [province, city] = values;
    } else if (values && typeof values === 'object') {
      // 新版本可能直接返回选中项对象
      province = values.selectedOptions[0];
      city = values.selectedOptions[1];
    } else {
      console.error('无法解析城市选择数据:', values);
      showToast('选择城市失败，请重试');
      return;
    }
    
    if (!province || !city) {
      console.error('省份或城市数据缺失:', { province, city });
      showToast('请选择完整的省市信息');
      return;
    }
    
    console.log('解析后的省市数据:', { province, city });
    
    // 更新表单数据
    formData.value.city = `${province.name || province.text} ${city.name || city.text}`;
    formData.value.provinceCode = province.code || province.value;
    formData.value.cityCode = city.code || city.value;
    
    console.log('设置城市信息:', {
      city: formData.value.city,
      provinceCode: formData.value.provinceCode,
      cityCode: formData.value.cityCode
    });
    
    showCityPicker.value = false;
  } catch (error) {
    console.error('处理城市选择出错:', error);
    showToast('选择城市失败，请重试');
  }
};

// 处理头像上传
const handleAvatarChange = async (event) => {
  try {
    const file = event.target.files[0]
    if (!file) return
    
    // 检查文件类型
    if (!file.type.includes('image/')) {
      showToast('请上传图片文件')
      return
    }
    
    // 检查文件大小(最大5MB)
    if (file.size > 5 * 1024 * 1024) {
      showToast('图片大小不能超过5MB')
      return
    }
    
    // 显示加载
    showLoadingToast({
      message: '上传中...',
      forbidClick: true,
      duration: 0
    })
    
    // 创建表单数据
    const formDataUpload = new FormData()
    formDataUpload.append('file', file)
    
    // 调用API上传
    const response = await userApi.uploadAvatar(formDataUpload)
    
    closeToast()
    
    if (response && response.data) {
      // 更新头像显示
      userAvatar.value = response.data
      
      // 同步头像到基本信息
      try {
        await userApi.updateUserBasicInfo({
          avatar: response.data
        })
      } catch (error) {
        console.error('同步头像失败', error)
      }
      
      showToast('头像上传成功')
    } else {
      showToast('头像上传失败，请重试')
    }
    
    // 重置文件输入
    event.target.value = ''
  } catch (error) {
    closeToast()
    console.error('上传头像失败', error)
    showToast('上传失败，请稍后重试')
    
    if (event.target) {
      event.target.value = ''
    }
  }
}

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

// 返回上一页
const goBack = () => {
  router.back()
}

// 确认性别选择的函数
const confirmGenderSelection = (genderValue) => {
  console.log('准备选择性别:', genderValue);
  // 显示确认对话框
  showDialog({
    title: '确认选择',
    message: '性别一旦设置后将无法修改，请确认您的选择。',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    showCancelButton: true,
  }).then(() => {
    // 用户确认后设置性别
    formData.value.gender = parseInt(genderValue, 10);
    console.log('已确认并设置性别:', formData.value.gender);
  }).catch(() => {
    // 用户取消，不操作
    console.log('用户取消性别选择');
  });
};

// 获取当前位置
const getCurrentLocation = async () => {
  if (isLocating.value) return
  
  try {
    isLocating.value = true
    showLoadingToast({
      message: '获取位置信息...',
      forbidClick: true,
      duration: 0
    })
    
    // 使用高德地图API获取位置信息
    const locationInfo = await getCurrentLocationInfo()
    
    // 更新表单数据
    formData.value.city = locationInfo.city
    formData.value.provinceCode = locationInfo.province ? locationInfo.province.replace(/省|市/g, '') : ''
    formData.value.cityCode = locationInfo.city ? locationInfo.city.replace(/市/g, '') : ''
    
    showSuccessToast('位置获取成功')
          } catch (error) {
          console.error('获取位置失败:', error)
    showToast('获取位置失败，请稍后重试')
  } finally {
    isLocating.value = false
    closeToast()
  }
}

// 跳转到认证中心
const goToVerificationCenter = () => {
  router.push('/verification');
};
</script>

<style scoped>
.profile-settings-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 30px;
}

.content-container {
  padding: 16px;
}

/* 导航栏 */
.custom-nav {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* 卡片公共样式 */
.profile-card {
  background-color: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f2f2f2;
  font-size: 15px;
  font-weight: 500;
  color: #323233;
}

.card-icon {
  margin-right: 8px;
  color: #1989fa;
}

.card-content {
  padding: 16px;
  color: #323233;
}

/* 头像卡片样式 */
.avatar-card {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
}

.avatar-card .card-content {
  display: flex;
  justify-content: center;
}

.avatar-upload {
  position: relative;
  width: 100px;
  height: 100px;
}

.avatar-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-edit-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: #1989fa;
  color: #fff;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 6px rgba(25, 137, 250, 0.3);
}

.hidden-input {
  display: none;
}

/* 表单样式 */
.custom-field {
  --field-label-width: 80px;
  --field-label-color: #323233;
  --field-text-color: #323233;
}

.intro-field {
  min-height: 100px;
}

/* 兴趣爱好标签样式 */
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
  color: #323233;
}

.hobby-tag-selected {
  background-color: #e6f7ff;
  color: #1989fa;
  border: 1px solid #91d5ff;
}

.tag-hint {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

/* 提交按钮样式 */
.submit-button-wrapper {
  padding: 16px;
  margin-top: 20px;
}

.submit-button {
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

/* 优化性别选择器样式 */
.gender-radio-group {
  display: flex;
  gap: 12px;
}

.gender-option {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  background-color: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gender-selected {
  background-color: #e6f7ff;
  color: #1989fa;
  font-weight: 500;
}

.gender-icon {
  margin-right: 4px;
  font-size: 14px;
}

.gender-display {
  display: flex;
  align-items: center;
}

.gender-selected-display {
  font-size: 14px;
  color: #323233;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-locked {
  margin-left: 8px;
  background-color: #ff976a;
  font-size: 10px;
}

/* 添加年龄选择器样式 */
.age-selector {
  display: flex;
  align-items: center;
}

.age-input-container {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 16px;
  padding: 4px 12px;
  width: auto;
  max-width: 100px;
}

.age-input {
  width: 40px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1989fa;
  font-weight: 500;
  padding: 4px 0;
  text-align: center;
}

.age-input:focus {
  outline: none;
}

.age-unit {
  color: #666;
  margin-left: 2px;
  font-size: 13px;
}

/* 城市选择样式 */
.location-display {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.location-selected {
  display: flex;
  align-items: center;
  color: #323233;
  font-size: 14px;
}

.location-placeholder {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  color: #999;
  font-size: 14px;
}

.location-icon {
  color: #1989fa;
  font-size: 16px;
  margin-right: 4px;
}

.arrow-icon {
  color: #c8c9cc;
  font-size: 14px;
}

/* 修改教育学历选择器为直接选择风格 */
.education-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.education-option {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  background-color: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.education-selected {
  background-color: #e6f7ff;
  color: #1989fa;
  font-weight: 500;
}

.education-display {
  display: flex;
  align-items: center;
  width: 100%;
}

.education-selected-display {
  font-size: 14px;
  color: #323233;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 修复编码问题引起的文字不显示 */
:deep(.van-field__label),
:deep(.van-cell__title),
:deep(.van-cell__value),
:deep(.van-button__text) {
  color: #323233 !important;
}

:deep(input),
:deep(textarea) {
  color: #323233 !important;
}

:deep(.van-field__control) {
  color: #323233 !important;
}

:deep(.van-cell) {
  color: #323233 !important;
}

/* 认证状态样式 */
.verification-status-list {
  margin-bottom: 15px;
}

.verification-status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.verification-status-item:last-child {
  border-bottom: none;
}

.verification-type {
  display: flex;
  align-items: center;
}

.verification-icon {
  margin-right: 5px;
  font-size: 18px;
}

.verification-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-pending {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.status-default {
  background-color: #f5f5f5;
  color: #999;
  border: 1px solid #d9d9d9;
}

.verification-action {
  margin-top: 15px;
  text-align: center;
}

.verification-status-badges {
  display: flex;
  align-items: center;
}

.verification-icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
  font-size: 16px;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  vertical-align: middle;
  color: #fff;
}

.verification-icon-badge.identity.badge-verified {
  background-color: #52c41a;
}

.verification-icon-badge.education.badge-verified {
  background-color: #722ed1;
}

.verification-icon-badge.badge-unverified {
  background-color: #d9d9d9;
}
</style> 
