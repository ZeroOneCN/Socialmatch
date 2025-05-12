<template>
  <div class="ai-chat-page">
    <!-- 导航栏 -->
    <van-nav-bar
      left-arrow
      @click-left="onClickLeft"
      class="ai-nav-bar"
    >
      <template #title>
        <div class="chat-title">
          <span class="ai-badge">AI</span>
          {{ aiSettings.name || 'Social AI' }}
        </div>
      </template>
      <template #right>
        <van-icon name="delete-o" size="18" @click="showClearConfirm" />
      </template>
    </van-nav-bar>
    
    <!-- 聊天内容区域 -->
    <div class="chat-container" ref="chatContainer">
      <!-- 背景装饰 -->
      <div class="ai-background"></div>
      
      <!-- 欢迎消息 -->
      <div v-if="chatHistory.length === 0" class="welcome-container">
        <div class="ai-avatar">
          <van-image
            :src="aiSettings.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
            round
            fit="cover"
            @error="(e) => e.target.src = 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
          />
        </div>
        <h2>{{ aiSettings.name || 'Social AI' }}</h2>
        <p class="welcome-text">{{ aiSettings.welcomeMessage || '你好！我是你的AI助手，有什么我可以帮你的吗？' }}</p>
        
        <!-- 快捷问题建议 -->
        <div class="quick-questions">
          <div class="question-chip" v-for="(question, index) in suggestedQuestions" :key="index" @click="askQuestion(question)">
            {{ question }}
          </div>
        </div>
      </div>
      
      <!-- 聊天记录 -->
      <div class="messages-container">
        <!-- 消息项 -->
        <div v-for="(message, index) in chatHistory" 
             :key="index" 
             :class="['message-item', message.role === 'user' ? 'user-message' : 'ai-message']">
          
          <!-- AI消息 -->
          <template v-if="message.role === 'assistant'">
            <div class="message-avatar">
              <van-image
                :src="aiSettings.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
                round
                fit="cover"
                @error="(e) => e.target.src = 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
              />
            </div>
            <div class="message-content ai-content">
              <span v-html="parseMarkdown(message.message)"></span>
            </div>
          </template>
          
          <!-- 用户消息 -->
          <template v-else>
            <div class="message-content user-content">
              <span>{{ message.message }}</span>
            </div>
            <div class="message-avatar">
              <van-image
                :src="userAvatar || '/static/images/default_avatar.png'"
                round
                fit="cover"
                @error="(e) => e.target.src = '/static/images/default_avatar.png'"
              />
            </div>
          </template>
        </div>
      </div>
      
      <!-- 加载中提示 -->
      <div v-if="loading" class="loading-message">
        <div class="message-avatar">
          <van-image
            :src="aiSettings.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
            round
            fit="cover"
            @error="(e) => e.target.src = 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png'"
          />
        </div>
        <div class="message-content ai-content">
          <span class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </div>
      </div>
    </div>
    
    <!-- 输入框区域 -->
    <div class="input-container">
      <van-field
        v-model="messageText"
        placeholder="输入消息..."
        class="message-input"
        :disabled="loading || !aiEnabled"
        @keypress.enter.prevent="sendMessage"
      >
        <template #button>
          <van-button 
            type="primary" 
            size="small" 
            class="send-button"
            :loading="loading"
            :disabled="!messageText || !aiEnabled"
            @click="sendMessage"
          >
            发送
          </van-button>
        </template>
      </van-field>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showDialog } from 'vant';
import { useUserStore } from '../stores/user';
import dayjs from 'dayjs';
import request from '../utils/request';
import { marked } from 'marked';

// 路由
const router = useRouter();

// 用户信息
const userStore = useUserStore();
const userAvatar = computed(() => userStore.avatar || '/static/images/default_avatar.png');

// 聊天状态
const messageText = ref('');
const chatHistory = ref([]);
const loading = ref(false);
const chatContainer = ref(null);
const aiEnabled = ref(true);

// AI设置
const aiSettings = ref({
  enabled: true,
  name: 'Social AI',
  avatar: 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
  welcomeMessage: '你好！我是你的AI助手，有什么我可以帮你的吗？'
});

// 快捷问题建议
const suggestedQuestions = [
  '你能做什么？',
  '介绍一下同频App',
  '推荐几个聊天话题',
  '如何提高匹配率？'
];

// 返回上一页
const onClickLeft = () => {
  router.back();
};

