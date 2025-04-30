<template>
  <div class="sketch-chat-page">
    <van-nav-bar
      left-arrow
      @click-left="onClickLeft"
      class="sketch-nav-bar"
    >
      <template #title>
        <div class="chat-title">
          <span>{{ currentConversation?.targetUserName || currentConversation?.targetUserNickname || '聊天' }}</span>
          <span v-if="currentConversation?.isOnline === true" class="online-status">●</span>
          <span v-else-if="currentConversation?.isOnline === false" class="offline-status">○</span>
        </div>
      </template>
    </van-nav-bar>
    
    <div class="chat-container sketch-container" ref="chatContainer">
      <div class="sketch-background"></div>
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="sketch-refresh">
        <div v-if="loading" class="loading-container">
          <div class="sketch-loading">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
        
        <div v-else-if="sortedMessages.length === 0" class="empty-container">
          <div class="sketch-empty">
            <div class="sketch-empty-icon"></div>
            <p>暂无消息，开始聊天吧</p>
          </div>
        </div>
        
        <div v-else class="message-list sketch-messages" :key="forceUpdateKey">
          <!-- 顶部填充空间，确保有足够的滚动空间 -->
          <div class="top-spacer" :style="topSpacerHeight"></div>
          
          <div
            v-for="message in sortedMessages"
            :key="message.messageId"
            :class="['message-item', message.isSelf ? 'self' : 'other']"
          >
            <div class="sketch-avatar">
              <van-image
                :src="getMessageAvatar(message)"
                round
                width="40"
                height="40"
                fit="cover"
                class="avatar-image"
                @error="handleAvatarError($event, message)"
              />
            </div>
            
            <div class="message-content">
              <div class="message-sender" v-if="!message.isSelf && showSenderName">
                {{ message.senderNickname || getMessageSenderName(message) }}
              </div>
              <div class="message-bubble-container">
                <div class="message-bubble sketch-bubble">
                  <div v-html="formatMessageContent(message.content)"></div>
                  <small v-if="isDev && false" class="debug-info">
                    {{ message.isSelf ? '(我)' : '(对方)' }} ID:{{ message.messageId }}
                  </small>
                </div>
                <div class="message-status" v-if="message.isSelf">
                  <span v-if="message.status === 'sending'" class="sketch-status sending"></span>
                  <span v-else-if="message.status === 'sent'" class="sketch-status sent">✓</span>
                  <span v-else-if="message.status === 'failed'" class="sketch-status failed">!</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 滚动到底部的锚点元素 -->
          <div ref="bottomAnchor" class="bottom-anchor"></div>
        </div>
      </van-pull-refresh>
    </div>
    
    <div class="input-container sketch-input">
      <van-icon 
        name="smile-o" 
        size="24" 
        class="emoji-btn"
        @click="toggleEmojiPanel"
      />
      <van-field
        v-model="messageContent"
        placeholder="输入消息..."
        :border="false"
        class="message-input"
        maxlength="500"
        @keypress.enter.prevent="sendMessage"
      >
        <template #right-icon>
          <button 
            class="sketch-send-btn" 
            :class="{ 'active': messageContent.trim() }" 
            :disabled="!messageContent.trim()"
            @click="sendMessage"
          >
            发送
          </button>
        </template>
      </van-field>
      
      <!-- 表情选择面板 -->
      <div class="emoji-panel" v-show="showEmojiPanel">
        <div class="emoji-grid">
          <div 
            v-for="emoji in emojiList" 
            :key="emoji" 
            class="emoji-item"
            @click="insertEmoji(emoji)"
          >
            {{ emoji }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '../stores/message'
import { useUserStore } from '../stores/user'
import { showToast } from 'vant'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()

// 状态
const messageContent = ref('')
const chatContainer = ref(null)
const bottomAnchor = ref(null)  // 底部锚点引用
const refreshing = ref(false)
const forceUpdateKey = ref(0)
const shouldScrollToBottom = ref(true)  // 是否应该滚动到底部
const showEmojiPanel = ref(false) // 表情面板显示状态

// 表情列表
const emojiList = [
  '😊', '😂', '😍', '🥰', '😘', '🤗', '🙄', '😮', '😯', '😲', 
  '😳', '🥺', '😢', '😭', '😱', '😖', '😞', '😤', '😠', '🤬',
  '🤔', '🤫', '🤭', '🤥', '😬', '😰', '😨', '😵', '🤪', '🥴',
  '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥶', '🥵', '👍', '👎',
  '👏', '🙌', '🤝', '👊', '✌️', '🤞', '🤘', '👌', '❤️', '💔',
  '💯', '✨', '🔥', '🎉', '🎂', '🎁', '🌹', '🌈', '☀️', '🌙'
]

// 切换表情面板显示状态
const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value
}

