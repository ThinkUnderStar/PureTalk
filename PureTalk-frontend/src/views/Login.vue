<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>PureTalk</h1>
        <p>欢迎回来</p>
      </div>

      <div class="type-selector">
        <div class="selector-track" :class="userType"></div>
        <button
          class="selector-btn"
          :class="{ active: userType === 'user' }"
          @click="userType = 'user'"
        >
          用户登录
        </button>
        <button
          class="selector-btn"
          :class="{ active: userType === 'admin' }"
          @click="userType = 'admin'"
        >
          管理员登录
        </button>
      </div>

      <div class="login-content">
        <transition name="fade-slide" mode="out-in">
          <div v-if="userType === 'user'" key="user" class="form-section">
            <div class="login-type-selector">
              <button
                :class="['type-btn', { active: loginType === 'password' }]"
                @click="loginType = 'password'"
              >
                密码登录
              </button>
              <button
                :class="['type-btn', { active: loginType === 'code' }]"
                @click="loginType = 'code'"
              >
                验证码登录
              </button>
            </div>

            <div class="form-group">
              <label>手机号</label>
              <input
                type="tel"
                v-model="form.phone"
                placeholder="请输入手机号"
              />
            </div>

            <div class="form-group" v-if="loginType === 'password'">
              <label>密码</label>
              <input
                type="password"
                v-model="form.password"
                placeholder="请输入密码"
              />
            </div>

            <div class="form-group code-group" v-else>
              <label>验证码</label>
              <div class="code-input-row">
                <input
                  type="text"
                  v-model="form.code"
                  placeholder="请输入验证码"
                  maxlength="6"
                />
                <button
                  class="code-btn"
                  :disabled="countdown > 0 || !form.phone"
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
              </div>
            </div>

            <label class="remember-label" v-if="loginType === 'code'">
              <input type="checkbox" v-model="form.remember" />
              <span>记住登录状态</span>
            </label>
          </div>

          <div v-else key="admin" class="form-section">
            <div class="form-group">
              <label>管理员用户名</label>
              <input
                type="text"
                v-model="form.adminUsername"
                placeholder="请输入管理员用户名"
              />
            </div>

            <div class="form-group">
              <label>管理员密码</label>
              <input
                type="password"
                v-model="form.adminPassword"
                placeholder="请输入管理员密码"
              />
            </div>
          </div>
        </transition>
      </div>

      <button class="submit-btn" :disabled="loading" @click="handleSubmit">
        <span v-if="!loading">登 录</span>
        <span v-else class="loading-dots">登录中<span>.</span><span>.</span><span>.</span></span>
      </button>

      <div class="form-footer">
        <router-link to="/register" class="link">
          还没有账号？<span>立即注册</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const userType = ref<'user' | 'admin'>('user')
const loginType = ref<'password' | 'code'>('password')
const loading = ref<boolean>(false)
const countdown = ref<number>(0)
const form = ref({
  phone: '',
  password: '',
  code: '',
  remember: false,
  adminUsername: '',
  adminPassword: ''
})

const sendCode = debounce(async () => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(form.value.phone)) {
    alert('手机号格式不正确')
    return
  }

  try {
    const response = await userApi.sendLoginCode(form.value.phone)
    const data = response as any
    if (data.code === 200) {
      alert('验证码已发送')
      startCountdown()
    } else {
      alert(data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    alert('发送验证码失败')
  }
}, 500)

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

const handleSubmit = debounce(async () => {
  if (userType.value === 'user') {
    if (!form.value.phone) {
      alert('请输入手机号')
      return
    }
    if (loginType.value === 'password' && !form.value.password) {
      alert('请输入密码')
      return
    }
    if (loginType.value === 'code' && !form.value.code) {
      alert('请输入验证码')
      return
    }
  } else {
    if (!form.value.adminUsername || !form.value.adminPassword) {
      alert('请输入管理员用户名和密码')
      return
    }
  }

  loading.value = true
  try {
    let response
    if (userType.value === 'user') {
      if (loginType.value === 'password') {
        response = await userApi.login({
          phone: form.value.phone,
          password: form.value.password,
          isRemember: form.value.remember
        })
      } else {
        response = await userApi.loginWithCode({
          phone: form.value.phone,
          phoneCode: form.value.code,
          isRemember: form.value.remember
        })
      }
    } else {
      response = await userApi.adminLogin({
        phone: form.value.adminUsername,
        password: form.value.adminPassword,
        isRemember: false
      })
    }

    const data = response as any
    if (data.code === 200) {
      localStorage.setItem('token', data.data?.token || '')
      localStorage.setItem('userId', data.data?.id?.toString() || '')
      localStorage.setItem('userType', userType.value)
      localStorage.setItem('username', data.data?.username || '')
      localStorage.setItem('avatar', data.data?.avatar || '')
      router.replace('/')
    } else {
      alert(data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    alert('登录失败')
  } finally {
    loading.value = false
  }
}, 500)
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 1rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 2.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  animation: cardEntry 0.6s ease-out;
}

@keyframes cardEntry {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
  letter-spacing: 2px;
}

.login-header p {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.type-selector {
  position: relative;
  display: flex;
  background: #f0f0f0;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 2rem;
}

.selector-track {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.selector-track.admin {
  transform: translateX(100%);
}

.selector-btn {
  flex: 1;
  padding: 0.875rem;
  border: none;
  background: none;
  font-size: 0.95rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: color 0.3s ease;
}

.selector-btn.active {
  color: #667eea;
}

.login-type-selector {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.type-btn {
  flex: 1;
  padding: 0.5rem;
  border: none;
  background: none;
  font-size: 0.85rem;
  color: #999;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.type-btn::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.type-btn.active {
  color: #667eea;
  font-weight: 600;
}

.type-btn.active::after {
  width: 100%;
}

.login-content {
  min-height: 200px;
}

.form-section {
  animation: contentFade 0.3s ease-out;
}

@keyframes contentFade {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.9rem 1rem;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #fafafa;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.code-group input {
  text-align: center;
  letter-spacing: 4px;
  font-size: 1.2rem;
}

.code-input-row {
  display: flex;
  gap: 0.75rem;
}

.code-input-row input {
  flex: 1;
  text-align: left;
  letter-spacing: normal;
  font-size: 1rem;
}

.code-btn {
  padding: 0.9rem 1rem;
  border: 1.5px solid #667eea;
  border-radius: 10px;
  background: #fff;
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: 100px;
}

.code-btn:hover:not(:disabled) {
  background: #667eea;
  color: #fff;
}

.code-btn:disabled {
  border-color: #ccc;
  color: #999;
  cursor: not-allowed;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666;
}

.remember-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1.5rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.loading-dots span {
  animation: loadingDot 1.4s infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loadingDot {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.form-footer {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #666;
}

.form-footer .link {
  color: #666;
  text-decoration: none;
  transition: color 0.3s ease;
}

.form-footer .link span {
  color: #667eea;
  font-weight: 500;
}

.form-footer .link:hover span {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    padding: 2rem 1.5rem;
    border-radius: 20px;
  }

  .login-header h1 {
    font-size: 1.8rem;
  }

  .selector-btn {
    padding: 0.75rem 0.5rem;
    font-size: 0.85rem;
  }

  .code-input-row {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }
}
</style>