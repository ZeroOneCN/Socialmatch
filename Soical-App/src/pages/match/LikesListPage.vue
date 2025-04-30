<template>
  <div class="likes-list-page">
    <van-nav-bar
      title="喜欢记录"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="tabs-container">
      <van-tabs 
        v-model:active="activeTab" 
        sticky 
        swipeable 
        animated 
        class="like-tabs" 
        background="#ffffff"
        title-active-color="#ff6b81"
        color="#ff6b81"
        title-inactive-color="#333333"
        :border="true"
      >
        <van-tab title="我喜欢的" title-style="font-weight: bold; font-size: 15px;">
          <div class="tab-content">
            <div v-if="loading.likesGiven" class="loading-container">
              <van-loading type="spinner" color="#1989fa" size="24px" />
              <span class="loading-text">加载中...</span>
            </div>
            
            <div v-else-if="likesGiven.length === 0" class="empty-container">
              <van-empty description="暂无喜欢的用户" image="search" />
              <van-button round type="primary" size="small" @click="goToRecommend">去查看推荐</van-button>
            </div>
            
            <div v-else class="card-list">
              <van-swipe-cell v-for="user in likesGiven" :key="user.userId">
                <div class="user-card">
                  <div class="user-card-content" @click="viewUserProfile(user.userId)">
                    <div class="avatar-container">
                      <img :src="user.avatar || '/images/default-avatar.png'" class="avatar" />
                      <div v-if="user.matchStatus === 1" class="match-badge">已匹配</div>
                    </div>
                    <div class="user-info">
                      <div class="username">{{ user.nickname || user.username }}</div>
                      <div class="status-text">
                        <span v-if="user.matchStatus === 1" class="status matched">
                          <van-icon name="like" color="#ff6b81" />互相喜欢
                        </span>
                        <span v-else-if="user.matchStatus === 0" class="status pending">
                          <van-icon name="clock" />等待回应
                        </span>
                      </div>
                      <div class="user-tags" v-if="user.tags && user.tags.length">
                        <span class="tag" v-for="tag in user.tags.slice(0, 3)" :key="tag">{{ tag }}</span>
                      </div>
                    </div>
                    <div class="action-buttons">
                      <van-button 
                        v-if="user.matchStatus === 1" 
                        size="small" 
                        type="primary" 
                        icon="chat-o"
                        round
                        @click.stop="startChat(user.userId)"
                      >
                        聊天
                      </van-button>
                      <van-button 
                        v-else 
                        size="small" 
                        plain 
                        type="primary" 
                        disabled
                        round
                      >
                        等待中
                      </van-button>
                    </div>
                  </div>
                </div>
                
                <template #right>
                  <van-button square type="danger" text="取消喜欢" @click="dislikeUser(user.userId)" />
                </template>
              </van-swipe-cell>
            </div>
          </div>
        </van-tab>
        
        <van-tab title="喜欢我的" title-style="font-weight: bold; font-size: 15px;">
          <div class="tab-content">
            <div v-if="loading.likesReceived" class="loading-container">
              <van-loading type="spinner" color="#1989fa" size="24px" />
              <span class="loading-text">加载中...</span>
            </div>
            
            <div v-else-if="likesReceived.length === 0" class="empty-container">
              <van-empty description="暂无人喜欢你" image="search" />
              <van-button round type="primary" size="small" @click="goToFrequency">去完善频率</van-button>
            </div>
            
            <div v-else class="card-list">
              <div v-for="user in likesReceived" :key="user.userId" class="user-card">
                <div class="user-card-content" @click="viewUserProfile(user.userId)">
                  <div class="avatar-container">
                    <img :src="user.avatar || '/images/default-avatar.png'" class="avatar" />
                    <div v-if="user.matchStatus === 1" class="match-badge">已匹配</div>
                  </div>
                  <div class="user-info">
                    <div class="username">{{ user.nickname || user.username }}</div>
                    <div class="status-text">
                      <span v-if="user.matchStatus === 1" class="status matched">
                        <van-icon name="like" color="#ff6b81" />互相喜欢
                      </span>
                      <span v-else-if="user.matchStatus === 0" class="status waiting">
                        <van-icon name="question-o" />等待你回应
                      </span>
                    </div>
                    <div class="user-tags" v-if="user.tags && user.tags.length">
                      <span class="tag" v-for="tag in user.tags.slice(0, 3)" :key="tag">{{ tag }}</span>
                    </div>
                  </div>
                  
                  <div class="action-buttons">
                    <template v-if="user.matchStatus === 1">
                      <van-button 
                        size="small" 
                        type="primary" 
                        icon="chat-o"
                        round
                        @click.stop="startChat(user.userId)"
                      >
                        聊天
                      </van-button>
                    </template>
                    <template v-else-if="user.matchStatus === 0 && !user.isLiked">
                      <div class="response-buttons">
                        <van-button 
                          size="small" 
                          type="primary"
                          round
                          icon="like-o"
                          @click.stop="likeUser(user.userId)"
                          :loading="likeLoading === user.userId"
                        >
                          喜欢
                        </van-button>
                        <van-button 
                          size="small" 
                          type="danger"
                          plain
                          round
                          icon="close"
                          @click.stop="dislikeUser(user.userId)"
                          :loading="dislikeLoading === user.userId"
                        >
                          拒绝
                        </van-button>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showLoadingToast, closeToast } from 'vant';
import * as matchApi from '../../api/match';

const router = useRouter();
const activeTab = ref(0);

// 我喜欢的用户列表
const likesGiven = ref([]);
// 喜欢我的用户列表
const likesReceived = ref([]);

// 加载状态
const loading = ref({
  likesGiven: false,
  likesReceived: false,
});

// 操作加载状态
const likeLoading = ref(null);
const dislikeLoading = ref(null);

