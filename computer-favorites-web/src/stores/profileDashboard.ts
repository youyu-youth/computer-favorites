/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页看板 Pinia Store（user-15 M3）。
 *
 * 设计要点：
 *  - 5 个卡片状态独立，单点失败不影响其他卡片；
 *  - overview/category/radar 仅首屏拉取一次，可 refresh；
 *  - graph 按 year 切换拉取；
 *  - trend 按 (range, metric) 切换拉取；
 *  - 首屏并发：loadInitial 用 Promise.allSettled 触发 overview/graph(当前年)/category/radar/trend(30d/pv)。
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  getCategoryDistribution,
  getContributionGraph,
  getDashboardOverview,
  getTechRadar,
  getTrendSeries,
  type DashboardRequestContext,
  type CategoryDistribution,
  type ContributionGraph,
  type DashboardOverview,
  type TechRadar,
  type TrendMetric,
  type TrendRange,
  type TrendSeries,
} from '@/api/user-profile-dashboard'

type LoadState = 'idle' | 'loading' | 'success' | 'error'

const CACHE_TTL_MS = 5 * 60 * 1000

interface SectionState<T> {
  data: T | null
  state: LoadState
  error: string
}

const initial = <T>(): SectionState<T> => ({ data: null, state: 'idle', error: '' })

export const useProfileDashboardStore = defineStore('profileDashboard', () => {
  const overview = ref<SectionState<DashboardOverview>>(initial())
  const graph = ref<SectionState<ContributionGraph>>(initial())
  const category = ref<SectionState<CategoryDistribution>>(initial())
  const radar = ref<SectionState<TechRadar>>(initial())
  const trend = ref<SectionState<TrendSeries>>(initial())

  const currentYear = ref<number>(new Date().getFullYear())
  const trendRange = ref<TrendRange>('30d')
  const trendMetric = ref<TrendMetric>('pv')
  const requestContext = ref<DashboardRequestContext>({})
  const lastInitialLoadedAt = ref(0)
  const lastInitialCacheKey = ref('')

  const loadingAny = computed(() =>
    [overview, graph, category, radar, trend].some((s) => s.value.state === 'loading'),
  )

  const handle = async <T>(
    target: { value: SectionState<T> },
    loader: () => Promise<T>,
  ): Promise<void> => {
    target.value = { data: target.value.data, state: 'loading', error: '' }
    try {
      const data = await loader()
      target.value = { data, state: 'success', error: '' }
    } catch (e) {
      const msg = e instanceof Error ? e.message : '加载失败'
      target.value = { data: target.value.data, state: 'error', error: msg }
    }
  }

  const setContext = (context?: DashboardRequestContext) => {
    requestContext.value = context?.username ? { username: context.username } : {}
  }

  const buildInitialCacheKey = (context?: DashboardRequestContext) =>
    context?.username ? `public:${context.username}` : 'self'

  const isInitialCacheFresh = (cacheKey: string) =>
    lastInitialCacheKey.value === cacheKey &&
    lastInitialLoadedAt.value > 0 &&
    Date.now() - lastInitialLoadedAt.value < CACHE_TTL_MS &&
    [overview, graph, category, radar, trend].every((section) => section.value.state === 'success')

  const loadOverview = () => handle(overview, () => getDashboardOverview(requestContext.value))
  const loadCategory = () => handle(category, () => getCategoryDistribution(requestContext.value))
  const loadRadar = () => handle(radar, () => getTechRadar(requestContext.value))

  const loadGraph = (year?: number) => {
    if (year && year !== currentYear.value) {
      currentYear.value = year
    }
    const y = year ?? currentYear.value
    return handle(graph, () => getContributionGraph(y, requestContext.value))
  }

  const loadTrend = (range?: TrendRange, metric?: TrendMetric) => {
    if (range && range !== trendRange.value) trendRange.value = range
    if (metric && metric !== trendMetric.value) trendMetric.value = metric
    return handle(trend, () => getTrendSeries(trendRange.value, trendMetric.value, requestContext.value))
  }

  /** 首屏并发加载全部卡片 */
  const loadInitial = async (context?: DashboardRequestContext): Promise<void> => {
    const cacheKey = buildInitialCacheKey(context)
    if (isInitialCacheFresh(cacheKey)) {
      return
    }
    setContext(context)
    await Promise.allSettled([loadOverview(), loadGraph(), loadCategory(), loadRadar(), loadTrend()])
    if ([overview, graph, category, radar, trend].some((section) => section.value.state === 'success')) {
      lastInitialLoadedAt.value = Date.now()
      lastInitialCacheKey.value = cacheKey
    }
  }

  /** 切换热力图年份 */
  const setGraphYear = (year: number) => loadGraph(year)

  /** 切换趋势图 range / metric */
  const setTrend = (range: TrendRange, metric: TrendMetric) => loadTrend(range, metric)

  const reset = () => {
    overview.value = initial()
    graph.value = initial()
    category.value = initial()
    radar.value = initial()
    trend.value = initial()
    currentYear.value = new Date().getFullYear()
    trendRange.value = '30d'
    trendMetric.value = 'pv'
    requestContext.value = {}
    lastInitialLoadedAt.value = 0
    lastInitialCacheKey.value = ''
  }

  return {
    overview,
    graph,
    category,
    radar,
    trend,
    currentYear,
    trendRange,
    trendMetric,
    requestContext,
    lastInitialLoadedAt,
    lastInitialCacheKey,
    loadingAny,
    setContext,
    loadOverview,
    loadCategory,
    loadRadar,
    loadGraph,
    loadTrend,
    loadInitial,
    setGraphYear,
    setTrend,
    reset,
  }
})
