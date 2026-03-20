<script setup lang="ts">
const props = defineProps<{
  rows: number
  cols: number
  getHeatValue: (row: number, col: number) => number
}>()

const getClassByValue = (value: number) => {
  if (value >= 4) return 'bg-emerald-500'
  if (value === 3) return 'bg-emerald-600'
  if (value === 2) return 'bg-emerald-700'
  if (value === 1) return 'bg-emerald-800/80'
  return 'bg-gray-200 dark:bg-gray-700'
}
</script>

<template>
  <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl overflow-hidden">
    <div class="space-y-4">
      <div class="flex items-center gap-2">
        <UIcon name="i-lucide-chart-no-axes-combined" class="size-5 text-primary-500" />
        <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">活跃热力图</h3>
      </div>
      <div class="overflow-x-auto">
        <div class="w-max space-y-1.5">
          <div v-for="row in props.rows" :key="`row-${row}`" class="flex gap-1.5">
            <div
              v-for="col in props.cols"
              :key="`col-${row}-${col}`"
              class="size-3 rounded-sm"
              :class="getClassByValue(props.getHeatValue(row, col))"
            />
          </div>
        </div>
      </div>
    </div>
  </UCard>
</template>
