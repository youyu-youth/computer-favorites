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
          tokenValue: 'admin-token-e2e-reports',
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
          lastLoginTime: '2026-04-16 08:00:00',
          lastLoginIp: '127.0.0.1',
          createTime: '2026-03-01 00:00:00',
          updateTime: '2026-04-16 08:00:00',
        },
      }),
    })
  })
}

test.describe('管理端举报工作台真实接口接入', () => {
  test.beforeEach(async ({ context, page }) => {
    await context.addInitScript(() => {
      localStorage.setItem('adminAccessToken', 'admin-token-e2e-reports')
      localStorage.setItem('adminTokenName', 'satoken')
      localStorage.setItem('adminUserId', '9001')
    })

    await mockAdminAuth(page)
  })

  test('应请求真实举报接口并清除 mock 文案', async ({ page }) => {
    const requestedListUrls: string[] = []
    const requestedStatisticsUrls: string[] = []
    const requestedDetailUrls: string[] = []
    const handlePayloads: Array<Record<string, unknown>> = []

    await page.route('**/api/admin/report/list**', async (route) => {
      requestedListUrls.push(route.request().url())
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '查询成功',
          data: {
            records: [
              {
                id: 101,
                userId: 3001,
                userName: '举报人甲',
                userEmail: 'reporter@example.com',
                type: 1,
                targetId: 8101,
                targetName: 'Digital Shortcut Hub',
                targetUrl: 'https://example.com/digital-shortcut-hub',
                uploaderId: 9002,
                uploaderName: '站长风纪官',
                reason: '内容误导: 页面宣称永久免费，但实际跳转后要求付费订阅。',
                images: ['https://picsum.photos/seed/report-e2e-list/960/600'],
                status: 0,
                handleResult: null,
                handlerId: null,
                handlerName: null,
                handleTime: null,
                createTime: '2026-04-16T08:42:00',
                updateTime: '2026-04-16T08:42:00',
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 6,
            totalPages: 1,
          },
        }),
      })
    })

    await page.route('**/api/admin/report/statistics**', async (route) => {
      requestedStatisticsUrls.push(route.request().url())
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '查询成功',
          data: {
            total: 8,
            pending: 3,
            processed: 4,
            rejected: 1,
            last24Hours: 2,
            websiteCount: 5,
            commentCount: 3,
            processRate: 63,
          },
        }),
      })
    })

    await page.route('**/api/admin/report/101', async (route) => {
      requestedDetailUrls.push(route.request().url())
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '查询成功',
          data: {
            id: 101,
            userId: 3001,
            userName: '举报人甲',
            userEmail: 'reporter@example.com',
            type: 1,
            targetId: 8101,
            targetName: 'Digital Shortcut Hub',
            targetUrl: 'https://example.com/digital-shortcut-hub',
            uploaderId: 9002,
            uploaderName: '站长风纪官',
            reason: '内容误导: 页面宣称永久免费，但实际跳转后要求付费订阅。',
            images: ['https://picsum.photos/seed/report-e2e-detail/960/600'],
            status: 0,
            handleResult: null,
            handlerId: null,
            handlerName: null,
            handleTime: null,
            createTime: '2026-04-16T08:42:00',
            updateTime: '2026-04-16T08:42:00',
            targetStatusLabel: '在线可访问',
            evidenceSummary: '已提交 1 张截图证据，可用于辅助判定。',
            timeline: [
              {
                id: 'create-101',
                title: '举报已提交',
                description: '举报人甲 提交了网站举报，等待管理员处理。',
                time: '2026-04-16T08:42:00',
                tone: 'done',
              },
            ],
          },
        }),
      })
    })

    await page.route('**/api/admin/report/101/handle', async (route) => {
      const payload = route.request().postDataJSON() as Record<string, unknown>
      handlePayloads.push(payload)
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          msg: '处置成功',
          data: {
            reportId: 101,
            status: 1,
            handleResult: payload.handleResult,
            handleTime: '2026-04-16T10:00:00',
            actionExecuted: true,
          },
        }),
      })
    })

    await page.goto('/computer/admin/reports')
    await expect(page).toHaveURL('/computer/admin/reports')
    await page.waitForLoadState('networkidle')

    await expect.poll(() => requestedStatisticsUrls.length).toBeGreaterThan(0)
    await expect.poll(() => requestedListUrls.length).toBeGreaterThan(0)
    await expect(page.locator('body')).not.toContainText(/mock/i)
    await expect(page.getByText('举报人甲')).toBeVisible()
    await expect(page.getByText('Digital Shortcut Hub')).toBeVisible()

    await page.getByRole('button', { name: '查看' }).first().click()
    await expect.poll(() => requestedDetailUrls.length).toBeGreaterThan(0)
    await expect(page.getByText('举报详情')).toBeVisible()
    await expect(page.getByText('在线可访问')).toBeVisible()
    await expect(page.locator('body')).not.toContainText(/mock/i)

    await page.getByRole('button', { name: '通过举报' }).click()
    await page
      .getByPlaceholder('请输入至少 10 个字符，说明为什么通过或驳回该举报。')
      .fill('经核实存在误导信息，已执行下架处置。')
    await page.getByRole('button', { name: '确认通过' }).click()

    await expect.poll(() => handlePayloads.length).toBe(1)
    await expect(handlePayloads[0]?.action).toBe('pass')
    await expect(handlePayloads[0]?.executeAction).toBeTruthy()
    await expect(page.locator('body')).not.toContainText(/mock/i)
  })
})
