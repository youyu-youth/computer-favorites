<script setup lang="ts">
import { onMounted } from 'vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import AuditBreadcrumbs from '@/components/admin/audit/AuditBreadcrumbs.vue'
import AuditDetailModal from '@/components/admin/audit/AuditDetailModal.vue'
import AuditStatsCards from '@/components/admin/audit/AuditStatsCards.vue'
import AuditTable from '@/components/admin/audit/AuditTable.vue'
import AuditToolbar from '@/components/admin/audit/AuditToolbar.vue'
import { useAuditLogManagement } from '@/composables/admin/useAuditLogManagement'
import { useAdminNavStore } from '@/stores/adminNav'

const adminNavStore = useAdminNavStore()

const {
  loading,
  query,
  pagedLogs,
  totalItems,
  totalPages,
  visiblePages,
  detailOpen,
  detailRecord,
  statistics,
  setModule,
  setAction,
  setUserType,
  setResult,
  setStartTime,
  setEndTime,
  setKeyword,
  resetFilters,
  prevPage,
  nextPage,
  goToPage,
  openDetail,
  closeDetail,
  exportLogs,
} = useAuditLogManagement()

onMounted(() => {
  adminNavStore.setActiveMenu('auditLogs')
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <AuditBreadcrumbs />
      <AuditStatsCards :statistics="statistics" />
      <AuditToolbar
        class="mb-5"
        :module="query.module"
        :action="query.action"
        :userType="query.userType"
        :result="query.result"
        :startTime="query.startTime"
        :endTime="query.endTime"
        :keyword="query.keyword"
        @update:module="setModule"
        @update:action="setAction"
        @update:userType="setUserType"
        @update:result="setResult"
        @update:startTime="setStartTime"
        @update:endTime="setEndTime"
        @update:keyword="setKeyword"
        @refresh="() => {}"
        @reset="resetFilters"
        @export="exportLogs"
      />
      <section class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card sm:p-5">
        <AuditTable :rows="pagedLogs" :loading="loading" @view="openDetail" />
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
    <AuditDetailModal
      :open="detailOpen"
      :detail="detailRecord"
      @update:open="(value: boolean) => { if (!value) closeDetail() }"
    />
  </main>
</template>
