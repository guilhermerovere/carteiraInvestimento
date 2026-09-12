import { expect, test, type Page } from "@playwright/test";

async function login(page: Page) {
  await page.goto("/login?returnTo=%2Fcarteira");
  await page.getByLabel("E-mail").fill("user@example.test");
  await page.getByLabel("Senha").fill("Password1!");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL("/carteira");
  await expect(page.getByText("Patrimônio total")).toBeVisible();
}

async function theme(page: Page, value: "light" | "dark") {
  await page.evaluate((selected) => localStorage.setItem("carteira-theme", selected), value);
  await page.reload();
  await expect(page.locator("html")).toHaveClass(new RegExp(value));
}

test("QA visual explícito Light/Dark em desktop, tablet, mobile e estados", async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  await login(page);
  await theme(page, "light");
  await page.screenshot({ path: "test-results/visual-qa/light-desktop-carteira.png", fullPage: true });

  await page.setViewportSize({ width: 900, height: 900 });
  await page.goto("/carteira/posicoes");
  await expect(page.getByRole("heading", { name: "Suas posições" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/light-tablet-posicoes.png", fullPage: true });

  await page.setViewportSize({ width: 390, height: 844 });
  await page.screenshot({ path: "test-results/visual-qa/light-mobile-posicoes.png", fullPage: true });

  await page.route("**/api/finance/transactions?*", (route) => route.fulfill({
    status: 200,
    contentType: "application/json",
    headers: { "Cache-Control": "no-store" },
    body: JSON.stringify({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
  }));
  await page.goto("/carteira/transacoes");
  await expect(page.getByRole("heading", { name: "Sem transações" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/light-mobile-empty.png", fullPage: true });
  await page.unroute("**/api/finance/transactions?*");

  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto("/carteira/transacoes");
  await theme(page, "dark");
  await expect(page.getByText("SELL · Venda")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-desktop-transacoes.png", fullPage: true });

  await page.setViewportSize({ width: 900, height: 900 });
  await page.goto("/carteira/movimentacoes");
  await expect(page.getByText("Aporte mensal")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-tablet-movimentacoes.png", fullPage: true });

  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/carteira");
  await expect(page.getByText("Patrimônio total")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-mobile-carteira.png", fullPage: true });

  await page.setViewportSize({ width: 1440, height: 1000 });
  const refresh = page.getByRole("button", { name: "Atualizar mercado" });
  await refresh.click();
  await expect(page.getByRole("button", { name: "Atualizando…" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-desktop-refresh.png", fullPage: true });
  await expect(page.getByText("Dados de mercado atualizados.")).toBeAttached();

  await page.route("**/api/finance/portfolio/summary", async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 1500));
    await route.fulfill({ status: 502, contentType: "application/problem+json", body: JSON.stringify({ status: 502, title: "Indisponível" }) });
  });
  await page.setViewportSize({ width: 900, height: 900 });
  await page.reload();
  await expect(page.getByRole("status", { name: "Carregando resumo da carteira" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-tablet-loading.png", fullPage: true });
  await expect(page.getByText("Não foi possível atualizar os dados de mercado agora.")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-tablet-error.png", fullPage: true });
});
