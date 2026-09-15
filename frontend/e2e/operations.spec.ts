import { expect, test, type Page } from "@playwright/test";

async function login(page: Page) {
  await page.goto("/login"); await page.getByLabel("E-mail").fill("user@example.test"); await page.getByLabel("Senha").fill("Password1!"); await page.getByRole("button", { name: "Entrar" }).click(); await expect(page).toHaveURL("/carteira");
}
async function launch(page: Page, name: "Comprar" | "Depositar" | "Sacar") {
  const details = page.locator(".desktop-sidebar details").filter({ has: page.locator('summary[aria-label="Operar"]') });
  if (await details.getAttribute("open") === null) await details.locator("summary").click();
  await details.getByRole("menuitem", { name }).click();
}
async function finish(page: Page, success: RegExp) {
  const dialog = page.getByRole("dialog"); await dialog.getByRole("button", { name: "Revisar" }).click(); await dialog.getByRole("button", { name: "Confirmar operação" }).click(); await expect(dialog.getByText(success)).toBeVisible();
  const content = await dialog.textContent(); expect(content).not.toMatch(/[{}]|movimentacao|saldoResultante|ativoId|transaction-technical/); await dialog.getByRole("button", { name: "Concluir" }).click();
}

test.beforeEach(async ({ page }) => login(page));

test("Operar fica no rodapé da sidebar, sai do header e abre Dialog centralizado", async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  const sidebar = page.locator(".desktop-sidebar"); const operate = sidebar.locator('summary[aria-label="Operar"]'); const collapse = sidebar.getByRole("button", { name: "Recolher menu" });
  await expect(operate).toBeVisible(); const operateBox = await operate.boundingBox(); const collapseBox = await collapse.boundingBox(); expect(operateBox!.y).toBeLessThan(collapseBox!.y);
  await expect(page.locator(".shell-header").getByText("Operar", { exact: true })).toHaveCount(0);
  await launch(page, "Comprar"); const dialog = page.getByRole("dialog"); await expect(dialog).toBeVisible();
  const box = await dialog.boundingBox(); expect(box).not.toBeNull(); expect(box!.width).toBeGreaterThanOrEqual(520); expect(box!.width).toBeLessThanOrEqual(640); expect(Math.abs(box!.x - (1440 - box!.width) / 2)).toBeLessThan(4);
});

test("sidebar recolhida mantém Operar acessível com tooltip e menu funcional", async ({ page }) => {
  await page.getByRole("button", { name: "Recolher menu" }).click(); const sidebar = page.locator(".desktop-sidebar");
  const operate = sidebar.locator('summary[aria-label="Operar"]'); await operate.hover(); await expect(sidebar.getByRole("tooltip", { name: "Operar" })).toBeVisible(); await operate.click(); await sidebar.getByRole("menuitem", { name: "Depositar" }).click(); await expect(page.getByRole("dialog").getByRole("heading", { name: "Operação financeira" })).toBeVisible();
});

test("depósito e saque têm revisão e resultado amigável sem JSON", async ({ page }) => {
  for (const entry of [{ action: "Depositar", success: /Depósito realizado com sucesso/ }, { action: "Sacar", success: /Saque realizado com sucesso/ }] as const) {
    await launch(page, entry.action); await page.getByRole("dialog").getByRole("textbox", { name: "Valor" }).fill("10,00"); await finish(page, entry.success);
  }
});

test("BUY e SELL apresentam resultados tipados", async ({ page }) => {
  await launch(page, "Comprar"); let dialog = page.getByRole("dialog"); await dialog.getByRole("option", { name: /ACME3/ }).click(); await dialog.getByRole("button", { name: "Atualizar cotação" }).click(); await dialog.getByRole("option", { name: /XP Investimentos/ }).click(); await dialog.getByLabel("Quantidade").fill("1"); await finish(page, /Compra registrada com sucesso/);
  await page.goto("/carteira/posicoes"); await page.locator(".reference-table-card").getByRole("button", { name: "Vender" }).click(); dialog = page.getByRole("dialog"); await dialog.getByRole("option", { name: /XP Investimentos/ }).click(); await dialog.getByLabel("Quantidade").fill("1"); await dialog.getByLabel(/Preço unitário/).fill("10,00"); await finish(page, /Venda registrada com sucesso/);
});

