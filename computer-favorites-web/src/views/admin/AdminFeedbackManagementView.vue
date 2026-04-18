<script setup lang="ts">
import { computed, onMounted, shallowRef } from 'vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import FeedbackDetailPanel from '@/components/admin/feedbacks/FeedbackDetailPanel.vue'
import FeedbackHandleDialog from '@/components/admin/feedbacks/FeedbackHandleDialog.vue'
import FeedbacksBreadcrumbs from '@/components/admin/feedbacks/FeedbacksBreadcrumbs.vue'
import FeedbacksOverviewCards from '@/components/admin/feedbacks/FeedbacksOverviewCards.vue'
import FeedbacksTable from '@/components/admin/feedbacks/FeedbacksTable.vue'
import FeedbacksToolbar from '@/components/admin/feedbacks/FeedbacksToolbar.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import { useAdminFeedbackManagement } from '@/composables/admin/useAdminFeedbackManagement'
import { useAdminNavStore } from '@/stores/adminNav'

type FeedbackState = {
  tone: 'success' | 'error'
  text: string
}

const adminNavStore = useAdminNavStore()
const feedback = shallowRef<FeedbackState | null>(null)

const {
  loading,
  detailLoading,
  query,
  pagedFeedbacks,
  totalItems,
  totalPages,
  visiblePages,
  detailOpen,
  detailRecord,
  imagePreviewOpen,
  imagePreviewUrl,
  imagePreviewTitle,
  handleDialog,
  statistics,
  statusSummaryText,
  setKeyword,
  setStatus,
  setType,
  setHasImages,
  setHasContact,
  refreshData,
  prevPage,
  nextPage,
  goToPage,
  openDetail,
  closeDetail,
  openImagePreview,
  closeImagePreview,
  openReplyDialog,
  openCloseDialog,
  closeHandleDialog,
  updateHandleForm,
  submitHandleAction,
} = useAdminFeedbackManagement()

const handleDialogForm = computed(() => ({
  action: handleDialog.action,
  reply: handleDialog.reply,
}))

const handleRefresh = async () => {
  const result = await refreshData()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.ok ? '反馈数据已刷新。' : result.message || '反馈数据刷新失败。',
  }
}

const handleSubmitAction = async () => {
  const result = await submitHandleAction()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message,
  }
}

onMounted(() => {
  adminNavStore.setActiveMenu('feedbacks')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <FeedbacksBreadcrumbs />

      <FeedbacksOverviewCards :statistics="statistics" :statusSummaryText="statusSummaryText" />

      <div
        v-if="feedback"
        class="mb-5 flex items-start gap-3 rounded-2xl border px-4 py-3 text-sm"
        :class="
          feedback.tone === 'success'
            ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-500/10 dark:text-emerald-200'
            : 'border-red-200 bg-red-50 text-red-700 dark:border-red-400/20 dark:bg-red-500/10 dark:text-red-200'
        "
      >
        <i :class="feedback.tone === 'success' ? 'fas fa-circle-check mt-0.5' : 'fas fa-circle-exclamation mt-0.5'"></i>
        <div class="flex-1">{{ feedback.text }}</div>
        <button
          type="button"
          class="cursor-pointer text-current/70 transition-colors hover:text-current"
          @click="feedback = null"
        >
          <i class="fas fa-xmark"></i>
        </button>
      </div>

      <FeedbacksToolbar
        :keyword="query.keyword"
        :status="query.status"
        :type="query.type"
        :hasImages="query.hasImages"
        :hasContact="query.hasContact"
        @update:keyword="setKeyword"
        @update:status="setStatus"
        @update:type="setType"
        @update:hasImages="setHasImages"
        @update:hasContact="setHasContact"
        @refresh="handleRefresh"
      />

      <FeedbacksTable
        :rows="pagedFeedbacks"
        :loading="loading"
        @view="openDetail"
        @reply="openReplyDialog"
        @close="openCloseDialog"
      />

      <AdminPagination
        v-if="totalItems > 0"
        :currentPage="query.pageNum"
        :totalPages="totalPages"
        :visiblePages="visiblePages"
        :total="totalItems"
        :pageSize="query.pageSize"
        @prev="prevPage"
        @next="nextPage"
        @goto="goToPage"
      />
    </div>

    <FeedbackDetailPanel
      :open="detailOpen"
      :loading="detailLoading"
      :feedback="detailRecord"
      @update:open="(value) => { if (!value) closeDetail() }"
      @preview-image="(payload) => openImagePreview(payload.url, payload.title)"
      @reply="openReplyDialog"
      @close="openCloseDialog"
    />

    <FeedbackHandleDialog
      :open="handleDialog.open"
      :form="handleDialogForm"
      @update:open="(value) => { if (!value) closeHandleDialog() }"
      @update:form="updateHandleForm"
      @submit="handleSubmitAction"
    />

    <UModal
      :open="imagePreviewOpen"
      :title="imagePreviewTitle"
      description="截图预览用于辅助核验反馈场景与页面异常。"
      :ui="{
        overlay: 'bg-black/55 backdrop-blur-sm z-[130]',
        content:
          'w-[min(96vw,980px)] rounded-[24px] border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card overflow-hidden shadow-[0_28px_60px_rgba(15,23,42,0.35)] dark:shadow-[0_32px_64px_rgba(2,6,23,0.7)]',
        header:
          'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
        title: 'text-base font-semibold text-gray-950 dark:text-white',
        description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
        body: 'px-5 py-5',
        footer: 'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex justify-end',
      }"
      @update:open="(value) => { if (!value) closeImagePreview() }"
    >
      <template #body>
        <div class="overflow-hidden rounded-2xl border border-gray-200 bg-gray-100 dark:border-dark-border dark:bg-dark-bg">
          <img
            :src="imagePreviewUrl"
            :alt="imagePreviewTitle"
            class="max-h-[72vh] w-full object-contain"
          />
        </div>
      </template>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-10 cursor-pointer items-center justify-center rounded-lg border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-200 dark:hover:bg-dark-bg"
          @click="closeImagePreview"
        >
          关闭预览
        </button>
      </template>
    </UModal>
  </main>
</template>
