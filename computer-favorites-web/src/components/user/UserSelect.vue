<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  modelValue: any
  options: any[]
  optionLabel: string
  optionValue: string
  placeholder?: string
  minWidth?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void
}>()

const isOpen = ref(false)
const selectRef = ref<HTMLElement | null>(null)

const toggle = () => {
  isOpen.value = !isOpen.value
}

const selectOption = (option: any) => {
  emit('update:modelValue', option[props.optionValue])
  isOpen.value = false
}

const selectedOption = computed(() => {
  return props.options.find(opt => opt[props.optionValue] === props.modelValue)
})

const handleClickOutside = (event: MouseEvent) => {
  if (selectRef.value && !selectRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="selectRef" class="relative" :style="{ minWidth: minWidth || '120px' }">
    <div
      @click="toggle"
      class="h-[38px] w-full bg-white dark:bg-[#111111] border border-gray-200 dark:border-zinc-800 rounded-lg hover:border-gray-300 dark:hover:border-zinc-600 transition-all shadow-sm flex items-center justify-between cursor-pointer group px-2 text-sm focus:outline-none focus:ring-1 focus:ring-emerald-500 focus:border-emerald-500"
      tabindex="0"
      @keydown.enter="toggle"
      @keydown.space.prevent="toggle"
    >
      <span class="text-gray-700 dark:text-zinc-300 cursor-pointer truncate">
        {{ selectedOption ? selectedOption[optionLabel] : placeholder }}
      </span>
      <i class="pi pi-chevron-down text-[10px] text-gray-400 group-hover:text-gray-600 dark:group-hover:text-zinc-400 transition-colors w-6 text-center"></i>
    </div>

    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <div
        v-if="isOpen"
        class="absolute z-50 w-full mt-1.5 bg-white dark:bg-[#111111] border border-gray-200 dark:border-zinc-800 rounded-lg shadow-xl overflow-hidden py-1.5 max-h-60 overflow-y-auto"
      >
        <div
          v-for="option in options"
          :key="option[optionValue]"
          @click="selectOption(option)"
          class="px-3.5 py-2 text-sm cursor-pointer transition-colors"
          :class="[
            option[optionValue] === modelValue
              ? 'bg-gray-50 dark:bg-zinc-800/80 text-emerald-600 dark:text-emerald-400 font-bold'
              : 'text-gray-700 dark:text-zinc-300 hover:bg-gray-50 dark:hover:bg-zinc-800'
          ]"
        >
          {{ option[optionLabel] }}
        </div>
      </div>
    </Transition>
  </div>
</template>
