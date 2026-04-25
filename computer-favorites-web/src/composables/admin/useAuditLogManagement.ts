import { computed, reactive, shallowRef } from 'vue'
import type {
  AuditAction,
  AuditLogDetail,
  AuditLogListItem,
  AuditLogQuery,
  AuditLogStatistics,
  AuditModule,
  AuditUserType,
} from '@/types/audit-log'
import { AuditResult } from '@/types/audit-log'

const DEFAULT_PAGE_SIZE = 10

const MODULES = ['auth', 'website', 'user', 'admin', 'comment', 'report', 'feedback', 'announcement', 'system'] as const

const ACTIONS = ['login', 'logout', 'create', 'update', 'delete', 'review', 'export', 'status', 'ban'] as const

const REQUEST_METHODS = ['GET', 'POST', 'PUT', 'DELETE'] as const

const MODULE_URL_MAP: Record<string, string[]> = {
  auth: ['/api/auth/login', '/api/auth/logout', '/api/auth/register', '/api/auth/refresh', '/api/auth/captcha'],
  website: ['/api/website/create', '/api/website/update', '/api/website/delete', '/api/website/list', '/api/website/detail'],
  user: ['/api/user/profile', '/api/user/update', '/api/user/avatar', '/api/user/password', '/api/user/list'],
  admin: ['/api/admin/dashboard', '/api/admin/config', '/api/admin/user/list', '/api/admin/user/ban', '/api/admin/log'],
  comment: ['/api/comment/create', '/api/comment/delete', '/api/comment/list', '/api/comment/review'],
  report: ['/api/report/create', '/api/report/list', '/api/report/handle', '/api/report/detail'],
  feedback: ['/api/feedback/create', '/api/feedback/list', '/api/feedback/reply', '/api/feedback/detail'],
  announcement: ['/api/announcement/create', '/api/announcement/update', '/api/announcement/delete', '/api/announcement/list'],
  system: ['/api/system/config', '/api/system/cache', '/api/system/log', '/api/system/health'],
}

const ERROR_MESSAGES = [
  '操作失败：权限不足',
  '操作失败：参数校验不通过',
  '操作失败：资源不存在',
  '操作失败：请求超时',
  '操作失败：服务器内部错误',
  '操作失败：数据已存在',
  '操作失败：账户已被禁用',
  '操作失败：token已过期',
]

const IP_SEGMENTS = [
  '192.168', '10.0', '172.16', '223.104', '114.88', '61.148',
  '116.25', '120.36', '183.60', '202.96', '211.161', '58.210',
  '218.76', '123.125', '39.156', '140.207', '36.152', '47.94',
]

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function randomItem<T>(arr: readonly T[]): T {
  return arr[randomInt(0, arr.length - 1)]!
}

function generateRandomIp(): string {
  const segment = randomItem(IP_SEGMENTS)
  const part3 = randomInt(0, 255)
  const part4 = randomInt(1, 254)
  return `${segment}.${part3}.${part4}`
}

