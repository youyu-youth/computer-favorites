import type {
  AdminFeedbackDetail,
  AdminFeedbackHandleResult,
  AdminFeedbackPage,
  AdminFeedbackQuery,
  AdminFeedbackStatistics,
  AdminFeedbackTimelineItem,
  AdminFeedbackListItem,
} from '@/types/feedback'
import { FeedbackStatus, FeedbackType, getFeedbackTypeMeta } from '@/types/feedback'

const MOCK_DELAY = 220

const STATUS_PRIORITY: Record<FeedbackStatus, number> = {
  [FeedbackStatus.PENDING]: 0,
  [FeedbackStatus.PROCESSED]: 1,
  [FeedbackStatus.CLOSED]: 2,
}

let mockFeedbackRecords: AdminFeedbackListItem[] = [
  {
    id: 9012,
    userId: 10021,
    userName: '林知夏',
    userEmail: 'zhixia.lin@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=LinZhixia',
    contact: '微信：linzhixia-dev',
    type: FeedbackType.BUG,
    content:
      'iPhone 端进入网站详情页后，点击收藏按钮没有任何反馈，刷新后收藏状态也没有同步，连续操作时偶尔还会出现白屏。',
    images: [
      'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80',
    ],
    status: FeedbackStatus.PENDING,
    reply: null,
    replyTime: null,
    createTime: '2026-04-17T19:35:00',
    updateTime: '2026-04-17T19:35:00',
  },
  {
    id: 9008,
    userId: 10005,
    userName: '顾南舟',
    userEmail: 'nanzhou.gu@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=GuNanzhou',
    contact: '邮箱：nanzhou.gu@example.com',
    type: FeedbackType.SUGGESTION,
    content:
      '希望管理后台的标签和分类检索支持拼音首字母匹配，目前中文全称输入成本有点高，特别是标签多的时候。',
    images: [],
    status: FeedbackStatus.PENDING,
    reply: null,
    replyTime: null,
    createTime: '2026-04-17T16:20:00',
    updateTime: '2026-04-17T16:20:00',
  },
  {
    id: 9003,
    userId: null,
    userName: '游客用户',
    userEmail: null,
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=GuestFeedback',
    contact: null,
    type: FeedbackType.COMPLAINT,
    content:
      '最近首页出现了多条重复站点，感觉审核速度变慢了，而且举报后迟迟没有结果反馈，影响继续使用的意愿。',
    images: ['https://images.unsplash.com/photo-1516321497487-e288fb19713f?auto=format&fit=crop&w=1200&q=80'],
    status: FeedbackStatus.PENDING,
    reply: null,
    replyTime: null,
    createTime: '2026-04-17T09:10:00',
    updateTime: '2026-04-17T09:10:00',
  },
  {
    id: 8999,
    userId: 9982,
    userName: '程野',
    userEmail: 'chengye@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=ChengYe',
    contact: 'QQ：18273645',
    type: FeedbackType.BUG,
    content:
      'Windows Chrome 下导入收藏夹时，拖入 html 文件后按钮一直 loading，控制台提示文件解析失败，但页面没有给出任何错误提示。',
    images: ['https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1200&q=80'],
    status: FeedbackStatus.PROCESSED,
    reply: '已定位为导入解析分支缺少异常提示，前端错误反馈和兼容补丁已排期，本周会优先修复。',
    replyTime: '2026-04-16T14:12:00',
    createTime: '2026-04-16T10:05:00',
    updateTime: '2026-04-16T14:12:00',
  },
  {
    id: 8995,
    userId: 9975,
    userName: '沈照',
    userEmail: 'shenzhao@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=ShenZhao',
    contact: '邮箱：shenzhao@example.com',
    type: FeedbackType.EXPERIENCE,
    content:
      '新版个人中心的信息分区更清晰了，消息中心按类型聚合后比之前好找很多，希望后面继续保持这种紧凑但不拥挤的节奏。',
    images: [],
    status: FeedbackStatus.PROCESSED,
    reply: '感谢认可，我们会继续保持信息密度与可读性的平衡，也欢迎后续继续反馈具体体验建议。',
    replyTime: '2026-04-15T18:24:00',
    createTime: '2026-04-15T15:40:00',
    updateTime: '2026-04-15T18:24:00',
  },
  {
    id: 8991,
    userId: 9961,
    userName: '白秋',
    userEmail: 'baiqiu@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=BaiQiu',
    contact: null,
    type: FeedbackType.SUGGESTION,
    content:
      '如果首页推荐区可以增加“最近更新”筛选会更方便，我经常想快速看看最近补充的新站点，而不是只看热度排序。',
    images: [],
    status: FeedbackStatus.CLOSED,
    reply: '建议已记录到首页改版需求池，当前阶段先关闭工单，后续若排期进入开发会在更新日志中同步。',
    replyTime: '2026-04-14T11:02:00',
    createTime: '2026-04-14T08:55:00',
    updateTime: '2026-04-14T12:10:00',
  },
  {
    id: 8988,
    userId: 9950,
    userName: '梁序',
    userEmail: 'liangxu@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=LiangXu',
    contact: '手机号：13800001111',
    type: FeedbackType.COMPLAINT,
    content:
      '我提交的网站已经通过审核，但列表里展示的封面图还是旧图，连续刷新也没有变化，导致内容看起来像是提交没有生效。',
    images: ['https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80'],
    status: FeedbackStatus.CLOSED,
    reply: '封面缓存已重新刷新，异常窗口已结束。当前问题无法稳定复现，本次工单先关闭，如再次出现请继续补充截图。',
    replyTime: '2026-04-13T17:40:00',
    createTime: '2026-04-13T11:18:00',
    updateTime: '2026-04-13T19:10:00',
  },
  {
    id: 8982,
    userId: 9926,
    userName: '赵今',
    userEmail: 'zhaojin@example.com',
    avatar: 'https://api.dicebear.com/9.x/notionists/svg?seed=ZhaoJin',
    contact: '邮箱：zhaojin@example.com',
    type: FeedbackType.BUG,
    content:
      '深色模式下管理端分页按钮的文字对比度偏低，特别是在夜间使用时需要仔细看才能分辨当前页和普通页。',
    images: [],
    status: FeedbackStatus.PROCESSED,
    reply: '已同步给前端样式治理任务，后续会统一提高暗色分页组件的对比度和可点击反馈。',
    replyTime: '2026-04-12T10:28:00',
    createTime: '2026-04-12T09:02:00',
    updateTime: '2026-04-12T10:28:00',
  },
]

