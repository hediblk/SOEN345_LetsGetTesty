import { expect, test } from '@playwright/test'

import { loginAsAdmin, resetDb, seedAdminCredentials } from './helpers'

test.describe('AT-8: Edit Events (US-8)', () => {
  test.beforeEach(async ({ request }) => {
    await resetDb(request)
  })

  test('Administrators have access to an "edit event" page', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    await expect(page).toHaveURL(/\/edit-events/)
    await expect(page.locator('form')).toBeVisible()
  })

  test('Administrators can select an event to edit', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    const jazz = page.locator('.preview-card').filter({ hasText: 'Montreal Jazz Night' })
    await expect(jazz.getByRole('button', { name: 'Select to Edit' })).toBeVisible()
  })

  test('The event that is being edited is marked with the label "currently editing"', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    const jazz = page.locator('.preview-card').filter({ hasText: 'Montreal Jazz Night' })
    await jazz.getByRole('button', { name: 'Select to Edit' }).click()
    await expect(jazz.getByRole('button', { name: 'Currently Editing' })).toBeVisible()
  })

  test('Every field is editable (title, location, date, category, price, and capacity)', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    const jazz = page.locator('.preview-card').filter({ hasText: 'Montreal Jazz Night' })
    await jazz.getByRole('button', { name: 'Select to Edit' }).click()

    const titleInput = page.locator('input[name="title"]')
    const locationInput = page.locator('input[name="location"]')
    const dateInput = page.locator('input[name="date"]')
    const typeInput = page.locator('select[name="category"]')
    const priceInput = page.locator('input[name="price"]')
    const capacityInput = page.locator('input[name="capacity"]')

    await expect(titleInput).toBeEditable()
    await expect(locationInput).toBeEditable()
    await expect(dateInput).toBeEditable()
    await expect(typeInput).toBeEditable()
    await expect(priceInput).toBeEditable()
    await expect(capacityInput).toBeEditable()
  })
  
  test('Edit events flow', async ({ request, page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Edit Event' }).click()

    const jazz = page.locator('.preview-card').filter({ hasText: 'Montreal Jazz Night' })
    await jazz.getByRole('button', { name: 'Select to Edit' }).click()

    const titleInput = page.locator('input[name="title"]')
    const locationInput = page.locator('input[name="location"]')
    const dateInput = page.locator('input[name="date"]')
    const typeInput = page.locator('select[name="category"]')
    const priceInput = page.locator('input[name="price"]')
    const capacityInput = page.locator('input[name="capacity"]')

    await titleInput.fill('New Title')
    await locationInput.fill('New Location')
    await dateInput.fill('2000-01-01')
    await typeInput.selectOption('Movies')
    await priceInput.fill("5")
    await capacityInput.fill("100")

    await page.locator("button").filter({ hasText: "Save Changes"}).click()

    await expect(page.locator(".form-notice")).toHaveText("Updated New Title.")
    await expect(page.locator("article").filter({ hasText: "New Title" })).toBeVisible()
  })
})
