<script setup lang="ts">
import { ref, computed } from 'vue'
import { ThumbsUp, MessageCircle, Trash2 } from 'lucide-vue-next'
import UAvatar from '@/components/ui-adapter/UAvatar.vue'
import CommentInput from './CommentInput.vue'
import type { CommentItem as CommentItemType, CommentReply } from '@/types/comment'
import { useAuthStore } from '@/stores/auth'

const props = withDefaults(
  defineProps<{
    comment: CommentItemType
    depth?: number
  }>(),
  {
    depth: 0,
  }
)

const emit = defineEmits<{
  (e: 'reply', commentId: number, content: string): void
  (e: 'like', commentId: number): void
  (e: 'likeReply', replyId: number): void
  (e: 'replyToReply', commentId: number, replyId: number, replyUserId: number, content: string): void
  (e: 'delete', commentId: number): void
}>()

const authStore = useAuthStore()

const showReplyInput = ref(false)
const replyTarget = ref<{ type: 'comment' | 'reply'; id: number; userId: number; nickname: string } | null>(null)

const maxDepth = 2
const canNest = computed(() => props.depth < maxDepth - 1)

const isOwnComment = computed(() => {
  const currentUserId = authStore.userId
  return currentUserId != null && props.comment.user.id === Number(currentUserId)
})

const relativeTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(' ', 'T'))
  if (isNaN(date.getTime())) return dateStr
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 0) return '刚刚'
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return dateStr.slice(0, 10)
}

const handleReplyClick = () => {
  replyTarget.value = { type: 'comment', id: props.comment.id, userId: props.comment.user.id, nickname: props.comment.user.nickname }
  showReplyInput.value = true
}

const handleReplyToReplyClick = (reply: CommentReply) => {
  if (!canNest.value) return
  replyTarget.value = { type: 'reply', id: reply.id, userId: reply.user.id, nickname: reply.user.nickname }
  showReplyInput.value = true
}

const handleReplySubmit = (content: string) => {
  if (!replyTarget.value) return
  if (replyTarget.value.type === 'comment') {
    emit('reply', props.comment.id, content)
  } else {
    emit('replyToReply', props.comment.id, replyTarget.value.id, replyTarget.value.userId, content)
  }
  showReplyInput.value = false
  replyTarget.value = null
}

const handleCancelReply = () => {
  showReplyInput.value = false
  replyTarget.value = null
}
</script>

