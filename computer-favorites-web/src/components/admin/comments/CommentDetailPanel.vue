<script setup lang="ts">
import { computed } from 'vue'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import USkeleton from '@/components/ui-adapter/USkeleton.vue'
import type { AdminCommentDetail } from '@/types/admin-comment'
import { AdminCommentStatus, getCommentStatusMeta } from '@/types/admin-comment'

const props = defineProps<{
  open: boolean
  loading: boolean
  comment: AdminCommentDetail | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'delete', commentId: number): void
  (e: 'restore', commentId: number): void
}>()

const canHide = computed(() =>
  props.comment && props.comment.status === AdminCommentStatus.VISIBLE,
)

const canShow = computed(() =>
  props.comment && props.comment.status === AdminCommentStatus.HIDDEN,
)

const detailModalUi = {
  overlay: 'bg-black/40 backdrop-blur-[2px] z-[120]',
  content:
    'flex max-h-[min(82vh,760px)] w-[min(94vw,860px)] flex-col overflow-hidden rounded-[20px] border border-gray-200 bg-white shadow-[0_18px_42px_rgba(15,23,42,0.24)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_24px_52px_rgba(2,6,23,0.58)] sm:rounded-[22px]',
  header:
    'border-b border-gray-200 bg-gray-50 px-4 py-4 dark:border-dark-border dark:bg-dark-bg sm:px-5 sm:py-4',
  title: 'text-lg font-semibold tracking-tight text-gray-950 dark:text-white',
  description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
  body: 'flex-1 overflow-y-auto px-4 py-4 sm:px-5 sm:py-5',
  footer:
    'border-t border-gray-200 bg-gray-50 px-4 py-3 dark:border-dark-border dark:bg-dark-bg sm:px-5 sm:py-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between',
} as const

