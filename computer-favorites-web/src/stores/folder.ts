/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 收藏夹数据 Pinia store — 全局唯一真相源
 * 所有消费收藏夹树、下拉选项、统计数据的组件统一使用此 store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CollectionCategory, CollectStats } from '@/types/collection'
import type { FolderOption } from '@/types/folder'
import { getFolderTree, getFolderOptions, updateFolder, deleteFolder, toggleFolderHide } from '@/api/user-folder'
import { getCollectStats } from '@/api/user-collect'
import { useToast } from '@/composables/useToast'

export const useFolderStore = defineStore('folder', () => {
  const toast = useToast()

  const categories = ref<CollectionCategory[]>([])
  const folderOptions = ref<FolderOption[]>([])
  const collectStats = ref<CollectStats>({ collectCount: 0, folderCount: 0 })
  const isLoading = ref(false)

  /**
   * 递归过滤隐藏的收藏夹
   */
  const filterHidden = (cats: CollectionCategory[]): CollectionCategory[] => {
    return cats
      .filter((c) => !c.isHide)
      .map((c) => ({
        ...c,
        children: c.children ? filterHidden(c.children) : [],
      }))
  }

  const visibleCategories = computed(() => filterHidden(categories.value))

  /**
   * 收集所有隐藏收藏夹的 ID
   */
  const collectHiddenIds = (cats: CollectionCategory[]): number[] => {
    const result: number[] = []
    const walk = (items: CollectionCategory[]) => {
      for (const item of items) {
        if (item.isHide) result.push(item.id)
        if (item.children) walk(item.children)
      }
    }
    walk(cats)
    return result
  }

  const hiddenCategoryIds = computed(() => collectHiddenIds(categories.value))

  const loadFolderTree = async (force = false) => {
    if (!force && isLoading.value) return
    isLoading.value = true
    try {
      categories.value = await getFolderTree()
    } catch (e) {
      console.error('加载文件夹树失败:', e)
      toast.add({ title: '加载失败', description: '文件夹加载失败，请刷新重试', type: 'error' })
    } finally {
      isLoading.value = false
    }
  }

  const loadFolderOptions = async () => {
    try {
      folderOptions.value = await getFolderOptions()
    } catch (e) {
      console.error('加载文件夹选项失败:', e)
      folderOptions.value = []
    }
  }

  const loadCollectStats = async () => {
    try {
      collectStats.value = await getCollectStats()
    } catch (e) {
      console.error('加载收藏统计失败:', e)
      collectStats.value = { collectCount: 0, folderCount: 0 }
    }
  }

  /**
   * 刷新全部文件夹相关数据
   */
  const refreshFolderData = async () => {
    await Promise.all([loadFolderTree(true), loadFolderOptions(), loadCollectStats()])
  }

  /**
   * 新建收藏夹后的数据刷新（toast 由调用方处理，避免重复提示）
   */
  const handleFolderCreated = async () => {
    await refreshFolderData()
  }

  /**
   * 重命名收藏夹
   */
  const handleRenameCategory = async (id: number, newName: string) => {
    try {
      await updateFolder(id, { name: newName })
      toast.add({ title: '重命名成功', description: `收藏夹已更名为「${newName}」`, type: 'success' })
      await loadFolderTree()
    } catch (e) {
      toast.add({ title: '重命名失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  /**
   * 删除收藏夹
   */
  const handleDeleteFolder = async (id: number) => {
    try {
      await deleteFolder(id)
      toast.add({ title: '已删除', description: '收藏夹已删除', type: 'success' })
      await refreshFolderData()
    } catch (e) {
      toast.add({ title: '删除失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  /**
   * 隐藏收藏夹
   */
  const handleHideFolder = async (id: number) => {
    try {
      await toggleFolderHide(id, true)
      toast.add({ title: '已隐藏', description: '收藏夹及其内容已隐藏', type: 'success' })
      await loadFolderTree()
    } catch (e) {
      toast.add({ title: '操作失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  /**
   * 显示隐藏的收藏夹
   */
  const handleShowHiddenFolders = async (hiddenIds: number[]) => {
    try {
      await Promise.all(hiddenIds.map((id) => toggleFolderHide(id, false)))
      toast.add({ title: '已恢复', description: '所有隐藏的收藏夹已恢复显示', type: 'success' })
      await loadFolderTree()
    } catch (e) {
      toast.add({ title: '操作失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  return {
    categories,
    folderOptions,
    collectStats,
    isLoading,
    visibleCategories,
    hiddenCategoryIds,
    loadFolderTree,
    loadFolderOptions,
    loadCollectStats,
    refreshFolderData,
    handleFolderCreated,
    handleRenameCategory,
    handleDeleteFolder,
    handleHideFolder,
    handleShowHiddenFolders,
  }
})
