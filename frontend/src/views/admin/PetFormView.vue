<template>
  <div class="container">
    <h2 class="page-title">{{ isEdit ? '编辑宠物' : '发布宠物' }}</h2>
    <div class="form-container" style="max-width:640px;margin:0 auto">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="品种" prop="breedId">
          <el-select v-model="form.breedId" placeholder="请选择品种">
            <el-option v-for="b in breeds" :key="b.id" :label="getBreedDisplay(b.breedName)" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="公">公</el-radio><el-radio label="母">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :precision="1" :min="0" :step="0.5" />
        </el-form-item>
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input v-model="form.healthStatus" placeholder="如：健康、已驱虫等" />
        </el-form-item>
        <el-form-item label="疫苗情况">
          <el-input v-model="form.vaccineStatus" placeholder="如：已接种三联疫苗" />
        </el-form-item>
        <el-form-item label="绝育情况">
          <el-input v-model="form.sterilizationStatus" placeholder="如：已绝育" />
        </el-form-item>
        <el-form-item label="性格特点" prop="personality">
          <el-input v-model="form.personality" type="textarea" :rows="2" placeholder="如：温顺、活泼、亲近人" />
        </el-form-item>
        <el-form-item label="领养要求">
          <el-input v-model="form.adoptionRequirement" type="textarea" :rows="2" placeholder="对领养人的要求" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="领养状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="可领养" value="available" />
            <el-option label="已领养" value="adopted" />
            <el-option label="待审核" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物图片">
          <div class="image-upload-area">
            <div v-for="(img, i) in form.images" :key="i" class="image-upload-item">
              <img :src="img.preview || '/uploads/' + img.url" />
              <el-button type="danger" circle size="small" class="image-remove" @click="removeImage(i)">×</el-button>
            </div>
            <div v-if="form.images.length < 6" class="upload-placeholder" @click="triggerUpload">
              <el-icon :size="24"><Plus /></el-icon><span>添加图片</span>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileSelect" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="handleSave" style="width:100%">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBreedDisplay } from '@/utils/labels'
import { createPet, updatePet, getAdminPetList } from '@/api/admin'
import { getBreeds } from '@/api/breed'
import { uploadFile } from '@/api/upload'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const saving = ref(false)
const fileInput = ref(null)
const breeds = ref([])

const form = reactive({
  name: '', breedId: null, gender: '公', age: 0, weight: null,
  healthStatus: '', vaccineStatus: '', sterilizationStatus: '',
  personality: '', adoptionRequirement: '', status: '',
  existingImages: [],
  newImages: [],
  deleteImageIds: [],
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  breedId: [{ required: true, message: '请选择品种', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  healthStatus: [{ required: true, message: '请输入健康状况', trigger: 'blur' }],
  personality: [{ required: true, message: '请描述性格特点', trigger: 'blur' }],
}

onMounted(async () => {
  try { breeds.value = await getBreeds() } catch {}
  const id = route.params.id
  if (id) {
    isEdit.value = true
    editingId.value = id
    try {
      const data = await getAdminPetList({ page: 1, size: 100 })
      const pet = (data.records || []).find(p => String(p.id) === String(id))
      if (pet) {
        form.name = pet.name; form.breedId = pet.breedId; form.gender = pet.gender
        form.age = pet.age || 0; form.weight = pet.weight
        form.healthStatus = pet.healthStatus || ''; form.vaccineStatus = pet.vaccineStatus || ''
        form.sterilizationStatus = pet.sterilizationStatus || ''; form.personality = pet.personality || ''
        form.adoptionRequirement = pet.adoptionRequirement || ''
        form.status = pet.status || ''
      }
    } catch {}
  }
})

function triggerUpload() { fileInput.value?.click() }

async function handleFileSelect(e) {
  const files = e.target.files
  if (!files.length) return
  for (const file of files) {
    try {
      const result = await uploadFile(file)
      const url = result.url || result
      form.images.push({ url: url, preview: URL.createObjectURL(file) })
    } catch {}
  }
  fileInput.value.value = ''
}

function removeImage(index) {
  form.images.splice(index, 1)
  if (form.coverIndex === index) form.coverIndex = 0
  else if (form.coverIndex > index) form.coverIndex--
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.name); fd.append('breedId', form.breedId)
    fd.append('gender', form.gender); fd.append('age', form.age)
    if (form.weight) fd.append('weight', form.weight)
    fd.append('healthStatus', form.healthStatus)
    fd.append('vaccineStatus', form.vaccineStatus || '')
    fd.append('sterilizationStatus', form.sterilizationStatus || '')
    fd.append('personality', form.personality)
    fd.append('adoptionRequirement', form.adoptionRequirement || '')
    if (isEdit.value && form.status) {
      fd.append('status', form.status)
    }
    fd.append('coverIndex', form.coverIndex)
    for (const img of form.images) {
      if (img.url) fd.append('imageUrls', img.url)
    }
    if (isEdit.value) {
      await updatePet(editingId.value, fd)
      ElMessage.success('更新成功')
    } else {
      await createPet(fd)
      ElMessage.success('发布成功')
    }
    router.push('/admin/pets')
  } catch {} finally { saving.value = false }
}
</script>

<style scoped>
.image-upload-area { display: flex; flex-wrap: wrap; gap: 8px; }
.image-upload-item { width: 100px; height: 100px; position: relative; border-radius: 6px; overflow: hidden; }
.image-upload-item img { width: 100%; height: 100%; object-fit: cover; }
.image-remove { position: absolute; top: -4px; right: -4px; width: 20px; height: 20px; }
.upload-placeholder { width: 100px; height: 100px; border: 2px dashed #dcdfe6; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; font-size: 12px; color: #909399; cursor: pointer; }
.upload-placeholder:hover { border-color: #409eff; color: #409eff; }
</style>
