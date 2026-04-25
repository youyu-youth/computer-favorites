/**
 * 用户收藏文件夹相关类型定义
 */

/** 文件夹表单数据（创建/编辑） */
export interface FolderFormData {
  /** 文件夹名称 */
  name: string
  /** 文件夹图标（Font Awesome 类名） */
  icon: string
  /** 文件夹颜色（HEX 格式） */
  color: string
  /** 父文件夹 ID，0 表示顶级 */
  parentId: number
  /** 排序值 */
  sort: number
}

/** 父文件夹选项（简化版，用于下拉选择） */
export interface FolderOption {
  id: number
  name: string
  icon?: string
  color?: string
}

/** 颜色预设项 */
export interface ColorPreset {
  label: string
  value: string
}

/** 创建文件夹返回结果 */
export interface UserFolderCreateResult {
  id: number
  name: string
  icon: string
  color: string
  parentId: number
  sort: number
}
