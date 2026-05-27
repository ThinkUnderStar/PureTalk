<template>
  <div class="page-wrapper">
    <button class="back-btn" @click="goBack">← 返回</button>
    <div class="feedback-container">
    <header class="feedback-header">
      <div class="header-content">
        <h2>意见反馈</h2>
        <p class="subtitle">💡 您的反馈是我们前进的动力</p>
      </div>
    </header>

    <div class="feedback-form">
      <div class="form-group">
        <label>反馈类型</label>
        <div class="type-selector">
          <button
            v-for="type in feedbackTypes"
            :key="type.value"
            :class="['type-btn', { active: form.type === type.value }]"
            @click="form.type = type.value"
          >
            {{ type.label }}
          </button>
        </div>
      </div>

      <div class="form-group" v-if="form.type === 'other'">
        <label for="title">反馈标题</label>
        <input
          type="text"
          id="title"
          v-model="form.title"
          placeholder="请输入反馈标题..."
          maxlength="50"
        />
        <span class="char-count">{{ form.title.length }}/50</span>
      </div>

      <div class="form-group">
        <label for="content">反馈内容</label>
        <textarea
          id="content"
          v-model="form.content"
          placeholder="请详细描述您的问题或建议..."
          rows="6"
        ></textarea>
        <span class="char-count">{{ form.content.length }}/500</span>
      </div>

      <button
        class="submit-btn"
        :disabled="!isFormValid || loading"
        @click="submitFeedback"
      >
        {{ loading ? '提交中...' : '提交反馈' }}
      </button>
    </div>

    <div v-if="submitSuccess" class="success-message">
      <div class="success-icon">✓</div>
      <p>感谢您的反馈！</p>
      <p class="success-sub">我们会尽快处理并回复您</p>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { feedbackApi } from '@/api/feedback'
import { debounce } from '@/utils/debounce'
import { getUserId } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const submitSuccess = ref(false)

const goBack = () => {
  router.back()
}

const feedbackTypes = [
  { label: '功能建议', value: 'suggestion' },
  { label: 'Bug反馈', value: 'bug' },
  { label: '体验问题', value: 'experience' },
  { label: '其他', value: 'other' }
]

const form = ref({
  type: 'suggestion',
  title: '',
  content: ''
})

const isFormValid = computed(() => {
  const contentValid = form.value.content.trim().length >= 10 && form.value.content.length <= 500
  const titleValid = form.value.type === 'other' ? form.value.title.trim().length >= 2 && form.value.title.length <= 50 : true
  return contentValid && titleValid
})

const submitFeedback = debounce(async () => {
  if (!isFormValid.value) {
    alert('请输入至少10个字符的反馈内容')
    return
  }

  const userId = getUserId()
  if (!userId) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    loading.value = true
    const response = await feedbackApi.sendFeedback({
      userId,
      title: form.value.type === 'other' ? form.value.title : form.value.type === 'suggestion' ? '功能建议' : form.value.type === 'bug' ? 'Bug反馈' : form.value.type === 'experience' ? '体验问题' : '其他',
      content: form.value.content
    })
    const data = response as any
    if (data.code === 200) {
      submitSuccess.value = true
      setTimeout(() => {
        router.push('/')
      }, 2000)
    } else {
      alert(data.msg || '提交失败')
    }
  } catch (error: any) {
    console.error('提交反馈失败:', error)
    alert(error.msg || '提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}, 500)
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: url('@/assets/picture/background.png') no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.back-btn {
  position: absolute;
  top: 2rem;
  left: 2rem;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(102, 126, 234, 0.4);
  font-size: 1rem;
  cursor: pointer;
  color: #667eea;
  padding: 0.55rem 1.1rem;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
}

.feedback-container {
  padding: 1rem;
  background: rgba(167, 243, 208, 0.95);
  backdrop-filter: blur(20px);
  width: 85%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  border-radius: 36px;
  border: 3px solid rgba(74, 222, 128, 0.4);
  box-shadow: 0 12px 40px rgba(167, 243, 208, 0.3);
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.feedback-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 8px;
  margin-bottom: 0.75rem;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.12);
}

.header-content {
  flex: 1;
  text-align: center;
}

.header-spacer {
  width: 100px;
}

.feedback-header h2 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 0.35rem;
  font-weight: 600;
}

.subtitle {
  color: #667eea;
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}

.feedback-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 8px;
  padding: 1.25rem 1.75rem;
  box-shadow: 0 6px 22px rgba(102, 126, 234, 0.15);
  flex: 1;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 1rem;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 0.55rem;
  color: #333;
  font-size: 0.95rem;
  font-weight: 600;
}

.type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.type-btn {
  padding: 0.8rem 1.6rem;
  border: 2px solid rgba(102, 126, 234, 0.4);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.type-btn:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.type-btn.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
  transform: translateY(-2px);
}

input[type="text"] {
  width: 100%;
  padding: 1rem;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-radius: 16px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.95);
}

input[type="text"]:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-radius: 16px;
  font-size: 1rem;
  resize: none;
  transition: all 0.3s ease;
  font-family: inherit;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.95);
}

textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.char-count {
  position: absolute;
  right: 0.5rem;
  bottom: -1.5rem;
  font-size: 0.78rem;
  color: #667eea;
  font-weight: 500;
}

.submit-btn {
  width: 100%;
  padding: 1.1rem;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1.5rem;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.45);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.success-message {
  max-width: 600px;
  margin: 2rem auto 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.success-icon {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 3.2rem;
  line-height: 90px;
  margin: 0 auto 1.5rem;
  animation: scaleIn 0.5s ease-out;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.success-message p {
  font-size: 1.4rem;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.success-sub {
  font-size: 1rem !important;
  color: #666 !important;
  margin-top: 0.5rem !important;
  font-weight: 500 !important;
}

@media (max-width: 768px) {
  .feedback-container {
    padding: 1rem;
  }

  .feedback-header h2 {
    font-size: 1.6rem;
  }

  .feedback-form {
    padding: 1.5rem;
    border-radius: 16px;
  }

  .type-selector {
    flex-direction: column;
  }

  .type-btn {
    width: 100%;
  }

  textarea {
    min-height: 150px;
  }
}
</style>