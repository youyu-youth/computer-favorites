<script setup lang="ts">
import { onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import TagsBreadcrumbs from '@/components/admin/tags/TagsBreadcrumbs.vue'
import TagsStatsCards from '@/components/admin/tags/TagsStatsCards.vue'
import TagsToolbar from '@/components/admin/tags/TagsToolbar.vue'
import TagsTable from '@/components/admin/tags/TagsTable.vue'
import TagEditorModal from '@/components/admin/tags/TagEditorModal.vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import { useTagsData } from '@/composables/admin/useTagsData'
import { useAdminNavStore } from '@/stores/adminNav'
import type {
  AdminTagFormModel,
  AdminTagItem,
  AdminTagSortField,
  AdminTagSortOrder,
} from '@/types/admin-tag'

const adminNavStore = useAdminNavStore()

const {
  searchKeyword,
  currentPage,
  pageSize,
  sortField,
  sortOrder,
  editorOpen,
  editorSubmitting,
  editorMode,
  formModel,
  formErrors,
  deleteDialogOpen,
  deleteSubmitting,
  deletingTag,
  selectedTagIds,
  selectedTagCount,
  batchDeleteSubmitting,
  pagedTags,
  totalItems,
  totalPages,
  visiblePages,
  stats,
  setSearchKeyword,
  setSort,
  openCreateEditor,
  openEditEditor,
  closeEditor,
  saveTag,
  refreshMockData,
  requestDeleteTag,
  closeDeleteDialog,
  confirmDeleteTag,
  setSelectedTagIds,
  batchDeleteSelectedTags,
  prevPage,
  nextPage,
  goToPage,
} = useTagsData()

const updateFormModel = (nextValue: AdminTagFormModel): void => {
  formModel.name = nextValue.name
  formModel.color = nextValue.color
}

const handleSortChange = (payload: {
  field: AdminTagSortField
  order: AdminTagSortOrder
}): void => {
  setSort(payload.field, payload.order)
}

const handleEditTag = (tag: AdminTagItem): void => {
  openEditEditor(tag)
}

const handleDeleteTag = (tag: AdminTagItem): void => {
  requestDeleteTag(tag)
}

const handleSelectionChange = (nextSelectedIds: number[]): void => {
  setSelectedTagIds(nextSelectedIds)
}

const handleDeleteModalOpenChange = (value: boolean): void => {
  if (!value) {
    closeDeleteDialog()
  }
}

const deleteDialogPt = {
  mask: { class: 'bg-black/45 backdrop-blur-[1px] z-[120]' },
  root: {
    class:
      'w-[min(92vw,440px)] rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
  },
  header: {
    class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
  },
  content: {
    class: 'px-5 py-4',
  },
  footer: {
    class:
      'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
  },
} as const

onMounted(() => {
  adminNavStore.setActiveMenu('tags')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <TagsBreadcrumbs />

      <TagsStatsCards :stats="stats" />

      <section class="rounded-xl border border-gray-200 p-4 dark:border-dark-border sm:p-5">
        <TagsToolbar
          :searchKeyword="searchKeyword"
          :selectedCount="selectedTagCount"
          :batchDeleteSubmitting="batchDeleteSubmitting"
          @update:searchKeyword="setSearchKeyword"
          @refresh="refreshMockData"
          @create="openCreateEditor"
          @batch-delete="batchDeleteSelectedTags"
        />

        <TagsTable
          :rows="pagedTags"
          :selectedRowIds="selectedTagIds"
          :sortField="sortField"
          :sortOrder="sortOrder"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @edit="handleEditTag"
          @delete="handleDeleteTag"
        />

        <AdminPagination
          v-if="totalItems > 0"
          :currentPage="currentPage"
          :totalPages="totalPages"
          :visiblePages="visiblePages"
          :total="totalItems"
          :pageSize="pageSize"
          @prev="prevPage"
          @next="nextPage"
          @goto="goToPage"
        />
      </section>
    </div>

    <TagEditorModal
      :open="editorOpen"
      :mode="editorMode"
      :modelValue="formModel"
      :errors="formErrors"
      :submitting="editorSubmitting"
      @update:open="
        (value) => {
          if (!value) closeEditor()
        }
      "
      @update:modelValue="updateFormModel"
      @submit="saveTag"
    />

    <Dialog
      :visible="deleteDialogOpen"
      modal
      :dismissableMask="true"
      :draggable="false"
      :pt="deleteDialogPt"
      @update:visible="handleDeleteModalOpenChange"
    >
      <template #header>
        <div class="space-y-1">
          <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">确认删除标签</h3>
          <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
            删除后该标签将从当前列表隐藏。
          </p>
        </div>
      </template>

      <div
        class="rounded-md border border-red-200 bg-red-50/70 px-3 py-2 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-900/15 dark:text-red-200"
      >
        即将删除标签：
        <span class="font-semibold">{{ deletingTag?.name || '-' }}</span>
      </div>

      <template #footer>
        <UButton
          color="neutral"
          variant="soft"
          :disabled="deleteSubmitting"
          @click="closeDeleteDialog"
        >
          取消
        </UButton>
        <UButton
          color="red"
          :loading="deleteSubmitting"
          :disabled="deleteSubmitting"
          @click="confirmDeleteTag"
        >
          确认删除
        </UButton>
      </template>
    </Dialog>
  </main>
</template>
