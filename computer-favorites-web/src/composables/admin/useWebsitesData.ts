import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
    getAdminWebsiteCategories,
    getAdminWebsitePage,
    getAdminWebsiteStats,
} from '@/api/admin-website'
import { useAdminNavStore } from '@/stores/adminNav'
import type {
    AdminWebsiteListItem,
    AdminWebsiteStats,
    DeletedFilterValue,
} from '@/types/admin-website'

type WebsiteTagStatus = 'good' | 'warning'

type WebsiteTag = {
    name: string
    status: WebsiteTagStatus
}

type WebsiteCardItem = {
    id: number
    title: string
    author: string
    isOfficial: boolean
    icon: string
    iconBg: string
    description: string
    tags: WebsiteTag[]
}

type WebsiteCategoryItem = {
    id: number
    name: string
    count: number
}

const ALL_CATEGORY_ID = 0
const DEFAULT_PAGE_SIZE = 12
const SEARCH_DEBOUNCE_MS = 300

function resolveErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof Error && error.message) {
        return error.message
    }
    return fallback
}

function formatTimeDisplay(timeValue?: string): string {
    if (!timeValue) {
        return '-'
    }
    const date = new Date(timeValue)
    if (Number.isNaN(date.getTime())) {
        return '-'
    }
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
}

function resolveCardIcon(record: AdminWebsiteListItem): string {
    if (record.source === 1) {
        return 'fas fa-user-edit'
    }
    if (record.isRecommend === 1) {
        return 'fas fa-star'
    }
    return 'fas fa-globe'
}

function resolveCardIconBg(record: AdminWebsiteListItem): string {
    if (record.deleted === 1) {
        return 'bg-gray-500'
    }
    if (record.status === 1) {
        return 'bg-emerald-600'
    }
    return 'bg-amber-600'
}

function resolveCardTags(record: AdminWebsiteListItem): WebsiteTag[] {
    const statusTag: WebsiteTag = {
        name: record.deleted === 1 ? 'deleted' : record.status === 1 ? 'online' : 'offline',
        status: record.deleted === 1 ? 'warning' : 'good',
    }

    const auditTag: WebsiteTag = {
        name: record.auditStatus === 0 ? 'pending' : record.auditStatus === 2 ? 'rejected' : 'approved',
        status: record.auditStatus === 2 ? 'warning' : 'good',
    }

    const recommendTag: WebsiteTag = {
        name: record.isRecommend === 1 ? 'recommend' : 'normal',
        status: record.isRecommend === 1 ? 'good' : 'warning',
    }

    return [statusTag, auditTag, recommendTag]
}

function mapRecordToCard(record: AdminWebsiteListItem): WebsiteCardItem {
    return {
        id: record.id,
        title: record.name,
        author: record.categoryName || '未分类',
        isOfficial: record.source === 0,
        icon: resolveCardIcon(record),
        iconBg: resolveCardIconBg(record),
        description: record.summary || record.description || record.url || '-',
        tags: resolveCardTags(record),
    }
}

