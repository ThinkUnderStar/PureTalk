<template>
  <div class="ai-chat-page">
    <header class="chat-header">
      <button class="back-btn" @click="goBack">
        <span class="iconfont icon-fanhui"></span>
        <span class="back-text">返回</span>
      </button>
      <h2>守言</h2>
      <div class="header-spacer"></div>
    </header>

    <div class="chat-container">
      <aside class="session-list" :class="{ collapsed: isSessionListCollapsed }">
        <div class="session-header">
          <h3>对话历史</h3>
          <button class="new-chat-btn" @click="startNewChat">新建对话</button>
        </div>
        <div class="sessions">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: currentSessionId === session.id }]"
            @click="selectSession(session.id)"
          >
            <span class="session-title">{{ session.title }}</span>
            <button class="delete-session-btn" @click.stop="deleteSession(session.id)">×</button>
          </div>
          <div v-if="sessions.length === 0" class="no-sessions">暂无对话记录</div>
        </div>
      </aside>

      <main class="chat-main">
        <div class="messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-chat">
            <div class="empty-icon">🤖</div>
            <p>开始和守言聊天吧！</p>
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              <img
                v-if="msg.role === 'user'"
                :src="userAvatar || 'https://ui-avatars.com/api/?name=' + username + '&background=e8967a&color=fff&size=64'"
                :alt="username"
                class="avatar-img"
              />
              <span v-else>🤖</span>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ msg.createTime || '' }}</div>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              placeholder="输入消息..."
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="isStreaming"
              rows="1"
            ></textarea>
            <button
              class="send-btn"
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isStreaming"
            >
              <span class="iconfont icon-fasong"></span>
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { aiApi, AiSession, AiMessage } from '@/api/ai'

const router = useRouter()
const messagesContainer = ref<HTMLElement | null>(null)

const sessions = ref<AiSession[]>([])
const currentSessionId = ref<number>(0)
const messages = ref<AiMessage[]>([])
const inputMessage = ref<string>('')
const isStreaming = ref<boolean>(false)
const isSessionListCollapsed = ref<boolean>(false)
const userAvatar = ref<string>('')
const username = ref<string>('')

const goBack = () => {
  router.back()
}

const loadUserInfo = () => {
  const token = localStorage.getItem('token')
  if (token) {
    username.value = localStorage.getItem('username') || '用户'
    userAvatar.value = localStorage.getItem('avatar') || ''
  }
}

const loadSessions = async () => {
  try {
    const response = await aiApi.getSessions(1, 100)
    const data = response as any
    if (data.code === 200) {
      sessions.value = data.data.records
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
  }
}

interface BackgroundTask {
  sessionId: number
  abortController: AbortController
  messages: AiMessage[]
}

const backgroundTasks = ref<Record<number, BackgroundTask>>({})
const sessionMessagesCache = ref<Record<number, AiMessage[]>>({})

const selectSession = async (sessionId: number) => {
  if (isStreaming.value && currentSessionId.value !== 0 && currentSessionId.value !== sessionId) {
    sessionMessagesCache.value[currentSessionId.value] = [...messages.value]
    isStreaming.value = false
  }
  
  currentSessionId.value = sessionId
  
  if (sessionMessagesCache.value[sessionId]) {
    messages.value = [...sessionMessagesCache.value[sessionId]]
    delete sessionMessagesCache.value[sessionId]
  } else {
    messages.value = []
  }
  
  await loadMessages(sessionId)
}

const completeBackgroundTask = (sessionId: number, finalMessages: AiMessage[]) => {
  if (backgroundTasks.value[sessionId]) {
    delete backgroundTasks.value[sessionId]
  }
  
  if (sessionId === currentSessionId.value) {
    sessionMessagesCache.value[sessionId] = [...finalMessages]
    loadMessages(sessionId)
  } else {
    sessionMessagesCache.value[sessionId] = [...finalMessages]
  }
}

const parseAiContent = (content: string): string => {
  if (!content) return ''
  try {
    const trimmed = content.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      const obj = JSON.parse(trimmed)
      if (obj.content) return obj.content
      if (obj.message && obj.message.content) return obj.message.content
    }
    return content
  } catch {
    return content
  }
}

