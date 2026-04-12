import { expect, test } from '@playwright/test'
import { resetDb, resetEmails, openCustomerHome, seedCustomerCredentials, getLatestEmailTo } from './helpers';

test.describe('AT-6: Receive Confirmation for Reservations (US-6)', () => {
  test.beforeEach(async ({ request }) => {
    await resetDb(request)
    await resetEmails()
  })

  test('A confirmation email or SMS is sent once the user reserves an event', async ({ page }) => {
    await openCustomerHome(page, seedCustomerCredentials)

    const dune = page.locator('.event-card').filter({ hasText: 'Dune: Messiah' })
    await dune.getByRole('button', { name: 'Reserve' }).click()

    const email = await getLatestEmailTo(seedCustomerCredentials.email);

    expect(email.Content.Headers.Subject[0]).toContain('Registration confirmed');
    expect(email.Content.Headers.Subject[0]).toContain('Dune: Messiah');
  })
})
