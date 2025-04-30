<template>
  <div class="post-card">
    <div class="post-header">
      <div class="user-info" @click="goToUserProfile">
        <img :src="post.avatar || '/images/default-avatar.png'" class="avatar" :alt="post.username" />
        <div class="user-detail">
          <div class="username">{{ post.nickname || post.username }}</div>
          <div class="post-time">{{ formatTime(post.createTime) }}</div>
        </div>
      </div>
      <div class="more-actions">
        <van-icon name="ellipsis" @click="showActions" />
      </div>
    </div>
    
    <div class="post-content" @click="goToPostDetail">
      <p class="text-content">{{ post.content }}</p>
      
      <!-- 图片内容 -->
      <div class="image-container" v-if="post.images && post.images.length > 0">
        <div :class="['image-grid', `grid-${Math.min(post.images.length, 4)}`]">
          <div 
            v-for="(image, index) in displayImages" 
            :key="index" 
            class="image-item"
            @click.stop="previewImages(index)"
          >
            <img :src="image" :alt="`图片${index + 1}`" />
            
            <!-- 如果有更多图片但只显示部分 -->
            <div class="more-overlay" v-if="index === 3 && post.images.length > 4">
              <span>+{{ post.images.length - 4 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="post-actions">
      <div class="action-button" @click="handleLike">
        <van-icon :name="post.isLiked ? 'like' : 'like-o'" :color="post.isLiked ? '#f44336' : ''" size="20px" />
        <span>{{ post.likeCount || 0 }}</span>
      </div>
      <div class="action-button" @click="handleComment">
        <van-icon name="chat-o" size="20px" />
        <span>{{ post.commentCount || 0 }}</span>
      </div>
      <div class="action-button" @click="handleShare">
        <van-icon name="share-o" size="20px" />
        <span>{{ post.shareCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { showImagePreview } from 'vant';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['like', 'comment', 'share']);

const router = useRouter();

// 处理图片显示，最多显示4张
const displayImages = computed(() => {
  if (!props.post.images) return [];
  return props.post.images.slice(0, 4);
});

// 格式化发布时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  try {
    const date = new Date(timestamp);
    return formatDistanceToNow(date, { addSuffix: true, locale: zhCN });
  } catch (error) {
    console.error('时间格式化错误', error);
    return '';
  }
};

// 预览图片
const previewImages = (startIndex) => {
  showImagePreview({
    images: props.post.images,
    startPosition: startIndex,
    closeable: true,
    closeOnPopstate: true
  });
};

// 显示更多操作菜单
const showActions = () => {
  // 实现更多操作菜单，可以根据需要添加
};

// 点赞操作
const handleLike = () => {
  emit('like', props.post);
};

// 评论操作
const handleComment = () => {
  emit('comment', props.post);
};

// 分享操作
const handleShare = () => {
  emit('share', props.post);
};

// 跳转到用户详情页
const goToUserProfile = () => {
  router.push(`/user/${props.post.userId}`);
};

// 跳转到帖子详情页
const goToPostDetail = () => {
  router.push(`/post/${props.post.postId}`);
};
</script>

<style scoped>
.post-card {
  background-color: #ffffff;
  border-radius: 8px;
  margin-bottom: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.more-actions {
  color: #666;
  cursor: pointer;
}

.post-content {
  cursor: pointer;
}

.text-content {
  font-size: 15px;
  line-height: 1.5;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-container {
  margin-bottom: 12px;
}

.image-grid {
  display: grid;
  grid-gap: 4px;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid-4 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.image-item {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.more-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 24px;
  font-weight: bold;
}

.post-actions {
  display: flex;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.action-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 0;
}

.action-button:hover {
  background-color: #f8f8f8;
  border-radius: 4px;
}
</style> 