test("BUY B3 usa somente quantidade, preço editado e taxas no formulário, revisão e payload", async ({ page }) => {
  let payload: Record<string, unknown> | undefined;
  await page.route("**/api/finance/transactions", async route => {
    if (route.request().method() !== "POST") return route.continue();
    payload = route.request().postDataJSON() as Record<string, unknown>;
    await route.continue();
  });
  await launch(page, "Comprar");
  const dialog = page.getByRole("dialog");
  await dialog.getByRole("option", { name: /ACME3/ }).click();
  await dialog.getByRole("option", { name: /XP Investimentos/ }).click();
  await dialog.getByLabel("Quantidade").fill("1");
  await dialog.getByLabel("Preço unitário").fill("1,00");
  await dialog.getByLabel("Taxas").fill("0,00");
  await expect(dialog.getByText("Total a pagar").locator("..")).toContainText("R$ 1,00");
  await dialog.getByRole("button", { name: "Revisar" }).click();
  await expect(dialog.getByText("Total a pagar").locator("..")).toContainText("R$ 1,00");
  await dialog.getByRole("button", { name: "Confirmar operação" }).click();
  await expect(dialog.getByText("Compra registrada com sucesso")).toBeVisible();
  expect(payload).toMatchObject({ tipo: "BUY", quantidade: "1", precoUnitario: "1.00", taxas: "0.00", exchangeRateId: null });
});

test("BUY US conserva a conversão e envia o exchangeRateId persistido", async ({ page }) => {
  let payload: Record<string, unknown> | undefined;
  await page.route("**/api/finance/transactions", async route => {
    if (route.request().method() !== "POST") return route.continue();
    payload = route.request().postDataJSON() as Record<string, unknown>;
    await route.continue();
  });
  await launch(page, "Comprar");
  const dialog = page.getByRole("dialog");
  await dialog.getByRole("option", { name: /AAPL/ }).click();
  await expect(dialog.getByText("USD/BRL")).toBeVisible();
  await dialog.getByRole("option", { name: /XP Investimentos/ }).click();
  await dialog.getByLabel("Quantidade").fill("1");
  await dialog.getByLabel("Preço unitário").fill("1,00");
  await dialog.getByLabel("Taxas").fill("0,00");
  await expect(dialog.getByText("Total a pagar").locator("..")).toContainText("R$ 5,12");
  await finish(page, /Compra registrada com sucesso/);
  expect(payload?.exchangeRateId).toBe("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaab");
});

test("provider indisponível preserva ticker, usa pt-BR e não oferece cadastro", async ({ page }) => {
  await page.route("**/api/finance/catalog/assets/ticker/ACME3", route => route.fulfill({ status: 502, contentType: "application/problem+json", body: JSON.stringify({ status: 502, title: "The financial asset validation service is temporarily unavailable." }) }));
  await launch(page, "Comprar"); const dialog = page.getByRole("dialog"); const input = dialog.getByPlaceholder("Ticker ou nome"); await input.fill("ACME3"); await dialog.getByRole("button", { name: "Verificar ticker" }).click();
  await expect(dialog.getByText("Não foi possível validar este ativo agora.")).toBeVisible(); await expect(input).toHaveValue("ACME3"); await expect(dialog.getByText(/Cadastrar ticker/)).toHaveCount(0); await expect(dialog.getByRole("button", { name: "Tentar novamente" })).toBeVisible(); await expect(dialog.getByText(/The financial/)).toHaveCount(0);
});

