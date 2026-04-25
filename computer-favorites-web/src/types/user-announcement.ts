import type { UserAnnouncementItem } from '@/types/notification'

export type AnnouncementType = 1 | 2 | 3

export type AnnouncementFilterValue = 'all' | AnnouncementType

export interface AnnouncementFilterOption {
  label: string
  value: AnnouncementFilterValue
}

export interface AnnouncementViewItem extends UserAnnouncementItem {
  typeLabel: string
  displayDate: string
  displayTime: string
  sortTime: string
}

export const ANNOUNCEMENT_TYPE_LABEL_MAP: Record<AnnouncementType, string> = {
  1: '新增内容',
  2: 'Bug 修复',
  3: '系统更新',
}

export const ANNOUNCEMENT_FILTER_OPTIONS: AnnouncementFilterOption[] = [
  { label: '全部', value: 'all' },
  { label: '新增内容', value: 1 },
  { label: 'Bug 修复', value: 2 },
  { label: '系统更新', value: 3 },
]

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

export function mapAnnouncementViewItem(item: UserAnnouncementItem): AnnouncementViewItem {
  const sortTime = item.publishTime

  return {
    ...item,
    typeLabel: ANNOUNCEMENT_TYPE_LABEL_MAP[item.type as AnnouncementType] || '未知类型',
    displayDate: sortTime ? formatAnnouncementDate(sortTime) : '-',
    displayTime: sortTime ? formatAnnouncementTime(sortTime) : '-',
    sortTime: sortTime || '',
  }
}

export function resolveAnnouncementFilterLabel(value: AnnouncementFilterValue): string {
  if (value === 'all') {
    return '全部'
  }

  return ANNOUNCEMENT_TYPE_LABEL_MAP[value]
}
