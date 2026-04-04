<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import HomeHeroHeader from '@/components/user/HomeHeroHeader.vue'
import HomeIntegrationsBar from '@/components/user/HomeIntegrationsBar.vue'
import { getWebsiteCategories, getWebsitePage } from '@/api/website'
import type { PublicWebsiteListItem } from '@/types/public-website'

defineOptions({
  name: 'HomeView',
})

interface CategoryItem {
  id: number
  name: string
  count: number
}

interface ResourceTag {
  label: string
  colorClass: string
}

interface ResourceItem {
  id: number
  name: string
  desc: string
  downloads: string
  stars: string
  category: string
  categoryId: number
  url: string
  tags: ResourceTag[]
}

const isDark = ref(false)
const isLoading = ref(false)
const loadError = ref('')
const searchQuery = ref('')
const activeCategoryId = ref(0)
const categories = ref<CategoryItem[]>([{ id: 0, name: 'All', count: 0 }])
const resources = ref<ResourceItem[]>([])

const tagColors = [
  'bg-blue-100 text-blue-700 border-blue-300 dark:bg-blue-500/10 dark:text-blue-400 dark:border-blue-500/50',
  'bg-emerald-100 text-emerald-700 border-emerald-300 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/50',
  'bg-cyan-100 text-cyan-700 border-cyan-300 dark:bg-cyan-500/10 dark:text-cyan-400 dark:border-cyan-500/50',
  'bg-amber-100 text-amber-700 border-amber-300 dark:bg-amber-500/10 dark:text-amber-400 dark:border-amber-500/50',
  'bg-lime-100 text-lime-700 border-lime-300 dark:bg-lime-500/10 dark:text-lime-400 dark:border-lime-500/50',
  'bg-rose-100 text-rose-700 border-rose-300 dark:bg-rose-500/10 dark:text-rose-400 dark:border-rose-500/50',
  'bg-slate-100 text-slate-700 border-slate-300 dark:bg-slate-500/10 dark:text-slate-300 dark:border-slate-500/50',
] as const

const categorySelectPt = {
  root: {
    class: 'w-full',
  },
  label: {
    class: 'truncate px-3 py-2 text-sm text-gray-900 dark:text-gray-200',
  },
  dropdown: {
    class: 'cursor-pointer px-3 text-gray-500 dark:text-gray-400',
  },
  overlay: {
    class: 'z-[80] mt-1 rounded-md border border-gray-200 bg-white shadow-lg dark:border-gray-800 dark:bg-[#0a0a0a]',
  },
  list: {
    class: 'py-1',
  },
  option: {
    class: 'cursor-pointer px-3 py-2 text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-white/10',
  },
}

let darkModeObserver: MutationObserver | null = null

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const updateDarkModeState = () => {
  isDark.value = document.documentElement.classList.contains('dark')
    || document.body.classList.contains('dark')
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
    category: item.categoryName?.trim() || 'Uncategorized',
    categoryId: item.categoryId ?? 0,
    url: item.url,
    tags: (item.tags || []).map((label, index) => ({
      label,
      colorClass: tagColors[(item.id + index) % tagColors.length] ?? tagColors[0],
    })),
  }
}

const loadWebsiteData = async () => {
  isLoading.value = true
  loadError.value = ''
  try {
    const [pageResult, categoryResult] = await Promise.all([
      getWebsitePage({ pageNum: 1, pageSize: 60 }),
      getWebsiteCategories(),
    ])

    resources.value = pageResult.records.map(mapWebsiteToResource)

    const mappedCategories: CategoryItem[] = categoryResult.map((category) => ({
      id: category.id,
      name: category.name,
      count: Number(category.count || 0),
    }))
    const allCount = mappedCategories.reduce((sum, item) => sum + item.count, 0)
    categories.value = [{ id: 0, name: 'All', count: allCount }, ...mappedCategories]
  } catch (error) {
    resources.value = []
    categories.value = [{ id: 0, name: 'All', count: 0 }]
    loadError.value = resolveErrorMessage(error, 'Failed to load website resources.')
  } finally {
    isLoading.value = false
  }
}

