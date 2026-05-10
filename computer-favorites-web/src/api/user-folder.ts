import { deleteJson, getJson, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  FolderFormData,
  FolderUpdateData,
  FolderOption,
  UserFolderCreateResult,
} from '@/types/folder'
import type { CollectionCategory } from '@/types/collection'

export async function createUserFolder(payload: FolderFormData): Promise<UserFolderCreateResult> {
  const res = await postJson<ApiResult<UserFolderCreateResult>>('/api/user/folder', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '创建收藏夹失败')
  }
  if (!res.data) {
    throw new Error('创建收藏夹返回数据异常')
  }
  return res.data
}

export async function getFolderTree(): Promise<CollectionCategory[]> {
  const res = await getJson<ApiResult<CollectionCategory[]>>('/api/user/folder/tree')
  if (res.code !== 200) {
    throw new Error(res.msg || '查询文件夹树失败')
  }
  return res.data || []
}

export async function updateFolder(id: number, data: FolderUpdateData): Promise<void> {
  const res = await putJson<ApiResult<null>>(`/api/user/folder/${id}`, data)
  if (res.code !== 200) {
    throw new Error(res.msg || '更新收藏夹失败')
  }
}

export async function deleteFolder(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`/api/user/folder/${id}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '删除收藏夹失败')
  }
}

export async function toggleFolderHide(id: number, isHide: boolean): Promise<void> {
  const res = await putJson<ApiResult<null>>(`/api/user/folder/${id}/hide?isHide=${isHide}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '操作失败')
  }
}

/**
 * 切换收藏夹对外可见性（user-15 公开收藏夹）。
 * @param id 收藏夹 ID
 * @param isPublic true=对外公开，false=对外私密
 */
export async function toggleFolderPublic(id: number, isPublic: boolean): Promise<void> {
  const res = await putJson<ApiResult<null>>(`/api/user/folder/${id}/public?isPublic=${isPublic}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '操作失败')
  }
}

export async function getFolderOptions(): Promise<FolderOption[]> {
  const res = await getJson<ApiResult<FolderOption[]>>('/api/user/folder/options')
  if (res.code !== 200) {
    throw new Error(res.msg || '查询文件夹选项失败')
  }
  return res.data || []
}
