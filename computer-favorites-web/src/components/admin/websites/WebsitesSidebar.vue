<script setup lang="ts">

const props = defineProps<{
    categories: Array<{
        name: string;
        count: string;
        icon: string;
        active: boolean;
    }>
}>()
</script>

<template>
  <aside class="w-full md:w-64 flex-shrink-0 hidden md:block">
    <div class="sticky top-24">
      <div class="relative mb-6">
        <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
        <input type="text" placeholder="Search categories..." class="w-full bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border text-gray-900 dark:text-gray-200 text-sm rounded-lg focus:ring-brand-orange focus:border-brand-orange block pl-10 p-2.5 outline-none transition-colors">
      </div>

      <nav class="space-y-1">
        <template v-for="(category, index) in categories" :key="index">
          <a href="#"
             class="flex items-center justify-between px-3 py-2 text-sm font-medium rounded-md group transition-colors cursor-pointer"
             :class="category.active ? 'text-brand-orange bg-orange-50 dark:bg-orange-900/10 border-l-2 border-brand-orange' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-card hover:text-gray-900 dark:hover:text-gray-200 border-l-2 border-transparent'">
            <div class="flex items-center gap-3">
              <i :class="[category.icon, category.active ? 'text-brand-orange' : 'text-gray-400 group-hover:text-gray-500']" class="w-5 text-center"></i>
              {{ category.name }}
            </div>
            <span :class="category.active ? 'text-brand-orange' : 'text-gray-400 dark:text-gray-500'" class="text-xs">{{ category.count }}</span>
          </a>
        </template>
      </nav>
    </div>
  </aside>
</template>

