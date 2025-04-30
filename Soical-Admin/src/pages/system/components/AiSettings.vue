<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>AI助手配置</span>
      </div>
    </template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <!-- 基础配置 -->
      <h3>基础配置</h3>
      <el-form-item label="启用AI助手" prop="enabled">
        <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
      </el-form-item>
      <el-form-item label="助手名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入助手名称" />
      </el-form-item>
      <el-form-item label="助手头像URL" prop="avatarUrl">
        <el-input v-model="form.avatarUrl" placeholder="请输入助手头像URL" />
      </el-form-item>
      <el-form-item label="欢迎消息" prop="welcomeMessage">
        <el-input v-model="form.welcomeMessage" type="textarea" :rows="3" placeholder="请输入欢迎消息" />
      </el-form-item>

      <!-- DeepSeek API配置 -->
      <h3>DeepSeek API配置</h3>
      <el-form-item label="API密钥" prop="apiKey">
        <el-input 
          v-model="form.apiKey" 
          :placeholder="apiKeyStatus.saved ? apiKeyStatus.placeholder : '请输入DeepSeek API密钥'" 
          show-password 
        />
        <div v-if="apiKeyStatus.saved" class="form-tip">
          <el-tag size="small" type="success">API密钥已保存</el-tag>
          <span class="ml-5">重新输入以更新</span>
        </div>
      </el-form-item>
      <el-form-item label="API基础URL" prop="apiBaseUrl">
        <el-input v-model="form.apiBaseUrl" placeholder="请输入DeepSeek API基础URL" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="form.modelName" placeholder="请输入模型名称" />
      </el-form-item>

      <!-- 高级设置 -->
      <h3>高级设置</h3>
      <el-form-item label="温度参数" prop="temperature">
        <el-slider v-model="form.temperature" :min="0" :max="1" :step="0.1" :format-tooltip="val => val.toFixed(1)" />
      </el-form-item>
      <el-form-item label="最大Token数" prop="maxTokens">
        <el-input-number v-model="form.maxTokens" :min="100" :max="4000" :step="50" />
      </el-form-item>
      <el-form-item label="系统提示词" prop="systemPrompt">
        <el-input v-model="form.systemPrompt" type="textarea" :rows="5" placeholder="请输入系统提示词" />
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存设置</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiSettings, updateAiSettings } from '@/api/system'

// 表单引用
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  enabled: false,
  name: '',
  avatarUrl: '',
  welcomeMessage: '',
  apiKey: '',
  apiBaseUrl: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 1000,
  systemPrompt: ''
})

// 表单验证规则
const rules = reactive({
  name: [{ required: true, message: '请输入助手名称', trigger: 'blur' }],
  apiKey: [{ 
    validator: (rule, value, callback) => {
      if (!value && !apiKeyStatus.value.saved) {
        callback(new Error('请输入API密钥'));
      } else {
        callback();
      }
    }, 
    trigger: 'blur' 
  }],
  apiBaseUrl: [{ required: true, message: '请输入API基础URL', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
})

// 在form定义后添加一个新的状态变量
const apiKeyStatus = ref({
  saved: false,
  placeholder: ''
})

// 获取当前设置
const fetchSettings = async () => {
  try {
    loading.value = true
    const res = await getAiSettings()
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 处理API密钥的特殊情况
      if (data.api_key_set === 'true') {
        apiKeyStatus.value.saved = true
        apiKeyStatus.value.placeholder = '******（已保存，重新输入以更改）'
        // 如果密钥已保存但后端未返回实际值，不清空表单中的值
        if (!data.api_key || data.api_key.trim() === '') {
          data.api_key = form.apiKey || '******' // 保持当前值或显示占位符
        }
      } else {
        apiKeyStatus.value.saved = false
        apiKeyStatus.value.placeholder = '请输入API密钥'
      }
      
      // 将返回数据填充到表单
      Object.keys(form).forEach(key => {
        // 将后端命名规则（snake_case）转换为前端命名规则（camelCase）
        const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)
        
        if (data[key] !== undefined) {
          form[key] = data[key]
        } else if (data[snakeKey] !== undefined) {
          // 处理snake_case命名的字段
          if (typeof form[key] === 'boolean') {
            form[key] = data[snakeKey] === true || data[snakeKey] === 'true'
          } else if (typeof form[key] === 'number') {
            form[key] = Number(data[snakeKey])
          } else {
            form[key] = data[snakeKey]
          }
        }
      })
      
      // 特殊字段处理
      if (data.api_key) form.apiKey = data.api_key
      if (data.api_base_url) form.apiBaseUrl = data.api_base_url
      if (data.model_name) form.modelName = data.model_name
      if (data.max_tokens) form.maxTokens = Number(data.max_tokens)
      if (data.system_prompt) form.systemPrompt = data.system_prompt
      if (data.welcome_message) form.welcomeMessage = data.welcome_message
      if (data.avatar) form.avatarUrl = data.avatar
      
      // 确保布尔值和数字类型正确
      if (data.enabled !== undefined) {
        form.enabled = data.enabled === true || data.enabled === 'true'
      }
      if (data.temperature !== undefined) {
        form.temperature = Number(data.temperature)
      }
      
      console.log('成功获取AI设置', form)
    } else {
      console.warn('获取AI设置无数据', res)
    }
  } catch (error) {
    console.error('获取AI设置失败', error)
    ElMessage.error('获取AI设置失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 转换字段名称以匹配后端API期望的格式
    const formData = {
      enabled: form.enabled.toString(),
      name: form.name,
      avatar: form.avatarUrl,
      welcome_message: form.welcomeMessage,
      api_base_url: form.apiBaseUrl,
      model_name: form.modelName,
      temperature: form.temperature.toString(),
      max_tokens: form.maxTokens.toString(),
      system_prompt: form.systemPrompt
    }
    
    // 只有当API密钥不是占位符时才发送
    if (form.apiKey && form.apiKey !== '******') {
      formData.api_key = form.apiKey
    }
    
    console.log('提交AI设置数据', formData)
    const res = await updateAiSettings(formData)
    if (res.code === 200) {
      ElMessage.success('AI设置更新成功')
      // 重新获取最新设置
      await fetchSettings()
    } else {
      ElMessage.error(res.message || 'AI设置更新失败')
    }
  } catch (error) {
    console.error('AI设置更新失败', error)
    ElMessage.error('表单验证失败或提交出错')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  fetchSettings()
}

// 组件挂载时获取设置
onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.box-card {
  margin: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

:deep(.el-form) {
  padding: 24px;
}

h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 24px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

h3:first-child {
  margin-top: 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input) {
  max-width: 400px;
}

:deep(.el-textarea) {
  max-width: 600px;
}

:deep(.el-slider) {
  max-width: 400px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.ml-5 {
  margin-left: 5px;
}

:deep(.el-button) {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 4px;
}

:deep(.el-button--primary) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.el-button--primary:hover) {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

:deep(.el-switch) {
  --el-switch-on-color: #1890ff;
}
</style> 