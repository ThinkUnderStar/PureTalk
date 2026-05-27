import axios from './axios'
import { Result } from './user'

export interface DoSendComment {
  postId: number
  content: string
  parentId?: number
  replyUserId?: number
}

export interface Comment {
  id: number
  postId: number
  userId: number
  username: string
  avatar: string
  content: string
  parentId?: number
  likeCount: number
  createTime: string
  replyUserName?: string
}

export interface CommentPageResult {
  records: Comment[]
  total: number
  current: number
  pages: number
  size: number
}

export const commentApi = {
  // 获取评论列表
  getComments: (postId: number, page: number = 1, size: number = 20) => 
    axios.get<Result<CommentPageResult>>('/comment', { params: { postId, page, size } }),
  // 发送评论
  sendComment: (data: DoSendComment) => axios.post<Result>('/comment/send', data),
  // 删除自己的评论
  deleteMyComment: (commentId: number) => axios.delete<Result>(`/comment/user/${commentId}/delete`),
  // 管理员删除评论
  deleteComment: (commentId: number) => axios.delete<Result>(`/comment/admin/${commentId}/delete`),
  // 点赞评论
  likeComment: (commentId: number) => axios.put<Result>(`/comment/${commentId}/like`)
}