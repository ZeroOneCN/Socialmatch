# Social - 社交应用系统

社交应用全栈系统，包含移动端、管理后台和服务端。

## 项目概述

Social是一个完整的社交应用系统，包含以下三个主要组件：

1. **Social-App** - 基于Vue 3和Vant UI开发的移动端应用
2. **Social-Admin** - 基于Vue 3和Element Plus开发的管理后台
3. **Social-Server** - 基于Spring Boot和MyBatis-Plus开发的后端服务

## 系统架构

```
Social/
├── Social-App/           # 移动端应用
├── Social-Admin/         # 管理后台
├── Social-Server/        # 后端服务
└── docs/                 # 项目文档
```

## 技术栈

### 前端技术

- Vue 3 - 前端框架
- Vite - 构建工具
- Pinia - 状态管理
- Vue Router - 路由管理
- Vant UI - 移动端UI组件库
- Element Plus - 管理后台UI组件库
- Axios - HTTP客户端
- Socket.io - 实时通讯

### 后端技术

- Spring Boot - 后端框架
- MyBatis-Plus - ORM框架
- Spring Security - 安全框架
- JWT - 认证方案
- MySQL - 数据库
- Redis - 缓存

## 快速开始

请参考各组件目录中的README文件，了解各组件的设置和启动方法：

- [Social-App README](./Social-App/README.md)
- [Social-Admin README](./Social-Admin/README.md)
- [Social-Server README](./Social-Server/README.md)

## 系统功能

### 用户端功能

- 用户注册与登录
- 个人资料管理
- 好友关系管理
- 消息聊天
- 动态发布与浏览
- 用户推荐

### 管理端功能

- 用户管理
- 内容审核
- 系统设置
- 数据统计与分析

## 数据库初始化

使用以下步骤初始化数据库：

1. 创建数据库：social002
2. 执行初始化脚本：`init_social_db.sql`

## 截图展示

(TODO: 添加系统主要功能的截图)

## 开发团队

(TODO: 添加开发团队信息)

## 许可证

(TODO: 添加许可证信息)
