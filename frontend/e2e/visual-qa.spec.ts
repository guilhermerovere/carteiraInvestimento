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
    await page.locator(".desktop-sidebar summary[aria-label='Operar']").click();
    const operationMenu = page.getByRole("menu", { name: "Operações financeiras" });
    await expect(operationMenu).toBeVisible();
    const menuMetrics = await operationMenu.evaluate((menu) => {
      const bounds = menu.getBoundingClientRect();
      return { left: bounds.left, right: bounds.right, viewport: document.documentElement.clientWidth };
    });
    expect(menuMetrics.left).toBeGreaterThan(84);
    expect(menuMetrics.right).toBeLessThanOrEqual(menuMetrics.viewport);
    await page.screenshot({ path: `test-results/visual-qa/${selected}-sidebar-recolhida-operar-1440.png` });
    await page.keyboard.press("Escape");
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
  await expect(page.locator(".shell-header").getByLabel(/Abrir perfil de/)).toBeVisible();
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
  await expect(page.getByRole("heading", { name: "Minhas posições" }).first()).toBeVisible();
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
  await expect(page.getByRole("heading", { name: "Nenhuma transação ainda" })).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/light-mobile-empty.png", fullPage: true });
  await page.unroute("**/api/finance/transactions?*");

  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto("/carteira/transacoes");
  await theme(page, "dark");
  await expect(page.getByText("Venda").first()).toBeVisible();
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
  const refresh = page.getByRole("button", { name: "Atualizar" });
  await refresh.click();
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
  await expect(page.getByText("Não foi possível atualizar as cotações agora.")).toBeVisible();
  await page.screenshot({ path: "test-results/visual-qa/dark-tablet-error.png", fullPage: true });
});

test("QA visual das operações em Light/Dark no desktop, tablet e mobile", async ({ page }) => {
  test.setTimeout(90_000);
  await page.setViewportSize({ width: 1440, height: 1000 });
  await login(page);
  const viewports = [
    { name: "desktop-1440", width: 1440, height: 1000, action: "Comprar", mobile: false },
    { name: "desktop-1100", width: 1100, height: 900, action: "Depositar", mobile: false },
    { name: "tablet-768", width: 768, height: 920, action: "Sacar", mobile: false },
    { name: "mobile-390", width: 390, height: 844, action: "Comprar", mobile: true },
    { name: "mobile-360", width: 360, height: 740, action: "Depositar", mobile: true },
  ] as const;
  for (const selected of ["light", "dark"] as const) {
    await theme(page, selected);
    for (const viewport of viewports) {
      await page.setViewportSize(viewport);
      const launcher = viewport.mobile ? page.locator(".more-navigation__popover .operation-launcher") : page.locator(".desktop-sidebar .operation-launcher");
      if (viewport.mobile) {
        const more = page.locator(".mobile-bottom-nav .more-navigation");
        if (await more.getAttribute("open") === null) await more.locator(":scope > summary").click();
      }
      if (await launcher.locator("details").getAttribute("open") === null) {
        await launcher.getByText("Operar", { exact: true }).click();
      }
      await launcher.getByRole("menuitem", { name: viewport.action }).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      if (viewport.action === "Comprar") {
        await dialog.getByRole("option", { name: /ACME3/ }).click();
        await dialog.getByRole("button", { name: "Atualizar cotação" }).click();
        await dialog.getByRole("option", { name: /XP Investimentos/ }).click();
        await dialog.getByLabel("Quantidade").fill("1");
        await expect(dialog.getByText("Preço médio estimado", { exact: false })).toHaveCount(0);
      } else {
        await dialog.getByRole("textbox", { name: "Valor" }).fill("123456789,12");
        await expect(dialog.getByText("Estimativa.", { exact: false })).toBeVisible();
      }
      const metrics = await dialog.evaluate((node) => ({
        width: node.getBoundingClientRect().width,
        viewport: document.documentElement.clientWidth,
        bodyScroll: node.querySelector(".operation-dialog__body")!.scrollHeight >= node.querySelector(".operation-dialog__body")!.clientHeight,
      }));
      expect(metrics.width).toBeLessThanOrEqual(metrics.viewport);
      expect(metrics.bodyScroll).toBe(true);
      await page.screenshot({ path: `test-results/visual-qa/${selected}-operation-${viewport.name}.png` });
      await dialog.getByRole("button", { name: "Cancelar" }).click();
    }
  }
});

test("QA visual Admin e Configurações em Light/Dark no desktop, tablet e mobile", async ({ page }) => {
  test.setTimeout(120_000);
  const viewports = [
    { name: "desktop-1440", width: 1440, height: 1000 },
    { name: "desktop-1100", width: 1100, height: 900 },
    { name: "tablet-768", width: 768, height: 920 },
    { name: "mobile-390", width: 390, height: 844 },
    { name: "mobile-360", width: 360, height: 740 },
  ] as const;
  await page.goto("/login"); await page.getByLabel("E-mail").fill("admin@example.test"); await page.getByLabel("Senha").fill("Password1!"); await page.getByRole("button", { name: "Entrar" }).click(); await expect(page).toHaveURL("/admin");
  for (const selected of ["light", "dark"] as const) {
    await theme(page, selected);
    for (const viewport of viewports) {
      await page.setViewportSize(viewport);
      for (const route of [{ path: "/admin", name: "overview" }, { path: "/admin/corretoras", name: "brokers" }, { path: "/admin/ativos", name: "assets" }]) {
        await page.goto(route.path); await expect(page.locator(".admin-page")).toBeVisible(); expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(viewport.width); await page.screenshot({ path: `test-results/visual-qa/${selected}-admin-${route.name}-${viewport.name}.png`, fullPage: true });
      }
    }
  }
  await page.context().clearCookies(); await page.setViewportSize({ width: 1440, height: 1000 }); await login(page);
  for (const selected of ["light", "dark"] as const) {
    await theme(page, selected);
    for (const viewport of viewports) {
      await page.setViewportSize(viewport); await page.goto("/configuracoes"); await expect(page.getByRole("heading", { name: "Configurações" })).toBeVisible(); expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(viewport.width); await page.screenshot({ path: `test-results/visual-qa/${selected}-settings-${viewport.name}.png`, fullPage: true });
    }
  }
});
