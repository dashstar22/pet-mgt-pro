<template>
  <div class="file-upload">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :show-file-list="false"
      accept="image/jpeg,image/png,image/jpg"
    >
      <div v-if="modelValue" class="upload-preview">
        <img :src="previewUrl" />
        <div class="upload-overlay">
          <span>更换图片</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="upload-actions">
      <el-button type="danger" size="small" @click="handleRemove">删除</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const previewUrl = computed(() => {
  if (!props.modelValue) return ''
  if (props.modelValue.startsWith('http')) return props.modelValue
  return '/uploads/' + props.modelValue
})

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

function handleSuccess(response) {
  const url = response.url || response
  emit('update:modelValue', url)
  ElMessage.success('上传成功')
}

function handleError() {
  ElMessage.error('上传失败')
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.upload-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}

.upload-preview:hover .upload-overlay {
  opacity: 1;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-actions {
  margin-top: 8px;
}
</style>
