<script setup lang="ts">
import { computed, onMounted, shallowRef } from 'vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import ReportDetailPanel from '@/components/admin/reports/ReportDetailPanel.vue'
import ReportHandleDialog from '@/components/admin/reports/ReportHandleDialog.vue'
import ReportsBreadcrumbs from '@/components/admin/reports/ReportsBreadcrumbs.vue'
import ReportsOverviewCards from '@/components/admin/reports/ReportsOverviewCards.vue'
import ReportsTable from '@/components/admin/reports/ReportsTable.vue'
import ReportsToolbar from '@/components/admin/reports/ReportsToolbar.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import { useAdminReportManagementMock } from '@/composables/admin/useAdminReportManagementMock'
import { useAdminNavStore } from '@/stores/adminNav'

type FeedbackState = {
  tone: 'success' | 'error'
  text: string
}

const adminNavStore = useAdminNavStore()
const feedback = shallowRef<FeedbackState | null>(null)

const {
  loading,
  query,
  pagedReports,
  totalItems,
  totalPages,
  visiblePages,
  selectedIds,
  selectedCount,
  pendingSelectionOnly,
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
  setPendingOnly,
  refreshData,
  prevPage,
  nextPage,
  goToPage,
  setSelectedIds,
  clearSelection,
  openDetail,
  closeDetail,
  openImagePreview,
  closeImagePreview,
  openSingleHandleDialog,
  openBatchHandleDialog,
  closeHandleDialog,
  updateHandleForm,
  submitHandleAction,
} = useAdminReportManagementMock()

const handleDialogForm = computed(() => ({
  action: handleDialog.action,
  handleResult: handleDialog.handleResult,
  executeAction: handleDialog.executeAction,
}))

const handleRefresh = () => {
  refreshData()
  feedback.value = {
    tone: 'success',
    text: '本地 mock 数据已重新排序刷新。',
  }
}

const handleSubmitAction = () => {
  const result = submitHandleAction()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message,
  }
}

onMounted(() => {
  adminNavStore.setActiveMenu('reports')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <ReportsBreadcrumbs />

      <ReportsOverviewCards :statistics="statistics" :statusSummaryText="statusSummaryText" />

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

      <ReportsToolbar
        :keyword="query.keyword"
        :status="query.status"
        :type="query.type"
        :pendingOnly="query.pendingOnly"
        :selectedCount="selectedCount"
        :pendingSelectionOnly="pendingSelectionOnly"
        @update:keyword="setKeyword"
        @update:status="setStatus"
        @update:type="setType"
        @update:pendingOnly="setPendingOnly"
        @refresh="handleRefresh"
        @batch-pass="openBatchHandleDialog('pass')"
        @batch-reject="openBatchHandleDialog('reject')"
        @clear-selection="clearSelection"
      />

      <ReportsTable
        :rows="pagedReports"
        :selectedIds="selectedIds"
        :loading="loading"
        @selection-change="setSelectedIds"
        @view="openDetail"
        @pass="openSingleHandleDialog($event, 'pass')"
        @reject="openSingleHandleDialog($event, 'reject')"
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

    <ReportDetailPanel
      :open="detailOpen"
      :report="detailRecord"
      @update:open="(value) => { if (!value) closeDetail() }"
      @preview-image="(payload) => openImagePreview(payload.url, payload.title)"
      @pass="openSingleHandleDialog($event, 'pass')"
      @reject="openSingleHandleDialog($event, 'reject')"
    />

    <ReportHandleDialog
      :open="handleDialog.open"
      :mode="handleDialog.mode"
      :selectedCount="handleDialog.reportIds.length"
      :form="handleDialogForm"
      @update:open="(value) => { if (!value) closeHandleDialog() }"
      @update:form="updateHandleForm"
      @submit="handleSubmitAction"
    />

    <UModal
      :open="imagePreviewOpen"
      :title="imagePreviewTitle"
      description="截图预览仅作为 mock 证据展示，不代表真实接口返回。"
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
