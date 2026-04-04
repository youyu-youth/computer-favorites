<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import InputText from 'primevue/inputtext'
import Drawer from 'primevue/drawer'
import AppPagination from '@/components/common/AppPagination.vue'
import HomeHeroHeader from '@/components/user/HomeHeroHeader.vue'
import HomeIntegrationsBar from '@/components/user/HomeIntegrationsBar.vue'
import HomeTagFilterBar from '@/components/user/HomeTagFilterBar.vue'
import WebsiteResourceCard from '@/components/user/WebsiteResourceCard.vue'
import { getWebsiteCategories, getWebsitePage, getWebsiteTags } from '@/api/website'
import type { PublicWebsiteListItem, PublicWebsiteTagItem } from '@/types/public-website'
import { normalizeTagColor } from '@/utils/tag-color'

defineOptions({
  name: 'HomeView',
})

interface CategoryItem {
  id: number
  name: string
  count: number
}

interface ResourceTag {
  id: number
  label: string
  color: string
}

interface ResourceItem {
  id: number
  name: string
  desc: string
  downloads: string
  stars: string
  collections: string
  category: string
  categoryId: number
  url: string
  icon: string
  score: string | number
  tags: ResourceTag[]
}

const isDark = ref(false)
const isLoading = ref(false)
const loadError = ref('')
const searchQuery = ref('')
const activeCategoryId = ref(0)
const mobileCategoryDrawerOpen = ref(false)
const categories = ref<CategoryItem[]>([{ id: 0, name: 'All', count: 0 }])
const resources = ref<ResourceItem[]>([])
const websiteTags = ref<PublicWebsiteTagItem[]>([])
const selectedTagIds = ref<number[]>([])
const isTagLoading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)
const totalPages = ref(1)
const keywordForQuery = ref('')

let darkModeObserver: MutationObserver | null = null
let searchDebounceTimer: number | undefined
let latestRequestId = 0

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const updateDarkModeState = () => {
  isDark.value =
    document.documentElement.classList.contains('dark') || document.body.classList.contains('dark')
}

const formatMetricCount = (value: number | null | undefined): string => {
  const normalizedValue = Number(value ?? 0)
  if (!Number.isFinite(normalizedValue) || normalizedValue <= 0) {
    return '0'
  }
  if (normalizedValue >= 1000000) {
    return `${(normalizedValue / 1000000).toFixed(1).replace(/\.0$/, '')}m`
  }
  if (normalizedValue >= 1000) {
    return `${(normalizedValue / 1000).toFixed(1).replace(/\.0$/, '')}k`
  }
  return String(Math.trunc(normalizedValue))
}

const mapWebsiteToResource = (item: PublicWebsiteListItem): ResourceItem => {
  return {
    id: item.id,
    name: item.name,
    desc: item.summary?.trim() || item.description?.trim() || 'No description available.',
    downloads: formatMetricCount(item.clickCount),
    stars: formatMetricCount(item.likeCount),
    collections: formatMetricCount(item.collectCount),
    category: item.categoryName?.trim() || 'Uncategorized',
    categoryId: item.categoryId ?? 0,
    url: item.url,
    icon: item.icon || '',
    score: item.score ?? 0,
    tags: (item.tags || [])
      .map((tag) => {
        const tagName = String(tag?.name || '').trim()
        if (!tagName) {
          return null
        }
        return {
          id: Number(tag.id) > 0 ? Number(tag.id) : 0,
          label: tagName,
          color: normalizeTagColor(tag.color),
        }
      })
      .filter((tag): tag is ResourceTag => tag !== null),
  }
}

const loadWebsiteTags = async () => {
  isTagLoading.value = true
  try {
    websiteTags.value = await getWebsiteTags()
  } catch {
    websiteTags.value = []
  } finally {
    isTagLoading.value = false
  }
}

const loadWebsiteCategories = async () => {
  const categoryResult = await getWebsiteCategories()
  const mappedCategories: CategoryItem[] = categoryResult.map((category) => ({
    id: category.id,
    name: category.name,
    count: Number(category.count || 0),
  }))
  const allCount = mappedCategories.reduce((sum, item) => sum + item.count, 0)
  categories.value = [{ id: 0, name: 'All', count: allCount }, ...mappedCategories]
}

const loadWebsiteData = async () => {
  const requestId = ++latestRequestId
  isLoading.value = true
  loadError.value = ''

  try {
    const pageResult = await getWebsitePage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      categoryId: activeCategoryId.value > 0 ? activeCategoryId.value : undefined,
      keyword: keywordForQuery.value || undefined,
      tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined,
    })

    if (requestId !== latestRequestId) {
      return
    }

    resources.value = pageResult.records.map(mapWebsiteToResource)
    totalPages.value = Math.max(1, Number(pageResult.totalPages || 0))

    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  } catch (error) {
    if (requestId === latestRequestId) {
      resources.value = []
      totalPages.value = 1
      loadError.value = resolveErrorMessage(error, 'Failed to load website resources.')
    }
  } finally {
    if (requestId === latestRequestId) {
      isLoading.value = false
    }
  }
}

