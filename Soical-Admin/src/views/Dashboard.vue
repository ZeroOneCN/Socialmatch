<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1 class="page-title">控制台</h1>
      <div class="header-actions">
        <el-button size="small" type="primary" plain @click="fetchAllData">
          <el-icon><Refresh /></el-icon>
          <span>刷新数据</span>
        </el-button>
      </div>
    </div>
    
    <!-- 数据概览 -->
    <el-row :gutter="24" class="data-overview">
      <el-col :xs="24" :sm="12" :md="8" :lg="4.8" v-for="(card, index) in dataSummary" :key="index">
        <el-card shadow="hover" class="data-card">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
              <div class="card-trend" v-if="card.trend !== undefined">
                <span :class="['trend-value', card.trend > 0 ? 'up' : (card.trend < 0 ? 'down' : 'neutral')]">
                  <el-icon v-if="card.trend > 0"><ArrowUp /></el-icon>
                  <el-icon v-else-if="card.trend < 0"><ArrowDown /></el-icon>
                  {{ Math.abs(card.trend) }}%
                </span>
                <span class="trend-label">较上周</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <div class="dashboard-main">
      <el-row :gutter="24" class="chart-container">
        <!-- 用户增长趋势图 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>用户增长趋势</h3>
                <el-radio-group v-model="userGrowthTimeRange" size="small" @change="fetchUserGrowthData">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="userGrowthChart" class="chart">
              <el-skeleton v-if="loading" :rows="6" animated />
            </div>
          </el-card>
        </el-col>
        
        <!-- 匹配趋势图 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>匹配成功趋势</h3>
                <el-radio-group v-model="matchTrendTimeRange" size="small" @change="fetchMatchTrendData">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="matchTrendChart" class="chart">
              <el-skeleton v-if="loading" :rows="6" animated />
            </div>
          </el-card>
        </el-col>
        
        <!-- 内容发布趋势图 -->
        <el-col :xs="24" :lg="16">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>内容发布趋势</h3>
                <el-radio-group v-model="contentPublishTimeRange" size="small" @change="fetchContentPublishData">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="contentPublishChart" class="chart">
              <el-skeleton v-if="loading" :rows="6" animated />
            </div>
          </el-card>
        </el-col>
        
        <!-- 用户性别分布 -->
        <el-col :xs="24" :lg="8">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>用户性别分布</h3>
              </div>
            </template>
            <div ref="genderDistributionChart" class="chart chart-pie">
              <el-skeleton v-if="loading" :rows="6" animated />
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 系统状态 -->
      <el-row :gutter="24">
        <el-col :span="24">
          <el-card shadow="hover" class="system-status-card">
            <template #header>
              <div class="chart-header">
                <h3>系统状态</h3>
                <el-tag type="success" v-if="systemStatus.status === 'online'" effect="dark">运行中</el-tag>
                <el-tag type="warning" v-else-if="systemStatus.status === 'maintenance'" effect="dark">维护中</el-tag>
                <el-tag type="danger" v-else effect="dark">离线</el-tag>
              </div>
            </template>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="系统版本">{{ systemStatus.version || 'v1.0.0' }}</el-descriptions-item>
                  <el-descriptions-item label="运行时间">{{ systemStatus.uptime || '10天12小时' }}</el-descriptions-item>
                  <el-descriptions-item label="服务器IP">{{ systemStatus.serverIp || '192.168.1.1' }}</el-descriptions-item>
                </el-descriptions>
              </el-col>
              <el-col :span="12">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="数据库状态">
                    <el-tag type="success" v-if="systemStatus.dbStatus === 'healthy'" effect="plain">正常</el-tag>
                    <el-tag type="warning" v-else effect="plain">异常</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="CPU使用率">
                    <el-progress :percentage="parseFloat(systemStatus.cpuUsage)" :color="getCpuColor(systemStatus.cpuUsage)" />
                  </el-descriptions-item>
                  <el-descriptions-item label="内存使用率">
                    <el-progress :percentage="parseFloat(systemStatus.memoryUsage)" :color="getMemoryColor(systemStatus.memoryUsage)" />
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '../stores/auth';
import { 
  User, 
  DataAnalysis, 
  Comment, 
  ChatLineRound, 
  Star, 
  Connection, 
  Refresh,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue';
import * as echarts from 'echarts/core';
import { 
  BarChart, 
  LineChart, 
  PieChart 
} from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import * as dashboardApi from '../api/dashboard';

// 注册必需的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  LineChart,
  PieChart,
  CanvasRenderer
]);

const authStore = useAuthStore();

// 图表引用
const userGrowthChart = ref(null);
const contentPublishChart = ref(null);
const genderDistributionChart = ref(null);
const matchTrendChart = ref(null);

