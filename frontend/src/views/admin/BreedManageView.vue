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
      <Pagination :total="total" :page="page" :size="size" @update:page="(v)=>{page=v;loadData()}" @update:size="(v)=>{size=v;page=1;loadData()}" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑品种' : '新增品种'" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="品种名称" prop="breedName">
          <el-input v-model="form.breedName" />
        </el-form-item>
        <el-form-item label="宠物类型" prop="petType">
          <el-select v-model="form.petType" placeholder="请选择">
            <el-option label="狗" value="dog" /><el-option label="猫" value="cat" /><el-option label="其他" value="other" />
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
const rules = { breedName: [{ required: true, message: '请输入品种名称', trigger: 'blur' }], petType: [{ required: true, message: '请选择宠物类型', trigger: 'change' }] }

async function loadData() {
  loading.value = true
  try { const data = await getAdminBreedList({ page: page.value, size: size.value }); breeds.value = data.records || []; total.value = data.total || 0 } catch {} finally { loading.value = false }
}

function showCreateDialog() { isEdit.value = false; form.id = null; form.breedName = ''; form.petType = ''; dialogVisible.value = true }
function showEditDialog(row) { isEdit.value = true; form.id = row.id; form.breedName = row.breedName; form.petType = row.petType; dialogVisible.value = true }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { breedName: form.breedName, petType: form.petType }
    if (isEdit.value) { await updateBreed(form.id, payload); ElMessage.success('更新成功') }
    else { await createBreed(payload); ElMessage.success('创建成功') }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定要删除品种 ${row.breedName} 吗？`, '确认', { type: 'warning' }); await deleteBreed(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

onMounted(loadData)
</script>
