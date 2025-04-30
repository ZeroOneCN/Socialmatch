import { defineStore } from 'pinia'
import { login, register } from '../api/auth'
import { getUserInfo } from '../api/user'
import { setToken, removeToken, getToken } from '../utils/auth'
import { useMessageStore } from './message'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userId: null,
    username: '',
    nickname: '',
    avatar: '',
    userInfo: null,
    loading: false,
    // 认证状态
    verifications: {
      identityStatus: 'not_submitted',
      educationStatus: 'not_submitted'
    }
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    // 获取当前用户信息
    async fetchUserInfo(forceRefresh = false) {
      if (!this.token) return;
      
      // 如果正在加载且不是强制刷新，则返回
      if (this.loading && !forceRefresh) return;
      
      this.loading = true;
      
      try {
        console.log('从服务器获取最新用户信息...');
        const timestamp = Date.now(); // 添加时间戳避免缓存
        const response = await getUserInfo(null, { _t: timestamp, forceRefresh: true });
        
        if (response && response.data) {
          const userData = response.data;
          console.log('用户信息获取成功:', userData);
          
          // 确保清除旧数据，使用新数据
          this.userId = userData.userId ? Number(userData.userId) : null;
          this.username = userData.username || '';
          
          // 确保从profile中获取nickname
          if (userData.profile && userData.profile.nickname) {
            this.nickname = userData.profile.nickname;
            console.log('从profile中更新昵称:', this.nickname);
          } else {
            this.nickname = userData.nickname || userData.username || '';
            console.log('从user中更新昵称:', this.nickname);
          }
          
          this.avatar = userData.profile?.avatar || userData.avatar || '';
          
          // 获取认证状态
          if (userData.verifications) {
            this.verifications.identityStatus = userData.verifications.identityStatus || 'not_submitted';
            this.verifications.educationStatus = userData.verifications.educationStatus || 'not_submitted';
            console.log('更新认证状态:', this.verifications);
          } else {
            // 尝试从后端获取认证状态
            this.fetchVerificationStatus();
          }
          
          // 深拷贝用户数据以防引用问题
          this.userInfo = JSON.parse(JSON.stringify(userData));
          
          if (userData.profile) {
            console.log('从profile中获取到用户信息：', 
                        '昵称:', userData.profile.nickname, 
                        '头像:', userData.profile.avatar);
          }
          
          console.log('用户信息更新完成：', 
                    '昵称:', this.nickname, 
                    '头像:', this.avatar);
        } else {
          console.error('获取用户信息失败, 响应结构不正确:', response);
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        // 如果获取失败且是认证错误，清除token
        if (error.response && error.response.status === 401) {
          this.logout();
        }
      } finally {
        this.loading = false;
      }
    },

    // 为了兼容保留getUserInfo方法，实际调用fetchUserInfo
    async getUserInfo() {
      return this.fetchUserInfo();
    },

    // 登录
    async login(loginData) {
      try {
        const response = await login(loginData)
        
        if (response && response.data && response.data.token) {
          // 保存token
          this.setToken(response.data.token)
          
          // 获取用户信息
          await this.fetchUserInfo()
          
          // 同步用户资料（确保头像和昵称在两个表中保持一致）
          try {
            await this.syncUserProfile();
            console.log('用户资料同步完成');
          } catch (syncError) {
            console.error('用户资料同步失败:', syncError);
            // 同步失败不影响登录流程
          }
          
          // 成功登录后立即连接WebSocket和开始在线状态心跳
          try {
            const messageStore = useMessageStore()
            if (messageStore) {
              console.log('登录成功，连接WebSocket并启动在线状态心跳')
              // 先连接WebSocket
              await messageStore.connectWebSocket()
              // 启动在线状态心跳
              messageStore.startUserStatusHeartbeat()
            }
          } catch (error) {
            console.error('启动WebSocket和在线状态心跳失败:', error)
          }
          
          return Promise.resolve(response.data)
        } else {
          return Promise.reject(new Error('登录失败，请检查用户名和密码'))
        }
      } catch (error) {
        console.error('登录失败:', error)
        return Promise.reject(error)
      }
    },

    // 同步用户资料
    async syncUserProfile() {
      try {
        const { updateUserBasicInfo } = await import('../api/user');
        if (this.userInfo) {
          // 构建只包含需要同步的字段的数据对象
          const userBaseData = {
            avatar: this.avatar, 
            nickname: this.nickname
          };
          
          // 过滤掉undefined的属性
          const filteredData = Object.fromEntries(
            Object.entries(userBaseData).filter(([_, v]) => v !== undefined)
          );
          
          // 只有在有需要同步的数据时才调用API
          if (Object.keys(filteredData).length > 0) {
            console.log('同步基本信息到User表:', filteredData);
            const syncResponse = await updateUserBasicInfo(filteredData);
            console.log('同步基本信息结果:', syncResponse);
          }
        }
      } catch (error) {
        console.error('同步用户资料失败:', error);
      }
    },

    // 注册
    async register(userData) {
      try {
        console.log('注册开始, 用户数据:', userData)
        const response = await register(userData)
        console.log('注册响应:', response)
        return Promise.resolve(response)
      } catch (error) {
        console.error('注册失败:', error)
        return Promise.reject(error)
      }
    },

    // 登出
    logout() {
      console.log('开始执行用户登出流程...')
      
      // 1. 先关闭WebSocket连接（避免登出后依然保持连接）
      try {
        // 立即断开WebSocket连接
        const messageStore = useMessageStore()
        if (messageStore && typeof messageStore.disconnectWebSocket === 'function') {
          console.log('登出时立即关闭WebSocket连接')
          messageStore.disconnectWebSocket()
        }
      } catch (error) {
        console.error('登出时关闭WebSocket连接失败', error)
      }

      // 2. 清除用户数据
      console.log('清除用户数据和Token...')
      this.token = ''
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.avatar = ''
      this.userInfo = null
      removeToken()

      // 3. 清除本地存储
      localStorage.removeItem('userToken')
      localStorage.removeItem('userInfo')
      
      console.log('用户登出流程完成')
    },

    // 设置用户数据
    setUserData({ token, userId, username, nickname, avatar }) {
      console.log('设置用户数据:', { token, userId, username, nickname, avatar })
      this.token = token || ''
      // 确保userId是数字类型或null
      this.userId = userId ? Number(userId) : null
      this.username = username || ''
      this.nickname = nickname || ''
      this.avatar = avatar || ''
    },
    
    // 更新用户昵称
    setNickname(nickname) {
      console.log('设置用户昵称:', nickname);
      
      // 确保有昵称值
      if (!nickname) {
        console.warn('尝试设置空昵称');
        return;
      }
      
      // 设置状态的昵称
      this.nickname = nickname;
      
      // 更新userInfo中的昵称
      if (this.userInfo) {
        this.userInfo.nickname = nickname;
        
        // 确保profile中的昵称也被更新
        if (this.userInfo.profile) {
          this.userInfo.profile.nickname = nickname;
          console.log('已更新profile中的nickname');
        }
      }
      
      console.log('昵称已更新。当前nickname:', this.nickname);
    },
    
    // 更新用户头像
    setAvatar(avatar) {
      this.avatar = avatar || '';
      if (this.userInfo) {
        this.userInfo.avatar = avatar || '';
      }
    },
    
    // 更新用户资料
    async updateProfile(profileData) {
      try {
        console.log('用户资料更新请求数据:', profileData);
        const { updateUserProfile, updateUserBasicInfo } = await import('../api/user');
        const response = await updateUserProfile(profileData);
        if (response && response.data) {
          // 更新昵称
          if (profileData.nickname) {
            this.setNickname(profileData.nickname);
          }
          
          // 更新头像
          if (profileData.avatar) {
            this.setAvatar(profileData.avatar);
          }
          
          // 更新用户信息对象
          if (this.userInfo) {
            this.userInfo = { ...this.userInfo, ...profileData };
          }
          
          // 更新主用户表的数据 - 确保头像和昵称同步到user表
          try {
            // 构建只包含需要同步的字段的数据对象
            const userBaseData = {
              avatar: profileData.avatar, // 如果有上传头像，同步到用户表
              nickname: profileData.nickname // 同步昵称到用户表
            };
            
            // 过滤掉undefined的属性
            const filteredData = Object.fromEntries(
              Object.entries(userBaseData).filter(([_, v]) => v !== undefined)
            );
            
            // 只有在有需要同步的数据时才调用API
            if (Object.keys(filteredData).length > 0) {
              console.log('同步基本信息到User表:', filteredData);
              const syncResponse = await updateUserBasicInfo(filteredData);
              console.log('同步基本信息结果:', syncResponse);
            }
          } catch (syncError) {
            console.error('同步基本数据失败:', syncError);
            // 此处失败不影响主流程，只记录日志
          }
        }
        return Promise.resolve(response);
      } catch (error) {
        console.error('更新用户资料失败:', error);
        return Promise.reject(error);
      }
    },
    
    // 检查并恢复登录状态
    async restoreLoginState() {
      const token = getToken();
      if (token && !this.userId) {
        console.log('检测到TOKEN，恢复登录状态...');
        this.token = token;
        await this.fetchUserInfo();
      }
    },

    // 设置token
    setToken(token, expireIn) {
      if (!token) return;
      this.token = token;
      setToken(token, expireIn);
    },

    // 清除缓存的个人资料数据
    clearProfileCache() {
      console.log('清除缓存的个人资料数据');
      // 清除nickname缓存，但保留username等基本信息
      this.nickname = ''; // 清空昵称，强制从服务器重新获取
      if (this.userInfo && this.userInfo.profile) {
        this.userInfo.profile.nickname = '';
      }
    },

    // 获取认证状态
    async fetchVerificationStatus() {
      try {
        if (!this.userId) return;
        
        const { getUserVerificationStatus } = await import('../api/verification');
        const { data } = await getUserVerificationStatus(this.userId);
        
        if (data) {
          this.verifications.identityStatus = data.identityStatus || 'not_submitted';
          this.verifications.educationStatus = data.educationStatus || 'not_submitted';
          
          // 如果userInfo存在，同时更新其中的认证状态
          if (this.userInfo) {
            if (!this.userInfo.verifications) {
              this.userInfo.verifications = {};
            }
            this.userInfo.verifications.identityStatus = this.verifications.identityStatus;
            this.userInfo.verifications.educationStatus = this.verifications.educationStatus;
          }
          
          console.log('认证状态已更新:', this.verifications);
        }
      } catch (error) {
        console.error('获取认证状态失败:', error);
      }
    }
  }
}) 