function generateRandomDate(daysAgo: number): string {
  const now = Date.now()
  const past = now - daysAgo * 24 * 60 * 60 * 1000
  const timestamp = randomInt(past, now)
  const date = new Date(timestamp)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function generateMockAuditLogs(count: number): AuditLogDetail[] {
  const logs: AuditLogDetail[] = []

  for (let i = 1; i <= count; i++) {
    const mod = randomItem(MODULES)
    const action = randomItem(ACTIONS)
    const isFailed = Math.random() < 0.15
    const userType = Math.random() < 0.7 ? 'user' : 'admin'
    const userId = randomInt(1, 100)
    const urls = MODULE_URL_MAP[mod] || ['/api/unknown']
    const requestUrl = randomItem(urls)
    const requestMethod = randomItem(REQUEST_METHODS)
    const createTime = generateRandomDate(30)

    logs.push({
      id: i,
      userId,
      userType,
      module: mod,
      action,
      targetType: mod,
      targetId: randomInt(1, 500),
      result: isFailed ? AuditResult.Failed : AuditResult.Success,
      ip: generateRandomIp(),
      requestUrl,
      requestMethod,
      errorMsg: isFailed ? randomItem(ERROR_MESSAGES) : null,
      createTime,
    })
  }

  logs.sort((a, b) => b.createTime.localeCompare(a.createTime))

  return logs
}

function maskIp(ip: string): string {
  const parts = ip.split('.')
  if (parts.length !== 4) {
    return '*.*.*.*'
  }
  return `${parts[0]}.${parts[1]}.*.*`
}

function createVisiblePages(totalPages: number, currentPage: number): Array<number | string> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  const pages: Array<number | string> = [1]

  if (currentPage > 3) {
    pages.push('...')
  }

  for (let page = Math.max(2, currentPage - 1); page <= Math.min(totalPages - 1, currentPage + 1); page += 1) {
    pages.push(page)
  }

  if (currentPage < totalPages - 2) {
    pages.push('...')
  }

  pages.push(totalPages)
  return pages
}

