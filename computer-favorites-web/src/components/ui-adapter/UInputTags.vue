<script setup lang="ts">
import { computed, ref, useAttrs } from 'vue'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import Button from 'primevue/button'

interface UiConfig {
  base?: string
  input?: string
  item?: string
  itemDelete?: string
  itemDeleteIcon?: string
}

const props = withDefaults(
  defineProps<{
    modelValue?: string[]
    placeholder?: string
    ui?: UiConfig
  }>(),
  {
    modelValue: () => [],
    placeholder: '',
    ui: () => ({}),
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const attrs = useAttrs()
const pending = ref('')

const tags = computed(() => props.modelValue ?? [])

const addPendingTag = () => {
  const value = pending.value.trim()
  if (!value) {
    return
  }
  if (tags.value.includes(value)) {
    pending.value = ''
    return
  }
  emit('update:modelValue', [...tags.value, value])
  pending.value = ''
}

const removeTag = (tag: string) => {
  emit(
    'update:modelValue',
    tags.value.filter((item) => item !== tag),
  )
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    addPendingTag()
    return
  }
  if (event.key === 'Backspace' && !pending.value && tags.value.length > 0) {
    emit('update:modelValue', tags.value.slice(0, -1))
  }
}
</script>

<template>
  <div :class="attrs.class">
    <div
      data-slot="base"
      :class="[
        'flex min-h-10 w-full flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 dark:border-white/10 dark:bg-[#000000]',
        ui.base,
      ]"
    >
      <Tag
        v-for="tag in tags"
        :key="tag"
        :value="tag"
        :class="[
          'inline-flex items-center gap-1 rounded-md bg-slate-100 text-slate-800 dark:bg-white/15 dark:text-slate-100',
          ui.item,
        ]"
      >
        <span>{{ tag }}</span>
        <Button
          icon="pi pi-times"
          text
          rounded
          size="small"
          :class="['h-4 w-4 cursor-pointer !p-0', ui.itemDelete, ui.itemDeleteIcon]"
          @click="removeTag(tag)"
        />
      </Tag>
      <InputText
        v-model="pending"
        :placeholder="placeholder"
        :class="[
          'min-w-[8rem] flex-1 border-0 bg-transparent px-0 py-0 text-sm text-slate-900 shadow-none outline-none focus:ring-0 dark:text-white',
          ui.input,
        ]"
        @keydown="handleKeydown"
        @blur="addPendingTag"
      />
    </div>
  </div>
</template>