// 获取我喜欢的用户列表
const fetchLikesGiven = async () => {
  loading.value.likesGiven = true;
  try {
    const response = await matchApi.getLikesGiven();
    likesGiven.value = response.data || [];
  } catch (error) {
    console.error('获取喜欢的用户列表失败', error);
    showToast('获取数据失败，请稍后重试');
  } finally {
    loading.value.likesGiven = false;
  }
};

// 获取喜欢我的用户列表
const fetchLikesReceived = async () => {
  loading.value.likesReceived = true;
  try {
    const response = await matchApi.getLikesReceived();
    likesReceived.value = response.data || [];
  } catch (error) {
    console.error('获取喜欢我的用户列表失败', error);
    showToast('获取数据失败，请稍后重试');
  } finally {
    loading.value.likesReceived = false;
  }
};

// 喜欢用户
const likeUser = async (userId) => {
  likeLoading.value = userId;
  try {
    const response = await matchApi.likeUser(userId);
    if (response.data?.matched) {
      showToast({
        type: 'success',
        message: '已匹配成功！现在可以开始聊天'
      });
    } else {
      showToast({
        type: 'success',
        message: '已喜欢该用户'
      });
    }
    
    // 重新加载数据
    await Promise.all([
      fetchLikesGiven(),
      fetchLikesReceived()
    ]);
  } catch (error) {
    console.error('喜欢用户失败', error);
    showToast('操作失败，请稍后重试');
  } finally {
    likeLoading.value = null;
  }
};

// 不喜欢用户
const dislikeUser = async (userId) => {
  dislikeLoading.value = userId;
  try {
    await matchApi.dislikeUser(userId);
    showToast({
      type: 'success',
      message: '已拒绝该用户'
    });
    
    // 重新加载列表数据
    await Promise.all([
      fetchLikesGiven(),
      fetchLikesReceived()
    ]);
  } catch (error) {
    console.error('拒绝用户失败', error);
    showToast('操作失败，请稍后重试');
  } finally {
    dislikeLoading.value = null;
  }
};

// 查看用户资料
const viewUserProfile = (userId) => {
  router.push(`/user/${userId}`);
};

// 开始聊天
const startChat = async (userId) => {
  // 显示加载提示
  showLoadingToast({
    message: '准备聊天...',
    forbidClick: true,
    duration: 0
  });
  
  try {
    // 先创建或获取与该用户的会话
    const response = await matchApi.createOrGetConversation(userId);
    
    if (response && response.data && response.data.conversationId) {
      // 获取到会话ID后跳转到聊天页面
      const conversationId = response.data.conversationId;
      closeToast();
      router.push(`/chat/${conversationId}`);
    } else {
      throw new Error('获取会话失败');
    }
  } catch (error) {
    console.error('创建会话失败:', error);
    closeToast();
    showToast('创建会话失败，请稍后重试');
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 去推荐页面
const goToRecommend = () => {
  router.push('/recommend');
};

// 去频率设置页面
const goToFrequency = () => {
  router.push('/settings/frequency');
};

// 组件加载时获取数据
onMounted(() => {
  // 同时获取两个Tab数据
  Promise.all([
    fetchLikesGiven(),
    fetchLikesReceived()
  ]);
});
</script>

<style scoped>
.likes-list-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}

/* 确保文本颜色可见 */
.user-card {
  color: #323233;
}

.username {
  color: #323233;
  font-weight: 500;
}

.status-text {
  color: #323233;
}

.user-tags .tag {
  color: #323233;
}

.empty-container {
  color: #323233;
}

.loading-text {
  color: #323233;
}

/* 使用深度选择器确保Vant组件内的文本可见 */
:deep(.van-tab__text) {
  color: #323233 !important;
}

:deep(.van-empty__description) {
  color: #323233 !important;
}

:deep(.van-nav-bar__title) {
  color: #323233 !important;
}

:deep(.van-button__text) {
  color: inherit !important;
}

:deep(.van-cell) {
  color: #323233 !important;
}

:deep(.van-swipe-cell) {
  color: #323233 !important;
}

:deep(.status.matched),
:deep(.status.pending),
:deep(.status.waiting) {
  color: #323233 !important;
}

/* 添加tabs容器样式 */
.tabs-container {
  position: relative;
  z-index: 10;
  margin-bottom: 8px;
}

/* 自定义标签页样式 */
:deep(.like-tabs .van-tabs__wrap) {
  height: 50px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid #eee;
  z-index: 9;
  background-color: white;
}

:deep(.like-tabs .van-tab) {
  padding: 0 24px;
  font-size: 16px;
  line-height: 50px;
}

:deep(.like-tabs .van-tabs__line) {
  background-color: #ff6b81;
  height: 3px;
  width: 30px !important;
  border-radius: 3px;
  bottom: 12px;
}

.tab-content {
  padding: 12px;
}

.loading-container, .empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 16px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.user-card-content {
  padding: 16px;
  display: flex;
  align-items: center;
  position: relative;
}

.avatar-container {
  position: relative;
  margin-right: 12px;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
}

.match-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background-color: #ff6b81;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  white-space: nowrap;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status.matched {
  color: #ff6b81;
}

.status.pending, .status.waiting {
  color: #ff9800;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.tag {
  background-color: #f0f2f5;
  color: #666;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
}

.action-buttons {
  margin-left: auto;
  display: flex;
  flex-direction: column;
}

.response-buttons {
  display: flex;
  gap: 8px;
}

/* 针对滑动单元格的右侧按钮样式 */
:deep(.van-swipe-cell__right) {
  display: flex;
  align-items: center;
  height: 100%;
}

:deep(.van-swipe-cell__right .van-button) {
  height: 100%;
}
</style> 