const formatDateTime = (value: string | null) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <UModal
    :open="open"
    title="评论详情"
    description="查看完整评论内容、所属网站信息与回复列表。"
    :ui="detailModalUi"
    @update:open="(value) => emit('update:open', value)"
  >
    <template #body>
      <div v-if="loading" class="space-y-4">
        <section class="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <article class="rounded-2xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card sm:p-5">
            <div class="flex flex-wrap items-center gap-2">
              <USkeleton width="4.5rem" height="1.75rem" borderRadius="999px" />
              <USkeleton width="6rem" height="1rem" />
            </div>
            <div class="mt-4 flex items-start gap-4">
              <USkeleton size="3.5rem" borderRadius="1rem" />
              <div class="flex-1 space-y-2">
                <USkeleton width="8rem" height="1.5rem" />
                <USkeleton width="12rem" height="1rem" />
              </div>
            </div>
            <div class="mt-5 space-y-3">
              <USkeleton width="100%" height="1rem" />
              <USkeleton width="92%" height="1rem" />
              <USkeleton width="80%" height="1rem" />
            </div>
          </article>
          <article class="rounded-2xl border border-gray-200 bg-gray-50/80 p-4 dark:border-dark-border dark:bg-dark-bg sm:p-5">
            <div class="space-y-2">
              <USkeleton width="3rem" height="0.85rem" />
              <USkeleton width="5rem" height="1.25rem" />
            </div>
            <div class="mt-4 space-y-4">
              <div v-for="i in 2" :key="i" class="rounded-2xl bg-gray-50 px-4 py-4 dark:bg-dark-card">
                <USkeleton width="100%" height="1rem" />
                <USkeleton width="78%" height="1rem" />
              </div>
            </div>
          </article>
        </section>
      </div>

      <div v-else-if="comment" class="space-y-4">
        <section class="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <!-- 左侧：评论主体 -->
          <article class="overflow-hidden rounded-2xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card sm:p-5">
            <div class="flex flex-wrap items-center gap-2">
              <span
                class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold"
                :class="getCommentStatusMeta(comment.status).toneClass"
              >
                <i :class="getCommentStatusMeta(comment.status).icon" class="text-[10px]"></i>
                {{ getCommentStatusMeta(comment.status).label }}
              </span>
              <span class="text-xs text-gray-500 dark:text-gray-400">评论编号 #{{ comment.id }}</span>
            </div>

            <div class="mt-4 flex items-start gap-4">
              <img
                :src="comment.userAvatar"
                :alt="comment.userName"
                class="h-14 w-14 rounded-2xl border border-gray-200 object-cover dark:border-dark-border"
              />
              <div class="min-w-0">
                <h3 class="text-xl font-semibold tracking-tight text-gray-950 dark:text-white">
                  {{ comment.userName }}
                </h3>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  用户ID: {{ comment.userId }}
                </p>
              </div>
            </div>

            <div class="mt-5 rounded-2xl border border-orange-100 bg-orange-50/70 px-4 py-4 dark:border-orange-400/20 dark:bg-orange-500/10">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-orange-600 dark:text-orange-200">Content</p>
              <p class="mt-2 text-sm leading-7 text-orange-700 dark:text-orange-100">
                {{ comment.content }}
              </p>
            </div>

            <div class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">所属网站</p>
                <div class="mt-2 flex items-center gap-2">
                  <img :src="comment.websiteIcon" class="h-4 w-4 rounded" @error="($event.target as HTMLImageElement).style.display = 'none'" />
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ comment.websiteName }}</p>
                </div>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">点赞数</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  <i class="fas fa-heart text-red-400 mr-1"></i>{{ comment.likeCount }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">举报次数</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  <i class="fas fa-flag text-red-400 mr-1"></i>{{ comment.reportCount }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">创建时间</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ formatDateTime(comment.createTime) }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">最后更新</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ formatDateTime(comment.updateTime) }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">管理操作</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ comment.lastAdminAction || '暂无' }}
                </p>
              </div>
            </div>
          </article>

          <!-- 右侧：回复列表 -->
          <article class="rounded-2xl border border-gray-200 bg-gray-50/80 p-4 dark:border-dark-border dark:bg-dark-bg sm:p-5">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Replies</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">回复列表</h4>
              </div>
              <div
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-gray-600 shadow-sm dark:bg-white/10 dark:text-gray-200"
              >
                <i class="fas fa-comment-dots"></i>
              </div>
            </div>

            <div v-if="comment.replies.length === 0" class="mt-4 rounded-2xl border border-dashed border-gray-300 bg-gray-50 px-4 py-6 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-card dark:text-gray-400">
              该评论暂无回复
            </div>

            <div v-else class="mt-4 space-y-3">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="rounded-2xl bg-white px-4 py-3 dark:bg-dark-card"
                :class="reply.isDeleted ? 'opacity-50' : ''"
              >
                <div class="flex items-start gap-3">
                  <img :src="reply.userAvatar" :alt="reply.userName" class="mt-0.5 h-8 w-8 rounded-xl border border-gray-200 object-cover dark:border-dark-border" />
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ reply.userName }}</span>
                      <span v-if="reply.replyTo" class="text-xs text-gray-400 dark:text-gray-500">
                        回复 <span class="text-orange-600 dark:text-orange-300">{{ reply.replyTo }}</span>
                      </span>
                      <UBadge v-if="reply.isDeleted" color="error" value="已删除" />
                    </div>
                    <p class="mt-1 text-sm leading-6 text-gray-700 dark:text-gray-300" :class="reply.isDeleted ? 'line-through' : ''">
                      {{ reply.content }}
                    </p>
                    <div class="mt-2 flex items-center gap-3 text-xs text-gray-400 dark:text-gray-500">
                      <span><i class="fas fa-heart mr-1 text-red-300"></i>{{ reply.likeCount }}</span>
                      <span>{{ formatDateTime(reply.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </article>
        </section>
      </div>

      <div v-else class="py-12 text-center text-sm text-gray-500 dark:text-gray-400">
        正在加载评论详情...
      </div>
    </template>

    <template #footer>
      <div class="order-2 text-xs text-gray-500 dark:text-gray-400 sm:order-1">
        {{ comment ? `最后更新时间：${formatDateTime(comment.updateTime)}` : '' }}
      </div>

      <div class="order-1 flex w-full flex-col-reverse gap-2 sm:order-2 sm:w-auto sm:flex-row">
        <UButton color="neutral" variant="soft" class="w-full sm:w-auto" @click="emit('update:open', false)">
          关闭详情
        </UButton>
        <UButton
          v-if="canShow"
          color="success"
          variant="soft"
          class="w-full sm:w-auto"
          @click="comment && emit('restore', comment.id)"
        >
          <i class="fas fa-eye text-xs"></i>
          显示评论
        </UButton>
        <UButton
          v-if="canHide"
          color="error"
          variant="soft"
          class="w-full sm:w-auto"
          @click="comment && emit('delete', comment.id)"
        >
          <i class="fas fa-eye-slash text-xs"></i>
          隐藏评论
        </UButton>
      </div>
    </template>
  </UModal>
</template>
