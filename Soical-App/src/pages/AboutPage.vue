<template>
    <div class="about-page">
      <van-nav-bar
        title="关于我们"
        left-arrow
        @click-left="goBack"
        fixed
        placeholder
      />
      
      <div class="content-container">
        <div class="logo-section">
          <img src="https://image-0620.oss-cn-shenzhen.aliyuncs.com/images/2025/04/09/d60c0ae5e4d249ec8ba654fe54bb7d72.png" alt="Soical Logo" class="app-logo">
          <h1 class="app-name">Soical</h1>
          <p class="app-version">Version 1.0.0</p>
        </div>
        
        <div class="info-section">
          <h2 class="section-title">应用简介</h2>
          <p class="section-content">
            Soical是一款致力于帮助用户找到志同道合伙伴的社交应用。我们通过独特的同频匹配算法，帮助用户找到真正有共同话题和兴趣的朋友。在这里，您可以分享生活点滴，结识新朋友，发现更多可能。
          </p>
          
          <h2 class="section-title">联系我们</h2>
          <p class="section-content">
            官方邮箱：service@01iot.top
          </p>
          
          <h2 class="section-title">版权信息</h2>
          <p class="section-content">
            Copyright © 2025 Soical All Rights Reserved.
          </p>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
  
  const goBack = () => {
    router.back()
  }
  </script>
  
  <style scoped>
  .about-page {
    min-height: 100vh;
    background-color: #f7f8fa;
  }
  
  .content-container {
    padding: 24px 16px;
  }
  
  .logo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 32px 0;
  }
  
  .app-logo {
    width: 80px;
    height: 80px;
    border-radius: 16px;
    margin-bottom: 16px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }
  
  .app-name {
    font-size: 24px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 8px;
  }
  
  .app-version {
    font-size: 14px;
    color: #969799;
    margin: 0;
  }
  
  .info-section {
    background-color: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-top: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
  
  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #323233;
    margin: 0 0 12px;
    position: relative;
    padding-left: 12px;
  }
  
  .section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 16px;
    background-color: #1989fa;
    border-radius: 2px;
  }
  
  .section-content {
    font-size: 14px;
    color: #646566;
    line-height: 1.8;
    margin: 0 0 24px;
  }
  
  .section-content:last-child {
    margin-bottom: 0;
  }
  
  .icp-info {
    font-size: 12px;
    color: #969799;
    display: block;
    margin-top: 8px;
  }
  
  :deep(.van-nav-bar) {
    background-color: #ffffff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.van-nav-bar__title) {
    font-weight: 500;
    color: #323233;
  }
  </style>
  