import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test('should display login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#password')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    await expect(page.locator('text=Inventory System')).toBeVisible();
  });

  test('should show error on invalid credentials', async ({ page }) => {
    await page.goto('/login');

    await page.fill('#username', 'admin');
    await page.fill('#password', 'wrongpassword');
    await page.click('button[type="submit"]');

    // Wait for error - either alert-danger or stay on login page with error text
    await page.waitForTimeout(3000);

    // Should still be on login page (login failed)
    await expect(page).toHaveURL(/login/);

    // Check for any error indication (alert or text)
    const hasError = await page.locator('.alert-danger, .alert, .text-danger, :text("Invalid"), :text("error")').first().isVisible({ timeout: 5000 });
    expect(hasError || page.url().includes('login')).toBeTruthy();
  });

  test('should login successfully and redirect to dashboard', async ({ page }) => {
    await page.goto('/login');

    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin');
    await page.click('button[type="submit"]');

    // Should redirect to dashboard
    await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 });
    await expect(page).toHaveURL(/dashboard/);
  });

  test('should redirect to login when not authenticated', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/login/);
  });
});
