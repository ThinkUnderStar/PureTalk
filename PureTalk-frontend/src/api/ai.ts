import axios from './axios'
import { Result } from './user'

export interface AiMessage {
  id: number
  sessionId: number
  role: string
  content: string
  createTime: string
}

export interface AiSession {
  id: number
  userId: number
  title: string
  status: number
  createTime: string
}

export interface AiSessionPageResult {
  records: AiSession[]
  total: number
  current: number
  pages: number
  size: number
}

export interface AiMessagePageResult {
  records: AiMessage[]
  total: number
  current: number
  pages: number
  size: number
}

export const aiApi = {
  getSessions: (page: number = 1, size: number = 20) =>
    axios.get<Result<AiSessionPageResult>>('/ai/sessions', { params: { page, size } }),

  getMessages: (sessionId: number, page: number = 1, size: number = 20) =>
    axios.get<Result<AiMessagePageResult>>(`/ai/sessions/${sessionId}/messages`, { params: { page, size } }),

  deleteSession: (sessionId: number) =>
    axios.delete<Result>(`/ai/sessions/${sessionId}/delete`)
}