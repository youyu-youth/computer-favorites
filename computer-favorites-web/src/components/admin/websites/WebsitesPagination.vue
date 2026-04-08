<script setup lang="ts">
import AdminPagination from '@/components/admin/common/AdminPagination.vue'

const props = defineProps<{
  currentPage: number
  totalPages: number
  visiblePages: Array<number | string>
  total: number
  pageSize: number
}>()

defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'goto', page: number | string): void
}>()
</script>

<template>
  <AdminPagination
    :currentPage="props.currentPage"
    :totalPages="props.totalPages"
    :visiblePages="props.visiblePages"
    :total="props.total"
    :pageSize="props.pageSize"
    @prev="$emit('prev')"
    @next="$emit('next')"
    @goto="$emit('goto', $event)"
  />
</template>
