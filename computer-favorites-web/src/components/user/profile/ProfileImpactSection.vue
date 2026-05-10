<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力看板主卡片（user-15 扩展）。
 *
 * 布局：单卡片，桌面端 grid-cols-2 左右两栏 + 中线分割；移动端单列堆叠。
 *  - 左：ImpactGaugePanel（左 legend / 右 270° gauge 横向并排）
 *  - 右：5 行 ImpactBarRow（24px 横向比例柱，柱长 = value / globalMax × 100%）
 *
 * 数据口径：range=all 用 totals；7d/30d/90d 用 rangeCounts（即区间内总和）。
 *
 * 状态：idle / loading（骨架）/ success / error（错误条 + 重试）/ empty（websiteCount=0）
 */
import { computed, onMounted, watch } from 'vue'
import { Activity, AlertCircle, Inbox } from 'lucide-vue-next'
import { useProfileImpactStore } from '@/stores/profileImpact'
import {
  type ImpactRange,
  type MetricGroup,
} from '@/api/user-profile-impact'
import { METRIC_ORDER, type MetricKey } from './impact/palette'
import ImpactGaugePanel from './impact/ImpactGaugePanel.vue'
import ImpactBarRow from './impact/ImpactBarRow.vue'

const props = defineProps<{
  /** 公开页传入 username；登录页（自己看自己）省略 */
  username?: string
}>()

const store = useProfileImpactStore()

/* ---------- range tabs ---------- */
const rangeOptions: { value: ImpactRange; label: string }[] = [
  { value: '7d', label: '7d' },
  { value: '30d', label: '30d' },
  { value: '90d', label: '90d' },
  { value: 'all', label: 'All' },
]

const activeRange = computed(() => store.range)

const handleRangeClick = (r: ImpactRange) => {
  if (r === store.range) {
    return
  }
  store.setRange(r)
}

/* ---------- 派生 ---------- */
const ZERO_GROUP: MetricGroup = {
  browse: 0,
  like: 0,
  collect: 0,
  comment: 0,
  score: 0,
}

const data = computed(() => store.impact.data)
const loadState = computed(() => store.impact.state)
const errorMsg = computed(() => store.impact.error)

const isLoading = computed(
  () => loadState.value === 'loading' && data.value === null,
)
const isError = computed(() => loadState.value === 'error' && data.value === null)
const isReady = computed(() => data.value !== null)
const isEmpty = computed(() => isReady.value && (data.value?.websiteCount ?? 0) <= 0)
const isRangeAll = computed(() => activeRange.value === 'all')

const websiteCount = computed(() => data.value?.websiteCount ?? 0)
const totals = computed(() => data.value?.totals ?? ZERO_GROUP)
const rangeCounts = computed(() => data.value?.rangeCounts ?? ZERO_GROUP)

/** 当前展示口径：range=all → 全时累计；其余 → 区间累计 */
const displayGroup = computed<MetricGroup>(() =>
  isRangeAll.value ? totals.value : rangeCounts.value,
)

const totalSum = computed(() =>
  METRIC_ORDER.reduce((acc, m) => acc + (displayGroup.value[m] || 0), 0),
)

const formattedTotalSum = computed(() =>
  new Intl.NumberFormat('zh-CN').format(totalSum.value),
)

/** 5 指标共享归一化最大值：取 5 个指标累计中的最大者，作为柱长 100% 基准 */
const globalMax = computed(() => {
  let max = 0
  METRIC_ORDER.forEach((m) => {
    const v = displayGroup.value[m] || 0
    if (v > max) {
      max = v
    }
  })
  return max
})

const totalOf = (metric: MetricKey): number => displayGroup.value[metric] || 0

const deltaOf = (metric: MetricKey): number | null => {
  const v = data.value?.delta?.[metric]
  if (v === null || v === undefined) {
    return null
  }
  return Number(v)
}

/* ---------- 数据加载 ---------- */
const buildContext = () =>
  props.username ? { username: props.username } : undefined

const reload = () => store.load(undefined, buildContext())

onMounted(() => {
  store.load(undefined, buildContext())
})

watch(
  () => props.username,
  () => {
    store.load(undefined, buildContext())
  },
)
</script>

