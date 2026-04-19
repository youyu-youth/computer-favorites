<script setup lang="ts">
import type { AdminAnnouncementViewItem } from '@/types/admin-announcement'
import UButton from '@/components/ui-adapter/UButton.vue'

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
  <aside v-if="open" class="hidden xl:block">
    <section class="sticky top-20 overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card">
      <div class="border-b border-gray-100 px-5 py-4 dark:border-dark-border/70">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Preview</p>
            <h3 class="mt-2 text-base font-semibold text-gray-950 dark:text-white">公告详情预览</h3>
          </div>
          <button
            type="button"
            class="inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-xl border border-gray-200 text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-700 dark:border-dark-border dark:text-gray-300 dark:hover:bg-dark-bg dark:hover:text-white"
            @click="emit('update:open', false)"
          >
            <i class="fas fa-xmark"></i>
          </button>
        </div>
      </div>

      <div v-if="announcement" class="space-y-5 px-5 py-5">
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

        <div class="grid grid-cols-1 gap-2 sm:grid-cols-2">
          <UButton variant="soft" @click="emit('edit', announcement.id)">
            <i class="fas fa-pen-to-square text-xs"></i>
            编辑公告
          </UButton>
          <UButton variant="soft" color="warning" @click="emit('toggle-top', announcement.id)">
            <i class="fas fa-thumbtack text-xs"></i>
            {{ announcement.isTop === 1 ? '取消置顶' : '设为置顶' }}
          </UButton>
          <UButton variant="soft" color="neutral" class="sm:col-span-2" @click="emit('toggle-status', announcement.id)">
            <i :class="announcement.status === 1 ? 'fas fa-eye-slash text-xs' : 'fas fa-eye text-xs'"></i>
            {{ announcement.status === 1 ? '切换为隐藏' : '切换为显示' }}
          </UButton>
        </div>
      </div>

      <div v-else class="px-5 py-12 text-center">
        <i class="fas fa-folder-open text-2xl text-gray-300 dark:text-gray-600"></i>
        <p class="mt-4 text-sm text-gray-500 dark:text-gray-400">暂无可预览的公告，试试切换筛选条件或新建一条公告。</p>
      </div>
    </section>
  </aside>

  <Transition name="announcement-drawer-fade">
    <div v-if="open" class="fixed inset-0 z-[130] xl:hidden">
      <div
        class="absolute inset-0 bg-[rgb(15_23_42/0.42)] backdrop-blur-sm dark:bg-[rgb(2_6_23/0.7)]"
        @click="emit('update:open', false)"
      ></div>

      <div class="absolute inset-x-0 bottom-0 max-h-[82vh] rounded-t-[24px] border border-gray-200 bg-white shadow-[0_-16px_40px_rgb(15_23_42/0.22)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_-20px_44px_rgb(2_6_23/0.58)]">
        <div class="mx-auto mt-3 h-1.5 w-12 rounded-full bg-gray-300 dark:bg-gray-600"></div>

        <div class="flex items-center justify-between px-4 py-4">
          <h3 class="text-base font-semibold text-gray-950 dark:text-white">公告详情</h3>
          <button
            type="button"
            class="inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-xl border border-gray-200 text-gray-500 transition-colors hover:bg-gray-50 dark:border-dark-border dark:text-gray-300 dark:hover:bg-dark-bg"
            @click="emit('update:open', false)"
          >
            <i class="fas fa-xmark"></i>
          </button>
        </div>

        <div class="max-h-[calc(82vh-4.5rem)] overflow-y-auto px-4 pb-5">
          <div v-if="announcement" class="space-y-4">
            <div class="flex flex-wrap items-center gap-2">
              <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveTypeTone(announcement.type)">
                {{ announcement.typeLabel }}
              </span>
              <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveStatusTone(announcement.status)">
                {{ announcement.statusLabel }}
              </span>
            </div>

            <h4 class="text-lg font-semibold leading-7 text-gray-950 dark:text-white">{{ announcement.title }}</h4>
            <p class="text-sm leading-7 text-gray-600 dark:text-gray-300">{{ announcement.content }}</p>

            <div class="grid grid-cols-1 gap-2 text-sm text-gray-500 dark:text-gray-400">
              <p>公告编号：#{{ announcement.id }}</p>
              <p>发布时间：{{ announcement.publishTimeText }}</p>
              <p>最近更新：{{ announcement.updateTime }}</p>
            </div>

            <div class="grid grid-cols-1 gap-2">
              <UButton variant="soft" @click="emit('edit', announcement.id)">编辑公告</UButton>
              <UButton variant="soft" color="warning" @click="emit('toggle-top', announcement.id)">
                {{ announcement.isTop === 1 ? '取消置顶' : '设为置顶' }}
              </UButton>
              <UButton variant="soft" color="neutral" @click="emit('toggle-status', announcement.id)">
                {{ announcement.status === 1 ? '切换为隐藏' : '切换为显示' }}
              </UButton>
            </div>
          </div>

          <div v-else class="py-8 text-center">
            <i class="fas fa-folder-open text-2xl text-gray-300 dark:text-gray-600"></i>
            <p class="mt-4 text-sm text-gray-500 dark:text-gray-400">暂无可预览的公告记录。</p>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.announcement-drawer-fade-enter-active,
.announcement-drawer-fade-leave-active {
  transition: opacity 0.18s ease;
}

.announcement-drawer-fade-enter-from,
.announcement-drawer-fade-leave-to {
  opacity: 0;
}
</style>
