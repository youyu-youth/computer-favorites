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

export type AdminTechStackSortField = 'sort' | 'createdAt' | 'updatedAt'
export type AdminTechStackSortOrder = 1 | -1
