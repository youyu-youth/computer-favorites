<script setup lang="ts">
import { ref, computed } from 'vue'
import { MessageSquareText } from 'lucide-vue-next'
import UCard from '@/components/ui-adapter/UCard.vue'
import CommentInput from './CommentInput.vue'
import CommentItem from './CommentItem.vue'
import { currentUser, mockComments, type CommentItem as CommentItemType, type CommentReply } from './comment-mock'
import { useMessage } from '@/composables/useMessage'

const message = useMessage()

const comments = ref<CommentItemType[]>([...mockComments])

const totalCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach((c) => {
    count += c.replies.length
  })
  return count
})

const handleTopLevelSubmit = (content: string) => {
  const newComment: CommentItemType = {
    id: Date.now(),
    user: { ...currentUser },
    content,
    createTime: new Date().toISOString(),
    likeCount: 0,
    isLiked: false,
    replies: [],
  }
  comments.value.unshift(newComment)
  message.add({ title: '评论发布成功', type: 'success', position: 'top-right' })
}

const handleReply = (commentId: number, content: string) => {
  const comment = comments.value.find((c) => c.id === commentId)
  if (!comment) return
  const newReply: CommentReply = {
    id: Date.now(),
    user: { ...currentUser },
    content,
    createTime: new Date().toISOString(),
    likeCount: 0,
    isLiked: false,
    replyTo: comment.user.nickname,
  }
  comment.replies.push(newReply)
  message.add({ title: '回复成功', type: 'success', position: 'top-right' })
}

const handleReplyToReply = (commentId: number, replyId: number, content: string) => {
  const comment = comments.value.find((c) => c.id === commentId)
  if (!comment) return
  const targetReply = comment.replies.find((r) => r.id === replyId)
  const newReply: CommentReply = {
    id: Date.now(),
    user: { ...currentUser },
    content,
    createTime: new Date().toISOString(),
    likeCount: 0,
    isLiked: false,
    replyTo: targetReply?.user.nickname,
  }
  comment.replies.push(newReply)
  message.add({ title: '回复成功', type: 'success', position: 'top-right' })
}

const handleLike = (commentId: number) => {
  const comment = comments.value.find((c) => c.id === commentId)
  if (!comment) return
  comment.isLiked = !comment.isLiked
  comment.likeCount += comment.isLiked ? 1 : -1
}

const handleLikeReply = (replyId: number) => {
  for (const comment of comments.value) {
    const reply = comment.replies.find((r) => r.id === replyId)
    if (reply) {
      reply.isLiked = !reply.isLiked
      reply.likeCount += reply.isLiked ? 1 : -1
      break
    }
  }
}
</script>

<template>
  <UCard class="mt-6">
    <div class="flex items-center gap-2 mb-5">
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

    <CommentInput
      :avatar="currentUser.avatar"
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
      />
    </div>

    <div
      v-if="comments.length === 0"
      class="py-12 text-center text-sm text-slate-400 dark:text-slate-500"
    >
      暂无评论，来发表第一条评论吧～
    </div>
  </UCard>
</template>
