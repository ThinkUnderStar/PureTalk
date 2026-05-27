<template>
  <div class="user-profile">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>PureTalk</h1>
      <div class="header-actions">
        <button class="logout-btn" @click="handleLogout">登出</button>
      </div>
    </header>

    <main class="main">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-wrapper">
            <img
              :src="avatar || 'https://ui-avatars.com/api/?name=' + userInfo.username + '&background=random&size=128'"
              :alt="userInfo.username"
              class="profile-avatar"
              @click="viewFullAvatar"
            />
          </div>
          <h2>{{ userInfo.username }}</h2>
          <button class="change-avatar-btn" @click="triggerAvatarUpload">
            更改头像
          </button>
          <input
            type="file"
            ref="avatarInput"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
          <div v-if="uploadingAvatar" class="avatar-uploading">上传中...</div>
        </div>

        <div class="profile-stats">
          <div class="stat-item">
            <span class="stat-value">{{ userInfo.likeCount || 0 }}</span>
            <span class="stat-label">获赞</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ userInfo.postCount || 0 }}</span>
            <span class="stat-label">帖子</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ userInfo.commentCount || 0 }}</span>
            <span class="stat-label">评论</span>
          </div>
        </div>

        <div class="profile-menu">
          <button class="menu-item" @click="showEditForm = true">
            <span class="menu-icon">✏️</span>
            <span class="menu-text">修改信息</span>
            <span class="menu-arrow">›</span>
          </button>
          <router-link to="/notification" class="menu-item">
            <span class="menu-icon iconfont icon-tongzhi"></span>
            <span class="menu-text">我的通知</span>
            <span class="menu-arrow">›</span>
          </router-link>
          <router-link to="/feedback" class="menu-item">
            <span class="menu-icon iconfont icon-fankui"></span>
            <span class="menu-text">意见反馈</span>
            <span class="menu-arrow">›</span>
          </router-link>
        </div>

        <div v-if="showEditForm" class="edit-section">
          <h3>修改个人信息</h3>
          <div class="profile-form">
            <div class="form-group">
              <label for="username">用户名</label>
              <input
                type="text"
                id="username"
                v-model="form.username"
                placeholder="请输入用户名"
              />
              <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
            </div>

            <div class="form-group">
              <label for="phone">手机号</label>
              <input
                type="tel"
                id="phone"
                v-model="form.phone"
                placeholder="请输入手机号"
              />
              <span v-if="errors.phone" class="error-text">{{ errors.phone }}</span>
            </div>

            <div class="form-group">
              <label for="email">邮箱</label>
              <input
                type="email"
                id="email"
                v-model="form.email"
                placeholder="请输入邮箱"
              />
              <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
            </div>

            <div class="form-group">
              <label for="password">密码（选填）</label>
              <input
                type="password"
                id="password"
                v-model="form.password"
                placeholder="请输入新密码，不修改则留空"
              />
              <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
            </div>

            <div class="form-actions">
              <button class="cancel-btn" @click="showEditForm = false">取消</button>
              <button
                class="update-btn"
                :disabled="loading"
                @click="handleUpdate"
              >
                {{ loading ? '更新中...' : '确认修改' }}
              </button>
            </div>
          </div>
        </div>

        <div class="danger-zone">
          <h3>危险操作</h3>
          <button class="delete-btn" @click="confirmDelete">删除账户</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'

const router = useRouter()
const loading = ref<boolean>(false)
const showEditForm = ref<boolean>(false)
const avatar = ref<string>('')
const uploadingAvatar = ref<boolean>(false)
const avatarInput = ref<HTMLInputElement | null>(null)
const userInfo = ref({
  username: '',
  likeCount: 0,
  postCount: 0,
  commentCount: 0
})
const form = ref({
  username: '',
  phone: '',
  email: '',
  password: ''
})
const errors = ref({
  username: '',
  phone: '',
  email: '',
  password: ''
})

const viewFullAvatar = () => {
  if (avatar.value) {
    window.open(avatar.value, '_blank')
  }
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await userApi.uploadAvatar(formData)
    const data = response as any
    if (data.code === 200) {
      avatar.value = data.data
      localStorage.setItem('avatar', data.data)
      alert('头像上传成功')
    } else {
      alert(data.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    alert('头像上传失败')
  } finally {
    uploadingAvatar.value = false
    target.value = ''
  }
}

const validateForm = (): boolean => {
  let isValid = true
  errors.value = { username: '', phone: '', email: '', password: '' }

  if (!form.value.username || form.value.username.trim().length < 2) {
    errors.value.username = '用户名至少2个字符'
    isValid = false
  }

  if (form.value.username && form.value.username.length > 20) {
    errors.value.username = '用户名不能超过20个字符'
    isValid = false
  }

  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    errors.value.phone = '请输入有效的手机号'
    isValid = false
  }

  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = '请输入有效的邮箱'
    isValid = false
  }

  if (form.value.password && form.value.password.length < 6) {
    errors.value.password = '密码至少6个字符'
    isValid = false
  }

  return isValid
}

onMounted(() => {
  userInfo.value.username = localStorage.getItem('username') || ''
  avatar.value = localStorage.getItem('avatar') || ''
})

