<template>
  <div class="pet-card" @click="goDetail">
    <div class="pet-card-image">
      <img v-if="pet.coverImageUrl" :src="getImageUrl(pet.coverImageUrl)" :alt="pet.name" />
      <div v-else class="pet-card-placeholder">🐾</div>
    </div>
    <div class="pet-card-body">
      <h3 class="pet-card-name">{{ pet.name }}</h3>
      <p class="pet-card-breed">{{ getBreedDisplay(pet.breedName) }}</p>
      <div class="pet-card-tags">
        <el-tag size="small">{{ pet.gender }}</el-tag>
        <el-tag size="small" type="info">{{ pet.age }}岁</el-tag>
        <el-tag size="small" :type="getPetStatusTagType(pet.status)">
          {{ getPetStatusDisplay(pet.status) }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getImageUrl } from '@/utils/imageUrl'
import { getBreedDisplay, getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'

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
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(124, 92, 252, 0.04), 0 3px 12px rgba(124, 92, 252, 0.06);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.pet-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 18px rgba(124, 92, 252, 0.08), 0 12px 32px rgba(124, 92, 252, 0.14);
}

.pet-card-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: linear-gradient(135deg, #ede9f6, #fce7f3);
  position: relative;
}

.pet-card-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(124,92,252,0.2) 0%, transparent 45%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.pet-card:hover .pet-card-image::after {
  opacity: 1;
}

.pet-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.pet-card:hover .pet-card-image img {
  transform: scale(1.06);
}

.pet-card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 60px;
  background: linear-gradient(135deg, #ede9f6, #fce7f3);
}

.pet-card-body {
  padding: 18px;
}

.pet-card-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 2px;
  color: #1e1b4b;
}

.pet-card-breed {
  color: #a78bfa;
  font-size: 13px;
  margin-bottom: 12px;
  font-weight: 500;
}

.pet-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
