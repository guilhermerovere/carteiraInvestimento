import { FinanceApiError } from "@/client/finance-api";
import type { CurrentUser } from "@/lib/auth-contracts";
import type { LogoProvider, Page, SafeProblemCode, SafeProblemDetail } from "@/lib/finance/contracts";

export type AdminBroker = { id: string; cnpj: string; razaoSocial: string; nomeFantasia: string | null; cep: string; logradouro: string; bairro: string; cidade: string; uf: string; numero: string | null; complemento: string | null; ativo: boolean; logoProvider: LogoProvider | null; logoReference: string | null; criadoEm: string; atualizadoEm: string };
export type AdminAsset = { id: string; ticker: string; nome: string; tipo: "ACAO" | "FII" | "ETF"; mercado: "B3" | "US"; moeda: "BRL" | "USD"; ativo: boolean; logoProvider: LogoProvider | null; logoReference: string | null; criadoEm: string; atualizadoEm: string };

function safeProblem(value: unknown, status: number): SafeProblemDetail {
  const data = value && typeof value === "object" ? value as Record<string, unknown> : {};
  const code = ["FX_EXPIRED", "EMAIL_IN_USE", "CURRENT_PASSWORD_INCORRECT", "CASH_NOT_ZERO", "OPEN_POSITIONS", "BROKER_CNPJ_INVALID", "BROKER_ALREADY_REGISTERED", "BROKER_CVM_NOT_REGISTERED", "BROKER_CVM_NOT_APPROVED", "BROKER_VALIDATION_UNAVAILABLE"].includes(String(data.code)) ? data.code as SafeProblemCode : undefined;
  return { status, title: typeof data.title === "string" ? data.title : "Solicitacao indisponivel", detail: typeof data.detail === "string" ? data.detail : undefined, code };
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response;
  try { response = await fetch(path, { ...init, cache: "no-store", credentials: "same-origin", headers: { Accept: "application/json", ...(init.body ? { "Content-Type": "application/json" } : {}), ...init.headers } }); }
  catch { throw new FinanceApiError(0, { status: 0, title: "Sem conexao." }); }
  if (!response.ok) {
    let body: unknown; try { body = await response.json(); } catch { body = undefined; }
    throw new FinanceApiError(response.status, safeProblem(body, response.status), response.headers.get("x-correlation-id") ?? undefined);
  }
  return response.status === 204 ? undefined as T : await response.json() as T;
}

export const accountApi = {
  updateName: (nome: string) => request<CurrentUser>("/api/account/profile", { method: "PATCH", body: JSON.stringify({ nome }) }),
  updateEmail: (email: string) => request<CurrentUser>("/api/account/profile", { method: "PATCH", body: JSON.stringify({ email }) }),
  changePassword: (senhaAtual: string, novaSenha: string) => request<void>("/api/account/password", { method: "POST", body: JSON.stringify({ senhaAtual, novaSenha }) }),
  close: (senhaAtual: string, confirmacao: string) => request<void>("/api/account/closure", { method: "POST", body: JSON.stringify({ senhaAtual, confirmacao }) }),
};

export const adminApi = {
  brokers: () => request<Page<AdminBroker>>("/api/admin/brokers?page=0&size=100"),
  createBroker: (body: { cnpj: string; numero?: string | null; complemento?: string | null }) => request<AdminBroker>("/api/admin/brokers", { method: "POST", body: JSON.stringify(body) }),
  editBroker: (id: string, body: { numero?: string | null; complemento?: string | null }) => request<AdminBroker>(`/api/admin/brokers/${encodeURIComponent(id)}`, { method: "PATCH", body: JSON.stringify(body) }),
  setBrokerActive: (id: string, ativo: boolean) => request<AdminBroker>(`/api/admin/brokers/${encodeURIComponent(id)}/lifecycle`, { method: "PATCH", body: JSON.stringify({ ativo }) }),
  assets: () => request<Page<AdminAsset>>("/api/admin/assets?page=0&size=100&sort=ticker&direction=asc"),
  createAsset: (body: { ticker: string; mercado: AdminAsset["mercado"] } | { ticker: string; nome: string; tipo: AdminAsset["tipo"]; mercado: AdminAsset["mercado"] }) => request<AdminAsset>("/api/admin/assets", { method: "POST", body: JSON.stringify(body) }),
  editAsset: (id: string, nome: string) => request<AdminAsset>(`/api/admin/assets/${encodeURIComponent(id)}`, { method: "PATCH", body: JSON.stringify({ nome }) }),
  setAssetActive: (id: string, ativo: boolean) => request<AdminAsset>(`/api/admin/assets/${encodeURIComponent(id)}/lifecycle`, { method: "PATCH", body: JSON.stringify({ ativo }) }),
};
