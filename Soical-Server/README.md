# Soical-Server 🚀

社交应用服务端 - 让社交更简单，让生活更有趣！

## 📝 项目简介

Soical-Server 是一个现代化的社交应用服务端项目，基于 Spring Boot、MyBatis-Plus、Spring Security 和 JWT 实现。它提供了完整的社交功能，包括用户管理、好友关系、动态发布等核心功能。

## 💻 技术栈

- 🍃 Spring Boot 2.7.0
- 📊 MyBatis-Plus 3.5.2
- 🔒 Spring Security
- 🔑 JWT
- 🐬 MySQL 8.0+
- 🎯 Redis
- 📚 Swagger 3.0.0
- ☁️ Aliyun OSS

## 📁 项目结构

```
Soical-Server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/soical/server/
│   │   │       ├── config/           # 配置类
│   │   │       ├── controller/       # 控制器
│   │   │       ├── service/          # 服务层
│   │   │       ├── mapper/           # 数据访问层
│   │   │       ├── entity/           # 实体类
│   │   │       ├── dto/              # 数据传输对象
│   │   │       ├── util/             # 工具类
│   │   │       └── common/           # 公共类
│   │   └── resources/
│   │       ├── application.yml       # 配置文件
│   │       ├── mapper/               # MyBatis映射文件
│   │       └── db/                   # 数据库脚本
│   └── test/                         # 测试代码
└── pom.xml                           # Maven配置
```

## 🚀 快速开始

### 🛠️ 环境准备

- ☕ JDK 1.8+
- 📦 Maven 3.6+
- 🐬 MySQL 8.0+
- 🎯 Redis 6.0+

### 💾 数据库初始化

1. 创建数据库：social
2. 执行初始化脚本：`src/main/resources/db/init.sql`

### ⚙️ 修改配置

修改 `src/main/resources/application.yml` 中的配置信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/social?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 🚀 启动项目

```bash
mvn spring-boot:run
```

## 📚 接口文档

启动项目后，访问 Swagger 文档：

```
http://localhost:8080/api/doc.html
```

## ✨ 功能模块

- 👤 用户认证：用户注册、登录、鉴权
- 👥 用户管理：用户信息查询、修改
- 🤝 社交功能：好友关系、消息管理
- 📝 内容服务：动态发布、评论、点赞
- 📤 文件上传：图片和其他媒体文件上传（支持阿里云 OSS）

## 🔑 测试账号

管理员账号：

- 👨‍💼 用户名：admin
- 🔐 密码：123456

## 🧪 接口测试流程

1. 📝 注册用户
   
   - 接口：`POST /api/auth/register`
   - 请求体：
     
     ```json
     {
       "username": "testuser",
       "phone": "13800138000",
       "password": "123456",
       "gender": 1,
       "nickname": "测试用户"
     }
     ```

2. 🔑 用户登录
   
   - 接口：`POST /api/auth/login`
   - 请求体：
     
     ```json
     {
       "username": "testuser",
       "password": "123456"
     }
     ```
   - 响应中获取 token

3. 👤 获取用户信息
   
   - 接口：`GET /api/user/current`
   - 请求头：`Authorization: Bearer {token}`

4. ✏️ 更新用户资料
   
   - 接口：`PUT /api/user/profile`
   - 请求头：`Authorization: Bearer {token}`
   - 请求体：
     
     ```json
     {
       "nickname": "新昵称",
       "selfIntro": "这是我的自我介绍",
       "location": "北京",
       "occupation": "工程师",
       "education": 2
     }
     ```

## ✨ 项目特点

- 🔒 安全可靠：使用 Spring Security 和 JWT 实现安全的身份认证
- 🚀 高性能：集成 Redis 缓存，提升系统响应速度
- 📦 易扩展：模块化设计，便于功能扩展
- 📚 文档完善：集成 Swagger，提供完整的 API 文档
- ☁️ 云存储：支持阿里云 OSS 文件存储
- 🔄 事务管理：完善的事务管理机制，确保数据一致性

## ⚠️ 注意事项

- 🛠️ 请确保 MySQL 和 Redis 服务已启动
- 💾 首次运行需要执行数据库初始化脚本
- 🔒 生产环境部署时请修改相关密钥和配置信息
- 🔐 建议使用 HTTPS 进行安全传输