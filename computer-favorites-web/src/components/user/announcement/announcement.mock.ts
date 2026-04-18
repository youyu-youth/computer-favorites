export type AnnouncementType = 1 | 2 | 3
export type AnnouncementFilterValue = 'all' | AnnouncementType

export interface AnnouncementMockItem {
  id: number
  title: string
  content: string
  type: AnnouncementType
  isTop: 0 | 1
  status: 0 | 1
  publishTime: string | null
  createTime: string
  updateTime: string
}

export interface AnnouncementViewItem extends AnnouncementMockItem {
  typeLabel: string
  displayDate: string
  displayTime: string
  sortTime: string
}

export interface AnnouncementFilterOption {
  label: string
  value: AnnouncementFilterValue
}

export const ANNOUNCEMENT_FILTER_OPTIONS: AnnouncementFilterOption[] = [
  { label: '全部', value: 'all' },
  { label: '新增内容', value: 1 },
  { label: 'Bug 修复', value: 2 },
  { label: '系统更新', value: 3 },
]

export const ANNOUNCEMENT_MOCK_LIST: AnnouncementMockItem[] = [
  {
    id: 101,
    title: 'v4.5.1 发布：资源卡片交互与筛选体验优化',
    content:
      '1. 首页资源卡片新增更稳定的交互反馈，收藏、评分与访问入口的可视层级更清晰。\n2. 标签筛选在移动端改为横向滚动布局，避免窄屏折行导致的阅读中断。\n3. 深色模式下统一卡片边框、分隔线和悬停高亮强度，让整页在夜间场景下更耐看。',
    type: 1,
    isTop: 1,
    status: 1,
    publishTime: '2026-04-18 10:30:00',
    createTime: '2026-04-18 10:00:00',
    updateTime: '2026-04-18 10:30:00',
  },
  {
    id: 102,
    title: '系统公告页预告：时间线阅读体验即将上线',
    content:
      '我们正在为用户端准备一套更适合版本更新阅读的公告页面。新版将支持时间线浏览、置顶公告区、完整正文展示，以及更统一的亮色/暗色视觉风格。',
    type: 3,
    isTop: 1,
    status: 1,
    publishTime: '2026-04-17 18:20:00',
    createTime: '2026-04-17 17:40:00',
    updateTime: '2026-04-17 18:20:00',
  },
  {
    id: 103,
    title: 'v4.5.0 发布：消息中心支持更清晰的未读处理',
    content:
      '1. 新增“全选未读”和“批量已读”能力。\n2. 消息条目中加入更直观的类型图标与未读红点反馈。\n3. 错误加载态补充重试入口，保证消息中心在网络波动下也能快速恢复。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-16 14:08:00',
    createTime: '2026-04-16 13:30:00',
    updateTime: '2026-04-16 14:08:00',
  },
  {
    id: 104,
    title: 'Bug 修复：移动端筛选栏按钮挤压问题',
    content:
      '修复了在 360px 到 420px 宽度区间内，筛选按钮被压缩后文字重叠的问题。现在移动端的筛选项会保持横向滚动，按钮宽度更稳定。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-14 11:26:00',
    createTime: '2026-04-14 10:58:00',
    updateTime: '2026-04-14 11:26:00',
  },
  {
    id: 105,
    title: '系统更新：用户端主题主色切换为暖橙色',
    content:
      '为了让用户端在内容浏览和状态提示上更具识别度，当前主题主色已统一切换为 #d97706。新主题会优先作用于标题强调、时间线节点、主要按钮和标签边框。',
    type: 3,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-12 09:12:00',
    createTime: '2026-04-12 09:00:00',
    updateTime: '2026-04-12 09:12:00',
  },
  {
    id: 106,
    title: '新增内容：个人主页项目卡片支持更多信息密度',
    content:
      '个人主页中的项目展示区域现已支持更紧凑的信息排布。你可以更快查看项目标题、更新时间、技术标签和摘要，减少频繁跳转的操作成本。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-10 16:40:00',
    createTime: '2026-04-10 16:05:00',
    updateTime: '2026-04-10 16:40:00',
  },
  {
    id: 107,
    title: 'Bug 修复：暗黑模式下卡片阴影层级过重',
    content:
      '修正了部分用户页面在暗黑模式下阴影偏重、卡片边界发灰的问题。调整后边框与阴影的平衡更自然，阅读主体更突出。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-08 21:15:00',
    createTime: '2026-04-08 20:56:00',
    updateTime: '2026-04-08 21:15:00',
  },
  {
    id: 108,
    title: '系统更新：公告与消息相关接口契约已预留',
    content:
      '虽然当前用户端公告页先采用本地假数据实现，但前端已经预留 `/api/announcements` 与公告详情相关类型定义。后续接入真实接口时，可以在不大改页面结构的前提下完成切换。',
    type: 3,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-06 15:30:00',
    createTime: '2026-04-06 15:00:00',
    updateTime: '2026-04-06 15:30:00',
  },
  {
    id: 109,
    title: '新增内容：用户端卡片布局支持更稳定的断点适配',
    content:
      '我们补齐了多个常见设备宽度下的断点细节。页面在桌面端会保留更舒展的双列结构，在平板与手机上则优先保障正文阅读，减少无意义留白和挤压。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-04 13:18:00',
    createTime: '2026-04-04 12:52:00',
    updateTime: '2026-04-04 13:18:00',
  },
  {
    id: 110,
    title: 'Bug 修复：部分页面滚动条在暗黑模式下对比度不足',
    content:
      '修复了暗黑模式下滚动条颜色过深、操作反馈不明显的问题。调整后滚动条在不破坏整体沉浸感的前提下，具备更好的可发现性。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-02 08:42:00',
    createTime: '2026-04-02 08:15:00',
    updateTime: '2026-04-02 08:42:00',
  },
]

