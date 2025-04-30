<template>
  <div class="system-settings-container">
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="basic" tab="基础设置">
          <basic-settings />
        </a-tab-pane>
        <a-tab-pane key="security" tab="安全设置">
          <security-settings />
        </a-tab-pane>
        <a-tab-pane key="notification" tab="通知设置">
          <notification-settings />
        </a-tab-pane>
        <a-tab-pane key="maintenance" tab="系统维护">
          <maintenance-settings />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import BasicSettings from './components/BasicSettings.vue';
import SecuritySettings from './components/SecuritySettings.vue';
import NotificationSettings from './components/NotificationSettings.vue';
import MaintenanceSettings from './components/MaintenanceSettings.vue';

const activeKey = ref('basic');
</script>

<style scoped>
.system-settings-container {
  padding: 24px;
  background-color: #f0f2f5;
}
</style> 