<script setup lang="ts">
import { onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import CategoriesBreadcrumbs from '@/components/admin/categories/CategoriesBreadcrumbs.vue'
import CategoriesToolbar from '@/components/admin/categories/CategoriesToolbar.vue'
import CategoriesTable from '@/components/admin/categories/CategoriesTable.vue'
import CategoryEditorModal from '@/components/admin/categories/CategoryEditorModal.vue'
import { useCategories } from '@/composables/admin/useCategories'
import { useAdminNavStore } from '@/stores/adminNav'
import type { AdminCategoryFormModel, AdminCategoryItem, AdminCategorySortField, AdminCategorySortOrder } from '@/types/category'

const adminNavStore = useAdminNavStore()

const {
  categories,
  searchKeyword,
  sortField,
  sortOrder,
  editorOpen,
  editorMode,
  editorSubmitting,
  formModel,
  formErrors,
  deleteDialogOpen,
  deleteSubmitting,
  deletingCategory,
  selectedCategoryIds,
  stats,
  allCategories,

  setSearchKeyword,
  setSort,
  openCreateEditor,
  openEditEditor,
  closeEditor,
  saveCategory,
  requestDeleteCategory,
  closeDeleteDialog,
  confirmDeleteCategory,
  setSelectedCategoryIds,
} = useCategories()

const updateFormModel = (nextValue: AdminCategoryFormModel): void => {
  Object.assign(formModel, nextValue)
}

const handleSortChange = (payload: { field: AdminCategorySortField; order: AdminCategorySortOrder }): void => {
  setSort(payload.field, payload.order)
}

const handleEditCategory = (category: AdminCategoryItem): void => {
  openEditEditor(category)
}

const handleDeleteCategory = (category: AdminCategoryItem): void => {
  requestDeleteCategory(category)
}

const handleSelectionChange = (nextSelectedIds: number[]): void => {
  setSelectedCategoryIds(nextSelectedIds)
}

const handleDeleteModalOpenChange = (value: boolean): void => {
  if (!value) {
    closeDeleteDialog()
  }
}

const refreshData = () => {
  // mock reload
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
  adminNavStore.setActiveMenu('categories')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <CategoriesBreadcrumbs />

      <div class="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400">
              <i class="fas fa-folder-tree text-lg"></i>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">总分类数</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.total }}
              </h3>
            </div>
          </div>
        </div>

        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400">
              <i class="fas fa-check-circle text-lg"></i>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">已启用分类</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.active }}
              </h3>
            </div>
          </div>
        </div>

        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400">
              <i class="fas fa-ban text-lg"></i>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">已禁用分类</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.disabled }}
              </h3>
            </div>
          </div>
        </div>
      </div>

      <section class="rounded-xl border border-gray-200 p-4 dark:border-dark-border sm:p-5">
        <CategoriesToolbar
          :searchKeyword="searchKeyword"
          :selectedCount="selectedCategoryIds.length"
          @update:searchKeyword="setSearchKeyword"
          @refresh="refreshData"
          @create="openCreateEditor"
        />

        <CategoriesTable
          :rows="categories"
          :selectedRowIds="selectedCategoryIds"
          :sortField="sortField"
          :sortOrder="sortOrder"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @edit="handleEditCategory"
          @delete="handleDeleteCategory"
        />
      </section>
    </div>

    <CategoryEditorModal
      :open="editorOpen"
      :mode="editorMode"
      :modelValue="formModel"
      :errors="formErrors"
      :submitting="editorSubmitting"
      :categories="allCategories"
      @update:open="
        (value) => {
          if (!value) closeEditor()
        }
      "
      @update:modelValue="updateFormModel"
      @submit="saveCategory"
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
          <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">确认删除分类</h3>
          <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
            仅当该分类下没有子分类且未被关联任何网站时，才可删除。
          </p>
        </div>
      </template>

      <div class="rounded-md border border-red-200 bg-red-50/70 px-3 py-2 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-900/15 dark:text-red-200">
        即将删除分类：
        <span class="font-semibold">{{ deletingCategory?.name || '-' }}</span>
      </div>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
          :disabled="deleteSubmitting"
          @click="closeDeleteDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-red-600 px-4 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-red-500 dark:hover:bg-red-600 dark:focus:ring-offset-gray-900"
          :disabled="deleteSubmitting"
          @click="confirmDeleteCategory"
        >
          <i v-if="deleteSubmitting" class="fas fa-spinner fa-spin mr-2"></i>
          <span>确认删除</span>
        </button>
      </template>
    </Dialog>
  </main>
</template>
