<template>
  <div class="report-container">
    <div class="report-header">
      <h2>内容举报</h2>
      <p class="subtitle">发现违规内容？告诉我们</p>
    </div>

    <div class="report-form">
      <div class="report-reason">
        <label>举报原因</label>
        <div class="reason-selector">
          <button
            v-for="reason in reportReasons"
            :key="reason.value"
            :class="['reason-btn', { active: form.reason === reason.value }]"
            @click="form.reason = reason.value"
          >
            {{ reason.label }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label for="detail">详细说明</label>
        <textarea
          id="detail"
          v-model="form.detail"
          placeholder="请详细描述违规内容..."
          rows="5"
        ></textarea>
        <span class="char-count">{{ form.detail.length }}/200</span>
      </div>

      <button
        class="submit-btn"
        :disabled="!isFormValid || loading"
        @click="submitReport"
      >
        {{ loading ? '提交中...' : '提交举报' }}
      </button>

      <p class="report-notice">
        温馨提示：举报成功后，我们会尽快核实处理。恶意举报将会被追究责任。
      </p>
    </div>

    <div v-if="submitSuccess" class="success-message">
      <div class="success-icon">✓</div>
      <p>举报成功！</p>
      <p class="success-sub">感谢您的监督，我们会尽快处理</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { reportApi } from '@/api/report'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitSuccess = ref(false)

const reportReasons = [
  { label: '垃圾广告', value: 'advertising' },
  { label: '色情低俗', value: 'pornography' },
  { label: '暴力血腥', value: 'violence' },
  { label: '政治敏感', value: 'politics' },
  { label: '违法犯罪', value: 'illegal' },
  { label: '侵犯隐私', value: 'privacy' },
  { label: '其他违规', value: 'other' }
]

const form = ref({
  reason: 'advertising',
  detail: ''
})

const isFormValid = computed(() => {
  return form.value.detail.trim().length >= 5 && form.value.detail.length <= 200
})

const submitReport = debounce(async () => {
  if (!isFormValid.value) {
    alert('请输入至少5个字符的详细说明')
    return
  }

  const targetType = route.query.type as string || 'post'
  const targetId = Number(route.query.targetId) || 0

  if (!targetId) {
    alert('缺少举报目标')
    return
  }

  try {
    loading.value = true
    const response = await reportApi.sendReport({
      reportType: targetType === 'post' ? 1 : 2,
      targetId,
      targetUserId: 0,
      reason: form.value.reason + ': ' + form.value.detail
    })
    const data = response as any
    if (data.code === 200) {
      submitSuccess.value = true
      setTimeout(() => {
        router.back()
      }, 2000)
    } else {
      alert(data.msg || '提交失败')
    }
  } catch (error: any) {
    console.error('提交举报失败:', error)
    alert(error.msg || '提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}, 500)
</script>

<style scoped>
.report-container {
  min-height: 100vh;
  padding: 2rem;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
}

.report-header {
  text-align: center;
  margin-bottom: 3rem;
  padding-top: 1rem;
}

.report-header h2 {
  font-size: 2rem;
  color: #e53935;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #666;
  font-size: 1rem;
}

.report-form {
  max-width: 600px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.report-reason {
  margin-bottom: 2rem;
}

.report-reason label {
  display: block;
  margin-bottom: 0.75rem;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

.reason-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.reason-btn {
  padding: 0.6rem 1.2rem;
  border: 2px solid #e0e0e0;
  border-radius: 20px;
  background: #fff;
  color: #666;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reason-btn:hover {
  border-color: #e53935;
  color: #e53935;
}

.reason-btn.active {
  border-color: #e53935;
  background: #e53935;
  color: #fff;
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.form-group {
  margin-bottom: 2rem;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 0.75rem;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  resize: none;
  transition: all 0.3s ease;
  font-family: inherit;
  line-height: 1.6;
}

textarea:focus {
  outline: none;
  border-color: #e53935;
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.1);
}

.char-count {
  position: absolute;
  right: 0.5rem;
  bottom: -1.5rem;
  font-size: 0.75rem;
  color: #999;
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #e53935 0%, #c62828 100%);
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(229, 57, 53, 0.4);
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.report-notice {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #fff3e0;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #e65100;
  text-align: center;
}

.success-message {
  max-width: 600px;
  margin: 2rem auto 0;
  background: #fff;
  border-radius: 16px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: #fff;
  font-size: 3rem;
  line-height: 80px;
  margin: 0 auto 1.5rem;
  animation: scaleIn 0.5s ease-out;
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
  font-size: 1.3rem;
  color: #333;
  margin: 0;
}

.success-sub {
  font-size: 0.95rem !important;
  color: #999 !important;
  margin-top: 0.5rem !important;
}

@media (max-width: 768px) {
  .report-container {
    padding: 1rem;
  }

  .report-header h2 {
    font-size: 1.5rem;
  }

  .report-form {
    padding: 1.5rem;
    border-radius: 12px;
  }

  .reason-selector {
    flex-direction: column;
  }

  .reason-btn {
    width: 100%;
    text-align: center;
  }

  textarea {
    min-height: 120px;
  }
}
</style>