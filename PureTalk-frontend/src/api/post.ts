import axios from './axios'
import { Result } from './user'

export interface DoSendPost {
  title: string
  content: string
}

export interface Post {
  id: number
  title: string
  content: string
  userId: number
  username: string
  avatar: string
  likeCount: number
  viewCount: number
  commentCount: number
  createTime: string
}

export interface PostPageResult {
  records: Post[]
  total: number
  current: number
  pages: number
  size: number
}

export const postApi = {
  // 获取帖子列表
  getPosts: (categoryId: number = 0, page: number = 1, size: number = 20) => 
    axios.get<Result<PostPageResult>>('/post', { params: { categoryId, page, size } }),
  // 搜索帖子
  searchPosts: (keyword: string, categoryId: number = 0, page: number = 1, size: number = 20) => 
    axios.get<Result<PostPageResult>>('/post/search', { params: { str: keyword, categoryId, page, size } }),
  // 发送帖子
  sendPost: (data: DoSendPost) => axios.post<Result>('/post/send', data),
  // 删除自己的帖子
  deleteMyPost: (postId: number) => axios.delete<Result>(`/post/user/${postId}/delete`),
  // 管理员删除帖子
  deletePost: (postId: number) => axios.delete<Result>(`/post/admin/${postId}/delete`),
  // 点赞帖子
  likePost: (postId: number) => axios.put<Result>(`/post/${postId}/like`),
  // 更新浏览量
  updateViewCount: (viewMap: Record<number, number>) => axios.put<Result>('/post/view', viewMap)
}