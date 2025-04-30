<template>
  <div class="swipe-cards-container">
    <!-- 匹配结果弹窗 -->
    <van-popup
      v-model:show="showMatchPopup"
      round
      :style="{ width: '85%', padding: '24px' }"
      close-on-click-overlay
    >
      <div class="match-popup">
        <div class="match-img-container">
          <img :src="currentUser?.avatar || '/images/default-avatar.png'" class="match-img" />
          <div class="heart-icon"><van-icon name="like" color="#ff6b81" size="36" /></div>
          <img :src="matchedUser?.avatar || '/images/default-avatar.png'" class="match-img" />
        </div>
        <h2 class="match-title">匹配成功!</h2>
        <p class="match-text">你和 {{matchedUser?.nickname || '用户'}} 互相喜欢对方</p>
        <div class="match-actions">
          <van-button round type="primary" block size="large" @click="startChat">开始聊天</van-button>
          <van-button round plain block size="large" style="margin-top: 12px;" @click="closeMatchPopup">继续浏览</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 卡片为空提示 -->
    <div class="empty-state" v-if="!loading && (!users || users.length === 0)">
      <van-empty image="search" description="暂无更多推荐">
        <template #bottom>
          <van-button round type="primary" size="large" @click="$emit('reload')">刷新</van-button>
        </template>
      </van-empty>
    </div>

    <!-- 卡片堆 -->
    <div class="cards-stack" v-else>
      <transition-group name="card">
        <div 
          v-for="(user, index) in visibleUsers" 
          :key="user.userId"
          class="user-card"
          :style="cardStyle(index)"
          :class="{ 'dragging': isDragging && currentIndex === index }"
          @mousedown="startDrag($event, index)"
          @touchstart="startDrag($event, index)"
        >
          <div class="card-content">
            <!-- 用户相册轮播 -->
            <div class="card-image-container">
              <!-- 在线状态指示器 -->
              <div class="status-indicator online" v-if="isOnline(user)">
                <div class="pulse-circle"></div>
                <span class="status-text">在线</span>
              </div>
              
              <!-- 不在线状态指示器 -->
              <div class="status-indicator offline" v-else>
                <div class="offline-circle"></div>
                <span class="status-text">离线</span>
              </div>
              
              <van-swipe
                class="card-swipe"
                :autoplay="0"
                :show-indicators="true"
                :touchable="index === 0"
                @click-item="expandImage"
                indicator-color="#ff6b81"
              >
                <!-- 显示用户头像作为第一张图 -->
                <van-swipe-item>
                  <img :src="user.avatar || '/images/default-avatar.png'" class="card-image" :alt="user.nickname || user.username" />
                </van-swipe-item>
                <!-- 显示用户相册其他图片 -->
                <van-swipe-item v-for="(photo, photoIndex) in (user.photos || [])" :key="photoIndex">
                  <img :src="photo" class="card-image" :alt="user.nickname || user.username" />
                </van-swipe-item>
              </van-swipe>
              
              <div class="user-info-overlay">
                <h2 class="user-name">
                  {{ user.nickname || user.username }} 
                  <span class="user-age" v-if="user.age">{{ user.age }}岁</span>
                </h2>
                <p class="user-location" v-if="user.location">
                  <van-icon name="location-o" /> {{ user.location }}
                </p>
              </div>
            </div>
            
            <div class="card-details">
              <!-- 匹配理由提示 -->
              <div class="match-reasons" v-if="getMatchReasons(user).length > 0">
                <div class="match-reasons-header">
                  <van-icon name="fire-o" color="#ff6b81" size="20" />
                  <h3 class="match-reasons-title">你们可能很合拍</h3>
                </div>
                <div class="reason-tags">
                  <van-tag v-for="(reason, i) in getMatchReasons(user)" :key="i" 
                    plain color="#ff6b81" size="medium" class="reason-tag">
                    {{ reason }}
                  </van-tag>
                </div>
              </div>
              
              <div class="user-info">
                <!-- 个性签名/自我介绍 -->
                <p class="user-intro" v-if="user.selfIntro">
                  <van-icon name="chat-o" style="margin-right: 6px;" />
                  <span class="intro-text">{{ user.selfIntro }}</span>
                </p>
                <p class="user-intro empty-intro" v-else>
                  <van-icon name="chat-o" style="margin-right: 6px;" />
                  <span class="intro-text">这个人很神秘，没有留下自我介绍</span>
                </p>
                
                <!-- 用户信息区域 -->
                <div class="user-details-section">
                  <!-- 职业信息 -->
                  <div class="info-item" v-if="user.occupation">
                    <van-icon name="manager-o" style="margin-right: 6px;" /> 
                    <span class="info-label">职业:</span>
                    <span class="info-value">{{ user.occupation }}</span>
                  </div>
                  
                  <!-- 技能信息 -->
                  <div class="info-item" v-if="user.skills">
                    <van-icon name="diamond-o" style="margin-right: 6px;" /> 
                    <span class="info-label">擅长:</span>
                    <span class="info-value">{{ user.skills }}</span>
                  </div>
                  
                  <!-- 学历信息 -->
                  <div class="info-item" v-if="user.education">
                    <van-icon name="award-o" style="margin-right: 6px;" /> 
                    <span class="info-label">学历:</span>
                    <span class="info-value">{{ getEducationText(user.education) }}</span>
                  </div>
                  
                  <!-- 无信息时显示 -->
                  <div class="info-item no-details" v-if="!user.occupation && !user.skills && !user.education">
                    <van-icon name="info-o" style="margin-right: 6px;" />
                    <span class="info-value">用户未完善详细信息</span>
                  </div>
                </div>
                
                <!-- 兴趣标签 -->
                <div class="hobbies-container" v-if="user.hobbies">
                  <div class="hobby-title">
                    <van-icon name="like-o" style="margin-right: 6px;" />
                    <span>兴趣爱好</span>
                    <span v-if="getCommonHobbiesCount(user) > 0" class="common-hobby-count">
                      {{ getCommonHobbiesCount(user) }}个共同兴趣
                    </span>
                  </div>
                  <div class="hobby-tags">
                    <van-tag 
                      v-for="(hobby, i) in getHobbyList(user.hobbies)" 
                      :key="i" 
                      plain 
                      :color="isCommonHobby(hobby) ? '#ff6b81' : '#1989fa'" 
                      size="medium"
                      class="hobby-tag"
                      :class="{ 'common-hobby': isCommonHobby(hobby) }"
                    >
                      <template v-if="isCommonHobby(hobby)">
                        <van-icon name="star" size="12" style="margin-right: 4px;" />
                      </template>
                      {{ hobby }}
                    </van-tag>
                  </div>
                </div>
                <div class="hobbies-container empty-hobbies" v-else>
                  <div class="hobby-title">
                    <van-icon name="like-o" style="margin-right: 6px;" />
                    <span>兴趣爱好</span>
                  </div>
                  <div class="empty-hobby-message">用户未添加兴趣爱好</div>
                </div>
                
                <!-- 相似度显示 -->
                <div class="similarity-container" v-if="user.similarity">
                  <div class="similarity-header">
                    <div class="similarity-title">匹配度</div>
                    <div class="similarity-value">{{ user.similarity }}%</div>
                  </div>
                  <div class="similarity-bar">
                    <div class="similarity-progress" :style="{ width: `${user.similarity}%` }"></div>
                  </div>
                  <div class="similarity-desc">
                    {{ getSimilarityDesc(user.similarity) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 底部按钮区域 -->
          <div class="profile-button-container">
            <van-button 
              size="normal" 
              type="primary" 
              @click.stop="viewProfile(user)"
              class="view-profile-btn"
              icon="contact"
            >
              查看完整资料
            </van-button>
          </div>
          
          <!-- 喜欢/不喜欢指示器 -->
          <div class="like-indicator" :class="{ 'visible': dragStatus === 'like' }">
            <van-icon name="like" color="#3cc952" size="60" />
          </div>
          
          <div class="dislike-indicator" :class="{ 'visible': dragStatus === 'dislike' }">
            <van-icon name="cross" color="#ff4757" size="60" />
          </div>
        </div>
      </transition-group>
      
      <!-- 底部操作按钮 -->
      <div class="action-buttons" v-if="visibleUsers.length > 0">
        <van-button round icon="close" class="action-btn dislike-btn" @click="handleDislike" />
        <van-button round icon="like-o" class="action-btn like-btn" @click="handleLike" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user';
import { showImagePreview, showToast } from 'vant';
import { getUserOnlineStatus } from '../../api/user';

const props = defineProps({
  users: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['like', 'dislike', 'empty', 'reload']);

const router = useRouter();
const userStore = useUserStore();

// 当前用户信息
const currentUser = computed(() => userStore.userInfo);

// 可见用户 (限制最多显示15张卡片)
const visibleUsers = computed(() => {
  console.log('SwipeCards组件接收到的用户列表:', props.users)
  const result = props.users.slice(currentIndex.value, currentIndex.value + 15);
  console.log('SwipeCards显示的可见用户:', result)
  return result;
});

// 当前显示的卡片索引
const currentIndex = ref(0);

// 当前用户的兴趣爱好列表 (用于计算共同兴趣)
const myHobbies = computed(() => {
  if (!currentUser.value || !currentUser.value.hobbies) return [];
  return getHobbyList(currentUser.value.hobbies);
});

// 拖拽状态
const isDragging = ref(false);
const dragStartX = ref(0);
const dragStartY = ref(0);
const dragX = ref(0);
const dragY = ref(0);
const dragStatus = ref(null); // 'like' or 'dislike' or null

// 匹配弹窗状态
const showMatchPopup = ref(false);
const matchedUser = ref(null);

// 用户在线状态
const onlineStatus = ref({});

// 监听用户列表变化
watch(() => props.users, () => {
  if (props.users.length === 0 && !props.loading) {
    emit('empty');
  }
}, { deep: true });

// 监听currentIndex的变化，确保卡片顺序正确
watch(currentIndex, (newIndex) => {
  console.log('当前卡片索引更新为:', newIndex);
}, { immediate: true });

// 获取学历文本
const getEducationText = (education) => {
  const educationMap = {
    0: '未填写',
    1: '高中及以下',
    2: '大专',
    3: '本科',
    4: '硕士',
    5: '博士及以上'
  };
  return educationMap[education] || '未知';
};

// 将兴趣字符串转换为数组
const getHobbyList = (hobbies) => {
  if (!hobbies) return [];
  return hobbies.split(/,|、|;/).map(item => item.trim()).filter(item => item);
};

// 判断是否是共同兴趣
const isCommonHobby = (hobby) => {
  if (!myHobbies.value.length) return false;
  
  return myHobbies.value.some(myHobby => 
    myHobby === hobby || 
    myHobby.includes(hobby) || 
    hobby.includes(myHobby)
  );
};

// 检查用户是否在线
const isOnline = (user) => {
  if (!user || !user.userId) return false;
  return onlineStatus.value[user.userId] || false;
};

// 获取用户在线状态
const fetchOnlineStatus = async () => {
  try {
    if (!visibleUsers.value.length) return;
    
    const userIds = visibleUsers.value.map(user => user.userId).filter(Boolean);
    if (!userIds.length) return;
    
    const response = await getUserOnlineStatus(userIds);
    if (response && response.data) {
      onlineStatus.value = response.data;
    }
  } catch (error) {
    console.error('获取用户在线状态失败:', error);
    // 出错时设置一些模拟数据，避免UI显示问题
    visibleUsers.value.forEach(user => {
      if (user && user.userId) {
        // 随机在线状态，约50%在线
        onlineStatus.value[user.userId] = Math.random() > 0.5;
      }
    });
  }
};

// 当可见用户变化时，获取最新在线状态
watch(() => visibleUsers.value, () => {
  fetchOnlineStatus();
}, { deep: true });

// 页面初始化获取在线状态
onMounted(() => {
  fetchOnlineStatus();
  
  // 定时更新在线状态
  const statusInterval = setInterval(fetchOnlineStatus, 30000); // 每30秒更新一次
  
  // 组件销毁时清除定时器
  onBeforeUnmount(() => {
    clearInterval(statusInterval);
  });
});

// 获取匹配理由
const getMatchReasons = (user) => {
  const reasons = [];
  
  // 根据相似度添加匹配理由
  if (user.similarity >= 80) {
    reasons.push("超高匹配度");
  } else if (user.similarity >= 60) {
    reasons.push("高匹配度");
  }
  
  // 检查共同兴趣
  if (user.hobbies && currentUser.value && currentUser.value.hobbies) {
    const userHobbies = getHobbyList(user.hobbies);
    const commonHobbies = userHobbies.filter(hobby => isCommonHobby(hobby));
    
    if (commonHobbies.length >= 3) {
      reasons.push(`${commonHobbies.length}个共同兴趣`);
    } else if (commonHobbies.length > 0) {
      const commonHobbyText = commonHobbies.slice(0, 2).join('、');
      reasons.push(`共同爱好：${commonHobbyText}`);
    }
  }
  
  // 检查同城
  if (user.location && currentUser.value && currentUser.value.location && 
      (user.location === currentUser.value.location || 
       user.location.includes(currentUser.value.location) || 
       currentUser.value.location.includes(user.location))) {
    reasons.push("同城");
  }
  
  // 检查相近职业
  if (user.occupation && currentUser.value && currentUser.value.occupation &&
      (user.occupation === currentUser.value.occupation ||
       user.occupation.includes(currentUser.value.occupation) ||
       currentUser.value.occupation.includes(user.occupation))) {
    reasons.push("相似职业");
  }
  
  // 检查相近学历
  if (user.education && currentUser.value && currentUser.value.education &&
      Math.abs(user.education - currentUser.value.education) <= 1) {
    reasons.push("相近学历");
  }
  
  return reasons;
};

// 展开图片预览
const expandImage = (index, photoIndex) => {
  const user = visibleUsers.value[index];
  if (!user) return;
  
  const images = [user.avatar || '/images/default-avatar.png'];
  if (user.photos && Array.isArray(user.photos)) {
    images.push(...user.photos);
  }
  
  showImagePreview({
    images,
    startPosition: photoIndex,
    closeable: true
  });
};

// 卡片样式计算
const cardStyle = (index) => {
  if (index === 0 && isDragging.value) {
    const rotate = dragX.value * 0.1;
    const scale = Math.max(1 - Math.abs(dragX.value) * 0.001, 0.9);
    
    return {
      transform: `translate(${dragX.value}px, ${dragY.value}px) rotate(${rotate}deg) scale(${scale})`,
      zIndex: 100 - index
    };
  }
  
  return {
    transform: `scale(${1 - index * 0.05}) translateY(${-index * 10}px)`,
    zIndex: 100 - index,
    opacity: Math.max(1 - index * 0.15, 0.6)
  };
};

// 开始拖拽
const startDrag = (event, index) => {
  if (index !== 0) return;
  
  isDragging.value = true;
  
  // 记录开始位置
  if (event.touches) {
    // 触摸事件
    dragStartX.value = event.touches[0].clientX;
    dragStartY.value = event.touches[0].clientY;
  } else {
    // 鼠标事件
    dragStartX.value = event.clientX;
    dragStartY.value = event.clientY;
  }
  
  // 添加移动和结束事件监听
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('touchmove', onDrag);
  document.addEventListener('mouseup', endDrag);
  document.addEventListener('touchend', endDrag);
};

// 拖拽中
const onDrag = (event) => {
  if (!isDragging.value) return;
  
  // 阻止默认行为和事件传播
  event.preventDefault();
  
  // 计算位移
  let clientX, clientY;
  if (event.touches) {
    clientX = event.touches[0].clientX;
    clientY = event.touches[0].clientY;
  } else {
    clientX = event.clientX;
    clientY = event.clientY;
  }
  
  dragX.value = clientX - dragStartX.value;
  dragY.value = clientY - dragStartY.value;
  
  // 根据水平位移判断状态
  if (dragX.value > 100) {
    dragStatus.value = 'like';
  } else if (dragX.value < -100) {
    dragStatus.value = 'dislike';
  } else {
    dragStatus.value = null;
  }
};

// 结束拖拽
const endDrag = () => {
  if (!isDragging.value) return;
  
  // 移除事件监听
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', endDrag);
  document.removeEventListener('touchend', endDrag);
  
  // 判断是否超过阈值
  if (dragX.value > 100) {
    likeCurrentUser();
  } else if (dragX.value < -100) {
    dislikeCurrentUser();
  }
  
  // 重置状态
  isDragging.value = false;
  dragX.value = 0;
  dragY.value = 0;
  dragStatus.value = null;
};

// 喜欢当前用户
const likeCurrentUser = () => {
  if (visibleUsers.value.length === 0) return;
  
  // 获取当前显示的第一个用户
  const user = visibleUsers.value[0];
  console.log(`[当前操作] 喜欢用户: ${user.userId}(${user.nickname || user.username})`);
  
  // 先获取当前可见用户列表（做备份便于调试）
  const currentVisibleUsers = [...visibleUsers.value].map(u => ({id: u.userId, name: u.nickname || u.username}));
  console.log('[当前状态] 可见用户列表:', JSON.stringify(currentVisibleUsers));
  
  // 记录当前索引
  const oldIndex = currentIndex.value;
  console.log('[当前状态] 当前索引:', oldIndex);
  
  // 先触发喜欢事件，但不立即移动到下一张卡片
  emit('like', user);
  
  // 确保在DOM更新后再处理卡片移动
  setTimeout(() => {
    // 移动到下一个用户
    nextCard();
  }, 50);
};

// 不喜欢当前用户
const dislikeCurrentUser = () => {
  if (visibleUsers.value.length === 0) return;
  
  // 获取当前显示的第一个用户
  const user = visibleUsers.value[0];
  console.log(`[当前操作] 不喜欢用户: ${user.userId}(${user.nickname || user.username})`);
  
  // 先获取当前可见用户列表（做备份便于调试）
  const currentVisibleUsers = [...visibleUsers.value].map(u => ({id: u.userId, name: u.nickname || u.username}));
  console.log('[当前状态] 可见用户列表:', JSON.stringify(currentVisibleUsers));
  
  // 记录当前索引
  const oldIndex = currentIndex.value;
  console.log('[当前状态] 当前索引:', oldIndex);
  
  // 先触发不喜欢事件，但不立即移动到下一张卡片
  emit('dislike', user);
  
  // 确保在DOM更新后再处理卡片移动
  setTimeout(() => {
    // 移动到下一个用户
    nextCard();
  }, 50);
};

// 移动到下一张卡片
const nextCard = () => {
  // 保存操作前的状态
  const beforeMoveIndex = currentIndex.value;
  const beforeMoveUsers = [...visibleUsers.value].map(u => ({id: u.userId, name: u.nickname || u.username}));
  console.log('[nextCard] 移动前索引:', beforeMoveIndex);
  console.log('[nextCard] 移动前可见用户:', JSON.stringify(beforeMoveUsers));
  
  // 增加索引，移动到下一张卡片
  currentIndex.value++;
  console.log('[nextCard] 移动后新索引:', currentIndex.value);
  
  // 等待Vue更新DOM后再检查新的可见用户
  setTimeout(() => {
    const afterMoveUsers = visibleUsers.value.map(u => ({id: u.userId, name: u.nickname || u.username}));
    console.log('[nextCard] 移动后可见用户:', JSON.stringify(afterMoveUsers));
    
    // 检查是否需要加载更多
    if (currentIndex.value + 5 >= props.users.length) {
      console.log('[nextCard] 推荐用户即将用完，触发加载更多事件');
      emit('load-more');
    }
    
    // 如果没有更多卡片，触发empty事件
    if (currentIndex.value >= props.users.length) {
      console.log('[nextCard] 没有更多推荐用户，触发empty事件');
      emit('empty');
    }
  }, 50);
};

// 喜欢按钮处理
const handleLike = () => {
  likeCurrentUser();
};

// 不喜欢按钮处理
const handleDislike = () => {
  dislikeCurrentUser();
};

// 显示匹配成功
const showMatch = (user) => {
  matchedUser.value = user;
  showMatchPopup.value = true;
};

// 关闭匹配弹窗
const closeMatchPopup = () => {
  showMatchPopup.value = false;
};

// 开始聊天
const startChat = () => {
  if (!matchedUser.value) return;
  
  router.push(`/chat/${matchedUser.value.userId}`);
  showMatchPopup.value = false;
};

// 查看用户资料
const viewProfile = (user) => {
  if (!user || !user.userId) {
    console.error('无法查看资料：用户ID不存在', user);
    showToast('无法查看用户资料');
    return;
  }
  
  console.log('查看用户资料:', user.userId);
  // 使用正确的用户资料路径
  router.push(`/user/profile/${user.userId}`);
};

// 获取与指定用户的共同兴趣数量
const getCommonHobbiesCount = (user) => {
  if (!user.hobbies || !currentUser.value || !currentUser.value.hobbies) {
    return 0;
  }
  
  const userHobbies = getHobbyList(user.hobbies);
  return userHobbies.filter(hobby => isCommonHobby(hobby)).length;
};

// 获取相似度描述
const getSimilarityDesc = (similarity) => {
  if (similarity >= 90) {
    return "极其罕见的高匹配度！";
  } else if (similarity >= 80) {
    return "非常高的匹配度，非常合拍！";
  } else if (similarity >= 70) {
    return "很高的匹配度，很有共同点！";
  } else if (similarity >= 60) {
    return "较高的匹配度，值得了解！";
  } else if (similarity >= 50) {
    return "中等匹配度，可以一试！";
  } else {
    return "基础匹配，或许有惊喜！";
  }
};

// 组件卸载前
onBeforeUnmount(() => {
  // 移除所有事件监听
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('mouseup', endDrag);
  document.removeEventListener('touchend', endDrag);
});
</script>

<style scoped>
.swipe-cards-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.cards-stack {
  position: relative;
  width: 100%;
  max-width: 360px;
  height: 580px;
  margin: 0 auto;
}

.user-card {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, opacity 0.3s ease;
  top: 0;
  left: 0;
  display: flex;
  flex-direction: column;
}

.user-card:not(:first-child) {
  pointer-events: none;
}

.dragging {
  transition: none;
}

.card-content {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

/* 卡片轮播 */
.card-image-container {
  position: relative;
  width: 100%;
  height: 55%;
  overflow: hidden;
  background-color: #f5f5f5;
}

.card-swipe {
  height: 100%;
  --swipe-indicator-size: 8px;
  --swipe-indicator-margin: 8px;
  --swipe-indicator-active-background-color: #ff6b81;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-info-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.user-name {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.user-age {
  font-size: 18px;
  font-weight: 400;
  margin-left: 8px;
}

.user-location {
  margin: 8px 0 0;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.user-location .van-icon {
  margin-right: 6px;
}

/* 卡片详情 */
.card-details {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 匹配理由区域 */
.match-reasons {
  margin-bottom: 16px;
  padding: 16px;
  background-color: rgba(255, 107, 129, 0.1);
  border-radius: 12px;
  border: 1px dashed rgba(255, 107, 129, 0.3);
  box-shadow: 0 2px 8px rgba(255, 107, 129, 0.05);
}

.match-reasons-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.match-reasons-header .van-icon {
  margin-right: 8px;
}

.match-reasons-title {
  margin: 0;
  font-size: 16px;
  color: #ff6b81;
  font-weight: 600;
}

.reason-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.reason-tag {
  margin-right: 0;
  font-weight: 500;
  transition: transform 0.2s ease;
}

.reason-tag:hover {
  transform: scale(1.05);
}

/* 用户信息区域 */
.user-info {
  flex: 1;
}

.user-intro {
  margin: 0 0 16px;
  padding: 12px;
  background-color: #f8f8f8;
  border-radius: 8px;
  border-left: 3px solid #1989fa;
  font-size: 14px;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
}

.intro-text {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
}

.info-value {
  color: #333;
  flex: 1;
}

/* 兴趣标签 */
.hobbies-container {
  margin-top: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background-color: rgba(25, 137, 250, 0.05);
  border-radius: 12px;
}

.hobby-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
  color: #333;
}

.common-hobby-count {
  margin-left: auto;
  font-size: 14px;
  color: #ff6b81;
  font-weight: 600;
  background-color: rgba(255, 107, 129, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
}

.hobby-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.hobby-tag {
  margin-right: 0;
  transition: all 0.3s ease;
}

.common-hobby {
  background-color: rgba(255, 107, 129, 0.1);
  font-weight: 600;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(255, 107, 129, 0.15);
}

/* 相似度区域 */
.similarity-container {
  margin-top: 24px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 12px;
}

.similarity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.similarity-title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.similarity-value {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b81;
  background: linear-gradient(to right, #1989fa, #ff6b81);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.similarity-bar {
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.similarity-progress {
  position: absolute;
  height: 100%;
  background: linear-gradient(to right, #1989fa, #ff6b81);
  border-radius: 5px;
  transition: width 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

.similarity-desc {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
  font-style: italic;
  text-align: center;
}

/* 按钮容器 */
.profile-button-container {
  padding: 12px 16px;
  background-color: rgba(255, 255, 255, 0.95);
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.view-profile-btn {
  width: 100%;
  font-weight: 500;
}

/* 喜欢/不喜欢指示器 */
.like-indicator, .dislike-indicator {
  position: absolute;
  top: 40%;
  font-size: 30px;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 50%;
  width: 90px;
  height: 90px;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
}

.like-indicator {
  right: 0;
  transform: translateX(-50%);
}

.dislike-indicator {
  left: 0;
  transform: translateX(50%);
}

.like-indicator.visible, .dislike-indicator.visible {
  opacity: 1;
}

/* 底部操作按钮 */
.action-buttons {
  display: flex;
  justify-content: space-between;
  width: 180px;
  margin: 20px auto 0;
}

.action-btn {
  width: 60px;
  height: 60px;
  padding: 0;
  border: none;
}

.like-btn {
  background-color: #3cc952;
  color: white;
}

.dislike-btn {
  background-color: #ff4757;
  color: white;
}

.action-btn .van-icon {
  font-size: 24px;
}

/* 卡片动画 */
.card-enter-active, .card-leave-active {
  transition: all 0.3s;
}

.card-enter-from, .card-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

/* 空态样式 */
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* 匹配弹窗样式 */
.match-popup {
  text-align: center;
}

.match-img-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
}

.match-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f5f5f5;
}

.match-img:first-child {
  margin-right: -20px;
  z-index: 1;
}

.match-img:last-child {
  margin-left: -20px;
  z-index: 1;
}

.heart-icon {
  position: relative;
  z-index: 2;
  background: white;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.match-title {
  color: #ff6b81;
  margin-bottom: 8px;
}

.match-text {
  color: #666;
  margin-bottom: 20px;
}

.match-actions {
  display: flex;
  flex-direction: column;
}

/* 加载状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.loading-text {
  margin-top: 16px;
  color: #999;
}

@media (max-width: 360px) {
  .cards-stack {
    max-width: calc(100% - 20px);
    height: 520px;
  }
  
  .user-card {
    border-radius: 12px;
  }
  
  .user-name {
    font-size: 18px;
  }
  
  .user-age {
    font-size: 16px;
  }
}

.empty-intro {
  background-color: #f9f9f9;
  border-left-color: #ddd;
  color: #999;
}

.user-details-section {
  background-color: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
}

.no-details {
  color: #999;
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.empty-hobbies {
  background-color: #f9f9f9;
}

.empty-hobby-message {
  padding: 12px;
  text-align: center;
  color: #999;
  font-style: italic;
}

/* 在线状态指示器样式 */
.status-indicator {
  position: absolute;
  top: 12px;
  right: 12px;
  border-radius: 16px;
  padding: 5px 12px;
  display: flex;
  align-items: center;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.online {
  background-color: rgba(19, 194, 36, 0.9);
}

.offline {
  background-color: rgba(150, 150, 150, 0.8);
}

.pulse-circle {
  width: 10px;
  height: 10px;
  background-color: white;
  border-radius: 50%;
  margin-right: 6px;
  position: relative;
  animation: pulse 1.5s infinite;
}

.offline-circle {
  width: 10px;
  height: 10px;
  background-color: white;
  border-radius: 50%;
  margin-right: 6px;
}

.status-text {
  color: white;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.online-tag {
  position: relative;
  overflow: hidden;
  font-weight: bold;
  background-color: rgba(19, 194, 36, 0.9) !important;
}

.offline-tag {
  position: relative;
  overflow: hidden;
  font-weight: bold;
  background-color: rgba(150, 150, 150, 0.8) !important;
  color: white !important;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7);
  }
  70% {
    transform: scale(1.1);
    box-shadow: 0 0 0 6px rgba(76, 175, 80, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
  }
}

@keyframes shine {
  0% {
    left: -100%;
  }
  20% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}
</style> 