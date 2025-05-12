<template>
  <div class="user-profile-page">
    <van-nav-bar
      title="用户资料"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="profile-container" v-if="userProfile">
      <!-- 用户基本信息 -->
      <div class="user-card">
        <div class="user-info">
          <img :src="userProfile.avatar || '/avatar-placeholder.png'" class="avatar" alt="用户头像" />
          <div class="user-details">
            <h2 class="nickname">
              {{ userProfile.nickname || userProfile.username }}
              <!-- 认证标识 -->
              <span class="verification-badge identity" 
                :class="{'badge-verified': userProfile.verifications?.identityStatus === 'approved', 'badge-unverified': userProfile.verifications?.identityStatus !== 'approved'}" 
                title="身份认证状态">
                <van-icon name="certificate" />
              </span>
              <span class="verification-badge education" 
                :class="{'badge-verified': userProfile.verifications?.educationStatus === 'approved', 'badge-unverified': userProfile.verifications?.educationStatus !== 'approved'}" 
                title="教育认证状态">
                <van-icon name="award" />
              </span>
            </h2>
            <p class="username" v-if="userProfile.username">@{{ userProfile.username }}</p>
            
            <div class="follow-button" v-if="userId !== currentUserId">
              <van-button 
                size="small" 
                :type="isFollowing ? 'default' : 'primary'" 
                :loading="followLoading"
                @click="toggleFollow"
              >
                {{ isFollowing ? '已关注' : '关注' }}
              </van-button>
            </div>
          </div>
        </div>
        
        <!-- 用户介绍 -->
        <div class="user-bio" v-if="userProfile.selfIntro">
          <p>{{ userProfile.selfIntro }}</p>
        </div>
      </div>
      
      <!-- 用户数据统计 -->
      <div class="stats-card">
        <div class="stats-row">
          <div class="stat-item" @click="goToUserPosts">
            <div class="stat-value">{{ userStats.posts || 0 }}</div>
            <div class="stat-label">动态</div>
          </div>
          <div class="stat-item" @click="goToFollowers">
            <div class="stat-value">{{ userStats.followers || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item" @click="goToFollowing">
            <div class="stat-value">{{ userStats.following || 0 }}</div>
            <div class="stat-label">关注</div>
          </div>
        </div>
      </div>
      
      <!-- 用户标签 -->
      <div class="feature-card" v-if="hobbiesList.length > 0">
        <div class="card-header">
          <van-icon name="bookmark-o" class="card-icon" />
          <span class="card-title">兴趣爱好</span>
        </div>
        <div class="tags-container">
          <span class="tag" v-for="(hobby, index) in hobbiesList" :key="index">{{ hobby }}</span>
        </div>
      </div>
      
      <!-- 用户详细信息 -->
      <div class="feature-card">
        <div class="card-header">
          <van-icon name="contact" class="card-icon" />
          <span class="card-title">基本资料</span>
        </div>
        <div class="details-list">
          <div class="detail-item" v-if="userProfile.gender !== undefined && userProfile.gender !== null">
            <van-icon name="user-circle-o" />
            <span>{{ genderText }}</span>
          </div>
          <div class="detail-item" v-if="userProfile.birthday">
            <van-icon name="birthday-cake-o" />
            <span>{{ formatBirthday(userProfile.birthday) }}</span>
          </div>
          <div class="detail-item" v-if="userProfile.city">
            <van-icon name="location-o" />
            <span>{{ userProfile.city }}</span>
          </div>
          <div class="detail-item" v-if="userProfile.occupation">
            <van-icon name="coupon-o" />
            <span>{{ userProfile.occupation }}</span>
          </div>
          <div class="detail-item" v-if="userProfile.education">
            <van-icon name="certificate" />
            <span>{{ educationText }}</span>
          </div>
          <!-- 当没有任何基本资料时显示默认项 -->
          <div class="detail-item empty-item" v-if="!hasAnyProfileDetail">
            <span class="empty-text">暂无基本资料</span>
          </div>
        </div>
      </div>
      
      <!-- 用户动态 -->
      <div class="feature-card">
        <div class="card-header">
          <van-icon name="comment-o" class="card-icon" />
          <span class="card-title">用户动态</span>
          <div class="view-all" @click="goToUserPosts">查看全部</div>
        </div>
        
        <div class="posts-content">
          <div class="empty-posts" v-if="!userPosts.length">
            <van-empty description="暂无动态" />
          </div>
          
          <post-card 
            v-for="post in userPosts" 
            :key="post.id" 
            :post="post"
            @like="handleLike"
            @comment="handleComment"
            @share="handleShare"
          />
          
          <div class="view-more" v-if="userPosts.length > 0" @click="goToUserPosts">
            查看更多动态
          </div>
        </div>
      </div>
      
      <!-- 底部留白 -->
      <div class="bottom-space"></div>
    </div>
    
    <div class="loading-container" v-else-if="loading">
      <van-loading type="spinner" color="#1989fa" />
    </div>
    
    <div class="error-container" v-else>
      <van-empty image="error" description="获取用户资料失败" />
      <van-button type="primary" @click="fetchUserProfile">重试</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showSuccessToast } from 'vant';
import { useUserStore } from '../../stores/user';
import * as userApi from '../../api/user';
import * as postApi from '../../api/post';
import * as followApi from '../../api/follow';
import PostCard from '../../components/post/PostCard.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 用户ID
const userId = computed(() => route.params.id);
const currentUserId = computed(() => userStore.userId);

// 数据状态
const loading = ref(true);
const userProfile = ref(null);
const userStats = ref({
  posts: 0,
  followers: 0,
  following: 0
});
const userPosts = ref([]);
const isFollowing = ref(false);
const followLoading = ref(false);

// 获取用户资料
const fetchUserProfile = async () => {
  if (!userId.value) return;
  
  loading.value = true;
  try {
    // 先重置数据，避免显示旧数据
    userProfile.value = null;
    userStats.value = { posts: 0, followers: 0, following: 0 };
    userPosts.value = [];
    isFollowing.value = false;

    // 获取用户基本信息
    const profileResponse = await userApi.getUserProfile(userId.value);
    if (profileResponse?.code === 200 && profileResponse.data) {
      userProfile.value = profileResponse.data;
      
      // 获取认证状态
      try {
        const { checkUserVerified } = await import('../../api/verification');
        
        // 获取身份认证状态
        const identityResponse = await checkUserVerified(userId.value, 'identity');
        if (identityResponse?.data) {
          if (!userProfile.value.verifications) {
            userProfile.value.verifications = {};
          }
          userProfile.value.verifications.identityStatus = identityResponse.data.status;
        }
        
        // 获取教育认证状态
        const educationResponse = await checkUserVerified(userId.value, 'education');
        if (educationResponse?.data) {
          if (!userProfile.value.verifications) {
            userProfile.value.verifications = {};
          }
          userProfile.value.verifications.educationStatus = educationResponse.data.status;
        }
      } catch (verificationError) {
        console.error('获取认证状态失败', verificationError);
      }
    } else {
      throw new Error(profileResponse?.message || '获取用户资料失败');
    }
    
    // 获取用户统计数据
    const statsResponse = await userApi.getUserStats(userId.value);
    if (statsResponse?.code === 200 && statsResponse.data) {
      userStats.value = {
        ...userStats.value,
        ...statsResponse.data
      };
    }
    
    // 获取用户最新动态
    const postsResponse = await postApi.getUserPosts(userId.value, {
      page: 1,
      pageSize: 3
    });
    if (postsResponse?.code === 200 && postsResponse.data?.records) {
      userPosts.value = postsResponse.data.records;
    }
    
    // 如果不是当前用户，检查是否已关注
    if (userId.value !== currentUserId.value) {
      const followResponse = await followApi.checkFollowStatus(userId.value);
      if (followResponse?.code === 200) {
        isFollowing.value = followResponse.data;
      }
    }
  } catch (error) {
    console.error('获取用户资料失败:', error);
    showToast('获取用户资料失败');
  } finally {
    loading.value = false;
  }
};

// 将爱好字符串拆分为标签列表
const hobbiesList = computed(() => {
  if (!userProfile.value?.hobbies) return [];
  return userProfile.value.hobbies.split(',').map(hobby => hobby.trim()).filter(hobby => hobby);
});

// 性别文本
const genderText = computed(() => {
  if (!userProfile.value) return '';
  if (userProfile.value.gender === 1) return '男';
  if (userProfile.value.gender === 2) return '女';
  return '保密';
});

// 学历文本
const educationOptions = ['高中及以下', '大专', '本科', '硕士', '博士及以上'];
const educationText = computed(() => {
  if (!userProfile.value?.education || userProfile.value.education < 1) return '';
  return educationOptions[userProfile.value.education - 1] || '';
});

// 判断是否有任何基本资料填写
const hasAnyProfileDetail = computed(() => {
  if (!userProfile.value) return false;
  return (
    userProfile.value.gender !== undefined && userProfile.value.gender !== null ||
    userProfile.value.birthday ||
    userProfile.value.city ||
    userProfile.value.occupation ||
    userProfile.value.education
  );
});

// 格式化生日
const formatBirthday = (birthday) => {
  if (!birthday) return '';
  const date = new Date(birthday);
  if (isNaN(date.getTime())) {
    // 如果不是有效日期，直接返回原字符串
    return birthday;
  }
  
  const now = new Date();
  const age = now.getFullYear() - date.getFullYear();
  
  // 格式化为YYYY-MM-DD并添加年龄
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  
  return `${year}-${month}-${day} (${age}岁)`;
};

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    console.log('用户ID变化，重新加载资料:', newId);
    fetchUserProfile();
  }
}, { immediate: true });

