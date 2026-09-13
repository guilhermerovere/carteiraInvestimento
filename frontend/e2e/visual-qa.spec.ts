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

async function publicTheme(page: Page, path: "/login" | "/register", value: "light" | "dark") {
  await page.goto(path);
  await page.evaluate((selected) => localStorage.setItem("carteira-theme", selected), value);
  await page.reload();
  await expect(page.locator("html")).toHaveClass(new RegExp(value));
}

async function waitForSidebarWidth(page: Page, expected: number) {
  await expect.poll(async () => page.locator(".app-shell").evaluate((shell) => Number.parseFloat(getComputedStyle(shell).gridTemplateColumns))).toBe(expected);
}

test("QA visual Valore login Light/Dark em desktop, desktop menor, tablet e mobile", async ({ page }) => {
  const viewports = [
    { name: "desktop-1440", width: 1440, height: 1000 },
    { name: "desktop-1100", width: 1100, height: 820 },
    { name: "tablet-768", width: 768, height: 920 },
    { name: "mobile-390", width: 390, height: 844 },
    { name: "mobile-360", width: 360, height: 740 },
  ] as const;

  for (const selected of ["light", "dark"] as const) {
    for (const viewport of viewports) {
      await page.setViewportSize(viewport);
      await publicTheme(page, "/login", selected);
      await expect(page.getByRole("heading", { name: "Bem-vindo de volta" })).toBeVisible();
      await page.screenshot({ path: `test-results/visual-qa/${selected}-login-${viewport.name}.png`, fullPage: true });
    }
  }
});

test("QA visual Valore register Light/Dark em desktop e mobile", async ({ page }) => {
  for (const selected of ["light", "dark"] as const) {
    for (const viewport of [{ name: "desktop", width: 1440, height: 1000 }, { name: "mobile", width: 390, height: 844 }] as const) {
      await page.setViewportSize(viewport);
      await publicTheme(page, "/register", selected);
      await expect(page.getByLabel("Nome Completo")).toBeVisible();
      await page.screenshot({ path: `test-results/visual-qa/${selected}-register-${viewport.name}.png`, fullPage: true });
    }
  }
});

test("QA visual sidebar Valore expandida e recolhida em Light/Dark", async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  await login(page);
  for (const selected of ["light", "dark"] as const) {
    await theme(page, selected);
    await expect(page.getByRole("button", { name: "Recolher menu" })).toBeVisible();
    await waitForSidebarWidth(page, 264);
    await page.screenshot({ path: `test-results/visual-qa/${selected}-sidebar-expandida-1440.png` });
    await page.getByRole("button", { name: "Recolher menu" }).click();
    await expect(page.getByRole("button", { name: "Expandir menu" })).toBeVisible();
    await waitForSidebarWidth(page, 84);
    await page.screenshot({ path: `test-results/visual-qa/${selected}-sidebar-recolhida-1440.png` });
    await page.getByRole("button", { name: "Expandir menu" }).click();
    await waitForSidebarWidth(page, 264);
  }

  await page.setViewportSize({ width: 900, height: 900 });
  await page.evaluate(() => window.scrollTo(0, 0));
  await expect(page.getByRole("button", { name: "Recolher menu" })).toBeVisible();
  await waitForSidebarWidth(page, 224);
  const expandedMetrics = await page.locator(".app-shell").evaluate((shell) => ({
    className: shell.className,
    sidebarWidth: getComputedStyle(shell).getPropertyValue("--sidebar-width").trim(),
    columns: getComputedStyle(shell).gridTemplateColumns,
    viewportWidth: document.documentElement.clientWidth,
    contentWidth: document.documentElement.scrollWidth,
  }));
  expect(expandedMetrics).toMatchObject({ className: "app-shell", sidebarWidth: "14rem", columns: "224px 676px", viewportWidth: 900, contentWidth: 900 });
  await page.screenshot({ path: "test-results/visual-qa/dark-sidebar-expandida-900.png" });
  await page.getByRole("button", { name: "Recolher menu" }).click();
  await waitForSidebarWidth(page, 80);
  const collapsedMetrics = await page.locator(".app-shell").evaluate((shell) => ({
    className: shell.className,
    sidebarWidth: getComputedStyle(shell).getPropertyValue("--sidebar-width").trim(),
    columns: getComputedStyle(shell).gridTemplateColumns,
    viewportWidth: document.documentElement.clientWidth,
    contentWidth: document.documentElement.scrollWidth,
  }));
  expect(collapsedMetrics).toMatchObject({ className: "app-shell app-shell--collapsed", sidebarWidth: "5rem", columns: "80px 820px", viewportWidth: 900, contentWidth: 900 });
  await expect(page.getByLabel("Abrir perfil de Pessoa Investidora")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-sidebar-recolhida-900.png" });

  await page.getByRole("button", { name: "Expandir menu" }).click();
  await page.setViewportSize({ width: 721, height: 900 });
  await waitForSidebarWidth(page, 192);
  await page.evaluate(() => window.scrollTo(0, 0));
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBe(0);
  await expect(page.getByRole("button", { name: "Recolher menu" })).toBeVisible();
  expect(await page.evaluate(() => ({ viewportWidth: document.documentElement.clientWidth, contentWidth: document.documentElement.scrollWidth }))).toEqual({ viewportWidth: 721, contentWidth: 721 });
  await page.screenshot({ path: "test-results/visual-qa/dark-sidebar-expandida-721.png" });
  await page.getByRole("button", { name: "Recolher menu" }).click();
  await waitForSidebarWidth(page, 80);
  await expect(page.getByRole("button", { name: "Expandir menu" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-sidebar-recolhida-721.png" });
});

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
