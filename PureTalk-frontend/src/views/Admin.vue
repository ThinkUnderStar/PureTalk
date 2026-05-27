<template>
  <div class="admin">
    <header class="header">
      <button class="back-btn" @click="handleBack">← 返回</button>
      <h1>管理中心</h1>
      <div class="header-actions">
        <button class="logout-btn" @click="handleLogout">登出</button>
      </div>
    </header>

    <main class="main">
      <div class="admin-card">
        <h2>管理员控制台</h2>

        <div class="admin-menu">
          <button class="menu-item" :class="{ active: activeMenu === 'users' }" @click="activeMenu = 'users'">
            用户管理
          </button>
          <button class="menu-item" :class="{ active: activeMenu === 'posts' }" @click="activeMenu = 'posts'">
            帖子管理
          </button>
          <button class="menu-item" :class="{ active: activeMenu === 'feedback' }" @click="activeMenu = 'feedback'">
            反馈管理
          </button>
          <button class="menu-item" :class="{ active: activeMenu === 'reports' }" @click="activeMenu = 'reports'">
            举报管理
          </button>
          <button class="menu-item" :class="{ active: activeMenu === 'settings' }" @click="activeMenu = 'settings'">
            系统设置
          </button>
          <button class="menu-item create-admin" @click="$router.push('/admin/create')">
            创建管理员
          </button>
        </div>

        <div class="menu-content">
          <div v-if="activeMenu === 'users'" class="content-section">
            <h3>用户管理</h3>
            <p>用户管理功能开发中...</p>
          </div>

          <div v-if="activeMenu === 'posts'" class="content-section">
            <h3>帖子管理</h3>
            <p>帖子管理功能开发中...</p>
          </div>

          <div v-if="activeMenu === 'feedback'" class="content-section">
            <h3>反馈管理</h3>
            <div v-if="feedbackLoading" class="loading">加载中...</div>
            <div v-else-if="feedbackList.length === 0" class="empty">暂无反馈</div>
            <div v-else class="list-container">
              <div v-for="item in feedbackList" :key="item.id" class="list-item">
                <div class="item-header">
                  <span class="item-type">{{ getFeedbackTypeLabel(item.type) }}</span>
                  <span class="item-time">{{ item.createTime }}</span>
                </div>
                <div class="item-content">{{ item.content }}</div>
                <div class="item-footer">
                  <span class="item-user">用户: {{ item.username }}</span>
                  <div class="item-actions">
                    <select v-model="item.handleStatus" class="status-select">
                      <option :value="0">待处理</option>
                      <option :value="1">处理中</option>
                      <option :value="2">已处理</option>
                    </select>
                    <button class="action-btn" @click="handleFeedback(item)">处理</button>
                  </div>
                </div>
              </div>
              <div v-if="feedbackHasMore" class="load-more" @click="loadMoreFeedback">加载更多</div>
            </div>
          </div>

          <div v-if="activeMenu === 'reports'" class="content-section">
            <h3>举报管理</h3>
            <div v-if="reportLoading" class="loading">加载中...</div>
            <div v-else-if="reportList.length === 0" class="empty">暂无举报</div>
            <div v-else class="list-container">
              <div v-for="item in reportList" :key="item.id" class="list-item">
                <div class="item-header">
                  <span class="item-type">{{ getReportTypeLabel(item.targetType) }}</span>
                  <span class="item-time">{{ item.createTime }}</span>
                </div>
                <div class="item-content">{{ item.reason }}</div>
                <div class="item-footer">
                  <span class="item-user">举报人: {{ item.username }}</span>
                  <div class="item-actions">
                    <select v-model="item.handleStatus" class="status-select">
                      <option :value="0">待处理</option>
                      <option :value="1">处理中</option>
                      <option :value="2">已处理</option>
                    </select>
                    <button class="action-btn" @click="handleReport(item)">处理</button>
                  </div>
                </div>
              </div>
              <div v-if="reportHasMore" class="load-more" @click="loadMoreReport">加载更多</div>
            </div>
          </div>

          <div v-if="activeMenu === 'settings'" class="content-section">
            <h3>系统设置</h3>
            <p>系统设置功能开发中...</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { feedbackApi, Feedback } from '@/api/feedback'
import { reportApi, Report } from '@/api/report'

const router = useRouter()
const activeMenu = ref<string>('users')

const feedbackList = ref<Feedback[]>([])
const feedbackLoading = ref(false)
const feedbackPage = ref(1)
const feedbackHasMore = ref(true)

const reportList = ref<Report[]>([])
const reportLoading = ref(false)
const reportPage = ref(1)
const reportHasMore = ref(true)

const handleBack = () => {
  // 直接返回首页，避免循环
  router.push('/')
}

