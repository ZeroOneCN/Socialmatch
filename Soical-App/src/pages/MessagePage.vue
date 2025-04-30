<template>
  <div class="page-container">
    <h1 class="page-title">消息</h1>
    
    <div class="content-container">
      <template v-if="userStore.isLoggedIn">
        <div v-if="loading" class="loading-container">
          <van-loading type="spinner" color="#1989fa" />
        </div>
        
        <div v-else-if="!conversationList.length && !aiSettings.enabled" class="empty-container">
          <van-empty description="暂无消息" />
          <p class="text-center text-secondary">匹配并喜欢其他用户后，就可以开始聊天啦</p>
        </div>
        
        <div v-else class="conversation-list">
          <!-- AI助手聊天入口 -->
          <div v-if="aiSettings.enabled" class="ai-assistant-entry" @click="goToAiChat">
            <div class="avatar-container">
              <div class="avatar-grid">
                <van-image
                  :src="aiSettings.avatar || '/static/images/ai_avatar.png'"
                  round
                  fit="cover"
                  class="avatar"
                  @error="(e) => e.target.src = 'https://img01.yzcdn.cn/vant/cat.jpeg'"
                />
                <div class="online-status is-online"></div>
              </div>
            </div>
            
            <div class="chat-content">
              <div class="chat-header">
                <div class="chat-name">
                  <span class="ai-badge">AI</span>
                  {{ aiSettings.name || 'Social AI助手' }}
                  <span class="online-text">● 总是在线</span>
                </div>
                <div class="chat-time"></div>
              </div>
              <div class="chat-message">
                <span class="message-text">{{ aiSettings.welcomeMessage || '你好！我是你的AI助手，有什么我可以帮你的吗？' }}</span>
              </div>
            </div>
          </div>
          
          <!-- 常规会话列表分隔线 -->
          <div v-if="aiSettings.enabled && conversationList.length > 0" class="divider">
            <span class="divider-text">聊天消息</span>
          </div>

          <van-swipe-cell
            v-for="conversation in conversationList"
            :key="conversation.conversationId"
            :before-close="beforeClose"
            :name="conversation.conversationId"
          >
            <div
              class="chat-item"
              @click="goToChat(conversation.conversationId)"
            >
              <div class="avatar-container">
                <van-badge :content="conversation.unreadCount || ''" :show-zero="false">
                  <div class="avatar-grid">
                    <van-image
                      :src="conversation.targetUserAvatar || 'https://img01.yzcdn.cn/vant/cat.jpeg'"
                      round
                      fit="cover"
                      class="avatar"
                      @error="(e) => handleAvatarError(e, conversation)"
                    />
                    <div class="online-status" :class="{ 'is-online': conversation.isOnline === true, 'is-offline': conversation.isOnline === false }"></div>
                  </div>
                </van-badge>
              </div>
              
              <div class="chat-content">
                <div class="chat-header">
                  <div class="chat-name">
                    {{ conversation.targetUserNickname || conversation.targetUserName || `用户${conversation.targetUserId || '未知'}` }}
                    <span class="online-text" v-if="conversation.isOnline === true">● 在线</span>
                    <span class="offline-text" v-else-if="conversation.isOnline === false">○ 离线</span>
                  </div>
                  <div class="chat-time">{{ formatTime(conversation.lastMessageTime) || '' }}</div>
                </div>
                <div class="chat-message">
                  <span class="message-text">{{ conversation.lastMessage || '暂无消息' }}</span>
                </div>
              </div>
              
              <div class="notification-control">
                <van-icon name="bell" color="#CCCCCC" size="18" />
              </div>
            </div>

            <template #right>
              <van-button square type="danger" text="删除" class="delete-button" />
            </template>
          </van-swipe-cell>
        </div>
      </template>
      
      <template v-else>
        <div class="login-tip">
          <van-empty description="请先登录" />
          <van-button type="primary" block @click="goToLogin">去登录</van-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onActivated, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '../stores/user'
