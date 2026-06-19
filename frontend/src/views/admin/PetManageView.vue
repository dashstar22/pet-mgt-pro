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
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getPetStatusTagType(row.status)" size="small">
              {{ getPetStatusDisplay(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/pets/${row.id}/edit`)">编辑</el-button>
            <el-dropdown trigger="click" @command="(status) => handleStatusChange(row, status)" style="margin:0 8px">
              <el-button size="small" type="warning">
                改状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="opt in statusOptions"
                    :key="opt.value"
                    :command="opt.value"
                    :disabled="row.status === opt.value"
                  >
                    {{ opt.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" :page="page" :size="size" @update:page="(v)=>{page=v;loadData()}" @update:size="(v)=>{size=v;page=1;loadData()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getAdminPetList, deletePet, updatePetStatus } from '@/api/admin'
import { getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'
import Pagination from '@/components/Pagination.vue'

const pets = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

const statusOptions = [
  { label: '可领养', value: 'available' },
  { label: '已领养', value: 'adopted' },
  { label: '待审核', value: 'pending' },
]

async function loadData() {
  loading.value = true
  try { const data = await getAdminPetList({ page: page.value, size: size.value }); pets.value = data.records || []; total.value = data.total || 0 } catch {} finally { loading.value = false }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定要删除 ${row.name} 吗？`, '确认', { type: 'warning' }); await deletePet(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

async function handleStatusChange(row, newStatus) {
  try {
    const labelMap = { available: '可领养', adopted: '已领养', pending: '待审核' }
    await ElMessageBox.confirm(
      `确定将「${row.name}」的状态从「${getPetStatusDisplay(row.status)}」改为「${labelMap[newStatus]}」吗？`,
      '确认修改状态',
      { type: 'warning', confirmButtonText: '确定修改' }
    )
    await updatePetStatus(row.id, newStatus)
    ElMessage.success('状态已更新')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>
