# Social-Admin

社交应用管理后台

## 项目简介

Social-Admin是一个社交应用的管理后台项目，基于Vue 3、Vite、Element Plus和Pinia实现。

## 技术栈

- Vue 3
- Vite
- Element Plus
- Pinia
- Axios
- ECharts
- Vue Router

## 项目结构

```
Social-Admin/
├── src/
│   ├── api/              # API接口
│   ├── assets/           # 静态资源
│   ├── components/       # 通用组件
│   ├── layouts/          # 布局组件
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

- 用户管理：管理用户账号、权限设置
- 内容管理：管理用户发布的内容
- 数据统计：用户增长、活跃度等数据分析
- 系统设置：系统参数配置

## 测试账号

管理员账号：

- 用户名：admin
- 密码：123456
