/**
 * 审计日志类型定义
 *
 * 对应数据库表 t_audit_log，包含审计日志的枚举、接口及中文标签映射
 */

/** 审计模块类型 */
export type AuditModule =
  | 'auth'
  | 'website'
  | 'user'
  | 'admin'
  | 'comment'
  | 'report'
  | 'feedback'
  | 'announcement'
  | 'system'

/** 审计操作类型 */
export type AuditAction =
  | 'login'
  | 'logout'
  | 'create'
  | 'update'
  | 'delete'
  | 'review'
  | 'export'
  | 'import'
  | 'status'
  | 'ban'
  | 'unban'

/** 审计用户类型 */
export type AuditUserType = 'user' | 'admin'

/** 审计操作结果 */
export enum AuditResult {
  /** 失败 */
  Failed = 0,
  /** 成功 */
  Success = 1,
}

/** 审计请求方法 */
export type AuditRequestMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

/** 审计日志列表项 — 表格展示用 */
export interface AuditLogListItem {
  /** 主键ID */
  id: number
  /** 操作用户ID */
  userId: number
  /** 用户类型 */
  userType: AuditUserType
  /** 所属模块 */
  module: AuditModule
  /** 操作类型 */
  action: AuditAction
  /** 操作对象类型（可为空） */
  targetType: string | null
  /** 操作对象ID（可为空） */
  targetId: number | null
  /** 操作结果 */
  result: AuditResult
  /** 操作IP地址 */
  ip: string
  /** 操作时间 */
  createTime: string
}

/** 审计日志详情 — 详情弹窗展示用 */
export interface AuditLogDetail extends AuditLogListItem {
  /** 错误信息（可为空） */
  errorMsg: string | null
  /** 请求URL（可为空） */
  requestUrl: string | null
  /** 请求方法（可为空） */
  requestMethod: AuditRequestMethod | null
}

/** 审计日志查询参数 */
export interface AuditLogQuery {
  /** 页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 所属模块 */
  module: AuditModule | null
  /** 操作类型 */
  action: AuditAction | null
  /** 用户类型 */
  userType: AuditUserType | null
  /** 操作结果 */
  result: AuditResult | null
  /** 开始时间 */
  startTime: string | null
  /** 结束时间 */
  endTime: string | null
  /** 关键词搜索 */
  keyword: string
}

/** 审计日志统计信息 */
export interface AuditLogStatistics {
  /** 今日操作数 */
  todayCount: number
  /** 成功率（0-100） */
  successRate: number
  /** 失败次数 */
  failCount: number
  /** 本周操作数 */
  weekCount: number
}

/** 审计模块中文标签 */
export const AUDIT_MODULE_LABELS: Record<AuditModule, string> = {
  auth: '认证',
  website: '网站',
  user: '用户',
  admin: '管理',
  comment: '评论',
  report: '举报',
  feedback: '反馈',
  announcement: '公告',
  system: '系统',
} as const

/** 审计操作中文标签 */
export const AUDIT_ACTION_LABELS: Record<AuditAction, string> = {
  login: '登录',
  logout: '登出',
  create: '新增',
  update: '修改',
  delete: '删除',
  review: '审核',
  export: '导出',
  import: '导入',
  status: '状态变更',
  ban: '封禁',
  unban: '解封',
} as const

/** 审计用户类型中文标签 */
export const AUDIT_USER_TYPE_LABELS: Record<AuditUserType, string> = {
  user: '普通用户',
  admin: '管理员',
} as const

/** 审计操作结果中文标签 */
export const AUDIT_RESULT_LABELS: Record<AuditResult, string> = {
  [AuditResult.Success]: '成功',
  [AuditResult.Failed]: '失败',
} as const
