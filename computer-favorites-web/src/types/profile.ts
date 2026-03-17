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
  icon: string
  url: string
}

export interface ProfileSiteItem {
  name: string
  description: string
  icon: string
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
