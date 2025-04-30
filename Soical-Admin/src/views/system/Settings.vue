<template>
  <div class="system-settings-container">
    <div class="settings-card">
      <el-tabs v-model="activeKey" class="settings-tabs">
        <el-tab-pane label="安全设置" name="security">
          <security-settings />
        </el-tab-pane>
        <el-tab-pane label="AI助手设置" name="ai">
          <ai-settings />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SecuritySettings from '../../pages/system/components/SecuritySettings.vue';
import AiSettings from '../../pages/system/components/AiSettings.vue';

const activeKey = ref('security');
</script>

<style scoped>
.system-settings-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.settings-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-tabs {
  padding: 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e8e8e8;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
}

:deep(.el-tabs__item.is-active) {
  color: #1890ff;
  font-weight: 500;
}

:deep(.el-tabs__active-bar) {
  background-color: #1890ff;
  height: 3px;
  border-radius: 2px;
}
</style> 