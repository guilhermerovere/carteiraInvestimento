import "server-only";

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

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
