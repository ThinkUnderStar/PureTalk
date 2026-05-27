<template>
  <div class="admin-create-container">
    <div class="admin-create-form">
      <div class="form-header">
        <h2>创建管理员</h2>
        <p class="subtitle">创建新的管理员账户</p>
      </div>

      <div class="form-tabs">
        <button
          :class="['tab', { active: currentStep === 'basic' }]"
          @click="currentStep = 'basic'"
        >
          基础信息
        </button>
        <button
          :class="['tab', { active: currentStep === 'verify' }]"
          @click="currentStep = 'verify'"
        >
          验证信息
        </button>
      </div>

      <form @submit.prevent="handleSubmit">
        <div v-show="currentStep === 'basic'">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              type="text"
              id="username"
              v-model="form.username"
              placeholder="4-16位字母、数字、下划线"
            />
            <span class="input-hint">以字母开头</span>
          </div>

          <div class="form-group">
            <label for="phone">手机号</label>
            <input
              type="tel"
              id="phone"
              v-model="form.phone"
              placeholder="请输入手机号"
            />
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input
              type="password"
              id="password"
              v-model="form.password"
              placeholder="8-20位，包含字母和数字"
            />
            <span class="input-hint">至少包含一个字母和一个数字</span>
          </div>

          <button
            type="button"
            class="submit-btn next-btn"
            @click="goToVerifyStep"
            :disabled="!isBasicInfoValid"
          >
            下一步
          </button>
        </div>

        <div v-show="currentStep === 'verify'">
          <div class="form-group">
            <label for="phoneCode">手机验证码</label>
            <div class="code-input-wrapper">
              <input
                type="text"
                id="phoneCode"
                v-model="form.phoneCode"
                placeholder="请输入手机验证码"
              />
              <button
                type="button"
                class="send-code-btn"
                :disabled="countdown.phone > 0 || !form.phone"
                @click="sendPhoneCode"
              >
                {{ countdown.phone > 0 ? `${countdown.phone}秒` : '获取验证码' }}
              </button>
            </div>
            <div v-if="verification.phone" class="verification-result success">
              ✓ {{ verification.phone }}
            </div>
          </div>

          <div class="form-group">
            <label for="emailCode">邮箱验证码</label>
            <div class="code-input-wrapper">
              <input
                type="text"
                id="emailCode"
                v-model="form.emailCode"
                placeholder="请输入邮箱验证码"
              />
              <button
                type="button"
                class="send-code-btn"
                :disabled="countdown.email > 0 || !form.email"
                @click="sendEmailCode"
              >
                {{ countdown.email > 0 ? `${countdown.email}秒` : '获取验证码' }}
              </button>
            </div>
            <div v-if="verification.email" class="verification-result success">
              ✓ {{ verification.email }}
            </div>
          </div>

          <div class="form-actions">
            <button
              type="button"
              class="submit-btn back-btn"
              @click="currentStep = 'basic'"
            >
              上一步
            </button>
            <button
              type="submit"
              class="submit-btn"
              :disabled="loading || !isAllVerified"
            >
              {{ loading ? '创建中...' : '创建管理员' }}
            </button>
          </div>
        </div>

        <div class="form-footer">
          <button class="link" @click="goBackToAdmin">返回管理中心</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { rootApi } from '@/api/user'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const loading = ref(false)
const currentStep = ref<'basic' | 'verify'>('basic')
const countdown = ref({
  phone: 0,
  email: 0
})
const form = ref({
  username: '',
  phone: '',
  email: '',
  password: '',
  phoneCode: '',
  emailCode: ''
})
const verification = ref({
  phone: '',
  email: ''
})

const isBasicInfoValid = computed(() => {
  const usernameRegex = /^[a-zA-Z][a-zA-Z0-9_]{3,15}$/
  const passwordRegex = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z!@#$%^&*_\-]{8,20}$/
  const phoneRegex = /^1[3-9]\d{9}$/
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  return (
    usernameRegex.test(form.value.username) &&
    passwordRegex.test(form.value.password) &&
    phoneRegex.test(form.value.phone) &&
    emailRegex.test(form.value.email)
  )
})

const isAllVerified = computed(() => {
  return verification.value.phone && verification.value.email
})

const goToVerifyStep = () => {
  if (!isBasicInfoValid.value) {
    alert('请完善基础信息')
    return
  }
  currentStep.value = 'verify'
}

