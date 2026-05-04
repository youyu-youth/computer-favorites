import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import type {
  AdminCommentAction,
  AdminCommentDetail,
  AdminCommentHandleForm,
  AdminCommentListItem,
  AdminCommentQuery,
  AdminCommentStatistics,
} from '@/types/admin-comment'
import { AdminCommentStatus } from '@/types/admin-comment'

type RefreshResult = { ok: boolean; message?: string }

interface HandleDialogState extends AdminCommentHandleForm {
  open: boolean
  commentId: number | null
  isBatch: boolean
  batchIds: number[]
}

const DEFAULT_PAGE_SIZE = 10

const DEFAULT_STATISTICS: AdminCommentStatistics = {
  total: 0,
  todayNew: 0,
  visible: 0,
  hidden: 0,
}

function createVisiblePages(totalPages: number, currentPage: number): Array<number | string> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }

  const pages: Array<number | string> = [1]
  if (currentPage > 3) pages.push('...')
  for (let p = Math.max(2, currentPage - 1); p <= Math.min(totalPages - 1, currentPage + 1); p += 1) {
    pages.push(p)
  }
  if (currentPage < totalPages - 2) pages.push('...')
  pages.push(totalPages)
  return pages
}

const MOCK_COMMENTS: AdminCommentListItem[] = [
  {
    id: 1001, websiteId: 10, websiteName: 'Vue.js 中文社区', websiteIcon: 'https://vuejs.org/logo.svg',
    userId: 201, userName: '代码猎人', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=hunter',
    content: '这个框架的响应式系统真的太棒了，用 Composition API 重构之后代码量减少了一半，而且逻辑复用变得非常自然。',
    likeCount: 24, replyCount: 5, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-04 10:23:00', updateTime: '2026-05-04 10:23:00',
  },
  {
    id: 1002, websiteId: 20, websiteName: 'MDN Web Docs', websiteIcon: 'https://developer.mozilla.org/favicon-48x48.png',
    userId: 202, userName: '前端小白', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=newbie',
    content: 'MDN 的 CSS 参考文档写得太清楚了，每次遇到不熟悉的属性都会来这里查，比看博客靠谱多了。',
    likeCount: 18, replyCount: 3, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-04 08:45:00', updateTime: '2026-05-04 08:45:00',
  },
  {
    id: 1003, websiteId: 30, websiteName: 'Stack Overflow', websiteIcon: 'https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico',
    userId: 203, userName: 'Java架构师', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=architect',
    content: 'Stack Overflow 上关于 Spring Boot 3.x 的回答质量参差不齐，很多高票回答还是基于 2.x 的写法，希望能有更多更新的解答。',
    likeCount: 31, replyCount: 8, reportCount: 2, status: AdminCommentStatus.HIDDEN,
    createTime: '2026-05-03 22:10:00', updateTime: '2026-05-04 06:00:00',
  },
  {
    id: 1004, websiteId: 10, websiteName: 'Vue.js 中文社区', websiteIcon: 'https://vuejs.org/logo.svg',
    userId: 204, userName: '全栈开发者', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=fullstack',
    content: 'Nuxt 3 和 Vue Router 4 的结合非常顺滑，SSR 部署到 Vercel 之后首屏加载时间从 3s 降到了 800ms，体验提升巨大。',
    likeCount: 42, replyCount: 12, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-03 18:30:00', updateTime: '2026-05-03 18:30:00',
  },
  {
    id: 1005, websiteId: 40, websiteName: 'LeetCode 力扣', websiteIcon: 'https://leetcode.cn/favicon.ico',
    userId: 205, userName: '算法刷题狂', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=algo',
    content: '动态规划的专题分类做得很好，但是部分题解的中文翻译有错误，建议增加社区纠错功能。',
    likeCount: 15, replyCount: 4, reportCount: 1, status: AdminCommentStatus.HIDDEN,
    createTime: '2026-05-03 15:20:00', updateTime: '2026-05-03 15:20:00',
  },
  {
    id: 1006, websiteId: 50, websiteName: 'GitHub', websiteIcon: 'https://github.githubassets.com/favicons/favicon.svg',
    userId: 206, userName: '开源爱好者', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=oss',
    content: 'GitHub Copilot 在写单元测试时真的太好用了，自动生成的测试用例覆盖率很高，只需要微调几行就能用。',
    likeCount: 56, replyCount: 15, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-03 12:05:00', updateTime: '2026-05-03 12:05:00',
  },
  {
    id: 1007, websiteId: 60, websiteName: '掘金', websiteIcon: 'https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/juejin/1.0.0/favicon.ico',
    userId: 207, userName: 'TypeScript拥趸', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=tsfan',
    content: '这篇文章关于 TypeScript 5.0 装饰器的讲解很到位，终于把 ES 装饰器和 TS 实验性装饰器的区别讲清楚了。',
    likeCount: 38, replyCount: 9, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-02 20:15:00', updateTime: '2026-05-02 20:15:00',
  },
  {
    id: 1008, websiteId: 70, websiteName: 'Rust 语言中文网', websiteIcon: 'https://www.rust-lang.org/favicon-32x32.png',
    userId: 208, userName: '系统编程新手', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=rustacean',
    content: '所有权系统的概念一开始很难理解，但是一旦理解了之后写代码反而更安全了，编译器就是最好的老师。',
    likeCount: 27, replyCount: 6, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-02 16:40:00', updateTime: '2026-05-02 16:40:00',
  },
  {
    id: 1009, websiteId: 30, websiteName: 'Stack Overflow', websiteIcon: 'https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico',
    userId: 209, userName: 'Pythonista', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=pythonista',
    content: '为什么 Python 相关的标签页总是充斥着低质量问题？建议加强提问前的搜索引导。',
    likeCount: 8, replyCount: 2, reportCount: 3, status: AdminCommentStatus.HIDDEN,
    createTime: '2026-05-02 14:10:00', updateTime: '2026-05-03 09:00:00',
  },
  {
    id: 1010, websiteId: 80, websiteName: 'CSDN', websiteIcon: 'https://g.csdnimg.cn/static/logo/favicon32.ico',
    userId: 210, userName: '技术博主', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=blogger',
    content: 'CSDN 的 VIP 文章越来越多，很多基础知识都被锁在付费墙后面，对新手非常不友好。',
    likeCount: 65, replyCount: 20, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-02 10:30:00', updateTime: '2026-05-02 10:30:00',
  },
  {
    id: 1011, websiteId: 90, websiteName: 'freeCodeCamp', websiteIcon: 'https://cdn.freecodecamp.org/platform/universal/favicons/favicon-32x32.png',
    userId: 211, userName: '自学编程者', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=selftaught',
    content: '免费课程质量不错，但是实践项目的自动测试有时候判定过于严格，和实际场景不符。',
    likeCount: 12, replyCount: 1, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-05-01 22:00:00', updateTime: '2026-05-01 22:00:00',
  },
  {
    id: 1012, websiteId: 40, websiteName: 'LeetCode 力扣', websiteIcon: 'https://leetcode.cn/favicon.ico',
    userId: 212, userName: '竞赛选手', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=competitor',
    content: '周赛题目质量越来越差，很多都是套路题，缺少原创性和考察深度的题目。',
    likeCount: 4, replyCount: 0, reportCount: 0, status: AdminCommentStatus.HIDDEN,
    createTime: '2026-04-30 18:20:00', updateTime: '2026-05-01 10:00:00',
  },
  {
    id: 1013, websiteId: 50, websiteName: 'GitHub', websiteIcon: 'https://github.githubassets.com/favicons/favicon.svg',
    userId: 213, userName: 'DevOps工程师', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=devops',
    content: 'GitHub Actions 的 workflow 语法文档写得太分散了，找个具体用法要跳好几个页面。',
    likeCount: 0, replyCount: 0, reportCount: 0, status: AdminCommentStatus.HIDDEN,
    createTime: '2026-04-29 09:15:00', updateTime: '2026-04-30 14:00:00',
  },
  {
    id: 1014, websiteId: 10, websiteName: 'Vue.js 中文社区', websiteIcon: 'https://vuejs.org/logo.svg',
    userId: 214, userName: 'React转Vue', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=switcher',
    content: '从 React 转到 Vue 3 之后感觉开发效率提升了不少，Pinia 比 Redux 的模板代码少太多了。',
    likeCount: 33, replyCount: 7, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-04-28 11:30:00', updateTime: '2026-04-28 11:30:00',
  },
  {
    id: 1015, websiteId: 60, websiteName: '掘金', websiteIcon: 'https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/juejin/1.0.0/favicon.ico',
    userId: 215, userName: '后端老兵', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=veteran',
    content: '掘金上的技术文章质量在下降，很多都是互相抄的，深度好文越来越少了。',
    likeCount: 49, replyCount: 14, reportCount: 0, status: AdminCommentStatus.VISIBLE,
    createTime: '2026-04-27 16:45:00', updateTime: '2026-04-27 16:45:00',
  },
]

