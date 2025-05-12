<!-- 关注列表页面 -->
<template>
  <div class="following-list-page">
    <van-nav-bar
      title="关注列表"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="list-container">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div class="user-item" v-for="user in followingList" :key="user.userId">
          <div class="user-info" @click="goToUserProfile(user.userId)">
            <img :src="user.avatar" class="avatar" />
            <div class="user-detail">
              <div class="nickname">{{ user.nickname }}</div>
              <div class="intro">{{ user.selfIntro }}</div>
            </div>
          </div>
          <van-button 
            size="small" 
            :type="user.isFollowing ? 'default' : 'primary'"
            :loading="user.followLoading"
            @click="toggleFollow(user)"
          >
            {{ user.isFollowing ? '已关注' : '关注' }}
          </van-button>
        </div>
      </van-list>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showSuccessToast } from 'vant';
import * as followApi from '../../api/follow';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const finished = ref(false);
const followingList = ref([]);
const currentPage = ref(1);
const pageSize = 20;

// 从路由参数获取用户ID
const userId = ref(route.params.id);

// 加载数据
const onLoad = async () => {
  try {
    if (!userId.value) {
      console.error('缺少用户ID参数');
      showToast('参数错误');
      finished.value = true;
      return;
    }
    
    console.log(`加载关注列表，用户ID: ${userId.value}, 页码: ${currentPage.value}`);
    loading.value = true;
    
    const response = await followApi.getFollowingList(userId.value, currentPage.value, pageSize);
    console.log('关注列表响应:', response);
    
    if (!response || !response.data) {
      console.error('API响应数据为空:', response);
      showToast('获取数据失败');
      finished.value = true;
      return;
    }
    
    const { records, total } = response.data;
    console.log('解析的数据:', { records, total });
    
    if (!Array.isArray(records)) {
      console.error('API响应数据格式错误:', records);
      showToast('数据格式错误');
      finished.value = true;
      return;
    }
    
    // 处理返回的数据，确保所有必要字段都存在
    const processedUsers = records.map(user => {
      console.log('处理用户数据:', user);
      return {
        userId: user.userId,
        nickname: user.nickname || user.username || '未设置昵称',
        avatar: user.avatar || '/avatar-placeholder.png',
        selfIntro: user.selfIntro || '这个人很懒，什么都没写~',
        isFollowing: true, // 关注列表中的用户必然是已关注的
        followLoading: false
      };
    });
    
    console.log('处理后的用户列表:', processedUsers);
    followingList.value.push(...processedUsers);
    
    // 判断是否加载完成
    if (followingList.value.length >= (total || 0)) {
      finished.value = true;
    } else {
      currentPage.value += 1;
    }
  } catch (error) {
    console.error('获取关注列表失败:', error);
    showToast('获取关注列表失败');
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

// 切换关注状态
const toggleFollow = async (user) => {
  if (user.followLoading) return;
  
  user.followLoading = true;
  try {
    if (user.isFollowing) {
      await followApi.unfollowUser(user.userId);
      showSuccessToast('已取消关注');
    } else {
      await followApi.followUser(user.userId);
      showSuccessToast('关注成功');
    }
    user.isFollowing = !user.isFollowing;
  } catch (error) {
    console.error('操作失败', error);
    showToast('操作失败，请稍后再试');
  } finally {
    user.followLoading = false;
  }
};

// 跳转到用户资料页
const goToUserProfile = (userId) => {
  router.push(`/user/profile/${userId}`);
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 初始化时检查路由参数
onMounted(() => {
  console.log('关注列表页面加载，路由参数:', route.params);
  if (!userId.value) {
    console.error('缺少用户ID参数');
    showToast('参数错误');
    router.back();
  }
});
</script>

<style scoped>
.following-list-page {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.list-container {
  padding: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  margin-right: 12px;
  cursor: pointer;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.user-detail {
  flex: 1;
}

.nickname {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.intro {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}
</style> 