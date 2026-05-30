# Stage 3: Vue3 前端构建 — 设计规格

**日期**: 2026-05-30
**状态**: 已批准
**目标**: 从零搭建 Vue 3 + Element Plus 前端，实现全部 18 个路由页面，与后端 REST API + JWT 认证对接

---

## 1. 背景

### 1.1 当前状态（Stage 1 & 2 完成后）

- 后端所有 Controller 已改为 `@RestController`，返回 JSON 统一格式 `{code, msg, data}`
- JWT 无状态认证已实现：登录返回 Token，JwtAuthenticationFilter 拦截验证
- CORS 已配置允许 `http://localhost:5173`
- 公开端点：`GET /api/pets/**`、`GET /api/breeds`、`/api/home`、`/api/auth/*`
- 管理员端点：`/api/admin/**` 需要 `ROLE_ADMIN`
- 其他 API 端点需要认证
- `frontend/` 目录尚不存在

### 1.2 构建目标

- 使用 Vue 3 Composition API + JavaScript 构建
- 实现全部 18 个页面路由
- 对接后端全部 30+ REST API 接口
- JWT Token 管理：登录存储、请求携带、过期处理
- 管理员权限隔离：导航栏动态切换 + 路由守卫拦截

---

## 2. 技术栈

| 层 | 选型 | 版本 | 说明 |
|---|------|------|------|
| 框架 | Vue 3 | 3.5+ | Composition API (`<script setup>`) |
| 构建 | Vite | 6.x | 开发服务器 + 生产打包 |
| 路由 | Vue Router | 4.x | 嵌套路由 + 导航守卫 |
| 状态管理 | Pinia | 2.x | userStore + appStore |
| UI 库 | Element Plus | 2.x | 全量引入，中文语言包 |
| HTTP | Axios | 1.x | 请求/响应双重拦截器 |
| 工具 | @vueuse/core | latest | `useLocalStorage` 等 |
| CSS | Plain CSS | — | scoped styles + Element Plus CSS 变量覆盖 |
| 语言 | JavaScript | ES2022+ | Vite 默认支持 |

---

## 3. 项目结构

```
frontend/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── api/                    # Axios 封装 + API 模块
    │   ├── request.js          # axios 实例 + 请求/响应拦截器
    │   ├── auth.js             # login(), register()
    │   ├── user.js             # getProfile(), updateProfile(), changePassword()
    │   ├── pet.js              # getPetList(), getPetDetail(), getPetImages()
    │   ├── breed.js            # getBreeds(), admin CRUD
    │   ├── application.js      # submit(), myList(), cancel(), delete()
    │   ├── ai.js               # aiMatch(), aiChat(), matchHistory()
    │   ├── upload.js           # uploadFile(formData)
    │   └── admin.js            # stats, user CRUD, pet CRUD, breed CRUD, approve/reject
    ├── components/             # 共享组件
    │   ├── AppHeader.vue       # 顶部导航栏，动态菜单
    │   ├── AppFooter.vue       # 底部版权栏
    │   ├── PetCard.vue         # 宠物卡片（首页/列表复用）
    │   ├── FileUpload.vue      # 文件上传（头像/宠物图片）
    │   └── Pagination.vue      # 分页组件
    ├── views/                  # 页面组件
    │   ├── HomeView.vue
    │   ├── LoginView.vue
    │   ├── RegisterView.vue
    │   ├── PetListView.vue
    │   ├── PetDetailView.vue
    │   ├── NotFoundView.vue    # 404 页面
    │   ├── user/
    │   │   ├── ProfileView.vue
    │   │   ├── MyApplicationsView.vue
    │   │   ├── ApplyFormView.vue
    │   │   ├── AiMatchView.vue
    │   │   ├── AiMatchResultView.vue
    │   │   ├── AiMatchHistoryView.vue
    │   │   └── AiChatView.vue
    │   └── admin/
    │       ├── DashboardView.vue
    │       ├── UserManageView.vue
    │       ├── BreedManageView.vue
    │       ├── PetManageView.vue
    │       ├── PetFormView.vue
    │       ├── ApplicationListView.vue
    │       └── ApplicationDetailView.vue
    ├── router/
    │   └── index.js            # 路由表 + beforeEach 导航守卫
    ├── stores/
    │   ├── user.js             # token, userInfo, roles, isAdmin, login(), logout(), fetchProfile()
    │   └── app.js              # breeds[], fetchBreeds()
    ├── utils/
    │   └── index.js            # 通用工具函数（日期格式化等）
    ├── styles/
    │   └── global.css          # 全局样式 + Element Plus 变量覆盖
    ├── App.vue                 # 根组件：AppHeader + router-view + AppFooter
    └── main.js                 # 入口：挂载 Element Plus + Router + Pinia
```

