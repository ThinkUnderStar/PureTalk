import axios from './axios'
import { Result } from './user'

export interface Notification {
  id: number
  userId: number
  type: string
  content: string
  isRead: boolean
  createTime: string
  relatedId?: number
  relatedType?: string
}

export interface NotificationPageResult {
  records: Notification[]
  total: number
  current: number
  pages: number
  size: number
}

export const notificationApi = {
  // 获取通知列表
  getNotifications: (page: number = 1, size: number = 20) =>
    axios.get<Result<NotificationPageResult>>('/notification', { params: { page, size } }),
  // 标记已读
  markAsRead: (notificationId: number) =>
    axios.put<Result>(`/notification/${notificationId}/read`),
  // 全部标记已读
  markAllAsRead: () => axios.put<Result>('/notification/read-all'),
  // 删除通知
  deleteNotification: (notificationId: number) =>
    axios.delete<Result>(`/notification/${notificationId}`)
}