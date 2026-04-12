import { expect, test } from '@playwright/test'

import { loginAsAdmin, resetDb, seedAdminCredentials } from './helpers'

test.describe('AT-7: Add Events (US-7)', () => {
  test.beforeEach(async ({ request }) => {
    await resetDb(request)
  })

  test('Administrators have access to an "add event" page', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)

    await page.locator('.nav-link').filter({ hasText: 'Add Event' }).click()

    await expect(page).toHaveURL(/\/add-events/)
    await expect(page.locator('form')).toBeVisible()
  })

  test('Administrators can set the title, location, type, price, and capacity of an event', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)
    
    await page.locator('.nav-link').filter({ hasText: 'Add Event' }).click()

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

  test('Events with invalid fields cannot be created (less than one capacity)', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)
    
    await page.locator('.nav-link').filter({ hasText: 'Add Event' }).click()

    const titleInput = page.locator('input[name="title"]')
    const locationInput = page.locator('input[name="location"]')
    const dateInput = page.locator('input[name="date"]')
    const typeInput = page.locator('select[name="category"]')
    const priceInput = page.locator('input[name="price"]')
    const capacityInput = page.locator('input[name="capacity"]')

    await titleInput.fill("My Event")
    await locationInput.fill("Location")
    await dateInput.fill("2026-04-15")
    await typeInput.selectOption('Movies')
    await priceInput.fill("0")
    await capacityInput.fill("-1")

    await page.locator("button").filter({ hasText: "Add Event"}).click()
  
    await expect(page.locator(".form-notice")).not.toBeVisible()
  })

  test('Events with invalid fields cannot be created (less than 0 price)', async ({ page }) => {
    await loginAsAdmin(page, seedAdminCredentials)
    
    await page.locator('.nav-link').filter({ hasText: 'Add Event' }).click()

    const titleInput = page.locator('input[name="title"]')
    const locationInput = page.locator('input[name="location"]')
    const dateInput = page.locator('input[name="date"]')
    const typeInput = page.locator('select[name="category"]')
    const priceInput = page.locator('input[name="price"]')
    const capacityInput = page.locator('input[name="capacity"]')

    await titleInput.fill("My Event")
    await locationInput.fill("Location")
    await dateInput.fill("2026-04-15")
    await typeInput.selectOption('Movies')
    await priceInput.fill("-1")
    await capacityInput.fill("1")

    await page.locator("button").filter({ hasText: "Add Event"}).click()
  
    await expect(page.locator(".form-notice")).not.toBeVisible()
  })

  test('Add event flow', async ({ request, page }) => {
    await loginAsAdmin(page, seedAdminCredentials)
    
    await page.locator('.nav-link').filter({ hasText: 'Add Event' }).click()

    const titleInput = page.locator('input[name="title"]')
    const locationInput = page.locator('input[name="location"]')
    const dateInput = page.locator('input[name="date"]')
    const typeInput = page.locator('select[name="category"]')
    const priceInput = page.locator('input[name="price"]')
    const capacityInput = page.locator('input[name="capacity"]')

    await titleInput.fill("My Event")
    await locationInput.fill("Location")
    await dateInput.fill("2026-04-15")
    await typeInput.selectOption('Movies')
    await priceInput.fill("0")
    await capacityInput.fill("120")

    await page.locator("button").filter({ hasText: "Add Event"}).click()
  
    await expect(page.locator(".form-notice")).toHaveText("Added My Event to the event list.")
    await expect(page.locator("article").filter({ hasText: "My Event" })).toBeVisible()
  })
})
