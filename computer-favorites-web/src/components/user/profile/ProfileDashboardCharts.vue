<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传内容数据看板 - 分类图表区块
 * 拆分自 ProfileHeroSection.vue：CategoryDonutCard + ContentTypeBarCard
 * 数据来源：useProfileDashboardStore（category）
 */
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import CategoryDonutCard from './dashboard/CategoryDonutCard.vue'
import ContentTypeBarCard from './dashboard/ContentTypeBarCard.vue'
import { useProfileDashboardStore } from '@/stores/profileDashboard'

const store = useProfileDashboardStore()
const { category } = storeToRefs(store)

const toNum = (v: number | string | null | undefined) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

/** CategoryDonut 数据：name + value（weight） */
const categoryPvList = computed(() =>
  (category.value.data?.items ?? []).map((item) => ({
    name: item.name,
    value: toNum(item.weight),
  })),
)

/** ContentTypeBar：复用 category pct 维度（M3 阶段后端暂无独立内容类型接口） */
const typeCountMerged = computed(() =>
  (category.value.data?.items ?? []).map((item) => ({
    name: item.name,
    value: toNum(item.pct),
  })),
)

const handleCategorySelect = (_name: string | null) => {
  // 保留交互占位（M6 再深挖分类过滤）
}

onMounted(() => {
  if (category.value.state === 'idle') store.loadCategory()
})
</script>

<template>
  <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
    <CategoryDonutCard
      title="各分类偏好权重占比"
      :data="categoryPvList"
      :selected="null"
      @select="handleCategorySelect"
    />
    <ContentTypeBarCard
      title="分类占比（前8项 + 其他）"
      :data="typeCountMerged"
    />
  </div>
</template>
