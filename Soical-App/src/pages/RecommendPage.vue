<template>
  <div class="page-container recommend-page">
    <!-- 页面头部 -->
    <div class="header">
      <h1 class="page-title">发现</h1>
      <div class="filter-bar" v-if="isLoggedIn">
        <van-button 
          size="small" 
          plain 
          icon="filter-o" 
          class="filter-btn"
          @click="showFilterPanel = true"
        >
          筛选
        </van-button>
      </div>
    </div>
    
    <div class="main-content" v-if="isLoggedIn">
      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <van-loading color="var(--primary-color)" size="36px" />
        <p class="loading-text">正在为您发掘有趣的人...</p>
      </div>
      
      <!-- 卡片浏览区域 -->
      <transition name="fade" mode="out-in">
        <swipe-cards 
          v-if="!loading"
          :users="recommendedUsers"
          :loading="loading"
          @like="handleLike"
          @dislike="handleDislike"
          @empty="handleEmpty"
          @reload="fetchRecommendedUsers"
          ref="swipeCards"
          class="swipe-cards-container"
        />
      </transition>
    </div>
    
    <!-- 未登录提示 -->
    <div class="login-prompt" v-if="!isLoggedIn">
      <van-empty image="search" description="">
        <template #description>
          <p class="login-text">登录后发现与您志趣相投的人</p>
        </template>
        <van-button type="primary" round size="large" @click="goToLogin" class="login-btn">立即登录</van-button>
      </van-empty>
    </div>
    
    <!-- 筛选面板 -->
    <van-popup
      v-model:show="showFilterPanel"
      position="bottom"
      round
      :style="{ height: '60%' }"
      close-icon="close"
      close-icon-position="top-right"
      closeable
      class="filter-popup"
    >
      <div class="filter-panel">
        <div class="filter-header">
          <span class="filter-title">个性化发现</span>
        </div>
        
        <div class="filter-form">
          <van-field
            v-model="tempFilters.gender"
            is-link
            readonly
            label="性别"
            placeholder="请选择"
            input-align="right"
            @click="showGenderPicker = true"
            class="filter-field"
          />
          
          <van-field
            v-model="tempFilters.location"
            label="位置"
            placeholder="请输入所在城市"
            input-align="right"
            class="filter-field location-field"
          >
            <template #button>
              <van-button 
                size="small" 
                type="primary" 
                icon="location-o" 
                class="location-btn"
                @click="getCurrentLocation" 
              >定位</van-button>
            </template>
          </van-field>
        </div>
        
        <!-- 兴趣标签筛选 -->
        <div class="filter-section">
          <div class="filter-title-bar">
            <span class="filter-section-title">兴趣标签</span>
            <span class="selected-count" v-if="selectedInterests.length > 0">
              已选 {{ selectedInterests.length }}
            </span>
          </div>
          <div class="interests-container">
            <div 
              v-for="interest in interestsOptions" 
              :key="interest"
              class="interest-tag"
              :class="{ 'selected': isInterestSelected(interest) }"
              @click="onInterestSelect(interest)"
            >
              {{ interest }}
            </div>
          </div>
        </div>
        
        <div class="filter-actions">
          <van-button round block type="primary" size="large" @click="applyFilters">应用筛选</van-button>
          <van-button round block plain size="large" style="margin-top: 12px;" @click="resetFilters">重置</van-button>
        </div>
      </div>
      
      <!-- 性别选择器 -->
      <van-popup v-model:show="showGenderPicker" position="bottom">
        <van-picker
          show-toolbar
          title="选择性别"
          :columns="genderOptions"
          @confirm="onGenderConfirm"
          @cancel="showGenderPicker = false"
        />
      </van-popup>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useMatchStore } from '../stores/match'
import { showToast, showLoadingToast, closeToast } from 'vant'
import SwipeCards from '../components/match/SwipeCards.vue'
import { getCurrentLocationInfo } from '../utils/mapUtils'

