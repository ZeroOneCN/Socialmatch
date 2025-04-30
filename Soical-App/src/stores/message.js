import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { showToast, showLoadingToast, closeToast } from 'vant'
import request from '../utils/request'
import { useUserStore } from './user'

// 创建消息状态管理
export const useMessageStore = defineStore('message', () => {
  // 状态
  const conversationList = ref([])
  const currentConversation = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const socket = ref(null)
  const heartbeatTimer = ref(null)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 10
  const messageCallbacks = ref([]) // 添加消息回调存储
  const onlineStatusCallbacks = ref([]) // 添加在线状态回调存储
  const isNetworkOnline = ref(navigator.onLine)
  const lastConnectAttemptTime = ref(null)
  
  // 用户store
  const userStore = useUserStore()
  
  // 添加用户在线状态心跳检查定时器
  const userStatusHeartbeatTimer = ref(null);
  
  // 计算属性
  const unreadCount = computed(() => {
    return conversationList.value.reduce((count, conv) => {
      return count + (conv.unreadCount || 0)
    }, 0)
  })
  
  // 监听网络状态变化
  const setupNetworkListeners = () => {
    // 处理网络在线事件
    window.addEventListener('online', handleNetworkOnline)
    // 处理网络离线事件
    window.addEventListener('offline', handleNetworkOffline)
  }
  
  // 清理网络监听器
  const cleanupNetworkListeners = () => {
    window.removeEventListener('online', handleNetworkOnline)
    window.removeEventListener('offline', handleNetworkOffline)
  }
  
  // 处理网络恢复在线
  const handleNetworkOnline = () => {
    console.log('网络已恢复连接，尝试重连WebSocket')
    isNetworkOnline.value = true
    
    // 如果用户已登录但WebSocket未连接，尝试重连
    if (userStore.isLoggedIn && (!socket.value || socket.value.readyState !== WebSocket.OPEN)) {
      reconnectAttempts.value = 0 // 重置重连计数
      setTimeout(() => connectWebSocket(), 1000) // 延迟1秒重连，确保网络稳定
    }
  }
  
  // 处理网络离线
  const handleNetworkOffline = () => {
    console.log('网络已断开连接，停止WebSocket重连尝试')
    isNetworkOnline.value = false
    stopHeartbeat()
    
    // 标记连接断开，但不主动关闭，等待系统关闭
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      // 不发送关闭请求，因为网络已断开
      console.log('网络已断开，WebSocket将自动关闭')
    }
  }
  
  // 注册网络状态监听
  setupNetworkListeners()
  
  // 注册消息回调，当收到新消息时会触发
  const registerMessageCallback = (callback) => {
    if (typeof callback === 'function') {
      messageCallbacks.value.push(callback);
      return true;
    }
    return false;
  }
  
  // 移除消息回调
  const unregisterMessageCallback = (callback) => {
    const index = messageCallbacks.value.indexOf(callback);
    if (index >= 0) {
      messageCallbacks.value.splice(index, 1);
      return true;
    }
    return false;
  }
  
  // 触发所有消息回调
  const triggerMessageCallbacks = (message) => {
    messageCallbacks.value.forEach(callback => {
      try {
        callback(message);
      } catch (e) {
        console.error('执行消息回调时出错:', e);
      }
    });
  }
  
  // 注册在线状态回调
  const registerOnlineStatusCallback = (callback) => {
    if (typeof callback === 'function') {
      onlineStatusCallbacks.value.push(callback);
      return true;
    }
    return false;
  }
  
  // 移除在线状态回调
  const unregisterOnlineStatusCallback = (callback) => {
    const index = onlineStatusCallbacks.value.indexOf(callback);
    if (index >= 0) {
      onlineStatusCallbacks.value.splice(index, 1);
      return true;
    }
    return false;
  }
  
  // 触发所有在线状态回调
  const triggerOnlineStatusCallbacks = (data) => {
    onlineStatusCallbacks.value.forEach(callback => {
      try {
        callback(data);
      } catch (e) {
        console.error('执行在线状态回调时出错:', e);
      }
    });
  }
  
  // 获取会话列表
  const fetchConversationList = async () => {
    if (!userStore.isLoggedIn) return
    
    loading.value = true
    
    try {
      showLoadingToast({
        message: '加载中...',
        forbidClick: true,
        duration: 0
      })
      
      // 去掉/api前缀，因为baseURL已经包含了
      const response = await request.get('/chat/conversations')
      console.log('获取会话列表原始响应:', response)
      
      // 确保会话列表字段完整
      const conversations = (response.data || []).map(conv => {
        // 调试
        console.log(`处理会话 ID=${conv.conversationId}:`, conv);
        
        // 检查必要字段
        if (!conv.targetUserNickname) {
          console.warn('会话缺失目标用户昵称:', conv);
          conv.targetUserNickname = `用户${conv.targetUserId || '未知'}`;
        }
        
        if (!conv.targetUserAvatar) {
          console.log('使用默认头像');
          conv.targetUserAvatar = 'https://img01.yzcdn.cn/vant/cat.jpeg'; // 默认头像
        }
        
        if (conv.lastMessage === null || conv.lastMessage === undefined) {
          console.log('最后消息为空，设置默认值');
          conv.lastMessage = ''; // 确保lastMessage不为null
        }
        
        // 为了兼容性，增加targetUserName字段
        conv.targetUserName = conv.targetUserNickname;
        
        // 初始化在线状态为未知
        if (conv.isOnline === undefined) {
          conv.isOnline = null;
        }
        
        return conv;
      });
      
      console.log('处理后的会话列表:', conversations);
      conversationList.value = conversations;
      
      // 会话列表加载完成后，立即主动请求用户在线状态
      if (conversations.length > 0 && socket.value && socket.value.readyState === WebSocket.OPEN) {
        try {
          const userIds = conversations.map(conv => conv.targetUserId).filter(id => id);
          
          if (userIds.length > 0) {
            // 发送请求获取批量用户在线状态
            socket.value.send(JSON.stringify({
              type: 'GET_USER_STATUS',
              userIds: userIds
            }));
            
            console.log('已请求批量用户在线状态:', userIds);
            
            // 设置默认状态为未知
            console.log('设置所有会话默认在线状态为null');
            conversations.forEach(conv => {
              if (conv.isOnline === undefined) {
                console.log(`会话 ${conv.conversationId} 初始化在线状态为null`);
                conv.isOnline = null;
              } else {
                console.log(`会话 ${conv.conversationId} 当前在线状态为:`, conv.isOnline);
              }
            });
          }
        } catch (e) {
          console.error('请求用户在线状态失败:', e);
        }
      } else {
        console.log('无法请求在线状态: 会话数量=', conversations.length, 
                    '连接状态=', socket.value ? socket.value.readyState : 'null');
      }
      
      closeToast()
    } catch (error) {
      console.error('获取会话列表失败:', error)
      showToast('获取会话失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  // 获取会话消息
  const fetchMessages = async (conversationId) => {
    if (!conversationId) return
    
    loading.value = true
    
    try {
      // 先获取当前用户ID，确保在处理消息时可用
      const currentUserId = userStore.userId;
      console.log('当前登录用户ID:', currentUserId);
      
      // 获取当前会话信息，以便获取目标用户头像和昵称
      let currentConv = null;
      if (currentConversation.value && currentConversation.value.conversationId === conversationId) {
        currentConv = currentConversation.value;
      } else {
        currentConv = conversationList.value.find(c => c.conversationId === conversationId);
      }
      
      if (!currentConv) {
        console.log('未找到当前会话信息，尝试获取会话详情');
        currentConv = await getConversationDetails(conversationId);
      }
      
      // 去掉/api前缀，因为baseURL已经包含了
      const response = await request.get(`/chat/messages/${conversationId}`)
      
      // 处理每条消息，正确标记是否为自己发送的消息
      const processedMessages = (response.data || []).map(msg => {
        // 确保以发送者ID与当前用户ID的精确比较来判断消息归属
        const isSelf = msg.senderId === currentUserId;
        console.log(`处理消息 ID:${msg.messageId}, 发送者ID:${msg.senderId}, 当前用户ID:${currentUserId}, 是自己发送的:${isSelf}, 是否已读:${msg.isRead}`);
        
        // 确保消息包含发送者头像和昵称
        if (isSelf) {
          // 如果是自己发送的消息，使用当前用户的头像和昵称
          msg.senderAvatar = msg.senderAvatar || userStore.avatar;
          msg.senderNickname = msg.senderNickname || userStore.nickname || userStore.username;
          
          // 接收者信息
          if (currentConv) {
            msg.receiverAvatar = msg.receiverAvatar || currentConv.targetUserAvatar;
            msg.receiverNickname = msg.receiverNickname || currentConv.targetUserNickname || currentConv.targetUserName;
          }
        } else {
          // 如果是别人发送的消息，使用会话中的目标用户信息
          if (currentConv) {
            msg.senderAvatar = msg.senderAvatar || currentConv.targetUserAvatar;
            msg.senderNickname = msg.senderNickname || currentConv.targetUserNickname || currentConv.targetUserName;
          }
          
          // 接收者信息（就是当前用户）
          msg.receiverAvatar = msg.receiverAvatar || userStore.avatar;
          msg.receiverNickname = msg.receiverNickname || userStore.nickname || userStore.username;
        }
        
        return {
          ...msg,
          isSelf: isSelf, // 明确根据senderId判断是自己发送的
          status: msg.isRead ? 'read' : 'unread' // 根据数据库的isRead字段判断消息状态
        };
      });
      
      // 替换消息列表
      messages.value = processedMessages;
      
      // 设置当前会话
      const conv = conversationList.value.find(c => c.conversationId === conversationId)
      if (conv) {
        currentConversation.value = conv
        
        // 如果会话有未读消息，将其标记为已读
        if (conv.unreadCount > 0) {
          // 重置本地未读数
          conv.unreadCount = 0
          
          // 向服务器发送请求，标记消息为已读
          try {
            // 获取当前用户ID
            const currentUserId = userStore.userId;
            if (currentUserId) {
              // 使用正确的请求参数格式调用API
              await request.post('/chat/messages/read', null, {
                params: {
                  conversationId: conversationId,
                  receiverId: currentUserId
                }
              });
              console.log(`已向服务器标记会话 ${conversationId} 的消息为已读`);
            }
          } catch (error) {
            console.error('标记消息为已读失败:', error);
            // 失败不影响后续操作
          }
        }
        
        // 检查targetUserId是否存在
        if (!conv.targetUserId) {
          console.log('会话缺少targetUserId，尝试从服务器获取会话详情');
          await getConversationDetails(conversationId);
        }
        
        // 尝试从消息更新会话中的用户信息
        if (processedMessages.length > 0) {
          const latestMsg = processedMessages[processedMessages.length - 1];
          if (!latestMsg.isSelf && latestMsg.senderAvatar && latestMsg.senderAvatar !== 'undefined' && latestMsg.senderAvatar !== 'null') {
            conv.targetUserAvatar = latestMsg.senderAvatar;
          }
          if (!latestMsg.isSelf && latestMsg.senderNickname) {
            conv.targetUserNickname = latestMsg.senderNickname;
            conv.targetUserName = latestMsg.senderNickname;
          }
        }
        
        // 如果会话缺少目标用户头像或昵称，尝试从用户资料获取完整信息
        if (conv.targetUserId && (!conv.targetUserAvatar || !conv.targetUserNickname || 
            conv.targetUserAvatar === 'https://img01.yzcdn.cn/vant/cat.jpeg' ||
            conv.targetUserNickname === `用户${conv.targetUserId}`)) {
          try {
            console.log('尝试从用户资料中获取完整信息:', conv.targetUserId);
            const userProfileResponse = await request.get(`/user/profile/${conv.targetUserId}`);
            
            if (userProfileResponse && userProfileResponse.data) {
              const userProfile = userProfileResponse.data;
              
              // 使用UserProfile中的完整信息
              if (userProfile.avatar) {
                console.log('使用UserProfile头像更新会话:', userProfile.avatar);
                conv.targetUserAvatar = userProfile.avatar;
              }
              
              if (userProfile.nickname) {
                console.log('使用UserProfile昵称更新会话:', userProfile.nickname);
                conv.targetUserNickname = userProfile.nickname;
                conv.targetUserName = userProfile.nickname;
              }
            } else {
              // 尝试获取User基本信息作为备选
              const userResponse = await request.get(`/user/${conv.targetUserId}`);
              
              if (userResponse && userResponse.data) {
                const user = userResponse.data;
                
                if (user.avatar) {
                  console.log('使用User头像更新会话:', user.avatar);
                  conv.targetUserAvatar = user.avatar;
                }
                
                if (user.username && (!conv.targetUserNickname || conv.targetUserNickname === `用户${conv.targetUserId}`)) {
                  console.log('使用User用户名更新会话:', user.username);
                  conv.targetUserNickname = user.username;
                  conv.targetUserName = user.username;
                }
              }
            }
          } catch (error) {
            console.error('获取用户资料失败:', error);
          }
        }
      } else {
        // 如果会话列表中没有找到，尝试从服务器获取详情
        console.log('在会话列表中未找到当前会话，尝试从服务器获取会话详情');
        await getConversationDetails(conversationId);
      }
    } catch (error) {
      console.error('获取消息失败:', error)
      showToast('获取消息失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  
  // 获取会话详情
  const getConversationDetails = async (conversationId) => {
    try {
      console.log('获取会话详情:', conversationId);
      
      // 如果没有会话列表或会话列表为空，先获取会话列表
      if (!conversationList.value || conversationList.value.length === 0) {
        console.log('会话列表为空，先获取会话列表');
        await fetchConversationList();
      }
      
      // 再次尝试从列表中查找会话
      let conv = conversationList.value.find(c => c.conversationId === parseInt(conversationId) || c.conversationId === conversationId);
      
      if (conv) {
        console.log('在会话列表中找到会话:', conv);
        
        // 确保有targetUserId字段
        if (!conv.targetUserId && (conv.userAId || conv.userBId)) {
          console.log('会话缺少targetUserId字段，计算目标用户');
          // 如果userAId是当前用户，则targetUserId是userBId，反之亦然
          if (conv.userAId === userStore.userId) {
            conv.targetUserId = conv.userBId;
          } else {
            conv.targetUserId = conv.userAId;
          }
          console.log('计算得到targetUserId:', conv.targetUserId);
        }
        
        // 确保我们有完整的用户信息，如果没有则尝试获取
        if (conv.targetUserId && (!conv.targetUserAvatar || !conv.targetUserNickname || 
                                conv.targetUserAvatar === 'https://img01.yzcdn.cn/vant/cat.jpeg')) {
          try {
            console.log(`尝试获取用户 ${conv.targetUserId} 的详细信息`);
            const response = await request.get(`/user/profile/${conv.targetUserId}`);
            if (response && response.data) {
              const userInfo = response.data;
              // 更新会话中的用户信息
              if (userInfo.avatar) {
                console.log(`更新用户 ${conv.targetUserId} 的头像:`, userInfo.avatar);
                conv.targetUserAvatar = userInfo.avatar;
              }
              if (userInfo.nickname || userInfo.username) {
                console.log(`更新用户 ${conv.targetUserId} 的昵称:`, userInfo.nickname || userInfo.username);
                conv.targetUserNickname = userInfo.nickname || userInfo.username;
                conv.targetUserName = userInfo.nickname || userInfo.username;
              }
            }
          } catch (error) {
            console.error(`获取用户 ${conv.targetUserId} 信息失败:`, error);
          }
        }
        
        // 更新当前会话
        currentConversation.value = conv;
        return conv;
      } else {
        console.log('在会话列表中未找到会话，尝试获取会话消息');
        
        // 尝试通过获取会话消息来间接获取会话信息
        try {
          const msgResponse = await request.get(`/chat/messages/${conversationId}`);
          if (msgResponse && msgResponse.success && msgResponse.data && msgResponse.data.length > 0) {
            const message = msgResponse.data[0];
            console.log('通过消息获取到会话信息:', message);
            
            // 根据消息构造会话对象
            const targetUserId = message.senderId === userStore.userId ? message.receiverId : message.senderId;
            
            conv = {
              conversationId: conversationId,
              targetUserId: targetUserId,
              lastMessage: message.content,
              lastMessageTime: message.createTime
            };
            
            console.log('构造的会话信息:', conv);
            currentConversation.value = conv;
            return conv;
          }
        } catch (msgError) {
          console.error('获取会话消息失败:', msgError);
        }
        
        console.error('无法获取会话详情，在会话列表和消息中均未找到');
        return null;
      }
    } catch (error) {
      console.error('获取会话详情失败:', error);
    }
    return null;
  }
  
  // 发送消息
  const sendMessage = async (content, type = 1) => {
    if (!currentConversation.value || !content.trim()) return false
    
    const convId = currentConversation.value.conversationId
    let targetUserId = currentConversation.value.targetUserId;
    
    // 获取当前用户ID，确保发送者ID正确
    const currentUserId = userStore.userId;
    console.log('发送消息 - 当前用户ID:', currentUserId, '目标用户ID:', targetUserId);
    
    // 如果targetUserId为空，尝试获取会话详情
    if (!targetUserId) {
      console.log('发送消息前发现targetUserId为空，尝试获取会话详情');
      const conv = await getConversationDetails(convId);
      if (conv && conv.targetUserId) {
        targetUserId = conv.targetUserId;
        console.log('成功获取到targetUserId:', targetUserId);
      } else {
        console.error('无法获取目标用户ID，无法发送消息');
        showToast('发送失败，无法确定接收用户');
        return false;
      }
    }
    
    // 确保消息格式符合后端期望
    const message = {
      conversationId: convId,
      content: content.trim(),
      messageType: type,  // 使用后端期望的字段名
      senderId: currentUserId,  // 确保使用正确的当前用户ID
      receiverId: targetUserId,
      createTime: new Date().toISOString()
    }
    
    console.log('构建消息对象:', message);
    
    // 乐观UI更新 - 明确标记为自己发送的消息
    const optimisticMessage = {
      ...message,
      messageId: `temp-${Date.now()}`,
      status: 'sending',
      isSelf: true  // 这里一定是自己发送的消息
    }
    
    console.log('添加本地消息到列表，发送者ID:', message.senderId, 'isSelf:', optimisticMessage.isSelf);
    messages.value.push(optimisticMessage)
    
    try {
      // 始终使用HTTP API发送消息
      console.log('使用HTTP API发送消息');
      const response = await request.post('/chat/send', message);
      console.log('HTTP API响应结果:', response);
      
      // 判断响应是否成功：优先检查success字段，然后检查code字段
      if (!response || (response.success === false && response.code !== 200)) {
        console.error('服务器返回错误:', response);
        throw new Error(response?.message || '服务器返回错误状态');
      }
      
      // 更新消息状态为已发送
      const index = messages.value.findIndex(m => m.messageId === optimisticMessage.messageId)
      if (index >= 0) {
        messages.value[index].status = 'sent'
        // 如果服务器返回了真实的消息ID，更新它
        if (response.data && response.data.messageId) {
          messages.value[index].messageId = response.data.messageId
        }
      }
      
      // 更新会话最后消息（参数true表示是自己发送的消息）
      updateConversationLastMessage(convId, content, true)
      
      return true // 添加返回值表示发送成功
    } catch (error) {
      console.error('发送消息失败:', error)
      
      // 更新消息状态为发送失败
      const index = messages.value.findIndex(m => m.messageId === optimisticMessage.messageId)
      if (index >= 0) {
        messages.value[index].status = 'failed'
      }
      
      showToast('发送失败，请重试')
      return false // 添加返回值表示发送失败
    }
  }
  
  // 更新会话最后消息
  const updateConversationLastMessage = (conversationId, lastMessage, isFromSelf = true) => {
    const index = conversationList.value.findIndex(c => c.conversationId === conversationId)
    if (index >= 0) {
      const conv = conversationList.value[index];
      conv.lastMessage = lastMessage;
      conv.lastMessageTime = new Date().toISOString();
      
      // 只有在以下条件都满足时才增加未读计数：
      // 1. 消息不是自己发送的（是接收到的消息）
      // 2. 不是当前正在查看的会话，或者当前没有打开任何会话
      // 3. 当前用户是消息的接收者
      if (!isFromSelf && 
          (!currentConversation.value || currentConversation.value.conversationId !== conversationId) &&
          userStore.userId) {
        // 增加未读计数
        conv.unreadCount = (conv.unreadCount || 0) + 1;
        console.log(`增加会话 ${conversationId} 未读计数为:`, conv.unreadCount);
      }
      
      // 重新排序会话列表，最新消息的会话排在前面
      conversationList.value.sort((a, b) => {
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime);
      });
    }
  }
  
  // 连接WebSocket
  const connectWebSocket = () => {
    // 返回Promise以支持async/await调用
    return new Promise((resolve, reject) => {
      // 检查用户登录状态，用户登出时应该立即关闭现有的WebSocket连接
      if (!userStore.isLoggedIn) {
        console.log('用户已登出，关闭现有WebSocket连接');
        // 关闭已有连接
        if (socket.value) {
          try {
            socket.value.close();
            socket.value = null; // 清空引用
          } catch (e) {
            console.warn('关闭WebSocket连接出错:', e);
          }
        }
        reject(new Error('用户未登录'));
        return;
      }

      // 添加自我保护: 防止连续多次重连导致重复WebSocket实例
      const now = Date.now()
      if (lastConnectAttemptTime.value && (now - lastConnectAttemptTime.value < 2000)) {
        console.log('连接尝试过于频繁，跳过本次连接');
        resolve(false);
        return;
      }
      lastConnectAttemptTime.value = now;
    
      if (!userStore.isLoggedIn || !userStore.token) {
        console.error('用户未登录或缺少token，无法连接WebSocket');
        reject(new Error('用户未登录'));
        return;
      }
      
      // 检查网络连接
      if (!navigator.onLine) {
        console.error('当前网络离线，无法连接WebSocket');
        reject(new Error('网络离线'));
        return;
      }
      
      // 如果已经连接，直接返回成功
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        console.log('WebSocket已经连接，直接返回');
        resolve(true);
        return;
      }
      
      // 如果正在连接中，等待连接完成
      if (socket.value && socket.value.readyState === WebSocket.CONNECTING) {
        console.log('WebSocket正在连接中，等待连接完成');
        resolve(true);
        return;
      }
      
      // 重置错误状态
      let connectionError = null;
    
    // 确保userId是有效的数字
    if (!userStore.userId) {
      console.error('用户ID无效，将在获取用户信息后重试连接WebSocket');
      userStore.fetchUserInfo().then(() => {
        if (userStore.userId) {
          console.log('成功获取用户信息，尝试重新连接WebSocket');
            setTimeout(() => connectWebSocket().then(resolve).catch(reject), 1000);
          } else {
            reject(new Error('无法获取用户ID'));
        }
        }).catch(reject);
      return;
    }
    
    try {
      // 使用前端开发服务器的URL创建WebSocket连接
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      
      // 构建WebSocket URL
      let wsHost;
      
      // 根据环境变量配置的API基础URL构建WebSocket主机地址
      const apiUrl = import.meta.env.VITE_API_BASE_URL || '';
      
      if (apiUrl) {
        // 1. 从API URL中提取主机名和端口（如果有）
        // 例如从 "http://localhost:8080/api" 提取 "localhost:8080"
        const apiUrlMatch = apiUrl.match(/^https?:\/\/([^\/]+)/);
        if (apiUrlMatch && apiUrlMatch[1]) {
          wsHost = apiUrlMatch[1];
          console.log('从API URL中提取的WebSocket主机:', wsHost);
        } else {
          // 如果无法从API URL中提取，使用当前页面的主机
          wsHost = window.location.host;
          console.log('无法从API URL中提取主机，使用当前主机:', wsHost);
        }
      } else {
        // 如果未配置API URL，使用当前页面的主机
        wsHost = window.location.host;
        console.log('未配置API URL，使用当前主机:', wsHost);
      }
      
      // 构建完整的WebSocket URL
      const wsUrl = `${wsProtocol}//${wsHost}/ws/chat?token=${encodeURIComponent(userStore.token)}&userId=${encodeURIComponent(String(userStore.userId))}`;
      
      console.log('正在连接WebSocket，完整URL:', wsUrl);
      console.log('连接参数 - userId:', userStore.userId, '类型:', typeof userStore.userId, 'token长度:', userStore.token ? userStore.token.length : 0);
      
      // 关闭已有连接
      if (socket.value) {
        try {
          socket.value.close();
        } catch (e) {
          console.warn('关闭旧WebSocket连接出错:', e);
        }
      }
      
      // 创建WebSocket连接
      socket.value = new WebSocket(wsUrl);
      
      socket.value.onopen = () => {
        console.log('WebSocket连接已建立，readyState:', socket.value.readyState);
        
        // 仅当启用了调试模式时才显示连接提示，使用localStorage设置的特定标志
        if (localStorage.getItem('debug_websocket') === 'true') {
          showToast('聊天连接已建立');
        }
          
        // 发送初始的认证消息
        try {
          socket.value.send(JSON.stringify({
            type: 'AUTH',
            token: userStore.token,
            userId: userStore.userId
          }));
          console.log('已发送WebSocket认证消息');
          
          // 开始心跳检测
          startHeartbeat();
          
          // 开始用户在线状态心跳检查
          startUserStatusHeartbeat();
          
          // 重置重连尝试次数
          reconnectAttempts.value = 0;
        } catch (e) {
          console.error('发送初始认证消息失败:', e);
        }
        
        // 连接成功后，立即请求获取会话中用户的在线状态
        setTimeout(() => {
          if (conversationList.value.length > 0) {
            try {
              const userIds = conversationList.value.map(conv => conv.targetUserId).filter(id => id);
              
              if (userIds.length > 0) {
                // 发送请求获取批量用户在线状态
                socket.value.send(JSON.stringify({
                  type: 'GET_USER_STATUS',
                  userIds: userIds
                }));
                
                console.log('WebSocket连接后，请求用户在线状态:', userIds);
              }
            } catch (e) {
              console.error('请求用户在线状态失败:', e);
            }
          }
        }, 1000); // 延迟1秒发送请求，确保AUTH完成
        
        // 连接成功，返回Promise
        resolve(true);
      };
      
      socket.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          // 忽略错误消息，不显示在控制台
          if (data.error) {
            return;
          }
          
          // 只处理有效的消息
          if (data && data.type) {
            handleWebSocketMessage(data);
          }
        } catch (e) {
          // 静默处理解析错误
          if (import.meta.env.DEV) {
            console.error('解析WebSocket消息失败:', e);
          }
        }
      };
      
      socket.value.onclose = (event) => {
        console.log('WebSocket连接已关闭:', event);
          
        // 停止心跳
        stopHeartbeat();
          
        // 只有在用户登录的情况下进行重连
        if (userStore.isLoggedIn) {
          // 仅在显式启用了WebSocket调试模式时显示断开提示
          if (localStorage.getItem('debug_websocket') === 'true') {
            showToast('聊天连接已断开，正在重连...');
          }
          // 添加重连逻辑
          if (reconnectAttempts.value < maxReconnectAttempts) {
            console.log(`WebSocket断开后准备重连 (${reconnectAttempts.value + 1}/${maxReconnectAttempts})`);
            // 使用指数退避策略，避免立即重连
            const delay = Math.min(1000 * (2 ** reconnectAttempts.value), 30000);
            setTimeout(() => {
              if (userStore.isLoggedIn) {
                reconnectWebSocket();
              }
            }, delay);
          } else {
            console.error('WebSocket重连次数已达上限，停止重连');
          }
        }
      };
      
      socket.value.onerror = (error) => {
        console.error('WebSocket错误:', error);
        connectionError = error; // 记录错误
          
        // 停止心跳
        stopHeartbeat();
          
        // 记录错误详情，帮助调试
        console.log('WebSocket错误详情:', {
          readyState: socket.value ? socket.value.readyState : 'socket为空',
          url: socket.value ? socket.value.url : '无URL',
          errorType: error.type,
          errorMessage: error.message || '无错误消息',
          errorStack: new Error().stack // 捕获调用栈
        });
          
        // 如果用户已登录，启动重连逻辑
        if (userStore.isLoggedIn && reconnectAttempts.value < maxReconnectAttempts) {
          console.log(`WebSocket错误后准备重连 (${reconnectAttempts.value + 1}/${maxReconnectAttempts})`);
          // 使用指数退避策略，避免立即重连
          const delay = Math.min(1000 * (2 ** reconnectAttempts.value), 30000);
          setTimeout(() => {
            reconnectWebSocket();
          }, delay);
        }
          
        // 仅在显式启用了WebSocket调试模式时显示错误提示
        if (localStorage.getItem('debug_websocket') === 'true') {
          showToast('聊天连接错误，正在重连...');
        }
        
        // 拒绝Promise并附带错误信息
        reject(new Error(`WebSocket连接错误: ${error.type}`));
      };
    } catch (error) {
      console.error('建立WebSocket连接失败:', error);
      // 仅在显式启用了WebSocket调试模式时显示错误提示
      if (localStorage.getItem('debug_websocket') === 'true') {
        showToast('聊天连接失败，稍后将重试');
      }
      // 连接失败后，3秒后尝试重连
      setTimeout(() => {
        if (userStore.isLoggedIn) {
          console.log('连接失败后尝试重新连接WebSocket...');
            connectWebSocket().catch(err => {
              console.error('WebSocket重连失败:', err);
            });
        }
      }, 3000);
        
        // 连接失败时Promise拒绝
        reject(error);
    }
  });
};
  
  // 处理WebSocket消息
  const handleWebSocketMessage = async (data) => {
    // 检查数据有效性
    if (!data || !data.type) {
      return;
    }
    
    // 根据消息类型处理
    switch (data.type) {
      case 'SESSION_CLOSED':
        // 处理会话关闭消息
        console.log('收到服务器会话关闭通知:', data.reason);
        // 主动关闭WebSocket连接
        disconnectWebSocket();
        break;
        
      case 'CHAT':
        // 处理新聊天消息
        if (!data.data) {
          return;
        }
        
        const message = data.data;
        
        // 基本数据验证
        if (!message.conversationId || !message.senderId) {
          return;
        }
        
        // 获取当前用户ID
        const currentUserId = userStore.userId;
        
        // 如果receiverId为空，尝试从当前上下文推断
        if (!message.receiverId && currentUserId) {
          // 如果是自己发的消息，接收者应该是对方
          if (message.senderId === currentUserId && currentConversation.value) {
            message.receiverId = currentConversation.value.targetUserId;
          } 
          // 如果是别人发的消息，接收者应该是自己
          else if (message.senderId !== currentUserId) {
            message.receiverId = currentUserId;
          }
        }
        
        // 确定消息是否是自己发送的，根据发送者ID判断
        const isSelfMessage = message.senderId === currentUserId;
        
        // 确定是否是消息的接收者
        const isMessageReceiver = message.receiverId === currentUserId;
        
        // 先检查该会话是否在列表中
        let conversationIndex = conversationList.value.findIndex(c => c.conversationId === message.conversationId);
        let conversation = null;
        
        // 如果会话不存在，可能是新会话，或者会话列表未加载
        if (conversationIndex === -1) {
          // 构建新会话对象
          const targetUserId = isSelfMessage ? message.receiverId : message.senderId;
          
          // 确定目标用户信息
          let targetUserNickname = '';
          let targetUserAvatar = '';
          
          if (isSelfMessage) {
            // 如果是自己发送的消息，目标用户是接收者
            targetUserNickname = message.receiverNickname || `用户${targetUserId}`;
            targetUserAvatar = message.receiverAvatar || 'https://img01.yzcdn.cn/vant/cat.jpeg';
          } else {
            // 如果是接收的消息，目标用户是发送者
            targetUserNickname = message.senderNickname || `用户${targetUserId}`;
            targetUserAvatar = message.senderAvatar || 'https://img01.yzcdn.cn/vant/cat.jpeg';
          }
          
          const newConversation = {
            conversationId: message.conversationId,
            targetUserId: targetUserId,
            targetUserName: targetUserNickname,
            targetUserNickname: targetUserNickname,
            targetUserAvatar: targetUserAvatar,
            lastMessage: message.content,
            lastMessageTime: message.createTime || new Date().toISOString(),
            unreadCount: (!isSelfMessage && isMessageReceiver && !message.isRead) ? 1 : 0 // 根据消息的isRead字段判断是否计入未读
          };
          
          // 尝试从服务器获取用户完整信息
          try {
            const userProfileResponse = await request.get(`/user/profile/${targetUserId}`);
            if (userProfileResponse && userProfileResponse.data) {
              const userProfile = userProfileResponse.data;
              
              // 优先使用UserProfile中的数据
              if (userProfile.avatar) {
                console.log(`使用UserProfile中的头像更新会话:`, userProfile.avatar);
                newConversation.targetUserAvatar = userProfile.avatar;
              }
              
              if (userProfile.nickname) {
                console.log(`使用UserProfile中的昵称更新会话:`, userProfile.nickname);
                newConversation.targetUserName = userProfile.nickname;
                newConversation.targetUserNickname = userProfile.nickname;
              }
            } else {
              // 获取User基本信息作为备选
              const userResponse = await request.get(`/user/${targetUserId}`);
              if (userResponse && userResponse.data) {
                const user = userResponse.data;
                
                if (user.avatar) {
                  console.log(`使用User中的头像更新会话:`, user.avatar);
                  newConversation.targetUserAvatar = user.avatar;
                }
                
                if (user.username && (!newConversation.targetUserNickname || newConversation.targetUserNickname === `用户${targetUserId}`)) {
                  console.log(`使用User中的用户名更新会话:`, user.username);
                  newConversation.targetUserName = user.username;
                  newConversation.targetUserNickname = user.username;
                }
              }
            }
          } catch (error) {
            console.error('获取用户资料失败:', error);
          }
          
          // 添加到会话列表
          conversationList.value.unshift(newConversation);
          conversationIndex = 0;
          conversation = newConversation;
          
          // 通知用户有新消息
          if (!isSelfMessage) {
            showToast(`收到来自 ${newConversation.targetUserNickname} 的新消息`);
          }
        } else {
          // 更新现有会话
          conversation = conversationList.value[conversationIndex];
          
          // 更新会话信息 - 根据消息来源更新对应信息
          if (!isSelfMessage) {
            // 如果是别人发来的消息，更新发送者的信息
            if (message.senderAvatar && message.senderAvatar !== 'undefined' && message.senderAvatar !== 'null') {
              conversation.targetUserAvatar = message.senderAvatar;
            }
            if (message.senderNickname) {
              conversation.targetUserNickname = message.senderNickname;
              conversation.targetUserName = message.senderNickname;
            }
            
            // 如果头像或昵称不完整，尝试获取完整用户资料
            if (!conversation.targetUserAvatar || 
                !conversation.targetUserNickname || 
                conversation.targetUserNickname === `用户${conversation.targetUserId}` ||
                conversation.targetUserAvatar === 'https://img01.yzcdn.cn/vant/cat.jpeg') {
              
              try {
                // 异步获取用户资料，不阻塞消息处理
                const fetchUserProfile = async () => {
                  const userProfileResponse = await request.get(`/user/profile/${conversation.targetUserId}`);
                  if (userProfileResponse && userProfileResponse.data) {
                    const userProfile = userProfileResponse.data;
                    
                    if (userProfile.avatar) {
                      console.log(`异步更新会话头像:`, userProfile.avatar);
                      conversation.targetUserAvatar = userProfile.avatar;
                    }
                    
                    if (userProfile.nickname) {
                      console.log(`异步更新会话昵称:`, userProfile.nickname);
                      conversation.targetUserNickname = userProfile.nickname;
                      conversation.targetUserName = userProfile.nickname;
                    }
                  }
                };
                
                fetchUserProfile().catch(e => console.error('异步获取用户资料失败:', e));
              } catch (error) {
                console.error('获取用户资料失败:', error);
              }
            }
          } else if (isSelfMessage && message.receiverAvatar && message.receiverNickname) {
            // 如果是自己发的消息，且包含接收者信息，也更新目标用户信息
            if (message.receiverAvatar !== 'undefined' && message.receiverAvatar !== 'null') {
              conversation.targetUserAvatar = message.receiverAvatar;
            }
            if (message.receiverNickname) {
              conversation.targetUserNickname = message.receiverNickname;
              conversation.targetUserName = message.receiverNickname;
            }
          }
          
          // 更新最后消息
          conversation.lastMessage = message.content;
          conversation.lastMessageTime = message.createTime || new Date().toISOString();
          
          // 只有在以下条件都满足时才增加未读计数：
          // 1. 当前用户是消息的接收者（不是发送者）
          // 2. 消息未读（根据数据库isRead字段）
          // 3. 不是当前正在查看的会话，或者当前没有打开任何会话
          if (!isSelfMessage && isMessageReceiver && !message.isRead && 
              (!currentConversation.value || message.conversationId !== currentConversation.value.conversationId)) {
            conversation.unreadCount = (conversation.unreadCount || 0) + 1;
            console.log(`WebSocket收到消息，增加会话 ${message.conversationId} 未读计数:`, conversation.unreadCount);
          } else if (!isSelfMessage && isMessageReceiver && currentConversation.value && 
                    message.conversationId === currentConversation.value.conversationId) {
            // 如果是当前会话中收到的消息（且不是自己发送的），立即标记为已读
            try {
              // 异步标记为已读，不等待结果
              request.post('/chat/messages/read', null, {
                params: {
                  conversationId: message.conversationId,
                  receiverId: currentUserId
                }
              }).then(() => {
                // 标记成功后更新消息状态
                message.isRead = true;
                // 更新消息列表中对应消息的状态
                const msgIndex = messages.value.findIndex(m => m.messageId === message.messageId);
                if (msgIndex !== -1) {
                  messages.value[msgIndex].status = 'read';
                }
              }).catch(error => {
                console.error('通过WebSocket标记当前会话消息为已读失败:', error);
              });
            } catch (error) {
              console.error('通过WebSocket标记当前会话消息为已读失败:', error);
            }
          }
          
          // 确保该会话排在最前面
          if (conversationIndex > 0) {
            // 从列表中移除
            conversationList.value.splice(conversationIndex, 1);
            // 添加到最前面
            conversationList.value.unshift(conversation);
          }
        }
        
        // 如果是当前会话，直接添加到消息列表
        if (currentConversation.value && 
            message.conversationId === currentConversation.value.conversationId) {
          
          // 检查消息是否已存在（防止重复）
          const existingMessage = messages.value.find(m => 
            m.messageId === message.messageId || 
            (m.content === message.content && 
             m.senderId === message.senderId && 
             m.createTime === message.createTime)
          );
          
          if (!existingMessage) {
            // 确保消息有发送者头像和昵称
            if (!message.senderAvatar && message.senderId === userStore.userId) {
              message.senderAvatar = userStore.avatar;
            } else if (!message.senderAvatar && currentConversation.value) {
              message.senderAvatar = currentConversation.value.targetUserAvatar;
            }
            
            if (!message.senderNickname && message.senderId === userStore.userId) {
              message.senderNickname = userStore.nickname || userStore.username;
            } else if (!message.senderNickname && currentConversation.value) {
              message.senderNickname = currentConversation.value.targetUserNickname || currentConversation.value.targetUserName;
            }
            
          // 添加消息，明确设置isSelf标志
            const newMessage = {
            ...message,
            isSelf: isSelfMessage,  // 根据发送者ID判断
            status: msg.isRead ? 'read' : 'unread' // 根据数据库的isRead字段判断消息状态
            };
            
            // 将消息添加到消息数组末尾
            messages.value.push(newMessage);
            
            // 立即触发回调 - 不等待，确保UI立即更新
            triggerMessageCallbacks(newMessage);
            
            // 如果不是自己发送的消息，播放提示音
            if (!isSelfMessage) {
              try {
                const audio = new Audio('/sounds/message.mp3');
                audio.play().catch(() => {});
              } catch (e) {}
            }
          }
        } else if (!isSelfMessage) {
          // 如果不是当前会话，且不是自己发送的，可以显示通知
          showToast(`收到来自 ${conversation.targetUserNickname} 的新消息`);
        }
        
        // 不要重复触发回调，已经在消息处理中触发过
        break;
      
      // 可以添加其他类型的消息处理
      case 'ERROR':
        // 仅在开发环境显示错误
        if (import.meta.env.DEV) {
          console.error('WebSocket错误消息:', data.message);
        }
        break;
        
      case 'CONNECT':
        // 连接成功消息不需要特别处理
        break;
        
      case 'PONG':
        // 服务器心跳响应
        reconnectAttempts.value = 0;
        break;
        
      case 'USER_STATUS_RESPONSE':
        // 处理用户状态响应
        if (data.statusMap) {
          console.log('收到用户状态响应:', data.statusMap);
          // 更新会话中的用户状态
          Object.entries(data.statusMap).forEach(([userId, isOnline]) => {
            // 在会话列表中查找相应用户
            const userIdNum = Number(userId);
            const conversation = conversationList.value.find(
              conv => conv.targetUserId === userIdNum
            );
            
            if (conversation) {
              // 只在状态变化时更新
              if (conversation.isOnline !== isOnline) {
                console.log(`用户 ${userId} 状态变为: ${isOnline ? '在线' : '离线'}`);
                conversation.isOnline = isOnline;
              }
            }
          });
          
          // 触发在线状态回调
          triggerOnlineStatusCallbacks(data.statusMap);
        }
        break;
        
      default:
        // 未知消息类型，仅在开发环境记录
        if (import.meta.env.DEV) {
          console.log('未处理的WebSocket消息类型:', data.type);
        }
    }
  }
  
  // 断开WebSocket连接
  const disconnectWebSocket = () => {
    console.log('正在断开WebSocket连接...')
    
    // 停止心跳
    stopHeartbeat()
    
    // 停止用户状态心跳检查
    stopUserStatusHeartbeat()
    
    // 清理网络监听器
    cleanupNetworkListeners()
    
    // 确保断开WebSocket连接
    if (socket.value) {
      try {
        if (socket.value.readyState === WebSocket.OPEN || 
            socket.value.readyState === WebSocket.CONNECTING) {
          console.log('关闭活跃的WebSocket连接')
          socket.value.close(1000, "User logout")
        } else {
          console.log('WebSocket连接已经处于关闭状态')
        }
      } catch (e) {
        console.error('关闭WebSocket连接失败:', e)
      } finally {
        // 确保引用被清除
        socket.value = null
      }
    } else {
      console.log('WebSocket连接不存在，无需关闭')
    }
    
    // 重置重连尝试次数
    reconnectAttempts.value = 0
    
    // 确保在线状态为空
    if (conversationList.value && conversationList.value.length > 0) {
      console.log('设置所有会话中的用户为离线状态')
      conversationList.value.forEach(conv => {
        if (conv.targetUserId) {
          conv.isOnline = false
        }
      })
    }
    
    console.log('WebSocket连接已完全断开')
  }
  
  // 创建或获取会话
  const createOrGetConversation = async (targetUserId) => {
    if (!targetUserId) {
      console.error('目标用户ID为空，无法创建会话');
      return null;
    }
    
    try {
      console.log('创建或获取与用户的会话:', targetUserId);
      const response = await request.post('/chat/conversation', {
        targetUserId: targetUserId
      });
      
      if (response && response.success && response.data) {
        const conv = response.data;
        console.log('成功创建或获取会话:', conv);
        
        // 确保有targetUserId字段
        if (!conv.targetUserId) {
          if (conv.userAId === userStore.userId) {
            conv.targetUserId = conv.userBId;
          } else {
            conv.targetUserId = conv.userAId;
          }
        }
        
        // 获取目标用户的详细资料信息
        if (conv.targetUserId) {
          try {
            console.log('获取会话目标用户的详细资料:', conv.targetUserId);
            const userProfileResponse = await request.get(`/user/profile/${conv.targetUserId}`);
            
            if (userProfileResponse && userProfileResponse.data) {
              const userProfile = userProfileResponse.data;
              
              // 更新会话中的用户资料信息
              if (userProfile.avatar) {
                console.log('从UserProfile中获取头像:', userProfile.avatar);
                conv.targetUserAvatar = userProfile.avatar;
              }
              
              if (userProfile.nickname) {
                console.log('从UserProfile中获取昵称:', userProfile.nickname);
                conv.targetUserNickname = userProfile.nickname;
                conv.targetUserName = userProfile.nickname;
              }
            } else {
              console.log('未获取到UserProfile，尝试获取User基本信息');
              const userResponse = await request.get(`/user/${conv.targetUserId}`);
              
              if (userResponse && userResponse.data) {
                const user = userResponse.data;
                
                // 更新会话中的用户基本信息
                if (user.avatar) {
                  console.log('从User中获取头像:', user.avatar);
                  conv.targetUserAvatar = user.avatar;
                }
                
                if (user.username) {
                  console.log('从User中获取用户名:', user.username);
                  // 如果没有昵称，使用用户名代替
                  if (!conv.targetUserNickname) {
                    conv.targetUserNickname = user.username;
                    conv.targetUserName = user.username;
                  }
                }
              }
            }
          } catch (profileError) {
            console.error('获取用户资料失败:', profileError);
          }
        }
        
        // 重新获取会话列表，确保包含新会话
        await fetchConversationList();
        
        return conv;
      }
    } catch (error) {
      console.error('创建或获取会话失败:', error);
      showToast('创建会话失败，请重试');
    }
    
    return null;
  }
  
  // 删除会话
  const deleteConversation = async (conversationId) => {
    if (!conversationId) {
      console.error('删除会话失败：会话ID为空');
      return false;
    }
    
    try {
      console.log('请求删除会话:', conversationId);
      const response = await request.delete(`/chat/conversation?conversationId=${conversationId}`);
      
      // 从列表中移除会话
      const index = conversationList.value.findIndex(c => c.conversationId === conversationId);
      if (index >= 0) {
        conversationList.value.splice(index, 1);
      }
      
      // 如果当前会话是被删除的会话，清空当前会话和消息
      if (currentConversation.value && currentConversation.value.conversationId === conversationId) {
        currentConversation.value = null;
        messages.value = [];
      }
      
      console.log('会话删除成功');
      return true;
    } catch (error) {
      console.error('删除会话失败:', error);
      return false;
    }
  }
  
  // 心跳检测，保持WebSocket连接活跃
  const startHeartbeat = () => {
    // 先清除已有的心跳
    stopHeartbeat();
    
    // 设置新的心跳，每30秒发送一次
    heartbeatTimer.value = setInterval(() => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        try {
          // 发送心跳消息
          socket.value.send(JSON.stringify({ type: 'PING' }));
          console.log('发送心跳PING');
        } catch (e) {
          console.error('发送心跳失败:', e);
          // 如果发送失败，尝试重连
          reconnectWebSocket();
        }
      } else if (userStore.isLoggedIn) {
        // 如果连接不是开启状态，但用户已登录，尝试重连
        reconnectWebSocket();
      }
    }, 30000); // 30秒
  };
  
  // 停止心跳
  const stopHeartbeat = () => {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value);
      heartbeatTimer.value = null;
    }
  };
  
  // 重连WebSocket
  const reconnectWebSocket = () => {
    if (reconnectAttempts.value < maxReconnectAttempts) {
      reconnectAttempts.value++;
      console.log(`尝试重连WebSocket (${reconnectAttempts.value}/${maxReconnectAttempts})`);
      connectWebSocket().then(() => {
        // 连接成功，重置重连次数
        reconnectAttempts.value = 0;
      }).catch(err => {
        console.error('WebSocket重连失败:', err);
      });
    } else {
      console.error('WebSocket重连次数过多，停止重连');
      stopHeartbeat();
    }
  };
  
  // 滚动API支持检查
  const hasInstantScrollBehavior = () => {
    try {
      // 尝试创建使用'instant'行为的ScrollIntoViewOptions对象
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
  
  // 请求单个用户在线状态
  const requestUserOnlineStatus = (userId) => {
    if (!userId || !socket.value || socket.value.readyState !== WebSocket.OPEN) {
      return false;
    }
    
    try {
      console.log('请求用户在线状态:', userId);
      socket.value.send(JSON.stringify({
        type: 'GET_USER_STATUS',
        userIds: [userId]
      }));
      return true;
    } catch (e) {
      console.error('请求用户在线状态失败:', e);
      return false;
    }
  }
  
  // 请求多个用户在线状态
  const requestMultipleUserOnlineStatus = (userIds) => {
    if (!userIds || !Array.isArray(userIds) || userIds.length === 0 || 
        !socket.value || socket.value.readyState !== WebSocket.OPEN) {
      return false;
    }
    
    try {
      console.log('请求多个用户在线状态:', userIds);
      socket.value.send(JSON.stringify({
        type: 'GET_USER_STATUS',
        userIds: userIds
      }));
      return true;
    } catch (e) {
      console.error('请求多个用户在线状态失败:', e);
      return false;
    }
  }
  
  // 开始用户在线状态心跳检查
  const startUserStatusHeartbeat = () => {
    console.log('启动用户在线状态心跳检查');
    
    // 先停止已有的定时器
    stopUserStatusHeartbeat();
    
    // 设置新的定时器，定期检查在线状态
    userStatusHeartbeatTimer.value = setInterval(() => {
      // 只在WebSocket连接正常且用户登录时执行
      if (socket.value && socket.value.readyState === WebSocket.OPEN && userStore.isLoggedIn) {
        console.log('执行用户在线状态心跳检查');
        
        // 获取会话中所有用户ID
        const userIds = conversationList.value
          .map(conv => conv.targetUserId)
          .filter(id => id);
        
        if (userIds.length > 0) {
          // 发送批量状态检查请求
          socket.value.send(JSON.stringify({
            type: 'GET_USER_STATUS',
            userIds: userIds
          }));
          console.log('已发送批量用户状态请求:', userIds);
        }
      } else {
        console.log('WebSocket未连接或用户未登录，跳过状态检查');
        
        // 如果用户未登录，将所有用户设为离线
        if (!userStore.isLoggedIn) {
          conversationList.value.forEach(conv => {
            if (conv.targetUserId) {
              conv.isOnline = false;
            }
          });
        }
      }
    }, 10000); // 每10秒检查一次
  };
  
  // 停止用户在线状态心跳检查
  const stopUserStatusHeartbeat = () => {
    if (userStatusHeartbeatTimer.value) {
      clearInterval(userStatusHeartbeatTimer.value);
      userStatusHeartbeatTimer.value = null;
      console.log('已停止用户在线状态心跳检查');
    }
  };
  
  // 返回暴露的状态和方法
  return {
    conversationList,
    currentConversation,
    messages,
    loading,
    unreadCount,
    fetchConversationList,
    fetchMessages,
    sendMessage,
    connectWebSocket,
    disconnectWebSocket,
    createOrGetConversation,
    deleteConversation,
    getConversationDetails,
    reconnectWebSocket,
    startHeartbeat,
    stopHeartbeat,
    requestUserOnlineStatus,
    requestMultipleUserOnlineStatus,
    // 添加WebSocket连接状态检查
    get isWebSocketConnected() {
      // 检查用户登录状态，未登录时始终返回false
      if (!userStore.isLoggedIn) {
        return false;
      }
      return socket.value && socket.value.readyState === WebSocket.OPEN;
    },
    registerMessageCallback,
    unregisterMessageCallback,
    triggerMessageCallbacks,
    registerOnlineStatusCallback,
    unregisterOnlineStatusCallback,
    startUserStatusHeartbeat,
    stopUserStatusHeartbeat
  }
}) 