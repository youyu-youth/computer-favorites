/**
 * 管理端用户列表项类型
 */
export interface AdminUserItem {
  id: number
  username: string
  email: string
  phone: string | null
  nickname: string | null
  avatar: string | null
  status: 0 | 1
  emailVerified: 0 | 1
  phoneVerified: 0 | 1
  lastLoginTime: string | null
  lastLoginIp: string | null
  createTime: string
  updateTime: string
  deleted: 0 | 1
}

/**
 * 新建/编辑用户表单数据
 */
export interface AdminUserFormModel {
  username: string
  email: string
  phone: string
  nickname: string
  password: string
  status: 0 | 1
}

/**
 * 用户列表分页查询参数
 */
export interface AdminUserPageQuery {
  keyword?: string
  status?: 0 | 1 | ''
  pageNum: number
  pageSize: number
}

/**
 * 用户列表分页结果
 */
export interface AdminUserPage {
  records: AdminUserItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 用户详情（含资料信息）
 */
export interface AdminUserDetail extends AdminUserItem {
  gender?: number | null
  city?: string | null
  signature?: string | null
  techStack?: string | null
  githubUrl?: string | null
  blogUrl?: string | null
}

/**
 * 用户统计数据
 */
export interface AdminUserStats {
  total: number
  normal: number
  disabled: number
  emailVerified: number
}
