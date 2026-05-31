<template>
  <div class="user-posts">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>{{ isOwnProfile ? '我的帖子' : `${userInfo.username}的帖子` }}</h1>
      <div class="header-actions"></div>
    </header>

    <main class="main">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-wrapper">
            <img
              :src="avatar || 'https://ui-avatars.com/api/?name=' + userInfo.username + '&background=random&size=128'"
              :alt="userInfo.username"
              class="profile-avatar"
            />
          </div>
          <h2>{{ userInfo.username }}</h2>
        </div>
      </div>

      <div class="posts-section">
        <div class="posts-header">
          <h3>全部帖子</h3>
        </div>
        <div class="posts-list">
          <div
            v-for="post in userPosts"
            :key="post.id"
            class="post-item"
            @click="goToPostDetail(post.id)"
          >
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-meta">
              <span class="post-time">{{ post.createTime }}</span>
              <button
                v-if="isOwnProfile"
                class="delete-btn"
                @click.stop="deletePost(post.id)"
              >
                删除
              </button>
            </div>
          </div>
          <div v-if="loadingPosts" class="loading">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-if="!loadingPosts && userPosts.length === 0" class="no-posts">
            暂无帖子
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { postApi, Post } from '@/api/post'

const router = useRouter()
const route = useRoute()

const userInfo = ref({
  username: '',
  userId: 0
})
const avatar = ref<string>('')
const userPosts = ref<Post[]>([])
const loadingPosts = ref<boolean>(false)

const isOwnProfile = computed(() => {
  const routeUserId = Number(route.params.userId)
  const currentUserId = Number(localStorage.getItem('userId') || '0')
  return !route.params.userId || routeUserId === currentUserId
})

const goBack = () => {
  router.back()
}

const loadUserPosts = async () => {
  loadingPosts.value = true
  try {
    const userId = isOwnProfile.value ? Number(localStorage.getItem('userId') || '0') : Number(route.params.userId)
    const response = await postApi.getUserPosts(userId, 1, 100)
    const data = response as any
    if (data.code === 200) {
      userPosts.value = data.data.posts.records.reverse()
      if (!isOwnProfile.value && data.data.userName) {
        userInfo.value.username = data.data.userName
        avatar.value = data.data.avatar || ''
      }
    }
  } catch (error) {
    console.error('加载用户帖子失败:', error)
  } finally {
    loadingPosts.value = false
  }
}

const goToPostDetail = (postId: number) => {
  router.push(`/post/${postId}`)
}

const deletePost = async (postId: number) => {
  if (!confirm('确定要删除这个帖子吗？')) return
  try {
    const response = await postApi.deleteMyPost(postId)
    const data = response as any
    if (data.code === 200) {
      userPosts.value = userPosts.value.filter(p => p.id !== postId)
      alert('删除成功')
    } else {
      alert(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除帖子失败:', error)
    alert('删除失败')
  }
}

onMounted(() => {
  if (isOwnProfile.value) {
    userInfo.value.username = localStorage.getItem('username') || ''
    avatar.value = localStorage.getItem('avatar') || ''
    userInfo.value.userId = Number(localStorage.getItem('userId') || '0')
  } else {
    const userId = Number(route.params.userId)
    userInfo.value.userId = userId
    userInfo.value.username = `用户${userId}`
  }
  loadUserPosts()
})
</script>

<style scoped>
.user-posts {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.header h1 {
  color: #fff;
  font-size: 1.3rem;
  margin: 0;
}

.header-actions {
  width: 80px;
}

.main {
  padding: 1.5rem;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.18);
  padding: 1.5rem;
  margin-bottom: 1.25rem;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.avatar-wrapper {
  position: relative;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(102, 126, 234, 0.3);
}

.profile-header h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.posts-section {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.18);
  padding: 1.5rem;
}

.posts-header {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1.5px solid rgba(102, 126, 234, 0.12);
}

.posts-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.posts-list .post-item {
  padding: 1rem;
  background: rgba(102, 126, 234, 0.04);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.posts-list .post-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  transform: translateX(4px);
}

.posts-list .post-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.5rem 0;
}

.posts-list .post-content {
  font-size: 0.92rem;
  color: #666;
  margin: 0 0 0.75rem 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.posts-list .post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.posts-list .post-time {
  font-size: 0.82rem;
  color: #999;
}

.posts-list .post-meta .delete-btn {
  padding: 0.3rem 0.6rem;
  font-size: 0.8rem;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: 6px;
  color: #e74c3c;
  cursor: pointer;
  transition: all 0.3s ease;
}

.posts-list .post-meta .delete-btn:hover {
  background: #e74c3c;
  color: #fff;
  border-color: #e74c3c;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  color: #667eea;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading p {
  margin: 0.5rem 0 0 0;
  font-size: 0.9rem;
}

.no-posts {
  text-align: center;
  color: #999;
  padding: 2rem;
  font-size: 0.95rem;
}
</style>