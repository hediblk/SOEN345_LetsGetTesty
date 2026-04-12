import { expect, test } from '@playwright/test'

import { openCustomerHome, seedCustomerCredentials, loginAsAdmin, seedAdminCredentials, resetHomeEventFilters, resetDb } from './helpers'

test.describe('AT-9: Cancel Events (US-9)', () => {
  test.beforeEach(async ({ request }) => {
    await resetDb(request)
  })

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

  test('Cancel events flow', async ({ request, page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    // cancel event on admin side
    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    await page.locator('.preview-card')
      .filter({ hasText: 'Montreal Jazz Night' })
      .getByRole('button', { name: 'Select to Edit' })
      .click()
    await page.locator("button").filter({ hasText: 'Cancel Selected Event' }).click()
    
    // reset browser
    await page.evaluate(() => localStorage.clear())
    await page.context().clearCookies()
    await page.goto('/')

    // view events on the user side
    await openCustomerHome(page, seedCustomerCredentials)
    await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
    await resetHomeEventFilters(page)
  
    const jazz = page.locator('.event-card').filter({ hasText: 'Montreal Jazz Night' })
    await expect(jazz.locator('.cancelled-badge-footer')).toHaveText('Cancelled', { timeout: 15_000 })
  })
})
