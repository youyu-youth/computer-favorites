<script setup lang="ts">
import shangjiaIcon from '@/assets/icons/svg/shangjia.svg'
import xiajiaIcon from '@/assets/icons/svg/xiajia.svg'
import type { AdminWebsiteStatusValue } from '@/types/admin-website'

import { shallowRef } from 'vue'

type WebsiteCard = {
    id: number
    title: string
    author: string
    isOfficial: boolean
    icon: string
    fallbackIcon: string
    iconBg: string
    description: string
    tags: Array<{ name: string; status: 'good' | 'warning' }>
    status: AdminWebsiteStatusValue
    deleted: number
}

const DEFAULT_WEBSITE_ICON = 'fas fa-globe'

const failedImageIds = shallowRef<number[]>([])

const props = defineProps<{
        servers: WebsiteCard[]
        viewMode: string
    selectedIds: number[]
    updatingWebsiteIds: number[]
}>()

const emit = defineEmits<{
    (e: 'toggle-select', websiteId: number): void
    (e: 'update-status', payload: { websiteId: number; status: AdminWebsiteStatusValue }): void
    (e: 'view-detail', websiteId: number): void
    (e: 'edit-website', websiteId: number): void
    (e: 'delete-website', websiteId: number): void
}>()

const isImageUrl = (icon: string): boolean => {
    if (!icon) {
        return false
    }
    const normalizedIcon = icon.trim().toLowerCase()
    if (!normalizedIcon) {
        return false
    }
    if (normalizedIcon.startsWith('http://') || normalizedIcon.startsWith('https://')) {
        return true
    }
    if (normalizedIcon.startsWith('data:image/')) {
        return true
    }
    if (normalizedIcon.startsWith('/')) {
        return true
    }
    return /\.(png|jpe?g|webp|gif|svg)(\?.*)?$/.test(normalizedIcon)
}

const shouldShowImageIcon = (server: WebsiteCard): boolean => {
    return isImageUrl(server.icon) && !failedImageIds.value.includes(server.id)
}

const handleImageError = (serverId: number): void => {
    if (failedImageIds.value.includes(serverId)) {
        return
    }
    failedImageIds.value = [...failedImageIds.value, serverId]
}

const resolveFallbackIcon = (server: WebsiteCard): string => {
    return server.fallbackIcon || DEFAULT_WEBSITE_ICON
}

const isSelected = (serverId: number): boolean => {
    return props.selectedIds.includes(serverId)
}

const isStatusUpdating = (serverId: number): boolean => {
    return props.updatingWebsiteIds.includes(serverId)
}

const isDeletedServer = (server: WebsiteCard): boolean => {
    return server.deleted === 1
}

const resolveStatusText = (server: WebsiteCard): string => {
    if (isDeletedServer(server)) {
        return '已删除'
    }
    return server.status === 1 ? '已上架' : '已下架'
}

const resolveStatusTextClass = (server: WebsiteCard): string => {
    if (isDeletedServer(server)) {
        return 'text-gray-500 dark:text-gray-400'
    }
    if (server.status === 1) {
        return 'text-emerald-600 dark:text-emerald-300'
    }
    return 'text-amber-700 dark:text-amber-300'
}

const resolveStatusDotClass = (server: WebsiteCard): string => {
    if (isDeletedServer(server)) {
        return 'bg-gray-400 dark:bg-gray-500'
    }
    if (server.status === 1) {
        return 'bg-emerald-500 dark:bg-emerald-400'
    }
    return 'bg-amber-500 dark:bg-amber-400'
}

const resolveStatusActionLabel = (server: WebsiteCard): string => {
    return server.status === 1 ? '下架' : '上架'
}

const resolveStatusActionIcon = (server: WebsiteCard): string => {
    return server.status === 1 ? xiajiaIcon : shangjiaIcon
}

const handleToggleSelect = (server: WebsiteCard): void => {
    if (isDeletedServer(server)) {
        return
    }
    emit('toggle-select', server.id)
}