watch(activeCategoryId, () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  void loadWebsiteData()
})

watch(currentPage, () => {
  void loadWebsiteData()
})

watch(searchQuery, (value) => {
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
  }

  searchDebounceTimer = window.setTimeout(() => {
    keywordForQuery.value = value.trim()
    if (currentPage.value !== 1) {
      currentPage.value = 1
      return
    }
    void loadWebsiteData()
  }, 300)
})

watch(
  selectedTagIds,
  () => {
    if (currentPage.value !== 1) {
      currentPage.value = 1
      return
    }
    void loadWebsiteData()
  },
  { deep: true },
)

onMounted(() => {
  updateDarkModeState()
  darkModeObserver = new MutationObserver(updateDarkModeState)
  darkModeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  })
  darkModeObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] })
  void (async () => {
    try {
      await loadWebsiteCategories()
    } catch (error) {
      categories.value = [{ id: 0, name: 'All', count: 0 }]
      loadError.value = resolveErrorMessage(error, 'Failed to load website categories.')
    }
    await loadWebsiteTags()
    await loadWebsiteData()
  })()
})

onUnmounted(() => {
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
  }
  darkModeObserver?.disconnect()
  darkModeObserver = null
})
</script>

<template>
  <div
    class="home-v2-page min-h-screen bg-gray-50 dark:bg-black text-gray-900 dark:text-[#e2e8f0] selection:bg-primary-500 selection:text-black flex justify-center transition-colors duration-300 subpixel-antialiased"
    :class="{ 'dark-gradient': isDark }"
    :style="
      !isDark
        ? 'background-image: repeating-linear-gradient(to bottom, transparent, transparent 2px, rgba(0,0,0,0.02) 2px, rgba(0,0,0,0.02) 4px);'
        : ''
    "
  >
    <div
      class="w-full max-w-[1600px] border-x border-gray-200 dark:border-[#1f1f1f] flex flex-col min-h-screen"
    >
      <HomeHeroHeader />
      <HomeIntegrationsBar />

      <main class="flex-1 flex flex-col p-6 lg:p-10">
        <h2 class="text-2xl font-bold text-primary-500 mb-8 font-mono">Find Skills</h2>

        <HomeTagFilterBar
          v-model="selectedTagIds"
          :tags="websiteTags"
          :loading="isTagLoading"
        />

        <div class="flex flex-col lg:flex-row gap-10 lg:gap-16">
          <aside class="w-full lg:w-64 flex-shrink-0 font-mono text-sm">
            <div class="block lg:hidden mb-6">
              <button
                type="button"
                class="w-full flex items-center justify-between bg-white dark:bg-[#0a0a0a] border border-gray-300 dark:border-gray-800 text-gray-900 dark:text-gray-300 p-3 rounded-md outline-none focus:border-primary-500 cursor-pointer"
                @click="mobileCategoryDrawerOpen = true"
              >
                <span class="truncate">
                  {{ categories.find((item) => item.id === activeCategoryId)?.name || '选择分类' }}
                  <span class="ml-2 text-xs text-gray-500"
                    >({{
                      categories.find((item) => item.id === activeCategoryId)?.count || 0
                    }})</span
                  >
                </span>
                <svg
                  class="w-4 h-4 text-gray-500"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>

              <Drawer
                :visible="mobileCategoryDrawerOpen"
                @update:visible="mobileCategoryDrawerOpen = $event"
                position="left"
                class="bg-white dark:bg-black border-r border-gray-200 dark:border-[#1f1f1f] !w-[280px] flex flex-col h-full"
                :pt="{
                  mask: { class: 'fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm' },
                  header: {
                    class:
                      'px-4 py-4 border-b border-gray-100 dark:border-[#1f1f1f] flex items-center justify-between shrink-0 box-border',
                  },
                  title: { class: 'text-lg font-bold text-gray-900 dark:text-white font-mono' },
                  closeButton: {
                    class:
                      'flex items-center justify-center w-8 h-8 rounded-full bg-gray-100 dark:bg-white/10 text-gray-500 hover:bg-gray-200 dark:hover:bg-white/20 transition-colors cursor-pointer shrink-0',
                  },
                  content: { class: 'p-0 overflow-y-auto flex-1' },
                }"
              >
                <template #header>
                  <span class="text-base font-bold text-gray-900 dark:text-gray-100 font-mono"
                    >Category Filter</span
                  >
                </template>
                <ul class="flex flex-col py-2">
                  <li v-for="cat in categories" :key="cat.id">
                    <button
                      class="w-full cursor-pointer flex justify-between items-center py-3 px-5 transition-colors"
                      :class="
                        activeCategoryId === cat.id
                          ? 'bg-primary-50 text-primary-600 dark:bg-primary-500/10 dark:text-primary-400'
                          : 'text-gray-600 hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-white/5'
                      "
                      @click="
                        activeCategoryId = cat.id;
                        mobileCategoryDrawerOpen = false;
                      "
                    >
                      <span :class="{ 'font-bold': activeCategoryId === cat.id }">{{
                        cat.name
                      }}</span>
                      <span class="text-xs opacity-70">{{ cat.count }}</span>
                    </button>
                  </li>
                </ul>
              </Drawer>
            </div>

            <ul class="hidden lg:flex flex-col gap-1">
              <li v-for="cat in categories" :key="cat.id">
                <button
                  class="w-full cursor-pointer flex justify-between items-center py-2 px-3 border-b transition-colors"
                  :class="
                    activeCategoryId === cat.id
                      ? 'border-gray-900 text-gray-900 bg-gray-100 dark:border-white dark:text-white dark:bg-white/5'
                      : 'border-transparent text-gray-500 hover:text-gray-800 hover:bg-gray-50 dark:hover:text-gray-300 dark:hover:bg-white/5'
                  "
                  @click="activeCategoryId = cat.id"
                >
                  <span :class="activeCategoryId === cat.id ? 'font-bold' : ''">{{
                    cat.name
                  }}</span>
                  <span>{{ cat.count }}</span>
                </button>
              </li>
            </ul>
          </aside>

          <section class="flex-1 flex flex-col">
            <div
              class="flex flex-col sm:flex-row justify-between items-start sm:items-center pb-4 border-b border-gray-200 dark:border-[#1f1f1f] mb-6 gap-4 font-mono text-sm text-gray-500 dark:text-gray-400"
            >
              <div
                class="flex items-center gap-3 w-full sm:w-1/2 bg-white dark:bg-transparent px-3 sm:px-0 py-2 sm:py-0 border sm:border-none border-gray-200 dark:border-transparent rounded sm:rounded-none"
              >
                <svg
                  class="w-4 h-4 flex-shrink-0"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
                <InputText
                  v-model="searchQuery"
                  placeholder="Search all skills"
                  class="bg-transparent border-none outline-none w-full text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-600 focus:ring-0"
                />
              </div>
              <div class="flex items-center gap-6 w-full sm:w-auto justify-between sm:justify-end">
                <button
                  class="cursor-pointer hover:text-gray-900 dark:hover:text-white transition-colors flex items-center gap-1 text-gray-900 dark:text-white"
                >
                  DOWNLOADS ↓
                </button>
                <button
                  class="cursor-pointer hover:text-gray-900 dark:hover:text-white transition-colors"
                >
                  STARS
                </button>
              </div>
            </div>

            <div
              v-if="isLoading"
              class="py-20 text-center font-mono text-gray-500 dark:text-gray-400"
            >
              <p>Loading website resources...</p>
            </div>

            <div
              v-else-if="loadError"
              class="py-20 text-center font-mono text-red-500 dark:text-red-400"
            >
              <p>{{ loadError }}</p>
            </div>

            <div v-else-if="resources.length > 0" class="flex flex-col gap-6">
              <div class="grid grid-cols-1 xl:grid-cols-2 gap-4">
                <WebsiteResourceCard
                  v-for="(item, index) in resources"
                  :key="item.id"
                  :item="item"
                  :rank-index="(currentPage - 1) * pageSize + index + 1"
                />
              </div>
            </div>

            <div v-else class="py-20 text-center font-mono text-gray-500 dark:text-gray-400">
              <p>No website resources found matching your criteria.</p>
            </div>

            <div v-if="!isLoading && !loadError">
              <AppPagination
                :currentPage="currentPage"
                @update:currentPage="currentPage = $event"
                :totalPages="totalPages"
              />
            </div>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.home-v2-page {
  font-family: 'Inter', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.home-v2-page.dark-gradient {
  background-image: repeating-linear-gradient(
    to bottom,
    #000000,
    #000000 2px,
    #050505 2px,
    #050505 4px
  ) !important;
}

.home-v2-page::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.home-v2-page::-webkit-scrollbar-track {
  background: transparent;
  border-left: 1px solid rgba(128, 128, 128, 0.2);
}

.home-v2-page::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 0;
}
.dark .home-v2-page::-webkit-scrollbar-thumb {
  background: #333;
}

.home-v2-page::-webkit-scrollbar-thumb:hover {
  background: #f59e0b;
}
</style>
