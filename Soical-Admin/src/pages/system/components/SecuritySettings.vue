<template>
  <div class="security-settings">
    <el-form 
      :model="formState" 
      :rules="rules"
      label-width="140px"
      ref="formRef"
    >
      <el-divider>密码策略</el-divider>
      
      <el-form-item label="密码最小长度" prop="passwordMinLength">
        <el-input-number 
          v-model="formState.passwordMinLength" 
          :min="6" 
          :max="32"
          style="width: 120px" 
        />
        <span class="form-text">字符</span>
      </el-form-item>

      <el-form-item label="密码复杂度要求" prop="passwordStrength">
        <el-checkbox-group v-model="formState.passwordStrength">
          <el-checkbox label="uppercase">包含大写字母</el-checkbox>
          <el-checkbox label="lowercase">包含小写字母</el-checkbox>
          <el-checkbox label="number">包含数字</el-checkbox>
          <el-checkbox label="special">包含特殊字符</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="密码过期时间" prop="passwordExpireDays">
        <el-input-number 
          v-model="formState.passwordExpireDays" 
          :min="0" 
          :max="365"
          style="width: 120px" 
        />
        <span class="form-text">天 (0表示永不过期)</span>
      </el-form-item>

      <el-divider>登录安全</el-divider>

      <el-form-item label="最大登录失败次数" prop="maxLoginAttempts">
        <el-input-number 
          v-model="formState.maxLoginAttempts" 
          :min="1" 
          :max="10"
          style="width: 120px" 
        />
        <span class="form-text">次 (超过将锁定账户)</span>
      </el-form-item>

      <el-form-item label="账户锁定时间" prop="accountLockTime">
        <el-input-number 
          v-model="formState.accountLockTime" 
          :min="5" 
          :max="1440"
          style="width: 120px" 
        />
        <span class="form-text">分钟</span>
      </el-form-item>

      <el-form-item label="启用验证码" prop="enableCaptcha">
        <el-switch v-model="formState.enableCaptcha" />
        <span class="form-text ml-10">开启后登录时需要输入验证码</span>
      </el-form-item>

      <el-form-item label="会话超时时间" prop="sessionTimeout">
        <el-input-number 
          v-model="formState.sessionTimeout" 
          :min="10" 
          :max="1440"
          style="width: 120px" 
        />
        <span class="form-text">分钟 (用户无操作超过该时间将自动退出)</span>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">保存设置</el-button>
        <el-button style="margin-left: 10px" @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getSecuritySettings, updateSecuritySettings } from '@/api/system';

const formRef = ref();
const loading = ref(false);

const formState = reactive({
  passwordMinLength: 8,
  passwordStrength: ['lowercase', 'number'],
  passwordExpireDays: 90,
  maxLoginAttempts: 5,
  accountLockTime: 30,
  enableCaptcha: false,
  sessionTimeout: 30
});

const rules = {
  passwordMinLength: [
    { required: true, message: '请输入密码最小长度', trigger: 'blur' }
  ],
  passwordStrength: [
    { type: 'array', message: '请选择密码复杂度要求', trigger: 'change' }
  ],
  maxLoginAttempts: [
    { required: true, message: '请输入最大登录失败次数', trigger: 'blur' }
  ],
  accountLockTime: [
    { required: true, message: '请输入账户锁定时间', trigger: 'blur' }
  ],
  sessionTimeout: [
    { required: true, message: '请输入会话超时时间', trigger: 'blur' }
  ]
};

onMounted(async () => {
  try {
    const res = await getSecuritySettings();
    if (res.code === 200) {
      // 处理passwordStrength可能是字符串形式的JSON数组
      if (res.data.passwordStrength) {
        try {
          // 尝试解析JSON字符串
          if (typeof res.data.passwordStrength === 'string') {
            if (res.data.passwordStrength.includes('[')) {
              formState.passwordStrength = JSON.parse(res.data.passwordStrength);
            } else if (res.data.passwordStrength.includes(',')) {
              // 处理可能的逗号分隔字符串
              formState.passwordStrength = res.data.passwordStrength
                .replace(/[\[\]']/g, '')
                .split(',')
                .map(item => item.trim());
            } else {
              // 单个值
              formState.passwordStrength = [res.data.passwordStrength];
            }
          } else if (Array.isArray(res.data.passwordStrength)) {
            // 已经是数组
            formState.passwordStrength = res.data.passwordStrength;
          }
        } catch (e) {
          console.error('解析passwordStrength失败', e);
          formState.passwordStrength = ['lowercase', 'number'];
        }
      }
      
      // 处理其他数值类型字段
      if (res.data.passwordMinLength) formState.passwordMinLength = Number(res.data.passwordMinLength);
      if (res.data.passwordExpireDays) formState.passwordExpireDays = Number(res.data.passwordExpireDays);
      if (res.data.maxLoginAttempts) formState.maxLoginAttempts = Number(res.data.maxLoginAttempts);
      if (res.data.accountLockTime) formState.accountLockTime = Number(res.data.accountLockTime);
      if (res.data.sessionTimeout) formState.sessionTimeout = Number(res.data.sessionTimeout);
      
      // 处理布尔类型
      if (res.data.enableCaptcha !== undefined) {
        formState.enableCaptcha = res.data.enableCaptcha === 'true' || res.data.enableCaptcha === true;
      }
    }
  } catch (error) {
    ElMessage.error('获取安全设置失败');
  }
});

const submitForm = () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    try {
      // 创建表单数据的副本
      const formData = { ...formState };
      
      // 不要将passwordStrength转换为JSON字符串，确保它作为原始数组发送
      // 如果后端API需要特定格式，可以在这里进行转换
      
      const res = await updateSecuritySettings(formData);
      if (res.code === 200) {
        ElMessage.success('保存成功');
      } else {
        ElMessage.error(res.msg || '保存失败');
      }
    } catch (error) {
      ElMessage.error('保存设置失败');
    } finally {
      loading.value = false;
    }
  });
};

const resetForm = () => {
  formRef.value.resetFields();
};
</script>

<style scoped>
.security-settings {
  padding: 24px 0;
}
.form-text {
  margin-left: 8px;
  color: rgba(0, 0, 0, 0.45);
}
.ml-10 {
  margin-left: 10px;
}
</style> 