<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'

import HomeHeroHeader from '@/components/user/HomeHeroHeader.vue'
import HomeIntegrationsBar from '@/components/user/HomeIntegrationsBar.vue'

defineOptions({
  name: 'HomeView',
})

const isDark = ref(false);

onMounted(() => {
  const checkDarkMode = () => {
    isDark.value = document.documentElement.classList.contains('dark') || document.body.classList.contains('dark');
  };
  checkDarkMode();
  const observer = new MutationObserver(checkDarkMode);
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
  observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
  onUnmounted(() => observer.disconnect());
});

interface CategoryItem {
  name: string
  count: string
}

interface RawResourceItem {
  id: number
  name: string
  author: string
  desc: string
  downloads: string
  stars: string
  category: string
  labels: string[]
}

interface ResourceTag {
  label: string
  colorClass: string
}

interface ResourceItem {
  id: number
  name: string
  author: string
  desc: string
  downloads: string
  stars: string
  category: string
  tags: ResourceTag[]
}

const searchQuery = ref('')
const activeCategory = ref('All')

const tagColors = [
  'bg-blue-100 text-blue-700 border-blue-300 dark:bg-blue-500/10 dark:text-blue-400 dark:border-blue-500/50',
  'bg-emerald-100 text-emerald-700 border-emerald-300 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/50',
  'bg-purple-100 text-purple-700 border-purple-300 dark:bg-purple-500/10 dark:text-purple-400 dark:border-purple-500/50',
  'bg-pink-100 text-pink-700 border-pink-300 dark:bg-pink-500/10 dark:text-pink-400 dark:border-pink-500/50',
  'bg-cyan-100 text-cyan-700 border-cyan-300 dark:bg-cyan-500/10 dark:text-cyan-400 dark:border-cyan-500/50',
  'bg-amber-100 text-amber-700 border-amber-300 dark:bg-amber-500/10 dark:text-amber-400 dark:border-amber-500/50',
  'bg-rose-100 text-rose-700 border-rose-300 dark:bg-rose-500/10 dark:text-rose-400 dark:border-rose-500/50'
] as const

const categories = ref<CategoryItem[]>([
  { name: 'All', count: '5147' },
  { name: 'Coding Agents & IDEs', count: '1160' },
  { name: 'Web & Frontend Development', count: '903' },
  { name: 'DevOps & Cloud', count: '375' },
  { name: 'Search & Research', count: '342' },
  { name: 'Browser & Automation', count: '306' },
  { name: 'Productivity & Tasks', count: '201' },
  { name: 'CLI Utilities', count: '170' },
  { name: 'AI & LLMs', count: '160' },
])

const rawResources: RawResourceItem[] = [
  {
    id: 1,
    name: 'Agent Browser',
    author: 'thesethrose/skills',
    desc: 'A fast Rust-based headless browser automation CLI.',
    downloads: '137.7k',
    stars: '597',
    category: 'Browser & Automation',
    labels: ['Rust', 'CLI', 'V8'],
  },
  {
    id: 2,
    name: 'gog',
    author: 'steipete/skills',
    desc: 'Google Workspace CLI for Gmail, Calendar, Drive, Contacts, Sheets, and Docs.',
    downloads: '117.1k',
    stars: '745',
    category: 'Productivity & Tasks',
    labels: ['Google', 'OAuth', 'Productivity'],
  },
  {
    id: 3,
    name: 'auto-updater',
    author: 'maximeprades/skills',
    desc: 'Automatically update Clawdbot and all installed skills once.',
    downloads: '46.8k',
    stars: '282',
    category: 'CLI Utilities',
    labels: ['Auto', 'System', 'Shell'],
  },
  {
    id: 4,
    name: 'api-gateway',
    author: 'byungkyu/skills',
    desc: 'API gateway for calling third-party APIs with managed auth.',
    downloads: '45.7k',
    stars: '225',
    category: 'DevOps & Cloud',
    labels: ['API', 'Auth', 'Proxy'],
  },
  {
    id: 5,
    name: 'baidu-search',
    author: 'ide-rea/skills',
    desc: 'Search the web using Baidu AI Search Engine (BDSE).',
    downloads: '45.2k',
    stars: '112',
    category: 'Search & Research',
    labels: ['Search', 'AI', 'Web'],
  },
  {
    id: 6,
    name: 'automation-workflows',
    author: 'jk-0001/skills',
    desc: 'Design and implement automation workflows to save time.',
    downloads: '41.9k',
    stars: '182',
    category: 'Coding Agents & IDEs',
    labels: ['Workflow', 'Efficiency'],
  },
  {
    id: 7,
    name: 'free-ride',
    author: 'shaivpidadi/skills',
    desc: 'Manages free AI models from OpenRouter for OpenClaw.',
    downloads: '37.1k',
    stars: '277',
    category: 'AI & LLMs',
    labels: ['LLM', 'Router', 'Free'],
  },
  {
    id: 8,
    name: 'vue-setup',
    author: 'frontend-guru/skills',
    desc: 'Scaffold a modern Vue 3 application with Tailwind and Vite pre-configured.',
    downloads: '35.0k',
    stars: '450',
    category: 'Web & Frontend Development',
    labels: ['Vue3', 'Tailwind', 'Vite'],
  },
  {
    id: 9,
    name: 'k8s-deploy',
    author: 'cloud-ninja/skills',
    desc: 'One-click deployment script for Kubernetes clusters.',
    downloads: '30.2k',
    stars: '320',
    category: 'DevOps & Cloud',
    labels: ['K8s', 'Cloud', 'Ops'],
  },
  {
    id: 10,
    name: 'prompt-gen',
    author: 'ai-master/skills',
    desc: 'Generate high-quality prompts for various LLMs automatically.',
    downloads: '28.5k',
    stars: '510',
    category: 'AI & LLMs',
    labels: ['Prompt', 'Engineering'],
  },
]