import { useMessageStore } from '../stores/message'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { ref } from 'vue'
import request from '../utils/request'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

// 计算属性
const loading = computed(() => messageStore.loading)
const conversationList = computed(() => messageStore.conversationList)

// AI设置
const aiSettings = ref({
  enabled: true,
  name: 'Social AI助手',
  avatar: 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
  welcomeMessage: '你好！我是你的AI助手，有什么我可以帮你的吗？'
});

// 方法
const goToChat = async (conversationId) => {
  // 检查conversationId是否有效
  if (conversationId) {
    console.log('导航到聊天页面:', conversationId);
    
    // 获取会话对象
    const conversation = conversationList.value.find(c => c.conversationId === conversationId);
    
    // 如果会话有未读消息，标记为已读
    if (conversation && conversation.unreadCount > 0) {
      // 在本地更新未读数
      conversation.unreadCount = 0;
      
      // 发送请求到服务器标记为已读
      try {
        if (userStore.userId) {
          await request.post('/chat/messages/read', null, {
            params: {
              conversationId: conversationId,
              receiverId: userStore.userId
            }
          });
          console.log(`已标记会话 ${conversationId} 的消息为已读`);
        }
      } catch (error) {
        console.error('标记消息为已读失败:', error);
        // 失败不影响导航
      }
    }
    
    // 导航到聊天页面
    router.push(`/chat/${conversationId}`);
  } else {
    console.error('无效的conversationId:', conversationId);
    showToast('聊天会话不存在');
  }
}

// 侧滑删除前确认
const beforeClose = ({ position, name: conversationId }) => {
  // 只处理右侧按钮
  if (position === 'right') {
    // 显示确认对话框
    return new Promise((resolve) => {
      showDialog({
        title: '删除会话',
        message: '确定要删除这个会话吗？\n聊天记录将会被清空。',
        showCancelButton: true,
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }).then(() => {
        // 确认删除
        deleteConversation(conversationId);
        resolve(true); // 关闭滑动单元格
      }).catch(() => {
        resolve(false); // 不关闭滑动单元格
      });
    });
  }
  return true; // 其他情况一律关闭
};

// 删除会话方法
const deleteConversation = async (conversationId) => {
  try {
    console.log('删除会话:', conversationId);
    const result = await messageStore.deleteConversation(conversationId);
    if (result) {
      showToast('已删除会话');
    }
  } catch (error) {
    console.error('删除会话失败:', error);
    showToast('删除失败，请重试');
  }
};

const goToLogin = () => {
  router.push('/login')
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const now = dayjs()
  const messageTime = dayjs(timestamp)
  
  if (now.diff(messageTime, 'day') < 1) {
    return messageTime.format('HH:mm')
  } else if (now.diff(messageTime, 'day') < 7) {
    return messageTime.format('ddd')
  } else {
    return messageTime.format('MM-DD')
  }
}

// 生命周期钩子
onMounted(() => {
  // 检查是否登录
  if (userStore.isLoggedIn) {
    loadData()
  } else {
    // 未登录时确保所有用户显示为离线
    clearOnlineStatus()
  }
  
  // 添加WebSocket定期检查
  startWebSocketCheck()
  
  // 获取AI助手配置
  fetchAiSettings()
})

onActivated(() => {
  // 每次页面激活时检查登录状态
  if (userStore.isLoggedIn) {
    loadData()
    
    // 如果WebSocket已连接，立即请求一次在线状态
    if (messageStore.isWebSocketConnected) {
      const userIds = conversationList.value
        .map(conv => conv.targetUserId)
        .filter(id => id);
      
      if (userIds.length > 0) {
        console.log('页面激活时立即请求在线状态:', userIds);
        messageStore.requestMultipleUserOnlineStatus(userIds);
      }
    }
  } else {
    // 未登录时确保所有用户显示为离线
    clearOnlineStatus()
  }
})

