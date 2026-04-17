export type UserMessageTypeValue = 1 | 2 | 3 | 4 | 5

export type UserMessageFilterValue = 'all' | UserMessageTypeValue

export type UserMessageViewType = 'system' | 'comment' | 'favorite' | 'audit' | 'report'

export interface UserMessageTypeOption {
  name: string
  id: UserMessageFilterValue
}

const userMessageTypeMap: Record<UserMessageTypeValue, UserMessageViewType> = {
  1: 'system',
  2: 'comment',
  3: 'favorite',
  4: 'audit',
  5: 'report',
}

export const USER_MESSAGE_TYPE_OPTIONS: UserMessageTypeOption[] = [
  { name: '所有类型', id: 'all' },
  { name: '系统通知', id: 1 },
  { name: '评论回复', id: 2 },
  { name: '收藏提醒', id: 3 },
  { name: '审核结果', id: 4 },
  { name: '举报反馈', id: 5 },
]

export function resolveUserMessageViewType(typeValue: number): UserMessageViewType {
  return userMessageTypeMap[typeValue as UserMessageTypeValue] || 'system'
}