// 插入表情到输入框
const insertEmoji = (emoji) => {
  messageContent.value += emoji
}

// 点击页面其他区域关闭表情面板
const closeEmojiPanel = (event) => {
  // 如果点击的不是表情按钮或表情面板内部元素，则关闭面板
  const isEmojiBtn = event.target.closest('.emoji-btn')
  const isEmojiPanel = event.target.closest('.emoji-panel')
  
  if (!isEmojiBtn && !isEmojiPanel && showEmojiPanel.value) {
    showEmojiPanel.value = false
  }
}

// 计算属性
const conversationId = computed(() => route.params.id)
const loading = computed(() => messageStore.loading)
const messages = computed(() => messageStore.messages)
const currentConversation = computed(() => messageStore.currentConversation)
const isDev = computed(() => import.meta.env.DEV)

// 强制刷新视图并滚动到底部
const forceUpdate = () => {
  forceUpdateKey.value += 1
  nextTick(scrollToBottom)
}

// 检查滚动API支持
const hasInstantScrollBehavior = () => {
  try {
    const options = { behavior: 'instant' };
    return 'instant' in options;
  } catch (e) {
    return false;
  }
}

// 获取最佳滚动行为
const getScrollBehavior = () => {
  return hasInstantScrollBehavior() ? 'instant' : 'auto';
}

// 方法
const onClickLeft = () => {
  router.back()
}

const onRefresh = async () => {
  await messageStore.fetchMessages(conversationId.value)
  refreshing.value = false
}

const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  
  const content = messageContent.value.trim()
  messageContent.value = ''
  
  try {
    // 检查会话ID
    if (!conversationId.value) {
      showToast('发送失败，会话异常');
      return;
    }
    
    // 检查当前会话信息
    if (!currentConversation.value || !currentConversation.value.targetUserId) {
      showToast('发送失败，接收者信息异常');
      return;
    }
    
    // 设置发送消息后应该滚动到底部
    shouldScrollToBottom.value = true;
    
    const success = await messageStore.sendMessage(content)
    
    if (success) {
      // 发送成功后立即滚动到底部
      requestAnimationFrame(() => {
        scrollToBottom();
      });
    } else {
      showToast('发送消息失败，请重试')
    }
  } catch (error) {
    showToast('发送失败，系统异常');
  }
}

// 监听器 - 当消息列表变化时确保滚动到底部
watch(messages, (newMessages, oldMessages) => {
  // 只要消息列表有变化（如首次加载完成或收到新消息）就触发滚动
  // 在首次加载（oldMessages为空）或消息数量变化时，始终滚动到底部
  if (!oldMessages.length || (newMessages.length !== oldMessages.length)) {
    // 强制延迟滚动，确保DOM已完全更新
    nextTick(() => {
      requestAnimationFrame(() => {
        // 强制滚动到底部，无论当前是否在底部
        scrollToBottom();
        
        // 二次确认滚动到底部
        setTimeout(scrollToBottom, 100);
      });
    });
  } else if (shouldScrollToBottom.value) {
    // 如果用户当前在底部，新消息也滚动到底部
    nextTick(() => {
      requestAnimationFrame(() => {
        scrollToBottom();
      });
    });
  }
}, { deep: true });

// 首次打开聊天页面，滚动到底部
const initialScrollToBottom = () => {
  // 设置应该滚动到底部标志
  shouldScrollToBottom.value = true;
  
  // 使用多种方式确保滚动到底部生效
  nextTick(() => {
    requestAnimationFrame(() => {
      // 尝试多种滚动方法
      if (bottomAnchor.value) {
        try {
          bottomAnchor.value.scrollIntoView({ behavior: 'auto', block: 'end' });
        } catch (e) {
          console.error('ScrollIntoView failed:', e);
        }
      }
      
      // 无论上面是否成功，都尝试直接设置scrollTop
      if (chatContainer.value) {
        chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
      }
      
      // 二次确认，确保在DOM完全更新后再次尝试滚动
      setTimeout(() => {
        if (chatContainer.value) {
          chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
        }
        
        // 三次确认，处理可能的延迟加载情况
        setTimeout(() => {
          if (chatContainer.value) {
            chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
          }
        }, 300);
      }, 100);
    });
  });
};