// 清空所有用户的在线状态
const clearOnlineStatus = () => {
  console.log('清空所有用户的在线状态')
  if (conversationList.value && conversationList.value.length > 0) {
    conversationList.value.forEach(conv => {
      if (conv.targetUserId) {
        conv.isOnline = false
      }
    })
  }
}

// 添加WebSocket连接检查
const wsCheckInterval = ref(null)

const startWebSocketCheck = () => {
  // 先清除之前的定时器
  if (wsCheckInterval.value) {
    clearInterval(wsCheckInterval.value)
  }
  
  // 设置新的定时器，定期检查WebSocket连接状态
  wsCheckInterval.value = setInterval(() => {
    // 只在页面可见且有网络连接时检查
    if (document.visibilityState === 'visible' && 
        navigator.onLine && 
        userStore.isLoggedIn && 
        !messageStore.isWebSocketConnected) {
      console.log('MessagePage: 定期检查发现WebSocket未连接，尝试重新连接')
      
      // 使用安静连接方法
      ensureQuietWebSocketConnection()
    }
  }, 15000) // 每15秒检查一次
}

// 确保WebSocket连接不弹出提示
const ensureQuietWebSocketConnection = async () => {
  // 临时禁用WebSocket连接的toast提示
  const debugSetting = localStorage.getItem('debug_websocket')
  localStorage.removeItem('debug_websocket')
  
  try {
    // 尝试连接WebSocket
    await messageStore.connectWebSocket()
    console.log('WebSocket连接已静默建立')
  } catch (error) {
    console.error('WebSocket静默连接失败:', error)
  } finally {
    // 恢复之前的设置
    if (debugSetting) {
      localStorage.setItem('debug_websocket', debugSetting)
    }
  }
}

// 在组件卸载时清除定时器
onBeforeUnmount(() => {
  if (wsCheckInterval.value) {
    clearInterval(wsCheckInterval.value)
    wsCheckInterval.value = null
  }
})

// 从服务器API获取用户在线状态
const fetchUserOnlineStatusFromAPI = async (userId) => {
  if (!userId) return false;
  
  // 添加检查 - 如果当前用户已登出，直接返回离线状态
  if (!userStore.isLoggedIn) {
    console.log('当前用户已登出，返回离线状态');
    return false;
  }
  
  try {
    console.log('通过API获取用户在线状态:', userId);
    const response = await request.get(`/user/status/${userId}`);
    if (response && response.data !== undefined) {
      const isOnline = response.data === true || response.data === 'ONLINE';
      return isOnline;
    }
    return false;
  } catch (error) {
    console.error('获取用户在线状态失败:', error);
    return false;
  }
}

// 批量获取用户在线状态
const fetchMultipleUserOnlineStatus = async (userIds) => {
  if (!userIds || userIds.length === 0) return {};
  
  // 添加检查 - 如果当前用户已登出，将所有用户标记为离线
  if (!userStore.isLoggedIn) {
    console.log('当前用户已登出，将所有用户标记为离线');
    const offlineMap = {};
    userIds.forEach(id => {
      updateConversationOnlineStatus(id, false);
      offlineMap[id] = false;
    });
    return offlineMap;
  }
  
  try {
    // 从API获取在线状态
    const statusMap = {};
    
    // 使用Promise.all并行请求多个用户状态
    await Promise.all(userIds.map(async (userId) => {
      const isOnline = await fetchUserOnlineStatusFromAPI(userId);
      // 更新会话列表中的状态
      updateConversationOnlineStatus(userId, isOnline);
      statusMap[userId] = isOnline;
    }));
    
    return statusMap;
  } catch (error) {
    console.error('批量获取用户在线状态失败:', error);
    return {};
  }
}

// 更新会话中的在线状态
const updateConversationOnlineStatus = (userId, isOnline) => {
  const conversation = conversationList.value.find(conv => conv.targetUserId === userId);
  if (conversation) {
    console.log(`API更新用户 ${userId} 在线状态为: ${isOnline ? '在线' : '离线'}`);
    conversation.isOnline = isOnline;
  }
}

