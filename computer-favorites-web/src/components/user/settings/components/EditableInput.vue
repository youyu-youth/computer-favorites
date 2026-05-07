<script setup lang="ts">
import { computed, inject, nextTick, ref, watch } from 'vue'
import { settingsLockSignalKey } from '@/components/user/settings/context'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    as?: 'input' | 'textarea'
    type?: string
    placeholder?: string
    maxlength?: number
    rows?: number
    autocomplete?: string
    inputClass?: string
  }>(),
  {
    modelValue: '',
    as: 'input',
    type: 'text',
    placeholder: '',
    maxlength: undefined,
    rows: 3,
    autocomplete: 'off',
    inputClass: '',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const isEditing = ref(false)
const snapshot = ref<string>(props.modelValue ?? '')
const wrapperRef = ref<HTMLElement | null>(null)

const innerValue = computed<string>({
  get: () => props.modelValue ?? '',
  set: (v: string) => emit('update:modelValue', v),
})

const isEmpty = computed(() => {
  const v = props.modelValue
  return v === undefined || v === null || String(v).trim() === ''
})

const lockSignal = inject(settingsLockSignalKey, ref(0))
watch(lockSignal, () => {
  // 父组件保存成功 → 统一重新锁定，并把当前值作为新的 baseline 快照
  isEditing.value = false
  snapshot.value = props.modelValue ?? ''
})

const handleEdit = async () => {
  snapshot.value = props.modelValue ?? ''
  isEditing.value = true
  await nextTick()
  const target = wrapperRef.value?.querySelector('input, textarea') as
    | HTMLInputElement
    | HTMLTextAreaElement
    | null
  if (target) {
    target.focus()
    const len = target.value?.length ?? 0
    try {
      target.setSelectionRange(len, len)
    } catch {
      // 部分 input type 不支持 setSelectionRange
    }
  }
}

const handleCancel = () => {
  emit('update:modelValue', snapshot.value)
  isEditing.value = false
}

const textareaMinHeight = computed(() => `${(props.rows ?? 3) * 24 + 16}px`)
</script>

<template>
  <div ref="wrapperRef" class="flex w-full items-start gap-2">
    <!-- 编辑态 -->
    <template v-if="isEditing">
      <UInput
        v-if="as === 'input'"
        v-model="innerValue"
        :type="type"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :autocomplete="autocomplete"
        :class="['flex-1', inputClass]"
      />
      <UTextarea
        v-else
        v-model="innerValue"
        :rows="rows"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :class="['flex-1', inputClass]"
      />
      <button
        type="button"
        class="cf-edit-action mt-0 inline-flex h-10 shrink-0 cursor-pointer items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 text-xs font-medium text-slate-600 transition-colors hover:border-slate-400 hover:text-slate-900 dark:border-white/15 dark:bg-[#1c1c25] dark:text-slate-300 dark:hover:border-white/25 dark:hover:bg-[#22222c] dark:hover:text-white"
        title="取消修改并恢复原值"
        @click="handleCancel"
      >
        <UIcon name="i-lucide-x" class="h-3.5 w-3.5" />
        <span>取消</span>
      </button>
    </template>

    <!-- 锁定态：纯文本展示 + 编辑按钮 -->
    <template v-else>
      <div
        class="cf-locked-field flex-1 rounded-lg border border-slate-200 bg-slate-50/60 px-3 py-2 leading-6 transition-colors dark:border-white/[0.08] dark:bg-[#13131a]"
        :class="[
          isEmpty ? 'text-slate-400 dark:text-slate-500' : 'text-slate-900 dark:text-white',
          inputClass,
        ]"
      >
        <span v-if="as === 'input'" class="block truncate">
          {{ isEmpty ? placeholder || '未填写' : modelValue }}
        </span>
        <span
          v-else
          class="block whitespace-pre-wrap break-words"
          :style="{ minHeight: textareaMinHeight }"
        >
          {{ isEmpty ? placeholder || '未填写' : modelValue }}
        </span>
      </div>
      <button
        type="button"
        class="cf-edit-action mt-0 inline-flex h-10 shrink-0 cursor-pointer items-center gap-1.5 rounded-lg border border-amber-400/40 bg-amber-50 px-3 text-xs font-medium text-amber-700 transition-colors hover:border-amber-400 hover:bg-amber-100/70 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-300 dark:hover:bg-amber-500/15"
        title="点击编辑"
        @click="handleEdit"
      >
        <UIcon name="i-lucide-pencil" class="h-3.5 w-3.5" />
        <span>编辑</span>
      </button>
    </template>
  </div>
</template>
