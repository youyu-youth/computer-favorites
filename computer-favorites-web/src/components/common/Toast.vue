<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { CheckCircle, XCircle, AlertCircle, Info, X } from 'lucide-vue-next'

export interface ToastProps {
  type?: 'success' | 'error' | 'warning' | 'info'
  message: string
  duration?: number
  onClose?: () => void
}

const props = withDefaults(defineProps<ToastProps>(), {
  type: 'info',
  duration: 3000,
})

const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const iconComponent = computed(() => {
  switch (props.type) {
    case 'success':
      return CheckCircle
    case 'error':
      return XCircle
    case 'warning':
      return AlertCircle
    default:
      return Info
  }
})

const colorClasses = computed(() => {
  switch (props.type) {
    case 'success':
      return {
        bg: 'bg-green-50 dark:bg-green-900/20',
        border: 'border-green-200 dark:border-green-700/50',
        icon: 'text-green-600 dark:text-green-400',
        text: 'text-green-800 dark:text-green-200',
      }
    case 'error':
      return {
        bg: 'bg-red-50 dark:bg-red-900/20',
        border: 'border-red-200 dark:border-red-700/50',
        icon: 'text-red-600 dark:text-red-400',
        text: 'text-red-800 dark:text-red-200',
      }
    case 'warning':
      return {
        bg: 'bg-amber-50 dark:bg-amber-900/20',
        border: 'border-amber-200 dark:border-amber-700/50',
        icon: 'text-amber-600 dark:text-amber-400',
        text: 'text-amber-800 dark:text-amber-200',
      }
    default:
      return {
        bg: 'bg-blue-50 dark:bg-blue-900/20',
        border: 'border-blue-200 dark:border-blue-700/50',
        icon: 'text-blue-600 dark:text-blue-400',
        text: 'text-blue-800 dark:text-blue-200',
      }
  }
})

const close = () => {
  visible.value = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
  setTimeout(() => {
    props.onClose?.()
  }, 300)
}

onMounted(() => {
  visible.value = true
  if (props.duration > 0) {
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="opacity-0 translate-y-[-20px]"
    enter-to-class="opacity-100 translate-y-0"
    leave-active-class="transition-all duration-200 ease-in"
    leave-from-class="opacity-100 translate-y-0"
    leave-to-class="opacity-0 translate-y-[-20px]"
  >
    <div
      v-if="visible"
      class="pointer-events-auto flex items-start gap-3 rounded-lg border-2 px-4 py-3 shadow-lg backdrop-blur-sm"
      :class="[colorClasses.bg, colorClasses.border]"
      role="alert"
    >
      <component :is="iconComponent" class="h-5 w-5 flex-shrink-0 mt-0.5" :class="colorClasses.icon" />
      <p class="flex-1 text-sm font-medium leading-relaxed" :class="colorClasses.text">
        {{ message }}
      </p>
      <button
        class="flex-shrink-0 rounded-full p-1 transition-colors hover:bg-black/5 dark:hover:bg-white/5"
        :class="colorClasses.icon"
        @click="close"
      >
        <X class="h-4 w-4" />
      </button>
    </div>
  </Transition>
</template>
