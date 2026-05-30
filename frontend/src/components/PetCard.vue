<template>
  <div class="pet-card" @click="goDetail">
    <div class="pet-card-image">
      <img v-if="pet.coverImageUrl" :src="'/uploads/' + pet.coverImageUrl" :alt="pet.name" />
      <div v-else class="pet-card-placeholder">🐾</div>
    </div>
    <div class="pet-card-body">
      <h3 class="pet-card-name">{{ pet.name }}</h3>
      <p class="pet-card-breed">{{ pet.breedName || '未知品种' }}</p>
      <div class="pet-card-tags">
        <el-tag size="small">{{ pet.gender }}</el-tag>
        <el-tag size="small" type="info">{{ pet.age }}岁</el-tag>
        <el-tag size="small" :type="pet.status === 'available' ? 'success' : 'warning'">
          {{ pet.status === 'available' ? '可领养' : pet.status }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  pet: { type: Object, required: true },
})

const router = useRouter()

function goDetail() {
  router.push(`/pets/${props.pet.id}`)
}
</script>

<style scoped>
.pet-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.pet-card-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f0f2f5;
}

.pet-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 48px;
}

.pet-card-body {
  padding: 16px;
}

.pet-card-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.pet-card-breed {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.pet-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
