<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 收藏到文件夹对话框
 * 展示用户的收藏夹树形结构，支持嵌套层级选择、搜索过滤
 * 内嵌创建文件夹对话框，创建成功后自动刷新树
 */
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { X, BookmarkPlus, Search, FolderPlus } from 'lucide-vue-next'
import FolderTreeNode from '@/components/user/FolderTreeNode.vue'
import CreateFolderDialog from '@/components/user/CreateFolderDialog.vue'
import { collectWebsite } from '@/api/user-collect'
import { useMessage } from '@/composables/useMessage'
import { useFolderStore } from '@/stores/folder'
import type { CollectionCategory } from '@/types/collection'

defineOptions({ name: 'CollectFolderDialog' })

interface Props {
  visible: boolean
  websiteId: number | null
  websiteName?: string
}

const props = withDefaults(defineProps<Props>(), {
  websiteName: '',
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', folderId: number, websiteId: number): void
}>()

const message = useMessage()
const folderStore = useFolderStore()

const selectedFolderId = ref<number | null>(null)
const searchQuery = ref('')
const isSubmitting = ref(false)
const showCreateDialog = ref(false)

let previousScrollY = 0

const filterTree = (nodes: CollectionCategory[], query: string): CollectionCategory[] => {
  if (!query.trim()) return nodes
  const lowerQuery = query.toLowerCase()
  const result: CollectionCategory[] = []
  for (const node of nodes) {
    const childMatches = filterTree(node.children, query)
    if (node.name.toLowerCase().includes(lowerQuery) || childMatches.length > 0) {
      result.push({ ...node, children: childMatches })
    }
  }
  return result
}

const findFolderName = (nodes: CollectionCategory[], id: number): string => {
  for (const node of nodes) {
    if (node.id === id) return node.name
    const found = findFolderName(node.children, id)
    if (found) return found
  }
  return ''
}

const filteredTree = computed(() => filterTree(folderStore.categories, searchQuery.value))
const canConfirm = computed(() => selectedFolderId.value !== null && !isSubmitting.value)

const closeDialog = () => {
  if (isSubmitting.value) return
  emit('update:visible', false)
}

const handleFolderSelect = (id: number) => {
  selectedFolderId.value = selectedFolderId.value === id ? null : id
}

const handleConfirm = async () => {
  if (!canConfirm.value || selectedFolderId.value === null || !props.websiteId) return
  isSubmitting.value = true
  try {
    await collectWebsite({ websiteId: props.websiteId, folderId: selectedFolderId.value })
    const folderName = findFolderName(folderStore.categories, selectedFolderId.value)
    message.add({ title: '收藏该网站成功', description: folderName ? `已收藏到「${folderName}」` : undefined, type: 'success', position: 'top-right' })
    emit('confirm', selectedFolderId.value, props.websiteId)
    closeDialog()
  } catch (e) {
    message.add({ title: '收藏失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error', position: 'top-right' })
  } finally {
    isSubmitting.value = false
  }
}

const handleCreateFolder = async () => {
  await folderStore.loadFolderOptions()
  showCreateDialog.value = true
}

const handleFolderCreated = async () => {
  showCreateDialog.value = false
  await folderStore.handleFolderCreated()
}

const lockPageScroll = () => {
  if (typeof window === 'undefined') return
  previousScrollY = window.scrollY
  document.documentElement.classList.add('collect-dialog-scroll-lock')
  document.body.classList.add('collect-dialog-scroll-lock')
  document.body.style.setProperty('--collect-dialog-scroll-y', previousScrollY + 'px')
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') return
  document.documentElement.classList.remove('collect-dialog-scroll-lock')
  document.body.classList.remove('collect-dialog-scroll-lock')
  document.body.style.removeProperty('--collect-dialog-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const resetState = () => {
  selectedFolderId.value = null
  searchQuery.value = ''
  isSubmitting.value = false
}

watch(
  () => props.visible,
  (isOpen) => {
    if (isOpen) {
      lockPageScroll()
      resetState()
      folderStore.loadFolderTree()
    } else {
      unlockPageScroll()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unlockPageScroll()
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-[9999] flex items-center justify-center px-4 sm:px-6"
      >
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70"
          @click="closeDialog"
        ></div>

        <Transition
          enter-active-class="transition-all duration-250 ease-out"
          enter-from-class="opacity-0 scale-95 translate-y-3"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-3"
        >
          <section
            v-if="visible"
            class="collect-dialog-panel relative z-[1] flex w-full flex-col overflow-hidden rounded-xl border bg-white sm:w-[min(92vw,30rem)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="collect-folder-title"
            @click.stop
          >
            <header class="collect-dialog-header flex shrink-0 items-center justify-between border-b px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="collect-dialog-icon flex h-9 w-9 items-center justify-center rounded-lg border">
                  <BookmarkPlus class="h-4 w-4" />
                </div>
                <div>
                  <h2 id="collect-folder-title" class="collect-dialog-title text-base font-semibold">
                    收藏到文件夹
                  </h2>
                  <p v-if="websiteName" class="collect-dialog-subtitle mt-0.5 text-xs">
                    {{ websiteName }}
                  </p>
                </div>
              </div>
              <button
                type="button"
                class="collect-dialog-close flex h-8 w-8 cursor-pointer items-center justify-center rounded-md transition-colors"
                aria-label="关闭对话框"
                :disabled="isSubmitting"
                @click="closeDialog"
              >
                <X class="h-4 w-4" />
              </button>
            </header>

            <div class="collect-search-bar shrink-0 border-b px-5 py-3">
              <div class="relative flex items-center">
                <Search class="collect-search-icon pointer-events-none absolute left-3 h-4 w-4" />
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索收藏夹..."
                  class="collect-search-input w-full rounded-lg border py-2.5 pl-9 pr-3 text-sm outline-none transition-all"
                  maxlength="50"
                />
              </div>
            </div>

            <div
              class="collect-folder-list flex-1 overflow-y-auto px-3 py-3"
              style="max-height: calc(80vh - 14rem)"
            >
              <div v-if="folderStore.isLoading" class="py-12 text-center">
                <div class="collect-loading-spinner mx-auto mb-3 h-8 w-8 animate-spin rounded-full border-2 border-t-transparent"></div>
                <p class="collect-loading-text text-xs">加载收藏夹...</p>
              </div>

              <div v-else-if="filteredTree.length === 0" class="py-12 text-center">
                <FolderPlus class="collect-empty-icon mx-auto mb-3 h-10 w-10" />
                <p class="collect-empty-title mb-1 text-sm font-medium">
                  {{ searchQuery.trim() ? '未找到匹配的收藏夹' : '还没有收藏夹' }}
                </p>
                <p class="collect-empty-desc mb-4 text-xs">
                  {{ searchQuery.trim() ? '试试其他关键词' : '创建一个收藏夹来整理你的收藏' }}
                </p>
                <button
                  v-if="!searchQuery.trim()"
                  type="button"
                  class="collect-empty-create cursor-pointer rounded-lg border px-4 py-2 text-sm font-medium transition-colors"
                  @click="handleCreateFolder"
                >
                  创建收藏夹
                </button>
              </div>

              <ul
                v-else
                class="space-y-1"
                role="tree"
                aria-label="收藏夹列表"
              >
                <FolderTreeNode
                  v-for="node in filteredTree"
                  :key="node.id"
                  :node="node"
                  :depth="0"
                  :selected-id="selectedFolderId"
                  @select="handleFolderSelect"
                />
              </ul>
            </div>

            <footer class="collect-dialog-footer flex shrink-0 items-center justify-between border-t px-5 py-4">
              <button
                type="button"
                class="collect-create-btn flex cursor-pointer items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors"
                @click="handleCreateFolder"
              >
                <FolderPlus class="h-4 w-4" />
                新建
              </button>

              <div class="flex items-center gap-3">
                <button
                  type="button"
                  class="collect-cancel-btn cursor-pointer rounded-lg border px-5 py-2.5 text-sm font-medium transition-colors"
                  :disabled="isSubmitting"
                  @click="closeDialog"
                >
                  取消
                </button>
                <button
                  type="button"
                  class="collect-confirm-btn cursor-pointer rounded-lg px-5 py-2.5 text-sm font-semibold transition-all disabled:cursor-not-allowed"
                  :disabled="!canConfirm"
                  @click="handleConfirm"
                >
                  {{ isSubmitting ? '收藏中...' : '确认收藏' }}
                </button>
              </div>
            </footer>
          </section>
        </Transition>
      </div>
    </Transition>
  </Teleport>

  <CreateFolderDialog
    v-model:open="showCreateDialog"
    :parent-options="folderStore.folderOptions"
    @submit="handleFolderCreated"
  />
</template>

<style scoped>
/* ========== Light Mode ========== */

.collect-dialog-panel {
  border: 1px solid rgb(229 231 235 / 0.8);
  box-shadow:
    0 20px 50px rgb(245 158 11 / 0.08),
    0 4px 16px rgb(0 0 0 / 0.06);
}

.collect-dialog-header {
  border-bottom: 1px solid rgb(229 231 235);
  background-color: rgb(255 251 235 / 0.5);
}

.collect-dialog-icon {
  border: 1px solid rgb(245 158 11 / 0.2);
  background-color: rgb(255 251 235);
  color: rgb(217 119 6);
}

.collect-dialog-title { color: rgb(17 24 39); }
.collect-dialog-subtitle { color: rgb(107 114 128); }

.collect-dialog-close { color: rgb(156 163 175); }
.collect-dialog-close:hover { background-color: rgb(243 244 246); color: rgb(75 85 99); }
.collect-dialog-close:disabled { cursor: not-allowed; opacity: 0.5; }

.collect-search-bar { border-bottom: 1px solid rgb(229 231 235); }
.collect-search-icon { color: rgb(156 163 175); }

.collect-search-input {
  border: 1px solid rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(17 24 39);
}
.collect-search-input::placeholder { color: rgb(156 163 175); }
.collect-search-input:focus {
  border-color: rgb(245 158 11);
  box-shadow: 0 0 0 3px rgb(245 158 11 / 0.12);
}

.collect-loading-spinner {
  border: 2px solid rgb(229 231 235);
  border-top-color: rgb(245 158 11);
}
.collect-loading-text { color: rgb(156 163 175); }

.collect-empty-icon { color: rgb(209 213 219); }
.collect-empty-title { color: rgb(55 65 81); }
.collect-empty-desc { color: rgb(156 163 175); }

.collect-empty-create {
  border: 1px solid rgb(245 158 11 / 0.3);
  background-color: rgb(255 251 235);
  color: rgb(217 119 6);
}
.collect-empty-create:hover { background-color: rgb(254 243 199); }

.collect-dialog-footer { border-top: 1px solid rgb(229 231 235); }

.collect-create-btn { color: rgb(107 114 128); }
.collect-create-btn:hover { background-color: rgb(249 250 251); color: rgb(55 65 81); }

.collect-cancel-btn {
  border: 1px solid rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(55 65 81);
}
.collect-cancel-btn:hover { background-color: rgb(249 250 251); }
.collect-cancel-btn:disabled { cursor: not-allowed; opacity: 0.5; }

.collect-confirm-btn {
  background-color: rgb(245 158 11);
  color: rgb(255 255 255);
}
.collect-confirm-btn:hover:not(:disabled) {
  background-color: rgb(217 119 6);
  transform: translateY(-1px);
}
.collect-confirm-btn:disabled {
  background-color: rgb(245 158 11 / 0.4);
  color: rgb(255 255 255 / 0.6);
}

/* ========== Dark Mode ========== */

:root.dark .collect-dialog-panel {
  background-color: #0a0a0e;
  border: 1px solid rgba(255 255 255 / 0.06);
  box-shadow:
    inset 0 1px 0 rgba(255 255 255 / 0.04),
    0 24px 60px rgba(0 0 0 / 0.65),
    0 0 0 1px rgba(255 255 255 / 0.02);
}

:root.dark .collect-dialog-header {
  background:
    linear-gradient(180deg, rgba(255 255 255 / 0.025) 0%, rgba(255 255 255 / 0) 100%),
    #0a0a0e;
  border-bottom: 1px solid rgba(255 255 255 / 0.05);
}

:root.dark .collect-dialog-icon {
  border: 1px solid rgba(245 158 11 / 0.2);
  background-color: rgba(245 158 11 / 0.08);
  color: rgb(251 191 36);
}

:root.dark .collect-dialog-title { color: rgb(240 240 244); }
:root.dark .collect-dialog-subtitle { color: rgb(140 145 158); }

:root.dark .collect-dialog-close { color: rgb(120 125 140); }
:root.dark .collect-dialog-close:hover {
  background-color: rgba(255 255 255 / 0.06);
  color: rgb(200 205 218);
}

:root.dark .collect-search-bar {
  border-bottom: 1px solid rgba(255 255 255 / 0.05);
}
:root.dark .collect-search-icon { color: rgb(100 105 120); }

:root.dark .collect-search-input {
  background-color: #0e0e13;
  border: 1px solid rgba(255 255 255 / 0.06);
  color: rgb(230 230 238);
  box-shadow: inset 0 1px 2px rgba(0 0 0 / 0.3);
}
:root.dark .collect-search-input::placeholder { color: rgb(90 95 110); }
:root.dark .collect-search-input:focus {
  border-color: rgba(245 158 11 / 0.5);
  box-shadow:
    0 0 0 3px rgba(245 158 11 / 0.12),
    inset 0 1px 2px rgba(0 0 0 / 0.2);
}

:root.dark .collect-loading-spinner {
  border-color: rgba(255 255 255 / 0.08);
  border-top-color: rgb(251 191 36);
}
:root.dark .collect-loading-text { color: rgb(100 105 120); }

:root.dark .collect-empty-icon { color: rgb(60 65 80); }
:root.dark .collect-empty-title { color: rgb(180 185 200); }
:root.dark .collect-empty-desc { color: rgb(100 105 120); }

:root.dark .collect-empty-create {
  border: 1px solid rgba(245 158 11 / 0.25);
  background-color: rgba(245 158 11 / 0.08);
  color: rgb(251 191 36);
}
:root.dark .collect-empty-create:hover { background-color: rgba(245 158 11 / 0.15); }

:root.dark .collect-dialog-footer {
  border-top: 1px solid rgba(255 255 255 / 0.05);
}

:root.dark .collect-create-btn { color: rgb(120 125 140); }
:root.dark .collect-create-btn:hover {
  background-color: rgba(255 255 255 / 0.05);
  color: rgb(200 205 218);
}

:root.dark .collect-cancel-btn {
  border: 1px solid rgba(255 255 255 / 0.08);
  background-color: transparent;
  color: rgb(180 185 200);
}
:root.dark .collect-cancel-btn:hover {
  background-color: rgba(255 255 255 / 0.05);
  border-color: rgba(255 255 255 / 0.12);
}
:root.dark .collect-cancel-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

:root.dark .collect-confirm-btn {
  background-color: rgb(245 158 11);
  color: rgb(17 24 39);
}
:root.dark .collect-confirm-btn:hover:not(:disabled) {
  background-color: rgb(251 191 36);
  transform: translateY(-1px);
}
:root.dark .collect-confirm-btn:disabled {
  background-color: rgb(245 158 11 / 0.25);
  color: rgb(17 24 39 / 0.4);
}

:global(html.collect-dialog-scroll-lock),
:global(body.collect-dialog-scroll-lock) {
  overflow: hidden !important;
}
:global(body.collect-dialog-scroll-lock) {
  position: fixed;
  width: 100%;
  top: calc(var(--collect-dialog-scroll-y, 0px) * -1);
  overscroll-behavior: none;
  touch-action: none;
}

@media (max-width: 640px) {
  .collect-dialog-panel {
    border-radius: 1rem;
    margin: 0.5rem;
  }
}
</style>