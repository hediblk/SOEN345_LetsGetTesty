import { expect, test } from '@playwright/test'

import { loginAsCustomer, resetHomeEventFilters } from './helpers'

test.describe('AT-2: Events acceptance (US-2)', () => {
  test('Events tab is visible after customer sign-in', async ({ page }) => {
    await loginAsCustomer(page)

    const eventsLink = page.getByRole('link', { name: 'Events' })
    await expect(eventsLink).toBeVisible()
    await expect(eventsLink).toHaveAttribute('href', '/')
  })

  test('Events list shows name, type, date, price, and location', async ({ page }) => {
    await loginAsCustomer(page)

    await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
    await expect(page.locator('[role="alert"]')).toHaveCount(0)

    await resetHomeEventFilters(page)

    const cards = page.locator('.event-card')
    await expect(cards.first()).toBeVisible({ timeout: 30_000 })
    const count = await cards.count()
    expect(count).toBeGreaterThanOrEqual(6)

    const dune = cards.filter({ hasText: 'Dune: Messiah' })
    await expect(dune).toHaveCount(1)
    await expect(dune).toContainText('Movies')
    await expect(dune).toContainText('Cinema Imperial, Montreal')
    await expect(dune).toContainText('$22')
    await expect(dune).toContainText('Apr 15, 2026')

    const concert = cards.filter({
      has: page.locator('.ev-cat', { hasText: 'Concerts' }),
    })
    await expect(concert.first()).toBeVisible({ timeout: 15_000 })
    const concertCard = concert.first()
    await expect(concertCard.locator('.ev-title')).not.toHaveText('')
    await expect(concertCard.locator('.ev-cat')).toHaveText('Concerts')
    await expect(concertCard.locator('.ev-date')).not.toHaveText('')
    await expect(concertCard).toContainText(/\$[\d]+|Free/)
    await expect(concertCard.locator('.ev-location')).not.toHaveText('')
  })

  test('Available and cancelled events both appear on the list', async ({ request, page }) => {
    const adminLogin = await request.post('/api/auth/admin/login', {
      data: {
        contact: 'admin1@example.com',
        contactType: 'EMAIL',
        password: 'admin123',
      },
    })
    expect(adminLogin.ok(), await adminLogin.text()).toBeTruthy()
    const { token } = (await adminLogin.json()) as { token: string }

    const cancel = await request.patch('/api/events/8/cancel', {
      headers: { Authorization: `Bearer ${token}` },
    })
    expect(cancel.ok(), await cancel.text()).toBeTruthy()

    await loginAsCustomer(page)
    await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
    await resetHomeEventFilters(page)

    await expect(page.getByRole('button', { name: 'Reserve' }).first()).toBeVisible()
    await expect(page.locator('.cancelled-badge-footer').first()).toBeVisible()
  })

  test('Cancelled events are not reservable on the customer list', async ({ page }) => {
    await loginAsCustomer(page)

    await expect(page.getByText('Loading events…')).not.toBeVisible({ timeout: 30_000 })
    await resetHomeEventFilters(page)

    const reserveOnCancelledCard = page
      .locator('.event-card:has(.cancelled-badge-footer)')
      .locator('.btn-reserve')
    await expect(reserveOnCancelledCard).toHaveCount(0)
  })
})
