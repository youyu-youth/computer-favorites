<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  searchKeyword: string
  selectedCount: number
  batchDeleteSubmitting: boolean
}>()

const emit = defineEmits<{
  (e: 'update:searchKeyword', value: string): void
  (e: 'refresh'): void
  (e: 'create'): void
  (e: 'batch-delete'): void
}>()

const searchModel = computed({
  get: () => props.searchKeyword,
  set: (value: string) => emit('update:searchKeyword', value),
})
</script>

<template>
  <section class="mb-4 md:mb-5">
    <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div class="w-full md:w-[360px]">
        <label class="sr-only" for="tag-search">搜索标签</label>
        <div class="relative">
          <i
            class="fas fa-search pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 dark:text-gray-500"
          ></i>
          <UInput id="tag-search" v-model="searchModel" placeholder="按标签名称搜索" class="pl-8" />
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <UButton
          class="rounded-none"
          color="red"
          variant="soft"
          :loading="batchDeleteSubmitting"
          :disabled="batchDeleteSubmitting || selectedCount <= 0"
          @click="emit('batch-delete')"
        >
          <i class="fas fa-trash-can text-xs"></i>
          批量删除
          <span v-if="selectedCount > 0">({{ selectedCount }})</span>
        </UButton>
        <UButton class="rounded-none" color="neutral" variant="soft" @click="emit('refresh')">
          <i class="fas fa-rotate-right text-xs"></i>
          刷新
        </UButton>
        <UButton class="rounded-none" color="primary" @click="emit('create')">
          <i class="fas fa-plus text-xs"></i>
          新建标签
        </UButton>
      </div>
    </div>
  </section>
</template>

<style scoped>
:deep([data-slot='base']) {
  border-radius: 0;
}
</style>