export function useAuditLogManagement() {
  const loading = shallowRef(false)
  const allLogs = shallowRef<AuditLogDetail[]>(generateMockAuditLogs(120))

  const query = reactive<AuditLogQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    module: null,
    action: null,
    userType: null,
    result: null,
    startTime: null,
    endTime: null,
    keyword: '',
  })

  const detailOpen = shallowRef(false)
  const detailRecord = shallowRef<AuditLogDetail | null>(null)

  const filteredLogs = computed<AuditLogDetail[]>(() => {
    let result = allLogs.value

    if (query.module !== null) {
      result = result.filter((log) => log.module === query.module)
    }

    if (query.action !== null) {
      result = result.filter((log) => log.action === query.action)
    }

    if (query.userType !== null) {
      result = result.filter((log) => log.userType === query.userType)
    }

    if (query.result !== null) {
      result = result.filter((log) => log.result === query.result)
    }

    if (query.startTime !== null) {
      const start = query.startTime
      result = result.filter((log) => log.createTime >= start)
    }

    if (query.endTime !== null) {
      const end = query.endTime
      result = result.filter((log) => log.createTime <= end)
    }

    if (query.keyword !== '') {
      const kw = query.keyword.toLowerCase()
      result = result.filter((log) => {
        const idMatch = String(log.id).includes(kw)
        const userIdMatch = String(log.userId).includes(kw)
        const ipMatch = log.ip.toLowerCase().includes(kw)
        const moduleMatch = log.module.toLowerCase().includes(kw)
        const actionMatch = log.action.toLowerCase().includes(kw)
        return idMatch || userIdMatch || ipMatch || moduleMatch || actionMatch
      })
    }

    return result
  })

  const totalItems = computed(() => filteredLogs.value.length)

  const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / query.pageSize)))

  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))

  const pagedLogs = computed<AuditLogListItem[]>(() => {
    const startIndex = (query.pageNum - 1) * query.pageSize
    const endIndex = startIndex + query.pageSize
    const slice = filteredLogs.value.slice(startIndex, endIndex)

    return slice.map((log) => ({
      id: log.id,
      userId: log.userId,
      userType: log.userType,
      module: log.module,
      action: log.action,
      targetType: log.targetType,
      targetId: log.targetId,
      result: log.result,
      ip: maskIp(log.ip),
      requestUrl: log.requestUrl,
      requestMethod: log.requestMethod,
      createTime: log.createTime,
    }))
  })

  const statistics = computed<AuditLogStatistics>(() => {
    const now = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`

    const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
    const weekStr = `${sevenDaysAgo.getFullYear()}-${pad(sevenDaysAgo.getMonth() + 1)}-${pad(sevenDaysAgo.getDate())}`

    const total = allLogs.value.length
    let todayCount = 0
    let successCount = 0
    let failCount = 0
    let weekCount = 0

    allLogs.value.forEach((log) => {
      if (log.createTime.startsWith(todayStr)) {
        todayCount += 1
      }
      if (log.result === AuditResult.Success) {
        successCount += 1
      } else {
        failCount += 1
      }
      if (log.createTime >= weekStr) {
        weekCount += 1
      }
    })

    const successRate = total > 0 ? Math.round((successCount / total) * 100) : 0

    return {
      todayCount,
      successRate,
      failCount,
      weekCount,
    }
  })

  const setModule = (value: string | number | null): void => {
    query.module = (value === null || value === '' ? null : value) as AuditModule | null
    query.pageNum = 1
  }

  const setAction = (value: string | number | null): void => {
    query.action = (value === null || value === '' ? null : value) as AuditAction | null
    query.pageNum = 1
  }

  const setUserType = (value: string | number | null): void => {
    query.userType = (value === null || value === '' ? null : value) as AuditUserType | null
    query.pageNum = 1
  }

  const setResult = (value: string | number | null): void => {
    query.result = value === null || value === '' ? null : (typeof value === 'number' ? value : Number(value)) as AuditResult
    query.pageNum = 1
  }

  const setStartTime = (value: string | number | null): void => {
    query.startTime = value === null || value === '' ? null : String(value)
    query.pageNum = 1
  }

  const setEndTime = (value: string | number | null): void => {
    query.endTime = value === null || value === '' ? null : String(value)
    query.pageNum = 1
  }

  const setKeyword = (value: string | number | null): void => {
    query.keyword = value === null ? '' : String(value)
    query.pageNum = 1
  }

  const resetFilters = (): void => {
    query.module = null
    query.action = null
    query.userType = null
    query.result = null
    query.startTime = null
    query.endTime = null
    query.keyword = ''
    query.pageNum = 1
  }

  const prevPage = (): void => {
    if (query.pageNum <= 1) {
      return
    }
    query.pageNum -= 1
  }

  const nextPage = (): void => {
    if (query.pageNum >= totalPages.value) {
      return
    }
    query.pageNum += 1
  }

  const goToPage = (page: number | string): void => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    const normalizedPage = Math.min(totalPages.value, Math.max(1, Math.trunc(page)))
    if (normalizedPage === query.pageNum) {
      return
    }
    query.pageNum = normalizedPage
  }

  const openDetail = (id: number): void => {
    const found = allLogs.value.find((log) => log.id === id) ?? null
    detailRecord.value = found
    detailOpen.value = true
  }

  const closeDetail = (): void => {
    detailOpen.value = false
    detailRecord.value = null
  }

  const exportLogs = (): void => {
    const BOM = '\uFEFF'
    const headers = ['ID', '用户ID', '用户类型', '模块', '动作', '目标类型', '目标ID', '结果', 'IP', '请求URL', '请求方法', '创建时间']
    const rows = filteredLogs.value.map((log) => [
      String(log.id),
      String(log.userId),
      log.userType,
      log.module,
      log.action,
      log.targetType,
      String(log.targetId),
      log.result === AuditResult.Success ? '成功' : '失败',
      log.ip,
      log.requestUrl,
      log.requestMethod,
      log.createTime,
    ])

    const csvContent = BOM + [headers.join(','), ...rows.map((row) => row.map((cell) => `"${cell}"`).join(','))].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `audit-log-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  return {
    loading,
    query,
    pagedLogs,
    totalItems,
    totalPages,
    visiblePages,
    filteredLogs,
    statistics,
    detailOpen,
    detailRecord,
    setModule,
    setAction,
    setUserType,
    setResult,
    setStartTime,
    setEndTime,
    setKeyword,
    resetFilters,
    prevPage,
    nextPage,
    goToPage,
    openDetail,
    closeDetail,
    exportLogs,
  }
}
