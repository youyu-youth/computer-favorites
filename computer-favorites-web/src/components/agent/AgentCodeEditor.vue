<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="$emit('close')">
    <div class="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[80vh] flex flex-col">
      <!-- Header -->
      <div class="flex items-center justify-between px-4 py-3 border-b border-gray-200 dark:border-gray-700">
        <h3 class="font-semibold text-sm text-gray-800 dark:text-gray-100">插入代码</h3>
        <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 cursor-pointer" @click="$emit('close')">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Language selector -->
      <div class="px-4 pt-3">
        <select
          v-model="language"
          class="w-full text-sm px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600
                 bg-white dark:bg-gray-900 text-gray-800 dark:text-gray-200
                 focus:outline-none focus:border-amber-400 cursor-pointer"
        >
          <option v-for="lang in languages" :key="lang.value" :value="lang.value">
            {{ lang.name }}
          </option>
        </select>
      </div>

      <!-- Code input -->
      <div class="flex-1 px-4 py-3">
        <textarea
          ref="codeInputRef"
          v-model="code"
          class="w-full min-h-[200px] text-sm font-mono px-3 py-2 rounded-lg
                 border border-gray-300 dark:border-gray-600
                 bg-gray-50 dark:bg-gray-950 text-gray-800 dark:text-gray-200
                 resize-none focus:outline-none focus:border-amber-400"
          placeholder="在此粘贴代码..."
          spellcheck="false"
        />
      </div>

      <!-- Footer -->
      <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-2">
        <button
          class="px-4 py-2 text-sm rounded-lg border border-gray-300 dark:border-gray-600
                 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800
                 transition-colors cursor-pointer"
          @click="$emit('close')"
        >
          取消
        </button>
        <button
          class="px-4 py-2 text-sm rounded-lg bg-amber-500 text-white hover:bg-amber-600
                 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="!code.trim()"
          @click="insert"
        >
          插入
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

defineOptions({ name: 'AgentCodeEditor' })

const emit = defineEmits<{
  close: []
  insert: [code: string, language: string]
}>()

const language = ref('java')
const code = ref('')
const codeInputRef = ref<HTMLTextAreaElement | null>(null)

const languages = [
  { name: 'Java', value: 'java' },
  { name: 'Python', value: 'python' },
  { name: 'JavaScript', value: 'javascript' },
  { name: 'TypeScript', value: 'typescript' },
  { name: 'C', value: 'c' },
  { name: 'C++', value: 'cpp' },
  { name: 'Go', value: 'go' },
  { name: 'Rust', value: 'rust' },
  { name: 'SQL', value: 'sql' },
  { name: 'Bash', value: 'bash' },
  { name: '无高亮', value: '' },
]

function insert() {
  if (!code.value.trim()) return
  emit('insert', code.value, language.value)
}

onMounted(() => {
  codeInputRef.value?.focus()
})
</script>
