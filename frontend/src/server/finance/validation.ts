import "server-only";

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const OFFSET_DATE_TIME = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,6})?(?:Z|[+-]\d{2}:\d{2})$/;

export class FinanceValidationError extends Error {
  readonly status = 400;
  constructor(message = "Requisição financeira inválida.") { super(message); }
}

function parseSafeInteger(raw: string, name: string): number {
  if (!/^(0|[1-9]\d*)$/.test(raw)) throw new FinanceValidationError(name + " deve ser um número inteiro não negativo.");
  const value = Number(raw);
  if (!Number.isFinite(value) || !Number.isSafeInteger(value) || value < 0) {
    throw new FinanceValidationError(name + " deve ser um número inteiro seguro.");
  }
  return value;
}

export function strictPagination(request: Request): { page: number; size: number } {
  const params = new URL(request.url).searchParams;
  for (const key of params.keys()) {
    if (key !== "page" && key !== "size") throw new FinanceValidationError("Parâmetro de consulta não permitido.");
  }
  if (params.getAll("page").length > 1 || params.getAll("size").length > 1) {
    throw new FinanceValidationError("Parâmetro de paginação duplicado.");
  }
  const page = parseSafeInteger(params.get("page") ?? "0", "page");
  const size = parseSafeInteger(params.get("size") ?? "20", "size");
  if (size < 1 || size > 100) throw new FinanceValidationError("size deve estar entre 1 e 100.");
  return { page, size };
}

export function assertNoQuery(request: Request): void {
  if ([...new URL(request.url).searchParams.keys()].length > 0) {
    throw new FinanceValidationError("Parâmetros de consulta não são permitidos.");
  }
}

export async function assertEmptyBody(request: Request): Promise<void> {
  if ((await request.text()).length > 0) throw new FinanceValidationError("Corpo não é permitido.");
}

export function assertUuid(value: string): string {
  if (!UUID.test(value)) throw new FinanceValidationError("Identificador inválido.");
  return value;
}

export function safeCorrelationId(value: string | null): string | undefined {
  return value && UUID.test(value) ? value : undefined;
}

export function assertIdempotencyKey(value: string | null): string {
  if (!value || value.length > 128 || !UUID.test(value)) throw new FinanceValidationError("Idempotency-Key inválida.");
  return value;
}
export function assertDecimal(value: unknown, precision: number, scale: number, positive = false): string {
  if (typeof value !== "string" || !/^(?:0|[1-9]\d*)(?:\.\d+)?$/.test(value)) throw new FinanceValidationError("Decimal inválido.");
  const [whole, fraction = ""] = value.split(".");
  if (whole.length + fraction.length > precision || fraction.length > scale || (positive && /^0(?:\.0+)?$/.test(value))) throw new FinanceValidationError("Precisão ou escala inválida.");
  return value;
}
export function assertOffsetDateTime(value: unknown): string {
  if (typeof value !== "string" || !OFFSET_DATE_TIME.test(value) || Number.isNaN(Date.parse(value))) throw new FinanceValidationError("Data e hora com offset inválidas.");
  return value;
}
export function strictObject(value: unknown, required: readonly string[], optional: readonly string[] = []): Record<string, unknown> {
  if (!value || typeof value !== "object" || Array.isArray(value)) throw new FinanceValidationError();
  const object = value as Record<string, unknown>; const allowed = new Set([...required, ...optional]);
  if (required.some((key) => !(key in object)) || Object.keys(object).some((key) => !allowed.has(key))) throw new FinanceValidationError("Campos inválidos.");
  return object;
}
export async function strictJson(request: Request): Promise<unknown> { try { return JSON.parse(await request.text()); } catch { throw new FinanceValidationError("JSON inválido."); } }
export function assertEnum<T extends string>(value: unknown, allowed: readonly T[]): T { if (typeof value !== "string" || !allowed.includes(value as T)) throw new FinanceValidationError("Enum inválido."); return value as T; }
