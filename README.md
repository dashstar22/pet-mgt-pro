# pet-mgt 宠物领养平台

前后端分离的宠物领养管理平台，支持宠物信息管理、领养申请审核、AI 智能匹配与问答等完整功能。

## 技术架构

```
┌──────────────────────┐       ┌──────────────────────────┐
│   Vue 3 前端 (5173)   │ ────▶ │  Spring Boot 后端 (8080)  │
│                       │       │                          │
│  Vue 3.5 + Vite 8     │  REST │  Spring Boot 3.5.6       │
│  Element Plus 2.14    │  API  │  Spring Security + JWT   │
│  Pinia 2.3            │       │  MyBatis-Plus 3.5.9      │
│  Vue Router 4.6       │       │  MySQL 8.0               │
│  Axios 1.16           │       │  DeepSeek AI             │
└──────────────────────┘       └──────────────────────────┘
```

| 层 | 技术栈 |
|---|--------|
| 前端 | Vue 3.5 + Vite 8 + Element Plus 2.14 + Pinia 2.3 + Vue Router 4.6 + Axios 1.16 |
| 后端 | Spring Boot 3.5.6 + MyBatis-Plus 3.5.9 + Spring Security + JWT (jjwt 0.12.6) |
| 数据库 | MySQL 8.0 |
| AI | DeepSeek Chat API |

---

## 项目结构

```
pet-mgt/
├── frontend/                             # Vue 3 前端项目
│   ├── index.html
│   ├── vite.config.js                    # Vite 配置（代理 /api → 8080）
│   ├── package.json
│   └── src/
│       ├── main.js                       # 入口：挂载 Pinia / Router / Element Plus
│       ├── App.vue                       # 根组件（Header + RouterView + Footer）
│       ├── styles/global.css             # 全局样式 + Element Plus 紫粉主题
│       ├── router/index.js               # 路由表 + 导航守卫（auth / admin 鉴权）
│       ├── stores/                       # Pinia 状态管理
│       │   ├── user.js                   # 用户认证状态（token / userInfo / logout）
│       │   └── app.js                    # 全局数据缓存（品种列表）
│       ├── api/                          # API 请求层
│       │   ├── request.js                # Axios 实例（拦截器：JWT 注入 + 错误处理）
│       │   ├── auth.js                   # 注册 / 登录
│       │   ├── pet.js                    # 宠物列表 / 详情 / 图片
│       │   ├── breed.js                  # 品种查询
│       │   ├── user.js                   # 用户资料 / 修改密码
│       │   ├── application.js            # 用户申请提交 / 查看
│       │   ├── admin.js                  # 后台管理（用户/宠物/品种/审核 CRUD）
│       │   ├── ai.js                     # AI 匹配 / AI 问答
│       │   └── upload.js                 # 文件上传
│       ├── components/                   # 通用组件
│       │   ├── AppHeader.vue             # 顶部导航（毛玻璃效果，紫粉渐变 Logo）
│       │   ├── AppFooter.vue             # 页脚
│       │   ├── PetCard.vue               # 宠物卡片
│       │   ├── FileUpload.vue            # 图片上传组件
│       │   └── Pagination.vue            # 分页器
│       ├── views/                        # 页面视图（27 个）
│       │   ├── HomeView.vue              # 首页
│       │   ├── LoginView.vue             # 登录
│       │   ├── RegisterView.vue          # 注册
│       │   ├── PetListView.vue           # 宠物列表（搜索筛选）
│       │   ├── PetDetailView.vue         # 宠物详情
│       │   ├── NotFoundView.vue          # 404
│       │   ├── user/                     # 用户端页面
│       │   │   ├── ProfileView.vue       # 个人中心
│       │   │   ├── ApplyFormView.vue     # 提交领养申请
│       │   │   ├── MyApplicationsView.vue # 我的申请
│       │   │   ├── AiMatchView.vue       # AI 宠物匹配
│       │   │   ├── AiMatchResultView.vue # 匹配结果
│       │   │   ├── AiMatchHistoryView.vue # 匹配历史
│       │   │   └── AiChatView.vue        # AI 养宠问答
│       │   └── admin/                    # 管理员页面
│       │       ├── DashboardView.vue     # 控制台统计
│       │       ├── UserManageView.vue    # 用户管理
│       │       ├── BreedManageView.vue   # 品种管理
│       │       ├── PetManageView.vue     # 宠物管理
│       │       ├── PetFormView.vue       # 宠物添加/编辑
│       │       ├── ApplicationListView.vue # 审核列表
│       │       └── ApplicationDetailView.vue # 审核详情 + AI 建议
│       └── utils/                        # 工具函数
│           ├── imageUrl.js               # 图片路径解析
│           └── labels.js                 # 品种/状态枚举映射
│
└── src/main/                             # Spring Boot 后端项目
    ├── java/com/petmgt/
    │   ├── PetMgtApplication.java        # 启动类
    │   ├── config/                        # 配置
    │   │   ├── SecurityConfig.java        # Spring Security + JWT 无状态认证
    │   │   ├── MyBatisPlusConfig.java     # 分页插件
    │   │   ├── MvcConfig.java             # /uploads/** 静态资源映射
    │   │   ├── CorsConfig.java            # CORS（允许 localhost:5173）
    │   │   ├── AiConfig.java              # DeepSeek REST 客户端
    │   │   └── ...
    │   ├── controller/                    # REST 控制器
    │   │   ├── AuthController.java        # POST /api/auth/register, /api/auth/login
    │   │   ├── PetController.java         # GET  /api/pets, /api/pets/{id}
    │   │   ├── BreedController.java       # GET  /api/breeds
    │   │   ├── FileController.java        # POST /api/upload
    │   │   ├── HomeController.java        # GET  /api/home
    │   │   ├── admin/                     # 管理员 API（需 ROLE_ADMIN）
    │   │   └── user/                      # 用户 API（需认证）
    │   ├── service/                       # 业务逻辑
    │   ├── entity/                        # 8 个数据实体
    │   ├── dto/                           # 数据传输对象
    │   ├── mapper/                        # MyBatis-Plus Mapper
    │   ├── security/                      # JWT 认证组件
    │   └── util/
    └── resources/
        ├── application.properties         # 主配置
        ├── schema.sql                     # 建表 DDL
        └── data.sql                       # 初始种子数据
```