// Store和路由
const router = useRouter()
const userStore = useUserStore()
const matchStore = useMatchStore()

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 推荐用户列表
const recommendedUsers = computed(() => {
  console.log('RecommendPage接收到的用户列表:', matchStore.recommendedUsers)
  return matchStore.recommendedUsers
})
const loading = computed(() => matchStore.loading)

// 滑动卡片组件引用
const swipeCards = ref(null)

// 筛选面板相关
const showFilterPanel = ref(false)
const showGenderPicker = ref(false)

// 临时筛选条件
const tempFilters = ref({
  gender: '',
  genderValue: null, // 存储性别的值
  location: '',
  interests: '' // 兴趣标签
})

// 性别选项
const genderOptions = [
  { text: '不限', value: null },
  { text: '男', value: 1 },
  { text: '女', value: 2 }
]

// 初始化兴趣标签选项
const interestsOptions = ref([
  "旅行", "读书", "电影", "音乐", "摄影", "美食", "健身", "游戏", "绘画", "编程", 
  "篮球", "足球", "瑜伽", "舞蹈", "动漫", "宠物", "园艺", "手工", "烹饪", "跑步"
]);

// 选择的兴趣标签
const selectedInterests = ref([]);

// 处理兴趣标签选择
const onInterestSelect = (interest) => {
  const index = selectedInterests.value.indexOf(interest);
  if (index > -1) {
    selectedInterests.value.splice(index, 1);
  } else {
    selectedInterests.value.push(interest);
  }
  // 更新筛选条件
  tempFilters.value.interests = selectedInterests.value.join(',');
};

// 判断标签是否被选中
const isInterestSelected = (interest) => {
  return selectedInterests.value.includes(interest);
};

// 获取推荐用户
const fetchRecommendedUsers = async (forceRefresh = false) => {
  if (!isLoggedIn.value) return Promise.reject(new Error('未登录'));
  
  console.log('[页面] 开始获取推荐用户, 强制刷新:', forceRefresh);
  try {
    // 显示加载提示
    showLoadingToast({
      message: '正在获取推荐...',
      forbidClick: true,
      duration: 0
    });

    await matchStore.fetchRecommendedUsers(forceRefresh);
    
    console.log('[页面] 获取推荐用户后的列表状态:', recommendedUsers.value.length);
    
    // 检查推荐列表状态
    if (recommendedUsers.value.length === 0) {
      // 如果有筛选条件但没有结果，尝试重置筛选条件
      const hasFilters = Object.values(matchStore.filters).some(value => value !== null && value !== '');
      if (hasFilters) {
        console.log('[页面] 检测到筛选条件但无结果，尝试重置筛选');
        matchStore.resetFilters();
        // 重新获取，但不显示无推荐提示
        await matchStore.fetchRecommendedUsers(true);
        
        if (recommendedUsers.value.length === 0) {
          showToast('未找到符合条件的推荐用户');
        }
      } else {
        showToast('暂无推荐用户');
      }
    }
    
    closeToast();
    return Promise.resolve(recommendedUsers.value);
  } catch (error) {
    console.error('[页面] 获取推荐用户失败:', error);
    closeToast();
    showToast('获取推荐失败，请稍后重试');
    return Promise.reject(error);
  }
}

// 处理喜欢操作
const handleLike = async (user) => {
  if (!user || !user.userId) return
  
  console.log('[页面] 处理喜欢操作:', user.userId, user.nickname || user.username);
  
  // 在操作前记录当前的推荐列表状态
  const beforeList = [...recommendedUsers.value];
  console.log('[页面] 操作前推荐列表长度:', beforeList.length);
  
  // 显示操作提示
  showLoadingToast({
    message: '处理中...',
    forbidClick: true,
    duration: 500
  });
  
  // 调用API，发送喜欢请求
  const matched = await matchStore.likeUser(user.userId)
  
  // 查看操作后的列表状态
  console.log('[页面] 操作后推荐列表长度:', recommendedUsers.value.length);
  
  // 如果匹配成功（双方都喜欢对方），显示匹配弹窗
  // matched为true表示双方互相喜欢，可以开始聊天
  if (matched && swipeCards.value) {
    swipeCards.value.showMatch(user)
  } else {
    // 如果只是单方面喜欢，只会保存状态，但不会立即匹配
    showToast('已喜欢此用户')
  }
}