// 滚动到底部 - 增强版
const scrollToBottom = () => {
  // 无论是否需要滚动，强制滚动到底部
  if (!chatContainer.value) return;
  
  // 尝试多种滚动方法保证至少一种生效
  try {
    if (bottomAnchor.value) {
      bottomAnchor.value.scrollIntoView({ behavior: getScrollBehavior(), block: 'end' });
    }
  } catch (error) {
    console.error('Error in scrollIntoView:', error);
  }
  
  // 无论上面是否成功，都尝试直接设置scrollTop
  try {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  } catch (error) {
    console.error('Error setting scrollTop:', error);
  }
}

// 监听滚动事件
const onScroll = () => {
  // 不进行频繁的滚动检查，只在用户主动滚动时记录一下状态
  if (chatContainer.value) {
    const { scrollTop, scrollHeight, clientHeight } = chatContainer.value;
    const atBottom = (scrollTop + clientHeight) >= (scrollHeight - 20);
    shouldScrollToBottom.value = atBottom;
  }
}

// 监听route.params.id变化，切换会话时重新获取消息
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    messageStore.fetchMessages(newId);
  }
});

// 定时获取最新消息
let messageRefreshInterval = null;

const startMessageRefresh = () => {
  // 每20秒刷新一次消息，降低频率
  messageRefreshInterval = setInterval(() => {
    if (userStore.isLoggedIn && conversationId.value && document.visibilityState === 'visible') {
      messageStore.fetchMessages(conversationId.value).then(() => {
        // 静默更新，不触发UI更新提示
        nextTick(scrollToBottom);
      });
    }
  }, 20000);
};

const stopMessageRefresh = () => {
  if (messageRefreshInterval) {
    clearInterval(messageRefreshInterval);
    messageRefreshInterval = null;
  }
};

// 消息回调函数 - 收到新消息时立即处理
const handleNewMessage = (message) => {
  if (message.conversationId === conversationId.value) {
    // 新消息到达，立即处理
    forceUpdateKey.value += 1; // 触发界面更新
    
    // 如果当前在底部，则自动滚动到底部
    if (shouldScrollToBottom.value) {
      requestAnimationFrame(() => {
        scrollToBottom();
      });
    } else if (!message.isSelf) {
      // 如果不在底部，且是对方发来的消息，显示新消息提示
      showToast('收到新消息，向上滚动查看');
    }
  }
}

// 处理在线状态更新的回调
const handleOnlineStatusChange = (data) => {
  // 检查是否是当前会话用户的状态更新
  if (currentConversation.value && currentConversation.value.targetUserId === data.userId) {
    // 更新当前会话的在线状态
    currentConversation.value.isOnline = data.isOnline;
    console.log(`聊天对象 ${data.userId} 在线状态已更新为: ${data.isOnline ? '在线' : '离线'}`);
  }
}

// 从服务器获取用户在线状态
const fetchUserOnlineStatusFromAPI = async (userId) => {
  if (!userId) return false;
  
  try {
    console.log('通过API获取用户在线状态:', userId);
    const response = await request.get(`/user/status/${userId}`);
    if (response && response.data !== undefined) {
      const isOnline = response.data === true || response.data === 'ONLINE';
      
      // 更新当前会话的在线状态
      if (currentConversation.value && currentConversation.value.targetUserId === userId) {
        console.log(`API更新聊天对象 ${userId} 在线状态为: ${isOnline ? '在线' : '离线'}`);
        currentConversation.value.isOnline = isOnline;
      }
      
      return isOnline;
    }
    return false;
  } catch (error) {
    console.error('获取用户在线状态失败:', error);
    return false;
  }
}