const parseStreamingChunk = (chunk: string): string => {
  if (!chunk) return ''
  try {
    const trimmed = chunk.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      const obj = JSON.parse(trimmed)
      if (obj.content) return obj.content
      if (obj.message && obj.message.content) return obj.message.content
    }
    return chunk
  } catch {
    return chunk
  }
}

const loadMessages = async (sessionId: number, skipCache = false) => {
  try {
    const response = await aiApi.getMessages(sessionId, 1, 100)
    const data = response as any
    if (data.code === 200) {
      const serverMessages = data.data.records.reverse().map((msg: any) => ({
        ...msg,
        content: msg.role === 'assistant' ? parseAiContent(msg.content) : msg.content
      }))
      
      if (!skipCache && messages.value.length > 0) {
        const localUserMessages = messages.value.filter(m => m.role === 'user')
        const serverUserMessages = serverMessages.filter(m => m.role === 'user')
        const newUserMessages = localUserMessages.filter(lm => 
          !serverUserMessages.some(sm => sm.content === lm.content && sm.createTime === lm.createTime)
        )
        
        const lastLocalMsg = messages.value[messages.value.length - 1]
        const hasIncompleteAssistant = lastLocalMsg && lastLocalMsg.role === 'assistant'
        
        messages.value = [...serverMessages, ...newUserMessages]
        
        if (hasIncompleteAssistant) {
          messages.value.push(lastLocalMsg)
        }
      } else {
        messages.value = serverMessages
      }
      
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const startNewChat = () => {
  currentSessionId.value = 0
  messages.value = []
  inputMessage.value = ''
}

const deleteSession = async (sessionId: number) => {
  if (!confirm('确定要删除这个对话吗？')) return
  try {
    const response = await aiApi.deleteSession(sessionId)
    const data = response as any
    if (data.code === 200) {
      sessions.value = sessions.value.filter(s => s.id !== sessionId)
      if (currentSessionId.value === sessionId) {
        startNewChat()
      }
    }
  } catch (error) {
    console.error('删除对话失败:', error)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return

  const messageText = inputMessage.value.trim()
  inputMessage.value = ''
  isStreaming.value = true
  const requestStartTime = Date.now()

  const localAbortController = new AbortController()
  const originalSessionId = currentSessionId.value
  const localMessages: AiMessage[] = [...messages.value]
  
  localMessages.push({
    id: Date.now(),
    sessionId: originalSessionId,
    role: 'user',
    content: messageText,
    createTime: new Date().toLocaleString()
  })
  
  messages.value = [...localMessages]
  scrollToBottom()

  const taskKey = originalSessionId || Date.now()
  backgroundTasks.value[taskKey] = {
    sessionId: originalSessionId,
    abortController: localAbortController,
    messages: localMessages
  }

  const timeoutId = setTimeout(() => {
    if (isStreaming.value && Date.now() - requestStartTime > 30000) {
      localAbortController.abort()
      console.warn('请求超时，已自动取消')
      if (currentSessionId.value === originalSessionId || originalSessionId === 0) {
        alert('请求超时，请重试')
      }
    }
  }, 30000)

  const processStream = async () => {
    let fullContent = ''
    let buffer = ''
    let currentSession = originalSessionId
    let taskMessages = [...localMessages]
    let isFirstChunk = true

    try {
      const token = localStorage.getItem('token')
      
      const response = await fetch('/api/ai/sessions/messages/post', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'satoken': token || ''
        },
        body: JSON.stringify({
          sessionId: originalSessionId,
          message: messageText
        }),
        signal: localAbortController.signal
      })

      if (!response.ok) {
        throw new Error('请求失败: ' + response.statusText)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('无法读取响应流')
      }

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          if (buffer.trim()) {
            const textContent = buffer.replace(/event:\s*session\s*/gi, '').replace(/data:\s*/g, '').trim()
            if (textContent) {
              const parsedContent = parseStreamingChunk(textContent)
              if (parsedContent) {
                fullContent += parsedContent
              }
            }
          }
          
          if (fullContent) {
            let lastMsg = taskMessages[taskMessages.length - 1]
            if (lastMsg && lastMsg.role === 'user') {
              taskMessages.push({
                id: Date.now() + 1,
                sessionId: currentSession,
                role: 'assistant',
                content: fullContent,
                createTime: new Date().toLocaleString()
              })
            } else if (lastMsg && lastMsg.role === 'assistant') {
              lastMsg.content = fullContent
              lastMsg.createTime = new Date().toLocaleString()
            }
          }
          
          completeBackgroundTask(currentSession, taskMessages)
          return
        }

        const chunk = new TextDecoder().decode(value, { stream: true })
        buffer += chunk
        
        while (true) {
          const combinedMatch = buffer.match(/(?:event:\s*session\s*)?data:\s*([^\r\n]+)/i)
          if (!combinedMatch) break
          
          const dataContent = combinedMatch[1].trim()
          buffer = buffer.substring(combinedMatch.index + combinedMatch[0].length)
          
          if (!dataContent) continue
          
          if (!isNaN(parseInt(dataContent)) && currentSession === 0) {
            currentSession = parseInt(dataContent)
            continue
          }
          
          const parsedContent = parseStreamingChunk(dataContent)
          fullContent += parsedContent
          
          let lastMsg = taskMessages[taskMessages.length - 1]
          if (lastMsg && lastMsg.role === 'user') {
            taskMessages.push({
              id: Date.now() + 1,
              sessionId: currentSession,
              role: 'assistant',
              content: fullContent,
              createTime: new Date().toLocaleString()
            })
          } else if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content = fullContent
            lastMsg.createTime = new Date().toLocaleString()
          }
          
          if (currentSession === currentSessionId.value) {
            messages.value = [...taskMessages]
            scrollToBottom()
          }
          
          isFirstChunk = false
        }
      }

    } catch (error: any) {
      if (error.name === 'AbortError') {
        console.log('请求已取消')
      } else {
        console.error('发送消息失败:', error)
        if (currentSession === currentSessionId.value) {
          alert('发送消息失败: ' + (error.message || '未知错误'))
        }
      }
    } finally {
      clearTimeout(timeoutId)
      if (currentSession === currentSessionId.value) {
        isStreaming.value = false
      }
      delete backgroundTasks.value[taskKey]
    }
  }

  processStream().catch(console.error)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content: string) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

