<template>
  <div class="container">
    <h2 class="page-title">📊 管理后台</h2>
    <div v-loading="loading">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🐾</div>
          <div class="stat-value">{{ stats.totalPets }}</div>
          <div class="stat-label">总宠物数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🏠</div>
          <div class="stat-value">{{ stats.availablePets }}</div>
          <div class="stat-label">可领养</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">❤️</div>
          <div class="stat-value">{{ stats.adoptedPets }}</div>
          <div class="stat-label">已领养</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📋</div>
          <div class="stat-value">{{ stats.pendingApplications }}</div>
          <div class="stat-label">待审核申请</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStats } from '@/api/admin'

const loading = ref(true)
const stats = reactive({
  totalUsers: 0, totalPets: 0, availablePets: 0, adoptedPets: 0, pendingApplications: 0,
})

onMounted(async () => {
  try {
    const data = await getStats()
    Object.assign(stats, data)
  } catch {} finally { loading.value = false }
})
</script>
