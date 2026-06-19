<template>
  <div class="container">
    <h2 class="page-title">审核详情</h2>
    <div v-if="detail" class="detail-layout">
      <div class="detail-section">
        <h3>申请信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="宠物">{{ detail.pet?.name }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detail.application?.breedName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="申请人邮箱">{{ detail.user?.email }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(detail.application?.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getAppStatusTagType(detail.application?.status)" size="small">
              {{ getAppStatusDisplay(detail.application?.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <h3 style="margin-top:20px">AI 审核建议</h3>
        <div v-if="detail.aiReview" class="ai-review"><pre>{{ JSON.stringify(detail.aiReview, null, 2) }}</pre></div>
        <el-empty v-else description="暂无AI审核建议" :image-size="60" />
        <div v-if="detail.application?.status === 'pending'" class="review-actions">
          <el-button type="success" size="large" @click="handleApprove">通过</el-button>
          <el-button type="danger" size="large" @click="showRejectDialog = true">拒绝</el-button>
          <el-button
            v-if="detail.application?.status !== 'approved'"
            type="danger"
            size="large"
            plain
            @click="handleDelete"
          >
            <el-icon><Delete /></el-icon> 删除记录
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="申请不存在" />
    <el-dialog v-model="showRejectDialog" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, ArrowLeft, Delete } from '@element-plus/icons-vue'
import { getAppDetail, approveApp, rejectApp, deleteApp } from '@/api/admin'
import { getAppStatusDisplay, getAppStatusTagType } from '@/utils/labels'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)

onMounted(async () => { try { detail.value = await getAppDetail(route.params.id) } catch {} })

async function handleApprove() {
  try {
    await ElMessageBox.confirm('确认通过该申请？', '确认', { type: 'warning' })
    await approveApp(route.params.id)
    ElMessage.success('已通过')
    router.push('/admin/applications')
  } catch {}
}

async function handleReject() {
  if (!rejectReason.value.trim()) { ElMessage.error('请输入拒绝原因'); return }
  rejecting.value = true
  try {
    await rejectApp(route.params.id, rejectReason.value)
    ElMessage.success('已拒绝')
    showRejectDialog.value = false
    router.push('/admin/applications')
  } catch {} finally { rejecting.value = false }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(
      '确定要删除此审核记录吗？此操作不可恢复。',
      '确认删除',
      { type: 'warning', confirmButtonText: '确定删除' }
    )
    await deleteApp(route.params.id)
    ElMessage.success('已删除')
    router.push('/admin/applications')
  } catch {}
}

function formatDate(d) { return d ? new Date(d).toLocaleString('zh-CN') : '-' }
</script>

<style scoped>
.detail-section { grid-column: 1 / -1; }
.ai-review { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-top: 12px; }
.ai-review pre { white-space: pre-wrap; font-family: inherit; font-size: 14px; color: #303133; }
.review-actions { margin-top: 24px; display: flex; gap: 12px; }
</style>