// 图表实例
let userGrowthChartInstance = null;
let contentPublishChartInstance = null;
let genderDistributionChartInstance = null;
let matchTrendChartInstance = null;

// 时间范围选择
const userGrowthTimeRange = ref('week');
const contentPublishTimeRange = ref('week');
const matchTrendTimeRange = ref('week');

// 系统状态
const systemStatus = reactive({
  status: 'online',
  version: 'v1.0.0',
  uptime: '10天12小时',
  serverIp: '192.168.1.1',
  dbStatus: 'healthy',
  cpuUsage: '32',
  memoryUsage: '45'
});

// 用户名
const userName = computed(() => authStore.user?.username || '管理员');

// 加载状态
const loading = ref(true);

// 获取CPU颜色
const getCpuColor = (value) => {
  const percentage = parseFloat(value);
  if (percentage < 50) return '#67C23A';
  if (percentage < 80) return '#E6A23C';
  return '#F56C6C';
};

// 获取内存颜色
const getMemoryColor = (value) => {
  const percentage = parseFloat(value);
  if (percentage < 50) return '#67C23A';
  if (percentage < 80) return '#E6A23C';
  return '#F56C6C';
};

// 数据概览
const dataSummary = reactive([
  { 
    label: '用户总数', 
    value: '0', 
    icon: User, 
    color: '#409EFF',
    trend: 0
  },
  { 
    label: '动态总数', 
    value: '0', 
    icon: DataAnalysis, 
    color: '#67C23A',
    trend: 0
  },
  { 
    label: '评论总数', 
    value: '0', 
    icon: Comment, 
    color: '#E6A23C',
    trend: 0
  },
  { 
    label: '活跃用户', 
    value: '0', 
    icon: ChatLineRound, 
    color: '#F56C6C',
    trend: 0
  },
  { 
    label: '匹配成功率', 
    value: '0%', 
    icon: Connection, 
    color: '#9254de',
    trend: 0
  }
]);

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 用户增长趋势图
    if (userGrowthChart.value) {
      userGrowthChartInstance = echarts.init(userGrowthChart.value);
    }
    
    // 内容发布趋势图
    if (contentPublishChart.value) {
      contentPublishChartInstance = echarts.init(contentPublishChart.value);
    }
    
    // 用户性别分布图
    if (genderDistributionChart.value) {
      genderDistributionChartInstance = echarts.init(genderDistributionChart.value);
    }
    
    // 匹配趋势图
    if (matchTrendChart.value) {
      matchTrendChartInstance = echarts.init(matchTrendChart.value);
    }
    
    // 获取图表数据
    fetchAllData();
  });
};

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  userGrowthChartInstance?.resize();
  contentPublishChartInstance?.resize();
  genderDistributionChartInstance?.resize();
  matchTrendChartInstance?.resize();
};

// 获取所有数据
const fetchAllData = async () => {
  loading.value = true;
  try {
    await Promise.all([
      fetchUserStats(),
      fetchUserGrowthData(),
      fetchContentPublishData(),
      fetchUserGenderDistribution(),
      fetchSystemSummary(),
      fetchMatchTrendData()
    ]);
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const data = await dashboardApi.getUserStats();
    if (data) {
      dataSummary[0].value = data.totalUsers || 0;
      dataSummary[0].trend = data.userGrowthRate || 0;
      dataSummary[3].value = data.activeUsers || 0;
      
      // 获取内容统计数据
      const contentData = await dashboardApi.getContentStats();
      if (contentData) {
        dataSummary[1].value = contentData.totalPosts || 0;
        dataSummary[1].trend = contentData.postsGrowthRate || 0;
        dataSummary[2].value = contentData.totalComments || 0;
        dataSummary[2].trend = contentData.commentsGrowthRate || 0;
      }
      
      // 获取匹配统计数据
      try {
        const matchData = await dashboardApi.getMatchStats();
        if (matchData) {
          dataSummary[4].value = (matchData.matchSuccessRate || 0) + '%';
          // 计算相对上周的匹配率变化
          const lastWeekRate = matchData.lastWeekMatchRate || 0;
          const currentRate = matchData.matchSuccessRate || 0;
          dataSummary[4].trend = lastWeekRate > 0 ? Math.round((currentRate - lastWeekRate) * 10) / 10 : 0;
        }
      } catch (matchError) {
        console.error('获取匹配统计数据失败:', matchError);
        // 设置默认值
        dataSummary[4].value = '0%';
        dataSummary[4].trend = 0;
      }
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error);
  }
};