const initializeChat = async () => {
  loadUserInfo()
  await loadSessions()
  
  if (sessions.value.length > 0) {
    const latestSession = sessions.value[0]
    currentSessionId.value = latestSession.id
    await loadMessages(latestSession.id)
  }
  
  const cacheKeys = Object.keys(sessionMessagesCache.value)
  if (cacheKeys.length > 0) {
    const firstKey = parseInt(cacheKeys[0])
    if (firstKey === currentSessionId.value) {
      messages.value = [...sessionMessagesCache.value[firstKey]]
      delete sessionMessagesCache.value[firstKey]
    }
  }
}

onMounted(() => {
  initializeChat()
})
</script>

<style scoped>
.ai-chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 25%, #f5e6d3 50%, #f0d9c4 75%, #e8c9a8 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  color: #fff;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
}

.chat-header h2 {
  flex: 1;
  text-align: center;
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.back-btn {
  width: auto;
  min-width: 100px;
  background: rgba(255, 255, 255, 0.95);
  border: 3px solid rgba(232, 150, 114, 0.4);
  color: #d97706;
  font-size: 1.4rem;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(232, 150, 114, 0.3);
  font-weight: bold;
}

.back-text {
  font-size: 0.95rem;
  font-weight: 600;
}

.back-btn:hover {
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  color: #fff;
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 6px 25px rgba(232, 150, 114, 0.5);
  border-color: transparent;
}

.header-spacer {
  width: 100px;
}

.chat-container {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 1.5rem;
  gap: 1.5rem;
}

.session-list {
  width: 280px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.session-list.collapsed {
  width: 0;
  margin-left: -1.5rem;
  opacity: 0;
}

.session-header {
  padding: 1.5rem;
  background: linear-gradient(135deg, #e8967a 0%, #f5c9a9 100%);
  border-bottom: none;
}

.session-header h3 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  color: #fff;
  font-weight: 600;
}

.new-chat-btn {
  width: 100%;
  padding: 0.8rem 1rem;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.sessions {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem;
}

.sessions::-webkit-scrollbar {
  width: 6px;
}

.sessions::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.sessions::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
  transition: background 0.3s ease;
}

.sessions::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
  border: 2px solid rgba(232, 150, 114, 0.15);
  background: rgba(255, 255, 255, 0.6);
}

.session-item:hover {
  background: linear-gradient(135deg, #e8967a15 0%, #f5c9a915 100%);
  transform: translateX(4px);
  border-color: rgba(232, 150, 114, 0.35);
}

.session-item.active {
  background: linear-gradient(135deg, #e8967a25 0%, #f5c9a925 100%);
  border-color: rgba(232, 150, 114, 0.5);
  box-shadow: 0 2px 8px rgba(232, 150, 114, 0.15);
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.92rem;
  color: #333;
  font-weight: 500;
}

.delete-session-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 1.3rem;
  cursor: pointer;
  padding: 0 0.4rem;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.session-item:hover .delete-session-btn {
  opacity: 1;
}

.delete-session-btn:hover {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
}

.no-sessions {
  text-align: center;
  color: #999;
  padding: 2.5rem 1.5rem;
  font-size: 0.95rem;
  line-height: 1.6;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
}

.messages::-webkit-scrollbar {
  width: 8px;
}

.messages::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.messages::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  border-radius: 4px;
}

.empty-chat {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.empty-chat p {
  font-size: 1.2rem;
  font-weight: 500;
  color: #555;
}

.message {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  animation: messageSlideIn 0.4s ease;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0d9c4, #e8c9a8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(232, 150, 114, 0.2);
  border: 2px solid rgba(232, 150, 114, 0.3);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  color: #fff;
  border: 2px solid rgba(232, 150, 114, 0.5);
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  color: #fff;
  border: 2px solid rgba(232, 150, 114, 0.5);
}

.message-content {
  max-width: 65%;
}

.message-text {
  padding: 1rem 1.25rem;
  border-radius: 16px;
  line-height: 1.7;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.message-text:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.message.assistant .message-text {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-top-left-radius: 4px;
  color: #333;
}

.message.user .message-text {
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  color: #fff;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 0.78rem;
  color: #999;
  margin-top: 0.5rem;
  padding: 0 0.5rem;
  font-weight: 500;
}

.message.user .message-time {
  text-align: right;
}

.message.assistant .message-text.typing {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 1rem 1.25rem;
}

.message.assistant .message-text.typing .dot {
  animation: bounce 1.4s infinite ease-in-out both;
  font-size: 0.6rem;
  color: #e8967a;
  width: 12px;
  height: 12px;
  background: #e8967a;
  border-radius: 50%;
}

.message.assistant .message-text.typing .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.message.assistant .message-text.typing .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.chat-input-area {
  padding: 1.5rem;
  background: #fff;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.input-wrapper {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  background: #f8f9fa;
  padding: 0.75rem;
  border-radius: 24px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #e8967a;
  background: #fff;
  box-shadow: 0 4px 20px rgba(232, 150, 114, 0.2);
}

.input-wrapper textarea {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  background: transparent;
  border-radius: 16px;
  resize: none;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  max-height: 150px;
  line-height: 1.6;
}

.send-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8967a, #f5c9a9);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(232, 150, 114, 0.4);
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #f5c9a9, #e8967a);
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 20px rgba(232, 150, 114, 0.5);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

code {
  background: rgba(232, 150, 114, 0.15);
  padding: 0.2em 0.5em;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.95em;
  color: #d97706;
}

strong {
  font-weight: 600;
}

@media (max-width: 768px) {
  .chat-container {
    padding: 1rem;
    gap: 1rem;
  }

  .session-list {
    position: absolute;
    left: 1rem;
    top: 80px;
    bottom: 1rem;
    z-index: 100;
    width: 280px;
  }

  .session-list.collapsed {
    width: 0;
    margin-left: 0;
    left: -300px;
  }

  .chat-main {
    margin: 0;
  }

  .message-content {
    max-width: 80%;
  }

  .messages {
    padding: 1.5rem;
  }

  .empty-icon {
    font-size: 4rem;
  }

  .empty-chat p {
    font-size: 1.1rem;
  }
}
</style>