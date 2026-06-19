<template>
  <div class="container">
    <div v-if="loading" style="padding:40px">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="pet" class="detail-layout">
      <!-- Images -->
      <div class="detail-images">
        <el-image
          v-if="mainImage"
          :src="'/uploads/' + mainImage"
          fit="cover"
          class="main-image"
        />
        <div v-else class="main-image-placeholder">🐾</div>
        <div class="image-thumbs" v-if="images.length > 1">
          <img
            v-for="img in images"
            :key="img.id"
            :src="'/uploads/' + img.imageUrl"
            class="thumb"
            :class="{ active: mainImage === img.imageUrl }"
            @click="mainImage = img.imageUrl"
          />
        </div>
      </div>

      <!-- Info -->
      <div class="detail-info">
        <h1 class="detail-name">{{ pet.name }}</h1>
        <p class="detail-breed">{{ getBreedDisplay(pet.breedName) }}</p>

        <el-descriptions :column="1" border class="detail-desc">
          <el-descriptions-item label="性别">{{ pet.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ pet.age }} 岁</el-descriptions-item>
          <el-descriptions-item label="体重" v-if="pet.weight">{{ pet.weight }} kg</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ pet.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="疫苗情况">{{ pet.vaccineStatus || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="绝育情况">{{ pet.sterilizationStatus || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="性格">{{ pet.personality }}</el-descriptions-item>
          <el-descriptions-item label="领养要求" v-if="pet.adoptionRequirement">{{ pet.adoptionRequirement }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getPetStatusTagType(pet.status)">
              {{ getPetStatusDisplay(pet.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button
            v-if="pet.status === 'available' && userStore.isLoggedIn"
            type="primary"
            size="large"
            @click="$router.push(`/user/apply/${pet.id}`)"
          >
            申请领养
          </el-button>
          <el-button
            v-if="pet.status === 'available' && !userStore.isLoggedIn"
            type="primary"
            size="large"
            @click="$router.push('/login')"
          >
            登录后申请领养
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="宠物不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPetDetail, getPetImages } from '@/api/pet'
import { useUserStore } from '@/stores/user'
import { getBreedDisplay, getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'

const route = useRoute()
const userStore = useUserStore()

const pet = ref(null)
const images = ref([])
const mainImage = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const id = route.params.id
    const [petData, imageData] = await Promise.all([
      getPetDetail(id),
      getPetImages(id).catch(() => []),
    ])
    pet.value = petData
    images.value = Array.isArray(imageData) ? imageData : []
    if (images.value.length > 0) {
      mainImage.value = images.value.find(i => i.isCover === 1)?.imageUrl || images.value[0]?.imageUrl
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.main-image {
  width: 100%;
  height: 360px;
  border-radius: 8px;
}

.main-image-placeholder {
  width: 100%;
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: #f0f2f5;
  border-radius: 8px;
}

.image-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumb.active {
  border-color: #409eff;
}

.detail-info {
  padding-left: 8px;
}

.detail-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.detail-breed {
  color: #909399;
  font-size: 16px;
  margin-bottom: 20px;
}

.detail-desc {
  margin-bottom: 24px;
}

.detail-actions {
  margin-top: 20px;
}
</style>
