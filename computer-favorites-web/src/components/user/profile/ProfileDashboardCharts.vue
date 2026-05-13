<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传内容数据看板 - 分类图表区块
 * 拆分自 ProfileHeroSection.vue：CategoryDonutCard + ContentTypeBarCard
 * 数据来源：useProfileDashboardStore（category / submittedCategory）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import CategoryDonutCard from './dashboard/CategoryDonutCard.vue'
import ContentTypeBarCard from './dashboard/ContentTypeBarCard.vue'
import { useProfileDashboardStore } from '@/stores/profileDashboard'

type DashboardCategoryMode = 'interaction' | 'submitted'

const store = useProfileDashboardStore()
const { category, submittedCategory } = storeToRefs(store)

const toNum = (v: number | string | null | undefined) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const mode = ref<DashboardCategoryMode>('interaction')
const modeOptions = [
  { label: '互动偏好', value: 'interaction' },
  { label: '投稿分类', value: 'submitted' },
]

const currentSection = computed(() =>
  mode.value === 'interaction' ? category.value : submittedCategory.value,
)

/** CategoryDonut 数据：name + value（weight） */
const categoryPvList = computed(() =>
  (currentSection.value.data?.items ?? []).map((item) => ({
    name: item.name,
    value: toNum(item.weight),
  })),
)

/** ContentTypeBar：复用 pct 维度 */
const typeCountMerged = computed(() =>
  (currentSection.value.data?.items ?? []).map((item) => ({
    name: item.name,
    value: toNum(item.pct),
  })),
)

const donutTitle = computed(() =>
  mode.value === 'interaction' ? '互动偏好权重占比' : '投稿分类数量占比',
)
const barTitle = computed(() =>
  mode.value === 'interaction' ? '分类占比（前8项 + 其他）' : '投稿分类（前8项 + 其他）',
)

const handleCategorySelect = (_name: string | null) => {
  // 保留交互占位（M6 再深挖分类过滤）
}

watch(mode, (next) => {
  if (next === 'submitted' && submittedCategory.value.state === 'idle') {
    void store.loadSubmittedCategory()
  }
})

onMounted(() => {
  if (category.value.state === 'idle') store.loadCategory()
})
</script>

<template>
  <section class="space-y-3">
    <header class="flex items-center justify-between gap-3">
      <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">分类分析</h3>
      <USelect v-model="mode" :options="modeOptions" class="w-32" />
    </header>
    <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <CategoryDonutCard
        :title="donutTitle"
        :data="categoryPvList"
        :selected="null"
        @select="handleCategorySelect"
      />
      <ContentTypeBarCard :title="barTitle" :data="typeCountMerged" />
    </div>
  </section>
</template>
