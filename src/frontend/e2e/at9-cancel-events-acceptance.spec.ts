import { expect, test } from '@playwright/test'

import { openCustomerHome, seedCustomerCredentials, loginAsAdmin, seedAdminCredentials, resetHomeEventFilters } from './helpers'

test.describe('AT-9: Cancel Events (AT-9)', () => {
  test('A cancel event option is visible when editing an event', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    const jazz = page.locator('.preview-card').filter({ hasText: 'Montreal Jazz Night' })
    await jazz.getByRole('button', { name: 'Select to Edit' }).click()
    await expect(page.locator("button").filter({ hasText: 'Cancel Selected Event' })).toBeEnabled()
  })

  test('Cancelled events are marked as cancelled in the events page', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)
    await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
    await resetHomeEventFilters(page)
  
    const formula1 = page.locator('.event-card').filter({ hasText: 'Grand Prix de Montreal' })
    await expect(formula1.locator('.cancelled-badge-footer')).toHaveText('Cancelled', { timeout: 15_000 })
  })
})
