# MiyaShop 女装电商系统

一个专注于女装、首饰、化妆用品的电商网站，具备秒杀和团购功能。

## 项目结构

```
miyashop/
├── miyashop-backend/          # SpringBoot后端
│   ├── miyashop-common/       # 公共模块
│   ├── miyashop-service/      # 业务服务层
│   └── miyashop-web/          # Web层(Controller + 配置)
│
├── miyashop-frontend/         # Vue3前端
│   ├── miyashop-admin/        # 后台管理系统
│   └── miyashop-h5/           # 移动端H5商城
│
└── README.md                  # 项目说明
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- MyBatis-Plus 3.5.x
- MySQL 8.0
- Redis 7.x
- Knife4j 4.x (API文档)

### 前端
- Vue 3.4.x
- TypeScript 5.x
- Element Plus (后台)
- Vant (移动端)
- Pinia (状态管理)
- Vite (构建工具)

## 快速开始

### 后端启动

```bash
cd miyashop-backend

# 1. 创建数据库
mysql -u root -p < miyashop-web/src/main/resources/schema.sql

# 2. 修改配置
# 编辑 miyashop-web/src/main/resources/application.yml
# 修改数据库连接、Redis配置等

# 3. 启动项目
mvn spring-boot:run
```

API文档地址: http://localhost:8080/doc.html

### 前端启动

#### 后台管理系统
```bash
cd miyashop-frontend/miyashop-admin
npm install
npm run dev
```

访问地址: http://localhost:3000

#### 移动端商城
```bash
cd miyashop-frontend/miyashop-h5
npm install
npm run dev
```

访问地址: http://localhost:3001

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 核心功能

### 商品管理
- 商品分类管理 (3级分类)
- 品牌管理
- 商品SPU/SKU管理
- 商品上下架
- 库存管理

### 订单管理
- 购物车
- 订单创建流程
- 订单状态流转
- 订单退款/售后

### 秒杀功能
- 秒杀活动配置
- 防超卖 (Redis Lua脚本)
- 限流 (令牌桶算法)
- 秒杀预热机制

### 团购功能
- 团购活动配置
- 团购队伍管理
- 拼团进度展示
- 团购超时处理

### 支付模块
- 微信支付
- 支付宝
- 支付回调处理
- 退款处理

## UI设计风格

### 配色方案
| 用途 | 颜色 | 十六进制 |
|------|------|---------|
| 主色调 (玫粉) | #FF6B95 | 主要按钮、高亮 |
| 辅助色 (纯白) | #FFFFFF | 背景、卡片 |
| 背景色 (浅灰) | #F5F5F5 | 页面背景 |
| 点缀色 (金色) | #C9A96E | VIP标识、活动标签 |

## 开发规范

### 后端
- 统一使用 `Result<T>` 作为返回结果
- 异常使用 `BusinessException` 抛出
- Controller使用 `@Tag`、`@Operation` 注解添加API文档
- Service层实现业务逻辑，Repository层处理数据访问

### 前端
- 使用 TypeScript 进行类型检查
- 使用 Pinia 进行状态管理
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case

## 许可证

MIT License