---

## 4. 路由设计

### 4.1 路由表（18 条）

#### 公开路由（无需登录）

| 路径 | 视图 | 说明 |
|------|------|------|
| `/` | `HomeView` | 首页，展示最新 8 只宠物 |
| `/login` | `LoginView` | 登录页 |
| `/register` | `RegisterView` | 注册页 |
| `/pets` | `PetListView` | 宠物列表，分页 + 品种/性别/年龄筛选 |
| `/pets/:id` | `PetDetailView` | 宠物详情 + 领养申请入口 |

#### 需登录路由

| 路径 | 视图 | 说明 |
|------|------|------|
| `/user/profile` | `ProfileView` | 个人中心，修改邮箱/头像/密码 |
| `/user/applications` | `MyApplicationsView` | 我的领养申请列表 |
| `/user/apply/:petId` | `ApplyFormView` | 提交领养申请表单 |
| `/user/ai-match` | `AiMatchView` | AI 宠物匹配表单 |
| `/user/ai-match/result` | `AiMatchResultView` | 匹配结果展示 |
| `/user/ai-match/history` | `AiMatchHistoryView` | 匹配历史记录 |
| `/user/ai-chat` | `AiChatView` | AI 养宠问答对话 |

#### 管理员路由（meta: { requiresAdmin: true }）

| 路径 | 视图 | 说明 |
|------|------|------|
| `/admin` | `DashboardView` | 后台首页，统计数据卡片 |
| `/admin/users` | `UserManageView` | 用户管理（列表+CRUD） |
| `/admin/breeds` | `BreedManageView` | 品种管理（列表+CRUD） |
| `/admin/pets` | `PetManageView` | 宠物管理（列表+删除） |
| `/admin/pets/create` | `PetFormView` | 发布宠物（表单+图片上传） |
| `/admin/pets/:id/edit` | `PetFormView` | 编辑宠物 |
| `/admin/applications` | `ApplicationListView` | 领养申请审核列表 |
| `/admin/applications/:id` | `ApplicationDetailView` | 审核详情+通过/拒绝 |

### 4.2 导航守卫 (router.beforeEach)

```
router.beforeEach((to, from, next) => {
  1. 恢复登录态：有 token 但 userStore.userInfo 为空
     → 调用 fetchProfile() 从 /api/user/profile 恢复用户信息
     → 失败则清除 token，放行（让后续守卫拦截到 /login）

  2. 需要认证但无 token：to.meta.requiresAuth && !token
     → next('/login')

  3. 需要管理员但非管理员：to.meta.requiresAdmin && !isAdmin
     → next('/') + ElMessage.warning('无权限访问')

  4. 已登录访问登录/注册页：token && (to.path === '/login' || to.path === '/register')
     → next('/')

  5. 其他：next()
})
```

所有需登录路由设置 `meta: { requiresAuth: true }`，管理员路由额外设置 `meta: { requiresAdmin: true }`。

### 4.3 404 通配路由

```js
{ path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
```

---

## 5. 状态管理 (Pinia)

### 5.1 userStore

```js
// stores/user.js
state: {
  token: ''       // JWT Token，持久化到 localStorage
  userInfo: null  // { id, username, email, avatarUrl, roles[] }
}

getters: {
  isLoggedIn: !!token && !!userInfo
  isAdmin: roles.includes('ADMIN')
}

actions: {
  login(username, password)       // POST /api/auth/login → 存储 token + userInfo
  register(form)                  // POST /api/auth/register
  logout()                        // 清 token + userInfo
  fetchProfile()                  // GET /api/user/profile → 更新 userInfo
  updateProfile(data)             // PUT /api/user/profile
  changePassword(oldPwd, newPwd)  // PUT /api/user/password
}
```

Token 使用 `@vueuse/core` 的 `useStorage`（基于 localStorage），页面刷新后自动恢复。

### 5.2 appStore

