<script setup lang="ts">
import type { AdminAnnouncementViewItem } from '@/types/admin-announcement'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'

defineProps<{
  open: boolean
  announcement: AdminAnnouncementViewItem | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'edit', id: number): void
  (e: 'toggle-status', id: number): void
  (e: 'toggle-top', id: number): void
}>()

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
  <UModal
    :open="open"
    title="公告详情预览"
    description="支持直接在详情中执行编辑、置顶和显示状态切换。"
    :ui="{
      overlay: 'bg-[rgb(15_23_42/0.42)] backdrop-blur-sm dark:bg-[rgb(2_6_23/0.72)] z-[130]',
      content:
        'w-[min(96vw,960px)] rounded-[24px] border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card overflow-hidden shadow-[0_28px_60px_rgb(15_23_42/0.24)] dark:shadow-[0_32px_64px_rgb(2_6_23/0.62)]',
      header: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
      title: 'text-base font-semibold text-gray-950 dark:text-white',
      description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
      body: 'px-5 py-5',
      footer: 'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg',
    }"
    @update:open="(value) => emit('update:open', value)"
  >
    <template #body>
      <div v-if="announcement" class="space-y-5">
        <div>
          <div class="flex flex-wrap items-center gap-2">
            <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveTypeTone(announcement.type)">
              {{ announcement.typeLabel }}
            </span>
            <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveStatusTone(announcement.status)">
              {{ announcement.statusLabel }}
            </span>
            <span
              class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
              :class="
                announcement.isTop === 1
                  ? 'border-amber-100 bg-amber-50 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200'
                  : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
              "
            >
              {{ announcement.topLabel }}
            </span>
          </div>

          <h4 class="mt-4 text-xl font-semibold leading-8 text-gray-950 dark:text-white">{{ announcement.title }}</h4>
          <p class="mt-3 text-sm leading-7 text-gray-600 dark:text-gray-300">{{ announcement.content }}</p>
        </div>

        <dl class="grid grid-cols-1 gap-3 rounded-2xl border border-gray-100 bg-gray-50/80 p-4 dark:border-dark-border/70 dark:bg-dark-bg/70">
          <div class="flex items-start justify-between gap-3">
            <dt class="text-sm text-gray-500 dark:text-gray-400">公告编号</dt>
            <dd class="text-sm font-medium text-gray-900 dark:text-white">#{{ announcement.id }}</dd>
          </div>
          <div class="flex items-start justify-between gap-3">
            <dt class="text-sm text-gray-500 dark:text-gray-400">发布时间</dt>
            <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ announcement.publishTimeText }}</dd>
          </div>
          <div class="flex items-start justify-between gap-3">
            <dt class="text-sm text-gray-500 dark:text-gray-400">最近更新</dt>
            <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ announcement.updateTime }}</dd>
          </div>
          <div class="flex items-start justify-between gap-3">
            <dt class="text-sm text-gray-500 dark:text-gray-400">创建时间</dt>
            <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ announcement.createTime }}</dd>
          </div>
        </dl>
      </div>

      <div v-else class="py-8 text-center">
        <i class="fas fa-folder-open text-2xl text-gray-300 dark:text-gray-600"></i>
        <p class="mt-4 text-sm text-gray-500 dark:text-gray-400">暂无可预览的公告，试试切换筛选条件或新建一条公告。</p>
      </div>
    </template>

    <template #footer>
      <div class="grid grid-cols-1 gap-2 sm:grid-cols-4">
        <UButton
          v-if="announcement"
          variant="soft"
          class="sm:col-span-1"
          @click="emit('edit', announcement.id)"
        >
          <i class="fas fa-pen-to-square text-xs"></i>
          编辑公告
        </UButton>
        <UButton
          v-if="announcement"
          variant="soft"
          color="warning"
          class="sm:col-span-1"
          @click="emit('toggle-top', announcement.id)"
        >
          <i class="fas fa-thumbtack text-xs"></i>
          {{ announcement.isTop === 1 ? '取消置顶' : '设为置顶' }}
        </UButton>
        <UButton
          v-if="announcement"
          variant="soft"
          color="neutral"
          class="sm:col-span-1"
          @click="emit('toggle-status', announcement.id)"
        >
          <i :class="announcement.status === 1 ? 'fas fa-eye-slash text-xs' : 'fas fa-eye text-xs'"></i>
          {{ announcement.status === 1 ? '切换为隐藏' : '切换为显示' }}
        </UButton>
        <UButton
          color="neutral"
          variant="soft"
          class="sm:col-span-1"
          @click="emit('update:open', false)"
        >
          关闭
        </UButton>
      </div>
    </template>
  </UModal>
</template>
