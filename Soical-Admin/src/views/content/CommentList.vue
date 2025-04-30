<template>
  <div class="comment-list-container">
    <h1 class="page-title">评论管理</h1>
    <div class="card-container">
      <p>这是评论列表页面，将在后续实现</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  console.log('评论列表页面初始化')
  // 在此处获取评论列表数据
})
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.comment-list-container {
  padding: 20px;
}

.page-title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
  color: $text-color-primary;
}
</style> 