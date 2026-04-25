/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏夹页面相关类型定义
 */

/** 收藏夹网站条目 */
export interface CollectionWebsite {
  /** 网站ID */
  id: number
  /** 网站名称 */
  name: string
  /** 网站描述 */
  description: string
  /** 网站URL */
  url: string
  /** 网站图标URL */
  icon: string
  /** 标签列表 */
  tags: string[]
  /** 所属分类名称 */
  category: string
  /** 是否星标收藏 */
  isStarred: boolean
  /** 点赞数 */
  likeCount: number
  /** 收藏日期 */
  dateAdded: string
  /** 最后访问时间 */
  lastVisited: string
}

/** 快捷访问项 */
export interface CollectionQuickAccess {
  /** 唯一标识 */
  key: string
  /** 显示名称 */
  label: string
  /** FontAwesome 图标类名 */
  icon: string
}

/** 收藏分类项 */
export interface CollectionCategory {
  /** 分类ID */
  id: number
  /** 分类名称 */
  name: string
  /** FontAwesome 图标类名 */
  icon: string
  /** 该分类下的网站数量 */
  count: number
}

/** 视图模式 */
export type ViewMode = 'grid' | 'list'

/** 面板显示状态 */
export interface PanelState {
  /** 详情面板是否打开 */
  detailOpen: boolean
  /** 移动端侧边栏是否打开 */
  mobileSidebarOpen: boolean
  /** 移动端详情面板是否打开 */
  mobileDetailOpen: boolean
}
