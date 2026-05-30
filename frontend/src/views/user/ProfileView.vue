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