function wait(ms = MOCK_DELAY) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms)
  })
}

function cloneRecord(record: AdminFeedbackListItem): AdminFeedbackListItem {
  return {
    ...record,
    images: [...record.images],
  }
}

function formatDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function buildSummaryText(record: AdminFeedbackListItem): string {
  if (record.status === FeedbackStatus.PENDING) {
    return record.images.length > 0
      ? '当前为待处理工单，且用户提供了截图证据，建议优先进入详情核验。'
      : '当前为待处理工单，建议结合联系方式评估是否需要优先回复。'
  }

  if (record.status === FeedbackStatus.PROCESSED) {
    return '该工单已回复完成，可在详情中查看回复内容与最后更新时间。'
  }

  return '该工单已关闭，当前仅保留原始反馈与历史回复内容供复盘查阅。'
}

function buildTimeline(record: AdminFeedbackListItem): AdminFeedbackTimelineItem[] {
  const timeline: AdminFeedbackTimelineItem[] = [
    {
      id: `created-${record.id}`,
      title: '用户提交反馈',
      description: `${record.userName} 提交了${getFeedbackTypeMeta(record.type).label}工单。`,
      time: formatDateTime(record.createTime),
      tone: 'done',
    },
  ]

  if (record.reply && record.replyTime) {
    timeline.push({
      id: `replied-${record.id}`,
      title: '管理员完成回复',
      description: record.reply,
      time: formatDateTime(record.replyTime),
      tone: 'done',
    })
  }

  if (record.status === FeedbackStatus.CLOSED) {
    timeline.push({
      id: `closed-${record.id}`,
      title: '工单已关闭',
      description: '当前工单已结束跟进，后续如有新增问题需要重新提交。',
      time: formatDateTime(record.updateTime),
      tone: 'done',
    })
  }

  if (record.status === FeedbackStatus.PENDING) {
    timeline.push({
      id: `waiting-${record.id}`,
      title: '等待管理员处理',
      description: '可进入处理弹窗填写回复，或在确认无需继续跟进后关闭工单。',
      time: '待处理',
      tone: 'pending',
    })
  }

  return timeline
}

function buildDetail(record: AdminFeedbackListItem): AdminFeedbackDetail {
  return {
    ...cloneRecord(record),
    summaryText: buildSummaryText(record),
    timeline: buildTimeline(record),
  }
}

function sortFeedbackRecords(records: AdminFeedbackListItem[]): AdminFeedbackListItem[] {
  return [...records].sort((left, right) => {
    const statusDelta = STATUS_PRIORITY[left.status] - STATUS_PRIORITY[right.status]
    if (statusDelta !== 0) {
      return statusDelta
    }

    return new Date(right.createTime).getTime() - new Date(left.createTime).getTime()
  })
}

