import { postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { FolderFormData, UserFolderCreateResult } from '@/types/folder'

/**
 * 创建收藏文件夹
 *
 * @param payload 文件夹表单数据
 * @returns 创建结果
 */
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
