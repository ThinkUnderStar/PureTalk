<template>
  <div class="post-detail">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>PureTalk</h1>
      <div class="header-actions">
        <router-link to="/notification" v-if="isLoggedIn" class="btn icon-btn"><span class="iconfont icon-tongzhi"></span></router-link>
        <router-link to="/login" v-if="!isLoggedIn" class="btn">登录</router-link>
        <router-link to="/user/profile" v-else class="btn user-profile-btn">
          <img
            :src="userAvatar || 'https://ui-avatars.com/api/?name=' + username + '&background=random&size=64'"
            :alt="username"
            class="header-avatar"
          />
          <span>{{ username }}</span>
        </router-link>
      </div>
    </header>
    
    <main class="main">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="post" class="post-content">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-meta">
          <div class="post-meta-left">
            <img 
              :src="post.avatar || 'https://ui-avatars.com/api/?name=' + post.userName + '&background=random'" 
              :alt="post.userName" 
              class="post-avatar cursor-pointer"
              @click="goToUserProfile(post.userId)"
            />
            <div class="post-meta-info">
              <span class="post-author cursor-pointer" @click="goToUserProfile(post.userId)">{{ post.userName }}</span>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <div class="post-meta-right">
            <button
              v-if="isLoggedIn && post.userId === currentUserId"
              class="delete-btn"
              @click="deletePost"
            >
              删除帖子
            </button>
            <button
              v-if="isLoggedIn"
              class="report-btn"
              @click="reportPost"
            >
              举报
            </button>
          </div>
          <div class="post-stats">
            <span class="stat-item" @click="likePost">
              <span :class="['stat-icon', 'iconfont', 'icon-dianzan', { liked: isLiked }]"></span>
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
        <div class="post-body">
          {{ post.content }}
        </div>
      </div>
      
      <div class="comment-section">
        <h3>评论</h3>
        
        <div class="comment-form" v-if="isLoggedIn">
          <textarea 
            v-model="commentContent" 
            placeholder="写下你的评论..."
            rows="3"
          ></textarea>
          <button 
            class="submit-comment-btn" 
            :disabled="!commentContent.trim() || submittingComment"
            @click="submitComment"
          >
            {{ submittingComment ? '提交中...' : '提交评论' }}
          </button>
        </div>
        
        <div class="comment-list" ref="commentListRef" @scroll="handleCommentScroll">
          <div 
            v-for="comment in comments" 
            :key="comment.id" 
            class="comment-item"
            @click="showReplyInput(comment.id, comment.userName)"
          >
            <div class="comment-header">
              <img 
                :src="comment.avatar || 'https://ui-avatars.com/api/?name=' + comment.userName + '&background=random'" 
                :alt="comment.userName" 
                class="comment-avatar cursor-pointer"
                @click.stop="goToUserProfile(comment.userId)"
              />
              <div class="comment-header-info">
                <span class="comment-author cursor-pointer" @click.stop="goToUserProfile(comment.userId)">{{ comment.userName }}</span>
                <div class="comment-header-actions">
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <button
                    v-if="isLoggedIn && comment.userId === currentUserId"
                    class="comment-delete-btn"
                    @click.stop="deleteComment(comment.id)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
            <div class="comment-body">
              <div v-if="comment.parentId && comment.parentId !== 0" class="reply-indicator">
                回复 @{{ comment.replyUserName || '未知用户' }}
              </div>
              {{ comment.content }}
            </div>
            <div class="comment-footer">
              <span class="comment-like" @click.stop="likeComment(comment.id)">
                <span class="like-icon">{{ comment.isLiked ? '❤️' : '👍' }}</span>
                <span>{{ comment.likeCount }}</span>
              </span>
              <span class="comment-reply-btn" @click.stop="showReplyInput(comment.id, comment.userName)">
                回复
              </span>
            </div>
            
            <div v-if="replyToCommentId === comment.id" class="reply-input-box" @click.stop>
              <div class="reply-header">
                <span>回复 @{{ replyToUsername }}</span>
                <button class="cancel-reply-btn" @click="cancelReply">取消</button>
              </div>
              <textarea 
                v-model="replyContent" 
                :placeholder="`回复 @${replyToUsername}...`"
                rows="2"
                @click.stop
              ></textarea>
              <button 
                class="submit-reply-btn" 
                :disabled="!replyContent.trim() || submittingReply"
                @click.stop="submitReply"
              >
                {{ submittingReply ? '提交中...' : '提交回复' }}
              </button>
            </div>
          </div>
          
          <div v-if="loadingComments" class="loading">
            <div class="loading-spinner"></div>
            <p>加载评论中...</p>
          </div>
          
          <div v-if="hasMoreComments === false && comments.length > 0" class="no-more">
            没有更多评论了
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
import { commentApi, Comment } from '@/api/comment'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const route = useRoute()
const postId = computed(() => Number(route.params.id))
const post = ref<Post | null>(null)
const comments = ref<(Comment & { isLiked?: boolean })[]>([])
const loading = ref<boolean>(true)
const loadingComments = ref<boolean>(false)
const submittingComment = ref<boolean>(false)
const hasMoreComments = ref<boolean>(true)
const currentCommentPage = ref<number>(1)
const commentContent = ref<string>('')
const replyToCommentId = ref<number | null>(null)
const replyToUsername = ref<string>('')
const replyContent = ref<string>('')
const submittingReply = ref<boolean>(false)
const isLiked = ref<boolean>(false)
const isLoggedIn = ref<boolean>(!!localStorage.getItem('token'))
const currentUserId = ref<number>(Number(localStorage.getItem('userId') || '0'))
const username = ref<string>(localStorage.getItem('username') || '用户')
const userAvatar = ref<string>(localStorage.getItem('avatar') || '')
const commentListRef = ref<HTMLElement | null>(null)

