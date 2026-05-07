import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { UserTechStackOption } from '@/types/user-tech-stack'
import { mockTechStackOptions } from '@/components/user/settings/mock'

/**
 * 拉取已启用的技术栈字典（用户端公开接口）
 *
 * 后端尚未实现 `GET /api/tech-stack/list` 时自动回退到本地 mock，
 * 后端落地后无需改动调用方代码。
 *
 * @author yyyouth zg
 * @returns 启用状态的技术栈数组，按 sort 升序
 */
export async function listEnabledTechStack(): Promise<UserTechStackOption[]> {
  try {
    const res = await getJson<ApiResult<UserTechStackOption[]>>('/api/tech-stack/list')
    if (res.code !== 200 || !Array.isArray(res.data)) {
      throw new Error(res.msg || '技术栈列表数据为空')
    }
    return [...res.data].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  } catch (error) {
    console.warn('[user-tech-stack] 接口未就绪，回退本地 mock 数据', error)
    return [...mockTechStackOptions].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  }
}
