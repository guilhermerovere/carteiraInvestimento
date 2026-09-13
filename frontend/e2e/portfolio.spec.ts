import { expect, test, type Page } from "@playwright/test";

async function login(page: Page, email = "user@example.test", returnTo?: string) {
  await page.goto(returnTo ? "/login?returnTo=" + encodeURIComponent(returnTo) : "/login");
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill("Password1!");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL(returnTo ?? (email.includes("admin") ? "/admin" : "/carteira"));
}

test("login → carteira renderiza shell e resumo sem expor sessão", async ({ page }) => {
  await login(page);
  await expect(page.getByRole("heading", { level: 1, name: "Minha carteira" })).toBeVisible();
  await expect(page.getByText("Patrimônio total")).toBeVisible();
  await expect(page.getByText(/R\$ 125\.001\.(?:200|500),22345679/)).toBeVisible();
  const html = await page.content();
  expect(html).not.toMatch(/user-token|accessToken|Authorization|BACKEND_API_URL/);
});

test("login sem returnTo envia USER à carteira e ADMIN ao admin", async ({ page }) => {
  await login(page);
  await expect(page).toHaveURL("/carteira");
  await page.context().clearCookies();
  await login(page, "admin@example.test");
  await expect(page).toHaveURL("/admin");
});

test("cadastro mostra Nome Completo e mantém o contrato existente", async ({ page }) => {
  await page.goto("/register");
  await expect(page.getByText("Valore").first()).toBeVisible();
  await expect(page.getByLabel("Nome Completo")).toBeVisible();
  await page.getByLabel("Nome Completo").fill("Pessoa Valore");
});

test("sidebar Valore recolhe e expande com controle e tooltip acessíveis", async ({ page }) => {
  await login(page);
  await expect(page.locator(".desktop-sidebar").getByText("Valore")).toBeVisible();
  await page.getByRole("button", { name: "Recolher menu" }).click();
  const expand = page.getByRole("button", { name: "Expandir menu" });
  await expect(expand).toBeVisible();
  await expand.hover();
  await expect(page.getByRole("tooltip", { name: "Expandir menu" })).toBeVisible();
  await expand.click();
  await expect(page.getByRole("button", { name: "Recolher menu" })).toBeVisible();
});

test("roles incompatíveis recebem acesso negado sem perder a sessão", async ({ page }) => {
  await login(page, "admin@example.test");
  await page.goto("/carteira");
  await expect(page.getByRole("heading", { name: "Acesso negado" })).toBeVisible();
  await page.goto("/admin");
  await expect(page.getByRole("heading", { name: "Administração" })).toBeVisible();
  await page.context().clearCookies();
  await login(page);
  await page.goto("/admin");
  await expect(page.getByRole("heading", { name: "Acesso negado" })).toBeVisible();
  await page.goto("/carteira");
  await expect(page.getByRole("heading", { name: "Minha carteira" })).toBeVisible();
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
  await expect(page.getByRole("heading", { name: "Bem-vindo de volta" })).toBeVisible();
  await login(page, "admin@example.test");
  await expect(page.getByRole("heading", { name: "Administração" })).toBeVisible();
  await page.evaluate(() => { localStorage.setItem("carteira-theme", "dark"); document.documentElement.classList.add("dark"); });
  await page.reload();
  await expect(page.locator("html")).toHaveClass(/dark/);
  await expect(page.getByText("Área administrativa autenticada.")).toBeVisible();
});