// 获取AI助手配置
const fetchAiSettings = async () => {
  try {
    // 修正API路径，移除重复的/api前缀
    const response = await request.get('/system/settings/ai');
    console.log('AI设置响应:', response);
    
    // 检查响应的各种可能格式
    if (response && response.code === 200 && response.data) {
      const settings = response.data;
      console.log('获取到AI设置:', settings);
      aiSettings.value = {
        enabled: settings.enabled === 'true',
        name: settings.name || 'Social AI',
        avatar: settings.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
        welcomeMessage: settings.welcome_message || '你好！我是你的AI助手，有什么我可以帮你的吗？'
      };
      aiEnabled.value = settings.enabled === 'true';
      console.log('设置AI头像:', aiSettings.value.avatar);
    } 
    // 尝试其他响应格式 (code === 0)
    else if (response && response.code === 0 && response.data) {
      const settings = response.data;
      console.log('获取到AI设置(code=0):', settings);
      aiSettings.value = {
        enabled: settings.enabled === 'true',
        name: settings.name || 'Social AI',
        avatar: settings.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
        welcomeMessage: settings.welcome_message || '你好！我是你的AI助手，有什么我可以帮你的吗？'
      };
      aiEnabled.value = settings.enabled === 'true';
      console.log('设置AI头像:', aiSettings.value.avatar);
    }
    // 直接使用response作为数据
    else if (response && typeof response === 'object') {
      console.log('使用直接响应作为设置:', response);
      aiSettings.value = {
        enabled: response.enabled === 'true',
        name: response.name || 'Social AI',
        avatar: response.avatar || 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
        welcomeMessage: response.welcome_message || '你好！我是你的AI助手，有什么我可以帮你的吗？'
      };
      aiEnabled.value = response.enabled === 'true';
      console.log('设置AI头像:', aiSettings.value.avatar);
    }
    else {
      console.warn('获取AI设置失败或无数据:', response);
      // 如果接口失败，也要启用AI助手
      aiEnabled.value = true;
    }
  } catch (error) {
    console.error('获取AI助手配置失败:', error);
    // 出错时也确保AI助手可用，使用默认配置
    aiSettings.value = {
      enabled: true,
      name: 'Social AI',
      avatar: 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
      welcomeMessage: '你好！我是你的AI助手，有什么我可以帮你的吗？'
    };
    aiEnabled.value = true;
  }
};

// 获取聊天历史
const fetchChatHistory = async () => {
  try {
    // 确保用户已登录并获取用户ID
    const currentUserId = userStore.userId;
    if (!currentUserId) {
      chatHistory.value = [];
      return;
    }

    // 修正API路径，添加用户ID参数
    const response = await request.get('/ai/history', {
      params: { userId: currentUserId }
    });
    console.log('聊天历史响应:', response);
    
    if (response.code === 0 && response.data) {
      chatHistory.value = response.data;
      scrollToBottom();
    } 
    else if (response.code === 200 && response.data) {
      chatHistory.value = response.data;
      scrollToBottom();
    }
    else if (Array.isArray(response)) {
      chatHistory.value = response;
      scrollToBottom();
    }
  } catch (error) {
    console.error('获取聊天历史失败:', error);
    showToast('获取聊天历史失败');
    // 出错时清空聊天历史
    chatHistory.value = [];
  }
};

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim() || loading.value || !aiEnabled.value) return;
  
  // 保存用户消息
  const userMessage = {
    role: 'user',
    message: messageText.value,
    createTime: new Date()
  };
  
  chatHistory.value.push(userMessage);
  const message = messageText.value;
  messageText.value = '';
  
  // 滚动到底部
  await nextTick();
  scrollToBottom();
  
  // 显示加载状态
  loading.value = true;
  
  try {
    console.log('发送消息到AI:', message);
    // 修正API路径，移除重复的/api前缀
    const response = await request.post('/ai/chat', {
      message: message,
      newSession: chatHistory.value.length <= 1
    });
    
    console.log('AI响应:', response);
    
    // 检查不同可能的响应格式
    if (response.code === 0 && response.data) {
      // 添加AI回复
      chatHistory.value.push(response.data);
    } 
    else if (response.code === 200 && response.data) {
      // 可能的其他成功状态码
      chatHistory.value.push(response.data);
    }
    else if (response.message) {
      // 直接返回消息的格式
      chatHistory.value.push({
        role: 'assistant',
        message: response.message,
        createTime: new Date()
      });
    }
    else if (typeof response === 'object') {
      // 直接作为响应对象
      chatHistory.value.push({
        role: 'assistant',
        message: response.message || '抱歉，我无法理解您的问题',
        createTime: new Date()
      });
    }
    else {
      showToast('AI助手响应失败，请重试');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    
    // 即使失败，添加一个错误回复，确保用户体验
    chatHistory.value.push({
      role: 'assistant',
      message: 'AI助手暂时无法回复，请稍后再试。',
      createTime: new Date()
    });
    
    showToast('发送消息失败，请重试');
  } finally {
    loading.value = false;
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  }
};

// 使用快捷问题
const askQuestion = (question) => {
  if (loading.value || !aiEnabled.value) return;
  messageText.value = question;
  sendMessage();
};

// 清除聊天记录确认
const showClearConfirm = () => {
  showDialog({
    title: '清除聊天记录',
    message: '确定要清除所有聊天记录吗？此操作不可恢复。',
    showCancelButton: true,
    confirmButtonText: '清除',
    cancelButtonText: '取消'
  }).then(() => {
    clearChatHistory();
  }).catch(() => {
    // 取消操作
  });
};

