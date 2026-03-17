<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWebsiteStore } from '@/stores/website'
import type { FilterLogic, Website } from '@/types/website'
import HeroSection from '@/components/user/HeroSection.vue'
import WebsiteFilter from '@/components/user/WebsiteFilter.vue'
import WebsiteGrid from '@/components/user/WebsiteGrid.vue'

defineOptions({
  name: 'HomeView',
})

const websiteStore = useWebsiteStore()

const searchQuery = ref('')
const activeCategories = ref<string[]>([])
const filterLogic = ref<FilterLogic>('OR')

const filteredWebsites = computed(() => {
  let result = websiteStore.websites

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (site) =>
        site.title.toLowerCase().includes(query) || site.desc.toLowerCase().includes(query),
    )
  }

  if (activeCategories.value.length > 0) {
    if (filterLogic.value === 'OR') {
      result = result.filter((site) => activeCategories.value.includes(site.tag))
    } else {
      result = result.filter((site) => activeCategories.value.every((cat) => site.tag === cat))
    }
  }

  return result
})

const handleCopyLink = (site: Website) => {
  if (site.url) {
    navigator.clipboard.writeText(site.url)
    alert('链接已复制到剪贴板')
  } else {
    alert('该网站暂无链接')
  }
}

const handleVisit = (site: Website) => {
  if (site.url) {
    window.open(site.url, '_blank')
  } else {
    alert('该网站暂无链接')
  }
}

const handleUpload = () => {
  alert('上传功能即将上线')
}

const handleBrowse = () => {
  alert('浏览热门功能即将上线')
}
</script>

<template>
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <HeroSection @upload-click="handleUpload" @browse-click="handleBrowse" />

    <WebsiteFilter
      :categories="websiteStore.categories"
      v-model="activeCategories"
      v-model:logic="filterLogic"
    />

    <WebsiteGrid
      :websites="filteredWebsites"
      v-model:search-query="searchQuery"
      @copy-link="handleCopyLink"
      @visit="handleVisit"
    />
  </main>
</template>