const loadData = async () => {
  await messageStore.fetchConversationList()
  // 打印会话列表查看数据结构
  console.log('MessagePage显示的会话列表:', conversationList.value)
  
  // 添加检查 - 如果用户已登出，将所有会话用户标记为离线
  if (!userStore.isLoggedIn) {
    console.log('用户已登出，将所有会话中的用户标记为离线');
    conversationList.value.forEach(conv => {
      if (conv.targetUserId) {
        conv.isOnline = false;
      }
    });
    return;
  }
  
  // 遍历会话列表，确保每个会话都有正确的用户信息
  for (const conversation of conversationList.value) {
    // 如果缺少头像或昵称，尝试获取用户信息
    if (conversation.targetUserId && 
        (!conversation.targetUserAvatar || !conversation.targetUserNickname || 
         conversation.targetUserAvatar === 'https://img01.yzcdn.cn/vant/cat.jpeg')) {
      try {
        console.log(`尝试获取用户 ${conversation.targetUserId} 的详细信息`);
        const response = await request.get(`/user/profile/${conversation.targetUserId}`);
        if (response && response.data) {
          const userInfo = response.data;
          // 确保更新会话中的用户信息
          if (userInfo.avatar) {
            console.log(`更新用户 ${conversation.targetUserId} 的头像:`, userInfo.avatar);
            conversation.targetUserAvatar = userInfo.avatar;
          }
          if (userInfo.nickname || userInfo.username) {
            console.log(`更新用户 ${conversation.targetUserId} 的昵称:`, userInfo.nickname || userInfo.username);
            conversation.targetUserNickname = userInfo.nickname || userInfo.username;
            conversation.targetUserName = userInfo.nickname || userInfo.username;
          }
        }
      } catch (error) {
        console.error(`获取用户 ${conversation.targetUserId} 信息失败:`, error);
      }
    }
  }
  
  // 检查网络状态
  if (!window.navigator.onLine) {
    console.log('当前网络离线，跳过WebSocket连接')
    return
  }
  
  // 检查WebSocket连接状态，如果未连接则尝试连接
  if (!messageStore.isWebSocketConnected) {
    console.log('MessagePage: WebSocket未连接，尝试建立连接')
    await ensureQuietWebSocketConnection()
  } else {
    console.log('MessagePage: WebSocket已连接，无需重新连接')
  }
  
  // 获取所有会话目标用户的ID列表
  const userIds = conversationList.value
    .map(conv => conv.targetUserId)
    .filter(id => id);
  
  if (userIds.length > 0) {
    // 首先通过API获取在线状态（直接从数据库）
    try {
      console.log('通过API获取所有用户在线状态:', userIds);
      await fetchMultipleUserOnlineStatus(userIds);
    } catch (error) {
      console.error('API获取在线状态失败:', error);
    }
    
    // 手动请求所有会话目标用户的在线状态（通过WebSocket补充）
    if (messageStore.isWebSocketConnected) {
      console.log('通过WebSocket补充请求所有会话目标用户的在线状态');
      
      // 给WebSocket连接一些时间确保完全建立
      setTimeout(() => {
        // 使用新添加的方法请求多个用户的在线状态
        console.log('延迟后请求在线状态，确保WebSocket已建立:', userIds);
        messageStore.requestMultipleUserOnlineStatus(userIds);
      }, 1000);
      
      // 再次请求，确保状态同步
      setTimeout(() => {
        messageStore.requestMultipleUserOnlineStatus(userIds);
      }, 3000);
    }
  }
}

