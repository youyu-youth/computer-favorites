export interface AdminProfile {
  id: number
  username: string
  email: string
  avatar: string
  nickname: string
  role: string
  status: number
  lastLoginTime: string
  lastLoginIp: string
  createTime: string
  updateTime: string
}

export interface UpdateAdminProfileRequest {
  nickname?: string
  email?: string
}
