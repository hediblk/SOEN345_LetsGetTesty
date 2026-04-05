import { expect, test } from '@playwright/test'

import { openCustomerHome, seedCustomerCredentials } from './helpers'

// Seed user (init.sql user 1) has Jazz booked; E2E_USER_* in .env is ignored here.
test.describe('AT-4: Reservations (US-4)', () => {
  test('Reserve is visible for an available event', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    const dune = page.locator('.event-card').filter({ hasText: 'Dune: Messiah' })
    await expect(dune.getByRole('button', { name: 'Reserve' })).toBeVisible()
  })

  test('Booked status is shown for an event the user already reserved', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    const jazz = page.locator('.event-card').filter({ hasText: 'Montreal Jazz Night' })
    await expect(jazz.locator('.booked-badge-footer')).toHaveText('Booked', { timeout: 15_000 })
  })

  test('Reservations are visible on My Tickets', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    await page.getByRole('link', { name: 'My Tickets' }).click()
    await expect(page).toHaveURL(/\/reservations/)
    await expect(page.getByRole('heading', { name: 'My Tickets' })).toBeVisible()
    await expect(page.getByText('Loading reservations…')).not.toBeVisible({ timeout: 15_000 })

    await expect(page.getByRole('heading', { name: 'Montreal Jazz Night' })).toBeVisible({
      timeout: 15_000,
    })
  })
})
