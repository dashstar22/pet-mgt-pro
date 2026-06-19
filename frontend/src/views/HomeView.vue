<template>
  <div class="container">
    <!-- Hero Section -->
    <div class="home-hero">
      <div class="hero-paws">
        <span class="hero-paw" v-for="i in 10" :key="i">🐾</span>
      </div>
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
  padding: 80px 24px;
  background: linear-gradient(135deg, #7c5cfc0f 0%, #f472b60f 40%, #e879f908 80%, #a78bfa0a 100%);
  border-radius: 24px;
  margin-bottom: 44px;
  position: relative;
  overflow: hidden;
}

/* 光斑 — 左上紫 */
.home-hero::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124,92,252,0.12) 0%, rgba(167,139,250,0.05) 35%, transparent 70%);
  top: -100px;
  right: -80px;
  animation: heroGlow1 6s ease-in-out infinite alternate;
}

/* 光斑 — 右下粉 */
.home-hero::after {
  content: '';
  position: absolute;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(244,114,182,0.1) 0%, rgba(232,121,249,0.04) 40%, transparent 70%);
  bottom: -70px;
  left: -50px;
  animation: heroGlow2 5s ease-in-out infinite alternate;
}

@keyframes heroGlow1 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-20px, 15px) scale(1.08); }
}

@keyframes heroGlow2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(15px, -10px) scale(1.06); }
}

.home-hero h1 {
  font-size: 44px;
  font-weight: 900;
  color: #1e1b4b;
  margin-bottom: 12px;
  letter-spacing: -0.8px;
  position: relative;
  z-index: 1;
}

.home-hero h1::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #7c5cfc, #f472b6);
  margin: 16px auto 0;
}

.home-hero p {
  font-size: 18px;
  color: #6d5d8b;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.home-hero .el-button {
  position: relative;
  z-index: 1;
  font-weight: 600;
  padding: 14px 40px;
  font-size: 16px;
  border-radius: 50px;
  letter-spacing: 0.5px;
}

/* 爪印装饰 */
.hero-paws {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  border-radius: 24px;
}

.hero-paw {
  position: absolute;
  font-size: 36px;
  opacity: 0.12;
  animation: pawFloat 8s ease-in-out infinite;
}

.hero-paw:nth-child(1) { top: 12%; left: 8%;  font-size: 44px; opacity: 0.14; animation-delay: 0s; }
.hero-paw:nth-child(2) { top: 8%;  left: 28%; font-size: 32px; opacity: 0.10; animation-delay: 1.2s; }
.hero-paw:nth-child(3) { top: 20%; left: 55%; font-size: 40px; opacity: 0.11; animation-delay: 2.5s; }
.hero-paw:nth-child(4) { top: 6%;  left: 78%; font-size: 30px; opacity: 0.13; animation-delay: 0.6s; }
.hero-paw:nth-child(5) { top: 50%; left: 5%;  font-size: 34px; opacity: 0.09; animation-delay: 3.2s; }
.hero-paw:nth-child(6) { top: 45%; left: 92%; font-size: 38px; opacity: 0.10; animation-delay: 1.8s; }
.hero-paw:nth-child(7) { top: 65%; left: 18%; font-size: 28px; opacity: 0.08; animation-delay: 4.0s; }
.hero-paw:nth-child(8) { top: 58%; left: 70%; font-size: 36px; opacity: 0.11; animation-delay: 0.3s; }
.hero-paw:nth-child(9) { top: 75%; left: 48%; font-size: 30px; opacity: 0.09; animation-delay: 2.0s; }
.hero-paw:nth-child(10) { top: 35%; left: 40%; font-size: 26px; opacity: 0.07; animation-delay: 5.0s; }

@keyframes pawFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-6px) rotate(3deg); }
  75% { transform: translateY(4px) rotate(-2deg); }
}

.home-section {
  margin-bottom: 44px;
}

.loading-area {
  padding: 40px;
}
</style>
