import { expect, test, type Page } from "@playwright/test";

async function login(page: Page, email: string) {
  await page.goto("/login"); await page.getByLabel("E-mail").fill(email); await page.getByLabel("Senha").fill("Password1!"); await page.getByRole("button", { name: "Entrar" }).click(); await expect(page).toHaveURL(email.includes("admin") ? "/admin" : "/carteira");
}

test("ADMIN usa shell próprio e administra corretoras e ativos", async ({ page }) => {
  await login(page, "admin@example.test"); await expect(page.getByRole("heading", { name: "Administração Valore" })).toBeVisible(); await expect(page.getByText("Operar", { exact: true })).toHaveCount(0);
  await page.getByRole("link", { name: "Corretoras" }).first().click(); await expect(page.getByText("XP Investimentos", { exact: true }).first()).toBeVisible(); await expect(page.getByRole("textbox", { name: /logo/i })).toHaveCount(0);
  await page.getByRole("button", { name: "Nova corretora" }).click(); await page.getByLabel("CNPJ").fill("02.332.886/0016-82"); await page.getByRole("button", { name: "Cadastrar corretora" }).click(); await expect(page.getByText("Corretora cadastrada com sucesso.")).toBeVisible();
  await page.getByRole("link", { name: "Ativos" }).first().click(); await expect(page.getByText("ACME3", { exact: true }).first()).toBeVisible(); await page.getByRole("button", { name: "Adicionar ativo" }).click(); await page.getByLabel("Ticker").fill("XPTO3"); await page.getByRole("button", { name: "Cadastrar ativo" }).click(); await expect(page.getByText("Ativo cadastrado com sucesso.")).toBeVisible();
});

test("ROLE_USER acessa Configurações pelo perfil e vê o nome atualizado imediatamente", async ({ page }) => {
  await login(page, "user@example.test"); await page.getByLabel(/Abrir perfil de/).click(); await page.getByRole("link", { name: /Configurações/ }).click(); await expect(page).toHaveURL("/configuracoes");
  const name = page.getByLabel(/^Nome/); await name.fill("Pessoa Atualizada"); await page.getByRole("button", { name: "Salvar nome" }).click(); await expect(page.getByText("Nome atualizado com sucesso.")).toBeVisible();
  await page.getByRole("link", { name: /Voltar ao início Valore/ }).click(); await page.getByLabel("Abrir perfil de Pessoa Atualizada").click(); await expect(page.getByText("Pessoa Atualizada", { exact: true }).first()).toBeVisible();
});

test("Zona de perigo bloqueia encerramento com saldo e posições", async ({ page }) => {
  await login(page, "user@example.test"); await page.goto("/configuracoes"); await expect(page.getByText(/primeiro deixe o saldo em caixa/)).toBeVisible(); await expect(page.getByText(/Venda suas posições/)).toBeVisible(); await expect(page.getByRole("button", { name: "Excluir conta" })).toBeDisabled();
});

test("mobile preserva bottom navigation e integra Operar pelo Mais em Sheet segura", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 }); await login(page, "user@example.test"); await expect(page.locator(".desktop-sidebar")).toBeHidden(); await expect(page.locator(".shell-header")).toBeHidden();
  await page.locator(".mobile-bottom-nav summary", { hasText: "Mais" }).click(); const more = page.locator(".more-navigation__popover"); await more.locator('summary[aria-label="Operar"]').click(); await more.getByRole("menuitem", { name: "Depositar" }).click();
  const dialog = page.getByRole("dialog"); await expect(dialog).toBeVisible(); const box = await dialog.boundingBox(); expect(box!.width).toBe(390); expect(box!.y + box!.height).toBeLessThanOrEqual(844); await expect(dialog.getByRole("button", { name: "Cancelar" })).toBeVisible(); await expect(dialog.getByRole("button", { name: "Revisar" })).toBeVisible();
});
