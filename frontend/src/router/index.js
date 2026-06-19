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
  const userStore = useUserStore()

  // If has token but no userInfo, try to restore session
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchProfile()
    } catch {
      // 401 interceptor clears token; isLoggedIn stays false → guard redirects to /login
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
