import { expect, type Page } from '@playwright/test'

export const customerEmail = process.env.E2E_USER_EMAIL?.trim() || 'user1@example.com'
export const customerPassword = process.env.E2E_USER_PASSWORD || 'user123'

export async function loginAsCustomer(page: Page) {
  await page.goto('/auth')
  await page.getByPlaceholder('you@example.com').fill(customerEmail)
  await page.locator('input[type="password"]').fill(customerPassword)
  await page.locator('form.auth-form button.auth-btn').click()
  await expect(page).toHaveURL(/\/$/)
}

export async function resetHomeEventFilters(page: Page) {
  await page.locator('.cat-pills').getByRole('button', { name: 'All' }).click()
  await page.locator('.search-input').fill('')
  await page.locator('.date-input').fill('')
}

export async function openCustomerHome(page: Page) {
  await loginAsCustomer(page)
  await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
  await resetHomeEventFilters(page)
}
