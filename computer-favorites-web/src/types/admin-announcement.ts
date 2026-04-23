import type { AdminSelectOption } from '@/components/admin/common/AdminSelect.vue'

export type AdminAnnouncementType = 1 | 2 | 3
export type AdminAnnouncementStatus = 0 | 1

export interface AdminAnnouncementRecord {
  id: number
  title: string
  content: string
  type: AdminAnnouncementType
  isTop: 0 | 1
  status: AdminAnnouncementStatus
  publishTime: string | null
  createTime: string
  updateTime: string
}

export interface AdminAnnouncementViewItem extends AdminAnnouncementRecord {
  typeLabel: string
  statusLabel: string
  topLabel: string
  publishTimeText: string
  contentPreview: string
}

export interface AdminAnnouncementQuery {
  pageNum: number
  pageSize: number
  keyword: string
  status: AdminAnnouncementStatus | null
  type: AdminAnnouncementType | null
  isTop: boolean | null
}

export interface AdminAnnouncementStatistics {
  total: number
  visible: number
  hidden: number
  top: number
}

export interface AdminAnnouncementFormModel {
  title: string
  content: string
  type: AdminAnnouncementType
  isTop: 0 | 1
  status: AdminAnnouncementStatus
  publishTime: string
}

export const ADMIN_ANNOUNCEMENT_TYPE_OPTIONS: AdminSelectOption[] = [
  { label: '全部类型', value: null },
  { label: '新增内容', value: 1 },
  { label: 'Bug 修复', value: 2 },
  { label: '系统更新', value: 3 },
]

export const ADMIN_ANNOUNCEMENT_STATUS_OPTIONS: AdminSelectOption[] = [
  { label: '全部状态', value: null },
  { label: '显示中', value: 1 },
  { label: '已隐藏', value: 0 },
]

export const ADMIN_ANNOUNCEMENT_TOP_OPTIONS: AdminSelectOption[] = [
  { label: '全部置顶状态', value: null },
  { label: '仅看置顶', value: 1 },
  { label: '仅看普通', value: 0 },
]

export const ADMIN_ANNOUNCEMENT_TYPE_LABEL_MAP: Record<AdminAnnouncementType, string> = {
  1: '新增内容',
  2: 'Bug 修复',
  3: '系统更新',
}

export const ADMIN_ANNOUNCEMENT_STATUS_LABEL_MAP: Record<AdminAnnouncementStatus, string> = {
  0: '已隐藏',
  1: '显示中',
}

export function createAdminAnnouncementFormModel(): AdminAnnouncementFormModel {
  return {
    title: '',
    content: '',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '',
  }
}

export function resolveAdminAnnouncementTypeLabel(type: AdminAnnouncementType): string {
  return ADMIN_ANNOUNCEMENT_TYPE_LABEL_MAP[type]
}

export function resolveAdminAnnouncementStatusLabel(status: AdminAnnouncementStatus): string {
  return ADMIN_ANNOUNCEMENT_STATUS_LABEL_MAP[status]
}

export function resolveAdminAnnouncementTopLabel(isTop: 0 | 1): string {
  return isTop === 1 ? '置顶中' : '普通公告'
}

export function resolveAnnouncementPublishTimeText(
  publishTime: string | null,
  createTime: string,
): string {
  return publishTime?.trim() || createTime
}

export function resolveAnnouncementContentPreview(content: string, maxLength = 80): string {
  const normalized = content.replace(/\s+/g, ' ').trim()
  if (normalized.length <= maxLength) {
    return normalized
  }
  return `${normalized.slice(0, maxLength)}...`
}

export function mapAdminAnnouncementViewItem(
  item: AdminAnnouncementRecord,
): AdminAnnouncementViewItem {
  return {
    ...item,
    typeLabel: resolveAdminAnnouncementTypeLabel(item.type),
    statusLabel: resolveAdminAnnouncementStatusLabel(item.status),
    topLabel: resolveAdminAnnouncementTopLabel(item.isTop),
    publishTimeText: resolveAnnouncementPublishTimeText(item.publishTime, item.createTime),
    contentPreview: resolveAnnouncementContentPreview(item.content),
  }
}

// ========== API 请求/响应类型 ==========

export interface AdminAnnouncementPageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
  type?: number
  isTop?: number
}

export interface AdminAnnouncementCreatePayload {
  title: string
  content: string
  type: AdminAnnouncementType
  isTop: 0 | 1
  status: AdminAnnouncementStatus
  publishTime: string
}

export interface AdminAnnouncementEditPayload extends AdminAnnouncementCreatePayload {}

export interface AdminAnnouncementStatusPayload {
  status: AdminAnnouncementStatus
}

export interface AdminAnnouncementPage {
  records: AdminAnnouncementRecord[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  stats: AdminAnnouncementStatistics
}