// 获取消息头像的函数
const getMessageAvatar = (message) => {
  console.log('获取消息头像:', message.messageId, message.isSelf ? '自己' : '对方');
  
  // 如果是自己发送的消息
  if (message.isSelf) {
    const avatar = userStore.avatar;
    console.log('使用当前用户头像:', avatar || '默认头像');
    return avatar || 'https://img01.yzcdn.cn/vant/cat.jpeg';
  }
  
  // 如果是别人发送的消息
  // 优先使用消息中的发送者头像
  if (message.senderAvatar && message.senderAvatar !== 'undefined' && message.senderAvatar !== 'null') {
    console.log('使用消息中的发送者头像:', message.senderAvatar);
    return message.senderAvatar;
  }
  
  // 其次使用会话中的目标用户头像
  if (currentConversation.value && currentConversation.value.targetUserAvatar) {
    console.log('使用会话中的目标用户头像:', currentConversation.value.targetUserAvatar);
    return currentConversation.value.targetUserAvatar;
  }
  
  // 最后使用默认头像
  console.log('未找到合适头像，使用默认头像');
  return 'https://img01.yzcdn.cn/vant/cat.jpeg';
};

// 获取消息发送者昵称
const getMessageSenderName = (message) => {
  if (message.isSelf) {
    return userStore.nickname || userStore.username || '我';
  }
  
  if (message.senderNickname) {
    return message.senderNickname;
  }
  
  if (currentConversation.value) {
    return currentConversation.value.targetUserNickname || 
           currentConversation.value.targetUserName || 
           `用户${message.senderId || '未知'}`;
  }
  
  return `用户${message.senderId || '未知'}`;
};

// 是否显示发送者昵称
const showSenderName = computed(() => {
  // 根据需要决定是否显示发送者昵称，例如在群聊中显示，私聊不显示
  // 这里我们默认不显示，因为是一对一聊天
  return false;
});

// 处理头像加载错误
const handleAvatarError = (event, message) => {
  console.warn('头像加载失败:', message.isSelf ? '自己' : '对方', message.messageId);
  // 设置默认头像
  event.target.src = 'https://img01.yzcdn.cn/vant/cat.jpeg';
};

