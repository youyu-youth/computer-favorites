<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  open: boolean
  modelValue: string
  title?: string
  description?: string
}>(), {
  title: '新增兴趣爱好',
  description: '输入一个兴趣，提交后会自动追加到标签列表。'
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:modelValue', value: string): void
  (e: 'cancel'): void
  (e: 'submit'): void
}>()

const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
})

const closeDialog = () => {
  emit('update:open', false)
  emit('cancel')
}

const submitDialog = () => {
  emit('submit')
}
</script>

<template>
  <Transition name="dialog-fade">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/45 px-4"
      @click.self="closeDialog"
      @keydown.esc.prevent.stop="closeDialog"
    >
      <div class="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-5 shadow-xl dark:border-white/10 dark:bg-black">
        <div class="space-y-1">
          <h4 class="text-base font-semibold text-slate-900 dark:text-white">{{ title }}</h4>
          <p class="text-sm text-slate-500 dark:text-slate-400">{{ description }}</p>
        </div>
        <div class="mt-4">
          <UInput
            v-model="inputValue"
            placeholder="例如：摄影"
            autofocus
            class="w-full [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white"
            @keyup.enter="submitDialog"
          />
        </div>
        <div class="mt-5 flex items-center justify-end gap-2">
          <UButton color="neutral" variant="outline" @click="closeDialog">取消</UButton>
          <UButton color="primary" @click="submitDialog">提交</UButton>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>
