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
            <el-avatar :src="avatarSrc" :size="24" class="header-avatar">
              <el-icon :size="14"><UserFilled /></el-icon>
            </el-avatar>
            <span style="color: #909399; margin-left: 6px;">{{ userStore.userInfo?.username }}</span>
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
import { getImageUrl } from '@/utils/imageUrl'
import { UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const avatarSrc = computed(() => getImageUrl(userStore.userInfo?.avatarUrl))

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

.header-avatar {
  flex-shrink: 0;
}
</style>
