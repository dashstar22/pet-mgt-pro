<template>
  <div class="container">
    <h2 class="page-title">AI 宠物匹配</h2>
    <el-card class="match-card">
      <p class="match-intro">告诉我们你的生活方式和偏好，AI 将为你推荐最合适的宠物。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="居住环境" prop="livingSpace">
          <el-select v-model="form.livingSpace" placeholder="请选择">
            <el-option label="公寓" value="apartment" />
            <el-option label="楼房（有阳台/院子）" value="house" />
            <el-option label="郊区/农村" value="rural" />
          </el-select>
        </el-form-item>
        <el-form-item label="每日可陪伴时间" prop="timeAvailable">
          <el-select v-model="form.timeAvailable" placeholder="请选择">
            <el-option label="较少（1-2小时）" value="low" />
            <el-option label="中等（3-5小时）" value="medium" />
            <el-option label="充足（5小时以上）" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有儿童" prop="hasChildren">
          <el-radio-group v-model="form.hasChildren">
            <el-radio label="yes">有</el-radio>
            <el-radio label="no">没有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="偏好宠物类型" prop="preferredType">
          <el-select v-model="form.preferredType" placeholder="请选择">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
            <el-option label="不限" value="any" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏好体型" prop="sizePreference">
          <el-select v-model="form.sizePreference" placeholder="请选择">
            <el-option label="小型" value="small" />
            <el-option label="中型" value="medium" />
            <el-option label="大型" value="large" />
            <el-option label="不限" value="any" />
          </el-select>
        </el-form-item>
        <el-form-item label="其他要求" prop="notes">
          <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="任何额外的要求或偏好" />
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
  livingSpace: '',
  timeAvailable: '',
  hasChildren: 'no',
  preferredType: '',
  sizePreference: '',
  notes: '',
})

const rules = {
  livingSpace: [{ required: true, message: '请选择居住环境', trigger: 'change' }],
  timeAvailable: [{ required: true, message: '请选择陪伴时间', trigger: 'change' }],
  preferredType: [{ required: true, message: '请选择偏好类型', trigger: 'change' }],
}

async function handleMatch() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const result = await aiMatch(form)
    sessionStorage.setItem('aiMatchResult', JSON.stringify(result))
    router.push('/user/ai-match/result')
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.match-card { max-width: 600px; margin: 0 auto; }
.match-intro { color: #909399; margin-bottom: 24px; line-height: 1.6; }
</style>
