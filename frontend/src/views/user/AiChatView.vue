<template>
  <div class="container">
    <h2 class="page-title">🐾 AI 养宠问答</h2>

    <div class="chat-wrapper">
      <!-- 消息列表 -->
      <div class="chat-messages" ref="chatBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <div class="empty-icon">
            <span class="empty-emojis">🐶🐱🐾</span>
          </div>
          <h3>你好！我是 AI 养宠助手</h3>
          <p>关于喂养、健康、训练、日常护理……尽管问我</p>
          <div class="quick-questions">
            <span class="quick-chip" v-for="q in quickQuestions" :key="q" @click="question = q; sendMessage()">{{ q }}</span>
          </div>
        </div>

        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="chat-message"
          :class="msg.role"
        >
          <div class="message-avatar" v-if="msg.role === 'assistant'">🤖</div>
          <div class="message-body">
            <div class="message-bubble">{{ msg.content }}</div>
          </div>
          <div class="message-avatar" v-if="msg.role === 'user'">👤</div>
        </div>

        <div v-if="thinking" class="chat-message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-body">
            <div class="message-bubble thinking">
              <span class="dot-pulse"></span>
              正在思考...
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <div class="chat-input-row">
          <el-input
            v-model="question"
            placeholder="输入你的问题，比如：狗狗不吃东西怎么办？"
            @keyup.enter="sendMessage"
            :disabled="thinking"
            size="large"
            class="chat-input"
          />
          <el-button
            type="primary"
            size="large"
            :loading="thinking"
            @click="sendMessage"
            class="send-btn"
          >
            <span v-if="!thinking">发送</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiChat } from '@/api/ai'

const messages = ref([])
const question = ref('')
const thinking = ref(false)
const chatBox = ref(null)

const quickQuestions = [
  '狗狗不吃东西怎么办？',
  '猫咪多久洗一次澡？',
  '小狗需要打哪些疫苗？',
  '如何训练猫咪用猫砂？',
]

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
/* ========== 整体容器 ========== */
.chat-wrapper {
  max-width: 720px;
  margin: 0 auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 1px 3px rgba(124,92,252,0.04), 0 6px 24px rgba(124,92,252,0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 580px;
}

/* ========== 消息区 ========== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(180deg, #faf8fc 0%, #fdf2f8 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-messages::-webkit-scrollbar {
  width: 5px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ddd6fe;
  border-radius: 10px;
}

/* ========== 空状态 ========== */
.chat-empty {
  text-align: center;
  padding: 40px 20px;
  margin: auto;
}

.empty-icon {
  margin-bottom: 16px;
}

.empty-emojis {
  font-size: 48px;
  letter-spacing: 8px;
}

.chat-empty h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.chat-empty p {
  color: #a78bfa;
  font-size: 14px;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-chip {
  display: inline-block;
  padding: 7px 16px;
  font-size: 13px;
  color: #7c5cfc;
  background: #f5f0ff;
  border: 1px solid #ddd6fe;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.quick-chip:hover {
  background: #7c5cfc;
  color: #fff;
  border-color: #7c5cfc;
}

/* ========== 消息气泡 ========== */
.chat-message {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  background: #fff;
  box-shadow: 0 1px 4px rgba(124,92,252,0.08);
}

.message-body {
  max-width: 72%;
}

.message-bubble {
  padding: 12px 18px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  border-radius: 18px;
}

.chat-message.user .message-bubble {
  background: linear-gradient(135deg, #7c5cfc, #a78bfa);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .message-bubble {
  background: #fff;
  color: #334155;
  border: 1px solid #ede9f6;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(124,92,252,0.04);
}

/* ========== 思考中 ========== */
.message-bubble.thinking {
  color: #a78bfa;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #a78bfa;
  animation: dotBounce 1.2s ease-in-out infinite;
}

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* ========== 输入区 ========== */
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid #ede9f6;
  background: #fff;
}

.chat-input-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 50px;
  background: #faf8fc;
  border-color: #ede9f6;
  padding: 4px 20px;
  box-shadow: none;
}

.chat-input :deep(.el-input__wrapper:hover) {
  border-color: #c4b5fd;
}

.chat-input :deep(.el-input__wrapper.is-focus) {
  border-color: #7c5cfc;
  box-shadow: 0 0 0 3px rgba(124,92,252,0.08) !important;
}

.send-btn {
  border-radius: 50px;
  padding: 12px 28px;
  font-weight: 600;
  flex-shrink: 0;
}
</style>
