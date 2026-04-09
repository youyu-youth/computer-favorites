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
