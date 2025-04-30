<template>
  <div class="system-settings-page">
    <a-card title="系统设置" :bordered="false">
      <a-tabs defaultActiveKey="1">
        <a-tab-pane key="1" tab="基本设置">
          <basic-settings />
        </a-tab-pane>
        <a-tab-pane key="2" tab="安全设置">
          <security-settings />
        </a-tab-pane>
        <a-tab-pane key="3" tab="通知设置">
          <notification-settings />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import BasicSettings from './components/BasicSettings.vue';
import SecuritySettings from './components/SecuritySettings.vue';
import NotificationSettings from './components/NotificationSettings.vue';
import { message } from 'ant-design-vue';

onMounted(() => {
  console.log('系统设置页面已加载');
});
</script>

<style scoped>
.system-settings-page {
  padding: 20px;
}
</style> 