test("recovery reenvia a mesma chave e não permite edição", async ({ page }) => {
  const key = "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"; const payload = { valor: "0.10000001", descricao: "Recuperação" };
  await page.evaluate(({ key, payload }) => sessionStorage.setItem("valore.pending-operation.v1", JSON.stringify({ version: 1, kind: "DEPOSIT", key, payload, createdAt: "2026-09-13T12:00:00.000Z", ambiguous: true })), { key, payload });
  let requests = 0; await page.route("**/api/finance/cash/deposit", async route => { requests++; expect(route.request().headers()["idempotency-key"]).toBe(key); expect(route.request().postDataJSON()).toEqual(payload); await route.fulfill({ status: 201, contentType: "application/json", body: JSON.stringify({ movimentacao: { id: "77777777-7777-4777-8777-777777777777", tipo: "DEPOSITO", valorBrl: "0.10", descricao: "Recuperação", dataHora: "2026-09-13T12:01:00Z" }, saldoResultante: "1200.20" }) }); });
  await page.reload(); await page.getByText("Revisar operação").click(); const dialog = page.getByRole("dialog"); await expect(dialog.getByText("Resultado pendente")).toBeVisible(); await expect(dialog.getByRole("button", { name: "Editar" })).toHaveCount(0); await dialog.getByRole("button", { name: "Tentar novamente" }).click(); await expect(dialog.getByText("Depósito realizado com sucesso")).toBeVisible(); expect(requests).toBe(1); await expect.poll(() => page.evaluate(() => sessionStorage.getItem("valore.pending-operation.v1"))).toBeNull();
});

test("BUY invalida e recarrega o historico persistido mesmo com cotacao indisponivel", async ({ page }) => {
  const historyStatuses: number[] = [];
  page.on("response", response => {
    if (response.request().method() === "GET" && response.url().includes("/api/finance/transactions?")) historyStatuses.push(response.status());
  });
  const initialHistory = page.waitForResponse(response => response.request().method() === "GET" && response.url().includes("/api/finance/transactions?"));
  await page.goto("/carteira/transacoes");
  const initialResponse = await initialHistory;
  expect(initialResponse.status(), await initialResponse.text()).toBe(200);
  await expect(page.getByRole("table")).toBeVisible();
  await page.route("**/api/finance/market/quotes/**", route => route.fulfill({ status: 502, contentType: "application/problem+json", body: JSON.stringify({ status: 502, title: "Provider indisponível" }) }));

  await launch(page, "Comprar");
  const dialog = page.getByRole("dialog");
  await dialog.getByRole("option", { name: /ACME3/ }).click();
  await dialog.getByRole("option", { name: /XP Investimentos/ }).click();
  await dialog.getByLabel("Quantidade").fill("5");
  await dialog.getByLabel("Preço unitário").fill("42,73");
  await expect(dialog.getByText(/Pre\u00e7o m\u00e9dio estimado/)).toHaveCount(0);
  await finish(page, /Compra registrada com sucesso/);

  const refetch = page.waitForResponse(response => response.request().method() === "GET" && response.url().includes("/api/finance/transactions?"));
  await page.goto("/carteira/transacoes");
  const response = await refetch;
  expect(response.status()).toBe(200);
  const ledger = await response.json();
  expect(ledger.items[0]).toMatchObject({ ticker: "ACME3", tipo: "BUY", quantidade: "5", moeda: "BRL", precoUnitario: "42.73", exchangeRateId: null, resultadoRealizadoBrl: null });
  const row = page.locator("tbody tr").first();
  await expect(row).toContainText("Compra");
  await expect(row).toContainText("5");
  await expect(row).toContainText("R$ 42,73");
  await expect(page.locator(".transactions-page [role=alert]")).toHaveCount(0);
  expect(historyStatuses).not.toContain(404);
  expect(historyStatuses).not.toContain(502);
});
