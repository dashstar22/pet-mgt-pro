<template>
  <div class="container">
    <h2 class="page-title">宠物列表</h2>

    <!-- Filters -->
    <div class="search-bar">
      <el-select v-model="filters.petType" placeholder="宠物类型" clearable @change="handleSearch">
        <el-option label="狗" value="dog" />
        <el-option label="猫" value="cat" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-select v-model="filters.gender" placeholder="性别" clearable @change="handleSearch">
        <el-option label="公" value="公" />
        <el-option label="母" value="母" />
      </el-select>
      <el-input
        v-model="filters.name"
        placeholder="搜索宠物名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Results -->
    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="4" animated />
    </div>
    <div v-else-if="pets.length > 0">
      <div class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
      </div>
      <Pagination
        :total="total"
        :page="currentPage"
        :size="pageSize"
        @update:page="onPageChange"
        @update:size="onSizeChange"
      />
    </div>
    <el-empty v-else description="没有找到符合条件的宠物" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { getPetList } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'
import Pagination from '@/components/Pagination.vue'

const pets = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const filters = reactive({
  petType: '',
  gender: '',
  name: '',
})

async function loadPets() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (filters.petType) params.petType = filters.petType
    if (filters.gender) params.gender = filters.gender
    if (filters.name) params.name = filters.name
    const data = await getPetList(params)
    pets.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadPets()
}

function onPageChange(val) {
  currentPage.value = val
  loadPets()
}

function onSizeChange(val) {
  pageSize.value = val
  currentPage.value = 1
  loadPets()
}

loadPets()
</script>

<style scoped>
.loading-area {
  padding: 40px;
}
</style>
