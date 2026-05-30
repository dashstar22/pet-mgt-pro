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
      <Pagination :total="total" :page="page" :size="size" @update:page="(v)=>{page=v;loadData()}" @update:size="(v)=>{size=v;page=1;loadData()}" />
    </div>

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
import { getUserList, createUser, updateUser, deleteUser } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, username: '', email: '', password: '', enabled: 1 })
const rules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }] }

async function loadData() {
  loading.value = true
  try {
    const data = await getUserList({ page: page.value, size: size.value })
    users.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

function resetForm() { form.id = null; form.username = ''; form.email = ''; form.password = ''; form.enabled = 1 }
function showCreateDialog() { isEdit.value = false; resetForm(); dialogVisible.value = true }
function showEditDialog(row) { isEdit.value = true; form.id = row.id; form.username = row.username; form.email = row.email || ''; form.password = ''; form.enabled = row.enabled; dialogVisible.value = true }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { username: form.username, email: form.email, enabled: form.enabled }
    if (form.password) payload.password = form.password
    if (isEdit.value) {
      await updateUser(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      if (!form.password) { ElMessage.error('请输入密码'); saving.value = false; return }
      await createUser(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '确认', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '-' }
onMounted(loadData)
</script>
