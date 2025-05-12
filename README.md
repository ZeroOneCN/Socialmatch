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

## 环境要求

### 开发环境

- Node.js 16.0+
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 服务器环境

- CentOS 7.0+ / Ubuntu 18.04+
- Nginx 1.18+
- MySQL 8.0+
- Redis 6.0+
- JDK 1.8+

## 快速开始

请参考各组件目录中的README文件，了解各组件的设置和启动方法：

- [Social-App README](./Social-App/README.md)
- [Social-Admin README](./Social-Admin/README.md)
- [Social-Server README](./Social-Server/README.md)

## 部署步骤

1. 数据库配置
   - 创建数据库：social002
   - 执行初始化脚本：`init_social_db.sql`
   - 配置数据库连接信息

2. 后端部署
   - 修改配置文件
   - 打包：`mvn clean package`
   - 运行：`java -jar social-server.jar`

3. 前端部署
   - 安装依赖：`npm install`
   - 构建：`npm run build`
   - 配置Nginx
   
详细部署说明请参考：[部署教程之宝塔面板](./docs/部署教程之宝塔面板.md)

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

## 系统截图

### 移动端
![登录页面](./docs/images/app-login.png)
![个人中心](./docs/images/app-profile.png)
![消息列表](./docs/images/app-messages.png)
![动态页面](./docs/images/app-moments.png)

### 管理后台
![登录页面](./docs/images/admin-login.png)
![用户管理](./docs/images/admin-users.png)
![内容审核](./docs/images/admin-content.png)
![数据统计](./docs/images/admin-stats.png)

## 开发团队

- 产品经理：张三
- 技术负责人：李四
- 前端开发：王五、赵六
- 后端开发：孙七、周八
- UI设计：吴九

## 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 联系我们

如有问题或建议，请联系：

- 邮箱：support@social.com
- QQ群：123456789
