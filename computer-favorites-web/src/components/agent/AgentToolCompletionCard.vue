<template>
  <div
    class="cf-tool-card mt-3 rounded-xl bg-white/90 dark:bg-stone-900/80 ring-1 ring-stone-200/70 dark:ring-stone-700/60 shadow-sm overflow-hidden"
  >
    <!-- 头部 -->
    <div class="flex items-center gap-2 px-4 py-2.5 border-b border-stone-100 dark:border-stone-800">
      <span class="text-xs text-stone-500 dark:text-stone-400 font-medium">
        {{ data.message }}
      </span>
    </div>

    <!-- 已收集字段 -->
    <div v-if="collectedEntries.length > 0" class="px-4 py-2">
      <div class="text-[10px] font-semibold uppercase tracking-wider text-stone-400 dark:text-stone-500 mb-1.5">
        已收集
      </div>
      <div class="space-y-1">
        <div
          v-for="[key, val] in collectedEntries"
          :key="key"
          class="flex items-center gap-2 text-xs"
        >
          <CheckCircle class="text-emerald-500 shrink-0" :size="12" :stroke-width="2" />
          <span class="text-stone-500 dark:text-stone-400 w-20 shrink-0">{{ fieldLabel(key) }}</span>
          <span class="text-stone-800 dark:text-stone-200 truncate">{{ val }}</span>
        </div>
      </div>
    </div>

    <!-- 缺失字段 -->
    <div class="px-4 py-2">
      <div class="text-[10px] font-semibold uppercase tracking-wider text-amber-600 dark:text-amber-400 mb-1.5">
        待补充
      </div>
      <div class="space-y-2.5">
        <div v-for="field in data.missing" :key="field.field">
          <label class="block text-xs text-stone-500 dark:text-stone-400 mb-1">
            {{ field.label }}
            <span v-if="field.required" class="text-rose-500">*</span>
            <span v-else class="text-stone-400">(可选)</span>
          </label>

          <!-- select 类型 -->
          <select
            v-if="field.type === 'select'"
            v-model="userValues[field.field]"
            class="w-full rounded-lg px-3 py-1.5 text-xs bg-stone-50 dark:bg-stone-800 ring-1 ring-stone-200 dark:ring-stone-700 text-stone-800 dark:text-stone-100 outline-none focus:ring-2 focus:ring-primary-400 cursor-pointer"
          >
            <option value="" disabled>请选择</option>
            <option
              v-for="s in field.suggestions"
              :key="s.value"
              :value="s.value"
            >
              {{ s.label }}
            </option>
          </select>

          <!-- text 类型 -->
          <input
            v-else
            v-model="userValues[field.field]"
            type="text"
            class="w-full rounded-lg px-3 py-1.5 text-xs bg-stone-50 dark:bg-stone-800 ring-1 ring-stone-200 dark:ring-stone-700 text-stone-800 dark:text-stone-100 outline-none focus:ring-2 focus:ring-primary-400"
            :placeholder="'输入' + field.label"
          />

          <!-- 推荐项 -->
          <div v-if="field.suggestions.length > 0" class="flex flex-wrap gap-1 mt-1">
            <span class="text-[10px] text-stone-400 dark:text-stone-500">推荐：</span>
            <button
              v-for="s in field.suggestions"
              :key="s.value"
              type="button"
              class="text-[10px] px-1.5 py-0.5 rounded-full bg-stone-100 dark:bg-stone-800 text-stone-600 dark:text-stone-300 hover:bg-primary-500/10 hover:text-primary-600 dark:hover:text-primary-400 transition-colors cursor-pointer"
              @click="applySuggestion(field.field, s.value)"
            >
              {{ s.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="flex items-center gap-2 px-4 py-2.5 border-t border-stone-100 dark:border-stone-800">
      <button
        type="button"
        class="cf-tool-submit-btn inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-white bg-primary-500 hover:bg-primary-600 shadow-sm transition-colors cursor-pointer"
        :disabled="!hasRequiredFields"
        @click="submitCompletion"
      >
        <Send :size="12" :stroke-width="2" />
        补充并发送
      </button>
      <button
        type="button"
        class="text-xs text-stone-400 dark:text-stone-500 hover:text-stone-600 dark:hover:text-stone-300 transition-colors cursor-pointer"
        @click="$emit('close')"
      >
        让助手继续问
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CheckCircle, Send } from 'lucide-vue-next'
import type { ToolIncompleteData } from '@/types/agent'

defineOptions({ name: 'AgentToolCompletionCard' })

const props = defineProps<{
  data: ToolIncompleteData
}>()

const emit = defineEmits<{
  submit: [message: string]
  close: []
}>()

const userValues = ref<Record<string, any>>({})

const collectedEntries = computed(() => Object.entries(props.data.collected))

const hasRequiredFields = computed(() => {
  return props.data.missing
    .filter((f) => f.required)
    .every((f) => userValues.value[f.field])
})

const fieldLabels: Record<string, string> = {
  name: '网站名称',
  url: '网站URL',
  description: '描述',
  categoryId: '分类',
  tags: '标签',
}

function fieldLabel(key: string): string {
  return fieldLabels[key] || key
}

function applySuggestion(fieldName: string, value: any) {
  userValues.value[fieldName] = value
}

function submitCompletion() {
  const parts: string[] = []
  for (const [key, val] of collectedEntries.value) {
    // 跳过无效值（0、null、空字符串）
    if (val === null || val === undefined || val === 0 || val === '') continue
    parts.push(`${fieldLabel(key)}：${val}`)
  }
  for (const field of props.data.missing) {
    const v = userValues.value[field.field]
    if (v !== null && v !== undefined && v !== '' && v !== 0) {
      parts.push(`${field.label}：${v}`)
    }
  }
  const message = `请帮我把以下网站提交投稿：\n${parts.join('\n')}`
  emit('submit', message)
}
</script>

<style scoped>
.cf-tool-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
