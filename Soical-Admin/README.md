# Soical-Admin 🎮

社交应用管理后台 - 让管理更简单，让运营更高效！

## 📝 项目简介

Soical-Admin 是一个现代化的社交应用管理后台项目，基于 Vue 3、Vite、Element Plus 和 Pinia 实现。它提供了强大的管理功能和直观的数据可视化，帮助运营团队高效管理社交平台。

## 💻 技术栈

- 🖖 Vue 3
- ⚡ Vite
- 🎨 Element Plus
- 🍍 Pinia
- 📡 Axios
- 📊 ECharts
- 🛣️ Vue Router
- ✨ ESLint + Prettier
- 🧪 Vitest

## 📁 项目结构

```
Soical-Admin/
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

## 🚀 快速开始

### ⚙️ 环境变量配置
- 📡 API基础URL
VITE_API_BASE_URL=http://后端地址/

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

- 👥 用户管理：管理用户账号、权限设置
- 📝 内容管理：管理用户发布的内容
- 📊 数据统计：用户增长、活跃度等数据分析
- ⚙️ 系统设置：系统参数配置
- 🔍 内容审核：用户内容审核、举报处理
- 📈 运营数据：用户行为分析、趋势图表
- 🔔 消息管理：系统通知、用户反馈
- 🛡️ 安全中心：操作日志、安全设置

## 🔑 测试账号

管理员账号：

- 👨‍💼 用户名：admin
- 🔐 密码：123456

## ✨ 项目特点

- 🎨 美观界面：采用 Element Plus 组件库，提供专业的管理界面
- ⚡ 快速响应：基于 Vite 构建，提供极速的开发体验
- 📊 数据可视化：集成 ECharts，提供丰富的数据展示
- 🔄 状态管理：使用 Pinia 实现高效的状态管理
- 🧪 测试覆盖：完善的单元测试和测试覆盖率
- 📦 按需加载：组件和路由的按需加载，优化性能
- 🔒 安全可靠：完善的权限控制和操作审计

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
