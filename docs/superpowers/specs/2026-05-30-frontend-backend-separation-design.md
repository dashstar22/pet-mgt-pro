# 宠物领养平台 — 前后端分离改造设计规格

**日期**: 2026-05-30
**状态**: 已批准
**目标**: 将现有 Spring Boot + Thymeleaf 单体应用改造为 Spring Boot REST API + Vue3 前后端分离架构

---

## 1. 背景与目标

### 1.1 现有系统

基于 Spring Boot 的宠物领养管理平台，采用 Thymeleaf 服务端渲染，功能包括：用户认证、宠物浏览、品种管理、领养申请审核、AI 宠物匹配、AI 养宠问答。

技术栈：Spring Boot 3.5.6 + MyBatis-Plus 3.5.9 + Thymeleaf + Bootstrap 5 + MySQL + Spring Security (Session/Form Login)

### 1.2 改造目标

- 后端改造为纯 REST API，去掉所有 Thymeleaf 模板渲染
- 新增 JWT 无状态认证，替换 Session 登录
- 前端使用 Vue 3 + Element Plus 全新构建
- 保持所有现有功能完整迁移

---

## 2. 架构设计

### 2.1 总体架构

```
┌─────────────────────────────────────────────┐
│  浏览器 (Vue3 SPA)                          │
│  localhost:5173 (开发) / Nginx (部署)       │
├─────────────────────────────────────────────┤
│  HTTP REST API (JSON)                       │
│  Authorization: Bearer <jwt_token>          │
├─────────────────────────────────────────────┤
│  Spring Boot REST API                       │
│  localhost:8080/api/*                       │
│  - JWT Filter                               │
│  - CORS Config                              │
│  - Controllers (@RestController)            │
│  - Services                                  │
│  - MyBatis-Plus Mappers                     │
├─────────────────────────────────────────────┤
│  MySQL 8.0 (pet_mgt)                        │
└─────────────────────────────────────────────┘
```

### 2.2 项目目录结构

```
pet-mgt/
├── src/                          # 后端 Spring Boot
│   └── main/java/com/petmgt/
│       ├── config/               # SecurityConfig改造, JwtConfig, CorsConfig
│       ├── security/             # JwtTokenProvider, JwtAuthenticationFilter
│       ├── controller/           # @Controller → @RestController
│       │   ├── AuthController.java
│       │   ├── PetController.java
│       │   ├── BreedController.java
│       │   ├── FileController.java
│       │   ├── user/
│       │   │   ├── ProfileController.java
│       │   │   ├── ApplicationController.java
│       │   │   ├── AiMatchController.java
│       │   │   └── AiChatController.java
│       │   └── admin/
│       │       ├── AdminController.java
│       │       ├── UserController.java
│       │       ├── PetManageController.java
│       │       ├── BreedController.java
│       │       ├── ApplicationController.java
│       │       └── AiRecordController.java
│       ├── dto/                  # 保留并增强
│       ├── entity/               # 保留
│       ├── service/              # 保留，微调
│       ├── mapper/               # 保留
│       ├── exception/            # 保留
│       └── handler/              # 改造为JSON响应
├── frontend/                     # 前端 Vue3 (新建)
│   ├── src/
│   │   ├── api/                  # Axios封装 + 各模块API
│   │   ├── router/               # 路由 + 导航守卫
│   │   ├── stores/               # Pinia stores
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 通用组件
│   │   ├── utils/                # 工具函数
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── 用户需求.md
│   ├── 技术方案设计.md
│   └── 开发周期.md
└── pom.xml
```

---

## 3. 后端 API 设计

### 3.1 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

