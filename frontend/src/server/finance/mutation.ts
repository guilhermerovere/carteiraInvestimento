import "server-only";
import { hasValidOrigin } from "@/server/auth/origin";
import { stringifyExactJson } from "./lossless";
import { FinanceValidationError, assertDecimal, assertEnum, assertIdempotencyKey, assertOffsetDateTime, assertUuid, strictObject } from "./validation";

export function assertMutationRequest(request: Request): string {
  if (!hasValidOrigin(request.headers.get("origin"))) throw new FinanceValidationError("Origin inválida.");
  return assertIdempotencyKey(request.headers.get("idempotency-key"));
}
export function cashBody(value: unknown): string {
  const body = strictObject(value, ["valor"], ["descricao"]);
  const descricao = body.descricao;
  if (descricao !== undefined && (typeof descricao !== "string" || descricao.length > 255)) throw new FinanceValidationError("Descrição inválida.");
  return stringifyExactJson({ valor: assertDecimal(body.valor, 18, 2, true), ...(descricao ? { descricao: descricao.trim() } : {}) }, new Set(["valor"]));
}
export function transactionBody(value: unknown): string {
  const fields = ["ativoId", "corretoraId", "tipo", "quantidade", "precoUnitario", "taxas", "dataNegociacao", "exchangeRateId"] as const;
  const body = strictObject(value, fields);
  const exchangeRateId = body.exchangeRateId;
  if (exchangeRateId !== null) assertUuid(String(exchangeRateId));
  return stringifyExactJson({
    ativoId: assertUuid(String(body.ativoId)), corretoraId: assertUuid(String(body.corretoraId)),
    tipo: assertEnum(body.tipo, ["BUY", "SELL"]), quantidade: assertDecimal(body.quantidade, 18, 0, true),
    precoUnitario: assertDecimal(body.precoUnitario, 18, 8, true), taxas: assertDecimal(body.taxas, 18, 8),
    dataNegociacao: assertOffsetDateTime(body.dataNegociacao), exchangeRateId,
  }, new Set(["quantidade", "precoUnitario", "taxas"]));
}
export function assetBody(value: unknown): string {
  const body = strictObject(value, ["ticker", "mercado"]);
  if (typeof body.ticker !== "string" || !/^[A-Za-z0-9.\-]{1,16}$/.test(body.ticker.trim())) throw new FinanceValidationError("Ticker inválido.");
  return JSON.stringify({ ticker: body.ticker.trim(), mercado: assertEnum(body.mercado, ["B3", "US"]) });
}