const handleToggleStatus = (server: WebsiteCard): void => {
    if (isDeletedServer(server)) {
        return
    }
    const targetStatus: AdminWebsiteStatusValue = server.status === 1 ? 0 : 1
    emit('update-status', { websiteId: server.id, status: targetStatus })
}

const handleViewDetail = (server: WebsiteCard): void => {
    emit('view-detail', server.id)
}

const handleEditWebsite = (server: WebsiteCard): void => {
    if (isDeletedServer(server)) {
        return
    }
    emit('edit-website', server.id)
}

const handleDeleteWebsite = (server: WebsiteCard): void => {
    if (isDeletedServer(server)) {
        return
    }
    emit('delete-website', server.id)
}
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
    <div v-for="server in servers" :key="server.id"
         :class="[
             'bg-white dark:bg-[#1a2126] border border-gray-200 dark:border-dark-border rounded-xl transition-all hover:shadow-md dark:shadow-none relative group',
             viewMode === 'list' ? 'flex flex-col sm:flex-row items-center p-3 gap-4 sm:pr-4 overflow-hidden hover:border-gray-300 dark:hover:border-gray-600' : 'flex flex-col p-5 overflow-hidden hover:border-gray-300 dark:hover:border-gray-600'
         ]">

        <!-- ======================= -->
        <!-- GRID VIEW CONTENT -->
        <!-- ======================= -->
        <template v-if="viewMode === 'grid'">
            <!-- 头部：图标、标题、操作 -->
            <div
                class="-m-1 mb-3 cursor-pointer rounded-lg p-1 transition-colors hover:bg-gray-50 dark:hover:bg-dark-card/40"
                @click="handleViewDetail(server)"
            >
                <div class="flex items-start justify-between mb-4">
                    <div class="flex items-center gap-3">
                        <div
                            class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden"
                            :class="shouldShowImageIcon(server) ? 'bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border' : server.iconBg"
                        >
                            <img
                                v-if="shouldShowImageIcon(server)"
                                :src="server.icon"
                                :alt="`${server.title} 图标`"
                                class="h-8 w-8 rounded-md object-cover"
                                @error="handleImageError(server.id)"
                            >
                            <i v-else :class="resolveFallbackIcon(server)" class="text-white text-lg"></i>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <span class="text-brand-blue font-medium hover:underline">{{ server.title }}</span>
                                <span v-if="server.isOfficial" class="px-1.5 py-0.5 rounded text-[10px] font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-brand-blue border border-blue-200 dark:border-blue-800/50">official</span>
                            </div>
                            <div class="text-gray-500 dark:text-gray-500 text-xs">{{ server.author }}</div>
                        </div>
                    </div>
                    <div class="flex items-center gap-0 sm:gap-0.5 text-gray-400 dark:text-gray-500">
                        <button
                            type="button"
                            :disabled="isDeletedServer(server)"
                            @click.stop="handleToggleSelect(server)"
                            class="inline-flex h-7 w-7 items-center justify-center rounded-md cursor-pointer transition-colors disabled:cursor-not-allowed disabled:opacity-50"
                            :class="isSelected(server.id) ? 'text-brand-orange' : 'hover:text-gray-700 dark:hover:text-gray-300'"
                            :title="isSelected(server.id) ? '取消选择' : '选择网站'"
                        >
                            <i :class="isSelected(server.id) ? 'fas fa-check-square' : 'far fa-square'"></i>
                        </button>
                        <button
                            type="button"
                            class="inline-flex h-7 w-7 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer"
                            title="详情"
                            @click.stop="handleViewDetail(server)"
                        >
                            <i class="fas fa-circle-info"></i>
                        </button>
                        <button
                            type="button"
                            :disabled="isDeletedServer(server)"
                            class="inline-flex h-7 w-7 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                            title="修改网站"
                            @click.stop="handleEditWebsite(server)"
                        >
                            <i class="fas fa-wrench"></i>
                        </button>
                        <button
                            type="button"
                            :disabled="isDeletedServer(server)"
                            class="inline-flex h-7 w-7 items-center justify-center rounded-md hover:text-red-600 dark:hover:text-red-400 transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                            title="放入垃圾桶"
                            @click.stop="handleDeleteWebsite(server)"
                        >
                            <i class="fas fa-trash-can"></i>
                        </button>
                        <button type="button" class="inline-flex h-7 w-7 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 transition-colors cursor-pointer" title="Link" @click.stop><i class="fas fa-link"></i></button>
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

            <!-- Footer: Status and Toggle btn -->
            <div class="mt-auto flex items-center justify-between gap-3 pt-2">
                <div class="inline-flex items-center gap-1.5 text-xs font-medium" :class="resolveStatusTextClass(server)">
                    <span aria-hidden="true" class="h-2 w-2 rounded-full" :class="resolveStatusDotClass(server)"></span>
                    <span>{{ resolveStatusText(server) }}</span>
                </div>

                <button
                    type="button"
                    :disabled="isDeletedServer(server) || isStatusUpdating(server.id)"
                    @click.stop="handleToggleStatus(server)"
                    class="inline-flex cursor-pointer items-center gap-1.5 rounded-md border px-3 py-1.5 text-xs font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60"
                    :class="server.status === 1
                        ? 'border-amber-300 bg-amber-50 text-[#2d241f] hover:bg-amber-100 dark:border-amber-700 dark:bg-amber-900/20 dark:text-[#e95322] dark:hover:bg-amber-900/35'
                        : 'border-emerald-300 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:border-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-200 dark:hover:bg-emerald-900/35'"
                >
                    <img
                        :src="resolveStatusActionIcon(server)"
                        :alt="resolveStatusActionLabel(server)"
                        class="h-3.5 w-3.5"
                    >
                    <span>{{ isStatusUpdating(server.id) ? '处理中...' : resolveStatusActionLabel(server) }}</span>
                </button>
            </div>
        </template>

        <!-- ======================= -->
        <!-- LIST VIEW CONTENT -->
        <!-- ======================= -->
        <template v-else>
            <!-- Checkbox (Moved to left side for standard list selection) -->
            <div class="hidden sm:flex pl-1">
                <button
                    type="button"
                    :disabled="isDeletedServer(server)"
                    @click.stop="handleToggleSelect(server)"
                    class="inline-flex h-5 w-5 items-center justify-center rounded cursor-pointer transition-colors disabled:cursor-not-allowed disabled:opacity-50"
                    :class="isSelected(server.id) ? 'text-brand-orange' : 'text-gray-400 hover:text-gray-700 dark:text-gray-500 dark:hover:text-gray-300'"
                    :title="isSelected(server.id) ? '取消选择' : '选择网站'"
                >
                    <i :class="isSelected(server.id) ? 'fas fa-check-square text-lg' : 'far fa-square text-lg'"></i>
                </button>
            </div>

            <!-- Icon -->
            <div
                class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden cursor-pointer"
                :class="shouldShowImageIcon(server) ? 'bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border' : server.iconBg"
                @click="handleViewDetail(server)"
            >
                <img
                    v-if="shouldShowImageIcon(server)"
                    :src="server.icon"
                    :alt="`${server.title} 图标`"
                    class="h-8 w-8 rounded-md object-cover"
                    @error="handleImageError(server.id)"
                >
                <i v-else :class="resolveFallbackIcon(server)" class="text-white text-lg"></i>
            </div>

            <!-- Main Content Area -->
            <div class="flex-grow min-w-0 pr-4 cursor-pointer" @click="handleViewDetail(server)">
                <div class="flex items-center gap-3 mb-0.5">
                    <span class="text-brand-blue font-medium hover:underline truncate">{{ server.title }}</span>
                    <span v-if="server.isOfficial" class="px-1.5 py-0.5 rounded text-[10px] font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-brand-blue border border-blue-200 dark:border-blue-800/50 flex-shrink-0">official</span>
                    <span class="text-gray-400 dark:text-gray-500 text-xs truncate max-w-[120px]">{{ server.author }}</span>
                </div>

                <div class="flex items-center gap-3 mt-1">
                    <p class="text-xs text-gray-500 dark:text-gray-400 truncate flex-grow">
                        {{ server.description }}
                    </p>
                    <div class="hidden md:flex items-center gap-3 flex-shrink-0">
                        <div v-for="tag in server.tags" :key="tag.name" class="flex items-center gap-1 text-[11px] text-gray-500 dark:text-gray-400">
                            <span class="h-1.5 w-1.5 rounded-full" :class="tag.status === 'good' ? 'bg-emerald-400' : 'bg-gray-400'"></span>
                            <span>{{ tag.name }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Actions & Status Area -->
            <div class="flex items-center justify-end gap-5 flex-shrink-0">
                <!-- Inner Actions: Edit, view, delete, link -->
                <div class="hidden sm:flex items-center gap-1 text-gray-400 dark:text-gray-500">
                    <button
                        type="button"
                        class="inline-flex h-8 w-8 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50 transition-all cursor-pointer"
                        title="详情"
                        @click.stop="handleViewDetail(server)"
                    >
                        <i class="fas fa-circle-info text-sm"></i>
                    </button>
                    <button
                        type="button"
                        :disabled="isDeletedServer(server)"
                        class="inline-flex h-8 w-8 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50 transition-all cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                        title="修改网站"
                        @click.stop="handleEditWebsite(server)"
                    >
                        <i class="fas fa-wrench text-sm"></i>
                    </button>
                    <button
                        type="button"
                        class="inline-flex h-8 w-8 items-center justify-center rounded-md hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50 transition-all cursor-pointer"
                        title="Link"
                        @click.stop
                    >
                        <i class="fas fa-link text-sm"></i>
                    </button>
                    <button
                        type="button"
                        :disabled="isDeletedServer(server)"
                        class="inline-flex h-8 w-8 items-center justify-center rounded-md hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                        title="放入垃圾桶"
                        @click.stop="handleDeleteWebsite(server)"
                    >
                        <i class="fas fa-trash-can text-sm"></i>
                    </button>
                </div>

                <div class="h-6 w-px bg-gray-200 dark:bg-gray-700 hidden sm:block"></div>

                <!-- Status indicator -->
                <div class="inline-flex items-center gap-1.5 text-[11px] font-medium whitespace-nowrap min-w-[4rem]" :class="resolveStatusTextClass(server)">
                    <span aria-hidden="true" class="h-2 w-2 rounded-full" :class="resolveStatusDotClass(server)"></span>
                    <span>{{ resolveStatusText(server) }}</span>
                </div>

                <!-- Toggle Button -->
                <button
                    type="button"
                    :disabled="isDeletedServer(server) || isStatusUpdating(server.id)"
                    @click.stop="handleToggleStatus(server)"
                    class="inline-flex cursor-pointer items-center min-w-[5rem] justify-center gap-1.5 rounded border px-2.5 py-1 text-xs font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60"
                    :class="server.status === 1
                        ? 'border-amber-300 bg-amber-50 text-[#2d241f] hover:bg-amber-100 dark:border-amber-700 dark:bg-amber-900/20 dark:text-[#e95322] dark:hover:bg-amber-900/35'
                        : 'border-emerald-300 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:border-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-200 dark:hover:bg-emerald-900/35'"
                >
                    <img
                        :src="resolveStatusActionIcon(server)"
                        :alt="resolveStatusActionLabel(server)"
                        class="h-3 w-3"
                    >
                    <span>{{ isStatusUpdating(server.id) ? '处理中...' : resolveStatusActionLabel(server) }}</span>
                </button>
            </div>
        </template>
    </div>
  </transition-group>
</template>

<style scoped>
.list-enter-active, .list-leave-active { transition: all 0.3s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(10px); }
</style>

