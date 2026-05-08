/**
 * @author yyyouth zg
 * @date 2026-05-07
 * ECharts 实例生命周期管理 composable：自动 init / dark-mode 重渲染 / resize / dispose
 */
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue'
import { useAppStore } from '@/stores/app'

interface UseEchartOptions {
  buildOption: (isDark: boolean) => EChartsOption
  onClick?: (params: unknown) => void
}

/**
 * 创建并管理一个 ECharts 实例
 * @param options buildOption 构造图表配置；onClick 可选点击回调
 * @returns 一个 ref 用于挂载到 DOM
 */
export const useEchart = (options: UseEchartOptions) => {
  const containerRef = ref<HTMLElement | null>(null)
  const appStore = useAppStore()
  let chart: ECharts | null = null
  let observer: ResizeObserver | null = null

  const render = () => {
    if (!chart) {
      return
    }
    chart.setOption(options.buildOption(appStore.isDark), true)
  }

  const handleResize = () => {
    chart?.resize()
  }

  const init = () => {
    if (!containerRef.value || chart) {
      return
    }
    chart = echarts.init(containerRef.value)
    if (options.onClick) {
      chart.on('click', options.onClick)
    }
    render()
    if (typeof ResizeObserver !== 'undefined') {
      observer = new ResizeObserver(() => handleResize())
      observer.observe(containerRef.value)
    }
  }

  watch(
    () => appStore.isDark,
    () => {
      nextTick(render)
    },
  )

  onMounted(() => {
    nextTick(init)
    window.addEventListener('resize', handleResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    observer?.disconnect()
    observer = null
    chart?.dispose()
    chart = null
  })

  return {
    containerRef,
    rerender: () => nextTick(render),
  } as { containerRef: Ref<HTMLElement | null>; rerender: () => Promise<void> }
}
