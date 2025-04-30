# Social-App

社交应用客户端

## 项目简介

Social-App是一个社交应用的移动端项目，基于Vue 3、Vite、Vant UI和Pinia实现。

## 技术栈

- Vue 3
- Vite
- Vant UI
- Pinia
- Axios
- Socket.io-client
- Vue Router

## 项目结构

```
Social-App/
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

## 快速开始

### 环境准备

- Node.js 16.0+
- npm 8.0+

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 单元测试

```bash
npm run test:unit
```

### 测试覆盖率

```bash
npm run test:coverage
```

## 功能模块

- 用户认证：注册、登录、找回密码
- 个人中心：个人资料、设置
- 社交功能：好友管理、消息聊天、动态发布
- 内容浏览：动态列表、用户推荐
- 实时通讯：基于Socket.io的即时通讯

## 测试账号

普通用户：

- 用户名：testuser
- 密码：123456
