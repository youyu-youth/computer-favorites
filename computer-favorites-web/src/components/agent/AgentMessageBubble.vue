<template>
  <div
    class="message-bubble flex gap-3"
    :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
  >
    <!-- Avatar -->
    <div class="shrink-0">
      <!-- User avatar -->
      <div
        v-if="msg.role === 'user'"
        class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center"
      >
        <svg class="w-4 h-4 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0" />
        </svg>
      </div>
      <!-- AI avatar -->
      <div
        v-else
        class="w-8 h-8 rounded-full bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center"
      >
        <svg class="w-4 h-4 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
        </svg>
      </div>
    </div>

    <!-- Message body -->
    <div class="max-w-[75%] md:max-w-[70%]">
      <!-- Thinking steps (AI only) -->
      <AgentThinkingSteps
        v-if="msg.role === 'assistant' && msg.thinkingSteps.length > 0"
        :steps="msg.thinkingSteps"
        class="mb-1.5"
      />

      <!-- Text content -->
      <div
        v-if="msg.content"
        class="px-4 py-2.5 text-sm rounded-xl leading-relaxed whitespace-pre-wrap break-words"
        :class="msg.role === 'user'
          ? 'bg-amber-500 text-white rounded-br-md'
          : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-bl-md'"
      >
        {{ msg.content }}
        <!-- Streaming cursor -->
        <span
          v-if="isStreaming && msg.role === 'assistant'"
          class="inline-block w-0.5 h-4 bg-amber-500 animate-pulse ml-0.5 align-text-bottom"
        />
      </div>

      <!-- Action bar (AI, not streaming, has content) -->
      <div
        v-if="msg.role === 'assistant' && !isStreaming && msg.content"
        class="flex gap-3 mt-1.5 text-xs text-gray-400 dark:text-gray-500"
      >
        <button class="hover:text-gray-600 dark:hover:text-gray-300 transition-colors cursor-pointer" @click="copyContent">复制</button>
        <button class="hover:text-gray-600 dark:hover:text-gray-300 transition-colors cursor-pointer" @click="$emit('regenerate')">重新生成</button>
        <button
          class="transition-colors cursor-pointer"
          :class="feedback === 'like' ? 'text-green-500' : 'hover:text-gray-600 dark:hover:text-gray-300'"
          @click="toggleFeedback('like')"
        >
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3" />
          </svg>
        </button>
        <button
          class="transition-colors cursor-pointer"
          :class="feedback === 'dislike' ? 'text-red-500' : 'hover:text-gray-600 dark:hover:text-gray-300'"
          @click="toggleFeedback('dislike')"
        >
          <svg class="w-3.5 h-3.5 rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3" />
          </svg>
        </button>
      </div>

      <!-- Edit mode (user messages) -->
      <div v-if="isEditing" class="mt-2 flex gap-2">
        <textarea
          ref="editInputRef"
          v-model="editContent"
          class="flex-1 text-sm px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600
                 bg-white dark:bg-gray-900 text-gray-800 dark:text-gray-200 resize-none
                 focus:outline-none focus:border-amber-400"
          rows="2"
        />
        <div class="flex flex-col gap-1">
          <button class="px-3 py-1 text-xs rounded-md bg-amber-500 text-white hover:bg-amber-600 cursor-pointer" @click="confirmEdit">发送</button>
          <button class="px-3 py-1 text-xs rounded-md border border-gray-300 dark:border-gray-600 text-gray-500 hover:text-gray-700 cursor-pointer" @click="cancelEdit">取消</button>
        </div>
      </div>

      <!-- Edit button (user messages, not streaming, not editing) -->
      <div v-if="msg.role === 'user' && !isStreaming && !isEditing" class="flex justify-end mt-1">
        <button class="text-xs text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors cursor-pointer" @click="startEdit">编辑</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import type { AgentMessage } from '@/types/agent'
import AgentThinkingSteps from '@/components/agent/AgentThinkingSteps.vue'

defineOptions({ name: 'AgentMessageBubble' })

const props = defineProps<{
  msg: AgentMessage
  isStreaming: boolean
}>()

const emit = defineEmits<{
  regenerate: []
  editResend: [msgId: string, newContent: string]
}>()

const feedback = ref<'like' | 'dislike' | null>(null)
const isEditing = ref(false)
const editContent = ref('')
const editInputRef = ref<HTMLTextAreaElement | null>(null)

async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.msg.content)
  } catch { /* ignore */ }
}

function toggleFeedback(type: 'like' | 'dislike') {
  feedback.value = feedback.value === type ? null : type
}

function startEdit() {
  isEditing.value = true
  editContent.value = props.msg.content
  nextTick(() => editInputRef.value?.focus())
}

function cancelEdit() {
  isEditing.value = false
  editContent.value = ''
}

function confirmEdit() {
  if (!editContent.value.trim()) return
  emit('editResend', props.msg.id, editContent.value.trim())
  isEditing.value = false
}
</script>
