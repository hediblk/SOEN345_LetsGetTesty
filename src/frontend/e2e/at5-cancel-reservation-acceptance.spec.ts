import { expect, test } from '@playwright/test'

import { openCustomerHome, seedCustomerCredentials } from './helpers'

// Seed user (init.sql user 1) has a cancelled event
test.describe('AT-5: Cancel Reservations (US-5)', () => {
  test('A cancel option is visible for ongoing reservations', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    await page.locator('.nav-link').filter({ hasText: 'My Tickets' }).click()

    const cancelBtn = page.locator('.btn-cancel')
    await expect(cancelBtn).toBeVisible()
  })

  test('Reservations are greyed out when cancelled', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    await page.locator('.nav-link').filter({ hasText: 'My Tickets' }).click()

    const cancelledReservation = page.locator('.res-item.cancelled').first()
    await expect(cancelledReservation).toHaveCSS('opacity', '0.5')
  })
})
