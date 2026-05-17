<template>
  <div class="border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-950 px-3 md:px-4 py-3">
    <!-- Skill selector + quota -->
    <div class="flex items-center gap-2 mb-2 text-xs text-gray-500 dark:text-gray-400">
      <select
        :value="store.currentSkillCode"
        class="text-xs px-2 py-1 rounded-md border border-gray-200 dark:border-gray-700
               bg-white dark:bg-gray-900 text-gray-700 dark:text-gray-300
               focus:outline-none focus:border-amber-400 cursor-pointer"
        @change="store.currentSkillCode = ($event.target as HTMLSelectElement).value"
      >
        <option v-for="skill in store.skills" :key="skill.code" :value="skill.code">
          {{ skill.name }}
        </option>
      </select>
      <span v-if="store.quotaRemaining !== null" class="ml-auto">
        配额 {{ store.quotaRemaining }}/{{ store.quota?.dailyMessageLimit ?? '-' }}
      </span>
    </div>

    <!-- Input row -->
    <div class="flex items-end gap-2">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        rows="1"
        class="flex-1 min-h-[40px] max-h-[160px] resize-none text-sm px-3 py-2
               rounded-lg border border-gray-300 dark:border-gray-600
               bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200
               placeholder-gray-400 dark:placeholder-gray-500
               focus:outline-none focus:border-amber-400 transition-colors"
        :disabled="disabled"
        placeholder="输入问题，Enter 发送，Shift+Enter 换行..."
        @keydown="handleKeydown"
        @input="autoResize"
      />

      <!-- Buttons -->
      <div class="flex items-center gap-1 shrink-0">
        <button
          class="px-2 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600
                 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200
                 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer
                 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="disabled"
          title="插入代码"
          @click="showCodeEditor = true"
        >
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M17.25 6.75L22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3l-4.5 16.5" />
          </svg>
        </button>
        <button
          class="px-2 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600
                 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200
                 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer
                 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="disabled"
          title="上传文件"
          @click="triggerFileUpload"
        >
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
          </svg>
        </button>
        <input
          ref="fileInputRef"
          type="file"
          accept=".java,.py,.js,.ts,.cpp,.c,.go,.rs,.txt,.md,.json,.xml,.yml,.yaml,image/*"
          class="hidden"
          multiple
          @change="handleFileChange"
        />
        <button
          class="px-4 py-1.5 text-sm rounded-lg bg-amber-500 text-white hover:bg-amber-600
                 transition-colors cursor-pointer font-medium
                 disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="disabled || !inputText.trim()"
          @click="send"
        >
          发送
        </button>
      </div>
    </div>

    <!-- File chips -->
    <div v-if="files.length > 0" class="flex gap-2 mt-2 flex-wrap">
      <div
        v-for="(file, idx) in files"
        :key="idx"
        class="flex items-center gap-1 px-2 py-0.5 text-xs rounded-md
               bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300"
      >
        <span class="max-w-[120px] truncate">{{ file.name }}</span>
        <button class="text-gray-400 hover:text-red-500 cursor-pointer" @click="removeFile(idx)">
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Code editor modal (teleported to body) -->
    <Teleport to="body">
      <AgentCodeEditor
        v-if="showCodeEditor"
        @close="showCodeEditor = false"
        @insert="insertCodeBlock"
      />
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentCodeEditor from '@/components/agent/AgentCodeEditor.vue'

defineOptions({ name: 'AgentInputArea' })

const emit = defineEmits<{
  send: [message: string, files: File[]]
}>()

const store = useAgentChatStore()

const inputText = ref('')
const files = ref<File[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const showCodeEditor = ref(false)

const disabled = computed(() => store.connectionState === 'streaming' || store.connectionState === 'connecting')

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function send() {
  if (!inputText.value.trim() && files.value.length === 0) return
  emit('send', inputText.value, [...files.value])
  inputText.value = ''
  files.value = []
  nextTick(() => {
    const el = textareaRef.value
    if (el) el.style.height = 'auto'
  })
}

function triggerFileUpload() {
  fileInputRef.value?.click()
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files) {
    files.value.push(...Array.from(target.files))
  }
  target.value = ''
}

function removeFile(idx: number) {
  files.value.splice(idx, 1)
}

function insertCodeBlock(code: string, language: string) {
  const langTag = language || ''
  inputText.value += `\n\`\`\`${langTag}\n${code}\n\`\`\`\n`
  showCodeEditor.value = false
  nextTick(() => textareaRef.value?.focus())
}
</script>
