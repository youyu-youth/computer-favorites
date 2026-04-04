import { expect, test } from '@playwright/test'

const profileResponse = {
  code: 200,
  msg: '查询成功',
  data: {
    user: {
      id: 1001,
      username: 'tester',
      nickname: '暗黑测试用户',
      avatar: '',
    },
  },
}

test.describe('用户端首页暗黑纯黑视觉', () => {
  test.beforeEach(async ({ context, page }) => {
    await context.addInitScript(() => {
      localStorage.setItem('theme-mode', 'dark')
      localStorage.setItem('theme', 'dark')
      localStorage.setItem('accessToken', 'token-e2e-home-dark-black')
      localStorage.setItem('tokenName', 'satoken')
    })

    await page.route('**/api/user/profile/current', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(profileResponse),
      })
    })

    await page.route('**/api/auth/session/renew', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 200, msg: '续期成功' }),
      })
    })
  })

  test('首页暗黑模式下背景与导航层为纯黑', async ({ page }) => {
    await page.goto('/computer/home')
    await expect(page.locator('[data-testid="home-dark-scope"]')).toBeVisible()

    const shellBg = await page.locator('[data-testid="home-dark-scope"]').evaluate((node) => {
      return getComputedStyle(node).backgroundColor
    })
    expect(shellBg).toBe('rgb(0, 0, 0)')

    const headerBg = await page.locator('[data-testid="navbar-root"]').evaluate((node) => {
      return getComputedStyle(node).backgroundColor
    })
    expect(headerBg).toBe('rgb(0, 0, 0)')

    await page.setViewportSize({ width: 390, height: 844 })
    await page.getByRole('button', { name: 'Open navigation' }).click()
    const mobileMenuBg = await page.locator('[data-testid="navbar-mobile-menu"]').evaluate((node) => {
      return getComputedStyle(node).backgroundColor
    })
    expect(mobileMenuBg).toBe('rgb(0, 0, 0)')

    await page.locator('[data-testid="profile-trigger"]').click()
    const profileMenuBg = await page.locator('[data-testid="profile-dropdown"]').evaluate((node) => {
      return getComputedStyle(node).backgroundColor
    })
    expect(profileMenuBg).toBe('rgb(0, 0, 0)')

    const footerBg = await page.locator('[data-testid="app-footer"]').evaluate((node) => {
      return getComputedStyle(node).backgroundColor
    })
    expect(footerBg).toBe('rgb(0, 0, 0)')
  })

  test('非首页暗黑模式不启用首页纯黑作用域', async ({ page }) => {
    await page.goto('/computer/profile')
    await expect(page.locator('[data-testid="home-dark-scope"]')).toHaveCount(0)
  })
})