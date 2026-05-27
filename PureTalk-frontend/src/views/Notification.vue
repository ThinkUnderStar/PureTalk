<template>
  <div class="page-wrapper">
    <button class="back-btn" @click="goBack">← 返回</button>
    <div class="notification-container">
      <div class="notification-header">
        <div class="header-center">
          <h2>我的通知</h2>
          <p class="header-subtitle">共 {{ notifications.length }} 条通知</p>
        </div>
        <div class="header-right">
          <button
            v-if="notifications.length > 0"
            class="mark-all-btn"
            @click="markAllRead"
          >
          全部标为已读
        </button>
        </div>
      </div>

    <div class="notification-list" v-if="notifications.length > 0">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        :class="['notification-item', { unread: !notification.isRead }]"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-icon" :class="notification.type" v-html="getNotificationIcon(notification.type)">
        </div>
        <div class="notification-content">
          <p class="notification-text">{{ notification.content }}</p>
          <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
        </div>
        <button
          class="delete-btn"
          @click.stop="deleteNotification(notification.id)"
        >
          ×
        </button>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon iconfont icon-tongzhi"></div>
      <p>暂无通知</p>
    </div>

    <div v-if="loading" class="loading-indicator">
      <div class="spinner"></div>
    </div>

    <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
      加载更多
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi, Notification } from '@/api/notification'
import { debounce } from '@/utils/debounce'
import { getUserId } from '@/api/user'

const router = useRouter()
const notifications = ref<Notification[]>([])
const loading = ref(false)
const currentPage = ref(1)
const hasMore = ref(true)

const goBack = () => {
  router.back()
}

const getNotificationIcon = (type: string) => {
  const icons: Record<string, string> = {
    like: '❤️',
    comment: '💬',
    follow: '👤',
    system: '<span class="iconfont icon-tongzhi"></span>',
    reply: '↩️'
  }
  return icons[type] || '🔔'
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString()
}

const fetchNotifications = async (page = 1) => {
  const userId = getUserId()
  if (!userId) {
    router.push('/login')
    return
  }

  try {
    loading.value = true
    const response = await notificationApi.getNotifications(page, 20)
    const data = response as any
    if (data.code === 200) {
      if (page === 1) {
        notifications.value = data.data.records
      } else {
        notifications.value.push(...data.data.records)
      }
      hasMore.value = data.data.current < data.data.pages
      currentPage.value = page
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = debounce(() => {
  if (hasMore.value && !loading.value) {
    fetchNotifications(currentPage.value + 1)
  }
}, 500)

const handleNotificationClick = debounce(async (notification: Notification) => {
  if (!notification.isRead) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  if (notification.relatedId && notification.relatedType === 'post') {
    router.push(`/post/${notification.relatedId}`)
  }
}, 300)

const markAllRead = debounce(async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
  } catch (error) {
    console.error('标记全部已读失败:', error)
  }
}, 500)

const deleteNotification = debounce(async (id: number) => {
  try {
    await notificationApi.deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
  } catch (error) {
    console.error('删除通知失败:', error)
  }
}, 300)

onMounted(() => {
  fetchNotifications()
})
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

.notification-container {
  padding: 1rem;
  background: rgba(167, 243, 208, 0.95);
  backdrop-filter: blur(20px);
  width: 85%;
  max-height: 80vh;
  overflow: hidden;
  border-radius: 36px;
  border: 3px solid rgba(74, 222, 128, 0.4);
  box-shadow: 0 12px 40px rgba(167, 243, 208, 0.3);
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 8px;
  margin-bottom: 0.75rem;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.12);
}

.header-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-right {
  display: flex;
  justify-content: flex-end;
}

.notification-header h2 {
  font-size: 1.3rem;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.header-subtitle {
  font-size: 0.8rem;
  color: #888;
  margin: 0.25rem 0 0 0;
  font-weight: 400;
}

.back-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.3);
}

.mark-all-btn {
  padding: 0.45rem 1rem;
  border: 2px solid rgba(102, 126, 234, 0.5);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
}

.mark-all-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 0.8rem 1.25rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 8px;
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.18);
  border-color: rgba(102, 126, 234, 0.25);
}

.notification-item.unread {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06) 0%, rgba(118, 75, 162, 0.06) 100%);
  border-left: 3px solid #667eea;
}

.notification-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
  margin-right: 1rem;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  border: 2px solid rgba(102, 126, 234, 0.3);
}

.notification-icon.like {
  background: linear-gradient(135deg, #ffe0e6, #ffccd5);
}

.notification-icon.comment {
  background: linear-gradient(135deg, #e0f0ff, #d4e9ff);
}

.notification-icon.follow {
  background: linear-gradient(135deg, #e0ffe0, #d4ffd4);
}

.notification-icon.system {
  background: linear-gradient(135deg, #fff4e0, #ffe8cc);
}

.notification-icon.reply {
  background: linear-gradient(135deg, #f0e0ff, #e6d4ff);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 0.95rem;
  line-height: 1.6;
  font-weight: 500;
}

.notification-time {
  font-size: 0.82rem;
  color: #667eea;
  font-weight: 500;
}

.delete-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 1.3rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 0.75rem;
  flex-shrink: 0;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  transform: scale(1.1);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
}

.empty-icon {
  font-size: 4.5rem;
  margin-bottom: 1rem;
  color: #667eea;
}

.empty-state p {
  color: #666;
  font-size: 1.1rem;
  font-weight: 500;
}

.loading-indicator {
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.load-more {
  text-align: center;
  padding: 1rem;
  color: #667eea;
  font-size: 0.95rem;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  margin-top: 0.75rem;
  transition: all 0.3s ease;
  font-weight: 600;
}

.load-more:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

@media (max-width: 768px) {
  .notification-header {
    padding: 1rem;
  }

  .notification-header h2 {
    font-size: 1.25rem;
  }

  .notification-item {
    padding: 1rem;
  }

  .notification-icon {
    width: 42px;
    height: 42px;
    font-size: 1.1rem;
    margin-right: 0.75rem;
  }

  .notification-text {
    font-size: 0.9rem;
  }
}
</style>