// 关注/取消关注
const toggleFollow = async () => {
  if (followLoading.value) return;
  
  followLoading.value = true;
  try {
    const response = await (isFollowing.value 
      ? followApi.unfollowUser(userId.value)
      : followApi.followUser(userId.value));
      
    if (response.code === 200) {
      isFollowing.value = !isFollowing.value;
      // 更新统计数据
      const newStats = { ...userStats.value };
      if (isFollowing.value) {
        newStats.followers++;
      } else {
        newStats.followers = Math.max(0, newStats.followers - 1);
      }
      userStats.value = newStats;
      
      showSuccessToast(isFollowing.value ? '关注成功' : '已取消关注');
    } else {
      throw new Error(response.message);
    }
  } catch (error) {
    console.error('操作失败:', error);
    showToast(error.message || '操作失败');
  } finally {
    followLoading.value = false;
  }
};

// 页面跳转方法
const goBack = () => router.back();
const goToUserPosts = () => router.push(`/user/${userId.value}/posts`);
const goToFollowers = () => router.push(`/user/${userId.value}/followers`);
const goToFollowing = () => router.push(`/user/${userId.value}/following`);

// 动态交互方法
const handleLike = async (postId) => {
  // 实现点赞逻辑
};

const handleComment = (postId) => {
  router.push(`/post/${postId}`);
};