const handleLogout = async () => {
  try {
    const response = await userApi.logout()
    const data = response as any
    if (data.code === 200) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('userType')
      localStorage.removeItem('username')
      router.push('/login')
    } else {
      alert(data.message || '登出失败')
    }
  } catch (error) {
    console.error('登出失败:', error)
    alert('登出失败')
  }
}

const getFeedbackTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    suggestion: '功能建议',
    bug: 'Bug反馈',
    experience: '体验问题',
    other: '其他'
  }
  return labels[type] || type
}

const getReportTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    post: '帖子举报',
    comment: '评论举报',
    user: '用户举报',
    other: '其他举报'
  }
  return labels[type] || type
}

const loadFeedbacks = async (page = 1) => {
  feedbackLoading.value = true
  try {
    const response = await feedbackApi.getFeedbacks(page, 10)
    const data = response as any
    if (data.code === 200) {
      if (page === 1) {
        feedbackList.value = data.data.records
      } else {
        feedbackList.value.push(...data.data.records)
      }
      feedbackHasMore.value = data.data.current < data.data.pages
      feedbackPage.value = page
    }
  } catch (error) {
    console.error('获取反馈列表失败:', error)
  } finally {
    feedbackLoading.value = false
  }
}

const loadMoreFeedback = () => {
  if (feedbackHasMore.value) {
    loadFeedbacks(feedbackPage.value + 1)
  }
}

const handleFeedback = async (item: Feedback) => {
  const handleContent = prompt('请输入处理结果:')
  if (!handleContent) return

  try {
    const response = await feedbackApi.handleFeedback(item.id, handleContent)
    const data = response as any
    if (data.code === 200) {
      alert('处理成功')
      loadFeedbacks(1)
    } else {
      alert(data.msg || '处理失败')
    }
  } catch (error) {
    console.error('处理反馈失败:', error)
    alert('处理失败')
  }
}

const loadReports = async (page = 1) => {
  reportLoading.value = true
  try {
    const response = await reportApi.getReports(page, 10)
    const data = response as any
    if (data.code === 200) {
      if (page === 1) {
        reportList.value = data.data.records
      } else {
        reportList.value.push(...data.data.records)
      }
      reportHasMore.value = data.data.current < data.data.pages
      reportPage.value = page
    }
  } catch (error) {
    console.error('获取举报列表失败:', error)
  } finally {
    reportLoading.value = false
  }
}

const loadMoreReport = () => {
  if (reportHasMore.value) {
    loadReports(reportPage.value + 1)
  }
}

const handleReport = async (item: Report) => {
  const handleResult = prompt('请输入处理结果:')
  if (!handleResult) return

  try {
    const response = await reportApi.handleReport(item.id, handleResult)
    const data = response as any
    if (data.code === 200) {
      alert('处理成功')
      loadReports(1)
    } else {
      alert(data.msg || '处理失败')
    }
  } catch (error) {
    console.error('处理举报失败:', error)
    alert('处理失败')
  }
}

onMounted(() => {
  loadFeedbacks()
  loadReports()
})
</script>

<style scoped>
.admin {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.header {
  background-color: #fff;
  padding: 1rem 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  flex: 1;
  margin: 0;
  font-size: 1.5rem;
  color: #333;
}

.back-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  background: #f0f0f0;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e0e0e0;
}

.logout-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  background: #ff4d4f;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #ff3335;
}

.main {
  flex: 1;
  padding: 2rem;
}

.admin-card {
  background: #fff;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.admin-card h2 {
  margin: 0 0 1.5rem 0;
  color: #333;
  font-size: 1.3rem;
}

.admin-menu {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  background: #f5f5f5;
  color: #666;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: #e8e8e8;
}

.menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.menu-item.create-admin {
  background: #fff;
  border: 1px solid #667eea;
  color: #667eea;
}

.menu-item.create-admin:hover {
  background: #667eea;
  color: #fff;
}

.content-section h3 {
  margin: 0 0 1rem 0;
  color: #333;
}

.loading, .empty {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.list-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.3s ease;
}

.list-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.item-type {
  background: #667eea;
  color: #fff;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
}

.item-time {
  color: #999;
  font-size: 0.85rem;
}

.item-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 0.75rem;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-user {
  color: #666;
  font-size: 0.85rem;
}

.item-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.status-select {
  padding: 0.4rem 0.6rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 0.85rem;
}

.action-btn {
  padding: 0.4rem 1rem;
  border: none;
  border-radius: 4px;
  background: #667eea;
  color: #fff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #5a6fd6;
}

.load-more {
  text-align: center;
  padding: 1rem;
  color: #667eea;
  cursor: pointer;
  background: #f5f5f5;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.load-more:hover {
  background: #667eea;
  color: #fff;
}

@media (max-width: 768px) {
  .header {
    padding: 1rem;
  }

  .main {
    padding: 1rem;
  }

  .admin-menu {
    flex-direction: column;
  }

  .menu-item {
    width: 100%;
    text-align: center;
  }

  .item-footer {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }

  .item-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>