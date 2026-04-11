import { expect, test } from '@playwright/test'
import { resetDb } from './helpers';

test.describe('AT-1: Registration and Login (US-1)', () => {
  test('Users are prompted to login when not logged in', async ({ page }) => {
    await page.goto('/auth')

    const loginForm = page.locator('form.auth-form');

    await expect(loginForm).toBeVisible()
  })

  test('Users can register or login using their phone number or email', async ({ page }) => {
    await page.goto('/auth')

    // sign in
    await page.getByRole('button', { name: 'Sign In' }).first().click()

    const form = page.locator('form.auth-form');

    // sign in - email
    await form.getByRole('button', { name: 'Email' }).click()
    await expect(form.locator('input[type="email"]')).toBeEditable()

    // sign in - phone number
    await form.getByRole('button', { name: 'Phone' }).click()
    await expect(form.locator('input[type="tel"]')).toBeEditable()

    // register
    await page.getByRole('button', { name: 'Register' }).click()

   // register - email
    await form.getByRole('button', { name: 'Email' }).click()
    await expect(form.locator('input[type="email"]')).toBeEditable()

    // register - phone number
    await form.getByRole('button', { name: 'Phone' }).click()
    await expect(form.locator('input[type="tel"]')).toBeEditable()
  })

  test('Register option is visible', async ({ page }) => {
    await page.goto('/auth')

    await page.getByRole('button', { name: 'Register' }).click()

    await expect(page.locator('form.auth-form')).toBeVisible()
  })

  test('Admin login option is visible', async ({ page }) => {
    await page.goto('/auth')

    await page.locator('.nav-link', { hasText: 'Admin' }).click()

    await expect(page.locator('form.auth-form')).toBeVisible()
  })

  test('Registeration flow', async ({ request, page }) => {
    await resetDb(request)
    await page.goto('/auth')
    const form = page.locator('form.auth-form');

    // register
    await page.getByRole('button', { name: 'Register' }).click()
    await form.getByRole('button', { name: 'Email' }).click()

    await form.locator('input[type="text"]').fill("Test User")
    await form.locator('input[type="email"]').fill("TestUser@example.com")
    await form.locator('input[type="password"]').fill("User123456_")

    await form.getByRole('button', { name: 'Create Account' }).click()

    const eventsLink = page.getByRole('link', { name: 'Events' })
    await expect(eventsLink).toBeVisible()
    await expect(eventsLink).toHaveAttribute('href', '/')
  })
})
