export type AdminTagDeletedValue = 0 | 1

export type AdminTagSortField =
  | 'id'
  | 'name'
  | 'color'
  | 'useCount'
  | 'createTime'
  | 'updateTime'

export type AdminTagSortOrder = 1 | -1

export type AdminTagEditorMode = 'create' | 'edit'

export interface AdminTagItem {
  id: number
  name: string
  color: string
  useCount: number
  createTime: string
  updateTime: string
  deleted: AdminTagDeletedValue
}

export interface AdminTagStats {
  total: number
  inUse: number
  unused: number
  updatedToday: number
}

export interface AdminTagFormModel {
  name: string
  color: string
}

export interface AdminTagFormErrors {
  name: string
  color: string
}
