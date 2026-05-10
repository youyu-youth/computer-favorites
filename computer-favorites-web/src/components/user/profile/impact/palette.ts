/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传网站影响力看板 — 赛博霓虹（Cyber Neon）5 指标色板与色彩工具
 *
 * 5 指标对应：
 *  - browse  电青   cyan
 *  - like    霓虹粉 pink
 *  - collect 霓虹黄 yellow
 *  - comment 电紫蓝 indigo
 *  - score   星云紫 purple
 */

export type MetricKey = 'browse' | 'like' | 'collect' | 'comment' | 'score'

/**
 * 5 指标色板（双主题）。
 * Light：偏 Tailwind *-500/600，深色背景下保持饱和度；
 * Dark ：偏 Tailwind *-300/400，亮度抬高以适配黑底霓虹感。
 */
export const IMPACT_PALETTE: Record<MetricKey, { light: string; dark: string }> = {
  browse: { light: '#06b6d4', dark: '#22d3ee' },
  like: { light: '#ec4899', dark: '#f472b6' },
  collect: { light: '#eab308', dark: '#facc15' },
  comment: { light: '#6366f1', dark: '#818cf8' },
  score: { light: '#a855f7', dark: '#c084fc' },
}

/** 5 指标顺序（与 legend / gauge 段叠加顺序保持一致） */
export const METRIC_ORDER: MetricKey[] = ['browse', 'like', 'collect', 'comment', 'score']

/** 5 指标中文 label */
export const metricLabel: Record<MetricKey, string> = {
  browse: '浏览',
  like: '点赞',
  collect: '收藏',
  comment: '评论',
  score: '评分',
}

/**
 * 解析 #RGB 或 #RRGGBB 为 [r, g, b]
 */
const parseHex = (hex: string): [number, number, number] => {
  const m = hex.replace('#', '').trim()
  const full =
    m.length === 3
      ? m
          .split('')
          .map((c) => c + c)
          .join('')
      : m
  const v = parseInt(full, 16)
  if (Number.isNaN(v)) {
    return [0, 0, 0]
  }
  return [(v >> 16) & 255, (v >> 8) & 255, v & 255]
}

/**
 * 将 hex 颜色转为带 alpha 的 rgba() 字符串。
 *
 * @param hex   #RGB 或 #RRGGBB
 * @param alpha 0~1 之间的透明度
 */
export const withAlpha = (hex: string, alpha: number): string => {
  const [r, g, b] = parseHex(hex)
  const clamped = Math.min(1, Math.max(0, alpha))
  return `rgba(${r}, ${g}, ${b}, ${clamped})`
}

/**
 * 线性插值。常用于「柱透明度按 value/max 在 [0.35, 1.0] 间映射」。
 */
export const lerp = (a: number, b: number, t: number): number => {
  const clamped = Math.min(1, Math.max(0, t))
  return a + (b - a) * clamped
}

/**
 * 取对应主题下的指标色 hex
 */
export const getMetricHex = (metric: MetricKey, isDark: boolean): string => {
  const pair = IMPACT_PALETTE[metric]
  return isDark ? pair.dark : pair.light
}
