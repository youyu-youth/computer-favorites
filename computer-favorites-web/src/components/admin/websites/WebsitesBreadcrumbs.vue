<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import type { AdminMenuKey } from '@/stores/adminNav'
import { useAdminNavStore } from '@/stores/adminNav'

const props = defineProps({
  viewMode: {
    type: String,
    default: 'grid'
  }
})

const adminNavStore = useAdminNavStore()

const {
  menuItems,
  activeMenu,
  activeMenuLabel,
  isWebsiteMenuActive,
  selectedCategoryLabel,
} = storeToRefs(adminNavStore)

const breadcrumbItems = computed(() => {
  const items = [
    { key: 'admin-root', label: '管理后台' },
    { key: `menu-${activeMenu.value}`, label: activeMenuLabel.value },
  ]

  if (isWebsiteMenuActive.value) {
    if (props.viewMode === 'add') {
      items.push({
        key: `add-website`,
        label: '添加网站',
      })
    } else {
      items.push({
        key: `category-${selectedCategoryLabel.value}`,
        label: selectedCategoryLabel.value,
      })
    }
  }

  return items
})

const activateMenu = (menuKey: AdminMenuKey) => {
  adminNavStore.setActiveMenu(menuKey)
}
</script>

<template>
  <div class="mb-6">
    <div class="menu-scroll text-sm text-gray-500 dark:text-gray-400 mb-4 flex items-center gap-2 overflow-x-auto whitespace-nowrap">
      <template v-for="(item, index) in breadcrumbItems" :key="item.key">
        <span class="text-gray-900 dark:text-gray-100 font-medium">
          {{ item.label }}
        </span>
        <i v-if="index < breadcrumbItems.length - 1" class="fas fa-chevron-right text-xs text-gray-400 dark:text-gray-500"></i>
      </template>
    </div>

    <div class="border-b border-gray-200 dark:border-dark-border pb-3">
      <div class="md:hidden -mx-1 px-1 menu-scroll overflow-x-auto">
        <div class="inline-flex min-w-max gap-2">
          <button
            v-for="menu in menuItems"
            :key="`mobile-${menu.key}`"
            type="button"
            class="cursor-pointer inline-flex shrink-0 items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors border"
            :class="activeMenu === menu.key
              ? 'text-[#e95322] border-[#e95322] bg-orange-50 dark:bg-[#161b22]'
              : 'text-gray-500 dark:text-gray-400 border-transparent hover:text-gray-900 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-card'"
            @click="activateMenu(menu.key)"
          >
            <i :class="menu.icon" class="text-xs"></i>
            <span>{{ menu.label }}</span>
            <span
              v-if="menu.mockOnly"
              class="text-[11px] px-1.5 py-0.5 rounded-full"
              :class="activeMenu === menu.key
                ? 'bg-[#e95322]/12 text-[#e95322] border border-[#e95322]/35'
                : 'bg-gray-100 dark:bg-dark-card text-gray-500 dark:text-gray-400'"
            >
              模拟
            </span>
          </button>
        </div>
      </div>

      <div class="hidden md:flex flex-wrap gap-3 text-sm font-medium">
        <button
          v-for="menu in menuItems"
          :key="`desktop-${menu.key}`"
          type="button"
          class="cursor-pointer inline-flex items-center gap-2 rounded-md px-3 py-2 transition-colors border"
          :class="activeMenu === menu.key
            ? 'text-[#e95322] border-[#e95322] bg-orange-50 dark:bg-[#161b22]'
            : 'text-gray-500 dark:text-gray-400 border-transparent hover:text-gray-900 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-card'"
          @click="activateMenu(menu.key)"
        >
          <i :class="menu.icon" class="text-xs"></i>
          <span>{{ menu.label }}</span>
          <span
            v-if="menu.mockOnly"
            class="text-[11px] px-1.5 py-0.5 rounded-full"
            :class="activeMenu === menu.key
              ? 'bg-[#e95322]/12 text-[#e95322] border border-[#e95322]/35'
              : 'bg-gray-100 dark:bg-dark-card text-gray-500 dark:text-gray-400'"
          >
            模拟
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-scroll {
  scrollbar-width: none;
}

.menu-scroll::-webkit-scrollbar {
  display: none;
}
</style>

