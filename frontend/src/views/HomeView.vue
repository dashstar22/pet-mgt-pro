<template>
  <div class="container">
    <!-- Hero Section -->
    <div class="home-hero">
      <h1>找到你的毛孩子</h1>
      <p>为流浪动物找到一个温暖的家</p>
      <el-button type="primary" size="large" @click="$router.push('/pets')">浏览待领养宠物</el-button>
    </div>

    <!-- Latest Pets -->
    <div class="home-section">
      <h2 class="page-title">最新发布</h2>
      <div v-if="loading" class="loading-area">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="pets.length > 0" class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
      </div>
      <el-empty v-else description="暂无宠物信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPetList } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'

const pets = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await getPetList({ page: 1, size: 8 })
    pets.value = data.records || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-hero {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #409eff10, #67c23a10);
  border-radius: 12px;
  margin-bottom: 40px;
}

.home-hero h1 {
  font-size: 36px;
  color: #303133;
  margin-bottom: 12px;
}

.home-hero p {
  font-size: 18px;
  color: #909399;
  margin-bottom: 24px;
}

.home-section {
  margin-bottom: 40px;
}

.loading-area {
  padding: 40px;
}
</style>
