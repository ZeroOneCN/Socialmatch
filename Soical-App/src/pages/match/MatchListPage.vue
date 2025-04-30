<template>
  <div class="match-list-page">
    <van-nav-bar
      title="我的同频匹配"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="content-container">
      <!-- 匹配统计信息 -->
      <div class="match-stats">
        <div class="match-stat-item">
          <div class="stat-number">{{ matchStats.total }}</div>
          <div class="stat-label">总匹配数</div>
        </div>
        <div class="match-stat-item">
          <div class="stat-number">{{ matchStats.newMatches }}</div>
          <div class="stat-label">新匹配</div>
        </div>
        <div class="match-stat-item">
          <div class="stat-number">{{ matchStats.conversations }}</div>
          <div class="stat-label">已聊天</div>
        </div>
        <div class="match-stat-item">
          <div class="stat-number">{{ matchStats.rate }}%</div>
          <div class="stat-label">匹配率</div>
        </div>
        <div class="match-stat-item">
          <div class="stat-number">{{ matchStats.potentialMatches }}</div>
          <div class="stat-label">潜在匹配</div>
        </div>
      </div>
      
      <!-- 匹配列表 -->
      <div class="match-list">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-empty 
            v-if="matchList.length === 0 && !loading" 
            description="暂无匹配，调整偏好设置获取更多匹配"
          >
            <template #image>
              <img src="../assets/empty-match.png" alt="暂无匹配" class="empty-image" onerror="this.onerror=null;this.src='https://img01.yzcdn.cn/vant/empty-image-default.png';" />
            </template>
            <van-button round type="primary" size="small" @click="goToFrequencySettings">
              调整偏好设置
            </van-button>
          </van-empty>
          
          <van-list
            v-else
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div v-for="match in matchList" :key="match.matchId || match.userId" class="match-item">
              <div class="match-user" @click="viewUserProfile(match.userId)">
                <van-image
                  round
                  fit="cover"
                  width="60"
                  height="60"
                  :src="match.avatar || 'https://img01.yzcdn.cn/vant/cat.jpeg'"
                  class="user-avatar"
                />
                <div class="user-info">
                  <div class="nickname">{{ match.nickname || '用户' }}</div>
                  <div class="user-detail">
                    <span v-if="match.age">{{ match.age }}岁</span>
                    <span v-if="match.location">{{ match.location }}</span>
                    <span v-if="match.occupation">{{ match.occupation }}</span>
                  </div>
                </div>
                <div class="match-rate">
                  <div class="rate-number">{{ match.similarity || match.matchRate || 0 }}%</div>
                  <div class="rate-label">匹配度</div>
                </div>
              </div>
              
              <div class="match-actions">
                <van-button round type="primary" size="small" @click="startChat(match.userId)">
                  <van-icon name="chat-o" />开始聊天
                </van-button>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import * as userApi from '../../api/user'

const router = useRouter()
const matchList = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 匹配统计信息
const matchStats = ref({
  total: 0,
  newMatches: 0,
  conversations: 0,
  rate: 0,
  potentialMatches: 0
})

// 获取匹配统计数据
const fetchMatchStats = async () => {
  try {
    const response = await userApi.getFrequencyStats()
    if (response.data) {
      matchStats.value.total = response.data.matches || 0
      matchStats.value.newMatches = response.data.newMatches || 0
      matchStats.value.conversations = response.data.conversations || 0
      matchStats.value.rate = response.data.rate || 0
      matchStats.value.potentialMatches = response.data.potentialMatches || 0
    }
  } catch (error) {
    console.error('获取匹配统计数据失败', error)
    showToast('获取匹配数据失败，请稍后重试')
  }
}

// 加载匹配列表
const fetchMatchList = async () => {
  try {
    const response = await userApi.getFrequencyMatches({
      page: page.value,
      pageSize: pageSize.value
    })
    
    console.log('匹配列表响应:', response)
    
    if (!response || !response.data) {
      console.error('匹配列表响应格式异常', response)
      loading.value = false
      refreshing.value = false
      return
    }
    
    // 兼容两种可能的响应格式
    let list = []
    if (Array.isArray(response.data)) {
      // 直接是数组的情况
      list = response.data
    } else if (response.data.records && Array.isArray(response.data.records)) {
      // 包含records字段的情况
      list = response.data.records || []
    } else {
      // 其他情况，尝试解析
      list = response.data || []
    }
    
    console.log('处理后的匹配列表数据:', list)
    
    if (refreshing.value) {
      matchList.value = list
    } else {
      matchList.value = [...matchList.value, ...list]
    }
    
    // 更新页码和加载状态
    page.value++
    loading.value = false
    refreshing.value = false
    
    // 判断是否加载完毕
    if (list.length < pageSize.value) {
      finished.value = true
    }
    
    // 如果请求成功但列表为空，再次检查统计数据
    if (matchList.value.length === 0) {
      console.log('匹配列表为空，但统计数据显示有匹配，重新获取统计数据')
      fetchMatchStats()
    }
  } catch (error) {
    console.error('获取匹配列表失败', error)
    loading.value = false
    refreshing.value = false
    
    showToast('获取匹配列表失败，请稍后重试')
  }
}

// 下拉刷新
const onRefresh = () => {
  finished.value = false
  page.value = 1
  refreshing.value = true
  fetchMatchList()
  fetchMatchStats()
}

// 加载更多
const onLoad = () => {
  if (!refreshing.value) {
    fetchMatchList()
  }
}

// 查看用户资料
const viewUserProfile = (userId) => {
  router.push(`/user/${userId}`)
}

// 开始聊天
const startChat = (userId) => {
  router.push(`/chat/${userId}`)
}

// 前往偏好设置
const goToFrequencySettings = () => {
  router.push('/settings/frequency')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  console.log('匹配列表页面加载')
  // 获取匹配统计
  fetchMatchStats()
  // 获取匹配列表
  if (!loading.value && !refreshing.value) {
    fetchMatchList()
  }
})
</script>

<style scoped>
.match-list-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
}

.content-container {
  padding: 16px;
}

.match-stats {
  display: flex;
  justify-content: space-around;
  padding: 15px 0;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  color: #323233;
}

.match-stat-item {
  text-align: center;
  flex: 1;
}

.stat-number {
  font-size: 18px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #323233;
  white-space: nowrap;
}

.match-list {
  margin-top: 16px;
}

.match-item {
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  color: #323233;
}

.match-user {
  flex: 1;
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 16px;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.nickname {
  font-size: 16px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 4px;
}

.user-detail {
  font-size: 12px;
  color: #323233;
  margin-bottom: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.match-rate {
  text-align: right;
  margin-left: 10px;
}

.rate-number {
  font-size: 18px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.rate-label {
  font-size: 12px;
  color: #323233;
  white-space: nowrap;
}

.match-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.empty-image {
  width: 120px;
  height: 120px;
}

/* 使用深度选择器确保Vant组件内的文本可见 */
:deep(.van-nav-bar__title) {
  color: #323233 !important;
}

:deep(.van-button__text) {
  color: inherit !important;
}

:deep(.van-empty__description) {
  color: #323233 !important;
}

:deep(.van-list__finished-text) {
  color: #323233 !important;
}

:deep(.van-pull-refresh) {
  color: #323233 !important;
}

:deep(.van-cell) {
  color: #323233 !important;
}
</style>