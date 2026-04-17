<script setup lang="ts">
import { computed } from 'vue'
import { Check, Heart, Info, MessageSquareText, ShieldAlert, ShieldCheck } from 'lucide-vue-next'

export interface MessageProps {
  id: number
  type: 'system' | 'comment' | 'favorite' | 'audit' | 'report' | string
  title: string
  content: string
  time: string
  isRead: boolean
}

const props = defineProps<{
  message: MessageProps
  checked: boolean
  selectable: boolean
  selecting: boolean
  markReadLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-select'): void
  (e: 'mark-read'): void
}>()

const iconBgClass = computed(() => {
  const classes: Record<string, string> = {
    system: 'bg-primary-100 text-primary-600 dark:bg-primary-900/40 dark:text-primary-400',
    comment: 'bg-green-100 text-green-600 dark:bg-green-900/40 dark:text-green-400',
    favorite: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/40 dark:text-yellow-400',
    audit: 'bg-cyan-100 text-cyan-700 dark:bg-cyan-900/40 dark:text-cyan-400',
    report: 'bg-rose-100 text-rose-600 dark:bg-rose-900/40 dark:text-rose-400',
    default: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400',
  }
  return classes[props.message.type] || classes.default
})

const IconComponent = computed(() => {
  const icons: Record<string, any> = {
    system: Info,
    comment: MessageSquareText,
    favorite: Heart,
    audit: ShieldCheck,
    report: ShieldAlert,
  }
  return icons[props.message.type] || Info
})
</script>

<template>
  <li
    :class="[
      'group relative p-4 transition-all duration-300 ease-out hover:bg-gray-50 dark:hover:bg-white/[0.02] sm:p-5',
      !message.isRead ? 'bg-primary-50/40 dark:bg-primary-900/10 border-l-4 border-l-primary-500' : 'border-l-4 border-l-transparent dark:bg-black',
    ]"
  >
    <div class="absolute left-4 top-4 sm:left-5 sm:top-5">
      <button
        type="button"
        :disabled="!selectable || selecting || message.isRead"
        :class="[
          'flex h-5 w-5 items-center justify-center rounded border transition-all duration-200',
          checked
            ? 'border-primary-500 bg-primary-500 text-white dark:border-primary-400 dark:bg-primary-400'
            : 'border-gray-300 bg-white text-transparent dark:border-dark-border dark:bg-dark-bg',
          !selectable || selecting || message.isRead
            ? 'cursor-not-allowed opacity-50'
            : 'cursor-pointer hover:border-primary-500 dark:hover:border-primary-400',
        ]"
        :aria-label="checked ? '取消勾选消息' : '勾选消息'"
        @click.stop="emit('toggle-select')"
      >
        <Check class="h-3.5 w-3.5" />
      </button>
    </div>

    <!-- 右上角未读红点 -->
    <div
      v-if="!message.isRead"
      class="absolute right-4 top-4 h-2.5 w-2.5 rounded-full bg-red-500 shadow-[0_0_4px_rgba(239,68,68,0.6)] sm:right-5 sm:top-5"
    ></div>

    <div class="flex gap-4 pl-8 sm:pl-9">
      <!-- 图标区 -->
      <div class="mt-1 shrink-0">
        <div :class="['flex h-10 w-10 items-center justify-center rounded-full', iconBgClass]">
          <component :is="IconComponent" class="h-5 w-5" />
        </div>
      </div>

      <!-- 内容区 -->
      <div class="min-w-0 flex-1 pr-6 flex justify-between flex-col">
        <div>
          <div class="mb-1 flex items-start justify-between">
            <h3
              :class="[
                'truncate pr-4 text-base font-bold tracking-tight',
                !message.isRead ? 'text-gray-900 dark:text-white' : 'text-gray-700 dark:text-gray-400',
              ]"
            >
              {{ message.title }}
            </h3>
            <span class="shrink-0 whitespace-nowrap text-xs font-mono font-medium text-gray-400 dark:text-gray-500 opacity-80 group-hover:opacity-100 transition-opacity">
              {{ message.time }}
            </span>
          </div>
          <p class="line-clamp-2 leading-relaxed text-sm text-gray-500 dark:text-gray-400 opacity-90 group-hover:opacity-100 transition-opacity">
            {{ message.content }}
          </p>
        </div>

        <div class="mt-3 flex items-center justify-end">
          <button
            v-if="!message.isRead"
            type="button"
            :disabled="markReadLoading || selecting"
            class="cursor-pointer rounded px-2 py-1 text-xs font-medium text-primary-600 transition-colors hover:bg-primary-50 hover:text-primary-700 disabled:cursor-not-allowed disabled:opacity-50 dark:text-primary-400 dark:hover:bg-primary-900/20 dark:hover:text-primary-300"
            @click.stop="emit('mark-read')"
          >
            {{ markReadLoading ? '处理中...' : '标记已读' }}
          </button>
          <span
            v-else
            class="text-xs text-gray-400 dark:text-gray-500"
          >
            已读
          </span>
        </div>
      </div>
    </div>
  </li>
</template>
