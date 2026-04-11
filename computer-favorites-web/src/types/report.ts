/**
 * 举报类型枚举
 */
export enum ReportType {
  WEBSITE = 1,
  COMMENT = 2,
}

/**
 * 举报状态枚举
 */
export enum ReportStatus {
  PENDING = 0,
  PROCESSED = 1,
  REJECTED = 2,
}

/**
 * 举报表单数据
 */
export interface ReportFormData {
  type: ReportType
  targetId: number
  reason: string
  images: string[]
}

/**
 * 举报原因选项
 */
export interface ReportReasonOption {
  value: number
  label: string
  description: string
}

/**
 * 举报记录
 */
export interface ReportRecord {
  id: number
  userId: number
  type: ReportType
  targetId: number
  reason: string
  images: string[] | null
  status: ReportStatus
  handleResult: string | null
  handlerId: number | null
  handleTime: string | null
  createTime: string
  updateTime: string
}
