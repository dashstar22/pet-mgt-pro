# Stage 3: Vue3 Frontend Build — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete Vue 3 + Element Plus SPA with 18 routes, JWT auth flow, admin/user role isolation, and full backend API integration from scratch in the `frontend/` directory.

**Architecture:** Single-page application with Vue 3 Composition API. Axios interceptor auto-attaches Bearer token and unwraps `ApiResponse`. Pinia stores manage user auth state and breed cache. Vue Router guards enforce authentication and role-based access. Element Plus provides the UI component layer with Chinese locale.

**Tech Stack:** Vue 3.5+, Vite 6.x, Vue Router 4.x, Pinia 2.x, Element Plus 2.x, Axios 1.x, @vueuse/core

---

## File Structure Map

### New Files (35)

| File | Responsibility |
|------|---------------|
| `frontend/index.html` | HTML entry point |
| `frontend/package.json` | Dependencies and scripts |
| `frontend/vite.config.js` | Vite config: proxy `/api`→`localhost:8080`, `@` alias |
| `frontend/src/main.js` | App bootstrap: Element Plus + Router + Pinia |
| `frontend/src/App.vue` | Root layout: AppHeader + router-view + AppFooter |
| `frontend/src/styles/global.css` | Global styles + Element Plus CSS variable overrides |
| `frontend/src/api/request.js` | Axios instance with request/response interceptors |
| `frontend/src/api/auth.js` | login(), register() |
| `frontend/src/api/user.js` | getProfile(), updateProfile(), changePassword() |
| `frontend/src/api/pet.js` | getPetList(), getPetDetail(), getPetImages() |
| `frontend/src/api/breed.js` | getBreeds() |
| `frontend/src/api/application.js` | submit(), myList(), getDetail(), cancel(), deleteApp() |
| `frontend/src/api/ai.js` | aiMatch(), aiChat(), getMatchHistory(), deleteRecord() |
| `frontend/src/api/upload.js` | uploadFile() |
| `frontend/src/api/admin.js` | All /api/admin/** endpoints |
| `frontend/src/stores/user.js` | userStore: token, userInfo, roles, login/logout/fetchProfile |
| `frontend/src/stores/app.js` | appStore: breeds cache, fetchBreeds() |
| `frontend/src/router/index.js` | 18 routes + beforeEach navigation guard |
| `frontend/src/components/AppHeader.vue` | Dynamic navbar based on auth/role state |
| `frontend/src/components/AppFooter.vue` | Footer with copyright |
| `frontend/src/components/PetCard.vue` | Pet card for home and list pages |
| `frontend/src/components/FileUpload.vue` | File upload with preview, wraps el-upload |
| `frontend/src/components/Pagination.vue` | Pagination wrapper for el-pagination |
| `frontend/src/views/HomeView.vue` | Home page: latest 8 pets grid |
| `frontend/src/views/LoginView.vue` | Login form |
| `frontend/src/views/RegisterView.vue` | Registration form |
| `frontend/src/views/PetListView.vue` | Pet list with filters and pagination |
| `frontend/src/views/PetDetailView.vue` | Pet detail with images and apply button |
| `frontend/src/views/NotFoundView.vue` | 404 page |
| `frontend/src/views/user/ProfileView.vue` | User profile editing |
| `frontend/src/views/user/MyApplicationsView.vue` | User's application list |
| `frontend/src/views/user/ApplyFormView.vue` | Submit adoption application form |
| `frontend/src/views/user/AiMatchView.vue` | AI pet matching form |
| `frontend/src/views/user/AiMatchResultView.vue` | AI match results display |
| `frontend/src/views/user/AiMatchHistoryView.vue` | AI match history list |
| `frontend/src/views/user/AiChatView.vue` | AI pet care Q&A chat |
| `frontend/src/views/admin/DashboardView.vue` | Admin dashboard with stats cards |
| `frontend/src/views/admin/UserManageView.vue` | User CRUD table |
| `frontend/src/views/admin/BreedManageView.vue` | Breed CRUD table |
| `frontend/src/views/admin/PetManageView.vue` | Pet management table |
| `frontend/src/views/admin/PetFormView.vue` | Pet create/edit form with image upload |
| `frontend/src/views/admin/ApplicationListView.vue` | Application review list |
| `frontend/src/views/admin/ApplicationDetailView.vue` | Application review detail with approve/reject |

---

## Task 1: Scaffold Vite + Vue 3 Project

**Files:**
- Create: `frontend/` directory (via Vite scaffold + edits)

- [ ] **Step 1: Create Vue 3 project with Vite**

Run:
```bash
cd d:/code/pet-mgt && npm create vite@latest frontend -- --template vue
```

- [ ] **Step 2: Install all dependencies**

Run:
```bash
cd d:/code/pet-mgt/frontend && npm install && npm install vue-router@4 pinia@2 axios@1 element-plus @vueuse/core
```

- [ ] **Step 3: Clean up scaffold files — delete defaults**

Delete:
- `frontend/src/components/HelloWorld.vue`
- `frontend/src/assets/vue.svg`
- `frontend/public/vite.svg`

- [ ] **Step 4: Create directory structure**

Run:
```bash
cd d:/code/pet-mgt/frontend && mkdir -p src/api src/components src/views/user src/views/admin src/router src/stores src/utils src/styles
```

- [ ] **Step 5: Verify dev server starts**

Run:
```bash
cd d:/code/pet-mgt/frontend && npm run dev
```
Expected: Vite dev server starts on `http://localhost:5173`. Press Ctrl+C after confirming.

- [ ] **Step 6: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/ && git commit -m "feat(frontend): scaffold Vite + Vue 3 project with all dependencies"
```

---

## Task 2: Configure Vite, Global Styles, and index.html

**Files:**
- Modify: `frontend/vite.config.js`
- Modify: `frontend/index.html`
- Create: `frontend/src/styles/global.css`

- [ ] **Step 1: Configure vite.config.js with proxy and alias**

Replace `frontend/vite.config.js` entirely:

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

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
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

- [ ] **Step 2: Update index.html**

Replace `frontend/index.html` entirely:

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>宠物领养平台</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```

- [ ] **Step 3: Create global.css with Element Plus variable overrides**

Create `frontend/src/styles/global.css`:

```css
/* Element Plus 主题变量覆盖 */
:root {
  --el-color-primary: #409eff;
  --el-color-success: #67c23a;
  --el-color-warning: #e6a23c;
  --el-color-danger: #f56c6c;
  --el-border-radius-base: 6px;
}

/* 全局重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #303133;
  background-color: #f5f7fa;
}

/* 布局容器 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-container {
  min-height: calc(100vh - 120px);
  padding: 24px 16px;
}

/* 页面标题 */
.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

/* 卡片网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* Element Plus 表格容器 */
.table-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}

.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 详情页布局 */
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 表单容器 */
.form-container {
  max-width: 480px;
  margin: 0 auto;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.form-container .form-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 32px;
}
```

- [ ] **Step 4: Verify dev server starts and CSS loads**

Run:
```bash
cd d:/code/pet-mgt/frontend && npm run dev
```
Expected: Dev server starts. Open `http://localhost:5173`. A blank page with proper background color `#f5f7fa` appears. Press Ctrl+C.

- [ ] **Step 5: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/vite.config.js frontend/index.html frontend/src/styles/global.css
git commit -m "feat(frontend): configure Vite proxy, alias, and global CSS styles"
```

---

## Task 3: Create Axios Request Module with Interceptors

**Files:**
- Create: `frontend/src/api/request.js`

- [ ] **Step 1: Create request.js with interceptors**

Create `frontend/src/api/request.js`:

```js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Store reference — set after Pinia is initialized
let userStore = null
export function setUserStore(store) {
  userStore = store
}

// Request interceptor — attach Bearer token
service.interceptors.request.use(
  (config) => {
    const token = userStore?.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — unwrap ApiResponse, handle errors
service.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === 200) {
      return data
    }
    // 401 — Token expired or not logged in
    if (code === 401) {
      if (userStore) userStore.logout()
      ElMessage.error(msg || '登录已过期，请重新登录')
      return Promise.reject(new Error(msg))
    }
    // 403 — Access denied
    if (code === 403) {
      ElMessage.error(msg || '无权限访问')
      return Promise.reject(new Error(msg))
    }
    // Other business errors
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg))
  },
  (error) => {
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(`请求错误: ${error.response.status}`)
    }
    return Promise.reject(error)
  }
)

