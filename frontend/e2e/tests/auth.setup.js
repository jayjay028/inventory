import { test as setup, expect } from '@playwright/test';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const authFile = join(__dirname, '../.auth/user.json');

/**
 * Authentication setup - runs once before all tests.
 * Logs in as admin and saves the auth state (localStorage tokens).
 */
setup('authenticate', async ({ page }) => {
  await page.goto('/login');

  // Wait for the login form to load
  await expect(page.locator('#username')).toBeVisible({ timeout: 10000 });

  // Fill login form
  await page.fill('#username', 'admin');
  await page.fill('#password', 'admin');

  // Submit
  await page.click('button[type="submit"]');

  // Wait for redirect to dashboard or for URL to change from /login
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 });

  // Verify we're on dashboard
  await expect(page).toHaveURL(/dashboard/);

  // Save auth state (localStorage with JWT tokens)
  await page.context().storageState({ path: authFile });
});
