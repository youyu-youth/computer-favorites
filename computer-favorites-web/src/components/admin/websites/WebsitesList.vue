<script setup lang="ts">

const props = defineProps<{
    servers: Array<any>;
    viewMode: string;
}>()
</script>

<template>
  <transition-group
    tag="div"
    :class="[
        'transition-all duration-300',
        viewMode === 'grid'
            ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-6'
            : 'flex flex-col gap-4'
    ]"
    name="list">

    <!-- 服务器卡片列表 -->
    <div v-for="server in servers" :key="server.title"
         :class="[
             'bg-white dark:bg-[#1a2126] border border-gray-200 dark:border-dark-border rounded-xl overflow-hidden hover:border-gray-300 dark:hover:border-gray-600 transition-all hover:shadow-md dark:shadow-none',
             viewMode === 'list' ? 'flex flex-col sm:flex-row items-stretch' : 'flex flex-col p-5'
         ]">

        <!-- 列表视图图标区 -->
        <div v-if="viewMode === 'list'" class="p-4 sm:pr-0 flex items-center justify-center sm:justify-start">
            <div class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0" :class="server.iconBg">
                <i :class="server.icon" class="text-white text-xl"></i>
            </div>
        </div>

        <!-- 卡片内容 -->
        <div :class="[viewMode === 'list' ? 'flex-grow p-4' : '']">
            <!-- 头部：图标、标题、操作 -->
            <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                    <!-- 网格视图图标 -->
                    <div v-if="viewMode === 'grid'" class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0" :class="server.iconBg">
                        <i :class="server.icon" class="text-white text-lg"></i>
                    </div>
                    <div>
                        <div class="flex items-center gap-2">
                            <a href="#" class="text-brand-blue font-medium hover:underline">{{ server.title }}</a>
                            <span v-if="server.isOfficial" class="px-1.5 py-0.5 rounded text-[10px] font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-brand-blue border border-blue-200 dark:border-blue-800/50">official</span>
                        </div>
                        <div class="text-gray-500 dark:text-gray-500 text-xs">{{ server.author }}</div>
                    </div>
                </div>
                <div class="flex items-center gap-2 text-gray-400 dark:text-gray-500">
                    <button class="hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer" title="Copy"><i class="far fa-copy"></i></button>
                    <button class="hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer" title="Settings"><i class="fas fa-wrench"></i></button>
                    <button class="hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer" title="Link"><i class="fas fa-link"></i></button>
                </div>
            </div>

            <!-- 标签区 -->
            <div class="flex flex-wrap items-center gap-4 mb-3">
                <div v-for="tag in server.tags" :key="tag.name" class="flex items-center gap-1.5 text-xs text-gray-600 dark:text-gray-400">
                    <div class="w-4 h-4 rounded-full border border-gray-300 dark:border-dark-border flex items-center justify-center bg-white dark:bg-dark-bg">
                        <span class="text-[8px] font-bold" :class="tag.status === 'good' ? 'text-brand-green' : 'text-gray-400'">A</span>
                    </div>
                    <span>{{ tag.name }}</span>
                </div>
            </div>

            <!-- 描述文本 -->
            <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 leading-relaxed">
                {{ server.description }}
            </p>
        </div>
    </div>
  </transition-group>
</template>

<style scoped>
.list-enter-active, .list-leave-active { transition: all 0.3s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(10px); }
</style>