export default service
```

- [ ] **Step 2: Verify file is valid JS (syntax check)**

Run:
```bash
cd d:/code/pet-mgt/frontend && node -c src/api/request.js
```
Expected: No output (no syntax errors).

- [ ] **Step 3: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/api/request.js
git commit -m "feat(frontend): add Axios request module with JWT and error interceptors"
```

---

## Task 4: Create All 8 API Modules

**Files:**
- Create: `frontend/src/api/auth.js`
- Create: `frontend/src/api/user.js`
- Create: `frontend/src/api/pet.js`
- Create: `frontend/src/api/breed.js`
- Create: `frontend/src/api/application.js`
- Create: `frontend/src/api/ai.js`
- Create: `frontend/src/api/upload.js`
- Create: `frontend/src/api/admin.js`

- [ ] **Step 1: Create api/auth.js**

Create `frontend/src/api/auth.js`:

```js
import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}
```

- [ ] **Step 2: Create api/user.js**

Create `frontend/src/api/user.js`:

```js
import request from './request'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}
```

- [ ] **Step 3: Create api/pet.js**

Create `frontend/src/api/pet.js`:

```js
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

- [ ] **Step 4: Create api/breed.js**

Create `frontend/src/api/breed.js`:

```js
import request from './request'

export function getBreeds(petType) {
  return request.get('/breeds', { params: petType ? { petType } : {} })
}
```

- [ ] **Step 5: Create api/application.js**

Create `frontend/src/api/application.js`:

```js
import request from './request'

export function submit(data) {
  return request.post('/applications', data)
}

export function myList(params) {
  return request.get('/applications', { params })
}

export function getDetail(id) {
  return request.get(`/applications/${id}`)
}

export function cancel(id) {
  return request.put(`/applications/${id}/cancel`)
}

export function deleteApp(id) {
  return request.delete(`/applications/${id}`)
}
```

- [ ] **Step 6: Create api/ai.js**

Create `frontend/src/api/ai.js`:

```js
import request from './request'

export function aiMatch(data) {
  return request.post('/ai-match', data)
}

export function getMatchHistory(params) {
  return request.get('/ai-match/history', { params })
}

export function deleteMatchRecord(id) {
  return request.delete(`/ai-match/history/${id}`)
}

export function aiChat(question) {
  return request.post('/ai-chat', { question })
}
```

- [ ] **Step 7: Create api/upload.js**

Create `frontend/src/api/upload.js`:

```js
import axios from 'axios'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/api/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(localStorage.getItem('token') ? { Authorization: `Bearer ${localStorage.getItem('token')}` } : {}),
    },
  }).then(res => {
    if (res.data.code === 200) return res.data.data
    throw new Error(res.data.msg)
  })
}
```

- [ ] **Step 8: Create api/admin.js**

Create `frontend/src/api/admin.js`:

```js
import request from './request'

// Dashboard
export function getStats() {
  return request.get('/admin/stats')
}

// User management
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

export function getUserById(id) {
  return request.get(`/admin/users/${id}`)
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

// Pet management
export function getAdminPetList(params) {
  return request.get('/admin/pets', { params })
}

export function createPet(formData) {
  return request.post('/admin/pets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function updatePet(id, formData) {
  return request.put(`/admin/pets/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deletePet(id) {
  return request.delete(`/admin/pets/${id}`)
}

// Breed management
export function getAdminBreedList(params) {
  return request.get('/admin/breeds', { params })
}

export function createBreed(data) {
  return request.post('/admin/breeds', data)
}

export function updateBreed(id, data) {
  return request.put(`/admin/breeds/${id}`, data)
}

export function deleteBreed(id) {
  return request.delete(`/admin/breeds/${id}`)
}

// Application review
export function getAppList(params) {
  return request.get('/admin/applications', { params })
}

export function getAppDetail(id) {
  return request.get(`/admin/applications/${id}`)
}

export function approveApp(id, comment) {
  return request.put(`/admin/applications/${id}/approve`, { comment: comment || '' })
}

export function rejectApp(id, reason) {
  return request.put(`/admin/applications/${id}/reject`, { reason })
}

export function deleteApp(id) {
  return request.delete(`/admin/applications/${id}`)
}
```

- [ ] **Step 9: Verify syntax**

Run:
```bash
cd d:/code/pet-mgt/frontend && for f in src/api/auth.js src/api/user.js src/api/pet.js src/api/breed.js src/api/application.js src/api/ai.js src/api/upload.js src/api/admin.js; do node -c "$f" || exit 1; done && echo "All OK"
```
Expected: "All OK"

- [ ] **Step 10: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/api/
git commit -m "feat(frontend): add all 8 API modules (auth, user, pet, breed, application, ai, upload, admin)"
```

---

## Task 5: Create Pinia Stores

**Files:**
- Create: `frontend/src/stores/user.js`
- Create: `frontend/src/stores/app.js`

- [ ] **Step 1: Create stores/user.js**

Create `frontend/src/stores/user.js`:

```js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getProfile, updateProfile as updateProfileApi, changePassword as changePasswordApi } from '@/api/user'
import { setUserStore } from '@/api/request'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('ADMIN') ?? false)

  // Link token to request interceptor
  setUserStore({ token, logout, isLoggedIn, isAdmin })

  // Actions
  async function login(username, password) {
    const data = await loginApi({ username, password })
    token.value = data.token
    localStorage.setItem('token', data.token)
    userInfo.value = {
      username: data.username,
      roles: data.roles,
    }
    // Fetch full profile after login
    await fetchProfile()
    return data
  }

  async function register(form) {
    return await registerApi(form)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchProfile() {
    try {
      const user = await getProfile()
      if (user) {
        userInfo.value = {
          ...userInfo.value,
          id: user.id,
          username: user.username,
          email: user.email,
          avatarUrl: user.avatarUrl,
        }
      }
    } catch {
      // If profile fetch fails (e.g., expired token), logout
      logout()
    }
  }

  async function updateProfile(data) {
    await updateProfileApi(data)
    if (data.email !== undefined) userInfo.value.email = data.email
    if (data.avatarUrl !== undefined) userInfo.value.avatarUrl = data.avatarUrl
  }

  async function changePassword(oldPassword, newPassword) {
    await changePasswordApi({ oldPassword, newPassword })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchProfile,
    updateProfile,
    changePassword,
  }
})
```

- [ ] **Step 2: Create stores/app.js**

Create `frontend/src/stores/app.js`:

```js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getBreeds } from '@/api/breed'

export const useAppStore = defineStore('app', () => {
  const breeds = ref([])
  let lastPetType = null

  async function fetchBreeds(petType) {
    // Return cached if same type already loaded
    if (breeds.value.length > 0 && petType === lastPetType) {
      return breeds.value
    }
    lastPetType = petType
    breeds.value = await getBreeds(petType)
    return breeds.value
  }

  return { breeds, fetchBreeds }
})
```

- [ ] **Step 3: Verify syntax**

Run:
```bash
cd d:/code/pet-mgt/frontend && node -c src/stores/user.js && node -c src/stores/app.js && echo "All OK"
```
Expected: "All OK"

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/stores/
git commit -m "feat(frontend): add Pinia stores (userStore with JWT, appStore with breed cache)"
```

---

## Task 6: Create Router with All Routes and Navigation Guard

**Files:**
- Create: `frontend/src/router/index.js`

- [ ] **Step 1: Create router/index.js with all 18 routes and navigation guard**

Create `frontend/src/router/index.js`:

