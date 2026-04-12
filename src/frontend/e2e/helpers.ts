import { type APIRequestContext, expect, type Page } from '@playwright/test'

export type CustomerCredentials = { email: string; password: string }
export type AdminCredentials = { email: string; password: string }

export const customerEmail = process.env.E2E_USER_EMAIL?.trim() || 'user1@example.com'
export const customerPassword = process.env.E2E_USER_PASSWORD || 'user123'

export const adminEmail = 'admin1@example.com'
export const adminPassword = 'admin123'

/** Matches `database/init.sql` user id 1 (CONFIRMED reservation on event 2, etc.). */
export const seedCustomerCredentials: CustomerCredentials = {
  email: 'user1@example.com',
  password: 'user123',
}

export const seedAdminCredentials: AdminCredentials = {
  email: 'admin1@example.com',
  password: 'admin123',
}

export async function loginAsCustomer(page: Page, creds?: CustomerCredentials) {
  const email = creds?.email ?? customerEmail
  const password = creds?.password ?? customerPassword
  await page.goto('/auth')
  await page.getByPlaceholder('you@example.com').fill(email)
  await page.locator('input[type="password"]').fill(password)
  await page.locator('form.auth-form button.auth-btn').click()
  await expect(page).toHaveURL(/\/$/)
}

export async function loginAsAdmin(page: Page, creds?: AdminCredentials) {
  const email = creds?.email ?? adminEmail
  const password = creds?.password ?? adminPassword
  await page.goto('/admin')
  await page.getByPlaceholder('you@example.com').fill(email)
  await page.locator('input[type="password"]').fill(password)
  await page.locator('form.auth-form button.auth-btn').click()
}

export async function resetHomeEventFilters(page: Page) {
  await page.locator('.cat-pills').getByRole('button', { name: 'All' }).click()
  await page.locator('.search-input').fill('')
  await page.locator('.date-input').fill('')
}

export async function openCustomerHome(page: Page, creds?: CustomerCredentials) {
  await loginAsCustomer(page, creds)
  await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
  await resetHomeEventFilters(page)
}

export async function resetDb(request: APIRequestContext) {
  const res = await request.post('/api/test/reset-db')
  expect(res.ok()).toBeTruthy()
}
