/**
 * @author yyyouth zg
 * @date 2026-05-07
 * Dashboard 看板专用类型
 */

export type RangeKey = '7d' | '30d' | '90d' | 'all'

export interface DashboardItem {
  date: string
  category: string
  contentType: string
  pv: number
  likes: number
  favorites: number
}

export interface SparklinePoint {
  date: string
  value: number
}

export interface LangShare {
  name: string
  pct: number
  color: string
}

export interface RadarSkill {
  name: string
  value: number
}

export interface RankCardStat {
  label: string
  value: string
  icon: 'star' | 'commit' | 'pr' | 'issue' | 'monitor' | 'flame'
}
