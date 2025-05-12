import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getRecommendedUsers, likeUser as apiLikeUser, dislikeUser as apiDislikeUser } from '../api/match'
import { showToast, showLoadingToast, closeToast } from 'vant'

export const useMatchStore = defineStore('match', () => {
  // 状态
  const recommendedUsers = ref([])
  const loading = ref(false)
  const filters = ref({
    gender: null,
    location: null,
    interests: null
  })
  const hasMore = ref(true)
  
  // 获取推荐用户
  const fetchRecommendedUsers = async (forceRefresh = false) => {
    if (loading.value) return
    
    if (forceRefresh) {
      recommendedUsers.value = []
      hasMore.value = true
    }
    
    if (!hasMore.value && !forceRefresh) {
      return
    }
    
    loading.value = true
    showLoadingToast({
      message: '加载中...',
      forbidClick: true,
      duration: 0
    })
    
    try {
      // 添加时间戳防止缓存
      const timestamp = new Date().getTime()
      const params = {
        ...filters.value,
        _t: timestamp,
        forceRefresh: forceRefresh,
        limit: 30 // 设置默认获取数量
      }
      
      // 打印请求参数
      console.log('[store] 获取推荐用户请求参数:', params)
      
      const response = await getRecommendedUsers(params)
      console.log('[store] 获取推荐用户响应:', response)
      
      if (response && response.code === 0) {
        if (Array.isArray(response.data)) {
          if (forceRefresh) {
            recommendedUsers.value = response.data
          } else {
            recommendedUsers.value = [...recommendedUsers.value, ...response.data]
          }
          hasMore.value = response.data.length > 0
          console.log('[store] 更新推荐用户列表成功，当前数量:', recommendedUsers.value.length)
        } else {
          console.error('[store] 响应数据格式错误，期望数组但收到:', typeof response.data)
          showToast('数据格式错误，请重试')
        }
      } else if (response && response.code === 200) {
        // 处理另一种可能的成功状态码
        if (Array.isArray(response.data)) {
          if (forceRefresh) {
            recommendedUsers.value = response.data
          } else {
            recommendedUsers.value = [...recommendedUsers.value, ...response.data]
          }
          hasMore.value = response.data.length > 0
          console.log('[store] 更新推荐用户列表成功(code=200)，当前数量:', recommendedUsers.value.length)
        } else {
          console.error('[store] 响应数据格式错误(code=200)，期望数组但收到:', typeof response.data)
          showToast('数据格式错误，请重试')
        }
      } else if (Array.isArray(response)) {
        // 直接处理数组响应
        if (forceRefresh) {
          recommendedUsers.value = response
        } else {
          recommendedUsers.value = [...recommendedUsers.value, ...response]
        }
        hasMore.value = response.length > 0
        console.log('[store] 更新推荐用户列表成功(直接数组)，当前数量:', recommendedUsers.value.length)
      } else {
        console.error('[store] 获取推荐用户失败，响应:', response)
        showToast('获取推荐失败，请重试')
        hasMore.value = false
      }
    } catch (error) {
      console.error('[store] 获取推荐用户出错:', error)
      showToast('获取推荐失败，请重试')
      hasMore.value = false
    } finally {
      loading.value = false
      closeToast()
    }
  }
  
  // 喜欢用户
  const likeUser = async (userId) => {
    try {
      // 在发送请求前保存用户列表状态用于对比
      const beforeIds = recommendedUsers.value.map(u => u.userId);
      console.log(`[store] 喜欢操作前用户列表: [${beforeIds.slice(0, 5).join(',')}${beforeIds.length > 5 ? '...' : ''}]`);
      
      console.log('[store] 发送喜欢用户请求:', userId);
      const response = await apiLikeUser(userId);
      
      // 找到被操作用户在列表中的位置
      const userIndex = recommendedUsers.value.findIndex(user => user.userId === userId);
      console.log('[store] 喜欢的用户索引位置:', userIndex);
      
      if (userIndex !== -1) {
        // 在原位置移除用户
        console.log('[store] 从列表中移除用户:', userId);
        recommendedUsers.value.splice(userIndex, 1);
        
        // 记录更新后的列表
        const afterIds = recommendedUsers.value.map(u => u.userId);
        console.log(`[store] 喜欢操作后用户列表: [${afterIds.slice(0, 5).join(',')}${afterIds.length > 5 ? '...' : ''}]`);
      }
      
      // 当列表剩余少于10个时自动加载更多
      if (recommendedUsers.value.length < 10 && hasMore.value) {
        console.log('[store] 推荐用户小于10个，自动加载更多');
        fetchRecommendedUsers();
      }
      
      // 返回是否匹配成功
      return response.data?.matched === true;
    } catch (error) {
      console.error('[store] 喜欢用户操作失败:', error);
      showToast('操作失败，请稍后重试');
      return false;
    }
  }
  
  // 不喜欢用户
  const dislikeUser = async (userId) => {
    try {
      // 在发送请求前保存用户列表状态用于对比
      const beforeIds = recommendedUsers.value.map(u => u.userId);
      console.log(`[store] 不喜欢操作前用户列表: [${beforeIds.slice(0, 5).join(',')}${beforeIds.length > 5 ? '...' : ''}]`);
      
      console.log('[store] 发送不喜欢用户请求:', userId);
      await apiDislikeUser(userId);
      
      // 找到被操作用户在列表中的位置
      const userIndex = recommendedUsers.value.findIndex(user => user.userId === userId);
      console.log('[store] 不喜欢的用户索引位置:', userIndex);
      
      if (userIndex !== -1) {
        // 在原位置移除用户
        console.log('[store] 从列表中移除用户:', userId);
        recommendedUsers.value.splice(userIndex, 1);
        
        // 记录更新后的列表
        const afterIds = recommendedUsers.value.map(u => u.userId);
        console.log(`[store] 不喜欢操作后用户列表: [${afterIds.slice(0, 5).join(',')}${afterIds.length > 5 ? '...' : ''}]`);
      }
      
      // 当列表剩余少于10个时自动加载更多
      if (recommendedUsers.value.length < 10 && hasMore.value) {
        console.log('[store] 推荐用户小于10个，自动加载更多');
        fetchRecommendedUsers();
      }
      
      return true;
    } catch (error) {
      console.error('[store] 不喜欢用户操作失败:', error);
      showToast('操作失败，请稍后重试');
      return false;
    }
  }
  
  // 更新筛选条件
  const updateFilters = (newFilters) => {
    console.log('更新筛选条件:', newFilters)
    filters.value = { ...filters.value, ...newFilters }
    // 重置列表状态
    recommendedUsers.value = []
    hasMore.value = true
  }
  
  // 重置筛选条件
  const resetFilters = () => {
    filters.value = {
      gender: null,
      location: null,
      interests: null
    }
    // 重置列表状态
    recommendedUsers.value = []
    hasMore.value = true
  }
  
  return {
    recommendedUsers,
    loading,
    filters,
    hasMore,
    fetchRecommendedUsers,
    likeUser,
    dislikeUser,
    updateFilters,
    resetFilters
  }
}) 