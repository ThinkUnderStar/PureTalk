import axios from './axios'
import { Result } from './user'

export interface DoFeedback {
  userId: number
  title: string
  content: string
}

export interface Feedback {
  id: number
  userId: number
  username: string
  content: string
  type: string
  handleStatus: number
  handleContent: string
  createTime: string
}

export interface FeedbackPageResult {
  records: Feedback[]
  total: number
  current: number
  pages: number
  size: number
}

export const feedbackApi = {
  // 提交反馈
  sendFeedback: (data: DoFeedback) => axios.post<Result>('/feedback/send', data),
  // 获取反馈列表（管理员）
  getFeedbacks: (page: number = 1, size: number = 20) => axios.get<Result<FeedbackPageResult>>('/feedback', { params: { page, size } }),
  // 管理员处理反馈
  handleFeedback: (feedbackId: number, handleContent: string) => axios.put<Result>('/feedback/handle', { feedbackId, handleContent })
}