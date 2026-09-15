import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { enrichPositionsByAssetId } from "@/lib/finance/enrichment";
import { decimalSign, formatAveragePrice, formatCurrency, formatDateTime, formatMoney, formatPercentage, formatQuantity } from "@/lib/finance/format";
import { assetId, position, secondAssetId, summary } from "@/test/finance-fixtures";
import { parseLosslessJson } from "./lossless";
import { mapPage, mapPosition, mapSummary, mapTransaction } from "./mappers";
import { FinanceValidationError, assertEmptyBody, assertNoQuery, assertUuid, safeCorrelationId, strictPagination } from "./validation";

describe("normalização financeira lossless", () => {
  it("preserva tokens BigDecimal representativos sem passar por Number", () => {
    const parsed = parseLosslessJson('{"a":0.10000001,"b":0.00000001,"c":123456789.12345678,"d":-123456789.12345678}') as Record<string, unknown>;
    expect(parsed).toEqual({ a: "0.10000001", b: "0.00000001", c: "123456789.12345678", d: "-123456789.12345678" });
  });

  it("normaliza BigDecimal em notacao cientifica para decimal exato no BFF", () => {
    const response = parseLosslessJson('{"id":"44444444-4444-4444-8444-444444444444","ativoId":"11111111-1111-4111-8111-111111111111","ticker":"ACME3","corretoraId":"55555555-5555-4555-8555-555555555555","exchangeRateId":null,"tipo":"BUY","quantidade":5.00000000,"moeda":"BRL","precoUnitario":4.273E+1,"taxas":0E-8,"taxaCambioBrl":1E+0,"valorTotalBrl":2.1365E+2,"resultadoRealizadoBrl":null,"dataNegociacao":"2026-09-11T15:00:00Z","dataRegistro":"2026-09-11T15:01:00Z"}');
    const transaction = mapTransaction(response);
    expect(transaction.quantidade).toBe("5.00000000");
    expect(transaction.precoUnitario).toBe("42.73");
    expect(transaction.taxas).toBe("0.00000000");
    expect(transaction.taxaCambioBrl).toBe("1");
    expect(transaction.valorTotalBrl).toBe("213.65");
    expect(transaction.resultadoRealizadoBrl).toBeNull();
  });

  it("mapeia todos os decimais do resumo e da custódia como strings", () => {
    const mappedSummary = mapSummary(summary);
    const mappedPosition = mapPosition(position);
    expect(mappedSummary.patrimonioTotalBrl).toBe("125001200.22345679");
    expect(mappedSummary.posicoes[0].quantidade).toBe("0.10000001");
    expect(mappedSummary.cambioAtual?.taxaCambioBrl).toBe("5.12345678");
    expect(mappedPosition.lucroRealizadoAcumuladoBrl).toBe("-123456789.12345678");
    expect(typeof mappedSummary.patrimonioTotalBrl).toBe("string");
  });

  it("só converte contadores paginados depois de validar inteiros seguros", () => {
    expect(mapPage({ items: [position], page: "0", size: "20", totalElements: "1", totalPages: "1" }, mapPosition).page).toBe(0);
    expect(() => mapPage({ items: [], page: "9007199254740992", size: "20", totalElements: "0", totalPages: "0" }, mapPosition)).toThrow(/inseguro/i);
    expect(() => mapPage({ items: [], page: "0.1", size: "20", totalElements: "0", totalPages: "0" }, mapPosition)).toThrow(/inválido/i);
  });
});

describe("validação estrita do BFF", () => {
  it("aceita somente page e size dentro do contrato", () => {
    expect(strictPagination(new Request("https://app.test/api/finance/positions?page=2&size=100"))).toEqual({ page: 2, size: 100 });
    for (const url of [
      "https://app.test/api/finance/positions?sort=ticker",
      "https://app.test/api/finance/positions?page=0&page=1",
      "https://app.test/api/finance/positions?page=-1",
      "https://app.test/api/finance/positions?size=101",
      "https://app.test/api/finance/positions?page=9007199254740992",
    ]) expect(() => strictPagination(new Request(url))).toThrow(FinanceValidationError);
  });

  it("rejeita query, corpo e UUID inválidos e filtra correlation ID", async () => {
    expect(() => assertNoQuery(new Request("https://app.test/path?x=1"))).toThrow(FinanceValidationError);
    await expect(assertEmptyBody(new Request("https://app.test/path", { method: "POST", body: "{}" }))).rejects.toThrow(FinanceValidationError);
    expect(() => assertUuid("../segredo")).toThrow(FinanceValidationError);
    expect(safeCorrelationId("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")).toBeTruthy();
    expect(safeCorrelationId("Bearer segredo")).toBeUndefined();
  });
});

describe("formatação e enriquecimento", () => {
  it("formata valores arbitrários diretamente da string canônica", () => {
    expect(formatMoney("123456789.12345678")).toBe("R$ 123.456.789,12");
    expect(formatMoney("-123456789.12345678")).toBe("- R$ 123.456.789,12");
    expect(formatPercentage("-0.10000001")).toBe("-0,10000001%");
    expect(formatQuantity("5.00000000")).toBe("5");
    expect(formatQuantity("2.00000000")).toBe("2");
    expect(formatCurrency("42.73000000", "BRL")).toBe("R$ 42,73");
    expect(formatCurrency("42.73000000", "USD")).toBe("US$ 42,73");
    expect(formatAveragePrice("23.33333333")).toBe("R$ 23,3333");
    expect(decimalSign("-0.00000001")).toBe("negative");
    expect(formatDateTime("2026-09-12T13:30:00Z")).toMatch(/12\/09\/2026/);
  });

  it("enriquece exclusivamente por ativoId e mantém valuation ausente", () => {
    const mismatched = { ...position, id: "77777777-7777-4777-8777-777777777777", ativoId: secondAssetId, ticker: summary.posicoes[0].ticker };
    const result = enrichPositionsByAssetId([position, mismatched], summary);
    expect(result[0].valuation?.ativoId).toBe(assetId);
    expect(result[1].valuation).toBeNull();
  });

  it("não usa conversão Number genérica nem expõe segredos no cliente", () => {
    const formatSource = readFileSync(join(process.cwd(), "src/lib/finance/format.ts"), "utf8");
    const clientSource = ["src/client/finance-api.ts", "src/client/portfolio-queries.ts"].map((path) => readFileSync(join(process.cwd(), path), "utf8")).join("\n");
    expect(formatSource).not.toMatch(/Number\s*\(\s*value\s*\)/);
    expect(clientSource).not.toMatch(/BACKEND_API_URL|Authorization|auth_session|server\/finance|server\/auth/i);
    expect(clientSource).toContain("/api/finance/");
  });
});