// 生命周期钩子
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }
  
  // 添加滚动事件监听
  if (chatContainer.value) {
    chatContainer.value.addEventListener('scroll', onScroll);
  }
  
  // 注册消息回调
  messageStore.registerMessageCallback(handleNewMessage);
  
  // 注册在线状态回调
  messageStore.registerOnlineStatusCallback(handleOnlineStatusChange);
  
  // 检查会话ID
  if (!conversationId.value) {
    showToast('无效的会话');
    router.push('/message');
    return;
  }
  
  // 标记正在进入聊天页面，用于触发额外的滚动行为
  const isEnteringChat = true;
  
  try {
    // 确保WebSocket连接已建立，使用静默连接方法
    await ensureQuietWebSocketConnection();
    
    // 从会话列表中查找当前会话
    if (messageStore.conversationList.length === 0) {
      await messageStore.fetchConversationList();
    }
    
    // 获取会话详情（此方法会检查targetUserId）
    let conversation = await messageStore.getConversationDetails(conversationId.value);
    
    // 如果没有找到会话，尝试判断是否是用户ID而不是会话ID
    if (!conversation) {
      try {
        // 尝试将会话ID当作用户ID来创建会话
        const possibleUserId = parseInt(conversationId.value);
        if (!isNaN(possibleUserId)) {
          const newConversation = await messageStore.createOrGetConversation(possibleUserId);
          
          if (newConversation) {
            // 使用正确的会话ID重新导航到聊天页面
            router.replace(`/chat/${newConversation.conversationId}`);
            return;
          }
        }
      } catch (convError) {
        // 错误处理
      }
      
      // 如果仍然失败，则返回消息列表
      showToast('无法加载会话，请返回重试');
      setTimeout(() => router.push('/message'), 1500);
      return;
    }
    
    // 加载消息
    await messageStore.fetchMessages(conversationId.value);
    
    // 确保会话中有用户信息，如果没有则更新
    if (currentConversation.value && currentConversation.value.targetUserId &&
        (!currentConversation.value.targetUserAvatar || !currentConversation.value.targetUserName)) {
      // 这里可以添加获取用户信息的逻辑
      console.log('尝试获取更多用户信息:', currentConversation.value.targetUserId);
      try {
        const userInfoResponse = await request.get(`/user/profile/${currentConversation.value.targetUserId}`);
        if (userInfoResponse && userInfoResponse.data) {
          const userInfo = userInfoResponse.data;
          // 更新会话中的用户信息
          currentConversation.value.targetUserName = userInfo.nickname || userInfo.username || currentConversation.value.targetUserName;
          currentConversation.value.targetUserNickname = userInfo.nickname || userInfo.username || currentConversation.value.targetUserNickname;
          currentConversation.value.targetUserAvatar = userInfo.avatar || currentConversation.value.targetUserAvatar;
          console.log('已更新对方用户信息:', currentConversation.value);
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
      }
    }
    
    // 确保WebSocket连接有效
    if (!messageStore.isWebSocketConnected) {
      console.log('在获取用户在线状态前确保WebSocket连接有效');
      await ensureQuietWebSocketConnection();
    }
    
    // 强制刷新DOM并滚动到底部
    forceUpdateKey.value += 1;
    
    // 通过API获取在线状态并通过WebSocket进行补充
    if (currentConversation.value && currentConversation.value.targetUserId) {
      // 首先通过API获取在线状态（直接从数据库）
      try {
        await fetchUserOnlineStatusFromAPI(currentConversation.value.targetUserId);
      } catch (error) {
        console.error('API获取在线状态失败:', error);
      }
      
      // 延迟一会儿再请求WebSocket在线状态，确保WebSocket连接已完全建立
      setTimeout(() => {
        // 通过WebSocket补充/更新在线状态
        if (currentConversation.value && currentConversation.value.targetUserId) {
          console.log('通过WebSocket请求聊天对象的在线状态:', currentConversation.value.targetUserId);
          messageStore.requestUserOnlineStatus(currentConversation.value.targetUserId);
          
          // 二次确认，100ms后再次请求
          setTimeout(() => {
            messageStore.requestUserOnlineStatus(currentConversation.value.targetUserId);
          }, 100);
          
          // 三次确认，500ms后再次请求
          setTimeout(() => {
            messageStore.requestUserOnlineStatus(currentConversation.value.targetUserId);
          }, 500);
        }
      }, 1000);
    }
    
    // 使用多个嵌套的nextTick和requestAnimationFrame确保DOM完成渲染
    nextTick(() => {
      requestAnimationFrame(() => {
        // 强制滚动到底部
        initialScrollToBottom();
        
        // 二次确认滚动到底部
        setTimeout(initialScrollToBottom, 200);
        
        // 三次确认滚动到底部（处理从消息列表返回的情况）
        setTimeout(initialScrollToBottom, 500);
      });
    });
    
    // 启动定时刷新消息（备用方案，确保消息实时性）
    startMessageRefresh();
    
    // 额外的滚动逻辑 - 解决从消息列表返回后不在底部的问题
    if (isEnteringChat) {
      // 延迟执行，确保在路由转换和DOM更新完成后
      setTimeout(() => {
        // 强制更新并滚动到底部
        forceUpdateKey.value += 1;
        nextTick(() => {
          initialScrollToBottom();
        });
      }, 300);
      
      // 二次延迟执行，处理更复杂的加载情况
      setTimeout(() => {
        initialScrollToBottom();
      }, 800);
    }
  } catch (error) {
    showToast('加载聊天信息失败');
    
    // 出错后仍然尝试连接WebSocket
    messageStore.connectWebSocket();
  }
  
  // 添加点击事件监听，用于关闭表情面板
  document.addEventListener('click', closeEmojiPanel)
})

// 添加activated钩子，用于处理keepAlive的情况下从消息页面返回
const activated = () => {
  console.log('Chat page activated - scrolling to bottom');
  // 激活时总是强制滚动到底部
  nextTick(() => {
    requestAnimationFrame(() => {
      initialScrollToBottom();
    });
  });
  
  // 延迟滚动，处理可能的异步情况
  setTimeout(() => {
    initialScrollToBottom();
  }, 300);
};

// 定期检查WebSocket连接状态，减少频率到30秒一次
const wsCheckInterval = setInterval(() => {
  if (!messageStore.isWebSocketConnected && userStore.isLoggedIn && document.visibilityState === 'visible') {
    ensureQuietWebSocketConnection();
  }
}, 30000);

