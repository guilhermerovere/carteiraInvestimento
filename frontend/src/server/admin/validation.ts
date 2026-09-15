import "server-only";
import { FinanceValidationError, assertEnum, strictObject } from "@/server/finance/validation";

function optionalText(value: unknown, max: number): string | null | undefined {
  if (value === undefined || value === null) return value;
  if (typeof value !== "string" || value.length > max) throw new FinanceValidationError("Campo de texto invalido.");
  return value;
}
function requiredText(value: unknown, max: number): string {
  if (typeof value !== "string" || !value.trim() || value.length > max) throw new FinanceValidationError("Campo obrigatorio invalido.");
  return value.trim();
}
export function brokerCreateBody(value: unknown): string {
  const body = strictObject(value, ["cnpj"], ["numero", "complemento"]);
  const cnpj = requiredText(body.cnpj, 18);
  return JSON.stringify({ cnpj, ...(body.numero !== undefined ? { numero: optionalText(body.numero, 20) } : {}), ...(body.complemento !== undefined ? { complemento: optionalText(body.complemento, 160) } : {}) });
}
export function brokerEditBody(value: unknown): string {
  const body = strictObject(value, [], ["numero", "complemento"]);
  if (Object.keys(body).length === 0) throw new FinanceValidationError("Informe numero ou complemento.");
  return JSON.stringify({ ...(body.numero !== undefined ? { numero: optionalText(body.numero, 20) } : {}), ...(body.complemento !== undefined ? { complemento: optionalText(body.complemento, 160) } : {}) });
}
export function lifecycleBody(value: unknown): string {
  const body = strictObject(value, ["ativo"]);
  if (typeof body.ativo !== "boolean") throw new FinanceValidationError("Estado invalido.");
  return JSON.stringify({ ativo: body.ativo });
}
export function assetCreateBody(value: unknown): string {
  const body = value && typeof value === "object" ? value as Record<string, unknown> : null;
  if (body && Object.keys(body).length === 2 && "ticker" in body && "mercado" in body) {
    return JSON.stringify({ ticker: requiredText(body.ticker, 6), mercado: assertEnum(body.mercado, ["B3", "US"]) });
  }
  const legacy = strictObject(value, ["ticker", "nome", "tipo", "mercado"]);
  return JSON.stringify({ ticker: requiredText(legacy.ticker, 6), nome: requiredText(legacy.nome, 160), tipo: assertEnum(legacy.tipo, ["ACAO", "FII", "ETF"]), mercado: assertEnum(legacy.mercado, ["B3", "US"]) });
}
export function assetEditBody(value: unknown): string {
  const body = strictObject(value, ["nome"]);
  return JSON.stringify({ nome: requiredText(body.nome, 160) });
}

export function adminListQuery(request: Request, kind: "brokers" | "assets"): string {
  const params = new URL(request.url).searchParams;
  const allowed = kind === "brokers" ? new Set(["page", "size"]) : new Set(["page", "size", "q", "tipo", "ativo", "sort", "direction"]);
  for (const key of params.keys()) if (!allowed.has(key)) throw new FinanceValidationError("Parametro de consulta nao permitido.");
  const page = params.get("page") ?? "0"; const size = params.get("size") ?? "20";
  if (!/^\d+$/.test(page) || !/^\d+$/.test(size) || Number(size) < 1 || Number(size) > 100) throw new FinanceValidationError("Paginacao invalida.");
  const output = new URLSearchParams(params); output.set("page", page); output.set("size", size);
  return "?" + output.toString();
}
