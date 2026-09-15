import { expect, test } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login?returnTo=%2Fcarteira");
  await page.getByLabel("E-mail").fill("user@example.test");
  await page.getByLabel("Senha").fill("Password1!");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL("/carteira");
}

test("carteira preserva gráficos principais e preenche os cards com estado atual em Light, Dark e mobile", async ({ page }, testInfo) => {
  await login(page);
  const evolution = page.getByRole("heading", { name: "Evolução patrimonial" });
  const composition = page.getByRole("heading", { name: "Composição por ação" });
  const positions = page.getByRole("heading", { name: "Minhas posições" });
  await expect(evolution).toBeVisible();
  await expect(composition).toBeVisible();
  await expect(positions).toBeVisible();
  const metrics = page.locator(".summary-metrics");
  await expect(metrics.locator(".current-metric")).toHaveCount(5);
  await expect(metrics.locator(".current-composition")).toBeVisible();
  await expect(metrics.getByText("Caixa", { exact: true })).toBeVisible();
  await expect(metrics.getByText("Posições", { exact: true })).toBeVisible();
  await expect(metrics.getByText("Investido", { exact: true })).toBeVisible();
  await expect(metrics.getByText("Atual", { exact: true })).toBeVisible();
  await expect(metrics.getByText(/do patrimônio em caixa/)).toBeVisible();
  await expect(metrics.getByText(/do patrimônio em ativos/)).toBeVisible();
  await expect(metrics.locator(".metric-sparkline")).toHaveCount(0);
  await expect(metrics.locator(".recharts-responsive-container")).toHaveCount(0);
  await expect(metrics.locator(".current-result").getByText("Resultado positivo")).toBeVisible();
  await expect(page.getByText("Resultado acumulado com vendas")).toBeVisible();
  expect(await evolution.evaluate((node) => node.getBoundingClientRect().top)).toBeLessThan(await positions.evaluate((node) => node.getBoundingClientRect().top));
  await page.screenshot({ path: testInfo.outputPath("carteira-current-light.png"), fullPage: true });
  await page.getByRole("button", { name: "Usar tema escuro" }).first().click();
  await expect(page.locator("html")).toHaveClass(/dark/);
  expect(await page.locator(".portfolio-chart").first().evaluate((node) => getComputedStyle(node).backgroundColor)).not.toBe("rgb(255, 255, 255)");
  await page.screenshot({ path: testInfo.outputPath("carteira-current-dark.png"), fullPage: true });
  await page.setViewportSize({ width: 390, height: 844 });
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390);
  await expect(evolution).toBeVisible();
  await expect(composition).toBeVisible();
  await page.screenshot({ path: testInfo.outputPath("carteira-current-mobile.png"), fullPage: true });
});