---

## 快速开始

### 环境要求

| 组件 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

### 1. 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pet_mgt DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 配置环境变量（可选）

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | 数据库密码 | `root` |
| `DEEPSEEK_API_KEY` | DeepSeek API Key（不配则 AI 功能降级） | 空 |

### 3. 启动后端（端口 8080）

```bash
# 开发模式
mvn spring-boot:run

# 或打包运行
mvn package -DskipTests
java -jar target/petmgt-0.0.1-SNAPSHOT.jar
```

首次启动自动执行 `schema.sql` 建表和 `data.sql` 导入初始数据。

### 4. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 **http://localhost:5173**。Vite 开发服务器自动将 `/api` 和 `/uploads` 请求代理到后端 8080 端口。

---

## 测试账号

> 所有账号密码均为 **`123456`**

| 角色 | 用户名 | 权限说明 |
|------|--------|----------|
| 管理员 | `admin` | 后台仪表盘、用户/宠物/品种管理、申请审核、AI 审核辅助 |
| 普通用户 | `user` | 浏览宠物、提交领养申请、个人中心、AI 宠物匹配、AI 养宠问答 |

---

## 功能总览

### 🏠 公共页面（无需登录）

| 页面 | 路由 | 说明 |
|------|------|------|
| 首页 | `/` | 最新待领养宠物卡片展示 |
| 宠物列表 | `/pets` | 多条件筛选（类型/品种/性别/状态）、分页浏览 |
| 宠物详情 | `/pets/:id` | 图片轮播、详细信息、品种简介、领养申请入口 |
| 登录 | `/login` | 登录后获 JWT Token（有效期 2 小时） |
| 注册 | `/register` | 用户名/邮箱唯一校验 |
| 404 | `/:pathMatch(.*)*` | 未匹配路由兜底 |

### 👤 普通用户（需登录）

| 页面 | 路由 | 说明 |
|------|------|------|
| 个人中心 | `/user/profile` | 查看/修改资料（邮箱、头像）、修改密码（需旧密码验证） |
| 提交申请 | `/user/apply/:petId` | 填写联系方式、养宠经验、陪伴时间、申请理由 |
| 我的申请 | `/user/applications` | 审核状态跟踪、取消/删除待审核申请 |
| AI 宠物匹配 | `/user/ai-match` | 描述偏好（居住环境、经验、性格），AI 智能推荐 |
| 匹配结果 | `/user/ai-match/result` | 匹配分数、推荐理由、结果保存 |
| 匹配历史 | `/user/ai-match/history` | 历史匹配记录回顾 |
| AI 养宠问答 | `/user/ai-chat` | 宠物护理问答（限宠物领域，拒绝医疗诊断） |

### 🔧 管理员（需 ADMIN 角色）

