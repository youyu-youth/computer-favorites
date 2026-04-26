/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏夹页面相关类型定义
 */

/** 收藏的网站条目（对齐后端 UserCollectItemVO） */
export interface CollectionWebsite {
  id: number
  websiteId: number
  websiteName: string
  websiteUrl: string
  websiteIcon: string
  websiteSummary: string
  websiteTags: string
  likeCount: number
  collectTime: string
  folderId: number
  folderName: string
}

/** 收藏文件夹树节点（对齐后端 UserFolderTreeVO） */
export interface CollectionCategory {
  id: number
  name: string
  icon: string
  color: string
  parentId: number
  sort: number
  websiteCount: number
  isHide: number
  isDefault: number
  children: CollectionCategory[]
}

/** 快捷访问项（前端配置，不依赖后端） */
export interface CollectionQuickAccess {
  key: string
  label: string
  icon: string
}

/** 收藏列表分页结果（对齐后端 UserCollectPageVO） */
export interface CollectPageResult {
  records: CollectionWebsite[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/** 收藏统计（对齐后端 UserCollectStatsVO） */
export interface CollectStats {
  collectCount: number
  folderCount: number
}

/** 视图模式 */
export type ViewMode = 'grid' | 'list'