分页响应：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 12
  }
}
```

### 3.2 接口清单

#### 一、用户认证类 (AuthController)

| 序号 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 1 | POST | /api/auth/register | 用户注册 | 公开 |
| 2 | POST | /api/auth/login | 用户登录，返回JWT | 公开 |
| 3 | GET | /api/user/profile | 获取当前用户信息 | 登录 |
| 4 | PUT | /api/user/profile | 更新个人信息/头像 | 登录 |
| 5 | PUT | /api/user/password | 修改密码 | 登录 |

#### 二、宠物信息类 (PetController, BreedController, FileController)

| 序号 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 6 | GET | /api/pets | 宠物列表(分页+筛选) | 公开 |
| 7 | GET | /api/pets/{id} | 宠物详情 | 公开 |
| 8 | POST | /api/pets | 发布宠物 | 管理员 |
| 9 | PUT | /api/pets/{id} | 编辑宠物 | 管理员 |
| 10 | DELETE | /api/pets/{id} | 删除宠物 | 管理员 |
| 11 | GET | /api/breeds | 品种列表 | 公开 |
| 12 | POST | /api/breeds | 新增品种 | 管理员 |
| 13 | PUT | /api/breeds/{id} | 编辑品种 | 管理员 |
| 14 | DELETE | /api/breeds/{id} | 删除品种 | 管理员 |
| 15 | POST | /api/upload | 上传文件 | 登录 |

#### 三、领养申请类 (ApplicationController)

| 序号 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 16 | POST | /api/applications | 提交领养申请 | 登录 |
| 17 | GET | /api/applications | 我的申请列表 | 登录 |
| 18 | GET | /api/applications/{id} | 申请详情 | 登录 |
| 19 | PUT | /api/applications/{id}/cancel | 取消申请 | 登录 |
| 20 | DELETE | /api/applications/{id} | 删除申请记录 | 登录 |
| 21 | GET | /api/admin/applications | 审核列表 | 管理员 |
| 22 | GET | /api/admin/applications/{id} | 审核详情 | 管理员 |
| 23 | PUT | /api/admin/applications/{id}/approve | 通过申请 | 管理员 |
| 24 | PUT | /api/admin/applications/{id}/reject | 拒绝申请 | 管理员 |

#### 四、AI 功能类 (AiMatchController, AiChatController)

| 序号 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 25 | POST | /api/ai-match | AI 宠物匹配 | 登录 |
| 26 | GET | /api/ai-match/history | 匹配历史 | 登录 |
| 27 | POST | /api/ai-chat | AI 养宠问答 | 登录 |

#### 额外管理接口

| 序号 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 28 | GET | /api/admin/stats | 后台统计数据 | 管理员 |
| 29 | GET | /api/admin/users | 用户管理列表 | 管理员 |
| 30 | GET | /api/admin/users/{id} | 用户详情 | 管理员 |
| 31 | POST | /api/admin/users | 新增用户 | 管理员 |
| 32 | PUT | /api/admin/users/{id} | 编辑用户 | 管理员 |
| 33 | DELETE | /api/admin/users/{id} | 删除用户 | 管理员 |

### 3.3 安全改造

- **JWT 认证流程**：登录成功返回 `{ token: "xxx" }`，前端存储到 localStorage
- **JWT 过滤器**：`OncePerRequestFilter`，从 Authorization 头提取 Token，校验后设置 SecurityContext
- **CORS 配置**：`WebMvcConfigurer.addCorsMappings`，允许 `http://localhost:5173` 的跨域请求
- **权限控制**：方法级别 `@PreAuthorize` 注解

---

## 4. 前端设计

### 4.1 技术栈

| 层 | 选型 | 版本 |
|----|------|------|
| 框架 | Vue 3 | 3.4+ |
| 构建 | Vite | 5.x |
| 路由 | Vue Router | 4.x |
| 状态管理 | Pinia | 2.x |
| UI库 | Element Plus | 2.x |
| HTTP | Axios | 1.x |
| 工具 | @vueuse/core | latest |

### 4.2 路由表

