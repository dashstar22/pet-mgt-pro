<template>
  <div class="container">
    <h2 class="page-title">审核管理</h2>
    <div class="table-container">
      <div class="search-bar">
        <el-select v-model="filters.status" placeholder="审核状态" clearable @change="handleFilter">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-button type="primary" @click="handleFilter">筛选</el-button>
      </div>
      <el-table :data="applications" stripe v-loading="loading" @row-click="goDetail">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="petName" label="宠物名称" />
        <el-table-column prop="breedName" label="品种" />
        <el-table-column prop="applicantUsername" label="申请人" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" @click.stop="goDetail(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" :page="page" :size="size" @update:page="(v)=>{page=v;loadData()}" @update:size="(v)=>{size=v;page=1;loadData()}" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAppList } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const applications = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive({ status: '' })

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.status) params.status = filters.status
    const data = await getAppList(params)
    applications.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

function handleFilter() { page.value = 1; loadData() }
function goDetail(row) { router.push(`/admin/applications/${row.id}`) }
function statusType(s) { const m = { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }; return m[s] || 'info' }
function statusLabel(s) { const m = { pending: '待审核', approved: '已通过', rejected: '已拒绝', cancelled: '已取消' }; return m[s] || s }
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '-' }

onMounted(loadData)
</script>
