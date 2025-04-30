<template>
  <div class="dashboard-container">
    <a-row :gutter="24">
      <a-col :span="24">
        <a-card title="系统概览" class="mb-20">
          <a-descriptions>
            <a-descriptions-item label="系统版本">{{ systemInfo.version }}</a-descriptions-item>
            <a-descriptions-item label="运行时间">{{ systemInfo.startTime }} ({{ systemInfo.runningDays }}天)</a-descriptions-item>
            <a-descriptions-item label="CPU使用率">{{ systemInfo.cpuUsage }}%</a-descriptions-item>
            <a-descriptions-item label="内存使用率">{{ systemInfo.memoryUsage }}%</a-descriptions-item>
            <a-descriptions-item label="磁盘使用率">{{ systemInfo.diskUsage }}%</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24">
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>用户总数</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ userStats.totalUsers }}</div>
            <div class="stat-description">社区总用户数量</div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>今日新增用户</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ userStats.newUsersToday }}</div>
            <div class="stat-description">今日新注册用户数量</div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>活跃用户</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ userStats.activeUsers }}</div>
            <div class="stat-description">近7天有登录行为的用户</div>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24">
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>帖子总数</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ contentStats.totalPosts }}</div>
            <div class="stat-description">社区总帖子数量</div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>评论总数</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ contentStats.totalComments }}</div>
            <div class="stat-description">社区总评论数量</div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="mb-20">
          <template #title>
            <span>今日发帖</span>
          </template>
          <div class="card-content">
            <div class="stat-number">{{ contentStats.postsToday }}</div>
            <div class="stat-description">今日新发布的帖子数量</div>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24">
      <a-col :span="12">
        <a-card title="用户性别分布" class="mb-20">
          <div class="chart-container">
            <a-pie-chart
              :data="genderData"
              :height="300"
              :tooltip="{ formatter: '{b}: {c} ({d}%)' }"
            />
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="用户增长趋势" class="mb-20">
          <div class="chart-filter mb-10">
            <a-radio-group v-model:value="userGrowthTimeRange" @change="fetchUserGrowthData">
              <a-radio-button value="week">近7天</a-radio-button>
              <a-radio-button value="month">近30天</a-radio-button>
            </a-radio-group>
          </div>
          <div class="chart-container">
            <a-line-chart
              :data="userGrowthData"
              :x-field="'date'"
              :y-field="'value'"
              :height="260"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24">
      <a-col :span="24">
        <a-card title="内容发布趋势" class="mb-20">
          <div class="chart-filter mb-10">
            <a-radio-group v-model:value="contentTrendTimeRange" @change="fetchContentTrendData">
              <a-radio-button value="week">近7天</a-radio-button>
              <a-radio-button value="month">近30天</a-radio-button>
            </a-radio-group>
          </div>
          <div class="chart-container">
            <a-dual-line-chart
              :data="contentTrendData"
              :x-field="'date'"
              :y-fields="['posts', 'comments']"
              :series-names="['帖子', '评论']"
              :height="300"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getUserStats, getContentStats, getSystemSummary, getUserGenderDistribution, getUserGrowthTrend, getContentPublishTrend } from '@/api/dashboard';
import APieChart from '@/components/charts/APieChart.vue';
import ALineChart from '@/components/charts/ALineChart.vue';
import ADualLineChart from '@/components/charts/ADualLineChart.vue';

// 状态数据
const userStats = reactive({
  totalUsers: 0,
  newUsersToday: 0,
  newUsersYesterday: 0,
  newUsersWeek: 0,
  newUsersMonth: 0,
  activeUsers: 0,
  userGrowthRate: 0
});

const contentStats = reactive({
  totalPosts: 0,
  totalComments: 0,
  postsToday: 0,
  commentsToday: 0,
  avgPostsPerDay: 0,
  avgCommentsPerPost: 0
});

const systemInfo = reactive({
  version: '',
  startTime: '',
  runningDays: 0,
  cpuUsage: 0,
  memoryUsage: 0,
  diskUsage: 0
});

// 图表数据
const genderData = ref([]);
const userGrowthData = ref([]);
const contentTrendData = ref([]);

// 时间范围选择
const userGrowthTimeRange = ref('week');
const contentTrendTimeRange = ref('week');

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const data = await getUserStats();
    if (data) {
      Object.assign(userStats, data);
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error);
  }
};

// 获取内容统计数据
const fetchContentStats = async () => {
  try {
    const data = await getContentStats();
    if (data) {
      Object.assign(contentStats, data);
    }
  } catch (error) {
    console.error('获取内容统计数据失败:', error);
  }
};

// 获取系统概要信息
const fetchSystemSummary = async () => {
  try {
    const data = await getSystemSummary();
    if (data) {
      Object.assign(systemInfo, data);
    }
  } catch (error) {
    console.error('获取系统概要信息失败:', error);
  }
};

// 获取用户性别分布数据
const fetchGenderDistribution = async () => {
  try {
    const data = await getUserGenderDistribution();
    if (data && data.data) {
      genderData.value = data.data;
    }
  } catch (error) {
    console.error('获取用户性别分布数据失败:', error);
  }
};

// 获取用户增长趋势数据
const fetchUserGrowthData = async () => {
  try {
    const data = await getUserGrowthTrend(userGrowthTimeRange.value);
    if (data) {
      const { dates, values } = data;
      userGrowthData.value = dates.map((date, index) => ({
        date,
        value: values[index]
      }));
    }
  } catch (error) {
    console.error('获取用户增长趋势数据失败:', error);
  }
};

// 获取内容发布趋势数据
const fetchContentTrendData = async () => {
  try {
    const data = await getContentPublishTrend(contentTrendTimeRange.value);
    if (data) {
      const { dates, postValues, commentValues } = data;
      contentTrendData.value = dates.map((date, index) => ({
        date,
        posts: postValues[index],
        comments: commentValues[index]
      }));
    }
  } catch (error) {
    console.error('获取内容发布趋势数据失败:', error);
  }
};

// 组件挂载时获取所有数据
onMounted(() => {
  fetchUserStats();
  fetchContentStats();
  fetchSystemSummary();
  fetchGenderDistribution();
  fetchUserGrowthData();
  fetchContentTrendData();
});
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .mb-10 {
    margin-bottom: 10px;
  }
  
  .mb-20 {
    margin-bottom: 20px;
  }
  
  .card-content {
    text-align: center;
    padding: 10px 0;
    
    .stat-number {
      font-size: 36px;
      font-weight: bold;
      color: #1890ff;
    }
    
    .stat-description {
      font-size: 14px;
      color: #8c8c8c;
      margin-top: 5px;
    }
  }
  
  .chart-container {
    width: 100%;
    display: flex;
    justify-content: center;
  }
  
  .chart-filter {
    display: flex;
    justify-content: flex-end;
  }
}
</style> 