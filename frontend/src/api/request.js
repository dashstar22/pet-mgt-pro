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
    // userStore.token is a Vue ref; access .value for the actual token string
    const tokenVal = userStore?.token?.value ?? userStore?.token
    if (tokenVal) {
      config.headers.Authorization = `Bearer ${tokenVal}`
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

    // Determine if this request was marked silent (suppress toasts, used for
    // session-restore profile fetch in the router guard)
    const isSilent = response.config?.headers?.['X-Silent'] === '1'

    // 401 — Token expired or not logged in
    if (code === 401) {
      if (userStore) userStore.logout()
      if (!isSilent) {
        ElMessage.error(msg || '登录已过期，请重新登录')
      }
      return Promise.reject(new Error(msg))
    }
    // 403 — Access denied
    if (code === 403) {
      if (!isSilent) {
        ElMessage.error(msg || '无权限访问')
      }
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
