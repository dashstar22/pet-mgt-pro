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
      <Pagination :total="total" :page="page" :size="size" @update:page="(v)=>{page=v;loadData()}" @update:size="(v)=>{size=v;page=1;loadData()}" />
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
  try { const data = await getAdminPetList({ page: page.value, size: size.value }); pets.value = data.records || []; total.value = data.total || 0 } catch {} finally { loading.value = false }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定要删除 ${row.name} 吗？`, '确认', { type: 'warning' }); await deletePet(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

onMounted(loadData)
</script>
