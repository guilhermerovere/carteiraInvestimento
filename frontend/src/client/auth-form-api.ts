import type { CurrentUser, LoginInput, RegisterInput } from "@/lib/auth-contracts";

export type AuthProblem = { status?: number; title?: string; detail?: string; instance?: string };
export type { CurrentUser, LoginInput, RegisterInput } from "@/lib/auth-contracts";

export class AuthFormError extends Error {
  constructor(public readonly kind: "http" | "network", public readonly status?: number, public readonly problem?: AuthProblem, public readonly correlationId?: string) {
    super(problem?.detail ?? problem?.title ?? "Não foi possível concluir a solicitação.");
  }
}

function asProblem(value: unknown): AuthProblem | undefined {
  if (!value || typeof value !== "object") return undefined;
  const data = value as Record<string, unknown>;
  return {
    status: typeof data.status === "number" ? data.status : undefined,
    title: typeof data.title === "string" ? data.title : undefined,
    detail: typeof data.detail === "string" ? data.detail : undefined,
    instance: typeof data.instance === "string" ? data.instance : undefined,
  };
}

async function problemFrom(response: Response): Promise<AuthProblem | undefined> {
  if (!response.headers.get("content-type")?.includes("json")) return undefined;
  try { return asProblem(await response.json()); } catch { return undefined; }
}

async function request<T>(path: "/api/auth/login" | "/api/auth/register" | "/api/auth/me" | "/api/auth/logout", init: RequestInit, expectedStatus: number): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, { ...init, credentials: "same-origin", cache: "no-store", headers: { Accept: "application/json", ...(init.body ? { "Content-Type": "application/json" } : {}), ...init.headers } });
  } catch {
    throw new AuthFormError("network");
  }
  if (response.status !== expectedStatus) throw new AuthFormError("http", response.status, await problemFrom(response), response.headers.get("x-correlation-id") ?? undefined);
  if (response.status !== 200) return undefined as T;
  return await response.json() as T;
}

export async function login(input: LoginInput): Promise<void> {
  await request<void>("/api/auth/login", { method: "POST", body: JSON.stringify(input) }, 204);
}

export async function getCurrentUser(): Promise<CurrentUser> {
  return request<CurrentUser>("/api/auth/me", { method: "GET" }, 200);
}

export async function loginAndConfirm(input: LoginInput): Promise<CurrentUser> {
  await login(input);
  return getCurrentUser();
}

export async function register(input: RegisterInput): Promise<void> {
  await request<void>("/api/auth/register", { method: "POST", body: JSON.stringify(input) }, 201);
}

export async function logout(): Promise<void> {
  await request<void>("/api/auth/logout", { method: "POST" }, 204);
}

export function isUnauthorized(error: unknown): error is AuthFormError {
  return error instanceof AuthFormError && error.status === 401;
}

export function authErrorMessage(error: unknown, action: "login" | "register"): string {
  if (!(error instanceof AuthFormError)) return "Não foi possível concluir a solicitação. Tente novamente.";
  if (error.kind === "network") return "Não foi possível conectar ao serviço. Verifique sua conexão e tente novamente.";
  if (error.status === 400) return "Revise os campos informados.";
  if (error.status === 401 && action === "login") return "E-mail ou senha incorretos.";
  if (error.status === 403) return "Você não tem permissão para realizar esta ação.";
  if (error.status === 409 && action === "register") return "Este email já está em uso.";
  if (error.status === 429) return "Muitas tentativas em pouco tempo. Aguarde e tente novamente.";
  if (error.status === 502 || error.status === 503) return "O serviço está temporariamente indisponível. Tente novamente em alguns instantes.";
  return action === "login" ? "Não foi possível iniciar a sessão." : "Não foi possível criar a conta.";
}
