<template>
  <div class="home">
    <header class="header" :class="{ collapsed: isHeaderCollapsed }">
      <div class="header-left">
        <h1>PureTalk</h1>
        <button class="header-toggle" @click="toggleHeader">
          <span class="toggle-icon" v-if="isHeaderCollapsed">☰</span>
          <span class="toggle-icon" v-else><span class="iconfont icon-shouqi"></span></span>
        </button>
      </div>
      <div class="header-content">
        <div class="header-actions">
          <button v-if="isLoggedIn" class="btn primary-btn" @click="showPostModal = true">发布帖子</button>
          <template v-if="!isLoggedIn">
            <router-link to="/login" class="btn">登录</router-link>
            <router-link to="/register" class="btn">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/ai/chat" class="btn icon-btn ai-btn">🤖</router-link>
            <router-link to="/notification" class="btn icon-btn"><span class="iconfont icon-tongzhi"></span></router-link>
            <router-link to="/feedback" class="btn icon-btn"><span class="iconfont icon-fankui"></span></router-link>
            <router-link to="/user/profile" class="btn user-profile-btn">
              <img
                :src="userAvatar || 'https://ui-avatars.com/api/?name=' + username + '&background=random&size=64'"
                :alt="username"
                class="header-avatar"
              />
              <span>{{ username }}</span>
            </router-link>
            <router-link to="/admin" v-if="userType === 'admin'" class="btn">管理中心</router-link>
          </template>
        </div>
      </div>
    </header>
    
    <main class="main">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <span class="search-icon">🔍</span>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索帖子..." 
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <button 
            v-if="searchKeyword" 
            class="clear-btn" 
            @click="clearSearch"
          >
            ✕
          </button>
        </div>
        <button 
          :class="['search-btn', { active: isSearching }]" 
          @click="handleSearch"
        >
          搜索
        </button>
      </div>
      
      <div class="category-tabs">
        <button 
          :class="['tab', { active: currentCategory === 'all' }]"
          @click="switchCategory('all')"
        >
          综合
        </button>
        <button 
          :class="['tab', { active: currentCategory === 'hot' }]"
          @click="switchCategory('hot')"
        >
          最热
        </button>
        <button 
          :class="['tab', { active: currentCategory === 'latest' }]"
          @click="switchCategory('latest')"
        >
          最新
        </button>
      </div>
      
      <div class="post-list" ref="postListRef" @scroll="handleScroll">
        <div 
          v-for="post in posts" 
          :key="post.id" 
          class="post-item"
          @click="goToPostDetail(post.id)"
        >
          <h2 class="post-title">{{ post.title }}</h2>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-meta">
            <span class="post-author">{{ post.username }}</span>
            <span class="post-time">{{ post.createTime }}</span>
            <button 
              v-if="isLoggedIn && post.userId === currentUserId" 
              class="delete-btn"
              @click="deletePost(post.id)"
            >
              删除
            </button>
          </div>
          <div class="post-stats">
              <span class="stat-item">
                <span class="stat-icon iconfont icon-dianzan"></span>
                <span>{{ post.likeCount }}</span>
              </span>
              <span class="stat-item">
                <span class="stat-icon iconfont icon-liulan"></span>
                <span>{{ post.viewCount }}</span>
              </span>
              <span class="stat-item">
                <span class="stat-icon iconfont icon-pinglun"></span>
                <span>{{ post.commentCount }}</span>
              </span>
            </div>
          </div>
        
        <div v-if="loading" class="loading">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        
        <div v-if="hasMore === false && posts.length > 0" class="no-more">
          没有更多内容了
        </div>
      </div>
    </main>
    
    <!-- 发布帖子模态框 -->
    <div v-if="showPostModal" class="modal-overlay" @click="closePostModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>发布帖子</h2>
          <button class="close-btn" @click="closePostModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitPost">
            <div class="form-group">
              <label for="post-title">标题</label>
              <input 
                type="text" 
                id="post-title" 
                v-model="postForm.title" 
                placeholder="请输入帖子标题"
                required
              />
            </div>
            <div class="form-group">
              <label for="post-content">内容</label>
              <textarea 
                id="post-content" 
                v-model="postForm.content" 
                placeholder="请输入帖子内容"
                rows="6"
                required
              ></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn" @click="closePostModal">取消</button>
              <button type="submit" class="btn primary-btn" :disabled="submittingPost">
                {{ submittingPost ? '发布中...' : '发布' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, Post } from '@/api/post'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const postListRef = ref<HTMLElement | null>(null)
const posts = ref<Post[]>([])
const currentCategory = ref<string>('all')
const currentPage = ref<number>(1)
const loading = ref<boolean>(false)
const hasMore = ref<boolean>(true)
const isLoggedIn = ref<boolean>(!!localStorage.getItem('token'))
const userType = ref<string>(localStorage.getItem('userType') || 'user')
const username = ref<string>(localStorage.getItem('username') || '用户')
const userAvatar = ref<string>(localStorage.getItem('avatar') || '')
const currentUserId = ref<number>(Number(localStorage.getItem('userId') || '0'))
const showPostModal = ref<boolean>(false)
const submittingPost = ref<boolean>(false)
const postForm = ref({
  title: '',
  content: ''
})
const searchKeyword = ref<string>('')
const isSearching = ref<boolean>(false)
const isHeaderCollapsed = ref<boolean>(true)

const toggleHeader = () => {
  isHeaderCollapsed.value = !isHeaderCollapsed.value
}

const loadPosts = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    let response
    const categoryId = currentCategory.value === 'hot' ? 1 : currentCategory.value === 'all' ? 0 : 2
    
    if (isSearching.value && searchKeyword.value.trim()) {
      response = await postApi.searchPosts(searchKeyword.value.trim(), categoryId, currentPage.value, 20)
    } else {
      response = await postApi.getPosts(categoryId, currentPage.value, 20)
    }
    
    const data = response as any
    if (data.code === 200) {
      const newPosts = data.data?.records || []
      posts.value = currentPage.value === 1 ? newPosts : [...posts.value, ...newPosts]
      hasMore.value = newPosts.length === 20
      currentPage.value++
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value = false
  }
}

