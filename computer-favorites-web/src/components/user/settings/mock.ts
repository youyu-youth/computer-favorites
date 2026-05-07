import type { UserTechStackOption } from '@/types/user-tech-stack'

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
  theme: string // light/dark/system
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

/**
 * 技术栈字典 mock 数据
 *
 * 当 `GET /api/tech-stack/list` 后端尚未实现时，作为前端选择对话框的回退数据源。
 * 图标走 jsdelivr CDN 上的 devicon 项目，国内可访问。
 */
export const mockTechStackOptions: UserTechStackOption[] = [
  {
    id: 1,
    name: 'Vue.js',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vuejs/vuejs-original.svg',
    description: '渐进式 JavaScript 前端框架，组合式 API 灵活易用',
    color: '#42b883',
    officialUrl: 'https://vuejs.org',
    sort: 1,
  },
  {
    id: 2,
    name: 'React',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg',
    description: 'Meta 推出的声明式 UI 库，组件化开发主流方案',
    color: '#61dafb',
    officialUrl: 'https://react.dev',
    sort: 2,
  },
  {
    id: 3,
    name: 'TypeScript',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg',
    description: 'JavaScript 的超集，提供静态类型与现代语法',
    color: '#3178c6',
    officialUrl: 'https://www.typescriptlang.org',
    sort: 3,
  },
  {
    id: 4,
    name: 'Tailwind CSS',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-original.svg',
    description: '原子化 CSS 框架，极速搭建一致的设计系统',
    color: '#06b6d4',
    officialUrl: 'https://tailwindcss.com',
    sort: 4,
  },
  {
    id: 5,
    name: 'Node.js',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nodejs/nodejs-original.svg',
    description: '基于 V8 的 JavaScript 运行时，高性能服务端选择',
    color: '#5fa04e',
    officialUrl: 'https://nodejs.org',
    sort: 5,
  },
  {
    id: 6,
    name: 'Spring Boot',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg',
    description: 'Java 后端开发主流框架，约定优于配置',
    color: '#6db33f',
    officialUrl: 'https://spring.io/projects/spring-boot',
    sort: 6,
  },
  {
    id: 7,
    name: 'Java',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg',
    description: '企业级跨平台编程语言，生态成熟稳定',
    color: '#f89820',
    officialUrl: 'https://www.java.com',
    sort: 7,
  },
  {
    id: 8,
    name: 'Python',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg',
    description: '简洁优雅的通用编程语言，AI 与脚本首选',
    color: '#3776ab',
    officialUrl: 'https://www.python.org',
    sort: 8,
  },
  {
    id: 9,
    name: 'Go',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original.svg',
    description: '云原生时代的高并发后端语言，编译快速',
    color: '#00add8',
    officialUrl: 'https://go.dev',
    sort: 9,
  },
  {
    id: 10,
    name: 'Rust',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rust/rust-original.svg',
    description: '内存安全且零成本抽象的系统级语言',
    color: '#dea584',
    officialUrl: 'https://www.rust-lang.org',
    sort: 10,
  },
  {
    id: 11,
    name: 'MySQL',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg',
    description: '全球最流行的开源关系型数据库',
    color: '#4479a1',
    officialUrl: 'https://www.mysql.com',
    sort: 11,
  },
  {
    id: 12,
    name: 'PostgreSQL',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg',
    description: '功能最强大的开源对象关系型数据库',
    color: '#336791',
    officialUrl: 'https://www.postgresql.org',
    sort: 12,
  },
  {
    id: 13,
    name: 'Redis',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg',
    description: '高性能内存数据结构存储，缓存与队列利器',
    color: '#dc382d',
    officialUrl: 'https://redis.io',
    sort: 13,
  },
  {
    id: 14,
    name: 'MongoDB',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mongodb/mongodb-original.svg',
    description: '面向文档的 NoSQL 数据库，灵活水平扩展',
    color: '#47a248',
    officialUrl: 'https://www.mongodb.com',
    sort: 14,
  },
  {
    id: 15,
    name: 'Docker',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg',
    description: '容器化标准方案，让交付变得简单可靠',
    color: '#2496ed',
    officialUrl: 'https://www.docker.com',
    sort: 15,
  },
  {
    id: 16,
    name: 'Kubernetes',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-original.svg',
    description: '容器编排事实标准，云原生基础设施',
    color: '#326ce5',
    officialUrl: 'https://kubernetes.io',
    sort: 16,
  },
]
