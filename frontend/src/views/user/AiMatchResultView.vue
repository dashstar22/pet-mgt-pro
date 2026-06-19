<template>
  <div class="container">
    <h2 class="page-title">匹配结果</h2>
    <div v-if="result && result.results && result.results.length > 0" class="match-result">
      <div class="ai-badge" v-if="result.aiUsed">
        <el-tag type="success" size="large">🤖 AI 智能匹配</el-tag>
      </div>
      <div class="ai-badge" v-else>
        <el-tag type="info" size="large">📋 规则引擎匹配</el-tag>
      </div>
      <h3 style="margin: 20px 0 16px">推荐的宠物</h3>
      <div class="result-list">
        <el-card v-for="pet in result.results" :key="pet.petId" class="result-item">
          <div class="result-item-inner">
            <div class="result-image" @click="$router.push(`/pets/${pet.petId}`)">
              <img v-if="pet.coverImageUrl" :src="getImageUrl(pet.coverImageUrl)" :alt="pet.petName" />
              <div v-else class="result-placeholder">🐾</div>
            </div>
            <div class="result-body">
              <div class="result-header">
                <h3 class="pet-name" @click="$router.push(`/pets/${pet.petId}`)">{{ pet.petName }}</h3>
                <el-tag
                  :type="pet.matchScore >= 80 ? 'danger' : pet.matchScore >= 60 ? 'warning' : 'info'"
                  size="large"
                  class="match-score"
                >
                  {{ pet.matchScore }} 分
                </el-tag>
              </div>
              <p class="reason">{{ pet.reason }}</p>
              <p v-if="pet.notes" class="notes">📝 {{ pet.notes }}</p>
              <div class="result-item-actions">
                <el-button size="small" @click="$router.push(`/pets/${pet.petId}`)">查看详情</el-button>
                <el-button
                  v-if="pet.suggestApply"
                  type="primary"
                  size="small"
                  @click="$router.push(`/user/apply/${pet.petId}`)"
                >
                  申请领养
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      <div class="result-actions">
        <el-button @click="$router.push('/pets')">浏览更多宠物</el-button>
        <el-button type="primary" @click="$router.push('/user/ai-match')">重新匹配</el-button>
      </div>
    </div>
    <el-empty v-else description="暂无匹配结果">
      <el-button type="primary" @click="$router.push('/user/ai-match')">去匹配</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getImageUrl } from '@/utils/imageUrl'

const result = ref(null)

onMounted(() => {
  const stored = sessionStorage.getItem('aiMatchResult')
  if (stored) {
    try { result.value = JSON.parse(stored) } catch { result.value = null }
  }
})
</script>

<style scoped>
.match-result { max-width: 800px; margin: 0 auto; }

.ai-badge {
  text-align: center;
  margin-bottom: 16px;
}

.ai-badge :deep(.el-tag) {
  padding: 8px 20px;
  font-size: 15px;
  border-radius: 50px;
  font-weight: 600;
}

.ai-badge :deep(.el-tag--success) {
  background: linear-gradient(135deg, #ede9f6, #fdf2f8);
  border-color: #c4b5fd;
  color: #7c5cfc;
}

.result-list { display: flex; flex-direction: column; gap: 16px; }

.result-item {
  cursor: default;
  border-radius: 16px;
  border: none;
  box-shadow: 0 1px 3px rgba(124,92,252,0.04), 0 4px 16px rgba(124,92,252,0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124,92,252,0.08), 0 8px 28px rgba(124,92,252,0.12);
}

.result-item-inner { display: flex; gap: 20px; }

.result-image {
  width: 160px; height: 160px; border-radius: 12px; overflow: hidden;
  background: linear-gradient(135deg, #ede9f6, #fce7f3); cursor: pointer;
  flex-shrink: 0; transition: transform 0.3s ease;
}

.result-image:hover { transform: scale(1.03); }

.result-image img { width: 100%; height: 100%; object-fit: cover; }

.result-placeholder {
  display: flex; align-items: center; justify-content: center;
  height: 100%; font-size: 56px; background: linear-gradient(135deg, #ede9f6, #fce7f3);
}

.result-body { flex: 1; display: flex; flex-direction: column; }

.result-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 8px;
  flex-wrap: wrap;
}

.pet-name {
  font-size: 20px; font-weight: 700; cursor: pointer;
  color: #1e1b4b; transition: color 0.2s;
}

.pet-name:hover { color: #7c5cfc; }

.match-score {
  font-size: 16px; font-weight: 800;
  padding: 4px 14px; border-radius: 50px;
}

.match-score.el-tag--danger {
  background: linear-gradient(135deg, #7c5cfc, #a78bfa);
  border-color: transparent; color: #fff;
}

.match-score.el-tag--warning {
  background: linear-gradient(135deg, #f472b6, #e879f9);
  border-color: transparent; color: #fff;
}

.match-score.el-tag--info {
  background: #f5f0ff; border-color: #ddd6fe; color: #7c5cfc;
}

.reason { color: #475569; font-size: 14px; line-height: 1.65; margin-bottom: 8px; }

.notes {
  color: #a78bfa; font-size: 13px; margin-bottom: 12px;
  background: #faf8fc; padding: 6px 12px; border-radius: 8px;
}

.result-item-actions { margin-top: auto; display: flex; gap: 8px; }

.result-actions {
  margin-top: 28px; display: flex; gap: 12px; justify-content: center;
}

.result-actions :deep(.el-button--primary) {
  border-radius: 50px;
  font-weight: 600;
}

@media (max-width: 600px) {
  .result-item-inner { flex-direction: column; }
  .result-image { width: 100%; height: 200px; }
}
</style>