const switchCategory = (category: string) => {
  currentCategory.value = category
  currentPage.value = 1
  posts.value = []
  hasMore.value = true
  loadPosts()
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    alert('请输入搜索关键词')
    return
  }
  isSearching.value = true
  currentPage.value = 1
  posts.value = []
  hasMore.value = true
  loadPosts()
}

const clearSearch = () => {
  searchKeyword.value = ''
  isSearching.value = false
  currentPage.value = 1
  posts.value = []
  hasMore.value = true
  loadPosts()
}

const goToPostDetail = (postId: number) => {
  router.push(`/post/${postId}`)
}

const handleScroll = debounce(() => {
  if (!postListRef.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = postListRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100 && !loading.value && hasMore.value) {
    loadPosts()
  }
}, 200)

const closePostModal = () => {
  showPostModal.value = false
  // 重置表单
  postForm.value = {
    title: '',
    content: ''
  }
  submittingPost.value = false
}

const deletePost = async (postId: number) => {
  if (!confirm('确定要删除这篇帖子吗？')) {
    return
  }
  
  try {
    const response = await postApi.deleteMyPost(postId)
    const data = response as any
    if (data.code === 200) {
      alert('删除成功')
      // 重新加载帖子列表
      currentPage.value = 1
      posts.value = []
      hasMore.value = true
      loadPosts()
    } else {
      alert(data.msg || '删除失败')
    }
  } catch (error: any) {
    console.error('删除帖子失败:', error)
    if (error.msg) {
      alert('删除失败: ' + error.msg)
    } else {
      alert('删除失败，请稍后重试')
    }
  }
}

