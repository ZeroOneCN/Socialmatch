<template>
  <div class="user-card" :style="cardStyle">
    <div class="card-image" :style="{ backgroundImage: `url(${user.avatar || '/avatar-placeholder.png'})` }">
      <div class="card-gradient"></div>
    </div>
    
    <div class="card-content">
      <div class="user-info">
        <h2 class="user-name">{{ user.nickname || user.username }} <span class="user-age" v-if="user.age">{{ user.age }}岁</span></h2>
        <p class="user-occupation" v-if="user.occupation">{{ user.occupation }}</p>
        <p class="user-location" v-if="user.location">{{ user.location }}</p>
      </div>
      
      <div class="user-intro" v-if="user.selfIntro">
        <p>{{ user.selfIntro }}</p>
      </div>
      
      <div class="user-tags" v-if="user.hobbies">
        <span class="tag" v-for="(hobby, index) in hobbiesList" :key="index">{{ hobby }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  index: {
    type: Number,
    default: 0
  },
  active: {
    type: Boolean,
    default: false
  }
})

// 将爱好字符串拆分为标签列表
const hobbiesList = computed(() => {
  if (!props.user.hobbies) return []
  return props.user.hobbies.split(',').map(hobby => hobby.trim()).filter(hobby => hobby)
})

// 卡片样式，根据index和active状态设置
const cardStyle = computed(() => {
  return {
    zIndex: 1000 - props.index,
    opacity: props.active ? 1 : 0.8,
    transform: props.active ? 'scale(1) translateY(0)' : `scale(0.95) translateY(${props.index * 10}px)`
  }
})
</script>

<style scoped>
.user-card {
  position: relative;
  width: 100%;
  height: 70vh;
  max-height: 600px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background-color: #fff;
  transition: all 0.3s ease;
}

.card-image {
  position: relative;
  width: 100%;
  height: 70%;
  background-size: cover;
  background-position: center;
}

.card-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 40%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
}

.card-content {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  color: #fff;
}

.user-info {
  margin-bottom: 10px;
}

.user-name {
  margin: 0 0 5px;
  font-size: 1.5rem;
  font-weight: 600;
}

.user-age {
  font-size: 1rem;
  font-weight: 400;
  opacity: 0.8;
  margin-left: 5px;
}

.user-occupation, .user-location {
  margin: 3px 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.user-intro {
  margin: 10px 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 3px 10px;
  border-radius: 15px;
  font-size: 0.8rem;
}
</style> 