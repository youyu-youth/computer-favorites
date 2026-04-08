export type AdminCategoryStatus = 'ACTIVE' | 'DISABLED'

export type AdminCategoryStatusValue = 0 | 1

export interface AdminCategoryTreeNode {
  id: number
  name: string
  description?: string
  icon?: string
  parentId: number
  sort: number
  status: AdminCategoryStatusValue
  createTime?: string
  updateTime?: string
  children?: AdminCategoryTreeNode[]
}

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
  depth?: number
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

export interface AdminCategoryCreatePayload {
  name: string
  description?: string
  icon?: string
  parentId: number
  sort: number
}

export interface AdminCategoryEditPayload {
  name: string
  description?: string
  icon?: string
  parentId: number
  sort: number
  status: AdminCategoryStatusValue
}

export interface AdminCategorySortPayload {
  sort: number
}

export type AdminCategorySortField = 'createdAt' | 'updatedAt' | 'sort'
export type AdminCategorySortOrder = 1 | -1
