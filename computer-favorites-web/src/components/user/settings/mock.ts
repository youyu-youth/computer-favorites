export interface UserBasicInfo {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  nickname: string
  emailVerified: number
  phoneVerified: number
}

export interface UserDetailProfile {
  gender: number // 0未知 1男 2女 3保密
  country: string
  city: string
  githubUrl: string
  giteeUrl: string
  otherRepoLinks: string
  blogUrl: string
  signature: string
  hobbyTags: string // 逗号分隔的字符串或JSON
  techStack: string // 逗号分隔的字符串或JSON
  favoriteWebsites: string
  uploadedWebsites: string
  contribution: string
  createTime?: string
  updateTime?: string
}

export interface UserPreferenceSetting {
  theme: string // light/dark
  language: string // zh-CN
  emailNotice: number // 0关闭 1开启
  collectNotice: number
  commentNotice: number
  homepageStyle: string // card/list
  pageSize: number
}

// Mock Data
export const mockUserBasicInfo: UserBasicInfo = {
  id: 10001,
  username: 'yyyouth',
  email: 'codebuddy@example.com',
  phone: '13800138000',
  avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=yyyouth',
  nickname: 'YYYouth',
  emailVerified: 1,
  phoneVerified: 0,
}

export const mockUserDetailProfile: UserDetailProfile = {
  gender: 1,
  country: '中国',
  city: '深圳',
  githubUrl: 'https://github.com/yyyouth',
  giteeUrl: '',
  otherRepoLinks: '',
  blogUrl: 'https://blog.example.com',
  signature: 'Code is poetry.',
  hobbyTags: '阅读,开源,设计',
  techStack: 'Vue.js,TypeScript,Spring Boot,MySQL',
  favoriteWebsites: '',
  uploadedWebsites: '',
  contribution: '累计提交 52 次代码，推荐 12 个优质网站。',
}

export const mockUserPreferenceSetting: UserPreferenceSetting = {
  theme: 'dark',
  language: 'zh-CN',
  emailNotice: 1,
  collectNotice: 1,
  commentNotice: 1,
  homepageStyle: 'card',
  pageSize: 20,
}

export interface SettingsStoreState {
  basicInfo: UserBasicInfo
  profile: UserDetailProfile
  setting: UserPreferenceSetting
}
