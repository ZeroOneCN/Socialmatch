<template>
  <div class="register-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="注册账号"
      left-arrow
      @click-left="goBack"
      :border="false"
      class="nav-bar"
    />

    <div class="register-container">
      <!-- 注册表单 -->
      <van-form @submit="onSubmit" class="register-form">
        <!-- 用户名输入框 -->
        <van-cell-group inset class="form-group">
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
            clearable
          />
          
          <!-- 手机号输入框 -->
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            type="tel"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
            clearable
          />
          
          <!-- 密码输入框 -->
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[
              { required: true, message: '请输入密码' },
              { validator: (val) => validatePassword(val), message: passwordRequirements }
            ]"
            clearable
          />
          
          <!-- 昵称输入框 -->
          <van-field
            v-model="formData.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ required: true, message: '请输入昵称' }]"
            clearable
          />
        </van-cell-group>

        <!-- 密码要求提示 -->
        <div class="password-requirements">
          {{ passwordRequirements }}
        </div>

        <!-- 注册按钮 -->
        <div class="submit-btn-wrapper">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit"
            :loading="loading"
          >
            注册
          </van-button>
        </div>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <router-link to="/login" class="link-text">立即登录</router-link>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showSuccessToast } from 'vant';
import * as authApi from '../../api/auth';
import { useUserStore } from '../../stores/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

// 安全设置
const securitySettings = ref({
  passwordMinLength: 8,
  passwordStrength: ['lowercase', 'number'],
  passwordExpireDays: 90,
  maxLoginAttempts: 5,
  accountLockTime: 30,
  enableCaptcha: true,
  sessionTimeout: 30
});

// 获取密码策略描述
const passwordRequirements = ref('');

// 表单数据
const formData = ref({
  username: '',
  phone: '',
  password: '',
  nickname: ''
});

// 加载安全设置
const loadSecuritySettings = async () => {
  try {
    const response = await authApi.getSecuritySettings();
    if (response.code === 200 && response.data) {
      securitySettings.value = response.data;
      updatePasswordRequirements();
    }
  } catch (error) {
    console.error('获取安全设置失败:', error);
  }
};

// 更新密码要求描述
const updatePasswordRequirements = () => {
  let requirements = `密码至少需要${securitySettings.value.passwordMinLength}位，且必须包含：`;
  const strengthMap = {
    lowercase: '小写字母',
    uppercase: '大写字母',
    number: '数字',
    special: '特殊字符'
  };
  
  const requiredTypes = Array.isArray(securitySettings.value.passwordStrength) 
    ? securitySettings.value.passwordStrength 
    : [];
    
  const requirementTexts = requiredTypes.map(type => strengthMap[type] || type);
  
  if (requirementTexts.length > 0) {
    requirements += requirementTexts.join('、');
  } else {
    requirements = `密码至少需要${securitySettings.value.passwordMinLength}位`;
  }
  
  passwordRequirements.value = requirements;
};

// 检查密码是否满足要求
const validatePassword = (password) => {
  if (!password) return false;
  
  // 检查长度
  if (password.length < securitySettings.value.passwordMinLength) {
    return false;
  }
  
  // 检查复杂度要求
  const strengthChecks = {
    lowercase: /[a-z]/.test(password),
    uppercase: /[A-Z]/.test(password),
    number: /\d/.test(password),
    special: /[^a-zA-Z0-9]/.test(password)
  };
  
  // 确保是数组
  const requiredStrength = Array.isArray(securitySettings.value.passwordStrength) 
    ? securitySettings.value.passwordStrength 
    : [];
    
  // 检查每个要求是否满足
  return requiredStrength.every(requirement => strengthChecks[requirement]);
};

// 初始化加载安全设置
onMounted(() => {
  loadSecuritySettings();
});

// 返回上一页
const goBack = () => {
  router.back();
};

// 提交注册
const onSubmit = async () => {
  if (loading.value) return;
  
  // 验证密码是否满足要求
  if (!validatePassword(formData.value.password)) {
    showToast(passwordRequirements.value);
    return;
  }
  
  loading.value = true;
  try {
    // 注册
    const response = await authApi.register(formData.value);
    showSuccessToast('注册成功');
    
    // 自动登录
    const loginResponse = await authApi.login({
      username: formData.value.username,
      password: formData.value.password
    });
    
    // 保存登录状态，这里使用 login 方法替代 setLoginState
    await userStore.login({
      username: formData.value.username,
      password: formData.value.password
    });
    
    // 跳转到首页
    router.replace('/');
  } catch (error) {
    console.error('注册失败:', error);
    showToast(error.response?.data?.message || '注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
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

.register-container {
  padding: 20px;
}

.register-form {
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

.login-link {
  text-align: center;
  font-size: 14px;
  color: #969799;
  margin-top: 16px;
}

.link-text {
  color: #1989fa;
  text-decoration: none;
}

.password-requirements {
  font-size: 12px;
  color: #909399;
  margin: 0 24px 10px;
  padding: 0 12px;
}
</style>