| 页面 | 路由 | 说明 |
|------|------|------|
| 控制台 | `/admin` | 统计卡片（用户/宠物/待审核/已通过等数据概览） |
| 用户管理 | `/admin/users` | 用户 CRUD、角色分配、启用/禁用 |
| 品种管理 | `/admin/breeds` | 品种 CRUD（有宠物关联时禁止删除） |
| 宠物管理 | `/admin/pets` | 宠物 CRUD、多图上传、状态变更 |
| 宠物编辑 | `/admin/pets/create` `/admin/pets/:id/edit` | 新增/编辑宠物（公用 PetFormView，含图片增删） |
| 审核列表 | `/admin/applications` | 按状态筛选、分页浏览 |
| 审核详情 | `/admin/applications/:id` | 查看申请信息 + AI 审核辅助建议 + 批准/拒绝操作 |

---

## 前端架构

### 路由导航守卫

`router/index.js` 中的 `beforeEach` 守卫实现三級权限控制：

1. **已登录用户**访问登录/注册页 → 重定向至首页（`meta.guest`）
2. **未登录用户**访问需认证页面 → 重定向至 `/login`（`meta.requiresAuth`）
3. **非管理员**访问管理后台 → 提示"无权限"并重定向至首页（`meta.requiresAdmin`）
4. 刷新页面时自动从 `localStorage` 恢复 Token 并请求 `/api/user/profile` 补全用户信息

### 状态管理 (Pinia)

| Store | 职责 |
|-------|------|
| `useUserStore` | Token 管理（localStorage 持久化）、用户信息、`isLoggedIn` / `isAdmin` 计算属性、登录/注册/登出/更新资料/修改密码 |
| `useAppStore` | 品种列表缓存（按 petType 去重请求） |

### Axios 封装

`api/request.js` 提供统一的 HTTP 客户端：

- **请求拦截器**：自动注入 `Authorization: Bearer <token>`
- **响应拦截器**：解包 `ApiResponse { code, msg, data }`，`code=200` 返回 `data`；`code=401` 自动登出并提示登录过期；`code=403` 弹出权限错误提示；网络异常提示"网络连接失败"
- **静默模式**：请求头 `X-Silent: 1` 时抑制错误弹窗（用于路由守卫中的会话恢复请求）

### 设计系统

基于 Element Plus 的**紫粉色系**自定义主题（`global.css`）：

- 主色 `#7c5cfc`（紫色），辅色 `#f472b6`（粉色）
- 全局背景渐变径向光晕、卡圆角 14px、悬停上浮 + 阴影动画
- 统计卡片渐变顶部色条、毛玻璃导航栏、渐变色品牌 Logo

---

## 后端 API 接口

### 认证方式

除公开接口外，所有 API 需在请求头携带 JWT：
```
Authorization: Bearer <token>
```

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录，返回 JWT + 用户名 + 角色列表 |
| GET | `/api/home` | 首页最新宠物（最多 8 只） |
| GET | `/api/pets` | 宠物分页搜索（name / breedId / petType / gender / status） |
| GET | `/api/pets/{id}` | 宠物详情（含品种名、封面图、图片列表） |
| GET | `/api/pets/{id}/images` | 宠物图片列表 |
| GET | `/api/breeds` | 品种列表（可选 `?petType=猫` 过滤） |

### 用户接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 获取个人资料（id / username / email / avatarUrl / roles） |
| PUT | `/api/user/profile` | 更新个人资料 |
| PUT | `/api/user/password` | 修改密码（需旧密码验证） |
| POST | `/api/applications` | 提交领养申请 |
| GET | `/api/applications` | 我的申请列表（分页） |
| GET | `/api/applications/{id}` | 申请详情 |
| PUT | `/api/applications/{id}/cancel` | 取消申请 |
| DELETE | `/api/applications/{id}` | 删除申请（非已通过状态） |
| POST | `/api/ai-match` | AI 宠物匹配 |
| GET | `/api/ai-match/history` | 匹配历史（分页） |
| DELETE | `/api/ai-match/history/{id}` | 删除匹配记录 |
| POST | `/api/ai-chat` | AI 养宠问答 |
| POST | `/api/upload` | 上传图片（2MB 限制，jpg/jpeg/png，自动生成 300×300 缩略图） |