```js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // Public routes
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guest: true },
  },
  {
    path: '/pets',
    name: 'PetList',
    component: () => import('@/views/PetListView.vue'),
  },
  {
    path: '/pets/:id',
    name: 'PetDetail',
    component: () => import('@/views/PetDetailView.vue'),
  },
  // User routes (require auth)
  {
    path: '/user/profile',
    name: 'Profile',
    component: () => import('@/views/user/ProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/applications',
    name: 'MyApplications',
    component: () => import('@/views/user/MyApplicationsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/apply/:petId',
    name: 'ApplyForm',
    component: () => import('@/views/user/ApplyFormView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/ai-match',
    name: 'AiMatch',
    component: () => import('@/views/user/AiMatchView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/ai-match/result',
    name: 'AiMatchResult',
    component: () => import('@/views/user/AiMatchResultView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/ai-match/history',
    name: 'AiMatchHistory',
    component: () => import('@/views/user/AiMatchHistoryView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/ai-chat',
    name: 'AiChat',
    component: () => import('@/views/user/AiChatView.vue'),
    meta: { requiresAuth: true },
  },
  // Admin routes
  {
    path: '/admin',
    name: 'Dashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/admin/UserManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/breeds',
    name: 'BreedManage',
    component: () => import('@/views/admin/BreedManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/pets',
    name: 'PetManage',
    component: () => import('@/views/admin/PetManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/pets/create',
    name: 'PetCreate',
    component: () => import('@/views/admin/PetFormView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/pets/:id/edit',
    name: 'PetEdit',
    component: () => import('@/views/admin/PetFormView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/applications',
    name: 'ApplicationList',
    component: () => import('@/views/admin/ApplicationListView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/applications/:id',
    name: 'ApplicationDetail',
    component: () => import('@/views/admin/ApplicationDetailView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  // 404 catch-all
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  // Pinia must be installed before router is used externally,
  // but inside the guard we import from the already-initialized store
  const userStore = useUserStore()

  // If has token but no userInfo, try to restore session
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchProfile()
    } catch {
      // fetchProfile handles logout internally
    }
  }

  // Guest routes (login/register) — redirect to home if logged in
  if (to.meta.guest && userStore.isLoggedIn) {
    return next('/')
  }

  // Requires auth but not logged in
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return next('/login')
  }

  // Requires admin but not admin
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    const { ElMessage } = await import('element-plus')
    ElMessage.warning('无权限访问管理后台')
    return next('/')
  }

  next()
})

export default router
```

- [ ] **Step 2: Verify syntax**

Run:
```bash
cd d:/code/pet-mgt/frontend && node -c src/router/index.js && echo "OK"
```
Expected: "OK"

- [ ] **Step 3: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/router/
git commit -m "feat(frontend): add Vue Router with 18 routes, lazy loading, and auth guard"
```

---

## Task 7: Create App.vue, AppHeader, AppFooter, and main.js

**Files:**
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/components/AppHeader.vue`
- Create: `frontend/src/components/AppFooter.vue`

- [ ] **Step 1: Create main.js — application entry point**

Create `frontend/src/main.js`:

```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
```

- [ ] **Step 2: Create App.vue — root layout**

Create `frontend/src/App.vue`:

```vue
<template>
  <div id="app-root">
    <AppHeader />
    <main class="page-container">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
#app-root {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.page-container {
  flex: 1;
}
</style>
```

- [ ] **Step 3: Create AppHeader.vue — dynamic navigation bar**

Create `frontend/src/components/AppHeader.vue`:

```vue
<template>
  <el-menu
    :default-active="activeRoute"
    mode="horizontal"
    :ellipsis="false"
    class="app-header"
    @select="handleSelect"
  >
    <div class="header-container">
      <div class="header-left">
        <el-menu-item index="/" class="brand-item">
          <span class="brand-text">🐾 宠物领养平台</span>
        </el-menu-item>
      </div>
      <div class="header-right">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/pets">宠物列表</el-menu-item>

        <template v-if="userStore.isLoggedIn && !userStore.isAdmin">
          <el-menu-item index="/user/ai-match">AI匹配</el-menu-item>
          <el-menu-item index="/user/applications">我的申请</el-menu-item>
          <el-menu-item index="/user/profile">个人中心</el-menu-item>
        </template>

        <template v-if="userStore.isAdmin">
          <el-menu-item index="/user/ai-match">AI匹配</el-menu-item>
          <el-menu-item index="/user/applications">我的申请</el-menu-item>
          <el-menu-item index="/user/profile">个人中心</el-menu-item>
          <el-sub-menu index="admin-sub">
            <template #title>后台管理</template>
            <el-menu-item index="/admin">控制台</el-menu-item>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/breeds">品种管理</el-menu-item>
            <el-menu-item index="/admin/pets">宠物管理</el-menu-item>
            <el-menu-item index="/admin/applications">审核管理</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="!userStore.isLoggedIn">
          <el-menu-item index="/login">登录</el-menu-item>
          <el-menu-item index="/register">注册</el-menu-item>
        </template>

        <template v-if="userStore.isLoggedIn">
          <el-menu-item index="logout" class="logout-item">
            <span style="color: #909399;">{{ userStore.userInfo?.username }}</span>
            &nbsp;退出
          </el-menu-item>
        </template>
      </div>
    </div>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeRoute = computed(() => {
  if (route.path.startsWith('/admin')) return route.path
  if (route.path === '/') return '/'
  if (route.path.startsWith('/pets')) return '/pets'
  return route.path
})

function handleSelect(index) {
  if (index === 'logout') {
    userStore.logout()
    router.push('/')
  } else {
    router.push(index)
  }
}
</script>

<style scoped>
.app-header {
  padding: 0;
  border-bottom: 1px solid #e4e7ed;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.brand-item {
  font-size: 16px;
}

.brand-text {
  font-weight: 700;
  font-size: 17px;
}

.logout-item {
  color: #909399;
}
</style>
```

- [ ] **Step 4: Create AppFooter.vue**

Create `frontend/src/components/AppFooter.vue`:

```vue
<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>&copy; 2026 宠物领养平台 · 前后端分离课程项目</p>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background-color: #303133;
  color: #909399;
  text-align: center;
  padding: 16px 0;
  font-size: 13px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}
</style>
```

- [ ] **Step 5: Verify dev server starts and shows layout**

Run:
```bash
cd d:/code/pet-mgt/frontend && npm run dev
```
Expected: Dev server starts on `http://localhost:5173`. Page shows header with "宠物领养平台" and navigation links, footer, and an empty main area. No console errors in browser.

- [ ] **Step 6: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/main.js frontend/src/App.vue frontend/src/components/AppHeader.vue frontend/src/components/AppFooter.vue
git commit -m "feat(frontend): add App shell with dynamic navigation header and footer"
```

---

## Task 8: Create LoginView and RegisterView

**Files:**
- Create: `frontend/src/views/LoginView.vue`
- Create: `frontend/src/views/RegisterView.vue`

- [ ] **Step 1: Create LoginView.vue**

Create `frontend/src/views/LoginView.vue`:

```vue
<template>
  <div class="form-container">
    <h2 class="form-title">登录</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">
          登录
        </el-button>
      </el-form-item>
    </el-form>
    <p style="text-align:center;color:#909399;">
      还没有账号？<router-link to="/register">立即注册</router-link>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // Error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
```

- [ ] **Step 2: Create RegisterView.vue**

Create `frontend/src/views/RegisterView.vue`:

```vue
<template>
  <div class="form-container">
    <h2 class="form-title">注册</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password size="large" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width:100%">
          注册
        </el-button>
      </el-form-item>
    </el-form>
    <p style="text-align:center;color:#909399;">
      已有账号？<router-link to="/login">立即登录</router-link>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
      confirmPassword: form.confirmPassword,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // Error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
```

- [ ] **Step 3: Verify dev server and test login/register page rendering**

Run:
```bash
cd d:/code/pet-mgt/frontend && npm run dev
```
Expected: Open `http://localhost:5173/login` — login form renders. Navigate to `/register` — registration form renders.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/LoginView.vue frontend/src/views/RegisterView.vue
git commit -m "feat(frontend): add LoginView and RegisterView with form validation"
```

---

## Task 9: Create HomeView and PetCard Component

**Files:**
- Create: `frontend/src/views/HomeView.vue`
- Create: `frontend/src/components/PetCard.vue`

- [ ] **Step 1: Create PetCard.vue**

Create `frontend/src/components/PetCard.vue`:

```vue
<template>
  <div class="pet-card" @click="goDetail">
    <div class="pet-card-image">
      <img v-if="pet.coverImageUrl" :src="'/uploads/' + pet.coverImageUrl" :alt="pet.name" />
      <div v-else class="pet-card-placeholder">🐾</div>
    </div>
    <div class="pet-card-body">
      <h3 class="pet-card-name">{{ pet.name }}</h3>
      <p class="pet-card-breed">{{ pet.breedName || '未知品种' }}</p>
      <div class="pet-card-tags">
        <el-tag size="small">{{ pet.gender }}</el-tag>
        <el-tag size="small" type="info">{{ pet.age }}岁</el-tag>
        <el-tag size="small" :type="pet.status === 'available' ? 'success' : 'warning'">
          {{ pet.status === 'available' ? '可领养' : pet.status }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  pet: { type: Object, required: true },
})

