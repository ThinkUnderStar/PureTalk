import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import PostDetail from '../views/PostDetail.vue'
import UserProfile from '../views/UserProfile.vue'
import Admin from '../views/Admin.vue'
import Feedback from '../views/Feedback.vue'
import Report from '../views/Report.vue'
import Notification from '../views/Notification.vue'
import AdminCreate from '../views/AdminCreate.vue'
import AIChat from '../views/AIChat.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: PostDetail
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: UserProfile
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: Feedback
  },
  {
    path: '/report',
    name: 'Report',
    component: Report
  },
  {
    path: '/notification',
    name: 'Notification',
    component: Notification
  },
  {
    path: '/admin/create',
    name: 'AdminCreate',
    component: AdminCreate
  },
  {
    path: '/ai/chat',
    name: 'AIChat',
    component: AIChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router