function filterFeedbackRecords(query: AdminFeedbackQuery): AdminFeedbackListItem[] {
  const keyword = query.keyword.trim().toLowerCase()

  return sortFeedbackRecords(mockFeedbackRecords).filter((record) => {
    if (typeof query.status === 'number' && record.status !== query.status) {
      return false
    }

    if (typeof query.type === 'number' && record.type !== query.type) {
      return false
    }

    if (query.hasImages && record.images.length === 0) {
      return false
    }

    if (query.hasContact && !record.contact?.trim()) {
      return false
    }

    if (!keyword) {
      return true
    }

    const searchableValues = [
      String(record.id),
      record.userName,
      record.userEmail ?? '',
      record.contact ?? '',
      record.content,
    ]

    return searchableValues.some((value) => value.toLowerCase().includes(keyword))
  })
}

function buildFeedbackStatistics(): AdminFeedbackStatistics {
  const total = mockFeedbackRecords.length
  const pending = mockFeedbackRecords.filter((record) => record.status === FeedbackStatus.PENDING).length
  const processed = mockFeedbackRecords.filter(
    (record) => record.status === FeedbackStatus.PROCESSED,
  ).length
  const closed = mockFeedbackRecords.filter((record) => record.status === FeedbackStatus.CLOSED).length
  const bugCount = mockFeedbackRecords.filter((record) => record.type === FeedbackType.BUG).length
  const withImagesCount = mockFeedbackRecords.filter((record) => record.images.length > 0).length
  const withContactCount = mockFeedbackRecords.filter((record) => Boolean(record.contact?.trim())).length
  const last24Hours = mockFeedbackRecords.filter((record) => {
    const diff = Date.now() - new Date(record.createTime).getTime()
    return diff <= 24 * 60 * 60 * 1000
  }).length

  return {
    total,
    pending,
    processed,
    closed,
    last24Hours,
    bugCount,
    withImagesCount,
    withContactCount,
    replyRate: total === 0 ? 0 : Math.round(((processed + closed) / total) * 100),
  }
}

function getRecordById(feedbackId: number): AdminFeedbackListItem {
  const record = mockFeedbackRecords.find((item) => item.id === feedbackId)
  if (!record) {
    throw new Error('反馈记录不存在')
  }
  return record
}

export async function getAdminFeedbackList(query: AdminFeedbackQuery): Promise<AdminFeedbackPage> {
  await wait()

  const filteredRecords = filterFeedbackRecords(query)
  const total = filteredRecords.length
  const pageNum = Math.max(1, Math.trunc(query.pageNum))
  const pageSize = Math.max(1, Math.trunc(query.pageSize))
  const totalPages = Math.max(1, Math.ceil(total / pageSize))
  const normalizedPageNum = Math.min(pageNum, totalPages)
  const startIndex = (normalizedPageNum - 1) * pageSize
  const pageRecords = filteredRecords
    .slice(startIndex, startIndex + pageSize)
    .map((record) => cloneRecord(record))

  return {
    records: pageRecords,
    total,
    pageNum: normalizedPageNum,
    pageSize,
    totalPages,
  }
}

export async function getAdminFeedbackDetail(feedbackId: number): Promise<AdminFeedbackDetail> {
  await wait()
  return buildDetail(getRecordById(feedbackId))
}

export async function replyAdminFeedback(
  feedbackId: number,
  payload: { reply: string },
): Promise<AdminFeedbackHandleResult> {
  await wait()
  const record = getRecordById(feedbackId)
  if (record.status === FeedbackStatus.CLOSED) {
    throw new Error('已关闭反馈不支持继续回复')
  }

  const now = new Date().toISOString()
  record.reply = payload.reply.trim()
  record.replyTime = now
  record.status = FeedbackStatus.PROCESSED
  record.updateTime = now

  return {
    feedbackId: record.id,
    status: record.status,
    reply: record.reply,
    replyTime: record.replyTime,
  }
}

export async function closeAdminFeedback(feedbackId: number): Promise<AdminFeedbackHandleResult> {
  await wait()
  const record = getRecordById(feedbackId)
  if (record.status === FeedbackStatus.CLOSED) {
    throw new Error('该反馈已关闭，请勿重复操作')
  }

  record.status = FeedbackStatus.CLOSED
  record.updateTime = new Date().toISOString()

  return {
    feedbackId: record.id,
    status: record.status,
    reply: record.reply,
    replyTime: record.replyTime,
  }
}

export async function getAdminFeedbackStatistics(): Promise<AdminFeedbackStatistics> {
  await wait(120)
  return buildFeedbackStatistics()
}
