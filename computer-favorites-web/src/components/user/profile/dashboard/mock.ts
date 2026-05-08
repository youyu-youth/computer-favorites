/**
 * @author yyyouth zg
 * @date 2026-05-07
 * Dashboard 看板 mock 数据生成
 */
import { githubStatsPalette } from './theme'
import type { DashboardItem, LangShare, RadarSkill, RankCardStat, SparklinePoint } from './types'

const today = new Date()

const categoryPool = ['前端框架', '后端服务', '数据库', 'AI工具', '开发效率', '设计资源']
const typePool = [
  '教程文章',
  '工具站点',
  '开源项目',
  '官方文档',
  '社区论坛',
  '模板素材',
  '视频课程',
  '实战案例',
]

const formatDate = (date: Date): string => date.toISOString().slice(0, 10)

/**
 * 生成 180 天的看板数据
 */
export const buildDashboardItems = (): DashboardItem[] =>
  Array.from({ length: 180 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - index)
    return {
      date: formatDate(date),
      category: categoryPool[index % categoryPool.length] || '未分类',
      contentType: typePool[(index * 3) % typePool.length] || '其他',
      pv: 1100 + ((index * 97) % 4800),
      likes: 35 + ((index * 29) % 360),
      favorites: 20 + ((index * 31) % 240),
    }
  })

/**
 * 把看板数据按字段抽出 sparkline 序列（按日期升序）
 */
export const toSparkline = (items: DashboardItem[], field: keyof DashboardItem): SparklinePoint[] =>
  [...items]
    .sort((a, b) => a.date.localeCompare(b.date))
    .map((item) => ({ date: item.date, value: Number(item[field]) || 0 }))

/**
 * 默认 Top Languages mock（GitHub Stats 风格的语言占比堆叠）
 */
export const buildTopLanguages = (skillNames: string[]): LangShare[] => {
  const baseColors = [
    githubStatsPalette.primary,
    githubStatsPalette.emerald,
    githubStatsPalette.cyan,
    githubStatsPalette.blue,
    githubStatsPalette.rose,
    githubStatsPalette.lime,
    githubStatsPalette.amber,
    githubStatsPalette.slate,
  ]
  const fallback = ['Vue', 'TypeScript', 'Java', 'Go', 'Python', 'SQL', 'Shell', 'Markdown']
  const names = skillNames.length >= 4 ? skillNames.slice(0, 8) : fallback
  // 生成稳定占比（降序）
  const weights = names.map((_, i) => Math.max(4, 32 - i * 4 + ((i * 7) % 5)))
  const total = weights.reduce((sum, w) => sum + w, 0)
  return names.map((name, i) => ({
    name,
    pct: Math.round(((weights[i] ?? 1) / total) * 1000) / 10,
    color: baseColors[i % baseColors.length] ?? githubStatsPalette.slate,
  }))
}

/**
 * 雷达图能力维度 mock
 */
export const buildRadarSkills = (skillCount: number): RadarSkill[] => {
  const dims = ['前端', '后端', '数据库', 'AI', '工程化', '算法']
  const seed = Math.max(40, Math.min(96, 60 + skillCount * 4))
  return dims.map((name, idx) => ({
    name,
    value: Math.max(35, Math.min(98, seed - idx * 6 + ((idx * 11) % 14))),
  }))
}

/**
 * 贡献概览 6 项 stats（GitHub Stats 主卡）
 */
export const buildRankCardStats = (contributionValue: number): RankCardStat[] => {
  const baseStars = 2200 + (contributionValue % 800)
  const baseCommits = 1000 + (contributionValue % 600)
  return [
    { label: 'Total Stars', value: formatThousands(baseStars), icon: 'star' },
    { label: 'Total Commits', value: formatThousands(baseCommits), icon: 'commit' },
    { label: 'Total PRs', value: String(180 + (contributionValue % 60)), icon: 'pr' },
    { label: 'Total Issues', value: String(80 + (contributionValue % 40)), icon: 'issue' },
    { label: 'Contributed to', value: String(40 + (contributionValue % 30)), icon: 'monitor' },
    { label: 'Streak', value: `${12 + (contributionValue % 18)} days`, icon: 'flame' },
  ]
}

const formatThousands = (n: number): string => {
  if (n >= 1000) {
    return `${(n / 1000).toFixed(1).replace(/\.0$/, '')}k`
  }
  return String(n)
}

/**
 * 根据 0-100 分计算等级文字
 */
export const computeRank = (score: number): { rank: string; color: string } => {
  if (score >= 92) return { rank: 'A++', color: githubStatsPalette.emerald }
  if (score >= 82) return { rank: 'A+', color: githubStatsPalette.cyan }
  if (score >= 70) return { rank: 'A', color: githubStatsPalette.primary }
  if (score >= 55) return { rank: 'B', color: githubStatsPalette.amber }
  return { rank: 'C', color: githubStatsPalette.rose }
}
