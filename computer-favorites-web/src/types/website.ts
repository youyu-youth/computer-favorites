export interface Website {
  id: number
  title: string
  desc: string
  tag: string
  hot: string
  url?: string
}

export interface Category {
  name: string
  color: string
}

export type FilterLogic = 'OR' | 'AND'