// 处理头像加载错误
const handleAvatarError = (event, conversation) => {
  console.warn('头像加载失败:', conversation.conversationId, conversation.targetUserId);
  event.target.src = 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/08/deepseek-color.png';
}

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
      console.log('设置AI头像:', aiSettings.value.avatar);
    }
    else {
      console.warn('获取AI设置失败或无数据:', response);
    }
  } catch (error) {
    console.error('获取AI助手配置失败:', error);
    // 出错时确保AI仍然显示，使用默认配置
    aiSettings.value = {
      enabled: true,
      name: 'Social AI',
      avatar: 'https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png',
      welcomeMessage: '你好！我是你的AI助手，有什么我可以帮你的吗？'
    };
  }
};

// 跳转到AI聊天页面
const goToAiChat = () => {
  router.push('/ai-chat');
};
</script>

<style scoped>
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fa;
  overflow: hidden; /* 修改为hidden，阻止外层容器滚动 */
}

.page-title {
  padding: 32px 16px 16px; /* 增加顶部padding */
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex-shrink: 0;
  background-color: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.content-container {
  flex: 1;
  overflow-y: auto; /* 只允许内容区域滚动 */
  padding-top: 24px; /* 增加内容区域的顶部padding */
  padding-bottom: 50px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  -webkit-overflow-scrolling: touch; /* 增加iOS流畅滚动 */
}

/* 移除外层容器的滚动条样式，因为它现在不应该滚动 */
.page-container::-webkit-scrollbar {
  display: none;
}

/* 保留内容区域的滚动条隐藏 */
.content-container::-webkit-scrollbar {
  display: none;
}

.loading-container,
.empty-container,
.login-tip {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  gap: 20px;
}

.text-secondary {
  color: #666;
  font-size: 0.9em;
}

.conversation-list {
  margin-top: 5px; /* 减少列表顶部留白，避免过多空白 */
  padding: 0 16px;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  position: relative;
  border-radius: 12px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.chat-item:active {
  background-color: #f9f9ff;
  transform: scale(0.99);
}

.avatar-container {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.avatar-grid {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background-color: #f0f0f0;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #ccc;
  border: 2px solid #fff;
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.online-status.is-online {
  background-color: #4CAF50; /* 绿色表示在线 */
  animation: pulse 2s infinite;
}

.online-status.is-offline {
  background-color: #9e9e9e; /* 灰色表示离线 */
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(76, 175, 80, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
  }
}

.online-text {
  font-size: 12px;
  color: #4CAF50;
  margin-left: 4px;
  font-weight: normal;
}

.offline-text {
  font-size: 12px;
  color: #9e9e9e;
  margin-left: 4px;
  font-weight: normal;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-content {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 6px;
}

.chat-name {
  font-weight: 600;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  color: #333;
}

.chat-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 8px;
}

.chat-message {
  display: flex;
  align-items: center;
}

.message-text {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  line-height: 1.5;
}

.notification-control {
  margin-left: 16px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: all 0.2s;
}

.notification-control:active {
  background-color: rgba(0, 0, 0, 0.05);
}

.login-tip {
  padding: 20px;
}

/* AI助手入口样式 */
.ai-assistant-entry {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f0f4ff;
  border-radius: 12px;
  margin: 5px 0 16px; /* 减少顶部margin，因为content-container已有padding */
  position: relative;
  box-shadow: 0 2px 8px rgba(94, 114, 228, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid #5e72e4;
}

.ai-assistant-entry:active {
  background-color: #e8eeff;
  transform: scale(0.98);
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

.divider {
  display: flex;
  align-items: center;
  margin: 16px 0;
  color: #999;
  font-size: 14px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #eee;
}

.divider-text {
  padding: 0 10px;
  color: #777;
  font-size: 13px;
  font-weight: 500;
}

/* 删除按钮样式 */
.delete-button {
  height: 100%;
}

/* 滑动单元格样式 */
:deep(.van-swipe-cell) {
  margin-bottom: 10px;
}

:deep(.van-swipe-cell__wrapper) {
  border-radius: 12px;
  overflow: hidden;
}

/* 确保页面布局在不同高度的屏幕上也能固定 */
html, body {
  height: 100%;
  overflow: hidden;
  margin: 0;
  padding: 0;
}
</style> 