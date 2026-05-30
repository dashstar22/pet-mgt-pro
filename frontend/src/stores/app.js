import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getBreeds } from '@/api/breed'

export const useAppStore = defineStore('app', () => {
  const breeds = ref([])
  let lastPetType = null

  async function fetchBreeds(petType) {
    if (breeds.value.length > 0 && petType === lastPetType) {
      return breeds.value
    }
    lastPetType = petType
    breeds.value = await getBreeds(petType)
    return breeds.value
  }

  return { breeds, fetchBreeds }
})
