<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import HomeHeroSection from '@/components/user/home/HomeHeroSection.vue'
import HomeFilterBar from '@/components/user/home/HomeFilterBar.vue'
import HomeResourceGrid from '@/components/user/home/HomeResourceGrid.vue'
import type { HomeResourceItem } from '@/components/user/home/types'

// 首页组合层：负责状态与组件编排
const categories = ['All Treasures', 'Frontend', 'Infrastructure', 'AI/ML Models', 'Security Labs']

const allCards: HomeResourceItem[] = [
  {
    id: 'card-1',
    category: 'Infrastructure',
    icon: 'hub',
    title: 'Zero-Knowledge Proofs in Distributed Systems',
    desc: 'In-depth guide to privacy-first infrastructure using modern cryptographic primitives and Rust patterns.',
    tag1: 'Backend',
    tag2: 'Crypto',
    author: 'Alex Chen',
    stars: '1.2k',
    bookmarks: '482',
  },
  {
    id: 'card-2',
    category: 'Infrastructure',
    icon: 'terminal',
    title: 'Bash Productivity Hacks for Kernel Contribs',
    desc: 'Automate your patch submission workflow with these 12 hidden scripts used by top maintainers.',
    tag1: 'Sysadmin',
    tag2: 'Linux',
    author: 'Marcus Thorne',
    stars: '892',
    bookmarks: '156',
  },
  {
    id: 'card-3',
    category: 'Frontend',
    icon: 'javascript',
    title: 'Web-Assembly Boilerplate for 2024',
    desc: 'The ultimate starting point for high-performance web applications using Rust and WASM-bindgen.',
    tag1: 'Frontend',
    tag2: 'Performance',
    author: 'Elena V.',
    stars: '2.1k',
    bookmarks: '930',
  },
  {
    id: 'card-4',
    category: 'AI/ML Models',
    icon: 'psychology',
    title: 'Mastering Transformer Models: Visual Guide',
    desc: 'A visual breakdown of self-attention mechanisms and multi-head attention in modern LLMs.',
    tag1: 'AI/ML',
    tag2: 'Education',
    author: 'Sarah Jenkins',
    stars: '3.4k',
    bookmarks: '1.1k',
  },
  {
    id: 'card-5',
    category: 'Security Labs',
    icon: 'shield',
    title: 'Post-Quantum Cryptography Prep',
    desc: 'Implementing NIST-approved quantum-resistant algorithms in your existing security stack today.',
    tag1: 'Security',
    tag2: 'Advanced',
    author: 'Dr. Aris',
    stars: '561',
    bookmarks: '224',
  },
  {
    id: 'card-6',
    category: 'Infrastructure',
    icon: 'settings_ethernet',
    title: 'Infrastructure as Code: 2024 Patterns',
    desc: 'Evolution of IaC from Terraform to Crossplane and Pulumi. Best practices for platform engineering.',
    tag1: 'Cloud',
    tag2: 'DevOps',
    author: 'Liam Rossi',
    stars: '1.5k',
    bookmarks: '670',
  },
]

const selectedCategory = shallowRef<string>('All Treasures')
const currentPage = shallowRef(1)
const pageSize = 6

const filteredCards = computed(() => {
  if (selectedCategory.value === 'All Treasures') {
    return allCards
  }
  return allCards.filter((card) => card.category === selectedCategory.value)
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredCards.value.length / pageSize)))

const pagedCards = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredCards.value.slice(start, start + pageSize)
})

watch(selectedCategory, () => {
  currentPage.value = 1
})

watch(totalPages, (value) => {
  if (currentPage.value > value) {
    currentPage.value = value
  }
}, { immediate: true })

const handleCategoryChange = (category: string) => {
  selectedCategory.value = category
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}
</script>

<template>
  <div class="min-h-screen bg-surface-page font-body text-text-primary selection:bg-primary-500 selection:text-white">
    <main class="mx-auto w-full max-w-7xl px-4 py-14 sm:px-6 sm:py-20 lg:px-8 lg:py-24">
      <HomeHeroSection />
      <HomeFilterBar
        :categories="categories"
        :model-value="selectedCategory"
        @update:model-value="handleCategoryChange"
      />
      <HomeResourceGrid
        :items="pagedCards"
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
      />
    </main>
  </div>
</template>
