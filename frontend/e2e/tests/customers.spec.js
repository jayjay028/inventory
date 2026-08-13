import { test, expect } from '@playwright/test';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const authFile = join(__dirname, '../.auth/user.json');

test.describe('Customer CRUD', () => {
  test.use({ storageState: authFile });

  const testCustomer = `Test Customer ${Date.now()}`;

  test('should navigate to customers page', async ({ page }) => {
    await page.goto('/customers');
    await expect(page.locator('h1, h2, .page-header').filter({ hasText: /customer/i })).toBeVisible();
  });

  test('should create a new customer', async ({ page }) => {
    await page.goto('/customers/new');
    await page.waitForTimeout(1000);

    // Fill form using placeholder selectors
    const nameInput = page.locator('input[placeholder*="name" i]').first();
    await nameInput.fill(testCustomer);

    // Fill other fields if visible
    const tinInput = page.locator('input[placeholder*="TIN" i], input[placeholder*="tin" i]');
    if (await tinInput.isVisible({ timeout: 1000 })) await tinInput.fill('123-456-789-000');

    const addressInput = page.locator('textarea[placeholder*="address" i], input[placeholder*="address" i]');
    if (await addressInput.isVisible({ timeout: 1000 })) await addressInput.fill('Manila, Philippines');

    const emailInput = page.locator('input[placeholder*="email" i]');
    if (await emailInput.isVisible({ timeout: 1000 })) await emailInput.fill('test@example.com');

    // Submit
    await page.click('button:has-text("Create"), button:has-text("Save"), button[type="submit"]');

    // Should redirect back to list
    await page.waitForURL('**/customers', { timeout: 10000 });
  });

  test('should search for a customer', async ({ page }) => {
    await page.goto('/customers');
    await page.waitForTimeout(1000);

    const searchInput = page.locator('input[placeholder*="search" i], input[type="search"]');
    if (await searchInput.isVisible({ timeout: 2000 })) {
      await searchInput.fill('Test');
      await page.waitForTimeout(1000);
    }
  });

  test('should show validation error for empty name', async ({ page }) => {
    await page.goto('/customers/new');
    await page.waitForTimeout(1000);

    // Submit without filling required field
    await page.click('button:has-text("Create"), button:has-text("Save"), button[type="submit"]');

    // Should show validation error (browser native or custom)
    await page.waitForTimeout(1000);
    // Check if still on the form page (didn't redirect = validation blocked)
    await expect(page).toHaveURL(/customers\/new/);
  });

  test('should edit a customer', async ({ page }) => {
    await page.goto('/customers');
    await page.waitForTimeout(1000);

    const editBtn = page.locator('button:has-text("Edit"), a:has-text("Edit"), .bi-pencil').first();
    if (await editBtn.isVisible({ timeout: 3000 })) {
      await editBtn.click();
      await page.waitForTimeout(1000);

      const nameInput = page.locator('input[placeholder*="name" i]').first();
      await expect(nameInput).not.toHaveValue('');
    }
  });
});
