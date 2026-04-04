<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

interface Props {
  open: boolean
  currentUsername?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentUsername: '',
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', value: string): void
}>()

const username = ref('')
const localError = ref('')

const usernameLength = computed(() => username.value.trim().length)

// 记录页面原始滚动位置，确保关闭弹窗后还原
let previousScrollY = 0

const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('username-modal-scroll-lock')
  document.body.classList.add('username-modal-scroll-lock')
  document.body.style.setProperty('--username-modal-scroll-y', scrollValue)
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.documentElement.classList.remove('username-modal-scroll-lock')
  document.body.classList.remove('username-modal-scroll-lock')
  document.body.style.removeProperty('--username-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const closeDialog = () => {
  emit('update:open', false)
}

const submitForm = () => {
  const normalizedUsername = username.value.trim()
  if (normalizedUsername.length < 1 || normalizedUsername.length > 120) {
    localError.value = '用户名长度需在1-120之间'
    return
  }
  localError.value = ''
  emit('submit', normalizedUsername)
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      username.value = props.currentUsername || ''
      localError.value = ''
      lockPageScroll()
      return
    }
    unlockPageScroll()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unlockPageScroll()
})

defineOptions({
  name: 'UsernameEditDialog',
})
</script>

<template>
  <UModal
    :open="open"
    :portal="true"
    title="修改用户名"
    description="每个自然月仅允许修改一次"
    :ui="{
      overlay: 'username-edit-modal-overlay z-[120]',
      content: 'username-edit-modal-content z-[130]',
      header: 'username-edit-modal-header',
      title: 'username-edit-modal-title',
      description: 'username-edit-modal-description',
      close: 'username-edit-modal-close cursor-pointer',
      body: 'username-edit-modal-body',
      footer: 'username-edit-modal-footer',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="space-y-3">
        <UFormField label="新用户名" required>
          <UInput v-model="username" placeholder="请输入新用户名" :maxlength="120" class="w-full" />
        </UFormField>
        <p class="text-xs text-amber-700 dark:text-amber-400">长度 {{ usernameLength }}/120</p>
        <p v-if="localError" class="text-xs text-red-500">{{ localError }}</p>
      </div>
    </template>

    <template #footer>
      <div class="username-edit-modal-actions flex w-full justify-end gap-2">
        <UButton
          color="neutral"
          variant="soft"
          class="cursor-pointer justify-center rounded-lg border border-gray-200 px-4 py-2 font-medium hover:bg-gray-100 dark:border-gray-700 dark:hover:bg-gray-800"
          :disabled="loading"
          @click="closeDialog"
        >
          取消
        </UButton>
        <UButton
          color="primary"
          variant="solid"
          class="cursor-pointer justify-center rounded-lg px-4 py-2 font-semibold text-white !bg-[#d97706] hover:!bg-[#b45309] active:!bg-[#92400e] focus-visible:!ring-2 focus-visible:!ring-[#f59e0b]/70 disabled:opacity-70"
          :loading="loading"
          :disabled="loading"
          @click="submitForm"
        >
          确认修改
        </UButton>
      </div>
    </template>
  </UModal>
</template>