| 路径 | 页面 | 权限 | 说明 |
|------|------|------|------|
| `/` | HomeView | 公开 | 首页，展示最新宠物 |
| `/login` | LoginView | 公开 | 登录 |
| `/register` | RegisterView | 公开 | 注册 |
| `/pets` | PetListView | 公开 | 宠物列表(筛选+分页) |
| `/pets/:id` | PetDetailView | 公开 | 宠物详情 |
| `/user/profile` | ProfileView | 登录 | 个人中心 |
| `/user/applications` | MyApplicationsView | 登录 | 我的申请 |
| `/user/apply/:petId` | ApplyFormView | 登录 | 提交申请 |
| `/user/ai-match` | AiMatchView | 登录 | AI宠物匹配 |
| `/user/ai-match/result` | AiMatchResultView | 登录 | 匹配结果 |
| `/user/ai-match/history` | AiMatchHistoryView | 登录 | 匹配历史 |
| `/user/ai-chat` | AiChatView | 登录 | AI养宠问答 |
| `/admin` | DashboardView | 管理员 | 后台首页 |
| `/admin/users` | UserManageView | 管理员 | 用户管理 |
| `/admin/breeds` | BreedManageView | 管理员 | 品种管理 |
| `/admin/pets` | PetManageView | 管理员 | 宠物管理 |
| `/admin/pets/create` | PetFormView | 管理员 | 发布宠物 |
| `/admin/pets/:id/edit` | PetFormView | 管理员 | 编辑宠物 |
| `/admin/applications` | ApplicationListView | 管理员 | 审核列表 |
| `/admin/applications/:id` | ApplicationDetailView | 管理员 | 审核详情 |

### 4.3 组件树

```
App.vue
├── AppHeader.vue          (导航栏，根据登录状态和角色切换菜单)
├── <router-view>
│   ├── HomeView.vue
│   │   └── PetCard.vue
│   ├── PetListView.vue
│   │   ├── PetCard.vue
│   │   └── Pagination.vue
│   ├── PetDetailView.vue
│   ├── LoginView.vue / RegisterView.vue
│   ├── user/ProfileView.vue
│   │   └── FileUpload.vue
│   ├── user/AiMatchView.vue
│   ├── user/AiMatchResultView.vue
│   ├── user/AiMatchHistoryView.vue
│   ├── user/AiChatView.vue
│   ├── user/MyApplicationsView.vue
│   ├── user/ApplyFormView.vue
│   ├── admin/DashboardView.vue
│   ├── admin/UserManageView.vue
│   ├── admin/BreedManageView.vue
│   ├── admin/PetManageView.vue
│   ├── admin/PetFormView.vue
│   │   └── FileUpload.vue
│   ├── admin/ApplicationListView.vue
│   └── admin/ApplicationDetailView.vue
└── AppFooter.vue
```

### 4.4 状态管理

- **userStore**：token, userInfo, isLoggedIn, isAdmin, login(), logout(), fetchProfile()
- **appStore**：breeds (缓存品种列表), fetchBreeds()

### 4.5 Axios 封装

- 请求拦截器：附加 `Authorization: Bearer <token>`
- 响应拦截器：统一处理 code !== 200 的错误情况
- 401 → 清除 token 并跳转登录页

---

## 5. 错误处理

### 5.1 后端统一异常处理

`GlobalExceptionHandler` 使用 `@RestControllerAdvice`，所有异常返回 JSON：
- `BusinessException` → 400 + 业务错误消息
- `AccessDeniedException` → 403
- 其他异常 → 500 + 通用错误消息

### 5.2 前端错误处理

- 网络错误：Element Plus `ElMessage.error` 提示
- 表单校验：Element Plus Form 校验规则
- 404 页面：Vue Router 配置 catch-all 路由

---

## 6. 验收标准

1. 所有 30+ REST API 接口通过 Postman 测试
2. Vue3 前端实现全部 18 个路由页面
3. 前后端联调通过，所有功能正常
4. JWT 认证流程完整：注册→登录→访问受保护接口→Token过期处理
5. 管理员权限隔离有效：普通用户无法访问管理后台
6. 三份文档完整：
   - `docs/用户需求.md`
   - `docs/技术方案设计.md`
   - `docs/开发周期.md`
