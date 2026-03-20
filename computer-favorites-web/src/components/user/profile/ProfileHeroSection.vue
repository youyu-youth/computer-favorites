<script setup lang="ts">
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import type { ProfileData } from '@/types/profile'

defineProps<{
  profile: ProfileData
}>()

type RangeKey = '7d' | '30d' | '90d' | 'all'
type DashboardItem = {
  date: string
  category: string
  contentType: string
  pv: number
  likes: number
  favorites: number
}
type PieTooltipPoint = { name: string; value: number; percent: number }
type AxisTooltipPoint = { name: string; value: number }

const appStore = useAppStore()
const numberFormatter = new Intl.NumberFormat('zh-CN')
const rangeOptions: Array<{ label: string; value: RangeKey }> = [
  { label: '近7天', value: '7d' },
  { label: '近30天', value: '30d' },
  { label: '近90天', value: '90d' },
  { label: '全部', value: 'all' },
]
const selectedRange = ref<RangeKey>('30d')
const selectedCategory = ref<string | null>(null)

const categoryPool = ['前端框架', '后端服务', '数据库', 'AI工具', '开发效率', '设计资源']
const typePool = ['教程文章', '工具站点', '开源项目', '官方文档', '社区论坛', '模板素材', '视频课程', '实战案例']
const today = new Date()

const mockItems: DashboardItem[] = Array.from({ length: 180 }, (_, index) => {
  const date = new Date(today)
  date.setDate(today.getDate() - index)
  return {
    date: date.toISOString().slice(0, 10),
    category: categoryPool[index % categoryPool.length] || '未分类',
    contentType: typePool[(index * 3) % typePool.length] || '其他',
    pv: 1100 + ((index * 97) % 4800),
    likes: 35 + ((index * 29) % 360),
    favorites: 20 + ((index * 31) % 240),
  }
})

const daysMap: Record<Exclude<RangeKey, 'all'>, number> = {
  '7d': 7,
  '30d': 30,
  '90d': 90,
}

const filteredItems = computed(() => {
  if (selectedRange.value === 'all') {
    return mockItems
  }
  const days = daysMap[selectedRange.value]
  const cutoff = new Date(today)
  cutoff.setDate(today.getDate() - (days - 1))
  const cutoffDay = cutoff.toISOString().slice(0, 10)
  return mockItems.filter((item) => item.date >= cutoffDay)
})

const overview = computed(() => {
  const totalPv = filteredItems.value.reduce((sum, item) => sum + item.pv, 0)
  const totalLikes = filteredItems.value.reduce((sum, item) => sum + item.likes, 0)
  const totalFavorites = filteredItems.value.reduce((sum, item) => sum + item.favorites, 0)
  return [
    { label: '上传网站总流量（PV）', value: totalPv },
    { label: '总点赞量', value: totalLikes },
    { label: '总收藏量', value: totalFavorites },
  ]
})

const categoryPvList = computed(() => {
  const map = new Map<string, number>()
  filteredItems.value.forEach((item) => {
    map.set(item.category, (map.get(item.category) || 0) + item.pv)
  })
  return [...map.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
})

const typeCountMerged = computed(() => {
  const map = new Map<string, number>()
  filteredItems.value.forEach((item) => {
    map.set(item.contentType, (map.get(item.contentType) || 0) + 1)
  })
  const sorted = [...map.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
  const topN = 6
  if (sorted.length <= topN) {
    return sorted
  }
  const top = sorted.slice(0, topN)
  const other = sorted.slice(topN).reduce((sum, item) => sum + item.value, 0)
  return [...top, { name: '其他', value: other }]
})

const pieRef = ref<HTMLElement | null>(null)
const barRef = ref<HTMLElement | null>(null)
let pieChart: ECharts | null = null
let barChart: ECharts | null = null

const buildPieOption = (): EChartsOption => {
  const isDark = appStore.isDark
  const textColor = isDark ? '#e5e7eb' : '#334155'
  const subTextColor = isDark ? '#94a3b8' : '#64748b'
  if (!categoryPvList.value.length) {
    return {
      title: {
        text: '暂无可视化数据',
        left: 'center',
        top: 'center',
        textStyle: { color: subTextColor, fontSize: 14, fontWeight: 400 },
      },
    }
  }
  return {
    color: ['#22c55e', '#3b82f6', '#f59e0b', '#06b6d4', '#8b5cf6', '#ef4444'],
    tooltip: {
      trigger: 'item',
      formatter: (params: unknown) => {
        const point = params as PieTooltipPoint
        return `${point.name}<br/>访问量：${numberFormatter.format(point.value)}<br/>占比：${point.percent}%`
      },
    },
    legend: {
      type: 'scroll',
      bottom: 0,
      textStyle: { color: textColor, fontSize: 12 },
    },
    series: [
      {
        name: '分类访问占比',
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        label: {
          color: textColor,
          formatter: '{b}\n{d}%',
        },
        labelLine: { lineStyle: { color: subTextColor } },
        data: categoryPvList.value,
      },
    ],
  }
}

const buildBarOption = (): EChartsOption => {
  const isDark = appStore.isDark
  const textColor = isDark ? '#e5e7eb' : '#334155'
  const subTextColor = isDark ? '#94a3b8' : '#64748b'
  const total = typeCountMerged.value.reduce((sum, item) => sum + item.value, 0)
  if (!typeCountMerged.value.length) {
    return {
      title: {
        text: '暂无可视化数据',
        left: 'center',
        top: 'center',
        textStyle: { color: subTextColor, fontSize: 14, fontWeight: 400 },
      },
    }
  }
  return {
    color: ['#3b82f6'],
    grid: { top: 30, left: 40, right: 20, bottom: 35, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: unknown) => {
        const rows = (params as AxisTooltipPoint[]) || []
        const row = rows[0]
        if (!row) return ''
        const percent = total > 0 ? ((row.value / total) * 100).toFixed(1) : '0.0'
        return `${row.name}<br/>内容数量：${numberFormatter.format(row.value)}<br/>占比：${percent}%`
      },
    },
    xAxis: {
      type: 'category',
      data: typeCountMerged.value.map((item) => item.name),
      axisLabel: { color: textColor, interval: 0, rotate: 20 },
      axisLine: { lineStyle: { color: subTextColor } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: textColor },
      splitLine: { lineStyle: { color: isDark ? '#374151' : '#e2e8f0' } },
    },
    series: [
      {
        name: '内容数量',
        type: 'bar',
        barWidth: '46%',
        data: typeCountMerged.value.map((item) => item.value),
        emphasis: { focus: 'series' },
      },
    ],
  }
}

const renderCharts = () => {
  if (pieChart) pieChart.setOption(buildPieOption(), true)
  if (barChart) barChart.setOption(buildBarOption(), true)
}

const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
}

