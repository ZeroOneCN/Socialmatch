# Soical-App 📱

社交应用客户端 - 连接你我，分享生活！

## 📝 项目简介

Soical-App 是一个现代化的社交应用移动端项目，基于 Vue 3、Vite、Vant UI 和 Pinia 实现。它提供了流畅的用户体验和丰富的社交功能，让用户随时随地分享生活点滴。

## 💻 技术栈

- 🖖 Vue 3
- ⚡ Vite
- 🎨 Vant UI
- 🍍 Pinia
- 📡 Axios
- 💬 Socket.io-client
- 🛣️ Vue Router
- ✨ ESLint + Prettier
- 🧪 Vitest

## 📁 项目结构

```
Soical-App/
├── src/
│   ├── api/              # API接口
│   ├── assets/           # 静态资源
│   ├── components/       # 通用组件
│   ├── composables/      # 组合式API
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── utils/            # 工具类
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── public/               # 公共资源
├── .env                  # 环境变量
├── index.html            # HTML模板
├── vite.config.js        # Vite配置
└── package.json          # 项目依赖
```

## 🚀 快速开始

### ⚙️ 环境变量配置
- 📡 API基础URL
VITE_API_BASE_URL=http://后端地址/
- 🗺️ 高德地图API配置（前往高德地图开发者获取）
VITE_AMAP_KEY=
VITE_AMAP_SECURITY_CODE=

### 🛠️ 环境准备

- ⚡ Node.js 16.0+
- 📦 npm 8.0+

### 📥 安装依赖

```bash
npm install
```

### 💻 开发模式

```bash
npm run dev
```

### 🏗️ 构建生产版本

```bash
npm run build
```

### 🧪 单元测试

```bash
npm run test:unit
```

### 📊 测试覆盖率

```bash
npm run test:coverage
```

## ✨ 功能模块

- 👤 用户认证：注册、登录、找回密码
- 👥 个人中心：个人资料、设置
- 🤝 社交功能：好友管理、消息聊天、动态发布
- 📱 内容浏览：动态列表、用户推荐
- 💬 实时通讯：基于 Socket.io 的即时通讯
- 🖼️ 媒体处理：图片上传、预览、压缩
- 🔔 消息通知：系统通知、互动提醒
- 🔍 搜索功能：用户搜索、内容搜索

## 🔑 测试账号

普通用户：（自行前往数据库添加）

- 👤 用户名：testuser
- 🔐 密码：123456

## ✨ 项目特点

- 🎨 美观界面：采用 Vant UI 组件库，提供精美的移动端界面
- ⚡ 快速响应：基于 Vite 构建，提供极速的开发体验
- 📱 移动优先：针对移动端优化的交互体验
- 🔄 状态管理：使用 Pinia 实现高效的状态管理
- 💬 实时通讯：集成 Socket.io 实现即时通讯
- 🧪 测试覆盖：完善的单元测试和测试覆盖率
- 📦 按需加载：组件和路由的按需加载，优化性能
- 🔒 安全可靠：完善的用户认证和权限控制

## 📋 开发规范

- ✨ 使用 ESLint + Prettier 进行代码规范检查
- 🛠️ 遵循 Vue 3 组合式 API 的最佳实践
- 📝 采用 TypeScript 进行类型检查
- 🔄 遵循 Git Flow 工作流程
- 📝 保持代码注释的完整性

## ⚠️ 注意事项

- 🛠️ 开发前请确保已安装所需环境
- 🔒 注意保护敏感信息，不要提交到版本控制
- 📋 遵循项目的代码规范和提交规范
- 🔄 定期更新依赖包，确保安全性