const ANNOUNCEMENT_TYPE_LABEL_MAP: Record<AnnouncementType, string> = {
  1: '新增内容',
  2: 'Bug 修复',
  3: '系统更新',
}

function parseAnnouncementDate(value: string): Date {
  const normalized = value.replace(' ', 'T')
  const parsed = new Date(normalized)

  if (!Number.isNaN(parsed.getTime())) {
    return parsed
  }

  return new Date(value)
}

export function formatAnnouncementDate(value: string): string {
  const parsed = parseAnnouncementDate(value)
  const year = parsed.getFullYear()
  const month = `${parsed.getMonth() + 1}`.padStart(2, '0')
  const day = `${parsed.getDate()}`.padStart(2, '0')

  return `${year}-${month}-${day}`
}

export function formatAnnouncementTime(value: string): string {
  const parsed = parseAnnouncementDate(value)
  const hours = `${parsed.getHours()}`.padStart(2, '0')
  const minutes = `${parsed.getMinutes()}`.padStart(2, '0')

  return `${hours}:${minutes}`
}

export function mapAnnouncementItem(item: AnnouncementMockItem): AnnouncementViewItem {
  const sortTime = item.publishTime || item.createTime

  return {
    ...item,
    typeLabel: ANNOUNCEMENT_TYPE_LABEL_MAP[item.type],
    displayDate: formatAnnouncementDate(sortTime),
    displayTime: formatAnnouncementTime(sortTime),
    sortTime,
  }
}

export function resolveVisibleAnnouncements(list: AnnouncementMockItem[]): AnnouncementViewItem[] {
  return list
    .filter((item) => item.status === 1)
    .map(mapAnnouncementItem)
    .sort((left, right) => right.sortTime.localeCompare(left.sortTime))
}

export function resolveAnnouncementFilterLabel(value: AnnouncementFilterValue): string {
  if (value === 'all') {
    return '全部'
  }

  return ANNOUNCEMENT_TYPE_LABEL_MAP[value]
}
