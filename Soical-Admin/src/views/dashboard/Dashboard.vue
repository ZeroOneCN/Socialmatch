<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-header">
            <div>
              <h2>欢迎使用Social管理后台</h2>
              <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
            <el-button type="primary" @click="refreshData">刷新数据</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="card-row">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" v-for="(card, index) in dataCards" :key="index">
        <el-card class="data-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value">{{ card.value }}</div>
              <div class="card-compare" v-if="card.compareText">
                <i :class="card.compareIcon"></i>
                <span>{{ card.compareText }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统状态卡片 -->
    <el-row :gutter="20" class="card-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>用户性别分布</span>
          </div>
          <div class="chart-container">
            <div id="gender-chart" ref="genderChart" style="height: 300px"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12" :lg="16">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>系统状态</span>
          </div>
          <div v-loading="loadingSystem">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="系统状态">
                <el-tag type="success">{{ systemData.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="版本号">{{ systemData.version }}</el-descriptions-item>
              <el-descriptions-item label="运行天数">{{ systemData.runningDays }} 天</el-descriptions-item>
              <el-descriptions-item label="启动时间">{{ systemData.startTime }}</el-descriptions-item>
              <el-descriptions-item label="CPU使用率">
                <el-progress :percentage="systemData.cpuUsage" :color="getProgressColor(systemData.cpuUsage)"></el-progress>
              </el-descriptions-item>
              <el-descriptions-item label="内存使用率">
                <el-progress :percentage="systemData.memoryUsage" :color="getProgressColor(systemData.memoryUsage)"></el-progress>
              </el-descriptions-item>
              <el-descriptions-item label="磁盘使用率">
                <el-progress :percentage="systemData.diskUsage" :color="getProgressColor(systemData.diskUsage)"></el-progress>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 趋势图表 -->
    <el-row :gutter="20" class="card-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header" class="card-header">
            <span>数据趋势</span>
            <div class="card-header-right">
              <el-radio-group v-model="trendType" size="small" @change="handleTrendTypeChange">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="用户增长趋势" name="users">
              <div class="chart-container" v-loading="loadingUserTrend">
                <div id="user-trend-chart" ref="userTrendChart" style="height: 350px"></div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="内容发布趋势" name="content">
              <div class="chart-container" v-loading="loadingContentTrend">
                <div id="content-trend-chart" ref="contentTrendChart" style="height: 350px"></div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts/core'
import { 
  BarChart, 
  LineChart, 
  PieChart 
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { 
  getUserStats, 
  getContentStats, 
  getSystemSummary, 
  getUserGenderDistribution,
  getUserGrowthTrend,
  getContentPublishTrend
} from '@/api/dashboard'

echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  BarChart,
  LineChart,
  PieChart,
  CanvasRenderer
])

export default {
  name: 'Dashboard',
  data() {
    return {
      currentDate: this.formatDate(new Date()),
      dataCards: [],
      systemData: {
        status: '正常运行',
        version: 'v1.0.0',
        startTime: '-',
        runningDays: 0,
        cpuUsage: 0,
        memoryUsage: 0,
        diskUsage: 0
      },
      loadingUser: true,
      loadingContent: true,
      loadingSystem: true,
      loadingUserTrend: true,
      loadingContentTrend: true,
      trendType: 'week',
      activeTab: 'users',
      userTrendData: {
        dates: [],
        values: []
      },
      contentTrendData: {
        dates: [],
        postValues: [],
        commentValues: []
      },
      genderDistribution: []
    }
  },
  created() {
    this.fetchAllData()
  },
  mounted() {
    this.$nextTick(() => {
      window.addEventListener('resize', this.resizeCharts)
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    this.destroyCharts()
  },
  methods: {
    async fetchAllData() {
      this.fetchUserStats()
      this.fetchContentStats()
      this.fetchSystemSummary()
      this.fetchGenderDistribution()
      this.fetchUserTrend()
      this.fetchContentTrend()
    },
    refreshData() {
      this.$message.success('数据刷新中...')
      this.fetchAllData()
    },
    async fetchUserStats() {
      try {
        this.loadingUser = true
        const { data } = await getUserStats()
        
        this.dataCards = [
          {
            title: '总用户数',
            value: this.formatNumber(data.totalUsers),
            icon: 'el-icon-user-solid',
            color: '#40C9C6'
          },
          {
            title: '今日新增用户',
            value: this.formatNumber(data.newUsersToday),
            icon: 'el-icon-user',
            color: '#36A3F7',
            compareText: `较昨日 ${this.getCompareText(data.newUsersToday, data.newUsersYesterday)}`,
            compareIcon: this.getCompareIcon(data.newUsersToday, data.newUsersYesterday)
          },
          {
            title: '活跃用户数',
            value: this.formatNumber(data.activeUsers),
            icon: 'el-icon-s-custom',
            color: '#F4516C'
          },
          {
            title: '用户增长率',
            value: data.userGrowthRate + '%',
            icon: 'el-icon-data-line',
            color: '#34BFA3'
          }
        ]
      } catch (error) {
        console.error('获取用户统计失败', error)
        this.$message.error('获取用户统计数据失败')
      } finally {
        this.loadingUser = false
      }
    },
    async fetchContentStats() {
      try {
        this.loadingContent = true
        const { data } = await getContentStats()
        
        // 追加内容统计卡片
        this.dataCards.push(
          {
            title: '总帖子数',
            value: this.formatNumber(data.totalPosts),
            icon: 'el-icon-document',
            color: '#975FE4'
          },
          {
            title: '今日发帖量',
            value: this.formatNumber(data.todayPosts),
            icon: 'el-icon-document-add',
            color: '#F59A23',
            compareText: `较昨日 ${this.getCompareText(data.todayPosts, data.yesterdayPosts)}`,
            compareIcon: this.getCompareIcon(data.todayPosts, data.yesterdayPosts)
          },
          {
            title: '总评论数',
            value: this.formatNumber(data.totalComments),
            icon: 'el-icon-chat-line-square',
            color: '#1ABBCE'
          },
          {
            title: '今日评论量',
            value: this.formatNumber(data.todayComments),
            icon: 'el-icon-chat-dot-square',
            color: '#FEC171',
            compareText: `较昨日 ${this.getCompareText(data.todayComments, data.yesterdayComments)}`,
            compareIcon: this.getCompareIcon(data.todayComments, data.yesterdayComments)
          }
        )
      } catch (error) {
        console.error('获取内容统计失败', error)
        this.$message.error('获取内容统计数据失败')
      } finally {
        this.loadingContent = false
      }
    },
    async fetchSystemSummary() {
      try {
        this.loadingSystem = true
        const { data } = await getSystemSummary()
        this.systemData = data
      } catch (error) {
        console.error('获取系统概要失败', error)
        this.$message.error('获取系统概要信息失败')
      } finally {
        this.loadingSystem = false
      }
    },
    async fetchGenderDistribution() {
      try {
        const { data } = await getUserGenderDistribution()
        this.genderDistribution = data
        this.renderGenderChart()
      } catch (error) {
        console.error('获取用户性别分布失败', error)
        this.$message.error('获取用户性别分布数据失败')
      }
    },
    async fetchUserTrend() {
      try {
        this.loadingUserTrend = true
        const { data } = await getUserGrowthTrend(this.trendType)
        this.userTrendData = data
        this.renderUserTrendChart()
      } catch (error) {
        console.error('获取用户增长趋势失败', error)
        this.$message.error('获取用户增长趋势数据失败')
      } finally {
        this.loadingUserTrend = false
      }
    },
    async fetchContentTrend() {
      try {
        this.loadingContentTrend = true
        const { data } = await getContentPublishTrend(this.trendType)
        this.contentTrendData = data
        this.renderContentTrendChart()
      } catch (error) {
        console.error('获取内容发布趋势失败', error)
        this.$message.error('获取内容发布趋势数据失败')
      } finally {
        this.loadingContentTrend = false
      }
    },
    renderGenderChart() {
      const chartDom = this.$refs.genderChart
      const chart = echarts.init(chartDom)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          data: this.genderDistribution.map(item => item.name)
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
            data: this.genderDistribution.map(item => ({
              name: item.name,
              value: item.value
            }))
          }
        ],
        color: ['#5470c6', '#ee6666', '#91cc75']
      }
      
      chart.setOption(option)
      this.genderChart = chart
    },
    renderUserTrendChart() {
      const chartDom = this.$refs.userTrendChart
      const chart = echarts.init(chartDom)
      
      const option = {
        title: {
          text: '用户增长趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
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
          boundaryGap: false,
          data: this.userTrendData.dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '新增用户',
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            data: this.userTrendData.values
          }
        ]
      }
      
      chart.setOption(option)
      this.userTrendChart = chart
    },
    renderContentTrendChart() {
      const chartDom = this.$refs.contentTrendChart
      const chart = echarts.init(chartDom)
      
      const option = {
        title: {
          text: '内容发布趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          data: ['帖子数量', '评论数量'],
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.contentTrendData.dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '帖子数量',
            type: 'line',
            stack: 'Total',
            emphasis: {
              focus: 'series'
            },
            data: this.contentTrendData.postValues
          },
          {
            name: '评论数量',
            type: 'line',
            stack: 'Total',
            emphasis: {
              focus: 'series'
            },
            data: this.contentTrendData.commentValues
          }
        ]
      }
      
      chart.setOption(option)
      this.contentTrendChart = chart
    },
    handleTrendTypeChange() {
      this.fetchUserTrend()
      this.fetchContentTrend()
    },
    handleTabClick() {
      this.$nextTick(() => {
        this.resizeCharts()
      })
    },
    resizeCharts() {
      if (this.genderChart) {
        this.genderChart.resize()
      }
      if (this.userTrendChart) {
        this.userTrendChart.resize()
      }
      if (this.contentTrendChart) {
        this.contentTrendChart.resize()
      }
    },
    destroyCharts() {
      if (this.genderChart) {
        this.genderChart.dispose()
        this.genderChart = null
      }
      if (this.userTrendChart) {
        this.userTrendChart.dispose()
        this.userTrendChart = null
      }
      if (this.contentTrendChart) {
        this.contentTrendChart.dispose()
        this.contentTrendChart = null
      }
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][date.getDay()]
      return `${year}年${month}月${day}日 ${week}`
    },
    formatNumber(num) {
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getCompareText(current, previous) {
      if (current === previous) return '持平'
      const diff = current - previous
      const percent = Math.abs(Math.round((diff / previous) * 100))
      return `${diff > 0 ? '增长' : '下降'} ${percent}%`
    },
    getCompareIcon(current, previous) {
      if (current > previous) return 'el-icon-top text-success'
      if (current < previous) return 'el-icon-bottom text-danger'
      return 'el-icon-minus text-info'
    },
    getProgressColor(value) {
      if (value < 60) return '#67C23A'
      if (value < 80) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  .welcome-card {
    margin-bottom: 20px;
    
    .welcome-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
        font-size: 20px;
      }
      
      p {
        margin: 5px 0 0;
        color: #606266;
      }
    }
  }
  
  .card-row {
    margin-bottom: 20px;
  }
  
  .data-card {
    height: 120px;
    margin-bottom: 20px;
    
    .card-content {
      display: flex;
      align-items: center;
      
      .card-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 15px;
        
        i {
          font-size: 28px;
          color: #fff;
        }
      }
      
      .card-info {
        flex: 1;
        
        .card-title {
          font-size: 14px;
          color: #909399;
        }
        
        .card-value {
          font-size: 24px;
          font-weight: bold;
          margin: 5px 0;
        }
        
        .card-compare {
          font-size: 12px;
          display: flex;
          align-items: center;
          
          i {
            margin-right: 5px;
          }
          
          .text-success {
            color: #67C23A;
          }
          
          .text-danger {
            color: #F56C6C;
          }
          
          .text-info {
            color: #909399;
          }
        }
      }
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      padding: 10px 0;
    }
  }
}
</style> 