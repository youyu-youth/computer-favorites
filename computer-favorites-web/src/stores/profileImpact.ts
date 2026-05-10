/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力看板 Pinia Store（user-15 扩展）
 *
 * 与 {@link ./profileDashboard} 拆分的理由：
 *  1. range 集合不同（多了 all）；
 *  2. 缓存策略独立（后端 TTL 10min，前端不需 5min 二级缓存）；
 *  3. 公开页与登录页共用同一 store + 不同 context.username。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getUploadImpact,
  type ImpactRange,
  type ImpactRequestContext,
  type UploadImpact,
} from '@/api/user-profile-impact'

type LoadState = 'idle' | 'loading' | 'success' | 'error'

interface SectionState<T> {
  data: T | null
  state: LoadState
  error: string
}

const initial = <T>(): SectionState<T> => ({ data: null, state: 'idle', error: '' })

export const useProfileImpactStore = defineStore('profileImpact', () => {
  const impact = ref<SectionState<UploadImpact>>(initial())
  const range = ref<ImpactRange>('30d')
  const context = ref<ImpactRequestContext>({})

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

  const setContext = (ctx?: ImpactRequestContext) => {
    context.value = ctx?.username ? { username: ctx.username } : {}
  }

  /**
   * 加载影响力数据；可同时切换 range 与 context。
   */
  const load = async (r?: ImpactRange, ctx?: ImpactRequestContext): Promise<void> => {
    if (r && r !== range.value) {
      range.value = r
    }
    if (ctx !== undefined) {
      setContext(ctx)
    }
    await handle(impact, () => getUploadImpact(range.value, context.value))
  }

  /** 仅切换 range */
  const setRange = (r: ImpactRange) => load(r)

  const reset = () => {
    impact.value = initial()
    range.value = '30d'
    context.value = {}
  }

  return {
    impact,
    range,
    context,
    setContext,
    load,
    setRange,
    reset,
  }
})