<template>
  <div
    class="group"
    :class="depth > 0 ? 'ml-0 sm:ml-4' : ''"
  >
    <div class="flex gap-3 py-4">
      <div v-if="depth > 0" class="relative hidden sm:flex">
        <div
          class="absolute -left-3 top-0 h-full w-px bg-slate-200 dark:bg-white/10"
        />
      </div>

      <UAvatar
        :src="comment.user.avatar"

        size="sm"
        class="h-9 w-9 flex-shrink-0"
      />

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-2">
          <span class="truncate text-sm font-semibold text-slate-900 dark:text-slate-100">
            {{ comment.user.nickname }}
          </span>
          <span class="text-xs text-slate-400 dark:text-slate-500">
            {{ relativeTime(comment.createTime) }}
          </span>
        </div>

        <p
          class="mt-1.5 text-sm leading-relaxed"
          :class="
            comment.isDeleted
              ? 'italic text-slate-400 dark:text-slate-500'
              : 'text-slate-700 dark:text-slate-300'
          "
        >
          {{ comment.content }}
        </p>

        <div class="mt-2 flex items-center gap-4">
          <button
            class="flex items-center gap-1 text-xs transition-colors cursor-pointer"
            :class="
              comment.isLiked
                ? 'text-[rgb(var(--cf-color-primary-500-rgb)/1)]'
                : 'text-slate-400 hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-300'
            "
            @click="emit('like', comment.id)"
          >
            <ThumbsUp class="h-3.5 w-3.5" :class="comment.isLiked ? 'fill-current' : ''" />
            {{ comment.likeCount > 0 ? comment.likeCount : '点赞' }}
          </button>

          <button
            v-if="!comment.isDeleted"
            class="flex items-center gap-1 text-xs text-slate-400 transition-colors hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-300 cursor-pointer"
            @click="handleReplyClick"
          >
            <MessageCircle class="h-3.5 w-3.5" />
            回复
          </button>

          <button
            v-if="isOwnComment && !comment.isDeleted"
            class="flex items-center gap-1 text-xs text-slate-400 transition-colors hover:text-red-500 dark:text-slate-500 dark:hover:text-red-400 cursor-pointer"
            @click="emit('delete', comment.id)"
          >
            <Trash2 class="h-3.5 w-3.5" />
            删除
          </button>
        </div>

        <div
          v-if="showReplyInput"
          class="mt-3 overflow-hidden transition-all duration-300"
        >
          <div class="rounded-lg border border-slate-100 bg-slate-50 p-3 dark:border-white/5 dark:bg-white/5">
            <div class="mb-2 flex items-center justify-between">
              <span class="text-xs text-slate-500 dark:text-slate-400">
                回复 <span class="font-medium text-[rgb(var(--cf-color-primary-500-rgb)/1)]">@{{ replyTarget?.nickname }}</span>
              </span>
              <button
                class="text-xs text-slate-400 hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-300 cursor-pointer"
                @click="handleCancelReply"
              >
                取消
              </button>
            </div>
            <CommentInput
              :placeholder="`回复 ${replyTarget?.nickname}...`"
              submit-label="发送"
              @submit="handleReplySubmit"
            />
          </div>
        </div>

        <div v-if="comment.replies.length > 0" class="mt-2 space-y-0">
          <div
            v-for="reply in comment.replies"
            :key="reply.id"
            class="flex gap-3 py-3"
          >
            <div class="relative hidden sm:flex">
              <div
                class="absolute -left-3 top-0 h-full w-px bg-slate-200 dark:bg-white/10"
              />
            </div>

            <UAvatar
              :src="reply.user.avatar"
              size="sm"
              class="h-8 w-8 flex-shrink-0"
            />

            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span class="truncate text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {{ reply.user.nickname }}
                </span>
                <span v-if="reply.replyTo" class="text-xs text-slate-400 dark:text-slate-500">
                  回复
                </span>
                <span v-if="reply.replyTo" class="text-xs font-medium text-[rgb(var(--cf-color-primary-500-rgb)/1)]">
                  @{{ reply.replyTo }}
                </span>
                <span class="text-xs text-slate-400 dark:text-slate-500">
                  {{ relativeTime(reply.createTime) }}
                </span>
              </div>

              <p
                class="mt-1 text-sm leading-relaxed"
                :class="
                  reply.isDeleted
                    ? 'italic text-slate-400 dark:text-slate-500'
                    : 'text-slate-700 dark:text-slate-300'
                "
              >
                {{ reply.content }}
              </p>

              <div class="mt-1.5 flex items-center gap-4">
                <button
                  class="flex items-center gap-1 text-xs transition-colors cursor-pointer"
                  :class="
                    reply.isLiked
                      ? 'text-[rgb(var(--cf-color-primary-500-rgb)/1)]'
                      : 'text-slate-400 hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-300'
                  "
                  @click="emit('likeReply', reply.id)"
                >
                  <ThumbsUp class="h-3.5 w-3.5" :class="reply.isLiked ? 'fill-current' : ''" />
                  {{ reply.likeCount > 0 ? reply.likeCount : '点赞' }}
                </button>

                <button
                  v-if="canNest && !reply.isDeleted"
                  class="flex items-center gap-1 text-xs text-slate-400 transition-colors hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-300 cursor-pointer"
                  @click="handleReplyToReplyClick(reply)"
                >
                  <MessageCircle class="h-3.5 w-3.5" />
                  回复
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
