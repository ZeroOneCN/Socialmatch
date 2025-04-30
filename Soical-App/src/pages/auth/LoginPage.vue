<template>
  <div class="login-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="登录"
      left-arrow
      @click-left="goBack"
      :border="false"
      class="nav-bar"
    />

    <div class="login-container">
      <!-- 登录表单 -->
      <van-form @submit="onSubmit" class="login-form">
        <van-cell-group inset class="form-group">
          <!-- 用户名输入框 -->
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
            clearable
          />
          
          <!-- 密码输入框 -->
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            clearable
          />
        </van-cell-group>

        <!-- 登录按钮 -->
        <div class="submit-btn-wrapper">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit"
            :loading="loading"
          >
            登录
          </van-button>
        </div>

        <!-- 注册链接 -->
        <div class="register-link">
          还没有账号？
          <router-link to="/register" class="link-text">立即注册</router-link>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import * as authApi from '../../api/auth';
import { useUserStore } from '../../stores/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

// 表单数据
const formData = ref({
  username: '',
  password: ''
});

// 返回上一页
const goBack = () => {
  router.back();
};

// 提交登录
const onSubmit = async () => {
  if (loading.value) return;
  
  loading.value = true;
  try {
    await userStore.login(formData.value);
    
    // 获取登录前的URL
    const redirectUrl = localStorage.getItem('login_redirect') || '/';
    localStorage.removeItem('login_redirect');
    
    // 跳转到目标页面
    router.replace(redirectUrl);
  } catch (error) {
    console.error('登录失败:', error);
    showToast(error.response?.data?.message || '登录失败，请检查用户名和密码');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.nav-bar {
  background-color: transparent;
}

.nav-bar :deep(.van-nav-bar__title) {
  color: #323233;
  font-size: 18px;
  font-weight: 600;
}

.nav-bar :deep(.van-icon) {
  color: #323233;
}

.login-container {
  padding: 20px;
}

.login-form {
  margin-top: 20px;
}

.form-group {
  border-radius: 12px;
  overflow: hidden;
  background-color: #ffffff;
  margin-bottom: 20px;
}

.form-group :deep(.van-cell) {
  padding: 16px;
}

.form-group :deep(.van-field__label) {
  width: 60px;
  color: #323233;
}

.form-group :deep(.van-cell::after) {
  left: 16px;
  right: 16px;
}

.submit-btn-wrapper {
  margin: 24px 12px;
}

.submit-btn-wrapper :deep(.van-button) {
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #969799;
  margin-top: 16px;
}

.link-text {
  color: #1989fa;
  text-decoration: none;
}
</style> 