const submitPost = async () => {
  if (!postForm.value.title.trim() || !postForm.value.content.trim()) {
    alert('请填写标题和内容')
    return
  }
  
  submittingPost.value = true
  try {
    const response = await postApi.sendPost({
      title: postForm.value.title,
      content: postForm.value.content
    })
    const data = response as any
    if (data.code === 200) {
      alert('发布成功')
      closePostModal()
      currentPage.value = 1
      posts.value = []
      hasMore.value = true
      loadPosts()
    } else {
      alert(data.msg || '发布失败')
    }
  } catch (error: any) {
    console.error('发布帖子失败:', error)
    if (error.msg) {
      alert('发布失败: ' + error.msg)
    } else {
      alert('发布失败，请稍后重试')
    }
  } finally {
    submittingPost.value = false
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.home {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: linear-gradient(135deg, #0891b2 0%, #06b6d4 50%, #22d3d1 100%);
  padding: 1.5rem 2.5rem;
  box-shadow: 0 2px 10px rgba(6, 182, 212, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.header.collapsed {
  padding: 0.5rem 1rem;
}

.header.collapsed .header-content {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  font-size: 1.8rem;
  color: #fff;
  font-weight: bold;
  margin: 0;
}

.header-toggle {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  transition: background 0.3s ease;
}

.header-toggle:hover {
  background: rgba(255, 255, 255, 0.3);
}

.toggle-icon {
  font-size: 1rem;
}

.header-content {
  transition: all 0.3s ease;
  max-height: 100px;
  opacity: 1;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  text-decoration: none;
  color: #fff;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.1);
}

.btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.primary-btn {
  background-color: #fff;
  color: #3b82f6;
  border: 2px solid #fff;
  font-weight: 600;
}

.primary-btn:hover {
  background-color: rgba(255, 255, 255, 0.9);
  color: #1e3a8a;
  border-color: #fff;
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  overflow: hidden;
  margin: 0 auto;
  width: 100%;
}

.user-profile-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.8rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px !important;
  text-decoration: none;
  font-size: 0.9rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: none !important;
}

.user-profile-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.header-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 1rem;
  font-size: 1rem;
  color: #999;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-btn {
  position: absolute;
  right: 1rem;
  background: none;
  border: none;
  color: #999;
  font-size: 0.85rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: color 0.2s ease;
}

.clear-btn:hover {
  color: #666;
}

.search-btn {
  padding: 0.75rem 1.5rem;
  border: 2px solid #667eea;
  border-radius: 25px;
  background: #fff;
  color: #667eea;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.search-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
}

.category-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 0.5rem;
}

.tab {
  padding: 0.5rem 1rem;
  border: none;
  background: none;
  font-size: 1rem;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab:hover {
  color: #333;
}

.tab.active {
  color: #333;
  border-bottom-color: #4CAF50;
  font-weight: bold;
}

.post-list {
  background-color: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(15px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 1rem;
  height: calc(100% - 120px);
  overflow-y: auto;
}

.post-item {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-item:hover {
  background-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.post-item:last-child {
  border-bottom: none;
}

.post-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.post-content {
  font-size: 0.95rem;
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: #999;
}

.post-author {
  font-weight: 500;
  color: #666;
}

.post-stats {
  display: flex;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.stat-icon {
  font-size: 1rem;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-more {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-size: 0.9rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.modal-content .primary-btn {
  background-color: #3b82f6;
  color: #fff;
  border: 2px solid #3b82f6;
  padding: 0.6rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.modal-content .primary-btn:hover:not(:disabled) {
  background-color: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

.modal-content .primary-btn:disabled {
  background-color: #93c5fd;
  border-color: #93c5fd;
  cursor: not-allowed;
}

.delete-btn {
  background-color: #ff4757;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: auto;
}

.delete-btn:hover {
  background-color: #ff3742;
}

@media (max-width: 768px) {
  .header {
    padding: 1rem;
  }
  
  .main {
    padding: 1rem;
  }
  
  .category-tabs {
    overflow-x: auto;
    white-space: nowrap;
  }
  
  .post-item {
    padding: 1rem;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions button {
    width: 100%;
  }
  
  .delete-btn {
    margin-left: 0.5rem;
  }
}
</style>