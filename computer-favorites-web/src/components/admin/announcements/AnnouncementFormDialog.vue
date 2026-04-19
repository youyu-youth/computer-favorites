<script setup lang="ts">
import { computed } from 'vue'
import AdminSelect from '@/components/admin/common/AdminSelect.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import USwitch from '@/components/ui-adapter/USwitch.vue'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import type { AdminAnnouncementFormModel, AdminAnnouncementStatus } from '@/types/admin-announcement'
import {
  ADMIN_ANNOUNCEMENT_STATUS_OPTIONS,
  ADMIN_ANNOUNCEMENT_TYPE_OPTIONS,
} from '@/types/admin-announcement'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  form: AdminAnnouncementFormModel
  errors: Record<string, string>
  submitting: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:form', value: Partial<AdminAnnouncementFormModel>): void
  (e: 'submit'): void
}>()

const modalTitle = computed(() => (props.mode === 'create' ? '新建公告' : '编辑公告'))
const modalDesc = computed(() =>
  props.mode === 'create'
    ? '填写公告标题、类型、内容与显示策略，保存后立即加入公告列表。'
    : '更新公告内容与展示状态，保存后列表与详情会同步刷新。',
)

const statusOptions = computed(() => ADMIN_ANNOUNCEMENT_STATUS_OPTIONS.slice(1))
const typeOptions = computed(() => ADMIN_ANNOUNCEMENT_TYPE_OPTIONS.slice(1))

const updateField = (field: keyof AdminAnnouncementFormModel, value: unknown) => {
  emit('update:form', { [field]: value } as Partial<AdminAnnouncementFormModel>)
}

const handleClose = () => {
  if (!props.submitting) {
    emit('update:open', false)
  }
}

const handleStatusChange = (value: string | number | null) => {
  updateField('status', (value ?? 1) as AdminAnnouncementStatus)
}
</script>

<template>
  <Transition name="announcement-modal-fade" appear>
    <div v-if="open" class="fixed inset-0 z-[130] flex items-center justify-center p-4 sm:p-6">
      <div
        class="absolute inset-0 bg-[rgb(15_23_42/0.42)] backdrop-blur-sm dark:bg-[rgb(2_6_23/0.72)]"
        @click="handleClose"
      ></div>

      <section
        class="relative z-[1] flex max-h-[82vh] w-full max-w-[860px] flex-col overflow-hidden rounded-[28px] border border-gray-200 bg-white shadow-[0_28px_60px_rgb(15_23_42/0.24)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_32px_64px_rgb(2_6_23/0.62)]"
      >
        <button
          type="button"
          class="absolute right-4 top-4 inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-xl border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-700 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-slate-800 dark:hover:text-white"
          :disabled="submitting"
          @click="handleClose"
        >
          <i class="fas fa-xmark"></i>
        </button>

        <header class="shrink-0 border-b border-gray-200 bg-gray-50 px-5 py-4 pr-16 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex items-center gap-3">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-2xl border border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-900/40 dark:bg-blue-900/20 dark:text-blue-300"
            >
              <i class="fas fa-bullhorn text-sm"></i>
            </div>
            <div>
              <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ modalTitle }}</h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ modalDesc }}</p>
            </div>
          </div>
        </header>

        <div class="min-h-0 flex-1 overflow-y-auto px-5 py-5">
          <form class="space-y-5" @submit.prevent="emit('submit')">
            <div>
              <label for="announcement-title" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                公告标题 <span class="text-red-500">*</span>
              </label>
              <UInput
                id="announcement-title"
                :modelValue="form.title"
                maxlength="200"
                placeholder="例如：平台更新：管理端公告工作台上线"
                :disabled="submitting"
                @update:modelValue="(value) => updateField('title', value)"
              />
              <div class="mt-1 flex items-center justify-between gap-3">
                <p v-if="errors.title" class="text-xs text-red-500">{{ errors.title }}</p>
                <p v-else class="text-xs text-gray-400 dark:text-gray-500">控制在 200 个字符以内，方便列表和通知场景展示。</p>
                <span class="text-xs text-gray-400 dark:text-gray-500">{{ form.title.length }}/200</span>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">公告类型</label>
                <AdminSelect
                  :modelValue="form.type"
                  :options="typeOptions"
                  placeholder="请选择公告类型"
                  :disabled="submitting"
                  @update:modelValue="(value) => updateField('type', value)"
                />
              </div>

              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">显示状态</label>
                <AdminSelect
                  :modelValue="form.status"
                  :options="statusOptions"
                  placeholder="请选择显示状态"
                  :disabled="submitting"
                  @update:modelValue="handleStatusChange"
                />
              </div>
            </div>

            <div>
              <label for="announcement-content" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                公告内容 <span class="text-red-500">*</span>
              </label>
              <UTextarea
                id="announcement-content"
                :modelValue="form.content"
                :rows="8"
                placeholder="请输入公告正文内容，建议使用完整语句描述更新、通知或修复细节。"
                :disabled="submitting"
                @update:modelValue="(value) => updateField('content', value)"
              />
              <p v-if="errors.content" class="mt-1 text-xs text-red-500">{{ errors.content }}</p>
            </div>

            <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div>
                <label for="announcement-publish-time" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  发布时间
                </label>
                <UInput
                  id="announcement-publish-time"
                  :modelValue="form.publishTime"
                  type="datetime-local"
                  :disabled="submitting"
                  @update:modelValue="(value) => updateField('publishTime', value)"
                />
                <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">可为空，当前阶段仅作为前端展示字段预留。</p>
              </div>

              <div class="rounded-2xl border border-gray-200 bg-gray-50/80 px-4 py-3 dark:border-dark-border dark:bg-dark-bg">
                <div class="flex items-center justify-between gap-4">
                  <div>
                    <p class="text-sm font-medium text-gray-800 dark:text-gray-100">置顶公告</p>
                    <p class="mt-1 text-xs leading-5 text-gray-500 dark:text-gray-400">
                      开启后该公告会在列表中优先展示，并在运营视角中获得更高关注度。
                    </p>
                  </div>
                  <USwitch
                    :modelValue="form.isTop === 1"
                    :disabled="submitting"
                    @update:modelValue="(value) => updateField('isTop', value ? 1 : 0)"
                  />
                </div>
              </div>
            </div>
          </form>
        </div>

        <footer class="shrink-0 border-t border-gray-200 bg-gray-50 px-5 py-4 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <UButton color="neutral" variant="soft" :disabled="submitting" @click="handleClose">取消</UButton>
            <UButton :loading="submitting" @click="emit('submit')">
              <i class="fas fa-floppy-disk text-xs"></i>
              {{ mode === 'create' ? '保存公告' : '保存修改' }}
            </UButton>
          </div>
        </footer>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.announcement-modal-fade-enter-active,
.announcement-modal-fade-leave-active {
  transition: opacity 0.18s ease;
}

.announcement-modal-fade-enter-from,
.announcement-modal-fade-leave-to {
  opacity: 0;
}
</style>