```js
// stores/app.js
state: {
  breeds: []    // 品种列表缓存
}

actions: {
  fetchBreeds(petType = null)    // GET /api/breeds?petType=...
  // 如果已有缓存且参数匹配则跳过请求
}
```

---

## 6. Axios 封装

### 6.1 拦截器设计

```js
// api/request.js
const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器 — 自动附加 Authorization Header
service.interceptors.request.use(config => {
  const token = userStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 — 统一处理 ApiResponse 解包和错误
service.interceptors.response.use(
  response => {
    const { code, msg, data } = response.data
    if (code === 200) {
      return data   // 自动解包：调用方直接拿到业务 data
    }
    // 401 → Token 过期
    if (code === 401) {
      userStore().logout()
      router.push('/login')
      return Promise.reject(new Error(msg))
    }
    // 403 → 权限不足
    if (code === 403) {
      ElMessage.error(msg || '无权限访问')
      return Promise.reject(new Error(msg))
    }
    // 其他业务错误
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg))
  },
  error => {
    // 网络异常（无响应）
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(`请求错误: ${error.response.status}`)
    }
    return Promise.reject(error)
  }
)
```

### 6.2 API 模块示例

```js
// api/pet.js
import request from './request'

export function getPetList(params) {
  return request.get('/pets', { params })
}

export function getPetDetail(id) {
  return request.get(`/pets/${id}`)
}

export function getPetImages(id) {
  return request.get(`/pets/${id}/images`)
}
```

---

## 7. API 模块接口映射

### 7.1 认证模块 (api/auth.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `login(data)` | POST | `/auth/login` | `{username, password}` → `{token, username, roles}` |
| `register(data)` | POST | `/auth/register` | `RegisterForm` → void |

### 7.2 用户模块 (api/user.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getProfile()` | GET | `/user/profile` | → User 对象 |
| `updateProfile(data)` | PUT | `/user/profile` | `{email, avatarUrl}` → void |
| `changePassword(data)` | PUT | `/user/password` | `{oldPassword, newPassword}` → void |

### 7.3 宠物模块 (api/pet.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getPetList(params)` | GET | `/pets` | `{page, size, ...criteria}` → PageResponse |
| `getPetDetail(id)` | GET | `/pets/{id}` | → Pet 对象 |
| `getPetImages(id)` | GET | `/pets/{id}/images` | → PetImage[] |

### 7.4 品种模块 (api/breed.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getBreeds(petType?)` | GET | `/breeds` | → Breed[] |

### 7.5 领养申请模块 (api/application.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `submit(data)` | POST | `/applications` | ApplicationForm → void |
| `myList(params)` | GET | `/applications` | `{page, size}` → PageResponse |
| `getDetail(id)` | GET | `/applications/{id}` | → Application |
| `cancel(id)` | PUT | `/applications/{id}/cancel` | → void |
| `deleteApplication(id)` | DELETE | `/applications/{id}` | → void |

### 7.6 AI 模块 (api/ai.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `aiMatch(data)` | POST | `/ai-match` | AiMatchRequest → match result |
| `aiChat(question)` | POST | `/ai-chat` | `{question}` → `{answer}` |
| `getMatchHistory(params)` | GET | `/ai-match/history` | `{page, size}` → PageResponse |

### 7.7 文件上传模块 (api/upload.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `uploadFile(formData)` | POST | `/upload` | FormData → `{url}` |

### 7.8 管理模块 (api/admin.js)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getStats()` | GET | `/admin/stats` | → stats 对象 |
| `getUserList(params)` | GET | `/admin/users` | → PageResponse |
| `getUserById(id)` | GET | `/admin/users/{id}` | → User |
| `createUser(data)` | POST | `/admin/users` | → void |
| `updateUser(id, data)` | PUT | `/admin/users/{id}` | → void |
| `deleteUser(id)` | DELETE | `/admin/users/{id}` | → void |
| `getPetList(params)` | GET | `/admin/pets` | → PageResponse |
| `createPet(formData)` | POST | `/admin/pets` | FormData → void |
| `updatePet(id, formData)` | PUT | `/admin/pets/{id}` | FormData → void |
| `deletePet(id)` | DELETE | `/admin/pets/{id}` | → void |
| `getBreedList(params)` | GET | `/admin/breeds` | → PageResponse |
| `createBreed(data)` | POST | `/admin/breeds` | → void |
| `updateBreed(id, data)` | PUT | `/admin/breeds/{id}` | → void |
| `deleteBreed(id)` | DELETE | `/admin/breeds/{id}` | → void |
| `getApplicationList(params)` | GET | `/admin/applications` | → PageResponse |
| `getApplicationDetail(id)` | GET | `/admin/applications/{id}` | → detail |
| `approveApplication(id, comment)` | PUT | `/admin/applications/{id}/approve` | → void |
| `rejectApplication(id, reason)` | PUT | `/admin/applications/{id}/reject` | → void |
| `deleteApplication(id)` | DELETE | `/admin/applications/{id}` | → void |