// 获取用户增长数据
const fetchUserGrowthData = async () => {
  try {
    const data = await dashboardApi.getUserGrowthTrend(userGrowthTimeRange.value);
    if (data && userGrowthChartInstance) {
      const option = {
        title: {
          text: '用户增长趋势',
          left: 'center',
          show: false
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.dates || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value',
          name: '新增用户数'
        },
        series: [
          {
            name: '新增用户',
            type: 'bar',
            barWidth: '60%',
            data: data.values || [10, 15, 12, 20, 25, 18, 30],
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      };
      
      userGrowthChartInstance.setOption(option);
    }
  } catch (error) {
    console.error('获取用户增长数据失败:', error);
  }
};

// 获取内容发布趋势数据
const fetchContentPublishData = async () => {
  try {
    const data = await dashboardApi.getContentPublishTrend(contentPublishTimeRange.value);
    if (data && contentPublishChartInstance) {
      const option = {
        title: {
          text: '内容发布趋势',
          left: 'center',
          show: false
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.dates || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value',
          name: '发布数量'
        },
        series: [
          {
            name: '动态',
            type: 'line',
            data: data.postValues || [30, 40, 35, 50, 55, 45, 60],
            smooth: true,
            itemStyle: {
              color: '#67C23A'
            }
          },
          {
            name: '评论',
            type: 'line',
            data: data.commentValues || [70, 80, 65, 90, 95, 85, 100],
            smooth: true,
            itemStyle: {
              color: '#E6A23C'
            }
          }
        ]
      };
      
      contentPublishChartInstance.setOption(option);
    }
  } catch (error) {
    console.error('获取内容发布趋势数据失败:', error);
  }
};

// 获取用户性别分布数据
const fetchUserGenderDistribution = async () => {
  try {
    const data = await dashboardApi.getUserGenderDistribution();
    if (data && genderDistributionChartInstance) {
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 'bottom'
        },
        series: [
          {
            name: '用户性别',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { 
                value: data.maleUsers || 55, 
                name: '男性', 
                itemStyle: { color: '#409EFF' } 
              },
              { 
                value: data.femaleUsers || 35, 
                name: '女性', 
                itemStyle: { color: '#F56C6C' } 
              },
              { 
                value: data.unknownUsers || 10, 
                name: '未知', 
                itemStyle: { color: '#909399' } 
              }
            ]
          }
        ]
      };
      
      genderDistributionChartInstance.setOption(option);
    }
  } catch (error) {
    console.error('获取用户性别分布数据失败:', error);
  }
};

// 获取系统概要数据
const fetchSystemSummary = async () => {
  try {
    const data = await dashboardApi.getSystemSummary();
    if (data) {
      Object.assign(systemStatus, data);
    }
  } catch (error) {
    console.error('获取系统概要数据失败:', error);
  }
};

// 获取匹配趋势数据
const fetchMatchTrendData = async () => {
  try {
    const data = await dashboardApi.getMatchTrend(matchTrendTimeRange.value);
    if (data && matchTrendChartInstance) {
      const option = {
        title: {
          text: '匹配成功趋势',
          left: 'center',
          show: false
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'line'
          },
          formatter: function(params) {
            const date = params[0].axisValue;
            let result = `${date}<br/>`;
            
            // 匹配成功率
            const rateItem = params[0];
            result += `${rateItem.seriesName}: ${rateItem.value}%<br/>`;
            
            // 匹配总数和成功数
            if (params.length > 1) {
              const totalItem = params[1];
              const successItem = params[2];
              result += `${totalItem.seriesName}: ${totalItem.value}<br/>`;
              result += `${successItem.seriesName}: ${successItem.value}`;
            }
            
            return result;
          }
        },
        legend: {
          data: ['匹配成功率', '匹配总数', '成功匹配数'],
          bottom: 'bottom'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.dates || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: [
          {
            type: 'value',
            name: '匹配成功率(%)',
            position: 'left',
            axisLabel: {
              formatter: '{value}%'
            },
            max: function(value) {
              return Math.ceil(value.max * 1.2);
            }
          },
          {
            type: 'value',
            name: '匹配数量',
            position: 'right',
            axisLine: {
              show: true
            },
            axisTick: {
              show: true
            }
          }
        ],
        series: [
          {
            name: '匹配成功率',
            type: 'line',
            data: data.rateValues || [12.5, 13.2, 11.8, 14.2, 15.8, 13.5, 12.9],
            yAxisIndex: 0,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3
            },
            itemStyle: {
              color: '#9254de'
            },
            emphasis: {
              focus: 'series'
            },
            markLine: {
              data: [
                {
                  type: 'average',
                  name: '平均值'
                }
              ]
            }
          },
          {
            name: '匹配总数',
            type: 'bar',
            data: data.totalValues || [120, 132, 101, 134, 90, 180, 165],
            yAxisIndex: 1,
            barWidth: 10,
            itemStyle: {
              color: '#69c0ff'
            },
            emphasis: {
              focus: 'series'
            },
            z: 2
          },
          {
            name: '成功匹配数',
            type: 'bar',
            data: data.successValues || [15, 17, 12, 19, 14, 24, 21],
            yAxisIndex: 1,
            barWidth: 10,
            itemStyle: {
              color: '#5cdbd3'
            },
            emphasis: {
              focus: 'series'
            },
            z: 3
          }
        ]
      };
      
      matchTrendChartInstance.setOption(option);
    }
  } catch (error) {
    console.error('获取匹配趋势数据失败:', error);
  }
};

