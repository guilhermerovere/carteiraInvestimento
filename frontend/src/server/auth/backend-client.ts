import "server-only";
import { loadAuthConfig } from "./config";
import { BackendNetworkError, parseBackendError, parseLoginResult, type CurrentUser, type LoginInput, type LoginResult, type RegisteredUser, type RegisterInput } from "./types";

async function request<T>(path: string, init: RequestInit = {}, token?: string): Promise<T> {
  const url = new URL(path, loadAuthConfig().backendApiUrl).toString();
  const headers = new Headers(init.headers); headers.set("Accept", "application/json");
  if (init.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);
  let response: Response;
  try { response = await fetch(url, { ...init, headers, cache: "no-store" }); } catch { throw new BackendNetworkError(); }
  if (!response.ok) {
    throw await parseBackendError(response);
  }
  return response.status === 204 ? (undefined as T) : response.json() as Promise<T>;
}
export const backendClient = {
  login: async (input: LoginInput): Promise<LoginResult> => parseLoginResult(await request<unknown>("/api/v1/auth/login", { method: "POST", body: JSON.stringify(input) })),
  register: (input: RegisterInput) => request<RegisteredUser>("/api/v1/auth/register", { method: "POST", body: JSON.stringify(input) }),
  me: (token: string) => request<CurrentUser>("/api/v1/auth/me", { method: "GET" }, token),
};
