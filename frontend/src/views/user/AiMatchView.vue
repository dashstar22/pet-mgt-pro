<template>
  <div class="container">
    <h2 class="page-title">AI 宠物匹配</h2>
    <el-card class="match-card">
      <p class="match-intro">告诉我们你的生活方式和偏好，AI 将为你推荐最合适的宠物。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="偏好宠物类型" prop="petType">
          <el-select v-model="form.petType" placeholder="请选择">
            <el-option label="狗" value="狗" />
            <el-option label="猫" value="猫" />
            <el-option label="不限" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="居住环境" prop="livingSpace">
          <el-select v-model="form.livingSpace" placeholder="请选择">
            <el-option label="公寓" value="公寓" />
            <el-option label="普通住宅" value="普通住宅" />
            <el-option label="带院子" value="带院子" />
          </el-select>
        </el-form-item>
        <el-form-item label="每日可陪伴时间" prop="accompanyTime">
          <el-select v-model="form.accompanyTime" placeholder="请选择">
            <el-option label="少于1小时" value="少于1小时" />
            <el-option label="1-3小时" value="1-3小时" />
            <el-option label="3-6小时" value="3-6小时" />
            <el-option label="6小时以上" value="6小时以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望性格" prop="personality">
          <el-select v-model="form.personality" placeholder="请选择">
            <el-option label="温顺亲人" value="温顺亲人" />
            <el-option label="活泼好动" value="活泼好动" />
            <el-option label="独立安静" value="独立安静" />
            <el-option label="聪明机警" value="聪明机警" />
            <el-option label="不限" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="可接受健康状态" prop="healthAcceptance">
          <el-select v-model="form.healthAcceptance" placeholder="请选择">
            <el-option label="仅健康" value="仅健康" />
            <el-option label="可接受轻微健康问题" value="可接受轻微健康问题" />
            <el-option label="可接受任何状态" value="可接受任何状态" />
            <el-option label="不限" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="养宠经验" prop="experience">
          <el-select v-model="form.experience" placeholder="请选择">
            <el-option label="新手" value="新手" />
            <el-option label="有一定经验" value="有一定经验" />
            <el-option label="经验丰富" value="经验丰富" />
            <el-option label="不限" value="" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleMatch" style="width:100%">
            开始匹配
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { aiMatch } from '@/api/ai'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  petType: '',
  livingSpace: '',
  accompanyTime: '',
  personality: '',
  healthAcceptance: '',
  experience: '',
})

const rules = {
  petType: [{ required: true, message: '请选择偏好宠物类型', trigger: 'change' }],
  livingSpace: [{ required: true, message: '请选择居住环境', trigger: 'change' }],
  accompanyTime: [{ required: true, message: '请选择陪伴时间', trigger: 'change' }],
}

async function handleMatch() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const result = await aiMatch(form)
    sessionStorage.setItem('aiMatchResult', JSON.stringify(result))
    router.push('/user/ai-match/result')
  } catch (e) {
    ElMessage.error(e?.message || '匹配失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.match-card {
  max-width: 620px;
  margin: 0 auto;
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 1px 3px rgba(124,92,252,0.04), 0 6px 24px rgba(124,92,252,0.08);
}

.match-intro {
  color: #6d5d8b;
  margin-bottom: 28px;
  line-height: 1.7;
  font-size: 15px;
  background: linear-gradient(135deg, #f5f0ff, #fdf2f8);
  padding: 16px 20px;
  border-radius: 12px;
  border-left: 4px solid #7c5cfc;
}

.match-card :deep(.el-form-item__label) {
  font-weight: 600;
  color: #4c1d95;
}

.match-card :deep(.el-select) {
  width: 100%;
}

.match-card :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
}

.match-card :deep(.el-button--primary) {
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
</style>
