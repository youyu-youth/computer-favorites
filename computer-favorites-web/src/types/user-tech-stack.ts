/**
 * 用户端技术栈选项类型
 *
 * 用于"擅长技术栈"选择对话框，从字典中读取启用的技术栈供用户多选。
 *
 * @author yyyouth zg
 */
export interface UserTechStackOption {
  /** 主键 */
  id: number
  /** 技术名称（唯一） */
  name: string
  /** 图标 URL，可空（空时由前端展示首字母 fallback） */
  iconPng: string
  /** 简介，建议不超过 60 字 */
  description: string
  /** 品牌主色 #RRGGBB */
  color: string
  /** 官方站点（可选） */
  officialUrl?: string
  /** 字典排序权重，升序 */
  sort: number
}
