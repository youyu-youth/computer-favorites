<script setup lang="ts">
import { computed } from 'vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import type { AdminAnnouncementViewItem } from '@/types/admin-announcement'

const props = defineProps<{
  rows: AdminAnnouncementViewItem[]
  selectedIds: number[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'selection-change', value: number[]): void
  (e: 'view', id: number): void
  (e: 'edit', id: number): void
  (e: 'toggle-status', id: number): void
  (e: 'toggle-top', id: number): void
}>()

const selectedSet = computed(() => new Set(props.selectedIds))
const allChecked = computed(() => props.rows.length > 0 && props.rows.every((item) => selectedSet.value.has(item.id)))

const toggleAll = (checked: boolean) => {
  if (!checked) {
    emit('selection-change', [])
    return
  }
  emit(
    'selection-change',
    props.rows.map((item) => item.id),
  )
}

const toggleOne = (announcementId: number, checked: boolean) => {
  const nextIds = checked
    ? [...props.selectedIds, announcementId]
    : props.selectedIds.filter((id) => id !== announcementId)
  emit('selection-change', [...new Set(nextIds)])
}

const handleToggleAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  toggleAll(target.checked)
}

const handleToggleOne = (announcementId: number, event: Event) => {
  const target = event.target as HTMLInputElement
  toggleOne(announcementId, target.checked)
}

const resolveTypeTone = (type: number) => {
  if (type === 1) {
    return 'border-blue-100 bg-blue-50 text-blue-700 dark:border-blue-900/40 dark:bg-blue-900/20 dark:text-blue-200'
  }
  if (type === 2) {
    return 'border-rose-100 bg-rose-50 text-rose-700 dark:border-rose-900/40 dark:bg-rose-900/20 dark:text-rose-200'
  }
  return 'border-cyan-100 bg-cyan-50 text-cyan-700 dark:border-cyan-900/40 dark:bg-cyan-900/20 dark:text-cyan-200'
}

