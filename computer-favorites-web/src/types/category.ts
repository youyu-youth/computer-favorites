export type AdminCategoryStatus = 'ACTIVE' | 'DISABLED'

export interface AdminCategoryItem {
  id: number
  name: string
  description?: string
  icon?: string
  parentId: number | null
  sort: number
  status: AdminCategoryStatus
  createdAt: string
  updatedAt: string
  children?: AdminCategoryItem[]
}

export interface AdminCategoryFormModel {
  name: string
  description: string
  icon: string
  parentId: number | null
  sort: number
  status: AdminCategoryStatus
}

export type AdminCategorySortField = 'createdAt' | 'updatedAt' | 'sort'
export type AdminCategorySortOrder = 1 | -1
