import axios from './axios'
import { Result } from './user'

export interface DoReport {
  reportType: number
  targetId: number
  targetUserId: number
  reason: string
}

export interface Report {
  id: number
  userId: number
  username: string
  targetId: number
  targetType: string
  reason: string
  handleStatus: number
  handleResult: string
  createTime: string
}

export interface ReportPageResult {
  records: Report[]
  total: number
  current: number
  pages: number
  size: number
}

export const reportApi = {
  // 提交举报
  sendReport: (data: DoReport) => axios.post<Result>('/report/send', data),
  // 获取举报列表（管理员）
  getReports: (page: number = 1, size: number = 20) => axios.get<Result<ReportPageResult>>('/report', { params: { page, size } }),
  // 获取举报目标
  getReportTarget: (reportId: number) => axios.get<Result>(`/report/${reportId}/target`),
  // 处理举报（管理员）
  handleReport: (reportId: number, handleResult: string) => axios.put<Result>('/report/handle', { reportId, handleResult })
}