const router = useRouter()

function goDetail() {
  router.push(`/pets/${props.pet.id}`)
}
</script>

<style scoped>
.pet-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.pet-card-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f0f2f5;
}

.pet-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 48px;
}

.pet-card-body {
  padding: 16px;
}

.pet-card-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.pet-card-breed {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.pet-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
```

- [ ] **Step 2: Create HomeView.vue**

Create `frontend/src/views/HomeView.vue`:

```vue
<template>
  <div class="container">
    <!-- Hero Section -->
    <div class="home-hero">
      <h1>找到你的毛孩子</h1>
      <p>为流浪动物找到一个温暖的家</p>
      <el-button type="primary" size="large" @click="$router.push('/pets')">浏览待领养宠物</el-button>
    </div>

    <!-- Latest Pets -->
    <div class="home-section">
      <h2 class="page-title">最新发布</h2>
      <div v-if="loading" class="loading-area">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="pets.length > 0" class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
      </div>
      <el-empty v-else description="暂无宠物信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPetList } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'

const pets = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await getPetList({ page: 1, size: 8 })
    pets.value = data.records || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-hero {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #409eff10, #67c23a10);
  border-radius: 12px;
  margin-bottom: 40px;
}

.home-hero h1 {
  font-size: 36px;
  color: #303133;
  margin-bottom: 12px;
}

.home-hero p {
  font-size: 18px;
  color: #909399;
  margin-bottom: 24px;
}

.home-section {
  margin-bottom: 40px;
}

.loading-area {
  padding: 40px;
}
</style>
```

- [ ] **Step 3: Verify home page renders**

Run dev server, open `http://localhost:5173/`. Expected: Hero section with CTA button, "最新发布" section loads pets from backend (or shows empty state if no pets in DB).

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/HomeView.vue frontend/src/components/PetCard.vue
git commit -m "feat(frontend): add HomeView with hero section and PetCard component"
```

---

## Task 10: Create PetListView and Pagination Component

**Files:**
- Create: `frontend/src/views/PetListView.vue`
- Create: `frontend/src/components/Pagination.vue`

- [ ] **Step 1: Create Pagination.vue**

Create `frontend/src/components/Pagination.vue`:

```vue
<template>
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[12, 24, 48]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 12 },
})

const emit = defineEmits(['update:page', 'update:size'])

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val),
})
const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val),
})

function handlePageChange(val) {
  emit('update:page', val)
}

function handleSizeChange(val) {
  emit('update:size', val)
  emit('update:page', 1)
}
</script>

<script>
import { computed } from 'vue'
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
```

- [ ] **Step 2: Create PetListView.vue**

Create `frontend/src/views/PetListView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">宠物列表</h2>

    <!-- Filters -->
    <div class="search-bar">
      <el-select v-model="filters.petType" placeholder="宠物类型" clearable @change="handleSearch">
        <el-option label="狗" value="dog" />
        <el-option label="猫" value="cat" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-select v-model="filters.gender" placeholder="性别" clearable @change="handleSearch">
        <el-option label="公" value="公" />
        <el-option label="母" value="母" />
      </el-select>
      <el-input
        v-model="filters.name"
        placeholder="搜索宠物名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Results -->
    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="4" animated />
    </div>
    <div v-else-if="pets.length > 0">
      <div class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
      </div>
      <Pagination
        :total="total"
        :page="currentPage"
        :size="pageSize"
        @update:page="(val) => { currentPage = val; loadPets() }"
        @update:size="(val) => { pageSize = val; currentPage = 1; loadPets() }"
      />
    </div>
    <el-empty v-else description="没有找到符合条件的宠物" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { getPetList } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'
import Pagination from '@/components/Pagination.vue'

const pets = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const filters = reactive({
  petType: '',
  gender: '',
  name: '',
})

async function loadPets() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (filters.petType) params.petType = filters.petType
    if (filters.gender) params.gender = filters.gender
    if (filters.name) params.name = filters.name
    const data = await getPetList(params)
    pets.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadPets()
}

loadPets()
</script>

