<script setup lang="ts">
import { onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import TechStackBreadcrumbs from '@/components/admin/tech-stack/TechStackBreadcrumbs.vue'
import TechStackToolbar from '@/components/admin/tech-stack/TechStackToolbar.vue'
import TechStackTable from '@/components/admin/tech-stack/TechStackTable.vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import TechStackEditorModal from '@/components/admin/tech-stack/TechStackEditorModal.vue'
import { useTechStack } from '@/composables/admin/useTechStack'
import { useAdminNavStore } from '@/stores/adminNav'
import type {
  AdminTechStackFormModel,
  AdminTechStackItem,
  AdminTechStackSortField,
  AdminTechStackSortOrder,
} from '@/types/tech-stack'

const adminNavStore = useAdminNavStore()

const {
  techStacks,
  currentPage,
  pageSize,
  totalItems,
  totalPages,
  visiblePages,
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
  deletingItem,
  selectedIds,
  batchDeleteDialogOpen,
  batchDeleteSubmitting,
  stats,

  refreshTechStacks,
  setSearchKeyword,
  setSort,
  openCreateEditor,
  openEditEditor,
  closeEditor,
  saveItem,
  requestDeleteItem,
  closeDeleteDialog,
  confirmDeleteItem,
  openBatchDeleteDialog,
  closeBatchDeleteDialog,
  confirmBatchDelete,
  toggleStatus,
  setSelectedIds,
  prevPage,
  nextPage,
  goToPage,
} = useTechStack()

const updateFormModel = (nextValue: AdminTechStackFormModel): void => {
  Object.assign(formModel, nextValue)
}

const handleSortChange = (payload: { field: AdminTechStackSortField; order: AdminTechStackSortOrder }): void => {
  setSort(payload.field, payload.order)
}

const handleEditItem = (item: AdminTechStackItem): void => {
  openEditEditor(item)
}

const handleDeleteItem = (item: AdminTechStackItem): void => {
  requestDeleteItem(item)
}

const handleSelectionChange = (nextSelectedIds: number[]): void => {
  setSelectedIds(nextSelectedIds)
}

const handleDeleteModalOpenChange = (value: boolean): void => {
  if (!value) {
    closeDeleteDialog()
  }
}

const handleBatchDeleteModalOpenChange = (value: boolean): void => {
  if (!value) {
    closeBatchDeleteDialog()
  }
}

const refreshData = () => {
  void refreshTechStacks()
}

const enabledPercent = () => {
  if (stats.total === 0) return 0
  return Math.round((stats.enabled / stats.total) * 100)
}

const dialogPt = {
  mask: { class: 'bg-black/50 backdrop-blur-[2px] z-[120]' },
  root: {
    class:
      'w-[min(92vw,440px)] rounded-xl border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_20px_40px_rgba(15,23,42,0.28)] dark:shadow-[0_28px_56px_rgba(2,6,23,0.65)] overflow-hidden',
  },
  header: {
    class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50/80 dark:bg-dark-bg/80 px-6 py-5',
  },
  content: {
    class: 'px-6 py-5',
  },
  footer: {
    class:
      'px-6 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50/60 dark:bg-dark-bg/60 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
  },
} as const

onMounted(() => {
  adminNavStore.setActiveMenu('techStack')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <!-- Page Header -->
      <div class="mb-8">
        <TechStackBreadcrumbs />
        <div class="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
              技术栈管理
            </h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              管理全站技术栈字典，支撑用户画像与资源分类
            </p>
          </div>
          <button
            type="button"
            @click="openCreateEditor"
            class="inline-flex h-10 shrink-0 items-center justify-center rounded-lg border border-transparent bg-[#f55911] px-5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-[#e04e0a] hover:shadow-md focus:outline-none focus:ring-2 focus:ring-[#f55911]/40 focus:ring-offset-2 active:scale-[0.98] dark:focus:ring-offset-gray-900"
          >
            <i class="fas fa-plus mr-2 text-xs"></i>
            新建技术栈
          </button>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div
          class="group relative overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md dark:border-dark-border dark:bg-dark-card"
        >
          <div class="absolute left-0 top-0 h-full w-1 bg-[#f55911]"></div>
          <div class="flex items-center justify-between px-5 py-4 pl-6">
            <div>
              <p class="text-xs font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500">
                总技术栈
              </p>
              <h3 class="mt-1 text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.total }}
              </h3>
            </div>
            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-[#f55911]/8 text-[#f55911] dark:bg-[#f55911]/10 dark:text-[#f78166]"
            >
              <i class="fas fa-layer-group text-lg"></i>
            </div>
          </div>
        </div>

        <div
          class="group relative overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md dark:border-dark-border dark:bg-dark-card"
        >
          <div class="absolute left-0 top-0 h-full w-1 bg-emerald-500"></div>
          <div class="flex items-center justify-between px-5 py-4 pl-6">
            <div>
              <p class="text-xs font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500">
                已启用
              </p>
              <div class="mt-1 flex items-baseline gap-2">
                <h3 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
                  {{ stats.enabled }}
                </h3>
                <span class="text-xs font-medium text-emerald-600 dark:text-emerald-400">
                  {{ enabledPercent() }}%
                </span>
              </div>
            </div>
            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-500/8 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400"
            >
              <i class="fas fa-check-circle text-lg"></i>
            </div>
          </div>
          <div class="mx-5 mb-3 h-1.5 overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
            <div
              class="h-full rounded-full bg-emerald-500 transition-all duration-500"
              :style="{ width: enabledPercent() + '%' }"
            ></div>
          </div>
        </div>

        <div
          class="group relative overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md dark:border-dark-border dark:bg-dark-card"
        >
          <div class="absolute left-0 top-0 h-full w-1 bg-gray-400 dark:bg-gray-500"></div>
          <div class="flex items-center justify-between px-5 py-4 pl-6">
            <div>
              <p class="text-xs font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500">
                已禁用
              </p>
              <h3 class="mt-1 text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.disabled }}
              </h3>
            </div>
            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-gray-500/8 text-gray-500 dark:bg-gray-500/10 dark:text-gray-400"
            >
              <i class="fas fa-ban text-lg"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <section class="rounded-xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
        <div class="p-4 sm:p-5">
          <TechStackToolbar
            :searchKeyword="searchKeyword"
            :selectedCount="selectedIds.length"
            @update:searchKeyword="setSearchKeyword"
            @refresh="refreshData"
            @create="openCreateEditor"
            @batchDelete="openBatchDeleteDialog"
          />

          <TechStackTable
            :rows="techStacks"
            :selectedRowIds="selectedIds"
            :sortField="sortField"
            :sortOrder="sortOrder"
            @selection-change="handleSelectionChange"
            @sort-change="handleSortChange"
            @edit="handleEditItem"
            @delete="handleDeleteItem"
            @toggle-status="toggleStatus"
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
        </div>
      </section>
    </div>

    <!-- Editor Modal -->
    <TechStackEditorModal
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
      @submit="saveItem"
    />

    <!-- Single Delete Dialog -->
    <Dialog
      :visible="deleteDialogOpen"
      modal
      :dismissableMask="true"
      :draggable="false"
      :pt="dialogPt"
      @update:visible="handleDeleteModalOpenChange"
    >
      <template #header>
        <div class="flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 text-red-600 dark:bg-red-900/25 dark:text-red-400"
          >
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <div>
            <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">确认删除技术栈</h3>
            <p class="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
              此操作不可撤销，关联用户数据不受影响
            </p>
          </div>
        </div>
      </template>

      <div class="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50/80 px-4 py-3 dark:border-red-900/40 dark:bg-red-900/15">
        <div
          v-if="deletingItem?.color"
          class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-xs font-bold text-white"
          :style="{ backgroundColor: deletingItem.color }"
        >
          {{ deletingItem?.name?.charAt(0) || '?' }}
        </div>
        <div class="min-w-0">
          <p class="text-sm font-medium text-red-800 dark:text-red-200">
            {{ deletingItem?.name || '-' }}
          </p>
          <p v-if="deletingItem?.userCount" class="text-xs text-red-600/70 dark:text-red-300/60">
            被 {{ deletingItem.userCount }} 位用户关联
          </p>
        </div>
      </div>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg dark:focus:ring-offset-gray-900"
          :disabled="deleteSubmitting"
          @click="closeDeleteDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-transparent bg-red-600 px-4 text-sm font-medium text-white transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500/40 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-red-500 dark:hover:bg-red-600 dark:focus:ring-offset-gray-900"
          :disabled="deleteSubmitting"
          @click="confirmDeleteItem"
        >
          <i v-if="deleteSubmitting" class="fas fa-spinner fa-spin mr-2"></i>
          <span>确认删除</span>
        </button>
      </template>
    </Dialog>

    <!-- Batch Delete Dialog -->
    <Dialog
      :visible="batchDeleteDialogOpen"
      modal
      :dismissableMask="true"
      :draggable="false"
      :pt="dialogPt"
      @update:visible="handleBatchDeleteModalOpenChange"
    >
      <template #header>
        <div class="flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 text-red-600 dark:bg-red-900/25 dark:text-red-400"
          >
            <i class="fas fa-trash-alt"></i>
          </div>
          <div>
            <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">批量删除技术栈</h3>
            <p class="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
              此操作不可撤销，请确认选择
            </p>
          </div>
        </div>
      </template>

      <div class="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50/80 px-4 py-3 dark:border-red-900/40 dark:bg-red-900/15">
        <i class="fas fa-exclamation-circle text-red-500 dark:text-red-400"></i>
        <p class="text-sm text-red-700 dark:text-red-200">
          已选中 <span class="font-bold">{{ selectedIds.length }}</span> 个技术栈，删除后无法恢复
        </p>
      </div>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg dark:focus:ring-offset-gray-900"
          :disabled="batchDeleteSubmitting"
          @click="closeBatchDeleteDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-transparent bg-red-600 px-4 text-sm font-medium text-white transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500/40 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-red-500 dark:hover:bg-red-600 dark:focus:ring-offset-gray-900"
          :disabled="batchDeleteSubmitting"
          @click="confirmBatchDelete"
        >
          <i v-if="batchDeleteSubmitting" class="fas fa-spinner fa-spin mr-2"></i>
          <span>确认删除</span>
        </button>
      </template>
    </Dialog>
  </main>
</template>