### 管理员接口（需 ADMIN 角色，前缀 `/api/admin`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/stats` | 统计概览（用户数/宠物数/各类申请数） |
| GET/POST | `/users` | 用户列表 / 创建用户 |
| GET/PUT/DELETE | `/users/{id}` | 用户详情 / 更新 / 删除 |
| GET/POST | `/breeds` | 品种列表 / 创建品种 |
| PUT/DELETE | `/breeds/{id}` | 更新 / 删除品种 |
| GET/POST | `/pets` | 宠物列表 / 创建宠物（multipart，含图片） |
| PUT/DELETE | `/pets/{id}` | 更新 / 删除宠物 |
| PATCH | `/pets/{id}/status` | 修改宠物状态（available / pending / adopted） |
| GET | `/applications` | 申请列表（可按 status 过滤） |
| GET | `/applications/{id}` | 申请详情（含 AI 审核缓存结果） |
| GET | `/applications/{id}/ai-review` | 获取或生成 AI 审核建议 |
| PUT | `/applications/{id}/approve` | 批准（自动拒绝同宠物其他待审申请，标记宠物为已领养） |
| PUT | `/applications/{id}/reject` | 拒绝（若该宠物无其他待审申请则恢复为可领养） |
| DELETE | `/applications/{id}` | 删除申请 |
| GET | `/ai-records` | AI 匹配记录列表 |
| DELETE | `/ai-records/{id}` | 删除 AI 匹配记录 |

---

## 数据库设计

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户 | id, username, password(BCrypt), email, avatar_url, enabled |
| `role` | 角色 | id, role_name (ADMIN/USER) |
| `user_role` | 用户-角色 | user_id, role_id |
| `pet` | 宠物 | id, name, breed_id, gender, age, weight, health_status, personality, adoption_requirement, status |
| `pet_breed` | 品种 | id, breed_name, pet_type(猫/狗/兔), description |
| `pet_image` | 宠物图片 | id, pet_id, image_url, is_cover |
| `adoption_application` | 领养申请 | id, pet_id, user_id, phone, address, experience, accompany_time, reason, status, review_comment |
| `ai_match_record` | AI 匹配记录 | id, user_id, preference_text, result_text, recommended_pet_id, match_score |
| `ai_review_record` | AI 审核缓存 | id, application_id, result_text, score, suggestion |

**宠物状态流转**：`available` → `pending`（有申请待审核）→ `adopted`（通过）/ `available`（拒绝且无其他待审申请）

---

## AI 功能详解

集成 DeepSeek Chat API 提供三项 AI 能力，均具备**降级策略**：

### 1. AI 宠物匹配 (`AiMatchService`)

- **AI 模式**：向 DeepSeek 发送结构化提示词，要求返回 JSON 格式的匹配结果（推荐宠物 ID、匹配分数、理由）
- **规则引擎回退**（API 不可用）：本地评分算法接管 —— 类型匹配 (+30)、性格兼容 (+25)、健康 (+20)、陪伴时间 (+15)、居住环境 (+10)
- 匹配结果持久化到 `ai_match_record` 表

### 2. AI 养宠问答 (`AiChatService`)

- 系统提示词限定**宠物护理领域**，AI 拒绝回答医疗诊断
- API 不可用时返回友好提示

### 3. AI 审核辅助 (`AiReviewService`)

- 自动分析申请者资质，生成评分、优势、风险、建议
- **结果缓存**：首次调用后存入 `ai_review_record`，后续查看直接读缓存，避免重复 API 调用
- AI 仅作参考，最终决策由管理员做出

---

## 安全设计

- **JWT 无状态认证**：HMAC-SHA256 签名，2 小时有效期
- **密码加密**：BCrypt 哈希存储
- **角色权限**：`ROLE_ADMIN` / `ROLE_USER` 两级，管理员端点严格隔离
- **导航守卫**：前端路由层 + 后端 API 层双重鉴权
- **防自我修改**：管理员无法编辑/删除自己的账户
- **CORS**：仅允许前端开发服务器来源 (`localhost:5173`)

---

## 配置参考

完整配置见 `src/main/resources/application.properties`，关键项：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 后端端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/pet_mgt` | 数据库连接 |
| `spring.datasource.password` | `${DB_PASSWORD:root}` | 密码（环境变量优先） |
| `jwt.expiration` | `7200000`（2 小时） | Token 有效期 (ms) |
| `ai.deepseek.endpoint` | `https://api.deepseek.com/v1/chat/completions` | DeepSeek API |
| `ai.deepseek.model` | `deepseek-chat` | 模型 |
| `ai.timeout` | `30000` | AI 调用超时 (ms) |
| `spring.servlet.multipart.max-file-size` | `2MB` | 上传限制 |
| `file.upload.dir` | `uploads/pets` | 上传目录 |
| `file.allowed-extensions` | `jpg,jpeg,png` | 允许的图片格式 |