// 处理不喜欢操作
const handleDislike = async (user) => {
  if (!user || !user.userId) return
  
  console.log('[页面] 处理不喜欢操作:', user.userId, user.nickname || user.username);
  
  // 在操作前记录当前的推荐列表状态
  const beforeList = [...recommendedUsers.value];
  console.log('[页面] 操作前推荐列表长度:', beforeList.length);
  
  // 显示操作提示
  showLoadingToast({
    message: '处理中...',
    forbidClick: true,
    duration: 500
  });
  
  // 调用API，发送不喜欢请求
  await matchStore.dislikeUser(user.userId)
  
  // 查看操作后的列表状态
  console.log('[页面] 操作后推荐列表长度:', recommendedUsers.value.length);
}

// 处理推荐列表空的情况
const handleEmpty = async () => {
  console.log('[页面] 推荐列表为空，尝试重新获取');
  
  try {
    showLoadingToast({
      message: '正在加载更多推荐...',
      forbidClick: true,
      duration: 0
    });
    
    // 重置当前筛选条件，以便获取更多推荐
    const currentFilters = { ...matchStore.filters };
    console.log('[页面] 当前筛选条件:', currentFilters);
    
    // 如果有任何筛选条件，先尝试重置它们
    if (Object.values(currentFilters).some(value => value !== null && value !== '')) {
      console.log('[页面] 检测到筛选条件，尝试重置');
      matchStore.resetFilters();
      await fetchRecommendedUsers(true);
      
      if (recommendedUsers.value.length > 0) {
        closeToast();
        showToast('已重置筛选条件并获取新推荐');
        return;
      }
    }
    
    // 如果重置筛选条件后仍然没有推荐，尝试强制刷新
    await fetchRecommendedUsers(true);
    closeToast();
    
    if (recommendedUsers.value.length === 0) {
      showToast('暂无更多推荐用户，请稍后再试');
    } else {
      console.log('[页面] 成功获取新推荐，数量:', recommendedUsers.value.length);
    }
  } catch (error) {
    closeToast();
    console.error('[页面] 重新获取推荐失败:', error);
    showToast('获取推荐失败，请稍后再试');
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 性别选择确认
const onGenderConfirm = (value) => {
  console.log('选择的性别值:', value);
  if (value) {
    // 处理选择的性别值
    if (typeof value === 'object') {
      tempFilters.value.gender = value.text;
      tempFilters.value.genderValue = value.value;
    } else {
      // 处理直接传递的值（可能是v-model绑定的选中项）
      const option = genderOptions.find(opt => opt.text === value || opt.value === value);
      if (option) {
        tempFilters.value.gender = option.text;
        tempFilters.value.genderValue = option.value;
      } else {
        tempFilters.value.gender = value;
        tempFilters.value.genderValue = value === '男' ? 1 : value === '女' ? 2 : null;
      }
    }
  } else {
    tempFilters.value.gender = '';
    tempFilters.value.genderValue = null;
  }
  
  console.log('更新后的性别筛选值:', tempFilters.value.gender, tempFilters.value.genderValue);
  showGenderPicker.value = false;
};

// 获取当前位置
const getCurrentLocation = async () => {
  try {
    // 显示加载提示
    showLoadingToast({
      message: '正在获取位置...',
      forbidClick: true,
      duration: 0
    });
    
    // 检查API Key是否存在
    const apiKey = import.meta.env.VITE_AMAP_KEY;
    if (!apiKey) {
      closeToast();
      console.error('高德地图API密钥未配置');
      
      // 使用默认城市
      tempFilters.value.location = '北京';
      showToast({
        message: '高德地图API未配置，已设置为默认位置',
        type: 'warning',
        position: 'bottom'
      });
      return;
    }
    
    console.log('开始获取位置信息...');
    const locationInfo = await getCurrentLocationInfo();
    closeToast(); // 关闭加载提示
    
    console.log('获取到位置信息:', locationInfo);
    
    if (locationInfo && locationInfo.city) {
      tempFilters.value.location = locationInfo.city;
      showToast({
        message: '定位成功：' + locationInfo.city,
        type: 'success',
        position: 'bottom'
      });
    } else if (locationInfo && locationInfo.address) {
      // 如果没有城市但有地址
      tempFilters.value.location = locationInfo.address;
      showToast({
        message: '已设置位置：' + locationInfo.address,
        type: 'success',
        position: 'bottom'
      });
    } else {
      // 使用默认位置
      tempFilters.value.location = '北京';
      showToast({
        message: '未能获取具体城市，已设置为默认位置',
        type: 'warning',
        position: 'bottom'
      });
    }
  } catch (error) {
    closeToast(); // 确保关闭加载提示
    console.error('获取当前位置信息失败:', error);
    
    // 使用默认位置
    tempFilters.value.location = '北京';
    showToast({
      message: '定位失败: ' + (error.message || '未知错误') + '，已设置为默认位置',
      type: 'warning',
      position: 'bottom'
    });
  }
};

// 应用筛选条件
const applyFilters = () => {
  // 打印出当前的临时筛选条件，用于调试
  console.log('应用前的筛选条件:', JSON.stringify(tempFilters.value));
  
  // 构建筛选条件对象，确保值正确转换
  const filters = {
    gender: tempFilters.value.genderValue, // 使用数字类型的性别值
    location: tempFilters.value.location && tempFilters.value.location.trim() !== '' ? 
              tempFilters.value.location.trim() : null,
    interests: tempFilters.value.interests && tempFilters.value.interests.trim() !== '' ? 
               tempFilters.value.interests.trim() : null
  };
  
  // 打印要应用的筛选条件
  console.log('最终应用的筛选条件:', JSON.stringify(filters));
  matchStore.updateFilters(filters);
  
  // 显示加载提示
  showLoadingToast({
    message: '加载中...',
    forbidClick: true,
    duration: 0
  });
  
  // 刷新推荐列表
  fetchRecommendedUsers(true).then(() => {
    closeToast();
    showToast({
      message: '筛选条件已应用',
      type: 'success',
      position: 'bottom'
    });
  }).catch(error => {
    closeToast();
    console.error('获取推荐用户失败:', error);
    showToast({
      message: '获取推荐失败，请稍后重试',
      type: 'fail',
      position: 'bottom'
    });
  });
  
  // 关闭筛选面板
  showFilterPanel.value = false;
}

// 重置筛选条件
const resetFilters = () => {
  // 重置临时筛选条件
  tempFilters.value = {
    gender: '',
    genderValue: null,
    location: '',
    interests: ''
  };
  
  // 清空已选兴趣标签
  selectedInterests.value = [];
  
  console.log('重置筛选条件');
  
  // 重置store中的筛选条件并刷新列表
  matchStore.resetFilters();
  
  // 显示加载提示
  showLoadingToast({
    message: '加载中...',
    forbidClick: true,
    duration: 0
  });
  
  // 刷新推荐列表
  fetchRecommendedUsers(true).then(() => {
    closeToast();
    showToast({
      message: '已重置筛选条件',
      type: 'success',
      position: 'bottom'
    });
  }).catch(error => {
    closeToast();
    console.error('获取推荐用户失败:', error);
    showToast({
      message: '获取推荐失败，请稍后重试',
      type: 'fail',
      position: 'bottom'
    });
  });
}

// 页面加载和激活时的处理
onMounted(() => {
  if (isLoggedIn.value) {
    console.log('[页面] 页面挂载，开始获取推荐');
    fetchRecommendedUsers(true);
  }
});

onActivated(() => {
  if (isLoggedIn.value) {
    console.log('[页面] 页面激活，检查是否需要刷新');
    const lastUpdateTime = matchStore.lastUpdateTime || 0;
    const now = Date.now();
    if (recommendedUsers.value.length === 0 || (now - lastUpdateTime) > 5 * 60 * 1000) {
      console.log('[页面] 需要刷新推荐列表');
      fetchRecommendedUsers(true);
    }
  }
});

// 定时刷新推荐列表
let refreshInterval;
onMounted(() => {
  if (isLoggedIn.value) {
    // 设置定时刷新
    refreshInterval = setInterval(() => {
      if (isLoggedIn.value) {
        console.log('[页面] 定时刷新推荐列表');
        fetchRecommendedUsers(true);
      }
    }, 15 * 60 * 1000); // 15分钟刷新一次
  }
});

onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});
</script>

