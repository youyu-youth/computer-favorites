<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { AdminMenuKey, WebsiteAuditTabKey } from '@/stores/adminNav'
import { useAdminNavStore } from '@/stores/adminNav'
import WebsitesSidebar from '@/components/admin/websites/WebsitesSidebar.vue'
import { getAdminWebsiteStats } from '@/api/admin-website'

const navStore = useAdminNavStore()
const router = useRouter()
const route = useRoute()
const {
  activeMenu,
  menuItems,
  websiteCategories,
  selectedWebsiteAuditTab,
  selectedCategoryId,
  pendingAuditCount,
  mobileSidebarOpen,
} = storeToRefs(navStore)

const websitesSidebarMode = ref<'menu' | 'category'>('menu')
const websitesSubmenuExpanded = ref(true)

const isWebsiteMenuActive = computed(() => activeMenu.value === 'websites')

const isWebsiteSubmenuVisible = computed(() => {
  return isWebsiteMenuActive.value && websitesSidebarMode.value === 'menu' && websitesSubmenuExpanded.value
})

const isWebsiteCategorySidebarVisible = computed(() => {
  return isWebsiteMenuActive.value && websitesSidebarMode.value === 'category'
})

const hasPendingAudit = computed(() => pendingAuditCount.value > 0)

const pendingAuditBadgeText = computed(() => {
  if (pendingAuditCount.value > 99) {
    return '99+'
  }
  return String(pendingAuditCount.value)
})

const sidebarCategories = computed(() => {
  return websiteCategories.value.map((item) => ({
    ...item,
    active: item.id === selectedCategoryId.value,
  }))
})

const syncPendingAuditCount = async () => {
  try {
    const pendingStats = await getAdminWebsiteStats(0)
    navStore.setPendingAuditCount(Number(pendingStats.pendingAudit || 0))
  } catch {
    // 红点同步失败不影响侧栏主流程。
  }
}

const closeMobileSidebar = (): void => {
  navStore.closeMobileSidebar()
}

const navigateByMenu = async (menuKey: AdminMenuKey): Promise<void> => {
  if (menuKey === 'websites') {
    if (route.name !== 'adminWebsites') {
      await router.push({ name: 'adminWebsites' })
    }
    return
  }

  if (menuKey === 'tags' && route.name !== 'adminTags') {
    await router.push({ name: 'adminTags' })
  }

  if (menuKey === 'categories' && route.name !== 'adminCategories') {
    await router.push({ name: 'adminCategories' })
  }

  if (menuKey === 'users' && route.name !== 'adminUsers') {
    await router.push({ name: 'adminUsers' })
  }
}

const goWebsitesRoot = async (): Promise<void> => {
  navStore.setActiveMenu('websites')
  navStore.setSelectedWebsiteAuditTab('pending')
  navStore.setSelectedCategoryId(0)
  websitesSidebarMode.value = 'menu'
  websitesSubmenuExpanded.value = true

  if (route.name !== 'adminWebsites') {
    await router.push({ name: 'adminWebsites' })
  }
}

const handleMainMenuClick = (key: AdminMenuKey) => {
  if (key === 'websites') {
    void goWebsitesRoot()
    return
  }

  navStore.setActiveMenu(key)
  void navigateByMenu(key)

  if (typeof window !== 'undefined' && window.innerWidth < 768) {
    navStore.closeMobileSidebar()
  }
}

const handleAuditTabClick = (auditTab: WebsiteAuditTabKey) => {
  navStore.setSelectedWebsiteAuditTab(auditTab)
  websitesSidebarMode.value = 'category'

  if (route.name !== 'adminWebsites') {
    void router.push({ name: 'adminWebsites' })
  }
}

const handleCategorySelect = (categoryId: number) => {
  navStore.setSelectedCategoryId(categoryId)

  if (route.name !== 'adminWebsites') {
    void router.push({ name: 'adminWebsites' })
  }

  if (typeof window !== 'undefined' && window.innerWidth < 768) {
    navStore.closeMobileSidebar()
  }
}

const backToWebsitesMenu = () => {
  websitesSidebarMode.value = 'menu'
  websitesSubmenuExpanded.value = true
}

const toggleWebsitesSubmenu = () => {
  if (!isWebsiteMenuActive.value) {
    navStore.setActiveMenu('websites')
    websitesSidebarMode.value = 'menu'
    websitesSubmenuExpanded.value = true
    if (route.name !== 'adminWebsites') {
      void router.push({ name: 'adminWebsites' })
    }
    return
  }

  if (websitesSidebarMode.value !== 'menu') {
    websitesSidebarMode.value = 'menu'
    websitesSubmenuExpanded.value = true
    return
  }

  websitesSubmenuExpanded.value = !websitesSubmenuExpanded.value
}

