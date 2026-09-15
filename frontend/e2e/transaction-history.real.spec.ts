import { randomUUID } from "node:crypto";
import { expect, test, type Page } from "@playwright/test";

const password = "Password1!";

async function registerAndLogin(page: Page) {
  const email = `transaction-qa-${randomUUID()}@example.test`;
  await page.goto("/register");
  await page.getByLabel("Nome Completo").fill("QA Ledger");
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill(password);
  await page.getByRole("button", { name: "Cadastrar" }).click();
  await expect(page).toHaveURL(/\/login$/);
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill(password);
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL(/\/carteira$/);
}

async function launch(page: Page, operation: "Comprar" | "Depositar") {
  const launcher = page.locator(".desktop-sidebar details").filter({ has: page.locator('summary[aria-label="Operar"]') });
  if (await launcher.getAttribute("open") === null) await launcher.locator("summary").click();
  await launcher.getByRole("menuitem", { name: operation }).click();
}

async function completeDialog(page: Page, success: RegExp) {
  const dialog = page.getByRole("dialog");
  await dialog.getByRole("button", { name: "Revisar" }).click();
  await dialog.getByRole("button", { name: "Confirmar operação" }).click();
  await expect(dialog.getByText(success)).toBeVisible();
  await dialog.getByRole("button", { name: "Concluir" }).click();
}

test("backend persistido: BUY seguido de historico 200 apesar de falha de mercado", async ({ page }) => {
  await registerAndLogin(page);

  const historyResponses: number[] = [];
  page.on("response", response => {
    if (response.request().method() === "GET" && response.url().includes("/api/finance/transactions?")) historyResponses.push(response.status());
  });
  const initialHistory = page.waitForResponse(response => response.request().method() === "GET" && response.url().includes("/api/finance/transactions?"));
  await page.goto("/carteira/transacoes");
  await expect(page.getByRole("heading", { name: "Transações" })).toBeVisible();
  const initialResponse = await initialHistory;
  expect(initialResponse.status()).toBe(200);
  expect((await initialResponse.json()).items).toEqual([]);

  await launch(page, "Depositar");
  const depositDialog = page.getByRole("dialog");
  await depositDialog.getByLabel("Valor").fill("1000,00");
  await completeDialog(page, /Depósito realizado com sucesso/);

  await page.route("**/api/finance/market/**", route => route.fulfill({
    status: 502,
    contentType: "application/problem+json",
    body: JSON.stringify({ status: 502, title: "Provider de mercado indisponível" }),
  }));
  await launch(page, "Comprar");
  const dialog = page.getByRole("dialog");
  await dialog.getByRole("option", { name: /QAEX1/ }).click();
  await dialog.getByRole("option", { name: /QA Ledger Broker/ }).click();
  await dialog.getByLabel("Quantidade").fill("5");
  await dialog.getByLabel(/Preço unitário/).fill("42,73");
  await expect(dialog.getByText(/Preço médio estimado/)).toHaveCount(0);

  const committedHistory = page.waitForResponse(response => response.request().method() === "GET" && response.url().includes("/api/finance/transactions?"));
  await dialog.getByRole("button", { name: "Revisar" }).click();
  await dialog.getByRole("button", { name: "Confirmar operação" }).click();
  await expect(dialog.getByText("Compra registrada com sucesso")).toBeVisible();
  const refetched = await committedHistory;
  expect(refetched.status()).toBe(200);
  expect((await refetched.json()).items[0]).toMatchObject({
    ticker: "QAEX1", tipo: "BUY", quantidade: "5.00000000", precoUnitario: "42.73000000",
    exchangeRateId: null, resultadoRealizadoBrl: null, logoProvider: null, logoReference: null,
  });
  await dialog.getByRole("button", { name: "Concluir" }).click();

  await page.goto("/carteira/movimentacoes");
  const returnedHistory = page.waitForResponse(response => response.request().method() === "GET" && response.url().includes("/api/finance/transactions?"));
  await page.goto("/carteira/transacoes");
  const returnedResponse = await returnedHistory;
  expect(returnedResponse.status()).toBe(200);
  await expect(page.locator("tbody tr").first()).toContainText("QAEX1");
  await expect(page.locator("tbody tr").first()).toContainText("Compra");
  await expect(page.locator("tbody tr").first()).toContainText("5");
  await expect(page.locator("tbody tr").first()).toContainText("R$ 42,73");
  await expect(page.locator(".transactions-page [role=alert]")).toHaveCount(0);
  expect(historyResponses).not.toContain(404);
  expect(historyResponses).not.toContain(502);
});
