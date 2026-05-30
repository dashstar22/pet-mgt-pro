<template>
  <div class="container">
    <h2 class="page-title">提交领养申请</h2>
    <div class="form-container" style="max-width:600px;margin:0 auto">
      <div v-if="pet" class="apply-pet-info">
        <p>您正在申请领养：<strong>{{ pet.name }}</strong></p>
        <p class="text-secondary">{{ pet.breedName || '' }} · {{ pet.gender }} · {{ pet.age }}岁</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入居住地址" />
        </el-form-item>
        <el-form-item label="养宠经验" prop="experience">
          <el-input v-model="form.experience" type="textarea" :rows="3" placeholder="请描述你的养宠经验" />
        </el-form-item>
        <el-form-item label="申请说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请说明申请领养的原因和计划" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" style="width:100%">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPetDetail } from '@/api/pet'
import { submit } from '@/api/application'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const pet = ref(null)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  applicantName: '',
  phone: '',
  address: '',
  experience: '',
  remark: '',
})

const rules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  experience: [{ required: true, message: '请描述养宠经验', trigger: 'blur' }],
  remark: [{ required: true, message: '请说明申请原因', trigger: 'blur' }],
}

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.petId)
  } catch {
    ElMessage.error('获取宠物信息失败')
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submit({
      petId: route.params.petId,
      ...form,
    })
    ElMessage.success('申请已提交，请等待审核')
    router.push('/user/applications')
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.apply-pet-info {
  background: #ecf5ff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
}
</style>