<template>
  <section class="space-y-3" aria-labelledby="profile-impact-heading">
    <!-- 标题行（与「上传内容数据看板」样式统一） -->
    <div>
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div class="flex items-center gap-2">
          <Activity class="size-4 text-amber-500" :stroke-width="2" />
          <h3
            id="profile-impact-heading"
            class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100"
          >
            我的网站影响力
          </h3>
          <span
            class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400"
          >
            /IMPACT
          </span>
        </div>
        <div
          class="flex items-center gap-1 rounded-md border border-black/5 bg-white p-0.5 dark:border-white/5 dark:bg-black"
          role="tablist"
          aria-label="时间范围"
        >
          <button
            v-for="opt in rangeOptions"
            :key="opt.value"
            type="button"
            role="tab"
            :aria-selected="activeRange === opt.value"
            class="cursor-pointer rounded-sm px-2.5 py-1 font-mono text-[11px] transition-colors"
            :class="
              activeRange === opt.value
                ? 'bg-amber-500/15 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400'
                : 'text-gray-600 hover:bg-black/5 dark:text-gray-400 dark:hover:bg-white/5'
            "
            @click="handleRangeClick(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </header>

      <!-- 副标题 -->
      <p class="mt-1 text-[12px] text-gray-500 dark:text-gray-400">
        <template v-if="isReady">
          基于 <span class="font-mono tabular-nums text-gray-700 dark:text-gray-300">{{ websiteCount }}</span> 个上传网站 · 累计他人互动
          <span class="font-mono tabular-nums text-gray-700 dark:text-gray-300">{{ formattedTotalSum }}</span> 次
        </template>
        <template v-else-if="isLoading">数据加载中…</template>
        <template v-else-if="isError">数据加载失败</template>
        <template v-else>—</template>
      </p>
    </div>

    <!-- 加载态 骨架 -->
    <div v-if="isLoading" class="grid gap-4 md:grid-cols-2">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:gap-4">
        <div class="order-2 flex w-full flex-col gap-y-1.5 md:order-1 md:flex-1">
          <div
            v-for="i in 5"
            :key="i"
            class="h-5 w-full animate-pulse rounded bg-zinc-100 dark:bg-zinc-900/60"
          />
        </div>
        <div
          class="order-1 mx-auto aspect-[16/13] w-full max-w-[180px] animate-pulse rounded-full bg-zinc-100 dark:bg-zinc-900/60 md:order-2 md:w-[160px] md:flex-shrink-0"
        />
      </div>
      <div class="flex flex-col gap-y-1.5 md:border-l md:border-zinc-200/60 md:pl-4 md:dark:border-zinc-800/60">
        <div
          v-for="i in 5"
          :key="i"
          class="h-6 w-full animate-pulse rounded bg-zinc-100 dark:bg-zinc-900/60"
        />
      </div>
    </div>

    <!-- 错误态 -->
    <div
      v-else-if="isError"
      class="flex items-center justify-between gap-3 rounded border border-red-200/70 bg-red-50/80 px-3 py-2 text-[12px] text-red-700 dark:border-red-900/50 dark:bg-red-950/30 dark:text-red-300"
      role="alert"
    >
      <div class="flex items-center gap-1.5">
        <AlertCircle class="size-3.5" :stroke-width="2" />
        <span class="truncate">{{ errorMsg || '加载失败' }}</span>
      </div>
      <button
        type="button"
        class="cursor-pointer rounded bg-red-600/90 px-2 py-0.5 text-[11px] text-white hover:bg-red-600 dark:bg-red-500/80 dark:hover:bg-red-500"
        @click="reload"
      >
        重试
      </button>
    </div>

    <!-- 空态：websiteCount=0 -->
    <div
      v-else-if="isEmpty"
      class="flex flex-col items-center justify-center gap-y-2 rounded border border-dashed border-zinc-200/70 bg-zinc-50/60 py-8 text-center dark:border-zinc-800/70 dark:bg-zinc-900/30"
    >
      <Inbox class="size-6 text-zinc-400 dark:text-zinc-500" :stroke-width="1.5" />
      <p class="text-[12px] text-zinc-500 dark:text-zinc-400">
        暂无上传网站，影响力数据将在网站审核通过后开始累计
      </p>
    </div>

    <!-- 主体（success） -->
    <div
      v-else-if="isReady"
      class="grid gap-4 md:grid-cols-2"
      :key="`impact-${activeRange}`"
    >
      <!-- 左栏：legend + 270° gauge -->
      <ImpactGaugePanel
        :totals="totals"
        :range-counts="rangeCounts"
        :range="activeRange"
      />
      <!-- 右栏：5 根总额比例柱 -->
      <div
        class="flex flex-col gap-y-1.5 md:border-l md:border-zinc-200/60 md:pl-4 md:dark:border-zinc-800/60"
      >
        <ImpactBarRow
          v-for="metric in METRIC_ORDER"
          :key="metric"
          :metric="metric"
          :total="totalOf(metric)"
          :max="globalMax"
          :delta="deltaOf(metric)"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
/* range 切换微交互：left/right 内容同时 fade-in */
section :deep(.grid) > * {
  animation: impact-fade 200ms ease-out both;
}

@keyframes impact-fade {
  from {
    opacity: 0.4;
  }
  to {
    opacity: 1;
  }
}
</style>
