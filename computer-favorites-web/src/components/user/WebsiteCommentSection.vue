<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { MessageSquareText, Clock, Flame } from 'lucide-vue-next'
import UCard from '@/components/ui-adapter/UCard.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import CommentInput from './CommentInput.vue'
import CommentItem from './CommentItem.vue'
import { useAuthStore } from '@/stores/auth'
import { useMessage } from '@/composables/useMessage'
import {
  publishComment,
  getCommentPage,
  likeComment,
  unlikeComment,
  deleteComment,
} from '@/api/user-comment'
import type {
  CommentItem as CommentItemType,
  CommentReply,
  CommentPageResult,
} from '@/types/comment'

const props = defineProps<{
  websiteId: number | null
}>()

const message = useMessage()
const authStore = useAuthStore()

const comments = ref<CommentItemType[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const sort = ref<'time' | 'hot'>('time')
const loading = ref(false)
const hasMore = ref(true)

const currentAvatar = computed(() => authStore.avatar || '')

const totalCount = computed(() => total.value)

const loadComments = async (reset = false) => {
  if (loading.value || !props.websiteId) return
  if (!reset && !hasMore.value) return

  loading.value = true
  try {
    if (reset) {
      pageNum.value = 1
      hasMore.value = true
    }
    const result: CommentPageResult = await getCommentPage(props.websiteId, {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sort: sort.value,
    })
    if (reset) {
      comments.value = result.records
    } else {
      comments.value.push(...result.records)
    }
    total.value = result.total
    hasMore.value = pageNum.value < result.totalPages
  } catch (e: unknown) {
    message.add({ title: (e as Error).message || '加载评论失败', type: 'error', position: 'top-right' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  loadComments()
}

const handleSortChange = (newSort: 'time' | 'hot') => {
  if (sort.value === newSort) return
  sort.value = newSort
  loadComments(true)
}

const handleTopLevelSubmit = async (content: string) => {
  if (!props.websiteId) return
  try {
    await publishComment(props.websiteId, { content })
    message.add({ title: '评论发布成功', type: 'success', position: 'top-right' })
    loadComments(true)
  } catch (e: unknown) {
    message.add({ title: (e as Error).message || '评论发布失败', type: 'error', position: 'top-right' })
  }
}

const handleReply = async (commentId: number, content: string) => {
  if (!props.websiteId) return
  try {
    await publishComment(props.websiteId, { content, parentId: commentId })
    message.add({ title: '回复成功', type: 'success', position: 'top-right' })
    loadComments(true)
  } catch (e: unknown) {
    message.add({ title: (e as Error).message || '回复失败', type: 'error', position: 'top-right' })
  }
}

const handleReplyToReply = async (
  commentId: number,
  replyId: number,
  replyUserId: number,
  content: string,
) => {
  if (!props.websiteId) return
  try {
    await publishComment(props.websiteId, {
      content,
      parentId: commentId,
      replyUserId,
    })
    message.add({ title: '回复成功', type: 'success', position: 'top-right' })
    loadComments(true)
  } catch (e: unknown) {
    message.add({ title: (e as Error).message || '回复失败', type: 'error', position: 'top-right' })
  }
}

const toggleLike = async (commentId: number, isLiked: boolean) => {
  const target = findCommentOrReply(commentId)
  if (!target) return

  const prevLiked = target.isLiked
  const prevCount = target.likeCount
  target.isLiked = !prevLiked
  target.likeCount = prevLiked ? prevCount - 1 : prevCount + 1

  try {
    if (prevLiked) {
      await unlikeComment(commentId)
    } else {
      await likeComment(commentId)
    }
  } catch {
    target.isLiked = prevLiked
    target.likeCount = prevCount
  }
}

const handleLike = (commentId: number) => {
  const comment = comments.value.find((c) => c.id === commentId)
  if (comment) {
    toggleLike(commentId, comment.isLiked)
  }
}

const handleLikeReply = (replyId: number) => {
  for (const comment of comments.value) {
    const reply = comment.replies.find((r) => r.id === replyId)
    if (reply) {
      toggleLike(replyId, reply.isLiked)
      break
    }
  }
}

const handleDelete = async (commentId: number) => {
  try {
    await deleteComment(commentId)
    message.add({ title: '删除成功', type: 'success', position: 'top-right' })
    loadComments(true)
  } catch (e: unknown) {
    message.add({ title: (e as Error).message || '删除失败', type: 'error', position: 'top-right' })
  }
}

const findCommentOrReply = (
  id: number,
): { isLiked: boolean; likeCount: number } | null => {
  for (const c of comments.value) {
    if (c.id === id) return c
    const reply = c.replies.find((r) => r.id === id)
    if (reply) return reply
  }
  return null
}

watch(() => props.websiteId, () => {
  comments.value = []
  total.value = 0
  if (props.websiteId) {
    loadComments(true)
  }
})

onMounted(() => {
  if (props.websiteId) {
    loadComments(true)
  }
})
</script>

<template>
  <UCard class="mt-6">
    <div class="mb-5 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <MessageSquareText class="h-5 w-5 text-[rgb(var(--cf-color-primary-500-rgb)/1)]" />
        <h3 class="text-base font-bold text-slate-900 dark:text-white">
          网站评论
        </h3>
        <span
          class="rounded-full bg-[rgb(var(--cf-color-primary-50-rgb)/1)] px-2.5 py-0.5 text-xs font-medium text-[rgb(var(--cf-color-primary-600-rgb)/1)] dark:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)]"
        >
          {{ totalCount }}
        </span>
      </div>

      <div class="flex items-center gap-1 rounded-lg bg-slate-100 p-0.5 dark:bg-white/10">
        <button
          class="flex items-center gap-1 rounded-md px-2.5 py-1 text-xs transition-colors cursor-pointer"
          :class="
            sort === 'time'
              ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-700 dark:text-slate-100'
              : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          "
          @click="handleSortChange('time')"
        >
          <Clock class="h-3 w-3" />
          最新
        </button>
        <button
          class="flex items-center gap-1 rounded-md px-2.5 py-1 text-xs transition-colors cursor-pointer"
          :class="
            sort === 'hot'
              ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-700 dark:text-slate-100'
              : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          "
          @click="handleSortChange('hot')"
        >
          <Flame class="h-3 w-3" />
          最热
        </button>
      </div>
    </div>

    <CommentInput
      :avatar="currentAvatar"
      placeholder="分享你对这个网站的看法..."
      submit-label="发表评论"
      @submit="handleTopLevelSubmit"
    />

    <div class="mt-6 divide-y divide-slate-100 dark:divide-white/5">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @like="handleLike"
        @like-reply="handleLikeReply"
        @reply-to-reply="handleReplyToReply"
        @delete="handleDelete"
      />
    </div>

    <div
      v-if="comments.length === 0 && !loading"
      class="py-12 text-center text-sm text-slate-400 dark:text-slate-500"
    >
      暂无评论，来发表第一条评论吧～
    </div>

    <div v-if="hasMore && comments.length > 0" class="mt-4 text-center">
      <UButton size="sm" variant="text" :loading="loading" @click="loadMore">
        加载更多
      </UButton>
    </div>
  </UCard>
</template>
