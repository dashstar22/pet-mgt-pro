<template>
  <div class="container">
    <h2 class="page-title">匹配历史</h2>
    <div class="table-container">
      <el-table :data="records" stripe v-loading="loading" empty-text="暂无匹配记录">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="createdAt" label="匹配时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :size="size"
        @update:page="(v) => { page = v; loadData() }"
        @update:size="(v) => { size = v; page = 1; loadData() }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMatchHistory, deleteMatchRecord } from '@/api/ai'
import Pagination from '@/components/Pagination.vue'

const records = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadData() {
  loading.value = true
  try {
    const data = await getMatchHistory({ page: page.value, size: size.value })
    records.value = data.records || []
    total.value = data.total || 0
  } catch {} finally { loading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该记录吗？', '确认', { type: 'warning' })
    await deleteMatchRecord(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadData)
</script>
