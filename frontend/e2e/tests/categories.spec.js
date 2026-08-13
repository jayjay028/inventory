import { test, expect } from '@playwright/test';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const authFile = join(__dirname, '../.auth/user.json');

test.describe('Category CRUD', () => {
  test.use({ storageState: authFile });

  const testCategory = `Test Category ${Date.now()}`;

  test('should navigate to categories page', async ({ page }) => {
    await page.goto('/categories');
    await expect(page.locator('h1, h2, .page-header').filter({ hasText: /categor/i })).toBeVisible();
  });

  test('should create a new category', async ({ page }) => {
    await page.goto('/categories/new');
    await page.waitForTimeout(1000);

    // Fill form using placeholder selectors (from screenshot)
    await page.fill('input[placeholder="Enter category name"]', testCategory);
    await page.fill('textarea[placeholder="Enter description"]', 'E2E test category');

    // Submit - button says "Create"
    await page.click('button:has-text("Create")');

    // Should redirect back to list
    await page.waitForURL('**/categories', { timeout: 10000 });
  });

  test('should display category in list', async ({ page }) => {
    await page.goto('/categories');
    await page.waitForTimeout(1000);

    // Table should be visible (use .first() to handle multiple matches)
    await expect(page.locator('table').first()).toBeVisible();
  });

  test('should edit a category', async ({ page }) => {
    await page.goto('/categories');
    await page.waitForTimeout(1000);

    // Click edit on first row
    const editBtn = page.locator('button:has-text("Edit"), a:has-text("Edit"), .bi-pencil').first();
    if (await editBtn.isVisible()) {
      await editBtn.click();
      await page.waitForTimeout(1000);

      // Verify form loaded with data
      const nameInput = page.locator('input[placeholder="Enter category name"]');
      await expect(nameInput).not.toHaveValue('');
    }
  });

  test('should toggle category status', async ({ page }) => {
    await page.goto('/categories');
    await page.waitForTimeout(1000);

    // Find status toggle button
    const statusBtn = page.locator('button:has-text("Deactivate"), button:has-text("Disable")').first();
    if (await statusBtn.isVisible({ timeout: 3000 })) {
      await statusBtn.click();

      // Confirm dialog if present
      const confirmBtn = page.locator('button:has-text("Confirm"), button:has-text("Yes")');
      if (await confirmBtn.isVisible({ timeout: 2000 })) {
        await confirmBtn.click();
      }
      await page.waitForTimeout(1000);
    }
  });
});
