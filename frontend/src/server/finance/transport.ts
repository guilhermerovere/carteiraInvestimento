import "server-only";
import { cookies } from "next/headers";
import { AUTH_SESSION_COOKIE } from "@/server/auth/cookie";
import { loadAuthConfig } from "@/server/auth/config";
import { parseLosslessJson } from "./lossless";
import { safeCorrelationId } from "./validation";

export type FinanceProblem = { status: number; title: string; detail?: string; instance?: string };

function safeText(value: unknown): string | undefined {
  if (typeof value !== "string") return undefined;
  return value
    .replace(/Bearer\s+[^\s,;]+/gi, "Bearer [redacted]")
    .replace(/((?:access|refresh)[_-]?token|authorization|senha|password|credential|hash|cookie)\s*[:=]\s*[^\s,;]+/gi, "$1=[redacted]")
    .replace(/https?:\/\/[^\s,;]+/gi, "[redacted-url]");
}

export class FinanceHttpError extends Error {
  constructor(readonly status: number, readonly problem: FinanceProblem, readonly correlationId?: string) {
    super(problem.detail ?? problem.title);
  }
}
export class FinanceNetworkError extends Error {
  constructor() { super("Serviço financeiro indisponível."); }
}

function safeProblem(value: unknown, status: number): FinanceProblem {
  if (!value || typeof value !== "object") return { status, title: "Não foi possível concluir a consulta financeira." };
  const d = value as Record<string, unknown>;
  return {
    status,
    title: safeText(d.title) ?? "Não foi possível concluir a consulta financeira.",
    detail: safeText(d.detail),
    instance: safeText(d.instance),
  };
}

export async function financeBackendRequest(
  path: string,
  options: { method?: "GET" | "POST"; correlationId?: string } = {},
): Promise<{ data: unknown; correlationId?: string }> {
  const token = (await cookies()).get(AUTH_SESSION_COOKIE)?.value;
  if (!token) throw new FinanceHttpError(401, { status: 401, title: "Sessão necessária." });
  const headers = new Headers({ Accept: "application/json", Authorization: "Bearer " + token });
  const incoming = safeCorrelationId(options.correlationId ?? null);
  if (incoming) headers.set("X-Correlation-ID", incoming);
  let response: Response;
  try {
    response = await fetch(new URL(path, loadAuthConfig().backendApiUrl), {
      method: options.method ?? "GET", headers, cache: "no-store",
    });
  } catch {
    throw new FinanceNetworkError();
  }
  const responseCorrelation = safeCorrelationId(response.headers.get("x-correlation-id"));
  const raw = await response.text();
  if (!response.ok) {
    let problem: unknown;
    try { problem = raw ? JSON.parse(raw) : undefined; } catch { problem = undefined; }
    throw new FinanceHttpError(response.status, safeProblem(problem, response.status), responseCorrelation);
  }
  try {
    return { data: parseLosslessJson(raw), correlationId: responseCorrelation };
  } catch {
    throw new FinanceHttpError(502, { status: 502, title: "Resposta financeira inválida." }, responseCorrelation);
  }
}