const sendPhoneCode = debounce(async () => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(form.value.phone)) {
    alert('手机号格式不正确')
    return
  }

  try {
    const response = await rootApi.sendPhoneCode(form.value.phone)
    const data = response as any
    if (data.code === 200) {
      alert('验证码已发送')
      startCountdown('phone')
    } else {
      alert(data.message || '发送失败')
    }
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    alert(error.message || '发送失败')
  }
}, 500)

const sendEmailCode = debounce(async () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.value.email)) {
    alert('邮箱格式不正确')
    return
  }

  try {
    const response = await rootApi.sendEmailCode(form.value.email)
    const data = response as any
    if (data.code === 200) {
      alert('验证码已发送')
      startCountdown('email')
    } else {
      alert(data.message || '发送失败')
    }
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    alert(error.message || '发送失败')
  }
}, 500)

const startCountdown = (type: 'phone' | 'email') => {
  countdown.value[type] = 60
  const timer = setInterval(() => {
    if (countdown.value[type] > 0) {
      countdown.value[type]--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

const goBackToAdmin = () => {
  router.replace('/admin')
}

const handleSubmit = async () => {
  if (!verification.value.phone) {
    alert('请先验证手机验证码')
    return
  }

  if (!verification.value.email) {
    alert('请先验证邮箱验证码')
    return
  }

  try {
    loading.value = true
    const response = await rootApi.createAdmin({
      username: form.value.username,
      password: form.value.password,
      phone: form.value.phone,
      email: form.value.email,
      rightPhone: true,
      rightEmail: true
    })
    const data = response as any
    if (data.code === 200) {
      alert('管理员创建成功')
      router.push('/admin')
    } else {
      alert(data.msg || '创建失败')
    }
  } catch (error: any) {
    console.error('创建管理员失败:', error)
    alert(error.msg || '创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-create-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 2rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.admin-create-form {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  padding: 2.5rem;
  width: 100%;
  max-width: 450px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h2 {
  color: #333;
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  letter-spacing: 1px;
}

.subtitle {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.form-tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #f0f0f0;
}

.tab {
  flex: 1;
  padding: 0.75rem;
  border: none;
  background: none;
  font-size: 1rem;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
  font-weight: 500;
}

.tab:hover {
  color: #e94560;
}

.tab.active {
  color: #e94560;
  border-bottom-color: #e94560;
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.5rem;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.form-group input {
  width: 100%;
  padding: 0.9rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: #f9f9f9;
}

.form-group input:focus {
  outline: none;
  border-color: #e94560;
  box-shadow: 0 0 0 3px rgba(233, 69, 96, 0.1);
  background-color: #fff;
}

.input-hint {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #999;
}

.code-input-wrapper {
  display: flex;
  gap: 1rem;
}

.code-input-wrapper input {
  flex: 1;
}

.send-code-btn {
  padding: 0.9rem 1.2rem;
  border: 1px solid #e94560;
  border-radius: 8px;
  background-color: #fff;
  color: #e94560;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.send-code-btn:hover:not(:disabled) {
  background-color: #e94560;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(233, 69, 96, 0.3);
}

.send-code-btn:disabled {
  border-color: #e0e0e0;
  color: #999;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.verification-result {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  padding: 0.5rem;
  border-radius: 4px;
  background-color: rgba(76, 175, 80, 0.1);
  border: 1px solid rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.submit-btn {
  flex: 1;
  padding: 0.9rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #e94560 0%, #c23a51 100%);
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(233, 69, 96, 0.4);
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.next-btn {
  margin-top: 1.5rem;
}

.back-btn {
  background: #fff;
  border: 1px solid #e0e0e0;
  color: #666;
}

.back-btn:hover:not(:disabled) {
  background: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.9rem;
  color: #666;
}

.form-footer .link {
  color: #e94560;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  background: none;
  border: none;
  padding: 0;
  font-size: 0.9rem;
  cursor: pointer;
  font-family: inherit;
}

.form-footer .link:hover {
  color: #c23a51;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .admin-create-container {
    padding: 1rem;
  }

  .admin-create-form {
    padding: 2rem;
  }

  .form-header h2 {
    font-size: 1.5rem;
  }

  .code-input-wrapper {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>