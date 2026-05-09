<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = withDefaults(
  defineProps<{
    username?: string
    reason?: 'private' | 'login' | 'notFound' | 'error'
    message?: string
  }>(),
  {
    username: '',
    reason: 'private',
    message: '',
  },
)

const router = useRouter()

const title = computed(() => {
  if (props.reason === 'login') return '请登录查看'
  if (props.reason === 'notFound') return '用户不存在或已被禁用'
  if (props.reason === 'error') return '主页加载失败'
  return '该用户未公开主页'
})

const description = computed(() => {
  if (props.message) return props.message
  if (props.reason === 'login') return '该用户主页设置为仅登录用户可见，登录后即可继续访问。'
  if (props.reason === 'notFound') return '请检查用户名是否正确，或稍后再试。'
  if (props.reason === 'error') return '网络或服务暂时不可用，请稍后重试。'
  return '该用户已将个人主页设为私密，仅本人可见。'
})

const iconName = computed(() => {
  if (props.reason === 'login') return 'i-lucide-log-in'
  if (props.reason === 'notFound') return 'i-lucide-user-x'
  if (props.reason === 'error') return 'i-lucide-circle-alert'
  return 'i-lucide-lock-keyhole'
})

const handleLogin = () => {
  const redirect = props.username ? `/computer/profile/${props.username}` : '/computer/home'
  router.push({ name: 'login', query: { redirect } })
}
</script>

<template>
  <section class="flex min-h-[50vh] items-center justify-center px-4 py-12">
    <div class="w-full max-w-xl rounded-2xl border border-black/5 bg-white p-8 text-center shadow-sm dark:border-white/[0.08] dark:bg-black">
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-amber-500/20 bg-amber-500/10 text-amber-600 dark:text-amber-400">
        <UIcon :name="iconName" class="h-7 w-7" />
      </div>
      <div class="mt-5 space-y-2">
        <h1 class="text-xl font-semibold tracking-tight text-gray-900 dark:text-gray-100">
          {{ title }}
        </h1>
        <p class="text-sm leading-6 text-gray-500 dark:text-gray-400">
          {{ description }}
        </p>
      </div>
      <div class="mt-6 flex flex-col justify-center gap-3 sm:flex-row">
        <UButton v-if="reason === 'login'" type="button" @click="handleLogin">
          去登录
        </UButton>
        <UButton type="button" variant="outline" color="neutral" @click="router.push('/computer/home')">
          返回首页
        </UButton>
      </div>
    </div>
  </section>
</template>
