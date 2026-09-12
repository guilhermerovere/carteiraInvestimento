import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { enrichPositionsByAssetId } from "@/lib/finance/enrichment";
import { decimalSign, formatDateTime, formatMoney, formatPercentage, formatQuantity } from "@/lib/finance/format";
import { assetId, position, secondAssetId, summary } from "@/test/finance-fixtures";
import { parseLosslessJson } from "./lossless";
import { mapPage, mapPosition, mapSummary } from "./mappers";
import { FinanceValidationError, assertEmptyBody, assertNoQuery, assertUuid, safeCorrelationId, strictPagination } from "./validation";

describe("normalização financeira lossless", () => {
  it("preserva tokens BigDecimal representativos sem passar por Number", () => {
    const parsed = parseLosslessJson('{"a":0.10000001,"b":0.00000001,"c":123456789.12345678,"d":-123456789.12345678}') as Record<string, unknown>;
    expect(parsed).toEqual({ a: "0.10000001", b: "0.00000001", c: "123456789.12345678", d: "-123456789.12345678" });
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
    expect(formatMoney("123456789.12345678")).toBe("R$ 123.456.789,12345678");
    expect(formatMoney("-123456789.12345678")).toBe("- R$ 123.456.789,12345678");
    expect(formatPercentage("-0.10000001")).toBe("-0,10000001%");
    expect(formatQuantity("0.00000001")).toBe("0,00000001");
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