// 清除聊天记录
const clearChatHistory = async () => {
  try {
    // 修正API路径，移除重复的/api前缀
    const response = await request.delete('/ai/history');
    if (response.code === 0 && response.data) {
      chatHistory.value = [];
      showToast('聊天记录已清除');
    } else {
      showToast('清除失败，请重试');
    }
  } catch (error) {
    console.error('清除聊天记录失败:', error);
    showToast('清除失败，请重试');
  }
};

// 时间格式化
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  return dayjs(timestamp).format('HH:mm');
};

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// 监听聊天记录变化，自动滚动到底部
watch(chatHistory, () => {
  nextTick(() => {
    scrollToBottom();
  });
});

// 监听用户登录状态变化
watch(() => userStore.userId, (newUserId, oldUserId) => {
  if (newUserId !== oldUserId) {
    // 用户ID变化时清空聊天历史
    chatHistory.value = [];
    if (newUserId) {
      // 如果是新用户登录，重新获取聊天历史
      fetchChatHistory();
    }
  }
});

// 生命周期钩子
onMounted(async () => {
  // 获取AI助手配置
  await fetchAiSettings();
  
  // 获取聊天历史
  if (aiEnabled.value) {
    await fetchChatHistory();
  }
  
  // 滚动到底部
  await nextTick();
  scrollToBottom();
});

// 解析Markdown格式的文本
const parseMarkdown = (text) => {
  if (!text) return '';
  try {
    // 配置marked选项
    marked.setOptions({
      breaks: true, // 允许换行
      gfm: true,    // 启用GitHub风格Markdown
      headerIds: false // 禁用标题ID自动生成
    });
    return marked.parse(text);
  } catch (e) {
    console.error('Markdown解析失败:', e);
    return text; // 解析失败时返回原始文本
  }
};
</script>

<style scoped>
.ai-chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fa;
}

.ai-nav-bar {
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chat-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.ai-badge {
  display: inline-block;
  background-color: #5e72e4;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  margin-right: 6px;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  position: relative;
}

.ai-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.05;
  background-image: url('/static/images/ai_pattern.png');
  background-size: 200px;
  pointer-events: none;
}

.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 16px;
  text-align: center;
}

.ai-avatar {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar .van-image {
  width: 100%;
  height: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-container h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #333;
}

.welcome-text {
  margin: 0 0 24px;
  font-size: 16px;
  color: #666;
  line-height: 1.5;
  max-width: 280px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.question-chip {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  color: #5e72e4;
  cursor: pointer;
  transition: all 0.2s;
}

.question-chip:active {
  background-color: #f0f0ff;
  transform: scale(0.98);
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  width: 100%;
  display: flex;
  max-width: 100%;
  align-items: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.message-avatar .van-image {
  width: 100%;
  height: 100%;
}

.ai-message .message-content {
  margin-left: 8px;
}

.user-message .message-content {
  margin-right: 8px;
}

.message-content {
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  position: relative;
  max-width: 70%;
  word-break: break-word;
}

.user-content {
  background-color: #5e72e4;
  color: white;
  border-top-right-radius: 4px;
}

.ai-content {
  background-color: white;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 12px;
  margin-top: 4px;
  text-align: right;
}

.user-time {
  color: rgba(255, 255, 255, 0.7);
}

.ai-message .message-time {
  color: #999;
}

.loading-message {
  display: flex;
  align-self: flex-start;
  max-width: 85%;
  margin-top: 16px;
}

.typing-indicator {
  display: flex;
  align-items: center;
}

.typing-indicator span {
  height: 8px;
  width: 8px;
  border-radius: 50%;
  background-color: #888;
  display: block;
  margin: 0 2px;
  opacity: 0.4;
}

.typing-indicator span:nth-child(1) {
  animation: bounce 1s infinite;
}

.typing-indicator span:nth-child(2) {
  animation: bounce 1s 0.33s infinite;
}

.typing-indicator span:nth-child(3) {
  animation: bounce 1s 0.66s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.input-container {
  padding: 10px 16px;
  background-color: white;
  border-top: 1px solid #eee;
}

.message-input {
  background-color: #f5f5f5;
  border-radius: 20px;
  padding: 0 8px;
}

.send-button {
  border-radius: 20px;
  height: 36px;
  margin-left: 8px;
}

/* Markdown内容样式 */
.ai-content :deep(p) {
  margin: 0 0 8px;
}

.ai-content :deep(p:last-child) {
  margin-bottom: 0;
}

.ai-content :deep(strong) {
  font-weight: 600;
}

.ai-content :deep(em) {
  font-style: italic;
}

.ai-content :deep(code) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.ai-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}

.ai-content :deep(ul), .ai-content :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.ai-content :deep(li) {
  margin-bottom: 4px;
}

.ai-content :deep(a) {
  color: #5e72e4;
  text-decoration: none;
}
</style> 