const resources = ref<ResourceItem[]>(
  rawResources.map((resource) => ({
    id: resource.id,
    name: resource.name,
    author: resource.author,
    desc: resource.desc,
    downloads: resource.downloads,
    stars: resource.stars,
    category: resource.category,
    tags: resource.labels.map((label, index) => ({
      label,
      colorClass: tagColors[(resource.id + index) % tagColors.length] ?? tagColors[0],
    })),
  })),
)

const filteredResources = computed(() => {
  let result = resources.value

  if (activeCategory.value !== 'All') {
    result = result.filter((item) => item.category === activeCategory.value)
  }

  const keyword = searchQuery.value.trim().toLowerCase()
  if (keyword) {
    result = result.filter(
      (item) =>
        item.name.toLowerCase().includes(keyword)
        || item.desc.toLowerCase().includes(keyword)
        || item.author.toLowerCase().includes(keyword),
    )
  }

  return result
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
              <select
                v-model="activeCategory"
                class="w-full bg-white dark:bg-[#0a0a0a] border border-gray-300 dark:border-gray-800 text-gray-900 dark:text-gray-300 p-3 rounded-md outline-none focus:border-primary-500"
              >
                <option v-for="cat in categories" :key="cat.name" :value="cat.name">
                  {{ cat.name }} ({{ cat.count }})
                </option>
              </select>
            </div>

            <ul class="hidden lg:flex flex-col gap-1">
              <li v-for="cat in categories" :key="cat.name">
                <button
                  class="w-full flex justify-between items-center py-2 px-3 border-b transition-colors"
                  :class="activeCategory === cat.name ? 'border-gray-900 text-gray-900 bg-gray-100 dark:border-white dark:text-white dark:bg-white/5' : 'border-transparent text-gray-500 hover:text-gray-800 hover:bg-gray-50 dark:hover:text-gray-300 dark:hover:bg-white/5'"
                  @click="activeCategory = cat.name"
                >
                  <span :class="activeCategory === cat.name ? 'font-bold' : ''">{{ cat.name }}</span>
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
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="Search all skills"
                  class="bg-transparent border-none outline-none w-full text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-600 focus:ring-0"
                />
              </div>
              <div class="flex items-center gap-6 w-full sm:w-auto justify-between sm:justify-end">
                <button class="hover:text-gray-900 dark:hover:text-white transition-colors flex items-center gap-1 text-gray-900 dark:text-white">DOWNLOADS ↓</button>
                <button class="hover:text-gray-900 dark:hover:text-white transition-colors">STARS</button>
              </div>
            </div>

            <div v-if="filteredResources.length > 0" class="grid grid-cols-1 xl:grid-cols-2 gap-4">
              <a
                v-for="(item, index) in filteredResources"
                :key="item.id"
                href="#"
                class="group relative block p-5 transition-all duration-200 hover:-translate-y-0.5 bg-white hover:bg-gray-50 dark:bg-[#062016]/60 dark:hover:bg-[#0a3324] border border-gray-200 dark:border-transparent backdrop-blur-sm overflow-hidden"
              >
                <div class="flex gap-4">
                  <div class="text-xs font-mono text-gray-400 dark:text-gray-600 mt-1 w-4 text-right flex-shrink-0">{{ index + 1 }}</div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-baseline gap-2 mb-1 flex-wrap">
                      <h3 class="text-base sm:text-lg font-bold text-gray-900 dark:text-gray-200 group-hover:text-primary-500 dark:group-hover:text-white transition-colors font-mono truncate">
                        {{ item.name }}
                      </h3>
                      <span class="text-[10px] sm:text-xs text-primary-500/80 dark:text-primary-500/70 font-mono truncate">{{ item.author }}</span>
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
              <p>No skills found matching your criteria.</p>
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
