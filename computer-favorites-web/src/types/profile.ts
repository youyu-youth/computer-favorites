export interface ProfileTimelineItem {
  title: string
  date: string
}

export interface ProfileStat {
  label: string
  value: string
}

export interface ProfileSocialLink {
  label: string
  icon?: string
  imageIcon?: string
  url: string
}

export interface ProfileSiteItem {
  name: string
  description: string
  icon: string
  /** 站内详情页 ID，存在时整行可点击跳 /computer/website/:id（仅"上传网站"卡片使用） */
  websiteId?: number
  /** 远程 icon URL（OSS / 站点图标），存在时优先于 lucide 图标 */
  iconUrl?: string
}

export interface ProfileSkill {
  name: string
  level: '基础' | '熟练' | '精通'
}

export interface ProfileContribution {
  title: string
  summary: string
}

export interface ProfileData {
  name: string
  role: string
  location: string
  organization: string
  signature: string
  avatarUrl: string
  tags: string[]
  timeline: ProfileTimelineItem[]
  socialLinks: ProfileSocialLink[]
  favoriteSites: ProfileSiteItem[]
  uploadedProjects: ProfileSiteItem[]
  skills: ProfileSkill[]
  contributions: ProfileContribution[]
  stats: ProfileStat[]
}
