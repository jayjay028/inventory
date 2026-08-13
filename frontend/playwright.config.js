import { defineConfig } from '@playwright/test';

/**
 * Playwright configuration for Inventory + POS System E2E tests.
 * Configured for HEADED mode (visible browser) by default.
 * 
 * Run: npx playwright test --headed
 * Or:  npm run test:e2e
 */
export default defineConfig({
  testDir: './e2e/tests',
  timeout: 30000,
  expect: { timeout: 5000 },
  fullyParallel: false, // Run sequentially to see each test
  retries: 0,
  workers: 1, // Single worker for headed mode visibility
  reporter: [['html', { open: 'never' }], ['list']],

  use: {
    baseURL: 'http://localhost:5173',
    headless: false, // HEADED MODE - browser visible
    viewport: { width: 1280, height: 720 },
    actionTimeout: 10000,
    navigationTimeout: 15000,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
    launchOptions: {
      slowMo: 500, // Slow down actions by 500ms so you can see what's happening
    },
  },

  projects: [
    {
      name: 'setup',
      testMatch: /.*\.setup\.js/,
    },
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
      dependencies: ['setup'],
    },
  ],

  /* Run local dev server before tests */
  // Uncomment if you want Playwright to start the dev server automatically:
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: true,
  //   timeout: 30000,
  // },
});
