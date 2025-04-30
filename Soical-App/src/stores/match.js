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
      // 构建筛选参数，清除null和undefined值
      const params = {
        limit: 30 // 设置为30个推荐
      }
      
      // 性别筛选 - 需要确保值是正确的数字类型
      if (filters.value.gender !== null && filters.value.gender !== undefined) {
        // 确保性别是数字类型(1-男，2-女)
        const genderValue = Number(filters.value.gender);
        if (!isNaN(genderValue)) {
          params.gender = genderValue;
          console.log('[store] 应用性别筛选:', genderValue);
        }
      }
      
      // 位置筛选 - 确保不是空字符串
      if (filters.value.location && typeof filters.value.location === 'string' && filters.value.location.trim() !== '') {
        params.location = filters.value.location.trim();
        console.log('[store] 应用位置筛选:', params.location);
      }
      
      // 兴趣标签筛选 - 确保不是空字符串
      if (filters.value.interests && typeof filters.value.interests === 'string' && filters.value.interests.trim() !== '') {
        params.interests = filters.value.interests.trim();
        console.log('[store] 应用兴趣标签筛选:', params.interests);
      }
      
      console.log('[store] 请求推荐用户参数:', JSON.stringify(params));
      
      const response = await getRecommendedUsers(params);
      console.log('[store] 推荐API响应状态:', response.code);
      
      // 处理响应数据
      if (response && response.data) {
        const users = response.data;
        
        if (Array.isArray(users)) {
          // 记录原始列表
          const originalList = [...recommendedUsers.value];
          console.log(`[store] 现有列表数量: ${originalList.length}, 新接收数量: ${users.length}`);
          
          if (users.length === 0) {
            hasMore.value = false;
            if (recommendedUsers.value.length === 0) {
              showToast('没有符合条件的用户，请尝试调整筛选条件');
            } else {
              showToast('暂无更多推荐');
            }
          } else {
            // 过滤掉重复用户
            const existingUserIds = new Set(recommendedUsers.value.map(u => u.userId));
            const newUsers = users.filter(newUser => !existingUserIds.has(newUser.userId));
            
            if (newUsers.length > 0) {
              // 保留原有用户并添加新用户（不同于之前完全替换的方式）
              recommendedUsers.value = [...originalList, ...newUsers];
              console.log(`[store] 添加${newUsers.length}个新推荐用户，总计${recommendedUsers.value.length}个`);
            } else {
              hasMore.value = false;
              // 如果没有新用户但有现有用户，不显示提示
              if (recommendedUsers.value.length === 0) {
                showToast('暂无推荐用户');
              }
            }
          }
        } else {
          console.error('[store] 获取推荐用户返回格式错误:', users);
          hasMore.value = false;
        }
      } else {
        console.error('[store] API响应格式错误:', response);
        hasMore.value = false;
        showToast('获取推荐失败，请稍后重试');
      }
    } catch (error) {
      console.error('[store] 获取推荐用户失败:', error);
      hasMore.value = false;
      showToast('获取推荐失败，请稍后重试');
    } finally {
      loading.value = false;
      closeToast();
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