import { expect, test } from '@playwright/test'

test.describe('管理端认证链路', () => {
  test('退出回跳到登录页时应清空账号与密码输入框', async ({ page }) => {
    await page.goto('/computer/admin/login')

    await page.locator('#username').fill('root_admin')
    await page.locator('#password').fill('Admin@123456')

    await page.goto('/computer/admin/login?logoutReset=1')

    await expect(page.locator('#username')).toHaveValue('')
    await expect(page.locator('#password')).toHaveValue('')
  })

  test('未登录访问管理端受保护路由会跳转到管理员登录页并带 redirect', async ({ page }) => {
    await page.goto('/computer/admin/websites')
    await expect(page).toHaveURL(
      /\/computer\/admin\/login\?redirect=\/computer\/admin\/websites/,
    )
  })

  test('已登录管理员访问管理员登录页会按 redirect 回跳且携带管理员token头', async ({
    context,
    page,
  }) => {
    const sessionHeaders: Record<string, string>[] = []

    await context.addInitScript(() => {
      localStorage.setItem('adminAccessToken', 'admin-token-e2e-001')
      localStorage.setItem('adminTokenName', 'satoken')
    })

    await page.route('**/api/admin/auth/session/current', async (route) => {
      sessionHeaders.push(route.request().headers())
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '查询成功',
          data: {
            userId: 9001,
            tokenValue: 'admin-token-e2e-001',
            deviceType: 'web',
            timeoutSeconds: 1800,
          },
        }),
      })
    })

    await page.route('**/api/admin/auth/session/renew', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '续期成功',
        }),
      })
    })

    await page.goto('/computer/admin/login?redirect=/computer/admin/websites')
    await expect(page).toHaveURL('/computer/admin/websites')

    expect(sessionHeaders.length).toBeGreaterThan(0)
    expect(sessionHeaders[0].satoken).toBe('admin-token-e2e-001')
    expect(sessionHeaders[0].authorization).toBeUndefined()
  })

  test('管理员会话失效时应清理管理员登录态且不影响用户登录态', async ({ context, page }) => {
    await context.addInitScript(() => {
      localStorage.setItem('adminAccessToken', 'admin-token-expired')
      localStorage.setItem('adminTokenName', 'satoken')
      localStorage.setItem('adminUserId', '9001')
      localStorage.setItem('accessToken', 'user-token-keep')
      localStorage.setItem('tokenName', 'satoken')
    })

    await page.route('**/api/admin/auth/session/current', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 100202,
          msg: '登录状态已失效',
        }),
      })
    })

    await page.goto('/computer/admin/websites')
    await expect(page).toHaveURL(
      /\/computer\/admin\/login\?redirect=\/computer\/admin\/websites/,
    )

    const storageSnapshot = await page.evaluate(() => ({
      adminAccessToken: localStorage.getItem('adminAccessToken'),
      adminTokenName: localStorage.getItem('adminTokenName'),
      adminUserId: localStorage.getItem('adminUserId'),
      accessToken: localStorage.getItem('accessToken'),
      tokenName: localStorage.getItem('tokenName'),
    }))

    expect(storageSnapshot.adminAccessToken).toBeNull()
    expect(storageSnapshot.adminTokenName).toBeNull()
    expect(storageSnapshot.adminUserId).toBeNull()
    expect(storageSnapshot.accessToken).toBe('user-token-keep')
    expect(storageSnapshot.tokenName).toBe('satoken')
  })
})
