import type { ProfileData } from '@/types/profile'

export const profileMockData: ProfileData = {
  name: 'Zyyo',
  role: 'Full Stack Developer',
  location: '中国 · 河南',
  organization: 'Sias',
  signature: 'The only way to do great work is to love what you do.',
  avatarUrl: 'https://api.dicebear.com/7.x/notionists/svg?seed=Zyyo&backgroundColor=f8f9fa',
  tags: ['前端', '后端', 'Linux', '骑行', '开源', '架构'],
  timeline: [
    { title: '上线个人导航主页', date: '2024.01' },
    { title: '完成ICP备案', date: '2023.08' },
    { title: '注册独立域名', date: '2023.03' },
    { title: '开始系统化学习开发', date: '2021.02' },
  ],
  socialLinks: [
    { label: 'GitHub', icon: 'i-lucide-github', url: '#' },
    { label: 'Gitee', icon: 'i-lucide-folder-git-2', url: '#' },
    { label: '博客', icon: 'i-lucide-notebook-text', url: '#' },
    { label: '邮箱', icon: 'i-lucide-mail', url: '#' },
  ],
  favoriteSites: [
    { name: '技术博客', description: '记录开发实践与踩坑', icon: 'i-lucide-book-open' },
    { name: '文档中心', description: '框架与组件文档速查', icon: 'i-lucide-library' },
    { name: '效率工具', description: '日常开发效率增强', icon: 'i-lucide-wrench' },
    { name: '学习清单', description: '持续学习与知识沉淀', icon: 'i-lucide-list-checks' },
  ],
  uploadedProjects: [
    { name: '收藏夹平台', description: '网站分享与导航系统', icon: 'i-lucide-layers-3' },
    { name: '管理后台', description: '审核与运营管理能力', icon: 'i-lucide-layout-dashboard' },
    { name: '组件实验室', description: '可复用组件与模式验证', icon: 'i-lucide-box' },
    { name: '工具合集', description: '常用脚本与自动化工具', icon: 'i-lucide-cog' },
  ],
  skills: [
    { name: 'Vue 3', level: '精通' },
    { name: 'TypeScript', level: '熟练' },
    { name: 'Spring Boot', level: '熟练' },
    { name: 'MySQL', level: '熟练' },
    { name: 'Redis', level: '基础' },
    { name: 'Docker', level: '基础' },
  ],
  contributions: [
    { title: '内容贡献', summary: '持续整理并发布高质量技术站点与学习资料。' },
    { title: '社区互动', summary: '在评论与反馈中沉淀实践经验，帮助他人排障。' },
    { title: '功能共建', summary: '参与页面与组件优化，推动体验与可维护性提升。' },
  ],
  stats: [
    { label: '收藏网站', value: '128' },
    { label: '上传站点', value: '36' },
    { label: '贡献次数', value: '214' },
  ],
}

export const heatmapRows = 7
export const heatmapCols = 52

export const getHeatValue = (row: number, col: number) => {
  const seed = (row * 97 + col * 53 + row * col * 11) % 100
  if (seed > 92) return 4
  if (seed > 78) return 3
  if (seed > 58) return 2
  if (seed > 35) return 1
  return 0
}
