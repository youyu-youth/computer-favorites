<script setup lang="ts">
import { computed, onMounted, shallowRef } from 'vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import CommentDeleteDialog from '@/components/admin/comments/CommentDeleteDialog.vue'
import CommentDetailPanel from '@/components/admin/comments/CommentDetailPanel.vue'
import CommentsBreadcrumbs from '@/components/admin/comments/CommentsBreadcrumbs.vue'
import CommentsOverviewCards from '@/components/admin/comments/CommentsOverviewCards.vue'
import CommentsTable from '@/components/admin/comments/CommentsTable.vue'
import CommentsToolbar from '@/components/admin/comments/CommentsToolbar.vue'
import { useAdminCommentManagement } from '@/composables/admin/useAdminCommentManagement'
import { useAdminNavStore } from '@/stores/adminNav'
import type { AdminCommentAction } from '@/types/admin-comment'

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
  pagedComments,
  totalItems,
  totalPages,
  visiblePages,
  detailOpen,
  detailRecord,
  handleDialog,
  selectedIds,
  selectedCount,
  statistics,
  statusSummaryText,
  setKeyword,
  setStatus,
  refreshData,
  prevPage,
  nextPage,
  goToPage,
  openDetail,
  closeDetail,
  setSelectedIds,
  clearSelection,
  openSingleHandleDialog,
  openBatchHandleDialog,
  closeHandleDialog,
  updateHandleForm,
  submitHandleAction,
} = useAdminCommentManagement()

const handleDialogForm = computed(() => ({
  action: handleDialog.action,
  reason: handleDialog.reason,
}))

const handleRefresh = async () => {
  const result = await refreshData()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.ok ? '评论数据已刷新。' : result.message || '评论数据刷新失败。',
  }
}

const handleView = (commentId: number) => {
  openDetail(commentId)
}

const handleDelete = (commentId: number) => {
  openSingleHandleDialog(commentId, 'hide')
}

const handleRestore = (commentId: number) => {
  openSingleHandleDialog(commentId, 'show')
}

const handleDetailDelete = (commentId: number) => {
  closeDetail()
  openSingleHandleDialog(commentId, 'hide')
}

const handleDetailRestore = (commentId: number) => {
  closeDetail()
  openSingleHandleDialog(commentId, 'show')
}

const handleBatchDelete = () => {
  openBatchHandleDialog('hide')
}

const handleBatchRestore = () => {
  openBatchHandleDialog('show')
}

const handleSubmitAction = async () => {
  const result = await submitHandleAction()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message || (result.ok ? '操作成功。' : '操作失败。'),
  }
}

onMounted(() => {
  adminNavStore.setActiveMenu('comments')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <CommentsBreadcrumbs />

      <CommentsOverviewCards :statistics="statistics" :statusSummaryText="statusSummaryText" />

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

      <CommentsToolbar
        :keyword="query.keyword"
        :status="query.status"
        :selectedCount="selectedCount"
        @update:keyword="setKeyword"
        @update:status="setStatus"
        @batch-delete="handleBatchDelete"
        @batch-restore="handleBatchRestore"
        @clear-selection="clearSelection"
        @refresh="handleRefresh"
      />

      <CommentsTable
        :rows="pagedComments"
        :loading="loading"
        :selectedIds="selectedIds"
        @view="handleView"
        @delete="handleDelete"
        @restore="handleRestore"
        @selection-change="setSelectedIds"
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

    <CommentDetailPanel
      :open="detailOpen"
      :loading="detailLoading"
      :comment="detailRecord"
      @update:open="(value) => { if (!value) closeDetail() }"
      @delete="handleDetailDelete"
      @restore="handleDetailRestore"
    />

    <CommentDeleteDialog
      :open="handleDialog.open"
      :form="handleDialogForm"
      :isBatch="handleDialog.isBatch"
      :batchCount="handleDialog.batchIds.length"
      @update:open="(value) => { if (!value) closeHandleDialog() }"
      @update:form="updateHandleForm"
      @submit="handleSubmitAction"
    />
  </main>
</template>