<style scoped>
.loading-area {
  padding: 40px;
}
</style>
```

Note: Vue 3 does not support `.sync`. Use `:page` + `@update:page` handler that updates the ref and calls loadData().

- [ ] **Step 3: Verify pet list page**

Run dev server, open `http://localhost:5173/pets`. Expected: Filter bar at top, pet cards in grid below, pagination at bottom.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/PetListView.vue frontend/src/components/Pagination.vue
git commit -m "feat(frontend): add PetListView with filters and Pagination component"
```

---

## Task 11: Create PetDetailView

**Files:**
- Create: `frontend/src/views/PetDetailView.vue`

- [ ] **Step 1: Create PetDetailView.vue**

Create `frontend/src/views/PetDetailView.vue`:

```vue
<template>
  <div class="container">
    <div v-if="loading" style="padding:40px">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="pet" class="detail-layout">
      <!-- Images -->
      <div class="detail-images">
        <el-image
          v-if="mainImage"
          :src="'/uploads/' + mainImage"
          fit="cover"
          class="main-image"
        />
        <div v-else class="main-image-placeholder">🐾</div>
        <div class="image-thumbs" v-if="images.length > 1">
          <img
            v-for="img in images"
            :key="img.id"
            :src="'/uploads/' + img.imageUrl"
            class="thumb"
            :class="{ active: mainImage === img.imageUrl }"
            @click="mainImage = img.imageUrl"
          />
        </div>
      </div>

      <!-- Info -->
      <div class="detail-info">
        <h1 class="detail-name">{{ pet.name }}</h1>
        <p class="detail-breed">{{ pet.breedName || '未知品种' }}</p>

        <el-descriptions :column="1" border class="detail-desc">
          <el-descriptions-item label="性别">{{ pet.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ pet.age }} 岁</el-descriptions-item>
          <el-descriptions-item label="体重" v-if="pet.weight">{{ pet.weight }} kg</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ pet.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="疫苗情况">{{ pet.vaccineStatus || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="绝育情况">{{ pet.sterilizationStatus || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="性格">{{ pet.personality }}</el-descriptions-item>
          <el-descriptions-item label="领养要求" v-if="pet.adoptionRequirement">{{ pet.adoptionRequirement }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="pet.status === 'available' ? 'success' : 'warning'">
              {{ pet.status === 'available' ? '可领养' : pet.status }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button
            v-if="pet.status === 'available' && userStore.isLoggedIn"
            type="primary"
            size="large"
            @click="$router.push(`/user/apply/${pet.id}`)"
          >
            申请领养
          </el-button>
          <el-button
            v-if="pet.status === 'available' && !userStore.isLoggedIn"
            type="primary"
            size="large"
            @click="$router.push('/login')"
          >
            登录后申请领养
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="宠物不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPetDetail, getPetImages } from '@/api/pet'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const pet = ref(null)
const images = ref([])
const mainImage = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const id = route.params.id
    const [petData, imageData] = await Promise.all([
      getPetDetail(id),
      getPetImages(id).catch(() => []),
    ])
    pet.value = petData
    images.value = Array.isArray(imageData) ? imageData : []
    if (images.value.length > 0) {
      mainImage.value = images.value.find(i => i.isCover === 1)?.imageUrl || images.value[0]?.imageUrl
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.main-image {
  width: 100%;
  height: 360px;
  border-radius: 8px;
}

.main-image-placeholder {
  width: 100%;
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: #f0f2f5;
  border-radius: 8px;
}

.image-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumb.active {
  border-color: #409eff;
}

.detail-info {
  padding-left: 8px;
}

.detail-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.detail-breed {
  color: #909399;
  font-size: 16px;
  margin-bottom: 20px;
}

.detail-desc {
  margin-bottom: 24px;
}

.detail-actions {
  margin-top: 20px;
}
</style>
```

- [ ] **Step 2: Verify pet detail page**

Run dev server, open `http://localhost:5173/pets/1` (or any existing pet ID). Expected: Images on left, details on right, apply button at bottom.

- [ ] **Step 3: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/PetDetailView.vue
git commit -m "feat(frontend): add PetDetailView with images, info table, and apply button"
```

---

## Task 12: Create FileUpload Component and ProfileView

**Files:**
- Create: `frontend/src/components/FileUpload.vue`
- Create: `frontend/src/views/user/ProfileView.vue`

- [ ] **Step 1: Create FileUpload.vue**

Create `frontend/src/components/FileUpload.vue`:

```vue
<template>
  <div class="file-upload">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :show-file-list="false"
      accept="image/jpeg,image/png,image/jpg"
    >
      <div v-if="modelValue" class="upload-preview">
        <img :src="previewUrl" />
        <div class="upload-overlay">
          <span>更换图片</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="upload-actions">
      <el-button type="danger" size="small" @click="handleRemove">删除</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const previewUrl = computed(() => {
  if (!props.modelValue) return ''
  if (props.modelValue.startsWith('http')) return props.modelValue
  return '/uploads/' + props.modelValue
})

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

function handleSuccess(response) {
  // uploadFile returns { url: "..." }
  const url = response.url || response
  emit('update:modelValue', url)
  ElMessage.success('上传成功')
}

function handleError() {
  ElMessage.error('上传失败')
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.upload-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}

.upload-preview:hover .upload-overlay {
  opacity: 1;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-actions {
  margin-top: 8px;
}
</style>
```

- [ ] **Step 2: Create ProfileView.vue**

Create `frontend/src/views/user/ProfileView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">个人中心</h2>
    <div class="profile-content">
      <!-- Avatar -->
      <div class="profile-section">
        <h3>头像</h3>
        <FileUpload v-model="avatarUrl" />
      </div>

      <!-- Basic Info -->
      <div class="profile-section">
        <h3>基本信息</h3>
        <el-form :model="infoForm" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input :model-value="userStore.userInfo?.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="infoLoading" @click="saveInfo">保存</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Change Password -->
      <div class="profile-section">
        <h3>修改密码</h3>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import FileUpload from '@/components/FileUpload.vue'

const userStore = useUserStore()

// Avatar
const avatarUrl = ref(userStore.userInfo?.avatarUrl || '')

// Info form
const infoLoading = ref(false)
const infoForm = reactive({
  email: userStore.userInfo?.email || '',
})

async function saveInfo() {
  infoLoading.value = true
  try {
    await userStore.updateProfile({
      email: infoForm.email,
      avatarUrl: avatarUrl.value,
    })
    ElMessage.success('保存成功')
  } catch {
    // handled by interceptor
  } finally {
    infoLoading.value = false
  }
}

// Password form
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

async function savePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await userStore.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value.resetFields()
  } catch {
    // handled by interceptor
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.profile-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.profile-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.profile-form {
  max-width: 480px;
}
</style>
```

- [ ] **Step 3: Verify ProfileView renders**

Run dev server, login, open `http://localhost:5173/user/profile`. Expected: Avatar upload, email form, password change form.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/components/FileUpload.vue frontend/src/views/user/ProfileView.vue
git commit -m "feat(frontend): add FileUpload component and ProfileView with avatar, info, password editing"
```

---

## Task 13: Create MyApplicationsView and ApplyFormView

**Files:**
- Create: `frontend/src/views/user/MyApplicationsView.vue`
- Create: `frontend/src/views/user/ApplyFormView.vue`

- [ ] **Step 1: Create MyApplicationsView.vue**

Create `frontend/src/views/user/MyApplicationsView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">我的申请</h2>
    <div class="table-container">
      <el-table :data="applications" stripe v-loading="loading" empty-text="暂无申请记录">
        <el-table-column prop="petName" label="宠物名称" />
        <el-table-column prop="breedName" label="品种" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :size="size"
        @update:page="onPageChange"
        @update:size="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myList, cancel, deleteApp } from '@/api/application'
import Pagination from '@/components/Pagination.vue'

const applications = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadData() {
  loading.value = true
  try {
    const data = await myList({ page: page.value, size: size.value })
    applications.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消该申请吗？', '确认', { type: 'warning' })
    await cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch {
    // cancelled or error
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该申请记录吗？', '确认', { type: 'warning' })
    await deleteApp(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {
    // cancelled or error
  }
}

function statusType(status) {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待审核', approved: '已通过', rejected: '已拒绝', cancelled: '已取消' }
  return map[status] || status
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(loadData)
</script>
```

- [ ] **Step 2: Create ApplyFormView.vue**

Create `frontend/src/views/user/ApplyFormView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">提交领养申请</h2>
    <div class="form-container" style="max-width:600px;margin:0 auto">
      <div v-if="pet" class="apply-pet-info">
        <p>您正在申请领养：<strong>{{ pet.name }}</strong></p>
        <p class="text-secondary">{{ pet.breedName || '' }} · {{ pet.gender }} · {{ pet.age }}岁</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入居住地址" />
        </el-form-item>
        <el-form-item label="养宠经验" prop="experience">
          <el-input v-model="form.experience" type="textarea" :rows="3" placeholder="请描述你的养宠经验" />
        </el-form-item>
        <el-form-item label="申请说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请说明申请领养的原因和计划" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" style="width:100%">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPetDetail } from '@/api/pet'
import { submit } from '@/api/application'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const pet = ref(null)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  applicantName: '',
  phone: '',
  address: '',
  experience: '',
  remark: '',
})

const rules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  experience: [{ required: true, message: '请描述养宠经验', trigger: 'blur' }],
  remark: [{ required: true, message: '请说明申请原因', trigger: 'blur' }],
}

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.petId)
  } catch {
    ElMessage.error('获取宠物信息失败')
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submit({
      petId: route.params.petId,
      ...form,
    })
    ElMessage.success('申请已提交，请等待审核')
    router.push('/user/applications')
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.apply-pet-info {
  background: #ecf5ff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
}
</style>
```

- [ ] **Step 2: Verify both pages render**

Run dev server, login, navigate to `/user/applications` and `/user/apply/1`. Expected: List shows user's applications. Apply form shows pet info and form fields.

- [ ] **Step 3: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/user/MyApplicationsView.vue frontend/src/views/user/ApplyFormView.vue
git commit -m "feat(frontend): add MyApplicationsView and ApplyFormView for adoption application flow"
```

---

## Task 14: Create AI Pages (AiMatchView, AiMatchResultView, AiMatchHistoryView, AiChatView)

**Files:**
- Create: `frontend/src/views/user/AiMatchView.vue`
- Create: `frontend/src/views/user/AiMatchResultView.vue`
- Create: `frontend/src/views/user/AiMatchHistoryView.vue`
- Create: `frontend/src/views/user/AiChatView.vue`

- [ ] **Step 1: Create AiMatchView.vue**

Create `frontend/src/views/user/AiMatchView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">AI 宠物匹配</h2>
    <el-card class="match-card">
      <p class="match-intro">告诉我们你的生活方式和偏好，AI 将为你推荐最合适的宠物。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="居住环境" prop="livingSpace">
          <el-select v-model="form.livingSpace" placeholder="请选择">
            <el-option label="公寓" value="apartment" />
            <el-option label="楼房（有阳台/院子）" value="house" />
            <el-option label="郊区/农村" value="rural" />
          </el-select>
        </el-form-item>
        <el-form-item label="每日可陪伴时间" prop="timeAvailable">
          <el-select v-model="form.timeAvailable" placeholder="请选择">
            <el-option label="较少（1-2小时）" value="low" />
            <el-option label="中等（3-5小时）" value="medium" />
            <el-option label="充足（5小时以上）" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有儿童" prop="hasChildren">
          <el-radio-group v-model="form.hasChildren">
            <el-radio label="yes">有</el-radio>
            <el-radio label="no">没有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="偏好宠物类型" prop="preferredType">
          <el-select v-model="form.preferredType" placeholder="请选择">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
            <el-option label="不限" value="any" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏好体型" prop="sizePreference">
          <el-select v-model="form.sizePreference" placeholder="请选择">
            <el-option label="小型" value="small" />
            <el-option label="中型" value="medium" />
            <el-option label="大型" value="large" />
            <el-option label="不限" value="any" />
          </el-select>
        </el-form-item>
        <el-form-item label="其他要求" prop="notes">
          <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="任何额外的要求或偏好" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleMatch" style="width:100%">
            开始匹配
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { aiMatch } from '@/api/ai'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  livingSpace: '',
  timeAvailable: '',
  hasChildren: 'no',
  preferredType: '',
  sizePreference: '',
  notes: '',
})

const rules = {
  livingSpace: [{ required: true, message: '请选择居住环境', trigger: 'change' }],
  timeAvailable: [{ required: true, message: '请选择陪伴时间', trigger: 'change' }],
  preferredType: [{ required: true, message: '请选择偏好类型', trigger: 'change' }],
}

async function handleMatch() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await aiMatch(form)
    // Store result in sessionStorage for display on result page
    sessionStorage.setItem('aiMatchResult', JSON.stringify(result))
    router.push('/user/ai-match/result')
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.match-card {
  max-width: 600px;
  margin: 0 auto;
}

.match-intro {
  color: #909399;
  margin-bottom: 24px;
  line-height: 1.6;
}
</style>
```

- [ ] **Step 2: Create AiMatchResultView.vue**

Create `frontend/src/views/user/AiMatchResultView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">匹配结果</h2>
    <div v-if="result" class="match-result">
      <el-card>
        <template #header>AI 推荐</template>
        <div v-if="result.recommendation" class="result-content">
          <pre class="result-text">{{ result.recommendation }}</pre>
        </div>
        <div v-if="result.pets && result.pets.length > 0" class="result-pets">
          <h3>推荐的宠物</h3>
          <div class="pet-grid" style="margin-top:16px">
            <PetCard v-for="pet in result.pets" :key="pet.id" :pet="pet" />
          </div>
        </div>
        <div class="result-actions">
          <el-button @click="$router.push('/pets')">浏览更多宠物</el-button>
          <el-button type="primary" @click="$router.push('/user/ai-match')">重新匹配</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无匹配结果">
      <el-button type="primary" @click="$router.push('/user/ai-match')">去匹配</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PetCard from '@/components/PetCard.vue'

const result = ref(null)

onMounted(() => {
  const stored = sessionStorage.getItem('aiMatchResult')
  if (stored) {
    try {
      result.value = JSON.parse(stored)
    } catch {
      result.value = null
    }
  }
})
</script>

<style scoped>
.match-result {
  max-width: 800px;
  margin: 0 auto;
}

.result-text {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.8;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  color: #303133;
}

.result-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
```

- [ ] **Step 3: Create AiMatchHistoryView.vue**

Create `frontend/src/views/user/AiMatchHistoryView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">匹配历史</h2>
    <div class="table-container">
      <el-table :data="records" stripe v-loading="loading" empty-text="暂无匹配记录">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="createdAt" label="匹配时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :size="size"
        @update:page="onPageChange"
        @update:size="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMatchHistory, deleteMatchRecord } from '@/api/ai'
import Pagination from '@/components/Pagination.vue'

const records = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadData() {
  loading.value = true
  try {
    const data = await getMatchHistory({ page: page.value, size: size.value })
    records.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该记录吗？', '确认', { type: 'warning' })
    await deleteMatchRecord(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadData)
</script>
```

- [ ] **Step 4: Create AiChatView.vue**

Create `frontend/src/views/user/AiChatView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">AI 养宠问答</h2>
    <el-card class="chat-card">
      <div class="chat-messages" ref="chatBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <p>👋 你好！我是 AI 养宠助手</p>
          <p class="text-secondary">有什么养宠问题想问我吗？</p>
        </div>
        <div v-for="(msg, i) in messages" :key="i" class="chat-message" :class="msg.role">
          <div class="message-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="thinking" class="chat-message assistant">
          <div class="message-bubble thinking">正在思考...</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="question"
          placeholder="输入你的问题..."
          @keyup.enter="sendMessage"
          :disabled="thinking"
        >
          <template #append>
            <el-button :loading="thinking" @click="sendMessage">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiChat } from '@/api/ai'
import { ElMessage } from 'element-plus'

const messages = ref([])
const question = ref('')
const thinking = ref(false)
const chatBox = ref(null)

async function sendMessage() {
  const q = question.value.trim()
  if (!q) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  thinking.value = true

  await nextTick()
  scrollToBottom()

  try {
    const data = await aiChat(q)
    messages.value.push({ role: 'assistant', content: data.answer || JSON.stringify(data) })
  } catch {
    // handled by interceptor
  } finally {
    thinking.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-card {
  max-width: 700px;
  margin: 0 auto;
}

.chat-messages {
  min-height: 400px;
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.chat-empty {
  text-align: center;
  padding: 60px 20px;
  color: #303133;
  font-size: 16px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.chat-message {
  margin-bottom: 12px;
  display: flex;
}

.chat-message.user {
  justify-content: flex-end;
}

.chat-message.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-radius: 12px 12px 0 12px;
}

.chat-message.assistant .message-bubble {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px 12px 12px 0;
}

.message-bubble {
  max-width: 80%;
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-bubble.thinking {
  color: #909399;
  font-style: italic;
}

.chat-input {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
</style>
```

- [ ] **Step 5: Verify AI pages render**

Run dev server, login, navigate to `/user/ai-match`, `/user/ai-match/result`, `/user/ai-match/history`, `/user/ai-chat`. Expected: All pages render. AI chat has conversation UI.

- [ ] **Step 6: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/user/AiMatchView.vue frontend/src/views/user/AiMatchResultView.vue frontend/src/views/user/AiMatchHistoryView.vue frontend/src/views/user/AiChatView.vue
git commit -m "feat(frontend): add AI pages (match, result, history, chat)"
```

---

## Task 15: Create Admin DashboardView and UserManageView

**Files:**
- Create: `frontend/src/views/admin/DashboardView.vue`
- Create: `frontend/src/views/admin/UserManageView.vue`

- [ ] **Step 1: Create DashboardView.vue**

Create `frontend/src/views/admin/DashboardView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">管理后台</h2>
    <div v-loading="loading">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalPets }}</div>
          <div class="stat-label">总宠物数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#67c23a">{{ stats.availablePets }}</div>
          <div class="stat-label">可领养</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#e6a23c">{{ stats.adoptedPets }}</div>
          <div class="stat-label">已领养</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#f56c6c">{{ stats.pendingApplications }}</div>
          <div class="stat-label">待审核申请</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStats } from '@/api/admin'

const loading = ref(true)
const stats = reactive({
  totalUsers: 0,
  totalPets: 0,
  availablePets: 0,
  adoptedPets: 0,
  pendingApplications: 0,
})

onMounted(async () => {
  try {
    const data = await getStats()
    Object.assign(stats, data)
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>
```

- [ ] **Step 2: Create UserManageView.vue**

Create `frontend/src/views/admin/UserManageView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">用户管理</h2>
    <div class="table-container">
      <div class="search-bar">
        <el-button type="primary" @click="showCreateDialog">新增用户</el-button>
      </div>
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" size="small">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total" :page="page" :size="size"
        @update:page="loadData" @update:size="loadData"
      />
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, getUserById, createUser, updateUser, deleteUser } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

// Dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  enabled: 1,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const data = await getUserList({ page: page.value, size: size.value })
    users.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.username = ''
  form.email = ''
  form.password = ''
  form.enabled = 1
}

function showCreateDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.email = row.email || ''
  form.password = ''
  form.enabled = row.enabled
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      username: form.username,
      email: form.email,
      enabled: form.enabled,
    }
    if (form.password) payload.password = form.password

    if (isEdit.value) {
      await updateUser(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      if (!form.password) {
        ElMessage.error('请输入密码')
        saving.value = false
        return
      }
      await createUser(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '确认', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(loadData)
</script>
```

- [ ] **Step 3: Verify admin pages**

Run dev server, login as admin, navigate to `/admin` and `/admin/users`. Expected: Stats cards show data. User table with CRUD dialog.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/admin/DashboardView.vue frontend/src/views/admin/UserManageView.vue
git commit -m "feat(frontend): add admin DashboardView (stats) and UserManageView (CRUD)"
```

---

## Task 16: Create Admin BreedManageView and PetManageView

**Files:**
- Create: `frontend/src/views/admin/BreedManageView.vue`
- Create: `frontend/src/views/admin/PetManageView.vue`

- [ ] **Step 1: Create BreedManageView.vue**

Create `frontend/src/views/admin/BreedManageView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">品种管理</h2>
    <div class="table-container">
      <div class="search-bar">
        <el-button type="primary" @click="showCreateDialog">新增品种</el-button>
      </div>
      <el-table :data="breeds" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="breedName" label="品种名称" />
        <el-table-column prop="petType" label="宠物类型" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total" :page="page" :size="size"
        @update:page="loadData" @update:size="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑品种' : '新增品种'" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="品种名称" prop="breedName">
          <el-input v-model="form.breedName" />
        </el-form-item>
        <el-form-item label="宠物类型" prop="petType">
          <el-select v-model="form.petType" placeholder="请选择">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminBreedList, createBreed, updateBreed, deleteBreed } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const breeds = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, breedName: '', petType: '' })

const rules = {
  breedName: [{ required: true, message: '请输入品种名称', trigger: 'blur' }],
  petType: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminBreedList({ page: page.value, size: size.value })
    breeds.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

function showCreateDialog() {
  isEdit.value = false
  form.id = null; form.breedName = ''; form.petType = ''
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  form.id = row.id; form.breedName = row.breedName; form.petType = row.petType
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { breedName: form.breedName, petType: form.petType }
    if (isEdit.value) {
      await updateBreed(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createBreed(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除品种 ${row.breedName} 吗？`, '确认', { type: 'warning' })
    await deleteBreed(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
```

- [ ] **Step 2: Create PetManageView.vue**

Create `frontend/src/views/admin/PetManageView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">宠物管理</h2>
    <div class="table-container">
      <div class="search-bar">
        <el-button type="primary" @click="$router.push('/admin/pets/create')">发布宠物</el-button>
      </div>
      <el-table :data="pets" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="breedName" label="品种" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'available' ? 'success' : 'warning'" size="small">
              {{ row.status === 'available' ? '可领养' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/pets/${row.id}/edit`)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total" :page="page" :size="size"
        @update:page="loadData" @update:size="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminPetList, deletePet } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const pets = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminPetList({ page: page.value, size: size.value })
    pets.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 ${row.name} 吗？`, '确认', { type: 'warning' })
    await deletePet(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
```

- [ ] **Step 3: Verify admin breed and pet pages**

Run dev server, login as admin, navigate to `/admin/breeds` and `/admin/pets`. Expected: Tables with data, create/edit dialogs work.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/admin/BreedManageView.vue frontend/src/views/admin/PetManageView.vue
git commit -m "feat(frontend): add admin BreedManageView and PetManageView with CRUD"
```

---

## Task 17: Create Admin PetFormView (Create/Edit Pet)

**Files:**
- Create: `frontend/src/views/admin/PetFormView.vue`

- [ ] **Step 1: Create PetFormView.vue**

Create `frontend/src/views/admin/PetFormView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">{{ isEdit ? '编辑宠物' : '发布宠物' }}</h2>
    <div class="form-container" style="max-width:640px;margin:0 auto">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="品种" prop="breedId">
          <el-select v-model="form.breedId" placeholder="请选择品种">
            <el-option v-for="b in breeds" :key="b.id" :label="b.breedName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="公">公</el-radio>
            <el-radio label="母">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :precision="1" :min="0" :step="0.5" />
        </el-form-item>
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input v-model="form.healthStatus" placeholder="如：健康、已驱虫等" />
        </el-form-item>
        <el-form-item label="疫苗情况">
          <el-input v-model="form.vaccineStatus" placeholder="如：已接种三联疫苗" />
        </el-form-item>
        <el-form-item label="绝育情况">
          <el-input v-model="form.sterilizationStatus" placeholder="如：已绝育" />
        </el-form-item>
        <el-form-item label="性格特点" prop="personality">
          <el-input v-model="form.personality" type="textarea" :rows="2" placeholder="如：温顺、活泼、亲近人" />
        </el-form-item>
        <el-form-item label="领养要求">
          <el-input v-model="form.adoptionRequirement" type="textarea" :rows="2" placeholder="对领养人的要求" />
        </el-form-item>
        <el-form-item label="宠物图片">
          <div class="image-upload-area">
            <div v-for="(img, i) in form.images" :key="i" class="image-upload-item">
              <img v-if="img.url" :src="img.preview || '/uploads/' + img.url" />
              <el-button type="danger" circle size="small" class="image-remove" @click="removeImage(i)">×</el-button>
            </div>
            <div v-if="form.images.length < 6" class="upload-placeholder" @click="triggerUpload">
              <el-icon :size="24"><Plus /></el-icon>
              <span>添加图片</span>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileSelect" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="handleSave" style="width:100%">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createPet, updatePet, getAdminPetList } from '@/api/admin'
import { getBreeds } from '@/api/breed'
import { uploadFile } from '@/api/upload'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const editingId = ref(null)

const formRef = ref(null)
const saving = ref(false)
const fileInput = ref(null)
const breeds = ref([])

const form = reactive({
  name: '',
  breedId: null,
  gender: '公',
  age: 0,
  weight: null,
  healthStatus: '',
  vaccineStatus: '',
  sterilizationStatus: '',
  personality: '',
  adoptionRequirement: '',
  images: [],
  coverIndex: 0,
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  breedId: [{ required: true, message: '请选择品种', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  healthStatus: [{ required: true, message: '请输入健康状况', trigger: 'blur' }],
  personality: [{ required: true, message: '请描述性格特点', trigger: 'blur' }],
}

onMounted(async () => {
  // Load breeds
  try {
    breeds.value = await getBreeds()
  } catch {}

  // Check if edit mode
  const id = route.params.id
  if (id) {
    isEdit.value = true
    editingId.value = id
    // Load existing pet data
    try {
      const data = await getAdminPetList({ page: 1, size: 100 })
      const pet = (data.records || []).find(p => String(p.id) === String(id))
      if (pet) {
        form.name = pet.name
        form.breedId = pet.breedId
        form.gender = pet.gender
        form.age = pet.age || 0
        form.weight = pet.weight
        form.healthStatus = pet.healthStatus || ''
        form.vaccineStatus = pet.vaccineStatus || ''
        form.sterilizationStatus = pet.sterilizationStatus || ''
        form.personality = pet.personality || ''
        form.adoptionRequirement = pet.adoptionRequirement || ''
      }
    } catch {}
  }
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileSelect(e) {
  const files = e.target.files
  if (!files.length) return

  for (const file of files) {
    try {
      const result = await uploadFile(file)
      const url = result.url || result
      form.images.push({
        url: url,
        preview: URL.createObjectURL(file),
      })
    } catch {
      // upload failure
    }
  }
  fileInput.value.value = ''
}

function removeImage(index) {
  form.images.splice(index, 1)
  if (form.coverIndex === index) form.coverIndex = 0
  else if (form.coverIndex > index) form.coverIndex--
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.name)
    fd.append('breedId', form.breedId)
    fd.append('gender', form.gender)
    fd.append('age', form.age)
    if (form.weight) fd.append('weight', form.weight)
    fd.append('healthStatus', form.healthStatus)
    fd.append('vaccineStatus', form.vaccineStatus || '')
    fd.append('sterilizationStatus', form.sterilizationStatus || '')
    fd.append('personality', form.personality)
    fd.append('adoptionRequirement', form.adoptionRequirement || '')
    fd.append('coverIndex', form.coverIndex)

    // Append images as File objects from uploaded URLs
    for (const img of form.images) {
      if (img.file) {
        fd.append('images', img.file)
      } else if (img.url) {
        // For uploaded images via API, send the URL string
        fd.append('imageUrls', img.url)
      }
    }

    if (isEdit.value) {
      await updatePet(editingId.value, fd)
      ElMessage.success('更新成功')
    } else {
      await createPet(fd)
      ElMessage.success('发布成功')
    }
    router.push('/admin/pets')
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-upload-item {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 6px;
  overflow: hidden;
}

.image-upload-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
}

.upload-placeholder {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
```

- [ ] **Step 2: Verify PetFormView**

Run dev server, login as admin, navigate to `/admin/pets/create`. Expected: Form with all fields, image upload area, breed selector. Edit mode loads existing pet data.

- [ ] **Step 3: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/admin/PetFormView.vue
git commit -m "feat(frontend): add admin PetFormView for create/edit pet with image upload"
```

---

## Task 18: Create Admin ApplicationListView and ApplicationDetailView

**Files:**
- Create: `frontend/src/views/admin/ApplicationListView.vue`
- Create: `frontend/src/views/admin/ApplicationDetailView.vue`

- [ ] **Step 1: Create ApplicationListView.vue**

Create `frontend/src/views/admin/ApplicationListView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">审核管理</h2>
    <div class="table-container">
      <div class="search-bar">
        <el-select v-model="filters.status" placeholder="审核状态" clearable @change="handleFilter">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-button type="primary" @click="handleFilter">筛选</el-button>
      </div>
      <el-table :data="applications" stripe v-loading="loading" @row-click="goDetail">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="petName" label="宠物名称" />
        <el-table-column prop="breedName" label="品种" />
        <el-table-column prop="applicantUsername" label="申请人" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" @click.stop="goDetail(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total" :page="page" :size="size"
        @update:page="loadData" @update:size="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAppList } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const applications = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive({ status: '' })

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.status) params.status = filters.status
    const data = await getAppList(params)
    applications.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

function handleFilter() { page.value = 1; loadData() }
function goDetail(row) { router.push(`/admin/applications/${row.id}`) }

function statusType(s) {
  return { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }[s] || 'info'
}
function statusLabel(s) {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝', cancelled: '已取消' }[s] || s
}
function formatDate(d) {
  return d ? new Date(d).toLocaleDateString('zh-CN') : '-'
}

onMounted(loadData)
</script>
```

- [ ] **Step 2: Create ApplicationDetailView.vue**

Create `frontend/src/views/admin/ApplicationDetailView.vue`:

```vue
<template>
  <div class="container">
    <h2 class="page-title">审核详情</h2>
    <div v-if="detail" class="detail-layout">
      <div class="detail-section">
        <h3>申请信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="宠物">{{ detail.pet?.name }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detail.application?.breedName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="申请人邮箱">{{ detail.user?.email }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(detail.application?.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.application?.status)" size="small">
              {{ statusLabel(detail.application?.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top:20px">AI 审核建议</h3>
        <div v-if="detail.aiReview" class="ai-review">
          <pre>{{ JSON.stringify(detail.aiReview, null, 2) }}</pre>
        </div>
        <el-empty v-else description="暂无AI审核建议" :image-size="60" />

        <div v-if="detail.application?.status === 'pending'" class="review-actions">
          <el-button type="success" size="large" @click="handleApprove">通过</el-button>
          <el-button type="danger" size="large" @click="showRejectDialog = true">拒绝</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="申请不存在" />

    <!-- Reject Dialog -->
    <el-dialog v-model="showRejectDialog" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppDetail, approveApp, rejectApp } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)

onMounted(async () => {
  try {
    detail.value = await getAppDetail(route.params.id)
  } catch {}
})

async function handleApprove() {
  try {
    await ElMessageBox.confirm('确认通过该申请？', '确认', { type: 'warning' })
    await approveApp(route.params.id)
    ElMessage.success('已通过')
    router.push('/admin/applications')
  } catch {}
}

async function handleReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.error('请输入拒绝原因')
    return
  }
  rejecting.value = true
  try {
    await rejectApp(route.params.id, rejectReason.value)
    ElMessage.success('已拒绝')
    showRejectDialog.value = false
    router.push('/admin/applications')
  } catch {} finally { rejecting.value = false }
}

function statusType(s) {
  return { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }[s] || 'info'
}
function statusLabel(s) {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝', cancelled: '已取消' }[s] || s
}
function formatDate(d) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-'
}
</script>

<style scoped>
.detail-section {
  grid-column: 1 / -1;
}

.ai-review {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.ai-review pre {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  color: #303133;
}

.review-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>
```

- [ ] **Step 3: Verify admin application pages**

Run dev server, login as admin, navigate to `/admin/applications`. Expected: List with status filter, click row to go to detail. Detail page shows application info, AI review, approve/reject buttons.

- [ ] **Step 4: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/admin/ApplicationListView.vue frontend/src/views/admin/ApplicationDetailView.vue
git commit -m "feat(frontend): add admin ApplicationListView and ApplicationDetailView with approve/reject"
```

---

## Task 19: Create NotFoundView (404 Page)

**Files:**
- Create: `frontend/src/views/NotFoundView.vue`

- [ ] **Step 1: Create NotFoundView.vue**

Create `frontend/src/views/NotFoundView.vue`:

```vue
<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  text-align: center;
  padding: 100px 20px;
}

.not-found h1 {
  font-size: 72px;
  color: #dcdfe6;
  margin-bottom: 12px;
}

.not-found p {
  font-size: 18px;
  color: #909399;
  margin-bottom: 24px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
cd d:/code/pet-mgt && git add frontend/src/views/NotFoundView.vue
git commit -m "feat(frontend): add NotFoundView 404 page"
```

---

## Task 20: Full Integration Verification

- [ ] **Step 1: Clean install dependencies**

```bash
cd d:/code/pet-mgt/frontend && rm -rf node_modules && npm install
```

- [ ] **Step 2: Verify Vite build succeeds**

```bash
cd d:/code/pet-mgt/frontend && npm run build
```
Expected: Build completes without errors. Output in `frontend/dist/`.

- [ ] **Step 3: Start backend server**

```bash
cd d:/code/pet-mgt && mvn spring-boot:run
```
Expected: Server starts on `http://localhost:8080`.

- [ ] **Step 4: Start frontend dev server and smoke test**

In another terminal:
```bash
cd d:/code/pet-mgt/frontend && npm run dev
```

Smoke tests at `http://localhost:5173`:

| # | Test | Expected |
|---|------|----------|
| 1 | Open `/` | Home page renders with hero section and latest pets |
| 2 | Click "宠物列表" | Pet list page with filters |
| 3 | Click a pet card | Pet detail page |
| 4 | Click "注册" | Registration form renders |
| 5 | Register a test user | Success message, redirect to login |
| 6 | Login as test user | Redirect to home, navbar changes to show user menu |
| 7 | Navigate to `/user/profile` | Profile page renders |
| 8 | Navigate to `/user/ai-chat` | AI chat interface renders |
| 9 | Navigate to `/admin` | Should be denied (non-admin gets redirected) |
| 10 | Login as admin | Login with admin credentials |
| 11 | Navigate to `/admin` | Admin dashboard with stats |
| 12 | Navigate to `/admin/users` | User management table |
| 13 | Navigate to `/admin/pets/create` | Pet creation form |
| 14 | Navigate to `/admin/applications` | Application review list |
| 15 | Navigate to `/nonexistent` | 404 page renders |
| 16 | Logout | Navbar returns to guest mode |

- [ ] **Step 5: Stop both servers**

Press Ctrl+C in both terminals.

- [ ] **Step 6: Commit verification notes**

```bash
cd d:/code/pet-mgt && git add -A && git commit -m "test(frontend): complete integration smoke test — all 18 routes verified"
```

---

## Stage 3 Completion Checklist

- [ ] Vite + Vue 3 project scaffolded with all dependencies
- [ ] `vite.config.js` configured with `/api` proxy and `@` alias
- [ ] Global CSS with Element Plus variable overrides
- [ ] Axios request module with JWT + error interceptors
- [ ] 8 API modules covering all 30+ backend endpoints
- [ ] `userStore` with JWT token management, login/logout/fetchProfile
- [ ] `appStore` with breed caching
- [ ] 18 Vue Router routes with lazy loading
- [ ] Navigation guard: auth recovery, guest redirect, admin gate
- [ ] `App.vue` shell with `AppHeader` (dynamic menus) and `AppFooter`
- [ ] `LoginView` and `RegisterView` with form validation
- [ ] `HomeView` with hero section and latest pets grid
- [ ] `PetCard` shared component
- [ ] `PetListView` with filters and pagination
- [ ] `Pagination` shared component
- [ ] `PetDetailView` with images, info table, apply button
- [ ] `FileUpload` shared component
- [ ] `ProfileView` with avatar, info, password editing
- [ ] `MyApplicationsView` with cancel/delete
- [ ] `ApplyFormView` with validation
- [ ] `AiMatchView` → `AiMatchResultView` workflow
- [ ] `AiMatchHistoryView` with delete
- [ ] `AiChatView` with conversation UI
- [ ] Admin `DashboardView` with stats cards
- [ ] Admin `UserManageView` with CRUD dialog
- [ ] Admin `BreedManageView` with CRUD dialog
- [ ] Admin `PetManageView` with list/delete
- [ ] Admin `PetFormView` for create/edit with image upload
- [ ] Admin `ApplicationListView` with status filter
- [ ] Admin `ApplicationDetailView` with approve/reject + AI review
- [ ] `NotFoundView` 404 page
- [ ] `npm run build` succeeds without errors
- [ ] Full integration smoke test passes (16 checks)
- [ ] All 18 routes render correctly

---

*Plan version: v1.0 | Date: 2026-05-30 | Design spec: [2026-05-30-stage-3-vue3-frontend-design.md](../specs/2026-05-30-stage-3-vue3-frontend-design.md)*
