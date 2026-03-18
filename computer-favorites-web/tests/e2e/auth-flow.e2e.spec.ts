import { expect, test } from '@playwright/test'

const successProfileResponse = {
  code: 200,
  msg: '查询成功',
  data: {
    user: {
      id: 1001,
      username: 'tester',
      nickname: '测试用户',
      avatar: '',
    },
  },
}

test.describe('认证链路', () => {
  test('未登录访问受保护路由会跳转到登录页并带 redirect', async ({ page }) => {
    await page.goto('/computer/profile')
    await expect(page).toHaveURL(/\/computer\/login\?redirect=%2Fcomputer%2Fprofile/)
  })

  test('已登录访问登录页会按 redirect 回跳且不携带冗余 Authorization 头', async ({ context, page }) => {
    const profileHeaders: Record<string, string>[] = []

    await context.addInitScript(() => {
      localStorage.setItem('accessToken', 'token-e2e-001')
      localStorage.setItem('tokenName', 'satoken')
    })

    await page.route('**/api/user/profile/current', async (route) => {
      profileHeaders.push(route.request().headers())
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(successProfileResponse),
      })
    })

    await page.route('**/api/auth/session/renew', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '续期成功',
        }),
      })
    })

    await page.goto('/computer/login?redirect=/computer/settings')
    await expect(page).toHaveURL('/computer/settings')
    expect(profileHeaders.length).toBeGreaterThan(0)
    expect(profileHeaders[0].satoken).toBe('token-e2e-001')
    expect(profileHeaders[0].authorization).toBeUndefined()
  })

  test('续期返回未登录时触发 401 降级并清理本地登录态', async ({ context, page }) => {
    await context.addInitScript(() => {
      localStorage.setItem('accessToken', 'token-e2e-401')
      localStorage.setItem('tokenName', 'satoken')
      localStorage.setItem('userNickname', '降级验证')
    })

    await page.route('**/api/user/profile/current', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(successProfileResponse),
      })
    })

    await page.route('**/api/auth/session/renew', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 100202,
          msg: '登录状态已失效',
        }),
      })
    })

    await page.goto('/computer/home')
    await expect(page).toHaveURL(/\/computer\/login\?redirect=%2Fcomputer%2Fhome/)

    const storageSnapshot = await page.evaluate(() => ({
      accessToken: localStorage.getItem('accessToken'),
      tokenName: localStorage.getItem('tokenName'),
      userNickname: localStorage.getItem('userNickname'),
    }))
    expect(storageSnapshot.accessToken).toBeNull()
    expect(storageSnapshot.tokenName).toBeNull()
    expect(storageSnapshot.userNickname).toBeNull()
  })
})
