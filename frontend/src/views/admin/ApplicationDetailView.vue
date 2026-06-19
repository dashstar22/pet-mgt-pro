<template>
  <div class="container">
    <h2 class="page-title">审核详情</h2>

    <!-- 加载中 -->
    <div v-if="loading" style="text-align:center;padding:60px 0">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p style="color:#909399;margin-top:12px">加载中...</p>
    </div>

    <!-- 详情内容 -->
    <div v-else-if="detail" class="detail-layout">
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

        <!-- AI 审核建议 -->
        <h3 style="margin-top:20px">AI 审核建议</h3>

        <!-- 加载中 -->
        <div v-if="aiReviewLoading" style="text-align:center;padding:24px">
          <el-icon class="is-loading" :size="20"><Loading /></el-icon>
          <span style="color:#909399;margin-left:8px">AI 正在分析...</span>
        </div>

        <!-- 已加载的 AI 审核 -->
        <div v-else-if="aiReview" class="ai-review">
          <div class="ai-review-header">
            <span class="ai-score-label">综合评分</span>
            <el-tag
              :type="aiReview.score >= 80 ? 'success' : aiReview.score >= 60 ? 'warning' : 'danger'"
              size="large"
              class="ai-score-tag"
            >
              {{ aiReview.score }} 分
            </el-tag>
            <el-tag
              :type="aiReview.suggestion === '建议通过' ? 'success' : aiReview.suggestion === '谨慎通过' ? 'warning' : 'danger'"
              size="large"
            >
              {{ aiReview.suggestion }}
            </el-tag>
          </div>
          <div class="ai-review-section">
            <h4>✅ 优势</h4>
            <p>{{ aiReview.strengths || '暂无' }}</p>
          </div>
          <div class="ai-review-section">
            <h4>⚠️ 风险</h4>
            <p>{{ aiReview.risks || '暂无' }}</p>
          </div>
          <div v-if="aiReview.notes" class="ai-review-section">
            <h4>📝 补充说明</h4>
            <p>{{ aiReview.notes }}</p>
          </div>
        </div>

        <!-- 不在审核中的状态 -->
        <el-empty v-else-if="detail.application?.status !== 'pending'" description="该申请已处理完毕" :image-size="60" />

        <!-- 待审核但无法加载（异常情况） -->
        <el-empty v-else description="AI 审核建议暂不可用" :image-size="60" />

        <!-- 待审核操作按钮 -->
        <div v-if="detail.application?.status === 'pending'" class="review-actions">
          <el-button type="success" size="large" @click="handleApprove">通过</el-button>
          <el-button type="danger" size="large" @click="showRejectDialog = true">拒绝</el-button>
        </div>

        <!-- 删除按钮（所有状态均可删除） -->
        <div class="review-actions" style="margin-top:12px">
          <el-button
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

    <!-- 不存在 -->
    <el-empty v-else description="申请不存在" />

    <!-- 拒绝弹窗 -->
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
import { Delete, Loading } from '@element-plus/icons-vue'
import { getAppDetail, getAiReview, approveApp, rejectApp, deleteApp } from '@/api/admin'
import { getAppStatusDisplay, getAppStatusTagType } from '@/utils/labels'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(true)
const aiReview = ref(null)
const aiReviewLoading = ref(false)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)

onMounted(async () => {
  try {
    detail.value = await getAppDetail(route.params.id)
    // 如果有缓存的 AI 审核，直接使用
    if (detail.value?.aiReview) {
      aiReview.value = detail.value.aiReview
    }
    // 如果是待审核且没有缓存，异步请求 AI 审核
    if (detail.value?.application?.status === 'pending' && !detail.value?.aiReview) {
      fetchAiReview()
    }
  } catch {
    ElMessage.error('加载审核详情失败')
  } finally {
    loading.value = false
  }
})

async function fetchAiReview() {
  aiReviewLoading.value = true
  try {
    aiReview.value = await getAiReview(route.params.id)
  } catch {
    // AI 审核加载失败不弹错误，展示为不可用
  } finally {
    aiReviewLoading.value = false
  }
}

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
.detail-section { max-width: 700px; }

.ai-review {
  background: linear-gradient(135deg, #faf8fc, #fdf2f8);
  padding: 24px;
  border-radius: 14px;
  margin-top: 16px;
  border: 1px solid #ede9f6;
}

.ai-review-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.ai-score-label {
  font-size: 14px;
  color: #6d5d8b;
  font-weight: 600;
}

.ai-score-tag {
  font-size: 20px;
  font-weight: 800;
  padding: 4px 16px;
  border-radius: 50px;
}

.ai-review-section {
  margin-bottom: 16px;
  background: #fff;
  padding: 14px 16px;
  border-radius: 10px;
  border-left: 3px solid #ddd6fe;
}

.ai-review-section h4 {
  font-size: 14px;
  margin-bottom: 6px;
  color: #1e1b4b;
  font-weight: 600;
}

.ai-review-section p {
  font-size: 14px;
  color: #475569;
  line-height: 1.65;
  margin: 0;
}

.review-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.review-actions :deep(.el-button--success) {
  border-radius: 10px;
  font-weight: 600;
}

.review-actions :deep(.el-button--danger) {
  border-radius: 10px;
  font-weight: 600;
}
</style>