const handleShare = (post) => {
  // 实现分享逻辑
};
</script>

<style scoped>
.user-profile-page {
  padding-bottom: 60px;
  background-color: #f8f8f8;
  min-height: 100vh;
}

.profile-container {
  padding: 16px;
}

/* 用户卡片样式 */
.user-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  padding: 20px;
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 16px;
  border: 2px solid #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
  position: relative;
}

.nickname {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.username {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.follow-button {
  position: absolute;
  right: 0;
  top: 8px;
}

.user-bio {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 统计卡片样式 */
.stats-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  padding: 16px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  cursor: pointer;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 功能卡片样式 */
.feature-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  overflow: hidden;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  position: relative;
}

.card-icon {
  font-size: 18px;
  margin-right: 8px;
  color: #1989fa;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.view-all {
  font-size: 14px;
  color: #1989fa;
  cursor: pointer;
}

/* 标签容器样式 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  padding: 16px;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 6px 12px;
  background-color: #f0f8ff;
  color: #1989fa;
  border-radius: 16px;
  font-size: 12px;
}

/* 基本资料列表样式 */
.details-list {
  padding: 12px 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #333;
}

.detail-item:not(:last-child) {
  border-bottom: 1px solid #f6f6f6;
}

.detail-item .van-icon {
  font-size: 18px;
  margin-right: 12px;
  color: #1989fa;
}

.empty-item {
  justify-content: center;
  color: #999;
  padding: 16px 0;
}

/* 动态内容样式 */
.posts-content {
  padding: 16px;
}

.empty-posts {
  padding: 20px 0;
}

.view-more {
  text-align: center;
  padding: 12px;
  color: #1989fa;
  font-size: 14px;
  cursor: pointer;
}

/* 操作按钮样式 */
.action-buttons {
  display: none;  /* 隐藏底部的操作按钮区域 */
}

/* 加载容器样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

/* 错误容器样式 */
.error-container {
  padding: 40px 16px;
  text-align: center;
}

/* 底部留白 */
.bottom-space {
  height: 40px;
}

.verification-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 5px;
  font-size: 16px;
  color: #fff;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  vertical-align: middle;
}

.verification-badge.identity.badge-verified {
  background-color: #52c41a;
}

.verification-badge.education.badge-verified {
  background-color: #722ed1;
}

.verification-badge.badge-unverified {
  background-color: #d9d9d9;
  color: #ffffff;
}
</style> 