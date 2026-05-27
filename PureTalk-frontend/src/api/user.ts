import axios from './axios'

export interface DoLogin {
  phone: string
  password: string
  isRemember: boolean
}

export interface DoRegister {
  username: string
  password: string
  phone: string
  email: string
  rightPhone: boolean
  rightEmail: boolean
}

export interface DoUpdate {
  username?: string
  password?: string
  phone?: string
  email?: string
  avatar?: string
  isRight?: boolean
}

export interface ValidateCode {
  phone: string
  phoneCode: string
  email?: string
  emailCode?: string
  isRemember?: boolean
}

export interface LoginReturnData {
  id: number
  username: string
  phone: string
  email: string
  avatar: string
  role: number
  status: number
  createTime: string
  banReason: string | null
  banOn: string | null
  bannedUntil: string | null
  token: string
}

export interface Result<T = any> {
  code: number
  msg: string
  data?: T
}

export const userApi = {
  // 登录
  login: (data: DoLogin) => axios.post<Result<LoginReturnData>>('/user/login', data),
  // 发送验证码（登录用）
  sendLoginCode: (phone: string) => axios.get<Result>('/user/login/code', { params: { phone } }),
  // 验证码登录
  loginWithCode: (data: ValidateCode) => axios.post<Result<LoginReturnData>>('/user/login/code', data),
  // 管理员登录
  adminLogin: (data: DoLogin) => axios.post<Result<LoginReturnData>>('/admin/login', data),
  // 注册
  register: (data: DoRegister) => axios.post<Result>('/user/register', data),
  // 发送手机验证码（注册用）
  sendRegisterPhoneCode: (phone: string) => axios.get<Result>('/user/register/phone', { params: { phone } }),
  // 验证手机验证码
  validatePhoneCode: (data: ValidateCode) => axios.post<Result>('/user/register/phone', data),
  // 发送邮箱验证码（注册用）
  sendRegisterEmailCode: (email: string) => axios.get<Result>('/user/register/email', { params: { email } }),
  // 验证邮箱验证码
  validateEmailCode: (data: { email: string; emailCode: string }) => axios.post<Result>('/user/register/email', data),
  // 删除账户
  deleteAccount: () => axios.delete<Result>('/user/delete'),
  // 登出
  logout: () => axios.post<Result>('/user/logout'),
  // 上传头像
  uploadAvatar: (file: FormData) => axios.post<Result<string>>('/user/avatar', file, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  // 更新用户信息
  update: (data: DoUpdate) => axios.put<Result>('/user/update', data)
}

export const adminApi = {
  // 管理员封禁用户
  banUser: (data: { userId: number; banReason: string; banTime: number }) => axios.post<Result>('/admin/ban', data),
  // 管理员解禁用户
  unbanUser: (userId: number) => axios.post<Result>(`/admin/unban?userId=${userId}`),
  // 管理员注销自己
  deleteAdmin: () => axios.delete<Result>('/admin/delete'),
  // 管理员登出
  logout: () => axios.post<Result>('/admin/logout')
}

export const rootApi = {
  // 创建管理员
  createAdmin: (data: DoRegister) => axios.post<Result>('/root/register', data),
  // 获取待删除管理员信息
  getAdminInfo: (phone: string) => axios.get<Result>(`/root/delete?phone=${phone}`),
  // 删除管理员
  deleteAdmin: (phone: string) => axios.delete<Result>(`/root/delete?phone=${phone}`),
  // 发送手机验证码
  sendPhoneCode: (phone: string) => axios.get<Result>('/root/register/phone', { params: { phone } }),
  // 校验手机验证码
  validatePhoneCode: (data: ValidateCode) => axios.post<Result>('/root/register/phone', data),
  // 发送邮箱验证码
  sendEmailCode: (email: string) => axios.get<Result>('/root/register/email', { params: { email } }),
  // 校验邮箱验证码
  validateEmailCode: (data: { email: string; emailCode: string }) => axios.post<Result>('/root/register/email', data)
}

export function getUserId(): number {
  const userId = localStorage.getItem('userId')
  return userId ? Number(userId) : 0
}

export function getToken(): string {
  return localStorage.getItem('token') || ''
}

export function getRole(): string {
  return localStorage.getItem('role') || ''
}