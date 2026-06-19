<template>
  <div class="container">
    <h2 class="page-title">我的申请</h2>
    <div class="table-container">
      <el-table :data="applications" stripe v-loading="loading" empty-text="暂无申请记录">
        <el-table-column prop="petName" label="宠物名称" />
        <el-table-column prop="breedName" label="品种" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getAppStatusTagType(row.status)">{{ getAppStatusDisplay(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :size="size"
        @update:page="onPageChange"
        @update:size="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myList, cancel, deleteApp } from '@/api/application'
import { getAppStatusDisplay, getAppStatusTagType } from '@/utils/labels'
import Pagination from '@/components/Pagination.vue'

const applications = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadData() {
  loading.value = true
  try {
    const data = await myList({ page: page.value, size: size.value })
    applications.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function onPageChange(val) { page.value = val; loadData() }
function onSizeChange(val) { size.value = val; page.value = 1; loadData() }

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消该申请吗？', '确认', { type: 'warning' })
    await cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch {
    // cancelled or error
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该申请记录吗？', '确认', { type: 'warning' })
    await deleteApp(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(loadData)
</script>
