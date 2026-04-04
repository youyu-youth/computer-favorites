<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import { sendEmailUpdateCode } from '@/api/user'

interface Props {
  open: boolean
  currentEmail?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentEmail: '',
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', payload: { email: string; emailCode: string }): void
}>()

const toast = useToast()
const email = ref('')
const emailCode = ref('')
const localError = ref('')
const sendCodeLoading = ref(false)
const sendCodeCountdown = ref(0)
let sendCodeTimer: number | null = null
let previousScrollY = 0

const isValidEmail = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)

// 停止验证码倒计时，避免组件销毁后残留定时器
const stopSendCodeTimer = () => {
  if (sendCodeTimer === null) {
    return
  }
  window.clearInterval(sendCodeTimer)
  sendCodeTimer = null
}

// 启动验证码倒计时，防止短时间重复发送
const startSendCodeCountdown = () => {
  stopSendCodeTimer()
  sendCodeCountdown.value = 60
  sendCodeTimer = window.setInterval(() => {
    if (sendCodeCountdown.value <= 1) {
      sendCodeCountdown.value = 0
      stopSendCodeTimer()
      return
    }
    sendCodeCountdown.value -= 1
  }, 1000)
}

const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('email-modal-scroll-lock')
  document.body.classList.add('email-modal-scroll-lock')
  document.body.style.setProperty('--email-modal-scroll-y', scrollValue)
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.documentElement.classList.remove('email-modal-scroll-lock')
  document.body.classList.remove('email-modal-scroll-lock')
  document.body.style.removeProperty('--email-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const closeDialog = () => {
  if (props.loading) {
    return
  }
  emit('update:open', false)
}

const validateForm = (): string => {
  const normalizedEmail = email.value.trim()
  const normalizedEmailCode = emailCode.value.trim()

  if (!normalizedEmail) {
    return '请输入新邮箱'
  }
  if (!isValidEmail(normalizedEmail)) {
    return '请输入有效的邮箱地址'
  }
  if (normalizedEmail === props.currentEmail) {
    return '新邮箱不能与当前邮箱相同'
  }
  if (!normalizedEmailCode) {
    return '请输入邮箱验证码'
  }
  if (!/^\d{6}$/.test(normalizedEmailCode)) {
    return '验证码必须为6位数字'
  }
  return ''
}

const handleSendCode = async () => {
  if (sendCodeLoading.value || sendCodeCountdown.value > 0 || props.loading) {
    return
  }

  const normalizedEmail = email.value.trim()
  if (!normalizedEmail) {
    localError.value = '请先输入新邮箱'
    return
  }
  if (!isValidEmail(normalizedEmail)) {
    localError.value = '请输入有效的邮箱地址'
    return
  }
  if (normalizedEmail === props.currentEmail) {
    localError.value = '新邮箱不能与当前邮箱相同'
    return
  }

  localError.value = ''
  sendCodeLoading.value = true
  try {
    await sendEmailUpdateCode({ email: normalizedEmail })
    startSendCodeCountdown()
    toast.add({
      title: '发送成功',
      description: '验证码已发送到新邮箱，请注意查收',
      type: 'success',
    })
  } catch (error) {
    localError.value = error instanceof Error ? error.message : '验证码发送失败，请稍后重试'
  } finally {
    sendCodeLoading.value = false
  }
}

const submitForm = () => {
  const validationError = validateForm()
  if (validationError) {
    localError.value = validationError
    return
  }
  localError.value = ''
  emit('submit', {
    email: email.value.trim(),
    emailCode: emailCode.value.trim(),
  })
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      email.value = ''
      emailCode.value = ''
      localError.value = ''
      lockPageScroll()
      return
    }
    stopSendCodeTimer()
    sendCodeCountdown.value = 0
    unlockPageScroll()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  stopSendCodeTimer()
  unlockPageScroll()
})

defineOptions({
  name: 'EmailEditDialog',
})
</script>

<template>
  <UModal
    :open="open"
    :portal="true"
    title="修改邮箱"
    description="请输入新邮箱并完成验证码校验"
    :ui="{
      overlay: 'email-edit-modal-overlay z-[120]',
      content: 'email-edit-modal-content z-[130]',
      header: 'email-edit-modal-header',
      title: 'email-edit-modal-title',
      description: 'email-edit-modal-description',
      close: 'email-edit-modal-close cursor-pointer',
      body: 'email-edit-modal-body',
      footer: 'email-edit-modal-footer',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="space-y-4">
        <UFormField label="当前邮箱">
          <UInput :model-value="currentEmail || '未绑定邮箱'" readonly class="w-full" />
        </UFormField>

        <UFormField label="新邮箱" required>
          <UInput
            v-model="email"
            type="email"
            autocomplete="email"
            placeholder="请输入新的邮箱地址"
            class="w-full"
          />
        </UFormField>

        <UFormField label="邮箱验证码" required>
          <div class="flex flex-col gap-2 sm:flex-row">
            <UInput
              v-model="emailCode"
              maxlength="6"
              placeholder="请输入6位验证码"
              class="w-full"
            />
            <UButton
              type="button"
              variant="soft"
              color="neutral"
              class="h-10 shrink-0 cursor-pointer"
              :loading="sendCodeLoading"
              :disabled="sendCodeLoading || sendCodeCountdown > 0 || loading"
              @click="handleSendCode"
            >
              <span v-if="sendCodeLoading">发送中...</span>
              <span v-else-if="sendCodeCountdown > 0">{{ sendCodeCountdown }}s</span>
              <span v-else>发送验证码</span>
            </UButton>
          </div>
        </UFormField>

        <p v-if="localError" class="text-xs text-red-500">
          {{ localError }}
        </p>
      </div>
    </template>

    <template #footer>
      <div class="email-edit-modal-actions flex w-full justify-end gap-2">
        <UButton
          color="neutral"
          variant="soft"
          class="cursor-pointer"
          :disabled="loading"
          @click="closeDialog"
        >
          取消
        </UButton>
        <UButton
          color="primary"
          variant="solid"
          class="cursor-pointer !bg-[#d97706] text-white hover:!bg-[#b45309] active:!bg-[#92400e] disabled:opacity-70"
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