const handleUpdate = async () => {
  if (!validateForm()) {
    return
  }
  loading.value = true
  try {
    const updateData: any = {}
    if (form.value.username) updateData.username = form.value.username
    if (form.value.phone) updateData.phone = form.value.phone
    if (form.value.email) updateData.email = form.value.email
    if (form.value.password) updateData.password = form.value.password

    const response = await userApi.update(updateData)
    const data = response as any
    if (data.code === 200) {
      if (form.value.username) {
        localStorage.setItem('username', form.value.username)
        userInfo.value.username = form.value.username
      }
      alert('更新成功')
      showEditForm.value = false
      form.value.password = ''
    } else {
      alert(data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    alert('更新失败')
  } finally {
    loading.value = false
  }
}

const confirmDelete = () => {
  if (confirm('确定要删除账户吗？此操作不可恢复。')) {
    handleDelete()
  }
}

const handleDelete = async () => {
  try {
    const response = await userApi.deleteAccount()
    const data = response as any
    if (data.code === 200) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      router.push('/login')
    } else {
      alert(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败')
  }
}

const handleLogout = async () => {
  try {
    const response = await userApi.logout()
    const data = response as any
    if (data.code === 200) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      router.push('/login')
    } else {
      alert(data.message || '登出失败')
    }
  } catch (error) {
    console.error('登出失败:', error)
    alert('登出失败')
  }
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.header {
  background: linear-gradient(135deg, #ec4899 0%, #a855f7 50%, #8b5cf6 100%);
  padding: 0.65rem 1.5rem;
  box-shadow: 0 4px 20px rgba(168, 85, 247, 0.25);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  background: rgba(255, 255, 255, 0.25);
  border: 2px solid rgba(255, 255, 255, 0.4);
  font-size: 1.1rem;
  cursor: pointer;
  color: #fff;
  padding: 0.6rem 1.2rem;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(168, 85, 247, 0.35);
}

.header h1 {
  font-size: 1.8rem;
  font-weight: bold;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logout-btn {
  padding: 0.6rem 1.2rem;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(168, 85, 247, 0.35);
}

.main {
  flex: 1;
  padding: 1.25rem;
  max-width: 720px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.profile-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.18);
  padding: 1.5rem;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1.25rem;
}

.profile-avatar {
  width: 85px;
  height: 85px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 0.75rem;
  border: 3px solid rgba(102, 126, 234, 0.4);
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.18);
  transition: all 0.3s ease;
}

.avatar-wrapper {
  margin-bottom: 0.5rem;
}

.profile-avatar {
  cursor: pointer;
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.profile-header h2 {
  font-size: 1.6rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.75rem 0;
}

.change-avatar-btn {
  background: none;
  border: 2px solid rgba(102, 126, 234, 0.4);
  color: #667eea;
  font-size: 0.9rem;
  cursor: pointer;
  margin-top: 0.5rem;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.change-avatar-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
}

.avatar-uploading {
  font-size: 0.85rem;
  color: #667eea;
  margin-top: 0.25rem;
  font-weight: 500;
}

.error-text {
  color: #667eea;
  font-size: 0.82rem;
  margin-top: 0.25rem;
  display: block;
  font-weight: 500;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 2.25rem;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 1.5px solid rgba(102, 126, 234, 0.12);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 0.82rem;
  color: #667eea;
  font-weight: 500;
}

.profile-menu {
  margin-bottom: 1.25rem;
}

.menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0.85rem 1rem;
  border: none;
  background: rgba(102, 126, 234, 0.04);
  border-radius: 10px;
  cursor: pointer;
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
  margin-bottom: 0.35rem;
}

.menu-item:first-child {
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.menu-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  transform: translateX(4px);
}

.menu-icon {
  font-size: 1.3rem;
  margin-right: 1rem;
}

.menu-text {
  flex: 1;
  text-align: left;
  font-size: 1rem;
  font-weight: 500;
}

.menu-arrow {
  font-size: 1.3rem;
  color: #667eea;
  font-weight: bold;
}

.edit-section {
  margin-bottom: 1.25rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06), rgba(118, 75, 162, 0.06));
  border-radius: 14px;
  border: 1.5px solid rgba(102, 126, 234, 0.12);
}

.edit-section h3 {
  font-size: 1.05rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  text-align: center;
}

.profile-form {
  margin-bottom: 0;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  color: #333;
  font-size: 0.85rem;
  font-weight: 600;
}

.form-group input {
  width: 100%;
  padding: 0.65rem 0.85rem;
  border: 1.5px solid rgba(102, 126, 234, 0.25);
  border-radius: 10px;
  font-size: 0.92rem;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.cancel-btn {
  flex: 1;
  padding: 0.65rem;
  border: 1.5px solid rgba(102, 126, 234, 0.4);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
}

.cancel-btn:hover {
  background: rgba(102, 126, 234, 0.08);
}

.update-btn {
  flex: 1;
  padding: 0.65rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.3);
}

.update-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}

.update-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.danger-zone {
  border-top: 1.5px solid rgba(102, 126, 234, 0.12);
  padding-top: 1.25rem;
}

.danger-zone h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.delete-btn {
  width: 100%;
  padding: 0.65rem;
  border: 1.5px solid #e74c3c;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.95);
  color: #e74c3c;
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: #e74c3c;
  color: #fff;
  border-color: transparent;
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(231, 76, 60, 0.35);
}

@media (max-width: 768px) {
  .header {
    padding: 0.8rem 1rem;
  }

  .header h1 {
    font-size: 1.4rem;
  }

  .back-btn,
  .logout-btn {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }

  .main {
    padding: 1rem;
  }

  .profile-card {
    padding: 1.5rem;
  }

  .profile-stats {
    gap: 2rem;
  }

  .stat-value {
    font-size: 1.4rem;
  }

  .menu-item {
    padding: 0.9rem;
  }

  .edit-section {
    padding: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
