import type { CSSProperties } from 'vue'

const DEFAULT_TAG_COLOR = '#409EFF'
const HEX_COLOR_PATTERN = /^#?[0-9A-Fa-f]{6}$/

const toRgb = (hexColor: string): [number, number, number] => {
  const normalized = hexColor.replace('#', '')
  const r = Number.parseInt(normalized.slice(0, 2), 16)
  const g = Number.parseInt(normalized.slice(2, 4), 16)
  const b = Number.parseInt(normalized.slice(4, 6), 16)
  return [r, g, b]
}

const calcLuminance = (r: number, g: number, b: number): number => {
  return (0.299 * r + 0.587 * g + 0.114 * b) / 255
}

const brightenChannel = (value: number, ratio: number): number => {
  return Math.round(value + (255 - value) * ratio)
}

const darkenChannel = (value: number, ratio: number): number => {
  return Math.round(value * (1 - ratio))
}

export const normalizeTagColor = (color?: string): string => {
  if (!color || !color.trim()) {
    return DEFAULT_TAG_COLOR
  }

  const normalized = color.trim().toUpperCase()
  if (!HEX_COLOR_PATTERN.test(normalized)) {
    return DEFAULT_TAG_COLOR
  }
  return normalized.startsWith('#') ? normalized : `#${normalized}`
}

export const buildTagColorStyle = (color?: string): CSSProperties => {
  const normalizedColor = normalizeTagColor(color)
  const [r, g, b] = toRgb(normalizedColor)
  const luminance = calcLuminance(r, g, b)

  let textR = r
  let textG = g
  let textB = b

  if (luminance > 0.72) {
    textR = darkenChannel(r, 0.55)
    textG = darkenChannel(g, 0.55)
    textB = darkenChannel(b, 0.55)
  } else if (luminance < 0.35) {
    textR = brightenChannel(r, 0.35)
    textG = brightenChannel(g, 0.35)
    textB = brightenChannel(b, 0.35)
  }

  return {
    backgroundColor: `rgba(${r}, ${g}, ${b}, 0.14)`,
    color: `rgb(${textR}, ${textG}, ${textB})`,
    borderLeftColor: `rgba(${r}, ${g}, ${b}, 0.95)`,
  }
}
