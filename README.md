# 特色农产品销售平台

基于 SpringBoot 和 Vue3 的前后端分离特色农产品销售平台。

## 技术栈

### 后端
- **框架**: Spring Boot 3.1.5
- **语言**: Java 17
- **ORM**: MyBatis-Plus 3.5.4.1
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0+
- **构建工具**: Maven
- **部署**: WAR包部署到Tomcat

### 前端
- **框架**: Vue 3.3.4
- **构建工具**: Vite 5.0.5
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **UI组件**: Element Plus 2.4.4
- **HTTP客户端**: Axios 1.6.2

## 项目结构

```
nbcj-project/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/agricultural/
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── controller/      # 控制器
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── mapper/          # Mapper接口
│   │   │   │   ├── service/          # 服务层
│   │   │   │   ├── common/          # 通用类
│   │   │   │   ├── filter/          # 过滤器
│   │   │   │   └── util/            # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml   # 配置文件
│   │   └── pom.xml                   # Maven配置
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── views/           # 页面组件
│   │   ├── layouts/         # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # 状态管理
│   │   └── main.js          # 入口文件
│   ├── package.json
│   └── vite.config.js
├── database/                # 数据库脚本
│   └── init.sql
└── README.md
```

## 功能模块

### 1. 用户管理模块
- 用户注册、登录
- 权限控制（普通用户、商家、管理员）
- 个人信息维护
- 地址管理

### 2. 商品管理模块
- 商品分类展示
- 关键词搜索
- 商品详情查看
- 库存与价格管理
- 商家上架/下架商品

### 3. 购物车模块
- 添加商品到购物车
- 批量选择、删除
- 修改数量
- 跳转至订单结算

### 4. 订单管理模块
- 订单生成
- 支付模拟
- 订单状态跟踪（待支付、已支付、已发货、已完成）

### 5. 评价互动模块
- 用户对已购商品进行文字评价
- 商品评分（1-5星）

### 6. 个人中心模块
- 修改个人信息（用户名、密码、头像、收货地址）

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Tomcat 9.0+ (可选，用于WAR部署)

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE agricultural_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p agricultural_db < database/init.sql
```

3. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agricultural_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 编译项目：
```bash
mvn clean package
```

3. 运行项目（开发模式）：
```bash
mvn spring-boot:run
```

或者打包为WAR文件部署到Tomcat：
```bash
mvn clean package
# 将 target/agricultural-platform-1.0.0.war 复制到 Tomcat 的 webapps 目录
```

后端服务默认运行在：http://localhost:8080

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端服务默认运行在：http://localhost:3000

## API接口说明

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 用户接口
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息

### 商品接口
- `GET /api/products/public/list` - 获取商品列表（分页）
- `GET /api/products/public/{id}` - 获取商品详情
- `POST /api/products` - 创建商品（需要商家或管理员权限）
- `PUT /api/products/{id}` - 更新商品（需要商家或管理员权限）

### 购物车接口
- `GET /api/cart` - 获取购物车
- `POST /api/cart` - 添加商品到购物车
- `PUT /api/cart/{id}` - 更新购物车商品数量
- `DELETE /api/cart/{id}` - 删除购物车商品

### 订单接口
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 获取用户订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `POST /api/orders/{id}/pay` - 支付订单
- `POST /api/orders/{id}/complete` - 完成订单

### 地址接口
- `GET /api/addresses` - 获取用户地址列表
- `POST /api/addresses` - 添加地址
- `PUT /api/addresses/{id}` - 更新地址
- `DELETE /api/addresses/{id}` - 删除地址

### 评价接口
- `GET /api/evaluations/product/{productId}` - 获取商品评价
- `POST /api/evaluations` - 创建评价

## 默认账号

测试账号（密码均为：123456）：
- 管理员：admin
- 商家：merchant1
- 普通用户：user1

## 部署说明

### 后端部署（WAR包方式）

1. 修改 `pom.xml` 中的打包方式为 `war`
2. 执行打包命令：
```bash
mvn clean package
```
3. 将生成的 `target/agricultural-platform-1.0.0.war` 复制到 Tomcat 的 `webapps` 目录
4. 启动 Tomcat 服务器

### 前端部署

1. 构建生产版本：
```bash
npm run build
```
2. 将 `dist` 目录中的文件部署到 Nginx 或其他 Web 服务器

## 注意事项

1. JWT密钥配置在 `application.yml` 中，生产环境请修改为更安全的密钥
2. 数据库连接信息需要根据实际环境修改
3. 前端API代理配置在 `vite.config.js` 中，生产环境需要配置实际的后端地址
4. 默认密码为 `123456`，生产环境请修改

## 开发规范

- 后端采用 RESTful API 设计
- 统一使用 Result 类封装返回结果
- 使用 JWT 进行身份认证
- 前端使用 Pinia 进行状态管理
- 使用 Element Plus 作为 UI 组件库

## 许可证

MIT License

















