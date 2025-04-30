# Soical-Server

社交应用服务端

## 项目简介

Soical-Server是一个社交应用的服务端项目，基于Spring Boot、MyBatis-Plus、Spring Security和JWT实现。

## 技术栈

- Spring Boot 2.7.0
- MyBatis-Plus 3.5.2
- Spring Security
- JWT
- MySQL
- Swagger 3.0.0

## 项目结构

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

## 快速开始

### 环境准备

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 数据库初始化

1. 创建数据库：soical002
2. 执行初始化脚本：`src/main/resources/db/init.sql`

### 修改配置

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/soical002?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 启动项目

```bash
mvn spring-boot:run
```

## 接口文档

启动项目后，访问Swagger文档：

```
http://localhost:8080/api/doc.html
```

## 功能模块

- 用户认证：用户注册、登录、鉴权
- 用户管理：用户信息查询、修改
- 社交功能：好友关系、消息管理
- 内容服务：动态发布、评论、点赞
- 文件上传：图片和其他媒体文件上传

## 测试账号

管理员账号：

- 用户名：admin
- 密码：123456

## 接口测试流程

1. 注册用户
   
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

2. 用户登录
   
   - 接口：`POST /api/auth/login`
   - 请求体：
     
     ```json
     {
       "username": "testuser",
       "password": "123456"
     }
     ```
   - 响应中获取token

3. 获取用户信息
   
   - 接口：`GET /api/user/current`
   - 请求头：`Authorization: Bearer {token}`

4. 更新用户资料
   
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
