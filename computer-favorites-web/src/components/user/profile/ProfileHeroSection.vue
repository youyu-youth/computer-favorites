<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传内容数据看板 - 组合薄壳
 * 通过 showSummary / showCharts 控制是否渲染拆分后的两个子组件
 * 实际渲染交由 ProfileDashboardSummary / ProfileDashboardCharts 完成，本组件仅做组合
 * 保留对外 API 与默认行为以兼容 ProfilePublicView 等历史调用
 */
import ProfileDashboardSummary from './ProfileDashboardSummary.vue'
import ProfileDashboardCharts from './ProfileDashboardCharts.vue'

withDefaults(
  defineProps<{
    showSummary?: boolean
    showCharts?: boolean
  }>(),
  {
    showSummary: true,
    showCharts: true,
  },
)
</script>

<template>
  <section v-if="showSummary || showCharts" class="space-y-3">
    <ProfileDashboardSummary v-if="showSummary" />
    <ProfileDashboardCharts v-if="showCharts" />
  </section>
</template>