onBeforeUnmount(() => {
  // 清除定时器
  clearInterval(wsCheckInterval);
  stopMessageRefresh();
  
  // 移除滚动事件监听
  if (chatContainer.value) {
    chatContainer.value.removeEventListener('scroll', onScroll);
  }
  
  // 取消消息回调注册
  messageStore.unregisterMessageCallback(handleNewMessage);
  
  // 取消在线状态回调注册
  messageStore.unregisterOnlineStatusCallback(handleOnlineStatusChange);
  
  // 不断开WebSocket连接，允许在后台接收消息
  // messageStore.disconnectWebSocket()
  
  // 移除点击事件监听
  document.removeEventListener('click', closeEmojiPanel)
})

// 添加排序逻辑
const sortedMessages = computed(() => {
  return [...messages.value].sort((a, b) => {
    // 尝试多种可能的时间字段名
    const timeA = a.createTime || a.createdAt || a.timestamp || 0;
    const timeB = b.createTime || b.createdAt || b.timestamp || 0;
    return new Date(timeA) - new Date(timeB);
  });
});

// 计算顶部填充高度
const topSpacerHeight = computed(() => {
  // 如果消息数量很少，增加顶部填充，让聊天从底部开始
  // 假设每条消息平均高度为70px
  const estimatedMessageHeight = 70;
  const messageCount = sortedMessages.value.length;
  const messageHeight = messageCount * estimatedMessageHeight;
  
  // 获取聊天容器高度
  const containerHeight = chatContainer.value?.clientHeight || window.innerHeight * 0.7;
  
  if (messageHeight < containerHeight) {
    // 如果消息总高度小于容器高度，添加填充使消息显示在底部
    return `${Math.max(containerHeight - messageHeight, 0)}px`;
  }
  
  return '0px'; // 消息足够多时不需要额外填充
});

// 确保WebSocket连接不弹出提示
const ensureQuietWebSocketConnection = async () => {
  // 临时禁用WebSocket连接的toast提示
  const debugSetting = localStorage.getItem('debug_websocket');
  localStorage.removeItem('debug_websocket');
  
  try {
    // 尝试连接WebSocket
    await messageStore.connectWebSocket();
    console.log('WebSocket连接已静默建立');
  } catch (error) {
    console.error('WebSocket静默连接失败:', error);
  } finally {
    // 恢复之前的设置
    if (debugSetting) {
      localStorage.setItem('debug_websocket', debugSetting);
    }
  }
};

// 格式化消息内容，支持表情显示
const formatMessageContent = (content) => {
  if (!content) return '';
  // 如果内容中包含HTML危险字符，进行转义
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
</script>

<style scoped>
.sketch-chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #fafafa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.sketch-nav-bar {
  background-color: #fff;
  border-bottom: 1px dashed #ddd;
  box-shadow: none;
}

.sketch-nav-bar :deep(.van-nav-bar__title) {
  font-weight: normal;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.online-status {
  color: #4CAF50;
  font-size: 12px;
}

.offline-status {
  color: #9e9e9e;
  font-size: 12px;
}

.chat-container {
  flex: 1;
  overflow-y: scroll;
  padding: 16px 12px;
  position: relative;
  scrollbar-width: none;
  -ms-overflow-style: none;
  -webkit-overflow-scrolling: touch;
  overflow-scrolling: touch;
}

.chat-container::-webkit-scrollbar {
  display: none;
}

.sketch-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.05;
  pointer-events: none;
  background-image: radial-gradient(#666 1px, transparent 0);
  background-size: 20px 20px;
}

.sketch-refresh {
  min-height: 100%;
  background: transparent;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 200px;
}

.sketch-loading {
  display: flex;
  gap: 6px;
}

.sketch-loading span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #666;
  animation: sketch-loading 1.4s infinite ease-in-out both;
}

.sketch-loading span:nth-child(1) {
  animation-delay: -0.32s;
}

.sketch-loading span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes sketch-loading {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.sketch-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  font-size: 14px;
}

.sketch-empty-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 12px;
  border: 2px solid #ccc;
  border-radius: 50%;
  position: relative;
}

.sketch-empty-icon:before {
  content: "";
  position: absolute;
  top: 20px;
  left: 16px;
  width: 28px;
  height: 2px;
  background-color: #ccc;
  border-radius: 1px;
}

.sketch-empty-icon:after {
  content: "";
  position: absolute;
  top: 30px;
  left: 16px;
  width: 18px;
  height: 2px;
  background-color: #ccc;
  border-radius: 1px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
  min-height: 100%;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  position: relative;
  transition: all 0.3s ease;
}