const goToUserProfile = (userId: number) => {
  if (userId === currentUserId.value) {
    router.push('/user/profile')
  } else {
    router.push(`/user/${userId}`)
  }
}

const loadPost = async () => {
  loading.value = true
  try {
    // 这里应该调用获取单个帖子的接口，但是后端没有提供，所以我们暂时从列表中获取
    // 实际项目中应该实现一个获取单个帖子的接口
    const response = await postApi.getPosts(0, 1, 100)
    const data = response as any
    if (data.code === 200) {
      post.value = data.data?.records.find((p: Post) => p.id === postId.value) || null
      if (post.value) {
        // 更新浏览量
        await postApi.updateViewCount({ [postId.value]: 1 })
      }
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  if (loadingComments.value) return
  
  loadingComments.value = true
  try {
    const response = await commentApi.getComments(postId.value, currentCommentPage.value, 20)
    const data = response as any
    console.log('完整响应:', response)
    console.log('数据字段:', data.data)
    if (data.code === 200) {
      const pageData = data.data || {}
      console.log('pageData结构:', Object.keys(pageData))
      const newComments = pageData.records || pageData.content || []
      console.log('评论列表:', newComments)
      console.log('第一个评论:', newComments[0])
      const normalizedComments = newComments.map((comment: any) => ({
        ...comment,
        userName: comment.userName || comment.username || comment.user_name || '未知用户',
        avatar: comment.avatar || comment.avatarUrl || null
      }))
      comments.value = currentCommentPage.value === 1 ? normalizedComments : [...comments.value, ...normalizedComments]
      hasMoreComments.value = normalizedComments.length === 20
      currentCommentPage.value++
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

const likePost = async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  try {
    const response = await postApi.likePost(postId.value)
    const data = response as any
    if (data.code === 200) {
      if (post.value) {
        // 点赞切换效果：已点赞则取消（点赞数-1），未点赞则点赞（点赞数+1）
        if (isLiked.value) {
          post.value.likeCount--
        } else {
          post.value.likeCount++
        }
        isLiked.value = !isLiked.value
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const likeComment = async (commentId: number) => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return

  const wasLiked = comment.isLiked

  try {
    const response = await commentApi.likeComment(commentId)
    const data = response as any
    if (data.code === 200) {
      // 点赞切换效果：已点赞则取消（点赞数-1），未点赞则点赞（点赞数+1）
      comment.likeCount = wasLiked ? comment.likeCount - 1 : comment.likeCount + 1
      comment.isLiked = !wasLiked
    }
  } catch (error) {
    console.error('点赞评论失败:', error)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim() || !isLoggedIn.value) return
  
  submittingComment.value = true
  try {
    const response = await commentApi.sendComment({
      postId: postId.value,
      content: commentContent.value,
      parentId: 0,
      replyUserId: 0
    })
    const data = response as any
    if (data.code === 200) {
      commentContent.value = ''
      currentCommentPage.value = 1
      comments.value = []
      hasMoreComments.value = true
      loadComments()
    } else {
      alert(data.msg || '评论失败')
    }
  } catch (error: any) {
    console.error('提交评论失败:', error)
    if (error.msg) {
      alert('评论失败: ' + error.msg)
    } else {
      alert('评论失败，请稍后重试')
    }
  } finally {
    submittingComment.value = false
  }
}

const showReplyInput = (commentId: number, username: string) => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  replyToCommentId.value = commentId
  replyToUsername.value = username
  replyContent.value = ''
}

const cancelReply = () => {
  replyToCommentId.value = null
  replyToUsername.value = ''
  replyContent.value = ''
}

const submitReply = async () => {
  if (!replyContent.value.trim() || !replyToCommentId.value) return
  
  submittingReply.value = true
  try {
    const response = await commentApi.sendComment({
      postId: postId.value,
      content: replyContent.value,
      parentId: replyToCommentId.value,
      replyUserId: comments.value.find(c => c.id === replyToCommentId.value)?.userId || 0
    })
    const data = response as any
    if (data.code === 200) {
      replyContent.value = ''
      replyToCommentId.value = null
      replyToUsername.value = ''
      currentCommentPage.value = 1
      comments.value = []
      hasMoreComments.value = true
      loadComments()
    } else {
      alert(data.msg || '回复失败')
    }
  } catch (error: any) {
    console.error('提交回复失败:', error)
    alert(error.message || '回复失败，请稍后重试')
  } finally {
    submittingReply.value = false
  }
}

const goBack = () => {
  // 直接返回首页，避免循环
  router.push('/')
}

const reportPost = () => {
  router.push(`/report?type=post&targetId=${postId.value}`)
}

const deleteComment = (commentId: number) => {
  // 先显示确认弹窗
  const userConfirmed = confirm('确定要删除这条评论吗？')
  
  // 只有用户点击确定后才执行删除操作
  if (userConfirmed) {
    // 延迟执行，确保弹窗逻辑完全完成
    setTimeout(async () => {
      try {
        const response = await commentApi.deleteMyComment(commentId)
        const data = response as any
        if (data.code === 200) {
          // 从列表中移除删除的评论
          const index = comments.value.findIndex(c => c.id === commentId)
          if (index > -1) {
            comments.value.splice(index, 1)
          }
          alert('评论删除成功')
        } else {
          alert(data.message || '删除失败')
        }
      } catch (error: any) {
        console.error('删除评论失败:', error)
        alert(error.message || '删除失败')
      }
    }, 100)
  }
}

const deletePost = () => {
  // 先显示确认弹窗
  const userConfirmed = confirm('确定要删除这篇帖子吗？')
  
  // 只有用户点击确定后才执行删除操作
  if (userConfirmed) {
    // 延迟执行，确保弹窗逻辑完全完成
    setTimeout(async () => {
      try {
        const response = await postApi.deleteMyPost(postId.value)
        const data = response as any
        if (data.code === 200) {
          alert('删除成功')
          router.push('/')
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
    }, 100)
  }
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const handleCommentScroll = debounce(() => {
  if (!commentListRef.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = commentListRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100 && !loadingComments.value && hasMoreComments.value) {
    loadComments()
  }
}, 200)

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.post-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  padding: 1rem 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #333;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.back-btn:hover {
  background-color: #f5f5f5;
}

.header h1 {
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 1rem;
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

.btn {
  padding: 0.5rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  text-decoration: none;
  color: #333;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
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

.post-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 2rem;
}

.post-title {
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #999;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.post-meta-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.post-avatar {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  object-fit: cover;
}

.post-meta-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.post-meta-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-top: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
  width: 100%;
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
  cursor: pointer;
  transition: color 0.3s ease;
}

.stat-item:hover {
  color: #4CAF50;
}

.stat-icon {
  font-size: 1rem;
}

.stat-icon.liked {
  color: #e74c3c;
}

.post-body {
  font-size: 1rem;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
}

.comment-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.comment-section h3 {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 1.5rem;
}

.comment-form {
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
}

.comment-form textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 1rem;
  resize: vertical;
  margin-bottom: 1rem;
  font-family: inherit;
  line-height: 1.5;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.submit-comment-btn {
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.submit-comment-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.submit-comment-btn:disabled {
  background-color: #ccc;
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
  margin-left: 1rem;
}

.delete-btn:hover {
  background-color: #ff3742;
}

.report-btn {
  background-color: #ffa500;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: 0.5rem;
}

.report-btn:hover {
  background-color: #ff8c00;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.comment-header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.comment-delete-btn {
  background-color: #ff4757;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.comment-delete-btn:hover {
  background-color: #ff3742;
}

.comment-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item {
  padding: 1.5rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.comment-avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  object-fit: cover;
}

.comment-avatar.cursor-pointer:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

.comment-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.comment-header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.8rem;
  color: #999;
}

.comment-author {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.comment-author.cursor-pointer:hover {
  color: #667eea;
  text-decoration: underline;
}

.comment-time {
  font-size: 0.8rem;
  color: #999;
}

.comment-body {
  font-size: 0.95rem;
  line-height: 1.5;
  color: #333;
  margin-bottom: 0.5rem;
}

.reply-indicator {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 0.5rem;
  padding-left: 0.75rem;
  border-left: 2px solid #e0e0e0;
}

.comment-footer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 1rem;
}

.comment-reply-btn {
  font-size: 0.85rem;
  color: #666;
  cursor: pointer;
  transition: color 0.3s ease;
}

.comment-reply-btn:hover {
  color: #e94560;
}

.reply-input-box {
  margin-top: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
  color: #666;
}

.cancel-reply-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
}

.cancel-reply-btn:hover {
  color: #e94560;
}

.reply-input-box textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.9rem;
  resize: vertical;
  font-family: inherit;
  box-sizing: border-box;
}

.reply-input-box textarea:focus {
  outline: none;
  border-color: #e94560;
}

.submit-reply-btn {
  margin-top: 0.75rem;
  padding: 0.5rem 1.5rem;
  background: #e94560;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.3s ease;
}

.submit-reply-btn:hover:not(:disabled) {
  background: #c23a51;
}

.submit-reply-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  cursor: pointer;
  font-size: 0.85rem;
  color: #999;
  transition: color 0.3s ease;
}

.comment-like:hover {
  color: #4CAF50;
}

.like-icon {
  font-size: 1rem;
}

.no-more {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .header {
    padding: 1rem;
  }
  
  .main {
    padding: 1rem;
  }
  
  .post-content {
    padding: 1.5rem;
  }
  
  .comment-section {
    padding: 1.5rem;
  }
}
</style>