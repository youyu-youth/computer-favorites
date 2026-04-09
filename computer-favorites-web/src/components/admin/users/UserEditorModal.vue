<script setup lang="ts">
import { computed } from 'vue'
import type { AdminUserFormModel } from '@/types/user'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  modelValue: AdminUserFormModel
  errors: Record<string, string>
  submitting: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'update:modelValue', val: AdminUserFormModel): void
  (e: 'submit'): void
}>()

const isCreate = computed(() => props.mode === 'create')
const modalTitle = computed(() => (isCreate.value ? '新建用户' : '编辑用户'))
const modalDesc = computed(() =>
  isCreate.value ? '创建新用户账号并设置基本信息。' : '修改用户信息，保存后即时生效。',
)

const handleClose = () => {
  if (!props.submitting) {
    emit('update:open', false)
  }
}

const updateField = (field: keyof AdminUserFormModel, value: unknown) => {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}

const handleInput = (
  field: 'username' | 'email' | 'phone' | 'nickname' | 'password',
  event: Event,
): void => {
  const target = event.target as HTMLInputElement
  updateField(field, target.value)
}

const handleStatusChange = (event: Event): void => {
  const target = event.target as HTMLSelectElement
  updateField('status', Number(target.value) as 0 | 1)
}
</script>

<template>
  <Transition name="cf-user-modal-fade" appear>
    <div
      v-if="open"
      class="fixed inset-0 z-[130] flex items-center justify-center p-4 sm:p-6"
      @click.self="handleClose"
    >
      <div class="absolute inset-0 bg-black/45 backdrop-blur-[1px]" @click="handleClose"></div>

      <section
        class="relative z-[1] w-full max-w-[560px] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_22px_48px_rgba(15,23,42,0.3)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_30px_58px_rgba(2,6,23,0.7)]"
      >
        <!-- 关闭按钮 -->
        <button
          type="button"
          class="absolute right-4 top-4 inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-gray-100"
          :disabled="submitting"
          @click="handleClose"
        >
          <i class="fas fa-times text-sm"></i>
        </button>

        <!-- 头部 -->
        <header
          class="border-b border-gray-200 bg-gray-50 px-5 py-4 pr-16 dark:border-dark-border dark:bg-dark-bg"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-9 w-9 items-center justify-center rounded-lg border border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-900/50 dark:bg-blue-900/20 dark:text-blue-300"
            >
              <i class="fas fa-user-edit text-sm"></i>
            </div>
            <div>
              <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">
                {{ modalTitle }}
              </h3>
              <p class="mt-0.5 text-xs text-gray-500 dark:text-gray-400">{{ modalDesc }}</p>
            </div>
          </div>
        </header>

        <!-- 表单 -->
        <form class="space-y-4 px-5 py-5" @submit.prevent="emit('submit')">
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <!-- 用户名 -->
            <div>
              <label
                for="user-username"
                class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                用户名 <span class="text-red-500">*</span>
              </label>
              <input
                id="user-username"
                :value="modelValue.username"
                type="text"
                placeholder="例如：zhangsan"
                class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
                :class="errors.username ? 'border-red-400 focus:border-red-500 focus:ring-red-500/20' : ''"
                :disabled="submitting || !isCreate"
                @input="(e) => handleInput('username', e)"
              />
              <p v-if="errors.username" class="mt-1 text-xs text-red-500">{{ errors.username }}</p>
              <p v-if="!isCreate" class="mt-1 text-xs text-gray-400 dark:text-gray-500">
                用户名不可修改
              </p>
            </div>

            <!-- 昵称 -->
            <div>
              <label
                for="user-nickname"
                class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                昵称
              </label>
              <input
                id="user-nickname"
                :value="modelValue.nickname"
                type="text"
                placeholder="例如：张三"
                class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
                :disabled="submitting"
                @input="(e) => handleInput('nickname', e)"
              />
            </div>
          </div>

          <!-- 邮箱 -->
          <div>
            <label
              for="user-email"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              邮箱 <span class="text-red-500">*</span>
            </label>
            <input
              id="user-email"
              :value="modelValue.email"
              type="email"
              placeholder="例如：user@example.com"
              class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
              :class="errors.email ? 'border-red-400 focus:border-red-500 focus:ring-red-500/20' : ''"
              :disabled="submitting"
              @input="(e) => handleInput('email', e)"
            />
            <p v-if="errors.email" class="mt-1 text-xs text-red-500">{{ errors.email }}</p>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <!-- 手机号 -->
            <div>
              <label
                for="user-phone"
                class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                手机号
              </label>
              <input
                id="user-phone"
                :value="modelValue.phone"
                type="text"
                placeholder="例如：13800138000"
                class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
                :disabled="submitting"
                @input="(e) => handleInput('phone', e)"
              />
            </div>

            <!-- 状态 -->
            <div>
              <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                账号状态
              </label>
              <div class="relative">
                <select
                  :value="String(modelValue.status)"
                  class="cursor-pointer h-10 w-full appearance-none rounded-md border border-gray-300 bg-white px-3 pr-10 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:focus:border-blue-400"
                  :disabled="submitting"
                  @change="handleStatusChange"
                >
                  <option value="1">正常</option>
                  <option value="0">禁用</option>
                </select>
                <i
                  class="fas fa-chevron-down pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
                ></i>
              </div>
            </div>
          </div>

          <!-- 密码（仅新建时显示） -->
          <div v-if="isCreate">
            <label
              for="user-password"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              初始密码 <span class="text-red-500">*</span>
            </label>
            <input
              id="user-password"
              :value="modelValue.password"
              type="password"
              placeholder="至少8位字符"
              class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
              :class="errors.password ? 'border-red-400 focus:border-red-500 focus:ring-red-500/20' : ''"
              :disabled="submitting"
              @input="(e) => handleInput('password', e)"
            />
            <p v-if="errors.password" class="mt-1 text-xs text-red-500">{{ errors.password }}</p>
          </div>

          <!-- 底部按钮 -->
          <footer
            class="flex flex-col-reverse gap-2 border-t border-gray-200 pt-4 dark:border-dark-border sm:flex-row sm:justify-end"
          >
            <button
              type="button"
              class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
              :disabled="submitting"
              @click="handleClose"
            >
              取消
            </button>
            <button
              type="submit"
              class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-blue-500 dark:hover:bg-blue-600 dark:focus:ring-offset-gray-900"
              :disabled="submitting"
            >
              <i v-if="submitting" class="fas fa-spinner fa-spin mr-2"></i>
              <span>{{ isCreate ? '确认创建' : '保存修改' }}</span>
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.cf-user-modal-fade-enter-active,
.cf-user-modal-fade-leave-active {
  transition: opacity 0.18s ease;
}

.cf-user-modal-fade-enter-from,
.cf-user-modal-fade-leave-to {
  opacity: 0;
}
</style>