const resolveStatusTone = (status: number) => {
  return status === 1
    ? 'border-emerald-100 bg-emerald-50 text-emerald-700 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-200'
    : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
}
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card">
    <div class="border-b border-gray-100 px-4 py-3 dark:border-dark-border/70">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h3 class="text-base font-semibold text-gray-950 dark:text-white">公告列表</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">桌面端优先使用高密度表格，移动端自动切换为卡片视图。</p>
        </div>
      </div>
    </div>

    <div v-if="loading" class="px-6 py-12 text-center">
      <i class="fas fa-spinner fa-spin text-lg text-gray-400 dark:text-gray-500"></i>
      <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">正在整理公告数据...</p>
    </div>

    <div v-else-if="rows.length === 0" class="px-6 py-12 text-center">
      <i class="fas fa-bullhorn text-2xl text-gray-300 dark:text-gray-600"></i>
      <p class="mt-4 text-sm text-gray-500 dark:text-gray-400">当前筛选条件下暂无公告记录，可尝试调整筛选或新建公告。</p>
    </div>

    <template v-else>
      <div class="hidden overflow-x-auto md:block">
        <table class="min-w-[980px] table-fixed">
          <thead>
            <tr class="border-b border-gray-100 text-left text-xs uppercase tracking-[0.18em] text-gray-400 dark:border-dark-border/70 dark:text-gray-500">
              <th class="w-14 px-4 py-2">
                <label class="inline-flex cursor-pointer items-center">
                  <input
                    type="checkbox"
                    class="peer sr-only"
                    :checked="allChecked"
                    @change="handleToggleAll"
                  />
                  <span
                    class="inline-flex h-4 w-4 items-center justify-center rounded border border-slate-300 bg-white text-white transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-blue-500 peer-focus-visible:ring-offset-2 peer-checked:border-blue-600 peer-checked:bg-blue-600 dark:border-slate-500 dark:bg-slate-900 dark:peer-checked:border-blue-500 dark:peer-checked:bg-blue-500 dark:peer-focus-visible:ring-offset-slate-900"
                  >
                    <i class="fas fa-check text-[9px] opacity-0 transition-opacity peer-checked:opacity-100"></i>
                  </span>
                </label>
              </th>
              <th class="px-4 py-2">公告标题</th>
              <th class="w-32 px-4 py-2">类型</th>
              <th class="w-28 px-4 py-2">状态</th>
              <th class="w-28 px-4 py-2">置顶</th>
              <th class="w-40 px-4 py-2">发布时间</th>
              <th class="w-40 px-4 py-2">更新时间</th>
              <th class="w-48 px-4 py-2">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in rows"
              :key="row.id"
              class="border-b border-gray-100 align-top transition-colors hover:bg-gray-50/80 dark:border-dark-border/60 dark:hover:bg-dark-bg/60"
            >
              <td class="px-4 py-2">
                <label class="inline-flex cursor-pointer items-center">
                  <input
                    type="checkbox"
                    class="peer sr-only"
                    :checked="selectedSet.has(row.id)"
                    @change="handleToggleOne(row.id, $event)"
                  />
                  <span
                    class="inline-flex h-4 w-4 items-center justify-center rounded border border-slate-300 bg-white text-white transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-blue-500 peer-focus-visible:ring-offset-2 peer-checked:border-blue-600 peer-checked:bg-blue-600 dark:border-slate-500 dark:bg-slate-900 dark:peer-checked:border-blue-500 dark:peer-checked:bg-blue-500 dark:peer-focus-visible:ring-offset-slate-900"
                  >
                    <i class="fas fa-check text-[9px] opacity-0 transition-opacity peer-checked:opacity-100"></i>
                  </span>
                </label>
              </td>
              <td class="px-4 py-2">
                <button
                  type="button"
                  class="cursor-pointer text-left"
                  @click="emit('view', row.id)"
                >
                  <p class="line-clamp-2 text-sm font-semibold text-gray-950 transition-colors hover:text-blue-600 dark:text-white dark:hover:text-blue-300">
                    {{ row.title }}
                  </p>
                  <p class="mt-1 line-clamp-2 text-sm leading-5 text-gray-500 dark:text-gray-400">
                    {{ row.contentPreview }}
                  </p>
                </button>
              </td>
              <td class="px-4 py-2">
                <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveTypeTone(row.type)">
                  {{ row.typeLabel }}
                </span>
              </td>
              <td class="px-4 py-2">
                <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveStatusTone(row.status)">
                  {{ row.statusLabel }}
                </span>
              </td>
              <td class="px-4 py-2">
                <span
                  class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                  :class="
                    row.isTop === 1
                      ? 'border-amber-100 bg-amber-50 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200'
                      : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
                  "
                >
                  {{ row.topLabel }}
                </span>
              </td>
              <td class="px-4 py-2 text-sm text-gray-600 dark:text-gray-300">{{ row.publishTimeText }}</td>
              <td class="px-4 py-2 text-sm text-gray-500 dark:text-gray-400">{{ row.updateTime }}</td>
              <td class="px-4 py-2">
                <div class="flex flex-wrap items-center gap-1.5">
                  <UButton size="sm" variant="soft" color="neutral" @click="emit('view', row.id)">
                    查看
                  </UButton>
                  <UButton size="sm" variant="soft" @click="emit('edit', row.id)">
                    编辑
                  </UButton>
                  <UButton size="sm" variant="soft" color="neutral" @click="emit('toggle-status', row.id)">
                    {{ row.status === 1 ? '隐藏' : '显示' }}
                  </UButton>
                  <UButton size="sm" variant="soft" color="warning" @click="emit('toggle-top', row.id)">
                    {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
                  </UButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="divide-y divide-gray-100 md:hidden dark:divide-dark-border/70">
        <article v-for="row in rows" :key="row.id" class="px-4 py-4">
          <div class="flex items-start gap-3">
            <label class="mt-0.5 inline-flex cursor-pointer items-center">
              <input
                type="checkbox"
                class="peer sr-only"
                :checked="selectedSet.has(row.id)"
                @change="handleToggleOne(row.id, $event)"
              />
              <span
                class="inline-flex h-4 w-4 items-center justify-center rounded border border-slate-300 bg-white text-white transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-blue-500 peer-focus-visible:ring-offset-2 peer-checked:border-blue-600 peer-checked:bg-blue-600 dark:border-slate-500 dark:bg-slate-900 dark:peer-checked:border-blue-500 dark:peer-checked:bg-blue-500 dark:peer-focus-visible:ring-offset-slate-900"
              >
                <i class="fas fa-check text-[9px] opacity-0 transition-opacity peer-checked:opacity-100"></i>
              </span>
            </label>

            <div class="min-w-0 flex-1">
              <button type="button" class="w-full cursor-pointer text-left" @click="emit('view', row.id)">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold" :class="resolveTypeTone(row.type)">
                    {{ row.typeLabel }}
                  </span>
                  <span class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold" :class="resolveStatusTone(row.status)">
                    {{ row.statusLabel }}
                  </span>
                  <span
                    class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold"
                    :class="
                      row.isTop === 1
                        ? 'border-amber-100 bg-amber-50 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200'
                        : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
                    "
                  >
                    {{ row.topLabel }}
                  </span>
                </div>
                <h4 class="mt-3 text-sm font-semibold leading-6 text-gray-950 dark:text-white">{{ row.title }}</h4>
                <p class="mt-2 line-clamp-3 text-sm leading-6 text-gray-500 dark:text-gray-400">{{ row.contentPreview }}</p>
              </button>

              <div class="mt-3 grid grid-cols-1 gap-2 text-xs text-gray-500 dark:text-gray-400">
                <p>发布时间：{{ row.publishTimeText }}</p>
                <p>最近更新：{{ row.updateTime }}</p>
              </div>

              <div class="mt-4 flex flex-wrap gap-2">
                <UButton size="sm" variant="soft" color="neutral" @click="emit('view', row.id)">查看</UButton>
                <UButton size="sm" variant="soft" @click="emit('edit', row.id)">编辑</UButton>
                <UButton size="sm" variant="soft" color="neutral" @click="emit('toggle-status', row.id)">
                  {{ row.status === 1 ? '隐藏' : '显示' }}
                </UButton>
              </div>
            </div>
          </div>
        </article>
      </div>
    </template>
  </section>
</template>