// 组件挂载时
onMounted(() => {
  initCharts();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时清除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  // 销毁图表实例
  userGrowthChartInstance?.dispose();
  contentPublishChartInstance?.dispose();
  genderDistributionChartInstance?.dispose();
  matchTrendChartInstance?.dispose();
});
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .header-actions {
    display: flex;
    align-items: center;
    
    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 5px;
      border-radius: 6px;
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.page-title {
  margin-top: 0;
  margin-bottom: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 5px;
    height: 70%;
    width: 4px;
    background-color: #409EFF;
    border-radius: 2px;
  }
}

.data-overview {
  margin-bottom: 24px;
  
  .el-col {
    margin-bottom: 16px;
  }
  
  .data-card {
    height: 120px;
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    overflow: hidden;
    border: none;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
    
    .el-card__body {
      padding: 0;
      height: 100%;
    }
    
    .card-content {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 0 16px;
      box-sizing: border-box;
    }
    
    .card-icon {
      width: 60px;
      height: 60px;
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 14px;
      margin-right: 16px;
      color: #fff;
      font-size: 26px;
      box-shadow: 0 6px 12px rgba(0, 0, 0, 0.12);
      
      .el-icon {
        font-size: 26px;
      }
    }
    
    .card-info {
      flex: 1;
    }
    
    .card-value {
      font-size: 30px;
      font-weight: 700;
      line-height: 1.2;
      margin-bottom: 8px;
      color: #303133;
    }
    
    .card-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 6px;
    }
    
    .card-trend {
      font-size: 12px;
      display: flex;
      align-items: center;
      
      .trend-value {
        font-weight: 600;
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 2px;
          font-size: 12px;
        }
        
        &.up {
          color: #67C23A;
        }
        
        &.down {
          color: #F56C6C;
        }
        
        &.neutral {
          color: #909399;
        }
      }
      
      .trend-label {
        color: #909399;
        margin-left: 4px;
      }
    }
  }
}

.dashboard-main {
  margin-bottom: 24px;
}

.chart-container {
  margin-bottom: 24px;
  
  .el-col {
    margin-bottom: 24px;
  }
  
  .chart-card {
    height: 400px;
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    overflow: hidden;
    border: none;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
    }
    
    :deep(.el-card__body) {
      padding: 16px;
    }
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        position: relative;
        padding-left: 10px;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 25%;
          height: 50%;
          width: 3px;
          background-color: #409EFF;
          border-radius: 1.5px;
        }
      }
      
      :deep(.el-radio-group) {
        .el-radio-button__inner {
          border-radius: 4px;
          padding: 5px 12px;
        }
        
        .el-radio-button:first-child .el-radio-button__inner {
          border-radius: 4px 0 0 4px;
        }
        
        .el-radio-button:last-child .el-radio-button__inner {
          border-radius: 0 4px 4px 0;
        }
      }
    }
    
    .chart {
      height: 320px;
      display: flex;
      justify-content: center;
      align-items: center;
      
      :deep(.el-skeleton) {
        width: 100%;
        padding: 20px;
      }
    }
    
    .chart-pie {
      height: 320px;
    }
  }
  
  .system-status-card {
    height: auto;
    min-height: 240px;
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    overflow: hidden;
    border: none;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
    }
    
    :deep(.el-card__body) {
      padding: 16px;
    }
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        position: relative;
        padding-left: 10px;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 25%;
          height: 50%;
          width: 3px;
          background-color: #409EFF;
          border-radius: 1.5px;
        }
      }
    }
    
    :deep(.el-descriptions) {
      margin-top: 16px;
      
      .el-descriptions__label {
        font-weight: 500;
      }
      
      .el-descriptions__content {
        font-weight: 400;
      }
      
      .el-progress {
        margin: 5px 0;
      }
    }
  }
}

@media (max-width: 1200px) {
  .data-overview {
    .data-card {
      .card-icon {
        width: 50px;
        height: 50px;
        font-size: 22px;
        
        .el-icon {
          font-size: 22px;
        }
      }
      
      .card-value {
        font-size: 24px;
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .chart-container {
    .chart-card, .system-status-card {
      height: auto;
      
      .chart {
        height: 280px;
      }
    }
  }
}
</style> 