export function useWebsitesData() {
    const adminNavStore = useAdminNavStore()
    const { selectedCategoryId, activeMenu } = storeToRefs(adminNavStore)

    const viewMode = ref('grid')
    const searchQuery = ref('')
    const deletedFilter = ref<DeletedFilterValue>(-1)

    const tools = ref([
        { title: 'API-delete-a-block', subtitle: 'Notion MCP Server' },
        { title: 'API-retrieve-a-database', subtitle: 'Notion ReadOnly MCP Server' },
        { title: 'API-retrieve-a-page', subtitle: 'Notion ReadOnly MCP Server' },
        { title: 'API-retrieve-a-database', subtitle: 'Notion MCP Server' },
    ])

    const connectors = ref([
        { title: '0nMCP — Universal AI AP...', subtitle: 'io.github.0nork' },
        { title: '123elec-mcp', subtitle: 'io.github.Servicedsi' },
        { title: '1stay', subtitle: 'com.stayker' },
        { title: '1stDibs', subtitle: 'com.1stdibs' },
    ])

    const servers = ref<WebsiteCardItem[]>([])
    const currentPage = ref(1)
    const totalItems = ref(0)
    const loading = ref(false)
    const errorMessage = ref('')
    const stats = ref<AdminWebsiteStats>({
        total: 0,
        online: 0,
        offline: 0,
        pendingAudit: 0,
        rejectedAudit: 0,
        deleted: 0,
        latestUpdateTime: '',
    })

    let listRequestId = 0
    let searchTimer: ReturnType<typeof setTimeout> | null = null
    let syncingCategorySelection = false

    const pageSize = DEFAULT_PAGE_SIZE

    const totalPages = computed(() => {
        if (totalItems.value <= 0) {
            return 1
        }
        return Math.ceil(totalItems.value / pageSize)
    })

    const visiblePages = computed(() => {
        if (totalPages.value <= 7) {
            return Array.from({ length: totalPages.value }, (_, index) => index + 1)
        }
        if (currentPage.value <= 3) {
            return [1, 2, 3, 4, '...', totalPages.value - 1, totalPages.value]
        }
        if (currentPage.value >= totalPages.value - 2) {
            return [1, 2, '...', totalPages.value - 3, totalPages.value - 2, totalPages.value - 1, totalPages.value]
        }
        return [1, '...', currentPage.value - 1, currentPage.value, currentPage.value + 1, '...', totalPages.value]
    })

    const filteredServers = computed(() => servers.value)

    const formattedStats = computed(() => ({
        total: stats.value.total,
        online: stats.value.online,
        offline: stats.value.offline,
        pendingAudit: stats.value.pendingAudit,
        rejectedAudit: stats.value.rejectedAudit,
        deleted: stats.value.deleted,
        latestUpdateTime: formatTimeDisplay(stats.value.latestUpdateTime),
    }))

    const loadCategories = async () => {
        const categoryList = await getAdminWebsiteCategories(deletedFilter.value)
        const totalCount = categoryList.reduce((sum, item) => sum + Number(item.count || 0), 0)
        const mappedList: WebsiteCategoryItem[] = [
            {
                id: ALL_CATEGORY_ID,
                name: '全部分类',
                count: totalCount,
            },
            ...categoryList.map((item) => ({
                id: item.id,
                name: item.name,
                count: Number(item.count || 0),
            })),
        ]
        adminNavStore.setWebsiteCategories(mappedList)
    }

    const loadStats = async () => {
        stats.value = await getAdminWebsiteStats(deletedFilter.value)
    }

    const loadWebsitePage = async () => {
        if (activeMenu.value !== 'websites') {
            return
        }

        listRequestId += 1
        const requestId = listRequestId
        loading.value = true
        errorMessage.value = ''
        try {
            const pageData = await getAdminWebsitePage({
                pageNum: currentPage.value,
                pageSize,
                deleted: deletedFilter.value,
                categoryId: selectedCategoryId.value === ALL_CATEGORY_ID ? undefined : selectedCategoryId.value,
                keyword: searchQuery.value,
            })

            if (requestId !== listRequestId) {
                return
            }

            totalItems.value = Number(pageData.total || 0)
            servers.value = (pageData.records || []).map(mapRecordToCard)

            if (totalItems.value > 0 && currentPage.value > totalPages.value) {
                currentPage.value = totalPages.value
                await loadWebsitePage()
            }
        } catch (error) {
            if (requestId !== listRequestId) {
                return
            }
            servers.value = []
            totalItems.value = 0
            errorMessage.value = resolveErrorMessage(error, '网站列表加载失败')
        } finally {
            if (requestId === listRequestId) {
                loading.value = false
            }
        }
    }

    const prevPage = async () => {
        if (currentPage.value <= 1) {
            return
        }
        currentPage.value -= 1
        await loadWebsitePage()
    }

    const nextPage = async () => {
        if (currentPage.value >= totalPages.value) {
            return
        }
        currentPage.value += 1
        await loadWebsitePage()
    }

    const goToPage = async (page: number | string) => {
        if (page === '...' || typeof page !== 'number') {
            return
        }
        if (page < 1 || page > totalPages.value || page === currentPage.value) {
            return
        }
        currentPage.value = page
        await loadWebsitePage()
    }

    const reloadData = async () => {
        if (activeMenu.value !== 'websites') {
            return
        }

        errorMessage.value = ''
        try {
            await Promise.all([loadCategories(), loadStats()])
            await loadWebsitePage()
        } catch (error) {
            errorMessage.value = resolveErrorMessage(error, '网站管理数据加载失败')
        }
    }

    watch(deletedFilter, async () => {
        currentPage.value = 1
        syncingCategorySelection = true
        adminNavStore.setSelectedCategoryId(ALL_CATEGORY_ID)
        syncingCategorySelection = false
        await reloadData()
    })

    watch(selectedCategoryId, async (nextValue, previousValue) => {
        if (syncingCategorySelection) {
            return
        }
        if (nextValue === previousValue) {
            return
        }
        if (activeMenu.value !== 'websites') {
            return
        }
        currentPage.value = 1
        await loadWebsitePage()
    })

    watch(activeMenu, async (nextMenu, previousMenu) => {
        if (nextMenu !== 'websites') {
            return
        }
        if (previousMenu === 'websites') {
            return
        }
        await reloadData()
    })

    watch(searchQuery, () => {
        currentPage.value = 1
        if (searchTimer) {
            clearTimeout(searchTimer)
        }
        searchTimer = setTimeout(() => {
            void loadWebsitePage()
        }, SEARCH_DEBOUNCE_MS)
    })

    onMounted(() => {
        if (activeMenu.value !== 'websites') {
            return
        }
        void reloadData()
    })

    onBeforeUnmount(() => {
        if (searchTimer) {
            clearTimeout(searchTimer)
            searchTimer = null
        }
    })

    return {
        viewMode,
        searchQuery,
        deletedFilter,
        tools,
        connectors,
        filteredServers,
        currentPage,
        totalPages,
        totalItems,
        pageSize,
        loading,
        errorMessage,
        stats: formattedStats,
        prevPage,
        nextPage,
        goToPage,
        reloadData,
        visiblePages,
    }
}
