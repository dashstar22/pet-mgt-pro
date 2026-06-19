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
  const isAdmin = computed(() => {
    const roles = userInfo.value?.roles ?? []
    return roles.some(r => r === 'ADMIN' || r === 'ROLE_ADMIN')
  })

  // Link token to request interceptor
  setUserStore({ token, logout, isLoggedIn, isAdmin })

  // Actions
  async function login(username, password) {
    const data = await loginApi({ username, password })
    token.value = data.token
    localStorage.setItem('token', data.token)
    // Basic info from login response; full profile (id/email/avatarUrl)
    // is loaded on-demand by AppHeader / ProfileView
    userInfo.value = {
      username: data.username,
      roles: data.roles,
    }
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
      const user = await getProfile({ silent: true })
      if (user) {
        userInfo.value = {
          ...userInfo.value,
          id: user.id,
          username: user.username,
          email: user.email,
          avatarUrl: user.avatarUrl,
          roles: user.roles,
        }
      }
    } catch {
      // Silently fail — the caller decides how to handle it.
      // (router guard checks isLoggedIn; login keeps basic info from login response)
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
