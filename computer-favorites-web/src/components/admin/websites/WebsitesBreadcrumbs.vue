<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { AdminMenuKey } from '@/stores/adminNav'
import { useAdminNavStore } from '@/stores/adminNav'

const props = defineProps({
  viewMode: {
    type: String,
    default: 'grid'
  }
})

const emit = defineEmits<{
  (e: 'go-websites-root'): void
}>()

const adminNavStore = useAdminNavStore()
const router = useRouter()
const route = useRoute()

const {
  menuItems,
  activeMenu,
  activeMenuLabel,
  isWebsiteMenuActive,
  selectedCategoryLabel,
} = storeToRefs(adminNavStore)

type BreadcrumbItem = {
  key: string
  label: string
  level: number
  current: boolean
  clickable: boolean
  onClick: () => void | Promise<void>
}

const goWebsites = async () => {
  adminNavStore.setActiveMenu('websites')
  adminNavStore.setSelectedCategoryId(0)
  emit('go-websites-root')
  if (route.name === 'adminWebsites') {
    return
  }
  await router.push({ name: 'adminWebsites' })
}

const breadcrumbItems = computed(() => {
  const items: BreadcrumbItem[] = [
    {
      key: 'admin-root',
      label: '管理后台',
      level: 1,
      current: false,
      clickable: true,
      onClick: () => goWebsites(),
    },
    {
      key: `menu-${activeMenu.value}`,
      label: activeMenuLabel.value,
      level: 2,
      current: false,
      clickable: true,
      onClick: async () => {
        const menuKey = activeMenu.value as AdminMenuKey
        adminNavStore.setActiveMenu(menuKey)
        if (menuKey === 'websites') {
          await goWebsites()
          return
        }
        if (menuKey === 'tags' && route.name !== 'adminTags') {
          await router.push({ name: 'adminTags' })
        }
      },
    },
  ]

  if (isWebsiteMenuActive.value) {
    if (props.viewMode === 'add') {
      items.push({
        key: 'add-website',
        label: '添加网站',
        level: 3,
        current: true,
        clickable: true,
        onClick: () => {},
      })
    } else if (props.viewMode === 'edit') {
      items.push({
        key: 'edit-website',
        label: '修改网站',
        level: 3,
        current: true,
        clickable: true,
        onClick: () => {},
      })
    } else {
      items.push({
        key: `category-${selectedCategoryLabel.value}`,
        label: selectedCategoryLabel.value,
        level: 3,
        current: true,
        clickable: true,
        onClick: () => goWebsites(),
      })
    }
  }

  return items
})

const activateMenu = async (menuKey: AdminMenuKey) => {
  adminNavStore.setActiveMenu(menuKey)
  if (menuKey === 'websites') {
    await goWebsites()
    return
  }
  if (menuKey === 'tags' && route.name !== 'adminTags') {
    await router.push({ name: 'adminTags' })
  }
}
</script>

<template>
  <div class="mb-6">
    <UBreadcrumb :items="breadcrumbItems" className="mb-4" />

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
            @click="void activateMenu(menu.key)"
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
          @click="void activateMenu(menu.key)"
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

