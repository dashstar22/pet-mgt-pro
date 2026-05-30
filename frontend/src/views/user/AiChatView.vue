<template>
  <div class="container">
    <h2 class="page-title">AI 养宠问答</h2>
    <el-card class="chat-card">
      <div class="chat-messages" ref="chatBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <p>👋 你好！我是 AI 养宠助手</p>
          <p class="text-secondary">有什么养宠问题想问我吗？</p>
        </div>
        <div v-for="(msg, i) in messages" :key="i" class="chat-message" :class="msg.role">
          <div class="message-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="thinking" class="chat-message assistant">
          <div class="message-bubble thinking">正在思考...</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="question"
          placeholder="输入你的问题..."
          @keyup.enter="sendMessage"
          :disabled="thinking"
        >
          <template #append>
            <el-button :loading="thinking" @click="sendMessage">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiChat } from '@/api/ai'
import { ElMessage } from 'element-plus'

const messages = ref([])
const question = ref('')
const thinking = ref(false)
const chatBox = ref(null)

async function sendMessage() {
  const q = question.value.trim()
  if (!q) return
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  thinking.value = true
  await nextTick()
  scrollToBottom()
  try {
    const data = await aiChat(q)
    messages.value.push({ role: 'assistant', content: data.answer || JSON.stringify(data) })
  } catch {} finally {
    thinking.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (chatBox.value) { chatBox.value.scrollTop = chatBox.value.scrollHeight }
}
</script>

<style scoped>
.chat-card { max-width: 700px; margin: 0 auto; }
.chat-messages { min-height: 400px; max-height: 500px; overflow-y: auto; padding: 16px; background: #f5f7fa; border-radius: 8px; margin-bottom: 16px; }
.chat-empty { text-align: center; padding: 60px 20px; color: #303133; font-size: 16px; }
.text-secondary { color: #909399; font-size: 14px; margin-top: 8px; }
.chat-message { margin-bottom: 12px; display: flex; }
.chat-message.user { justify-content: flex-end; }
.chat-message.user .message-bubble { background: #409eff; color: #fff; border-radius: 12px 12px 0 12px; }
.chat-message.assistant .message-bubble { background: #fff; border: 1px solid #e4e7ed; border-radius: 12px 12px 12px 0; }
.message-bubble { max-width: 80%; padding: 10px 16px; font-size: 14px; line-height: 1.6; word-break: break-word; }
.message-bubble.thinking { color: #909399; font-style: italic; }
.chat-input { border-top: 1px solid #ebeef5; padding-top: 12px; }
</style>
