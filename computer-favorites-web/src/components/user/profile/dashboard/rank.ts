import { githubStatsPalette } from './theme'

export const computeRank = (score: number): { rank: string; color: string } => {
  if (score >= 92) return { rank: 'A++', color: githubStatsPalette.emerald }
  if (score >= 82) return { rank: 'A+', color: githubStatsPalette.cyan }
  if (score >= 70) return { rank: 'A', color: githubStatsPalette.primary }
  if (score >= 55) return { rank: 'B', color: githubStatsPalette.amber }
  return { rank: 'C', color: githubStatsPalette.rose }
}
