import path from 'node:path'
import { fileURLToPath } from 'node:url'

import dotenv from 'dotenv'
import { defineConfig, devices } from '@playwright/test'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
// Load local secrets from .env (gitignored). See .env.example.
dotenv.config({ path: path.join(__dirname, '.env') })

/**
 * E2E tests expect:
 * - Vite on http://127.0.0.1:5173 (Playwright starts it unless you set PW_SKIP_WEBSERVER=1)
 * - Spring Boot API on http://127.0.0.1:8080 (Vite proxies /api → 8080)
 * - Database seeded with src/backend/database/init.sql (default user1@example.com / user123)
 *
 * If you see "Port 5173 is already in use": stop the other Vite (`lsof -iTCP:5173 -sTCP:LISTEN`),
 * or keep one dev server running and use `npm run test:e2e:reuse-dev` instead of `npm run test:e2e`.
 */
const skipWebServer =
  process.env.PW_SKIP_WEBSERVER === '1' || process.env.PW_SKIP_WEBSERVER === 'true'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  // `line` prints each test as it runs; default webServer wait is silent without stdout pipe below.
  reporter: process.env.CI
    ? [
        ['github'],
        ['html', { open: 'never' }],
      ]
    : 'line',
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: skipWebServer
    ? undefined
    : {
        command: 'npm run dev',
        url: 'http://127.0.0.1:5173',
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
        stdout: 'pipe',
        stderr: 'pipe',
      },
})
