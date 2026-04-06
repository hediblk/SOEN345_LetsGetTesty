import { expect, test } from '@playwright/test'

import { openCustomerHome } from './helpers'

test.describe('AT-3: Event filters (US-3)', () => {
  test('Users can filter events by category', async ({ page }) => {
    await openCustomerHome(page)

    await page.locator('.cat-pills').getByRole('button', { name: 'Sports' }).click()

    const cards = page.locator('.event-card')
    await expect(cards.first()).toBeVisible({ timeout: 15_000 })
    await expect(cards.filter({ hasText: 'Canadiens' })).toHaveCount(1)
    await expect(cards.filter({ hasText: 'Grand Prix' })).toHaveCount(1)
    await expect(cards.filter({ hasText: 'Dune' })).toHaveCount(0)
  })

  test('Users can filter events by location', async ({ page }) => {
    await openCustomerHome(page)

    await page.locator('.search-input').fill('Place des Arts')

    const cards = page.locator('.event-card')
    await expect(cards).toHaveCount(1)
    await expect(cards.first()).toContainText('Montreal Jazz Night')
  })

  test('Users can filter events by date', async ({ page }) => {
    await openCustomerHome(page)

    await page.locator('.date-input').fill('2026-06-01')

    const cards = page.locator('.event-card')
    await expect(cards.filter({ hasText: 'Grand Prix' })).toHaveCount(1)
    await expect(cards.filter({ hasText: 'Dune' })).toHaveCount(0)
  })
})