.message-item.self {
  flex-direction: row-reverse;
}

.sketch-avatar {
  margin: 0 8px;
  position: relative;
  z-index: 1;
}

.sketch-avatar:after {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border: 1px solid #ddd;
  border-radius: 50%;
  z-index: -1;
}

.self .sketch-avatar:after {
  border-color: var(--primary-color, #1989fa);
  opacity: 0.3;
}

.message-content {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  max-width: 70%;
}

.self .message-content {
  align-items: flex-end;
}

.message-sender {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
  margin-left: 3px;
}

.message-bubble-container {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.self .message-bubble-container {
  flex-direction: row-reverse;
}

.sketch-bubble {
  padding: 10px 16px;
  border-radius: 18px;
  background-color: #fff;
  word-break: break-word;
  box-shadow: none;
  border: 1px solid #e0e0e0;
  position: relative;
  animation: sketch-draw 0.3s ease forwards;
  transform-origin: left center;
}

.self .sketch-bubble {
  background-color: var(--primary-light, #e8f0ff);
  border-color: var(--primary-color, #1989fa);
  transform-origin: right center;
}

@keyframes sketch-draw {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.message-status {
  margin: 0 6px;
  height: 16px;
  display: flex;
  align-items: center;
}

.sketch-status {
  font-size: 14px;
  line-height: 1;
}

.sketch-status.sending {
  position: relative;
  width: 12px;
  height: 12px;
}

.sketch-status.sending:after {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #999;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 0.5;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(0.5);
    opacity: 0.5;
  }
}

.sketch-status.sent {
  color: var(--primary-color, #1989fa);
}

.sketch-status.failed {
  color: #f56c6c;
}

.top-spacer {
  flex-shrink: 0;
}

.bottom-anchor {
  height: 1px;
  width: 100%;
  margin-top: 20px;
}

.sketch-input {
  padding: 10px;
  background-color: #fff;
  border-top: 1px dashed #e0e0e0;
}

.message-input {
  background-color: #f5f5f5;
  border-radius: 20px;
  position: relative;
}

.message-input:after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border: 1px solid #e0e0e0;
  border-radius: 22px;
  pointer-events: none;
  z-index: 1;
}

.sketch-send-btn {
  background: none;
  border: 1px solid var(--primary-color, #1989fa);
  color: var(--primary-color, #1989fa);
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
  outline: none;
}

.sketch-send-btn:before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border: 1px dashed var(--primary-color, #1989fa);
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.sketch-send-btn:hover:before,
.sketch-send-btn.active:before {
  opacity: 1;
}

.sketch-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sketch-send-btn:active {
  transform: scale(0.95);
}

/* Add custom CSS variables for colors */
:root {
  --primary-color: #5887e5;
  --primary-light: #e8f0ff;
  --text-primary: #333333;
  --text-secondary: #777777;
  --bg-primary: #ffffff;
  --bg-secondary: #f7f7f7;
  --bg-tertiary: #eff1f3;
  --border-light: #e0e0e0;
  --border-radius: 8px;
  --border-radius-lg: 12px;
  --border-radius-round: 20px;
  --transition-fast: 0.15s;
  --transition-normal: 0.25s;
  --ease-out: cubic-bezier(0.25, 0.8, 0.25, 1);
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 表情相关样式 */
.emoji-btn {
  padding: 8px 12px;
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}

.emoji-btn:hover {
  color: #1989fa;
}

.emoji-panel {
  position: absolute;
  bottom: 60px;
  left: 0;
  width: 100%;
  background-color: white;
  border-top: 1px solid #eee;
  padding: 16px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 999;
  max-height: 250px;
  overflow-y: auto;
  border-radius: 16px 16px 0 0;
  animation: slideUp 0.2s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
}

.emoji-item {
  font-size: 24px;
  text-align: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.emoji-item:hover {
  background-color: #f5f5f5;
  transform: scale(1.2);
}

.emoji-item:active {
  transform: scale(0.95);
}

/* 聊天消息气泡样式 */
.message-bubble {
  max-width: 70vw;
  padding: 10px 14px;
  border-radius: 18px;
  word-break: break-word;
  white-space: pre-wrap;
  font-size: 16px;
  line-height: 1.4;
}
</style> 