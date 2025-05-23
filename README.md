# Soical 社交应用项目 🚀

一个现代化的社交应用解决方案，包含服务端、移动端和管理后台。

## 📝 项目简介

Soical 是一个完整的社交应用解决方案，采用前后端分离架构，包含三个主要部分：

- 🖥️ **Soical-Server**: 基于 Spring Boot 的后端服务
- 📱 **Soical-App**: 基于 Vue 3 的移动端应用
- 🎮 **Soical-Admin**: 基于 Vue 3 的管理后台

## 💻 技术栈概览

### 🖥️ 服务端 (Soical-Server)
- 🍃 Spring Boot 2.7.0
- 📊 MyBatis-Plus 3.5.2
- 🔒 Spring Security
- 🔑 JWT
- 🐬 MySQL 8.0+
- 🎯 Redis
- 📚 Swagger 3.0.0
- ☁️ Aliyun OSS

### 📱 移动端 (Soical-App)
- 🖖 Vue 3
- ⚡ Vite
- 🎨 Vant UI
- 🍍 Pinia
- 📡 Axios
- 💬 Socket.io-client
- 🛣️ Vue Router
- ✨ ESLint + Prettier
- 🧪 Vitest

### 🎮 管理后台 (Soical-Admin)
- 🖖 Vue 3
- ⚡ Vite
- 🎨 Element Plus
- 🍍 Pinia
- 📡 Axios
- 📊 ECharts
- 🛣️ Vue Router
- ✨ ESLint + Prettier
- 🧪 Vitest

## 📸 系统截图

### 移动端
![登录页面](./docs/images/1-登录页面.png)
![个人中心](./docs/images/2-个人中心.png)
![消息列表](./docs/images/3-消息列表.png)
![动态页面](./docs/images/4-动态列表.png)

### 管理后台
![登录页面](./docs/images/1-后台登录页面.png)
![用户管理](./docs/images/2-后台用户管理.png)
![内容审核](./docs/images/3-后台动态审核.png)
![数据统计](./docs/images/4-后台数据统计.png)

## 🚀 快速开始

### 🛠️ 环境准备

- ⚡ Node.js 16.0+
- 📦 npm 8.0+
- ☕ JDK 1.8+
- 🐬 MySQL 8.0+
- 🎯 Redis 6.0+

### 📥 项目克隆

```bash
git clone https://github.com/ZeroOneCN/Socialmatch.git
cd Socialmatch
```

### 🖥️ 服务端启动

```bash
cd soical-server
mvn spring-boot:run
```

### 📱 移动端启动

```bash
cd soical-app
npm install
npm run dev
```

### 🎮 管理后台启动

```bash
cd soical-admin
npm install
npm run dev
```

## ✨ 功能特性

### 🖥️ 服务端功能
- 👤 用户认证与授权
- 👥 用户关系管理
- 📝 内容发布与管理
- 💬 即时通讯
- 📤 文件上传
- 🔄 缓存管理

### 📱 移动端功能
- 👤 用户认证：注册、登录、找回密码
- 👥 个人中心：个人资料、设置
- 🤝 社交功能：好友管理、消息聊天、动态发布
- 📱 内容浏览：动态列表、用户推荐
- 💬 实时通讯：基于 Socket.io 的即时通讯
- 🖼️ 媒体处理：图片上传、预览、压缩
- 🔔 消息通知：系统通知、互动提醒
- 🔍 搜索功能：用户搜索、内容搜索

### 🎮 管理后台功能
- 👥 用户管理：管理用户账号、权限设置
- 📝 内容管理：管理用户发布的内容
- 📊 数据统计：用户增长、活跃度等数据分析
- ⚙️ 系统设置：系统参数配置
- 🔍 内容审核：用户内容审核、举报处理
- 📈 运营数据：用户行为分析、趋势图表
- 🔔 消息管理：系统通知、用户反馈
- 🛡️ 安全中心：操作日志、安全设置

## 🔑 测试账号

### 🖥️ 服务端
- 👨‍💼 用户名：admin
- 🔐 密码：123456

### 📱 移动端
- 👤 用户名：testuser
- 🔐 密码：123456

### 🎮 管理后台
- 👨‍💼 用户名：admin
- 🔐 密码：123456

## 📋 开发规范

- ✨ 使用 ESLint + Prettier 进行代码规范检查
- 🛠️ 遵循各框架的最佳实践
- 📝 采用 TypeScript 进行类型检查
- 🔄 遵循 Git Flow 工作流程
- 📝 保持代码注释的完整性

## ⚠️ 注意事项

- 🛠️ 开发前请确保已安装所需环境
- 🔒 注意保护敏感信息，不要提交到版本控制
- 📋 遵循项目的代码规范和提交规范
- 🔄 定期更新依赖包，确保安全性
- 🌐 确保服务端、移动端和管理后台的配置正确

## 📚 项目文档

- [服务端文档](./Soical-Server/README.md)
- [移动端文档](./Soical-App/README.md)
- [管理后台文档](./Soical-Admin/README.md)
- [宝塔面板部署](./docs/部署教程之宝塔面板.md)