const filteredResources = computed(() => {
  let result = resources.value

  if (activeCategoryId.value > 0) {
    result = result.filter((item) => item.categoryId === activeCategoryId.value)
  }

  const keyword = searchQuery.value.trim().toLowerCase()
  if (keyword) {
    result = result.filter(
      (item) => item.name.toLowerCase().includes(keyword)
        || item.desc.toLowerCase().includes(keyword)
        || item.category.toLowerCase().includes(keyword),
    )
  }

  return result
})

onMounted(() => {
  updateDarkModeState()
  darkModeObserver = new MutationObserver(updateDarkModeState)
  darkModeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
  darkModeObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] })
  void loadWebsiteData()
})

onUnmounted(() => {
  darkModeObserver?.disconnect()
  darkModeObserver = null
})
</script>

<template>
  <div class="home-v2-page min-h-screen bg-gray-50 dark:bg-black text-gray-900 dark:text-[#e2e8f0] selection:bg-primary-500 selection:text-black flex justify-center transition-colors duration-300 subpixel-antialiased"
       :class="{'dark-gradient': isDark}"
       :style="!isDark ? 'background-image: repeating-linear-gradient(to bottom, transparent, transparent 2px, rgba(0,0,0,0.02) 2px, rgba(0,0,0,0.02) 4px);' : ''">
    <div class="w-full max-w-[1600px] border-x border-gray-200 dark:border-[#1f1f1f] flex flex-col min-h-screen">
      <HomeHeroHeader />
      <HomeIntegrationsBar />

      <main class="flex-1 flex flex-col p-6 lg:p-10">
        <h2 class="text-2xl font-bold text-primary-500 mb-8 font-mono">Find Skills</h2>

        <div class="flex flex-col lg:flex-row gap-10 lg:gap-16">
          <aside class="w-full lg:w-64 flex-shrink-0 font-mono text-sm">
            <div class="block lg:hidden mb-6">
              <Select
                v-model="activeCategoryId"
                :options="categories"
                optionLabel="name"
                optionValue="id"
                appendTo="self"
                class="w-full rounded-md border border-gray-300 bg-white dark:border-gray-800 dark:bg-[#0a0a0a]"
                :pt="categorySelectPt"
              >
                <template #value="slotProps">
                  <span v-if="slotProps.value == null" class="truncate text-sm text-gray-500 dark:text-gray-400">
                    选择分类
                  </span>
                  <span v-else class="truncate">
                    {{ categories.find((item) => item.id === slotProps.value)?.name }}
                  </span>
                </template>
                <template #option="slotProps">
                  <div class="flex items-center justify-between gap-3">
                    <span class="truncate">{{ slotProps.option.name }}</span>
                    <span class="shrink-0 text-xs text-gray-400">{{ slotProps.option.count }}</span>
                  </div>
                </template>
              </Select>
            </div>

            <ul class="hidden lg:flex flex-col gap-1">
              <li v-for="cat in categories" :key="cat.id">
                <button
                  class="w-full cursor-pointer flex justify-between items-center py-2 px-3 border-b transition-colors"
                  :class="activeCategoryId === cat.id ? 'border-gray-900 text-gray-900 bg-gray-100 dark:border-white dark:text-white dark:bg-white/5' : 'border-transparent text-gray-500 hover:text-gray-800 hover:bg-gray-50 dark:hover:text-gray-300 dark:hover:bg-white/5'"
                  @click="activeCategoryId = cat.id"
                >
                  <span :class="activeCategoryId === cat.id ? 'font-bold' : ''">{{ cat.name }}</span>
                  <span>{{ cat.count }}</span>
                </button>
              </li>
            </ul>
          </aside>

          <section class="flex-1 flex flex-col">
            <div
              class="flex flex-col sm:flex-row justify-between items-start sm:items-center pb-4 border-b border-gray-200 dark:border-[#1f1f1f] mb-6 gap-4 font-mono text-sm text-gray-500 dark:text-gray-400"
            >
              <div class="flex items-center gap-3 w-full sm:w-1/2 bg-white dark:bg-transparent px-3 sm:px-0 py-2 sm:py-0 border sm:border-none border-gray-200 dark:border-transparent rounded sm:rounded-none">
                <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <button class="cursor-pointer hover:text-gray-900 dark:hover:text-white transition-colors flex items-center gap-1 text-gray-900 dark:text-white">DOWNLOADS ↓</button>
                <button class="cursor-pointer hover:text-gray-900 dark:hover:text-white transition-colors">STARS</button>
              </div>
            </div>

            <div v-if="isLoading" class="py-20 text-center font-mono text-gray-500 dark:text-gray-400">
              <p>Loading website resources...</p>
            </div>

            <div v-else-if="loadError" class="py-20 text-center font-mono text-red-500 dark:text-red-400">
              <p>{{ loadError }}</p>
            </div>

            <div v-else-if="filteredResources.length > 0" class="grid grid-cols-1 xl:grid-cols-2 gap-4">
              <a
                v-for="(item, index) in filteredResources"
                :key="item.id"
                :href="item.url"
                target="_blank"
                rel="noopener noreferrer"
                class="group relative block cursor-pointer p-5 transition-all duration-200 hover:-translate-y-0.5 bg-white hover:bg-gray-50 dark:bg-[#062016]/60 dark:hover:bg-[#0a3324] border border-gray-200 dark:border-transparent backdrop-blur-sm overflow-hidden"
              >
                <div class="flex gap-4">
                  <div class="text-xs font-mono text-gray-400 dark:text-gray-600 mt-1 w-4 text-right flex-shrink-0">{{ index + 1 }}</div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-baseline gap-2 mb-1">
                      <h3 class="text-base sm:text-lg font-bold text-gray-900 dark:text-gray-200 group-hover:text-primary-500 dark:group-hover:text-white transition-colors font-mono truncate">
                        {{ item.name }}
                      </h3>
                    </div>
                    <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 leading-relaxed mb-4">{{ item.desc }}</p>

                    <div class="flex flex-wrap gap-2">
                      <span
                        v-for="tag in item.tags"
                        :key="tag.label"
                        class="px-2 py-0.5 text-[10px] font-mono rounded-sm border-l-2"
                        :class="tag.colorClass"
                      >
                        {{ tag.label }}
                      </span>
                    </div>
                  </div>

                  <div class="flex flex-col items-end gap-2 text-xs font-mono flex-shrink-0">
                    <div class="flex flex-col sm:flex-row items-end sm:items-center gap-1 sm:gap-4 text-gray-500 dark:text-gray-300">
                      <span class="font-bold group-hover:text-primary-500 transition-colors sm:w-12 text-right">
                        {{ item.downloads }}
                      </span>
                      <span class="font-bold sm:w-10 text-right">{{ item.stars }}</span>
                    </div>
                    <span class="px-2 py-0.5 bg-green-100 dark:bg-[#104d39]/30 text-[10px] text-green-700 dark:text-gray-500 rounded-sm mt-auto">
                      {{ item.category }}
                    </span>
                  </div>
                </div>

                <div class="absolute bottom-0 left-0 h-[2px] bg-primary-500 w-0 group-hover:w-full transition-all duration-300" />
              </a>
            </div>

            <div v-else class="py-20 text-center font-mono text-gray-500 dark:text-gray-400">
              <p>No website resources found matching your criteria.</p>
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
  background-image: repeating-linear-gradient(to bottom, #000000, #000000 2px, #050505 2px, #050505 4px) !important;
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