// 可拖拽改变侧栏宽度逻辑
const sidebarWidth = ref(288) // 默认 width: 288px (w-72)
const isDragging = ref(false)

const startDrag = () => {
  isDragging.value = true
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  let newWidth = e.clientX
  // 限制宽度的范围 (最小 200px, 最大 500px)
  if (newWidth < 200) newWidth = 200
  if (newWidth > 500) newWidth = 500
  sidebarWidth.value = newWidth
}

const stopDrag = () => {
  isDragging.value = false
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})

onMounted(() => {
  void syncPendingAuditCount()
})

watch(
  () => activeMenu.value,
  (menuKey) => {
    if (menuKey !== 'websites') {
      websitesSidebarMode.value = 'menu'
      websitesSubmenuExpanded.value = true
    }
  },
)
</script>

<template>
  <div>
    <!-- Desktop Sidebar -->
    <aside
      class="hidden md:flex relative flex-col flex-shrink-0 border-r border-gray-200 dark:border-dark-border bg-white dark:bg-dark-bg/50 h-[calc(100vh-4rem)] sticky top-16 transition-colors duration-300"
      :style="{ width: sidebarWidth + 'px' }"
    >
      <!-- Resize Handle -->
      <div
        class="absolute top-0 bottom-0 -right-1 w-2 cursor-col-resize z-10 transition-colors flex items-center justify-center group"
        :class="isDragging ? 'bg-[#f78166]/50' : 'hover:bg-gray-300 dark:hover:bg-gray-600'"
        @mousedown.prevent="startDrag"
      >
        <div
          class="h-8 w-1 rounded-full bg-gray-400 dark:bg-gray-500 opacity-0 group-hover:opacity-100 transition-opacity"
          :class="isDragging ? 'opacity-100 bg-[#f78166]' : ''"
        ></div>
      </div>

      <div
        v-if="isWebsiteCategorySidebarVisible"
        class="flex-1 overflow-y-auto px-3 py-4 w-full flex flex-col"
      >
        <div class="mb-3">
          <button
            type="button"
            class="cursor-pointer inline-flex items-center gap-2 rounded-md border border-gray-200 px-2.5 py-1.5 text-xs text-gray-600 transition-colors hover:bg-gray-100 dark:border-dark-border dark:text-gray-300 dark:hover:bg-dark-card"
            @click="backToWebsitesMenu"
          >
            <i class="fas fa-chevron-left text-[10px]"></i>
            <span>返回审核菜单</span>
          </button>
        </div>

        <div class="mb-2 px-1 text-xs text-gray-500 dark:text-gray-400">
          当前：{{ selectedWebsiteAuditTab === 'pending' ? '待审核网站' : '已审核网站' }}
        </div>

        <WebsitesSidebar :categories="sidebarCategories" :showSearch="true" @select="handleCategorySelect" />
      </div>

      <!-- Primary Main Level View -->
      <div v-else class="flex-1 overflow-y-auto px-3 py-4 w-full">
        <h2
          class="text-xs font-semibold tracking-wider text-gray-500 dark:text-gray-400 mb-4 px-3 uppercase"
        >
          导航主页
        </h2>

        <div class="space-y-1">
          <div v-for="menu in menuItems" :key="menu.key" class="flex flex-col mb-1">
            <div
              :data-testid="menu.key === 'websites' ? 'admin-menu-websites' : undefined"
              @click="handleMainMenuClick(menu.key)"
              class="group flex items-center justify-between px-3 py-2 cursor-pointer rounded-lg transition-colors border-l-2"
              :class="
                activeMenu === menu.key
                  ? 'border-[#f78166] bg-orange-50/50 text-[#f78166] dark:bg-[#f78166]/10'
                  : 'border-transparent text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50'
              "
            >
              <div class="flex items-center gap-3 min-w-0">
                <i
                  :class="[
                    menu.icon,
                    activeMenu === menu.key
                      ? 'text-[#f78166]'
                      : 'text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300',
                  ]"
                  class="w-5 text-center text-lg"
                ></i>
                <span class="text-sm font-medium truncate">{{ menu.label }}</span>
              </div>

              <div class="flex items-center gap-2">
                <span
                  v-if="menu.key === 'websites' && hasPendingAudit"
                  data-testid="admin-menu-websites-pending-badge"
                  class="inline-flex min-w-5 h-5 px-1.5 items-center justify-center rounded-full bg-red-500 text-[11px] font-semibold leading-none text-white"
                >
                  {{ pendingAuditBadgeText }}
                </span>

                <span
                  v-if="menu.mockOnly"
                  class="text-[10px] px-1.5 py-0.5 rounded-md bg-gray-100 dark:bg-dark-card text-gray-400 dark:text-gray-500"
                  >模拟</span
                >

                <button
                  v-else-if="menu.key === 'websites'"
                  type="button"
                  data-testid="admin-menu-websites-toggle"
                  class="cursor-pointer inline-flex h-5 w-5 items-center justify-center rounded-sm transition-colors hover:bg-gray-200/60 dark:hover:bg-dark-card"
                  :aria-expanded="isWebsiteSubmenuVisible ? 'true' : 'false'"
                  @click.stop="toggleWebsitesSubmenu"
                >
                  <i
                    class="fas text-[10px] transition-transform"
                    :class="[
                      isWebsiteSubmenuVisible ? 'fa-chevron-down' : 'fa-chevron-right',
                      activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400',
                    ]"
                  ></i>
                </button>

                <i
                  v-else
                  class="fas text-[10px] transition-transform"
                  :class="['fa-chevron-right', activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400']"
                ></i>
              </div>
            </div>

            <transition name="fade-slide-submenu">
              <div
                v-if="menu.key === 'websites' && isWebsiteSubmenuVisible"
                class="mt-2 ml-9 rounded-md border border-gray-200/80 bg-white/60 p-2 dark:border-dark-border/80 dark:bg-dark-card/30"
              >
                <div class="space-y-1.5">
                  <button
                    data-testid="admin-audit-tab-pending"
                    type="button"
                    class="w-full cursor-pointer inline-flex items-center justify-between rounded-md px-2.5 py-1.5 text-sm transition-colors"
                    :class="
                      selectedWebsiteAuditTab === 'pending'
                        ? 'bg-orange-100 text-[#e95322] dark:bg-[#2a1b12]'
                        : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-card'
                    "
                    @click="handleAuditTabClick('pending')"
                  >
                    <span>待审核网站</span>
                    <span
                      v-if="hasPendingAudit"
                      data-testid="admin-audit-tab-pending-badge"
                      class="inline-flex min-w-5 h-5 px-1.5 items-center justify-center rounded-full bg-red-500 text-[11px] font-semibold leading-none text-white"
                    >
                      {{ pendingAuditBadgeText }}
                    </span>
                  </button>

                  <button
                    data-testid="admin-audit-tab-audited"
                    type="button"
                    class="w-full cursor-pointer inline-flex items-center justify-between rounded-md px-2.5 py-1.5 text-sm transition-colors"
                    :class="
                      selectedWebsiteAuditTab === 'audited'
                        ? 'bg-orange-100 text-[#e95322] dark:bg-[#2a1b12]'
                        : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-card'
                    "
                    @click="handleAuditTabClick('audited')"
                  >
                    <span>已审核网站</span>
                  </button>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </aside>

    <!-- Mobile Drawer (Matches the exact same logic) -->
    <div v-if="mobileSidebarOpen" class="fixed inset-0 z-40 md:hidden">
      <div class="absolute inset-0 bg-black/45 backdrop-blur-sm" @click="closeMobileSidebar"></div>

      <aside
        class="absolute left-0 top-16 h-[calc(100vh-4rem)] w-[84vw] max-w-xs flex flex-col border-r border-gray-200 dark:border-dark-border bg-white dark:bg-dark-bg overflow-hidden shadow-2xl transition-transform"
      >
        <div
          class="flex items-center justify-between px-4 py-3 border-b border-gray-100 dark:border-dark-border/50"
        >
          <h2 class="text-sm font-semibold text-gray-800 dark:text-gray-200">导航主页</h2>
          <button
            type="button"
            class="w-8 h-8 flex items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 dark:hover:bg-dark-card transition-colors"
            @click="closeMobileSidebar"
          >
            <i class="fas fa-xmark"></i>
          </button>
        </div>

        <div
          v-if="isWebsiteCategorySidebarVisible"
          class="flex-1 overflow-y-auto px-3 py-4 w-full flex flex-col"
        >
          <div class="mb-3">
            <button
              type="button"
              class="cursor-pointer inline-flex items-center gap-2 rounded-md border border-gray-200 px-2.5 py-1.5 text-xs text-gray-600 transition-colors hover:bg-gray-100 dark:border-dark-border dark:text-gray-300 dark:hover:bg-dark-card"
              @click="backToWebsitesMenu"
            >
              <i class="fas fa-chevron-left text-[10px]"></i>
              <span>返回审核菜单</span>
            </button>
          </div>

          <div class="mb-2 px-1 text-xs text-gray-500 dark:text-gray-400">
            当前：{{ selectedWebsiteAuditTab === 'pending' ? '待审核网站' : '已审核网站' }}
          </div>

          <WebsitesSidebar
            :categories="sidebarCategories"
            :showSearch="true"
            :dense="true"
            @select="handleCategorySelect"
          />
        </div>

        <div v-else class="flex-1 overflow-y-auto px-3 py-4 w-full">
          <div class="space-y-1">
            <div v-for="menu in menuItems" :key="menu.key" class="flex flex-col mb-1">
              <div
                :data-testid="menu.key === 'websites' ? 'admin-menu-websites-mobile' : undefined"
                @click="handleMainMenuClick(menu.key)"
                class="group flex items-center justify-between px-3 py-2 cursor-pointer rounded-lg border-l-[3px]"
                :class="
                  activeMenu === menu.key
                    ? 'border-[#f78166] bg-orange-50/50 text-[#f78166] dark:bg-[#f78166]/10'
                    : 'border-transparent text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50'
                "
              >
                <div class="flex items-center gap-3 min-w-0">
                  <i
                    :class="[
                      menu.icon,
                      activeMenu === menu.key
                        ? 'text-[#f78166]'
                        : 'text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300',
                    ]"
                    class="w-5 text-center text-lg"
                  ></i>
                  <span class="text-sm font-medium truncate">{{ menu.label }}</span>
                </div>

                <div class="flex items-center gap-2">
                  <span
                    v-if="menu.key === 'websites' && hasPendingAudit"
                    data-testid="admin-menu-websites-pending-badge-mobile"
                    class="inline-flex min-w-5 h-5 px-1.5 items-center justify-center rounded-full bg-red-500 text-[11px] font-semibold leading-none text-white"
                  >
                    {{ pendingAuditBadgeText }}
                  </span>

                  <span
                    v-if="menu.mockOnly"
                    class="text-[10px] px-1.5 py-0.5 rounded-md bg-gray-100 dark:bg-dark-card text-gray-400 dark:text-gray-500"
                    >模拟</span
                  >

                  <button
                    v-else-if="menu.key === 'websites'"
                    type="button"
                    data-testid="admin-menu-websites-toggle-mobile"
                    class="cursor-pointer inline-flex h-5 w-5 items-center justify-center rounded-sm transition-colors hover:bg-gray-200/60 dark:hover:bg-dark-card"
                    :aria-expanded="isWebsiteSubmenuVisible ? 'true' : 'false'"
                    @click.stop="toggleWebsitesSubmenu"
                  >
                    <i
                      class="fas text-[10px] transition-transform"
                      :class="[
                        isWebsiteSubmenuVisible ? 'fa-chevron-down' : 'fa-chevron-right',
                        activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400',
                      ]"
                    ></i>
                  </button>

                  <i
                    v-else
                    class="fas text-[10px] transition-transform"
                    :class="['fa-chevron-right', activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400']"
                  ></i>
                </div>
              </div>

              <transition name="fade-slide-submenu">
                <div
                  v-if="menu.key === 'websites' && isWebsiteSubmenuVisible"
                  class="mt-2 ml-7 rounded-md border border-gray-200/80 bg-white/60 p-2 dark:border-dark-border/80 dark:bg-dark-card/30"
                >
                  <div class="space-y-1.5">
                    <button
                      data-testid="admin-audit-tab-pending-mobile"
                      type="button"
                      class="w-full cursor-pointer inline-flex items-center justify-between rounded-md px-2.5 py-1.5 text-sm transition-colors"
                      :class="
                        selectedWebsiteAuditTab === 'pending'
                          ? 'bg-orange-100 text-[#e95322] dark:bg-[#2a1b12]'
                          : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-card'
                      "
                      @click="handleAuditTabClick('pending')"
                    >
                      <span>待审核网站</span>
                      <span
                        v-if="hasPendingAudit"
                        data-testid="admin-audit-tab-pending-badge-mobile"
                        class="inline-flex min-w-5 h-5 px-1.5 items-center justify-center rounded-full bg-red-500 text-[11px] font-semibold leading-none text-white"
                      >
                        {{ pendingAuditBadgeText }}
                      </span>
                    </button>

                    <button
                      data-testid="admin-audit-tab-audited-mobile"
                      type="button"
                      class="w-full cursor-pointer inline-flex items-center justify-between rounded-md px-2.5 py-1.5 text-sm transition-colors"
                      :class="
                        selectedWebsiteAuditTab === 'audited'
                          ? 'bg-orange-100 text-[#e95322] dark:bg-[#2a1b12]'
                          : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-card'
                      "
                      @click="handleAuditTabClick('audited')"
                    >
                      <span>已审核网站</span>
                    </button>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.fade-slide-submenu-enter-active,
.fade-slide-submenu-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-slide-submenu-enter-from,
.fade-slide-submenu-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
