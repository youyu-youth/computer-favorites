<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useWebsiteStore } from '@/stores/website'
import { useI18n } from 'vue-i18n'
import type { FilterLogic, Website } from '@/types/website'
import HeroSection from '@/components/user/HeroSection.vue'
import WebsiteFilter from '@/components/user/WebsiteFilter.vue'
import WebsiteGrid from '@/components/user/WebsiteGrid.vue'

defineOptions({
  name: 'HomeView',
})

const websiteStore = useWebsiteStore()
const { t } = useI18n()

const searchQuery = ref('')
const activeCategories = ref<string[]>([])
const filterLogic = ref<FilterLogic>('OR')
const showRegisterSuccessDialog = ref(false)
const registerSuccessTitle = ref('')
const registerSuccessDescription = ref('')

const filteredWebsites = computed(() => {
  let result = websiteStore.websites

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (site) => site.title.toLowerCase().includes(query) || site.desc.toLowerCase().includes(query),
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
    alert(t('common.copy.success'))
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

const closeRegisterSuccessDialog = () => {
  showRegisterSuccessDialog.value = false
}

onMounted(() => {
  const messageRaw = sessionStorage.getItem('registerSuccessMessage')
  if (!messageRaw) {
    return
  }
  sessionStorage.removeItem('registerSuccessMessage')
  try {
    const message = JSON.parse(messageRaw) as { title?: string; description?: string }
    registerSuccessTitle.value = message.title || t('auth.login.registerSuccess')
    registerSuccessDescription.value = message.description || t('auth.login.subtitle')
  } catch {
    registerSuccessTitle.value = t('auth.login.registerSuccess')
    registerSuccessDescription.value = t('auth.login.subtitle')
  }
  showRegisterSuccessDialog.value = true
})
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
  <div
    v-if="showRegisterSuccessDialog"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/35 px-4"
  >
    <div
      class="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-xl dark:border-white/10 dark:bg-zinc-900"
    >
      <div class="text-xl font-semibold text-slate-900 dark:text-white">
        {{ registerSuccessTitle }}
      </div>
      <div class="mt-3 text-sm text-slate-600 dark:text-slate-400">
        {{ registerSuccessDescription }}
      </div>
      <button
        type="button"
        class="mt-6 inline-flex h-10 items-center justify-center rounded-lg bg-emerald-700 px-4 text-sm font-semibold text-white transition-colors hover:bg-emerald-800 dark:bg-emerald-600 dark:hover:bg-emerald-700"
        @click="closeRegisterSuccessDialog"
      >
        {{ t('common.confirm') }}
      </button>
    </div>
  </div>
</template>