---

## 8. 组件设计

### 8.1 App.vue（根组件）

```
+-----------------------------+
| AppHeader.vue               |  ← 导航栏，根据 userStore 动态显示菜单
+-----------------------------+
| <router-view>               |  ← 页面内容区
+-----------------------------+
| AppFooter.vue               |  ← 版权信息
+-----------------------------+
```

### 8.2 AppHeader.vue

根据 `userStore.isLoggedIn` 和 `userStore.isAdmin` 动态切换菜单：

- **未登录**：首页 | 宠物列表 | 登录 | 注册
- **已登录（普通用户）**：首页 | 宠物列表 | AI匹配 | 我的申请 | 个人中心 | 退出
- **已登录（管理员）**：上述全部 + 后台管理入口

### 8.3 PetCard.vue

宠物卡片，展示：封面图（或默认占位图）、名称、品种名、性别/年龄标签、状态标签（可领养/已领养）。点击跳转详情页。首页和列表页复用。

### 8.4 FileUpload.vue

封装 Element Plus `el-upload`，绑定 `/api/upload` 接口。上传成功后返回 URL，支持多文件、预览、删除。props: `modelValue` (URL 字符串或数组)。

### 8.5 Pagination.vue

封装 Element Plus `el-pagination`，统一 props: `page`, `size`, `total`。emits: `update:page`, `update:size`。

---

## 9. 样式方案

### 9.1 Element Plus 主题定制

使用 CSS 变量覆盖 Element Plus 默认主题色：

```css
/* styles/global.css */
:root {
  --el-color-primary: #409eff;      /* 主色调 */
  --el-color-success: #67c23a;
  --el-color-warning: #e6a23c;
  --el-color-danger: #f56c6c;
  --el-border-radius-base: 6px;
}
```

### 9.2 页面级样式

每个 `.vue` 文件使用 `<style scoped>` 编写页面/组件特定样式，利用 scoped 天然隔离。

### 9.3 全局布局样式

`global.css` 中定义全局布局类：`.container`（最大宽度 1200px 居中）、`.page-header`、`.page-body` 等。

---

## 10. Vite 配置

```js
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})
```

开发时 Vite 代理 `/api` 和 `/uploads` 到后端 Spring Boot（端口 8080），避免跨域问题。路径别名 `@` 指向 `src/`。

---

## 11. 错误处理

### 11.1 前端错误场景

| 场景 | 处理方式 |
|------|----------|
| Token 过期 | 响应拦截器捕获 401 → clear token → router.push('/login') + 提示 |
| 无权限 | 响应拦截器捕获 403 → ElMessage.error('无权限访问') |
| 业务错误 | 响应拦截器 → ElMessage.error(msg) |
| 网络断开 | 响应拦截器 → ElMessage.error('网络连接失败') |
| 404 页面 | 通配路由 → NotFoundView |
| 表单校验 | Element Plus Form validation rules |

### 11.2 Element Plus 全局配置

```js
// main.js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

app.use(ElementPlus, { locale: zhCn })
```

---

## 12. 验收标准

1. `npm run dev` 在 `localhost:5173` 正常启动，代理后端 API 正常
2. 18 个路由页面全部可访问，页面间导航正常
3. 注册 → 登录 → 获得 Token → 访问受保护页面 → 退出，完整流程通过
4. 管理员权限隔离：普通用户无法访问 `/admin/*` 路由
5. Token 过期后自动跳转登录页
6. 所有 API 调用返回数据正确渲染
7. Element Plus 组件正常工作（表单、表格、分页、弹窗、消息提示等）
8. 文件上传功能正常
9. 404 页面正确显示

---

*文档版本：v1.0 | 日期：2026-05-30*
