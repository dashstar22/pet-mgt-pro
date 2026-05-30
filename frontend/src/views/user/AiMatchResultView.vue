<template>
  <div class="container">
    <h2 class="page-title">匹配结果</h2>
    <div v-if="result" class="match-result">
      <el-card>
        <template #header>AI 推荐</template>
        <div v-if="result.recommendation" class="result-content">
          <pre class="result-text">{{ result.recommendation }}</pre>
        </div>
        <div v-if="result.pets && result.pets.length > 0" class="result-pets">
          <h3>推荐的宠物</h3>
          <div class="pet-grid" style="margin-top:16px">
            <PetCard v-for="pet in result.pets" :key="pet.id" :pet="pet" />
          </div>
        </div>
        <div class="result-actions">
          <el-button @click="$router.push('/pets')">浏览更多宠物</el-button>
          <el-button type="primary" @click="$router.push('/user/ai-match')">重新匹配</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无匹配结果">
      <el-button type="primary" @click="$router.push('/user/ai-match')">去匹配</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PetCard from '@/components/PetCard.vue'

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
.result-text { white-space: pre-wrap; font-family: inherit; font-size: 15px; line-height: 1.8; background: #f5f7fa; padding: 16px; border-radius: 8px; color: #303133; }
.result-actions { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>
