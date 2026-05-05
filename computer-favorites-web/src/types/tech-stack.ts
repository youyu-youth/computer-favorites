export type AdminTechStackStatus = 'ACTIVE' | 'DISABLED'

export type AdminTechStackStatusValue = 0 | 1

export interface AdminTechStackItem {
  id: number
  name: string
  iconPng: string
  officialUrl: string
  description: string
  color: string
  status: AdminTechStackStatus
  sort: number
  createdAt: string
  updatedAt: string
  userCount: number
}

export interface AdminTechStackFormModel {
  name: string
  iconPng: string
  officialUrl: string
  description: string
  color: string
  sort: number
  status: AdminTechStackStatus
}

export interface AdminTechStackCreatePayload {
  name: string
  iconPng?: string
  officialUrl?: string
  description?: string
  color?: string
  sort: number
}

export interface AdminTechStackEditPayload extends AdminTechStackCreatePayload {
  status: AdminTechStackStatusValue
}

export interface AdminTechStackStats {
  total: number
  enabled: number
  disabled: number
}

export type AdminTechStackSortField = 'sort' | 'name' | 'status' | 'createdAt' | 'updatedAt'
export type AdminTechStackSortOrder = 1 | -1

export interface AdminTechStackPage {
  records: AdminTechStackItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface AdminTechStackPageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: AdminTechStackStatusValue
  sortField?: AdminTechStackSortField
  sortOrder?: AdminTechStackSortOrder
}

export interface AdminTechStackBatchDeletePayload {
  techStackIds: number[]
}

export interface AdminTechStackIconUploadResult {
  objectKey: string
  iconUrl: string
}