<style scoped>
.recommend-page {
  background-color: var(--bg-secondary);
  position: relative;
  transition: background-color var(--transition-normal) var(--ease-out);
}

.header {
  background-color: var(--bg-primary);
  padding: 16px 0;
  border-bottom-left-radius: var(--border-radius-lg);
  border-bottom-right-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
  transition: background-color var(--transition-normal) var(--ease-out),
              box-shadow var(--transition-normal) var(--ease-out);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  padding: 0 20px;
  color: var(--text-primary);
  transition: color var(--transition-normal) var(--ease-out);
}

.filter-bar {
  padding-right: 20px;
}

.filter-btn {
  border-radius: var(--border-radius-round);
  font-size: 14px;
  background-color: var(--bg-tertiary);
  border-color: transparent;
  color: var(--text-secondary);
  transition: background-color var(--transition-fast) var(--ease-out),
              color var(--transition-fast) var(--ease-out),
              transform var(--transition-fast) var(--ease-out);
}

.filter-btn:active {
  transform: scale(0.95);
}

.main-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10px;
}

.swipe-cards-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.login-prompt {
  height: 70vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.login-text {
  color: var(--text-secondary);
  font-size: 16px;
  margin-bottom: 20px;
  transition: color var(--transition-normal) var(--ease-out);
}

.login-btn {
  width: 200px;
  height: 44px;
  font-weight: 600;
  border-radius: var(--border-radius-round);
  transition: background-color var(--transition-fast) var(--ease-out),
              transform var(--transition-fast) var(--ease-out);
}

.login-btn:active {
  transform: scale(0.96);
}

/* 筛选面板样式 */
.filter-popup {
  background-color: var(--bg-primary);
  transition: background-color var(--transition-normal) var(--ease-out);
}

.filter-panel {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.filter-header {
  text-align: center;
  margin-bottom: 20px;
}

.filter-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color var(--transition-normal) var(--ease-out);
}

.filter-form {
  margin-bottom: 20px;
}

.filter-field {
  margin-bottom: 8px;
  background-color: var(--bg-tertiary);
  border-radius: var(--border-radius);
  transition: background-color var(--transition-normal) var(--ease-out);
}

.location-field {
  margin-bottom: 16px;
}

.location-btn {
  border-radius: var(--border-radius-sm);
  height: 32px;
  font-size: 12px;
  transition: background-color var(--transition-fast) var(--ease-out),
              transform var(--transition-fast) var(--ease-out);
}

.location-btn:active {
  transform: scale(0.95);
}

.filter-section {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
}

.filter-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filter-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color var(--transition-normal) var(--ease-out);
}

.selected-count {
  font-size: 14px;
  color: var(--primary-color);
  transition: color var(--transition-normal) var(--ease-out);
}

.interests-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.interest-tag {
  padding: 8px 12px;
  background-color: var(--bg-tertiary);
  border-radius: var(--border-radius-round);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color var(--transition-fast) var(--ease-out),
              color var(--transition-fast) var(--ease-out),
              transform var(--transition-fast) var(--ease-out);
}

.interest-tag:active {
  transform: scale(0.95);
}

.interest-tag.selected {
  background-color: var(--primary-light);
  color: var(--text-inverse);
}

.filter-actions {
  margin-top: auto;
  padding-bottom: 20px;
}
</style> 