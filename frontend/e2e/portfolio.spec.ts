import { expect, test, type Page } from "@playwright/test";

async function login(page: Page, email = "user@example.test", returnTo = "/carteira") {
  await page.goto("/login?returnTo=" + encodeURIComponent(returnTo));
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill("Password1!");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL(returnTo);
}

test("login → carteira renderiza shell e resumo sem expor sessão", async ({ page }) => {
  await login(page);
  await expect(page.getByRole("heading", { level: 1, name: "Minha carteira" })).toBeVisible();
  await expect(page.getByText("Patrimônio total")).toBeVisible();
  await expect(page.getByText(/R\$ 125\.001\.(?:200|500),22345679/)).toBeVisible();
  const html = await page.content();
  expect(html).not.toMatch(/user-token|accessToken|Authorization|BACKEND_API_URL/);
});

test("Light → Dark persiste após reload", async ({ page }) => {
  await login(page);
  await page.getByRole("button", { name: "Usar tema claro" }).first().click();
  await expect(page.locator("html")).toHaveClass(/light/);
  await page.getByRole("button", { name: "Usar tema escuro" }).first().click();
  await expect(page.locator("html")).toHaveClass(/dark/);
  await page.reload();
  await expect(page.locator("html")).toHaveClass(/dark/);
  await expect(page.getByText("Patrimônio total")).toBeVisible();
});

test("preview e navegação abrem posições, transações e movimentações", async ({ page }) => {
  await login(page);
  await page.getByRole("link", { name: /Ver todas/ }).click();
  await expect(page).toHaveURL("/carteira/posicoes");
  await expect(page.getByRole("heading", { name: "Suas posições" })).toBeVisible();
  await expect(page.locator(".desktop-position-table .ticker", { hasText: "ACME3" })).toBeVisible();
  await page.locator('.desktop-sidebar a[href="/carteira/transacoes"]').click();
  await expect(page.getByRole("heading", { name: "Transações confirmadas" })).toBeVisible();
  await expect(page.getByText("SELL · Venda")).toBeVisible();
  await page.locator('.desktop-sidebar a[href="/carteira/movimentacoes"]').click();
  await expect(page.getByRole("heading", { name: "Caixa e movimentações" })).toBeVisible();
  await expect(page.getByText("Aporte mensal")).toBeVisible();
});

test("refresh mantém a tela e atualiza somente o resumo", async ({ page }) => {
  await login(page);
  const button = page.getByRole("button", { name: "Atualizar mercado" });
  await button.click();
  await expect(page.getByRole("button", { name: "Atualizando…" })).toBeVisible();
  await expect(page.getByText("R$ 125.001.500,22345679")).toBeVisible();
  await expect(page.getByText("Dados de mercado atualizados.")).toBeAttached();
  await expect(page.locator(".position-preview strong", { hasText: "ACME3" })).toBeVisible();
});

test("login e admin continuam legíveis em Light e Dark", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Entrar" })).toBeVisible();
  await login(page, "admin@example.test", "/admin");
  await expect(page.getByRole("heading", { name: "Administração" })).toBeVisible();
  await page.evaluate(() => { localStorage.setItem("carteira-theme", "dark"); document.documentElement.classList.add("dark"); });
  await page.reload();
  await expect(page.locator("html")).toHaveClass(/dark/);
  await expect(page.getByText("Área administrativa autenticada.")).toBeVisible();
});
