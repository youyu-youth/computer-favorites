import { expect, test } from '@playwright/test'

const mockAdminAuth = async (page: import('@playwright/test').Page) => {
  await page.route('**/api/admin/auth/session/current**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: {
          userId: 9001,
          tokenValue: 'admin-token-e2e-breadcrumb',
          deviceType: 'web',
          timeoutSeconds: 1800,
        },
      }),
    })
  })

  await page.route('**/api/admin/auth/session/renew**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, msg: '续期成功' }),
    })
  })

  await page.route('**/api/admin/profile/current**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: {
          id: 9001,
          username: 'root_admin',
          email: 'admin@test.com',
          avatar: '',
          nickname: '超级管理员',
          role: 'super_admin',
          status: 1,
          lastLoginTime: '2026-04-02 18:00:00',
          lastLoginIp: '127.0.0.1',
          createTime: '2026-03-01 00:00:00',
          updateTime: '2026-04-02 18:00:00',
        },
      }),
    })
  })
}

const mockWebsiteApis = async (page: import('@playwright/test').Page) => {
  await page.route('**/api/admin/website/categories**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: [{ id: 1, name: '软件架构', count: 1 }],
      }),
    })
  })

  await page.route('**/api/admin/website/stats**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: {
          total: 1,
          online: 1,
          offline: 0,
          pendingAudit: 0,
          rejectedAudit: 0,
          deleted: 0,
          latestUpdateTime: '2026-04-02 18:00:00',
        },
      }),
    })
  })

  await page.route('**/api/admin/website/list**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: {
          records: [
            {
              id: 12,
              name: 'test',
              url: 'https://example.com',
              icon: '',
              summary: 'summary',
              description: 'desc',
              categoryId: 1,
              categoryName: '软件架构',
              clickCount: 0,
              likeCount: 0,
              collectCount: 0,
              commentCount: 0,
              score: 5,
              tags: 'A',
              isTop: 0,
              isRecommend: 0,
              status: 1,
              source: 0,
              auditStatus: 1,
              deleted: 0,
              updateTime: '2026-04-02 18:00:00',
            },
          ],
          total: 1,
          pageNum: 1,
          pageSize: 12,
          totalPages: 1,
        },
      }),
    })
  })

  await page.route('**/api/admin/website/12', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: '查询成功',
        data: {
          id: 12,
          name: 'test',
          url: 'https://example.com',
          icon: '',
          summary: 'summary',
          description: 'desc',
          categoryId: 1,
          categoryName: '软件架构',
          clickCount: 0,
          likeCount: 0,
          collectCount: 0,
          commentCount: 0,
          score: 5,
          scoreCount: 1,
          tags: 'A',
          isTop: 0,
          isRecommend: 0,
          status: 1,
          sort: 0,
          source: 0,
          submitterId: 9001,
          auditStatus: 1,
          auditRemark: '',
          createTime: '2026-04-02 10:00:00',
          updateTime: '2026-04-02 18:00:00',
          deleted: 0,
          shelfTime: '2026-04-02 10:00:00',
          takedownTime: '',
          auditAdminId: 9001,
        },
      }),
    })
  })
}

test.describe('管理端面包屑替换（PrimeVue）', () => {
  test.describe.configure({ mode: 'serial' })

  test.beforeEach(async ({ context, page }) => {
    await context.addInitScript(() => {
      localStorage.setItem('adminAccessToken', 'admin-token-e2e-breadcrumb')
      localStorage.setItem('adminTokenName', 'satoken')
      localStorage.setItem('adminUserId', '9001')
    })

    await mockAdminAuth(page)
    await mockWebsiteApis(page)
  })

  test('应渲染全局面包屑并支持层级点击', async ({ page }) => {
    await page.goto('/computer/admin/websites')
    await expect(page).toHaveURL('/computer/admin/websites')
    await page.waitForLoadState('networkidle')

    const breadcrumb = page.getByTestId('admin-breadcrumb')
    await expect(breadcrumb).toBeVisible()

    await breadcrumb.locator('[data-breadcrumb-key="admin-root"]').click()
    await expect(page).toHaveURL('/computer/admin/websites')

    await breadcrumb.locator('[data-breadcrumb-level="3"]').click()
    await expect(page).toHaveURL('/computer/admin/websites')
  })

  test('添加网站页点击网站管理应返回网站首页列表', async ({ page }) => {
    await page.goto('/computer/admin/websites')
    await expect(page).toHaveURL('/computer/admin/websites')
    await page.waitForLoadState('networkidle')

    await page.getByRole('button', { name: '添加网站' }).click()
    await expect(page.getByText('取消录入')).toBeVisible()

    const breadcrumb = page.getByTestId('admin-breadcrumb')
    await expect(breadcrumb).toBeVisible()
    await breadcrumb.locator('[data-breadcrumb-key="menu-websites"]').click()

    await expect(page).toHaveURL('/computer/admin/websites')
    await expect(page.getByText('取消录入')).toHaveCount(0)
    await expect(page.getByText('MCP tools')).toBeVisible()
  })

  test('编辑页第三级面包屑可点击且保持当前页面', async ({ page }) => {
    await page.goto('/computer/admin/websites/12/edit')
    await expect(page).toHaveURL('/computer/admin/websites/12/edit')
    await page.waitForLoadState('networkidle')

    const breadcrumb = page.getByTestId('admin-breadcrumb')
    await expect(breadcrumb).toBeVisible()

    await breadcrumb.locator('[data-breadcrumb-key="edit-website"]').click()
    await expect(page).toHaveURL('/computer/admin/websites/12/edit')
  })
})
