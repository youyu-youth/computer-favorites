const SKIP_DEFAULT_AUTH_HEADER = 'X-CF-Skip-Auth'

export interface AdminAuthHeaderOptions {
  includeToken?: boolean
}

/**
 * 构建管理员接口请求头。
 *
 * 说明：
 * 1. 强制跳过用户端默认 token 注入，避免 admin/user token 混用。
 * 2. 按需附带管理员 token。
 */
export function buildAdminAuthHeaders(options: AdminAuthHeaderOptions = {}): Headers {
  const headers = new Headers()
  headers.set(SKIP_DEFAULT_AUTH_HEADER, '1')

  const includeToken = options.includeToken ?? true
  if (!includeToken) {
    return headers
  }

  const adminToken = localStorage.getItem('adminAccessToken')
  const adminTokenName = localStorage.getItem('adminTokenName') || 'satoken'
  if (!adminToken) {
    return headers
  }

  headers.set(adminTokenName, adminToken)
  return headers
}
