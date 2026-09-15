import type { CurrentUser, LoginInput, RegisterInput } from "@/lib/auth-contracts";

export type { CurrentUser, LoginInput, RegisterInput, Role } from "@/lib/auth-contracts";
export type RegisteredUser = CurrentUser;
export type LoginResult = { accessToken: string; tokenType: string; expiresIn: number };
export type ProblemDetail = { status?: number; title?: string; detail?: string; instance?: string };
export type BackendErrorKind = "problem-detail" | "http";

function safeText(value: unknown): string | undefined {
  if (typeof value !== "string") return undefined;
  return value
    .replace(/Bearer\s+[^\s,;]+/gi, "Bearer [redacted]")
    .replace(/((?:access|refresh)[_-]?token|authorization|senha|password|credential|hash|cookie)\s*[:=]\s*[^\s,;]+/gi, "$1=[redacted]")
    .replace(/https?:\/\/[^\s,;]+/gi, "[redacted-url]");
}

export class BackendHttpError extends Error {
  constructor(public readonly status: number, public readonly problem: ProblemDetail, public readonly correlationId?: string, public readonly kind: BackendErrorKind = "http") { super(problem.detail ?? problem.title ?? "Erro no serviço de autenticação"); }
}
export class BackendNetworkError extends Error {
  readonly kind = "network" as const;
  constructor() { super("Serviço de autenticação indisponível"); }
}

export function parseLoginResult(value: unknown): LoginResult {
  if (!value || typeof value !== "object") {
    throw new BackendHttpError(502, { status: 502, title: "Resposta inválida do serviço de autenticação" });
  }

  const data = value as Record<string, unknown>;
  if (
    typeof data.accessToken !== "string" ||
    !data.accessToken ||
    typeof data.tokenType !== "string" ||
    !data.tokenType ||
    typeof data.expiresIn !== "number" ||
    !Number.isFinite(data.expiresIn) ||
    data.expiresIn <= 0
  ) {
    throw new BackendHttpError(502, { status: 502, title: "Resposta inválida do serviço de autenticação" });
  }

  return { accessToken: data.accessToken, tokenType: data.tokenType, expiresIn: data.expiresIn };
}

function requiredString(value: unknown): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new BackendHttpError(400, { status: 400, title: "Requisição de autenticação inválida" });
  }
  return value;
}

export function loginInputFrom(value: unknown): LoginInput {
  const data = value && typeof value === "object" ? value as Record<string, unknown> : {};
  return { email: requiredString(data.email), senha: requiredString(data.senha) };
}

export function registerInputFrom(value: unknown): RegisterInput {
  const data = value && typeof value === "object" ? value as Record<string, unknown> : {};
  return { nome: requiredString(data.nome), email: requiredString(data.email), senha: requiredString(data.senha) };
}

export function safeProblem(value: unknown, fallbackStatus: number): ProblemDetail {
  if (!value || typeof value !== "object") return { status: fallbackStatus, title: "Erro de autenticação" };
  const data = value as Record<string, unknown>;
  return {
    status: typeof data.status === "number" ? data.status : fallbackStatus,
    title: safeText(data.title),
    detail: safeText(data.detail),
    instance: safeText(data.instance),
  };
}

export async function parseBackendError(response: Response): Promise<BackendHttpError> {
  const correlationId = response.headers.get("x-correlation-id") ?? undefined;
  const isProblemDetail = response.headers.get("content-type")?.toLowerCase().includes("application/problem+json") === true;
  if (isProblemDetail) {
    try {
      const body = await response.json() as unknown;
      if (body && typeof body === "object") {
        return new BackendHttpError(response.status, safeProblem(body, response.status), correlationId, "problem-detail");
      }
    } catch {
      // Invalid ProblemDetail bodies are intentionally reduced to a generic HTTP error.
    }
  }
  return new BackendHttpError(response.status, { status: response.status, title: "Erro HTTP no serviço de autenticação" }, correlationId, "http");
}
