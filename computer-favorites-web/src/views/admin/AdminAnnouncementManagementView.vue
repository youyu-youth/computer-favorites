<script setup lang="ts">
import { onMounted, shallowRef } from 'vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import AnnouncementDetailPanel from '@/components/admin/announcements/AnnouncementDetailPanel.vue'
import AnnouncementFormDialog from '@/components/admin/announcements/AnnouncementFormDialog.vue'
import AnnouncementsBreadcrumbs from '@/components/admin/announcements/AnnouncementsBreadcrumbs.vue'
import AnnouncementsOverviewCards from '@/components/admin/announcements/AnnouncementsOverviewCards.vue'
import AnnouncementsTable from '@/components/admin/announcements/AnnouncementsTable.vue'
import AnnouncementsToolbar from '@/components/admin/announcements/AnnouncementsToolbar.vue'
import { useAdminAnnouncementManagement } from '@/composables/admin/useAdminAnnouncementManagement'
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
  pagedAnnouncements,
  statistics,
  totalItems,
  totalPages,
  visiblePages,
  selectedIds,
  selectedCount,
  detailOpen,
  detailRecord,
  formDialog,
  setKeyword,
  setStatus,
  setType,
  setIsTop,
  refreshData,
  setSelectedIds,
  openDetail,
  closeDetail,
  openCreateDialog,
  openEditDialog,
  closeFormDialog,
  updateForm,
  submitForm,
  toggleAnnouncementStatus,
  toggleAnnouncementTop,
  batchShow,
  batchHide,
  batchCancelTop,
  prevPage,
  nextPage,
  goToPage,
} = useAdminAnnouncementManagement()

onMounted(() => {
  adminNavStore.setActiveMenu('announcements')
  refreshData()
})

const handleRefresh = async () => {
  const result = await refreshData()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.ok ? '公告数据已刷新。' : result.message || '公告数据刷新失败。',
  }
}

const handleSubmitForm = async () => {
  const result = await submitForm()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}

const handleToggleStatus = async (announcementId: number) => {
  const result = await toggleAnnouncementStatus(announcementId)
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}

const handleToggleTop = async (announcementId: number) => {
  const result = await toggleAnnouncementTop(announcementId)
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}

const handleBatchShow = async () => {
  const result = await batchShow()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}

const handleBatchHide = async () => {
  const result = await batchHide()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}

const handleBatchCancelTop = async () => {
  const result = await batchCancelTop()
  feedback.value = {
    tone: result.ok ? 'success' : 'error',
    text: result.message ?? '操作完成。',
  }
}
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <AnnouncementsBreadcrumbs />

      <AnnouncementsOverviewCards :statistics="statistics" />

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

      <AnnouncementsToolbar
        :keyword="query.keyword"
        :status="query.status"
        :type="query.type"
        :isTop="query.isTop"
        :selectedCount="selectedCount"
        @update:keyword="setKeyword"
        @update:status="setStatus"
        @update:type="setType"
        @update:isTop="setIsTop"
        @refresh="handleRefresh"
        @create="openCreateDialog"
        @batch-show="handleBatchShow"
        @batch-hide="handleBatchHide"
        @batch-cancel-top="handleBatchCancelTop"
      />

      <section class="min-w-0">
        <AnnouncementsTable
          :rows="pagedAnnouncements"
          :selectedIds="selectedIds"
          :loading="loading"
          @selection-change="setSelectedIds"
          @view="openDetail"
          @edit="openEditDialog"
          @toggle-status="handleToggleStatus"
          @toggle-top="handleToggleTop"
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
      </section>
    </div>

    <AnnouncementDetailPanel
      :open="detailOpen"
      :announcement="detailRecord"
      @update:open="(value) => { if (!value) closeDetail() }"
      @edit="openEditDialog"
      @toggle-status="handleToggleStatus"
      @toggle-top="handleToggleTop"
    />

    <AnnouncementFormDialog
      :open="formDialog.open"
      :mode="formDialog.mode"
      :form="formDialog.form"
      :errors="formDialog.errors"
      :submitting="formDialog.submitting"
      @update:open="(value) => { if (!value) closeFormDialog() }"
      @update:form="updateForm"
      @submit="handleSubmitForm"
    />
  </main>
</template>