const initCharts = () => {
  if (pieRef.value && !pieChart) {
    pieChart = echarts.init(pieRef.value)
    pieChart.on('click', (params: unknown) => {
      const payload = params as { name?: string }
      if (typeof payload.name !== 'string') {
        return
      }
      selectedCategory.value = selectedCategory.value === payload.name ? null : payload.name
    })
  }
  if (barRef.value && !barChart) {
    barChart = echarts.init(barRef.value)
  }
  renderCharts()
}

watch(categoryPvList, (value) => {
  if (!value.some((item) => item.name === selectedCategory.value)) {
    selectedCategory.value = null
  }
})

watch([() => appStore.isDark, filteredItems, categoryPvList, typeCountMerged], () => {
  nextTick(() => {
    renderCharts()
    handleResize()
  })
})

onMounted(() => {
  nextTick(() => {
    initCharts()
    handleResize()
  })
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<template>
  <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl">
    <div class="space-y-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h3 class="text-base sm:text-lg font-semibold text-gray-900 dark:text-gray-100">上传内容数据看板</h3>
        <div class="flex items-center gap-2">
          <UButton
            v-for="item in rangeOptions"
            :key="item.value"
            color="neutral"
            variant="soft"
            size="xs"
            :class="selectedRange === item.value
              ? '!bg-[#bc7b0e] !text-white hover:!bg-[#a96f0c] dark:!bg-[#bc7b0e] dark:hover:!bg-[#a96f0c]'
              : '!bg-gray-100 !text-gray-700 hover:!bg-gray-200 dark:!bg-white/10 dark:!text-gray-200 dark:hover:!bg-white/20'"
            @click="selectedRange = item.value"
          >
            {{ item.label }}
          </UButton>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <UCard
          v-for="item in overview"
          :key="item.label"
          class="!ring-0 bg-gray-50 dark:!bg-white/5 rounded-lg"
        >
          <div class="space-y-1">
            <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
            <p class="text-xl font-semibold text-gray-900 dark:text-gray-100">{{ numberFormatter.format(item.value) }}</p>
          </div>
        </UCard>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <UCard class="!ring-0 bg-gray-50 dark:!bg-white/5 rounded-lg">
          <div class="space-y-3">
            <div class="flex items-center justify-between gap-2">
              <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">各分类访问量占比</h4>
              <UButton
                v-if="selectedCategory"
                color="neutral"
                variant="soft"
                size="xs"
                @click="selectedCategory = null"
              >
                恢复默认
              </UButton>
            </div>
            <div ref="pieRef" class="h-60 w-full" />
          </div>
        </UCard>

        <UCard class="!ring-0 bg-gray-50 dark:!bg-white/5 rounded-lg">
          <div class="space-y-3">
            <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">内容类型占比（前6 + 其他）</h4>
            <div ref="barRef" class="h-60 w-full min-w-[320px]" />
          </div>
        </UCard>
      </div>

    </div>
  </UCard>
</template>