const MOCK_REPLIES: Record<number, AdminCommentDetail['replies']> = {
  1001: [
    { id: 2001, userId: 216, userName: 'Vue3粉丝', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=vfan', content: '确实！ref 和 reactive 分开用之后逻辑特别清晰。', likeCount: 5, replyTo: null, isDeleted: false, createTime: '2026-05-04 11:00:00' },
    { id: 2002, userId: 217, userName: '小白学习中', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=learner', content: '求问从 Options API 迁移有什么推荐步骤吗？', likeCount: 2, replyTo: '代码猎人', isDeleted: false, createTime: '2026-05-04 12:30:00' },
  ],
  1003: [
    { id: 2003, userId: 218, userName: 'Spring老司机', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=spring', content: '确实，Spring Boot 3 的大量改动还没有被广泛更新到 SO 上。', likeCount: 8, replyTo: null, isDeleted: false, createTime: '2026-05-04 07:00:00' },
  ],
  1004: [
    { id: 2004, userId: 219, userName: 'Vercel用户', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=vercel', content: 'Nuxt 3 的 auto-imports 也太好用了吧，写起来飞快。', likeCount: 3, replyTo: null, isDeleted: false, createTime: '2026-05-03 19:00:00' },
    { id: 2005, userId: 220, userName: 'SSR实践者', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=ssr', content: '请问部署到 Vercel 有没有什么坑？我这边 hydration 报错了。', likeCount: 1, replyTo: '全栈开发者', isDeleted: false, createTime: '2026-05-03 20:30:00' },
    { id: 2006, userId: 204, userName: '全栈开发者', userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=fullstack', content: 'hydration 报错通常是客户端和服务端数据不一致，检查下 onMounted 里有没有修改响应式数据。', likeCount: 4, replyTo: 'SSR实践者', isDeleted: false, createTime: '2026-05-03 21:15:00' },
  ],
}

function filterMockData(items: AdminCommentListItem[], query: AdminCommentQuery): AdminCommentListItem[] {
  let filtered = items

  if (typeof query.status === 'number') {
    filtered = filtered.filter((item) => item.status === query.status)
  }

  const keyword = query.keyword.trim().toLowerCase()
  if (keyword) {
    filtered = filtered.filter((item) =>
      [item.userName, item.content]
        .some((v) => v.toLowerCase().includes(keyword)),
    )
  }

  return filtered
}

export function useAdminCommentManagement() {
  const allComments = ref<AdminCommentListItem[]>([...MOCK_COMMENTS])
  const loading = shallowRef(false)
  const detailLoading = shallowRef(false)
  const statisticsState = ref<AdminCommentStatistics>({ ...DEFAULT_STATISTICS })
  const totalItemsState = shallowRef(0)
  const totalPagesState = shallowRef(1)
  const detailOpen = shallowRef(false)
  const detailRecord = shallowRef<AdminCommentDetail | null>(null)
  const selectedIds = ref<number[]>([])
  const handleSubmitting = shallowRef(false)

  const query = reactive<AdminCommentQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
  })

  const handleDialog = reactive<HandleDialogState>({
    open: false,
    commentId: null,
    action: 'hide',
    reason: '',
    isBatch: false,
    batchIds: [],
  })

  const totalItems = computed(() => totalItemsState.value)
  const totalPages = computed(() => Math.max(1, totalPagesState.value))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))
  const statistics = computed(() => statisticsState.value)
  const statusSummaryText = computed(() => `${statistics.value.hidden} 条评论已隐藏`)
  const selectedCount = computed(() => selectedIds.value.length)

  const pagedComments = computed(() => {
    const filtered = filterMockData(allComments.value, query)
    const start = (query.pageNum - 1) * query.pageSize
    return filtered.slice(start, start + query.pageSize)
  })

  const loadCommentPage = () => {
    loading.value = true
    try {
      const filtered = filterMockData(allComments.value, query)
      totalItemsState.value = filtered.length
      totalPagesState.value = Math.max(1, Math.ceil(filtered.length / query.pageSize))

      if (query.pageNum > totalPagesState.value) {
        query.pageNum = totalPagesState.value
      }
    } finally {
      loading.value = false
    }
  }

  const recalculateStatistics = () => {
    const items = allComments.value
    statisticsState.value = {
      total: items.length,
      todayNew: items.filter((i) => i.createTime.startsWith('2026-05-04')).length,
      visible: items.filter((i) => i.status === AdminCommentStatus.VISIBLE).length,
      hidden: items.filter((i) => i.status === AdminCommentStatus.HIDDEN).length,
    }
  }

  const refreshData = async (): Promise<RefreshResult> => {
    loadCommentPage()
    recalculateStatistics()
    return { ok: true }
  }

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    resetToFirstPage()
    loadCommentPage()
  }

  const setStatus = (status: AdminCommentStatus | null) => {
    query.status = status
    resetToFirstPage()
    loadCommentPage()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) return
    query.pageNum -= 1
    loadCommentPage()
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) return
    query.pageNum += 1
    loadCommentPage()
  }

  const goToPage = (page: number | string) => {
    const n = Number(page)
    if (!Number.isFinite(n)) return
    const normalized = Math.min(totalPages.value, Math.max(1, Math.trunc(n)))
    if (normalized === query.pageNum) return
    query.pageNum = normalized
    loadCommentPage()
  }

  const openDetail = (commentId: number) => {
    detailOpen.value = true
    detailLoading.value = true

    setTimeout(() => {
      const item = allComments.value.find((c) => c.id === commentId)
      if (!item) {
        detailRecord.value = null
        detailLoading.value = false
        return
      }

      const replies = MOCK_REPLIES[commentId] ?? []
      detailRecord.value = {
        ...item,
        replies,
        lastAdminAction: item.status === AdminCommentStatus.HIDDEN ? '隐藏评论' : null,
        lastAdminActionTime: item.status === AdminCommentStatus.HIDDEN ? item.updateTime : null,
      }
      detailLoading.value = false
    }, 300)
  }

  const closeDetail = () => {
    detailOpen.value = false
    detailRecord.value = null
  }

  const setSelectedIds = (ids: number[]) => {
    selectedIds.value = ids
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const openSingleHandleDialog = (commentId: number, action: AdminCommentAction) => {
    handleDialog.open = true
    handleDialog.commentId = commentId
    handleDialog.action = action
    handleDialog.reason = ''
    handleDialog.isBatch = false
    handleDialog.batchIds = []
  }

  const openBatchHandleDialog = (action: AdminCommentAction) => {
    handleDialog.open = true
    handleDialog.commentId = null
    handleDialog.action = action
    handleDialog.reason = ''
    handleDialog.isBatch = true
    handleDialog.batchIds = [...selectedIds.value]
  }

  const closeHandleDialog = () => {
    handleDialog.open = false
    handleDialog.commentId = null
    handleDialog.action = 'hide'
    handleDialog.reason = ''
    handleDialog.isBatch = false
    handleDialog.batchIds = []
  }

  const updateHandleForm = (payload: Partial<AdminCommentHandleForm>) => {
    if (typeof payload.action !== 'undefined') handleDialog.action = payload.action
    if (typeof payload.reason !== 'undefined') handleDialog.reason = payload.reason
  }

  const patchCommentLocally = (commentId: number, newStatus: AdminCommentStatus) => {
    const idx = allComments.value.findIndex((c) => c.id === commentId)
    if (idx >= 0) {
      const original = allComments.value[idx]!
      allComments.value.splice(idx, 1, {
        id: original.id,
        websiteId: original.websiteId,
        websiteName: original.websiteName,
        websiteIcon: original.websiteIcon,
        userId: original.userId,
        userName: original.userName,
        userAvatar: original.userAvatar,
        content: original.content,
        likeCount: original.likeCount,
        replyCount: original.replyCount,
        reportCount: original.reportCount,
        status: newStatus,
        createTime: original.createTime,
        updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      })
    }

    if (detailRecord.value && detailRecord.value.id === commentId) {
      detailRecord.value = {
        ...detailRecord.value,
        status: newStatus,
        updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
        lastAdminAction: newStatus === AdminCommentStatus.HIDDEN ? '隐藏评论' : '显示评论',
        lastAdminActionTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      }
    }
  }

  const submitHandleAction = async (): Promise<RefreshResult> => {
    if (handleSubmitting.value) {
      return { ok: false, message: '当前正在提交请求，请稍后再试。' }
    }

    handleSubmitting.value = true
    try {
      const targetIds = handleDialog.isBatch ? handleDialog.batchIds : (handleDialog.commentId ? [handleDialog.commentId] : [])
      const newStatus = handleDialog.action === 'hide' ? AdminCommentStatus.HIDDEN : AdminCommentStatus.VISIBLE

      for (const id of targetIds) {
        patchCommentLocally(id, newStatus)
      }

      selectedIds.value = selectedIds.value.filter((id) => !targetIds.includes(id))
      closeHandleDialog()
      loadCommentPage()
      recalculateStatistics()

      return {
        ok: true,
        message: handleDialog.isBatch
          ? `已${handleDialog.action === 'hide' ? '隐藏' : '显示'} ${targetIds.length} 条评论。`
          : `评论已${handleDialog.action === 'hide' ? '隐藏' : '显示'}。`,
      }
    } finally {
      handleSubmitting.value = false
    }
  }

  onMounted(() => {
    loadCommentPage()
    recalculateStatistics()
  })

  return {
    loading,
    detailLoading,
    query,
    pagedComments,
    totalItems,
    totalPages,
    visiblePages,
    detailOpen,
    detailRecord,
    handleDialog,
    selectedIds,
    selectedCount,
    statistics,
    statusSummaryText,
    setKeyword,
    setStatus,
    refreshData,
    prevPage,
    nextPage,
    goToPage,
    openDetail,
    closeDetail,
    setSelectedIds,
    clearSelection,
    openSingleHandleDialog,
    openBatchHandleDialog,
    closeHandleDialog,
    updateHandleForm,
    submitHandleAction,
  }
}
