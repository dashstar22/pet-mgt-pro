<template>
  <div class="container">
    <h2 class="page-title">个人中心</h2>
    <div class="profile-content">
      <!-- Avatar -->
      <div class="profile-section">
        <h3>头像</h3>
        <div class="avatar-area">
          <el-avatar :src="avatarPreviewUrl" :size="80" class="profile-avatar">
            <el-icon :size="40"><UserFilled /></el-icon>
          </el-avatar>
          <div class="avatar-upload">
            <FileUpload v-model="avatarUrl" />
          </div>
        </div>
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
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import FileUpload from '@/components/FileUpload.vue'
import { getImageUrl } from '@/utils/imageUrl'
import { UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()

// Load full profile (id, email, avatarUrl) when visiting this page
onMounted(async () => {
  await userStore.fetchProfile()
  avatarUrl.value = userStore.userInfo?.avatarUrl || ''
  infoForm.email = userStore.userInfo?.email || ''
})

// Avatar
const avatarUrl = ref(userStore.userInfo?.avatarUrl || '')
const avatarPreviewUrl = computed(() => getImageUrl(avatarUrl.value))

// Auto-save avatar to backend immediately on change (upload / remove)
watch(avatarUrl, async (newUrl) => {
  if (newUrl === (userStore.userInfo?.avatarUrl || '')) return
  try {
    await userStore.updateProfile({ avatarUrl: newUrl })
    ElMessage.success(newUrl ? '头像已更新' : '头像已移除')
  } catch {
    // handled by interceptor
  }
})

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
  max-width: 620px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-section {
  background: #fff;
  padding: 28px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(124,92,252,0.04), 0 4px 16px rgba(124,92,252,0.06);
}

.profile-section h3 {
  margin-bottom: 20px;
  font-size: 17px;
  font-weight: 700;
  padding-bottom: 14px;
  border-bottom: 2px solid #ede9f6;
  color: #1e1b4b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-section h3::before {
  content: '';
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: linear-gradient(180deg, #7c5cfc, #f472b6);
}

.avatar-area {
  display: flex;
  align-items: center;
  gap: 28px;
}

.profile-avatar {
  flex-shrink: 0;
  border: 3px solid #ddd6fe;
  transition: border-color 0.3s;
}

.profile-avatar:hover {
  border-color: #7c5cfc;
}

.avatar-upload {
  flex: 1;
}

.profile-form {
  max-width: 480px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4c1d95;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.profile-form :deep(.el-button--primary) {
  border-radius: 10px;
  font-weight: 600;
}
</style>
